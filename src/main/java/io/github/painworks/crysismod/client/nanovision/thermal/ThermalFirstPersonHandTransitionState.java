package io.github.painworks.crysismod.client.nanovision.thermal;

import net.minecraft.world.item.ItemStack;

public final class ThermalFirstPersonHandTransitionState {
   private static boolean animatedMainHandHasItem;
   private static boolean animatedOffHandHasItem;

   private ThermalFirstPersonHandTransitionState() {
   }

   public static void beginHandRender(ItemStack mainHandItem, ItemStack offHandItem) {
      animatedMainHandHasItem = mainHandItem != null && !mainHandItem.isEmpty();
      animatedOffHandHasItem = offHandItem != null && !offHandItem.isEmpty();
   }

   public static boolean keepMainHandCaptureForOutgoingItem() {
      return animatedMainHandHasItem;
   }

   public static boolean keepOffHandCaptureForOutgoingItem() {
      return animatedOffHandHasItem;
   }

   public static void endHandRender() {
      animatedMainHandHasItem = false;
      animatedOffHandHasItem = false;
   }
}
