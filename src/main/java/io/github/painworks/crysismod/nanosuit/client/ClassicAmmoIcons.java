package io.github.painworks.crysismod.nanosuit.client;

import io.github.painworks.crysismod.nanosuit.config.NanosuitClientConfig;
import net.minecraft.resources.ResourceLocation;

/** Only explicit ammo identity and the public shotgun category determine a special icon. */
public final class ClassicAmmoIcons {
   public enum Kind { BULLET, SHELL, ROCKET, GRENADE_ROUND }
   private ClassicAmmoIcons(){}
   public static Kind classify(String type,ResourceLocation ammo){
      if(ammo!=null)for(String entry:NanosuitClientConfig.classicAmmoIcons()){
         String[] pair=entry.split("\\|",2);
         if(pair.length==2&&pair[0].equals(ammo.toString()))return Kind.valueOf(pair[1].toUpperCase(java.util.Locale.ROOT));
      }
      return "shotgun".equals(type)?Kind.SHELL:Kind.BULLET;
   }
}
