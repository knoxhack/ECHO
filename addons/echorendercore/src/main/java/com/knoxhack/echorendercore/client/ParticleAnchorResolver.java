package com.knoxhack.echorendercore.client;

import com.knoxhack.echorendercore.profile.RenderCoreAnchor;
import com.knoxhack.echorendercore.profile.RenderCoreVector;
import com.knoxhack.echorendercore.profile.VisualProfile;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class ParticleAnchorResolver {
   private ParticleAnchorResolver() {
   }

   public static Vec3 resolve(Entity entity, VisualProfile profile, String anchorName) {
      if (entity == null) {
         return Vec3.ZERO;
      }
      RenderCoreVector offset = RenderCoreVector.ZERO;
      if (profile != null) {
         RenderCoreAnchor anchor = profile.anchor(anchorName);
         if (anchor != null) {
            offset = anchor.offset();
         }
      }
      float yaw = entity.getYRot() * Mth.DEG_TO_RAD;
      double sin = Mth.sin(yaw);
      double cos = Mth.cos(yaw);
      double x = offset.x() * cos - offset.z() * sin;
      double z = offset.x() * sin + offset.z() * cos;
      return entity.position().add(x, offset.y(), z);
   }

   public static Vec3 resolve(Vec3 origin, VisualProfile profile, String anchorName) {
      RenderCoreVector offset = RenderCoreVector.ZERO;
      if (profile != null) {
         RenderCoreAnchor anchor = profile.anchor(anchorName);
         if (anchor != null) {
            offset = anchor.offset();
         }
      }
      return (origin == null ? Vec3.ZERO : origin).add(offset.x(), offset.y(), offset.z());
   }
}
