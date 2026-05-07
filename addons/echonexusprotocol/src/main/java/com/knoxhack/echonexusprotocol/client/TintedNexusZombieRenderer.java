package com.knoxhack.echonexusprotocol.client;

import com.knoxhack.echonexusprotocol.entity.CorruptionWardenEntity;
import com.knoxhack.echonexusprotocol.entity.ArchiveSeekerEntity;
import com.knoxhack.echonexusprotocol.entity.DataWraithEntity;
import com.knoxhack.echonexusprotocol.entity.NexusGuardianEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.zombie.Zombie;

public class TintedNexusZombieRenderer extends ZombieRenderer {
   private final Identifier texture; private final int tint; private final float scale; private final float shadow; private int liveTint; private float liveScale = 1.0F; private float liveYOffset = 0.0F;
   public TintedNexusZombieRenderer(EntityRendererProvider.Context context, Identifier texture, int tint, float scale, float shadow) { super(context); this.texture = texture; this.tint = tint; this.scale = scale; this.shadow = shadow; }
   public Identifier getTextureLocation(ZombieRenderState state) { return this.texture; } protected int getModelTint(ZombieRenderState state) { return this.liveTint == 0 ? this.tint : this.liveTint; } protected void scale(ZombieRenderState state, PoseStack poseStack) { poseStack.translate(0.0D, this.liveYOffset, 0.0D); poseStack.scale(this.scale * this.liveScale, this.scale * this.liveScale, this.scale * this.liveScale); } public void extractRenderState(Zombie entity, ZombieRenderState state, float partialTicks) { super.extractRenderState(entity, state, partialTicks); this.liveTint = tintFor(entity); this.liveScale = scaleFor(entity, partialTicks); this.liveYOffset = yOffsetFor(entity, partialTicks); this.shadowRadius = this.shadow; }
   private int tintFor(Zombie entity) { if (entity instanceof NexusGuardianEntity guardian) { return switch (guardian.phase()) { case 1 -> 0xFF66E8FF; case 2 -> 0xFF8CB6FF; case 3 -> 0xFFD76BFF; default -> 0xFFFF4FD8; }; } if (entity instanceof CorruptionWardenEntity) { return entity.tickCount % 40 < 8 ? 0xFFFFA6F0 : this.tint; } return this.tint; }
   private float scaleFor(Zombie entity, float partialTicks) { float age = entity.tickCount + partialTicks; if (entity instanceof NexusGuardianEntity guardian) return 1.0F + guardian.phase() * 0.018F + (float)Math.sin(age * 0.12F) * 0.015F; if (entity instanceof CorruptionWardenEntity) return 1.0F + (entity.tickCount % 100 < 12 ? 0.045F : 0.0F); if (entity instanceof DataWraithEntity) return 0.94F + (float)Math.sin(age * 0.22F) * 0.035F; if (entity instanceof ArchiveSeekerEntity) return 1.0F + (float)Math.sin(age * 0.18F) * 0.018F; return 1.0F; }
   private float yOffsetFor(Zombie entity, float partialTicks) { if (entity instanceof DataWraithEntity) return 0.18F + (float)Math.sin((entity.tickCount + partialTicks) * 0.16F) * 0.08F; return 0.0F; }
}
