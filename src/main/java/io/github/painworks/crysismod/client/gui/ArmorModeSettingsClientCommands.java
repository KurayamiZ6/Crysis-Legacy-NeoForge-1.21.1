package io.github.painworks.crysismod.client.gui;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.painworks.crysismod.client.nanogeo.NanoGeoArmorModePulseCenterExtractor;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = "crysismod", value = Dist.CLIENT, bus = Bus.GAME)
public final class ArmorModeSettingsClientCommands {
   private ArmorModeSettingsClientCommands() {
   }

   @SubscribeEvent
   public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
      event.getDispatcher()
         .register(
            (LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("armormode")
                  .then(Commands.literal("settings").executes(ctx -> openSettingsScreen())))
               .then(
                  Commands.literal("extract_pulse_centers")
                     .executes(
                        ctx -> {
                           try {
                              NanoGeoArmorModePulseCenterExtractor.ExtractionResult result = NanoGeoArmorModePulseCenterExtractor.extractAndWrite();
                              ((CommandSourceStack)ctx.getSource())
                                 .sendSuccess(
                                    () -> Component.literal(
                                       "Extracted "
                                          + result.centerCount()
                                          + " Armor Mode pulse centers from "
                                          + result.markerPixelCount()
                                          + " red marker pixels into "
                                          + result.outputPath()
                                    ),
                                    false
                                 );
                           } catch (Exception var2) {
                              ((CommandSourceStack)ctx.getSource()).sendFailure(Component.literal("Pulse center extraction failed: " + var2.getMessage()));
                           }

                           return 1;
                        }
                     )
               )
         );
   }

   private static int openSettingsScreen() {
      Minecraft minecraft = Minecraft.getInstance();
      minecraft.execute(() -> minecraft.setScreen(new ArmorModeOverlaySettingsScreen(minecraft.screen)));
      return 1;
   }
}
