package io.github.painworks.crysismod.nanosuit.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.painworks.crysismod.nanosuit.handler.NanosuitPlayerHandler;
import io.github.painworks.crysismod.nanosuit.network.NanosuitNetwork;
import io.github.painworks.crysismod.nanosuit.network.SyncPowerJumpDebugPacket;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = "crysismod", bus = Bus.GAME)
public final class PowerJumpDebugCommands {
   private PowerJumpDebugCommands() {
   }

   @SubscribeEvent
   public static void onRegisterCommands(RegisterCommandsEvent event) {
      event.getDispatcher()
         .register(
            (LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("powerjump").requires(source -> source.hasPermission(2)))
               .then(
                  ((LiteralArgumentBuilder)Commands.literal("test").then(Commands.literal("start").executes(PowerJumpDebugCommands::start)))
                     .then(Commands.literal("stop").executes(PowerJumpDebugCommands::stop))
               )
         );
   }

   private static int start(CommandContext<CommandSourceStack> context) {
      ServerPlayer player = ((CommandSourceStack)context.getSource()).getPlayer();
      if (player == null) {
         ((CommandSourceStack)context.getSource()).sendFailure(Component.literal("This command must be run by a player."));
         return 0;
      } else {
         NanosuitPlayerHandler.startPowerJumpDebug(player);
         NanosuitNetwork.sendToPlayer(player, new SyncPowerJumpDebugPacket(true));
         ((CommandSourceStack)context.getSource())
            .sendSuccess(() -> Component.literal("Power-jump test logging started. Perform Shift + Space jumps, then use /powerjump test stop."), false);
         return 1;
      }
   }

   private static int stop(CommandContext<CommandSourceStack> context) {
      ServerPlayer player = ((CommandSourceStack)context.getSource()).getPlayer();
      if (player == null) {
         ((CommandSourceStack)context.getSource()).sendFailure(Component.literal("This command must be run by a player."));
         return 0;
      } else {
         boolean wasActive = NanosuitPlayerHandler.stopPowerJumpDebug(player);
         NanosuitNetwork.sendToPlayer(player, new SyncPowerJumpDebugPacket(false));
         ((CommandSourceStack)context.getSource())
            .sendSuccess(() -> Component.literal(wasActive ? "Power-jump test logging stopped." : "Power-jump test logging was not active."), false);
         return 1;
      }
   }
}
