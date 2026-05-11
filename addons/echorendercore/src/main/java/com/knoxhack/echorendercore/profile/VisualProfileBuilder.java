package com.knoxhack.echorendercore.profile;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.knoxhack.echorendercore.api.VisualState;
import com.knoxhack.echorendercore.api.VisualVariant;
import java.util.Collection;
import net.minecraft.resources.Identifier;

public final class VisualProfileBuilder extends RenderCoreProfileBuilder<VisualProfileBuilder> {
   private VisualProfileBuilder(Identifier id) {
      super(id);
   }

   public static VisualProfileBuilder create(Identifier id) {
      return new VisualProfileBuilder(id);
   }

   public VisualProfileBuilder schemaVersion(int schemaVersion) {
      return add("schema_version", schemaVersion);
   }

   public VisualProfileBuilder baseTexture(Identifier texture) {
      return add("base_texture", texture);
   }

   public VisualProfileBuilder animationProfile(Identifier profile) {
      return add("animation_profile", profile);
   }

   public VisualProfileBuilder particleProfile(Identifier profile) {
      return add("particle_profile", profile);
   }

   public VisualProfileBuilder defaultState(VisualState state) {
      return add("default_state", state == null ? VisualState.IDLE.name() : state.name());
   }

   public VisualProfileBuilder transitionSeconds(float seconds) {
      return add("transition_seconds", seconds);
   }

   public VisualProfileBuilder stateAnimation(VisualState state, String clip) {
      JsonObject animations = object("state_animations");
      animations.addProperty(state.name(), clip);
      return this;
   }

   public VisualProfileBuilder stateVariantTexture(VisualState state, Identifier texture) {
      JsonObject textures = object("state_texture_variants");
      textures.addProperty(state.name(), texture.toString());
      return this;
   }

   public VisualProfileBuilder variantTexture(VisualVariant variant, Identifier texture) {
      JsonObject variants = object("variants");
      variants.addProperty(variant.id(), texture.toString());
      return this;
   }

   public VisualProfileBuilder material(VisualMaterial material) {
      JsonObject materials = object("materials");
      JsonObject value = new JsonObject();
      value.addProperty("color", color(material.color()));
      value.addProperty("alpha", material.alpha());
      value.addProperty("emissive", material.emissive());
      value.addProperty("blend_mode", material.blendMode().name().toLowerCase(java.util.Locale.ROOT));
      materials.add(material.id(), value);
      return this;
   }

   public VisualProfileBuilder layer(VisualLayerProfile layer) {
      JsonObject value = new JsonObject();
      value.addProperty("id", layer.id());
      value.addProperty("kind", layer.kind().name().toLowerCase(java.util.Locale.ROOT));
      if (layer.texture() != null) {
         value.addProperty("texture", layer.texture().toString());
      }
      value.addProperty("material", layer.material());
      value.add("states", names(layer.states()));
      value.add("variants", variants(layer.variants()));
      value.add("parts", strings(layer.partFilter()));
      value.addProperty("color", color(layer.color()));
      value.addProperty("alpha", layer.alpha());
      value.addProperty("emissive", layer.emissive());
      layers().add(value);
      return this;
   }

   public VisualProfileBuilder anchor(String name, RenderCoreVector offset) {
      object("anchors").add(name, vector(offset));
      return this;
   }

   public VisualProfileBuilder blockPart(String alias, BlockPartSelectorProfile selector) {
      JsonObject blockParts = object("block_parts");
      JsonObject value = new JsonObject();
      if (!selector.indices().isEmpty()) {
         value.add("indices", ints(selector.indices()));
      }
      if (!selector.directions().isEmpty()) {
         JsonArray directions = new JsonArray();
         selector.directions().forEach(direction -> directions.add(direction.getName()));
         value.add("directions", directions);
      }
      if (selector.materialFlags() != 0) {
         value.addProperty("material_flags", selector.materialFlags());
      }
      if (selector.ambientOcclusion() != null) {
         value.addProperty("ambient_occlusion", selector.ambientOcclusion());
      }
      if (!selector.tintIndices().isEmpty()) {
         value.add("tint_indices", ints(selector.tintIndices()));
      }
      if (!selector.blockState().isEmpty()) {
         JsonObject blockState = new JsonObject();
         selector.blockState().forEach((property, values) -> blockState.add(property, strings(values)));
         value.add("block_state", blockState);
      }
      blockParts.add(alias, value);
      return this;
   }

   public VisualProfileBuilder blockPart(String alias, int... indices) {
      java.util.ArrayList<Integer> values = new java.util.ArrayList<>();
      for (int index : indices) {
         values.add(index);
      }
      return blockPart(alias, new BlockPartSelectorProfile(alias, values, java.util.Set.of(), 0, null, java.util.List.of()));
   }

   private JsonObject object(String key) {
      if (!json.has(key) || !json.get(key).isJsonObject()) {
         json.add(key, new JsonObject());
      }
      return json.getAsJsonObject(key);
   }

   private JsonArray layers() {
      if (!json.has("layers") || !json.get("layers").isJsonArray()) {
         json.add("layers", new JsonArray());
      }
      return json.getAsJsonArray("layers");
   }

   private static JsonArray names(java.util.Set<VisualState> states) {
      JsonArray array = new JsonArray();
      states.forEach(state -> array.add(state.name()));
      return array;
   }

   private static JsonArray variants(java.util.Set<VisualVariant> variants) {
      JsonArray array = new JsonArray();
      variants.forEach(variant -> array.add(variant.id()));
      return array;
   }

   private static JsonArray strings(Collection<String> values) {
      JsonArray array = new JsonArray();
      values.forEach(array::add);
      return array;
   }

   private static JsonArray ints(java.util.List<Integer> values) {
      JsonArray array = new JsonArray();
      values.forEach(array::add);
      return array;
   }
}
