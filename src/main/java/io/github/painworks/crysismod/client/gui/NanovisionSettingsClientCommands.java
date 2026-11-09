package io.github.painworks.crysismod.client.gui;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = "crysismod", value = Dist.CLIENT, bus = Bus.GAME)
public final class NanovisionSettingsClientCommands {
   private NanovisionSettingsClientCommands() {
   }

   @SubscribeEvent
   public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
      event.getDispatcher()
         .register((LiteralArgumentBuilder)Commands.literal("nanovision").then(Commands.literal("settings").executes(context -> openSettingsScreen())));
   }

   private static int openSettingsScreen() {
      Minecraft minecraft = Minecraft.getInstance();
      minecraft.execute(() -> minecraft.setScreen(new NanovisionSettingsScreen(minecraft.screen)));
      return 1;
   }
}
