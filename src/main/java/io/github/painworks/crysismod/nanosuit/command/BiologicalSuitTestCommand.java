package io.github.painworks.crysismod.nanosuit.command;

import io.github.painworks.crysismod.nanosuit.config.NanosuitServerConfig;
import io.github.painworks.crysismod.nanosuit.integration.BiologicalSuitIntegration;
import io.github.painworks.crysismod.nanosuit.integration.BiologicalSuitIntegration.Result;
import io.github.painworks.crysismod.nanosuit.integration.Undersuit;
import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import java.util.Locale;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = "crysismod", bus = EventBusSubscriber.Bus.GAME)
public final class BiologicalSuitTestCommand {
   private BiologicalSuitTestCommand() {}

   @SubscribeEvent
   public static void register(RegisterCommandsEvent event) {
      if (!Boolean.getBoolean("crysis.nanosuit.integrationTestCommands")) return;
      var root = Commands.literal("nanosuit_link_test")
         .requires(source -> source.hasPermission(2) && NanosuitServerConfig.debugCommandsEnabled());
      root.then(Commands.literal("status").executes(ctx -> status(ctx.getSource(), ctx.getSource().getPlayerOrException()))
         .then(Commands.argument("player", EntityArgument.player())
            .executes(ctx -> status(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))));
      for (String action : new String[]{"integrate", "extract"}) {
         boolean integrate = action.equals("integrate");
         root.then(Commands.literal(action).executes(ctx -> transfer(ctx.getSource(), ctx.getSource().getPlayerOrException(), integrate))
            .then(Commands.argument("player", EntityArgument.player())
               .executes(ctx -> transfer(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), integrate))));
      }
      event.getDispatcher().register(root);
   }

   private static int transfer(CommandSourceStack source, ServerPlayer player, boolean integrate) {
      var hotbar = player.getInventory().items.subList(0, 4);
      var result = integrate ? BiologicalSuitIntegration.integrate(player, hotbar) : BiologicalSuitIntegration.extract(player, hotbar);
      var message = Component.translatable("command.crysismod.biological." + result.name().toLowerCase(Locale.ROOT));
      if (result != Result.SUCCESS) {
         source.sendFailure(message);
         return 0;
      }
      source.sendSuccess(() -> message, false);
      return 1;
   }

   private static int status(CommandSourceStack source, ServerPlayer player) {
      var identity = BiologicalSuitIntegration.linkedIdentity(player);
      var profile = NanosuitArmorUtil.getEquippedProfile(player);
      source.sendSuccess(() -> Component.literal("Biological link: " + (identity == null ? "none" : identity)
         + " | armor: " + (profile == null ? "none" : profile.generation()) + " | undersuit: " + Undersuit.isComplete(player)), false);
      return 1;
   }
}
