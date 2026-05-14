package com.knoxhack.echorendercore.profile;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.Identifier;

public final class RenderCoreProfileMigration {
   private RenderCoreProfileMigration() {
   }

   public static boolean requiresMigration(VisualProfile profile) {
      return profile == null || profile.schemaVersion() != VisualProfile.CURRENT_SCHEMA_VERSION;
   }

   public static ProfileValidationIssue migrationRequiredIssue(Identifier id, int sourceSchemaVersion) {
      return migrationRequiredIssue(ProfileValidationSeverity.ERROR, id, sourceSchemaVersion);
   }

   public static ProfileValidationIssue migrationRequiredIssue(ProfileValidationSeverity severity, Identifier id, int sourceSchemaVersion) {
      return new ProfileValidationIssue(
         severity,
         id,
         "migration_required",
         "schema_version",
         "Visual profile uses schema_version " + sourceSchemaVersion
            + "; RenderCore V11 runtime only activates schema_version " + VisualProfile.CURRENT_SCHEMA_VERSION + ".",
         "Run /rendercore creator migrate " + (id == null ? "all" : id.getNamespace())
            + " dryrun, then write the generated V11 profile JSON when the report looks correct."
      );
   }

   public static CreatorMigrationReport migrateVisualProfile(Identifier id, JsonObject source) {
      JsonObject safeSource = source == null ? new JsonObject() : source.deepCopy();
      int sourceSchema = RenderCoreJsonParsers.visualSchemaVersion(safeSource);
      ArrayList<String> changes = new ArrayList<>();
      ArrayList<ProfileValidationIssue> issues = new ArrayList<>();
      JsonObject migrated = safeSource.deepCopy();
      boolean migrationRequired = sourceSchema != VisualProfile.CURRENT_SCHEMA_VERSION;
      if (migrationRequired) {
         issues.add(migrationRequiredIssue(ProfileValidationSeverity.WARNING, id, sourceSchema));
         changes.add("schema_version " + sourceSchema + " -> " + VisualProfile.CURRENT_SCHEMA_VERSION);
      }
      if (migrated.has("schemaVersion")) {
         migrated.remove("schemaVersion");
         changes.add("removed camelCase schemaVersion alias");
      }
      migrated.addProperty("schema_version", VisualProfile.CURRENT_SCHEMA_VERSION);
      normalizeEffectContainer(migrated, "effect", changes);
      normalizeMaterials(migrated, changes);
      normalizeLayers(migrated, changes);
      if (sourceSchema < 8) {
         changes.add("effect defaults to none for pre-V8 content");
      }
      if (sourceSchema < 10) {
         changes.add("advanced bloom mask fields use V11 defaults");
      }
      return new CreatorMigrationReport(
         id,
         sourceSchema,
         VisualProfile.CURRENT_SCHEMA_VERSION,
         migrationRequired,
         true,
         changes,
         issues,
         suggestedPath(id),
         migrated
      );
   }

   public static JsonObject normalizedVisualProfileJson(VisualProfile profile) {
      if (profile == null) {
         JsonObject empty = new JsonObject();
         empty.addProperty("schema_version", VisualProfile.CURRENT_SCHEMA_VERSION);
         return empty;
      }
      VisualProfileBuilder builder = VisualProfileBuilder.create(profile.id())
         .schemaVersion(VisualProfile.CURRENT_SCHEMA_VERSION)
         .baseTexture(profile.baseTexture())
         .animationProfile(profile.animationProfile())
         .particleProfile(profile.particleProfile())
         .defaultState(profile.defaultState())
         .transitionSeconds(profile.transitionSeconds())
         .effect(profile.effect());
      profile.stateAnimations().forEach(builder::stateAnimation);
      profile.stateTextureVariants().forEach(builder::stateVariantTexture);
      profile.variantTextures().forEach(builder::variantTexture);
      profile.anchors().forEach((name, anchor) -> builder.anchor(name, anchor.offset()));
      profile.materials().values().stream()
         .sorted(java.util.Comparator.comparing(VisualMaterial::id))
         .forEach(builder::material);
      profile.blockParts().entrySet().stream()
         .sorted(Map.Entry.comparingByKey())
         .forEach(entry -> builder.blockPart(entry.getKey(), entry.getValue()));
      profile.includes().forEach(builder::include);
      profile.layers().forEach(builder::layer);
      return builder.toJson();
   }

   private static void normalizeMaterials(JsonObject migrated, ArrayList<String> changes) {
      JsonElement materials = migrated.get("materials");
      if (materials == null || !materials.isJsonObject()) {
         return;
      }
      for (Map.Entry<String, JsonElement> entry : materials.getAsJsonObject().entrySet()) {
         if (entry.getValue().isJsonObject()) {
            normalizeEffectContainer(entry.getValue().getAsJsonObject(), "materials." + entry.getKey(), changes);
         }
      }
   }

   private static void normalizeLayers(JsonObject migrated, ArrayList<String> changes) {
      JsonElement layers = migrated.get("layers");
      if (layers == null || !layers.isJsonArray()) {
         return;
      }
      int index = 0;
      for (JsonElement layer : layers.getAsJsonArray()) {
         if (layer.isJsonObject()) {
            normalizeEffectContainer(layer.getAsJsonObject(), "layers." + index, changes);
         }
         index++;
      }
   }

   private static void normalizeEffectContainer(JsonObject json, String path, ArrayList<String> changes) {
      if (json.has("effects") && !json.has("effect")) {
         json.add("effect", json.get("effects").deepCopy());
         changes.add("renamed " + path + ".effects to effect");
      }
      json.remove("effects");
   }

   private static String suggestedPath(Identifier id) {
      if (id == null) {
         return "generated/rendercore_migrations/unknown.visual_profile.json";
      }
      return "generated/rendercore_migrations/assets/" + id.getNamespace()
         + "/rendercore/visual_profiles/" + id.getPath() + ".json";
   }
}
