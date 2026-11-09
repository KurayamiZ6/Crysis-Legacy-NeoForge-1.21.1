package io.github.painworks.crysismod.client.nanogeo;

import com.mojang.logging.LogUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.slf4j.Logger;

public final class NanoGeoArmorClientExtensions {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static boolean logShown;

   private NanoGeoArmorClientExtensions() {
   }

   public static IClientItemExtensions create() {
      return new IClientItemExtensions() {
         private NanoGeoArmorRenderer renderer;

         @Override
         public HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
            if (this.renderer == null) {
               this.renderer = new NanoGeoArmorRenderer();
            }

            this.renderer.prepForRender(livingEntity, itemStack, equipmentSlot, original);
            this.renderer.setSuppressRender(livingEntity instanceof Player player && NanoGeoArmorModeBaseCompositeCompat.shouldReplaceBaseArmor(player));
            if (NanoGeoClientConfig.nanoGeoLogModelLoad() && !NanoGeoArmorClientExtensions.logShown) {
               NanoGeoArmorClientExtensions.logShown = true;
               NanoGeoArmorClientExtensions.LOGGER.info("[CrysisMod][NanoGeo] Using NanoGeo experimental armor renderer for test items");
            }

            return this.renderer;
         }
      };
   }
}
