package io.github.painworks.crysismod.nanosuit.command;

import io.github.painworks.crysismod.nanosuit.config.NanosuitServerConfig;
import io.github.painworks.crysismod.nanosuit.cradle.RoboticArmAssemblyBlockEntity;
import io.github.painworks.crysismod.nanosuit.cradle.RoboticArmAssemblyBlockEntity.State;
import java.util.Locale;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = "crysismod", bus = EventBusSubscriber.Bus.GAME)
public final class RoboticArmTestCommand {
   private RoboticArmTestCommand() {}

   @SubscribeEvent
   public static void register(RegisterCommandsEvent event) {
      if (!Boolean.getBoolean("crysis.nanosuit.armTestCommands")) return;
      var root = Commands.literal("nanosuit_arms_test")
         .requires(source -> source.hasPermission(2) && NanosuitServerConfig.debugCommandsEnabled());
      var position = Commands.argument("assembly", BlockPosArgument.blockPos());
      for (var state : State.values()) {
         String name = state.name().toLowerCase(Locale.ROOT);
         position.then(Commands.literal(name).executes(ctx ->
            setState(ctx.getSource(), BlockPosArgument.getLoadedBlockPos(ctx, "assembly"), state, name)));
      }
      event.getDispatcher().register(root.then(position));
   }

   private static int setState(CommandSourceStack source, BlockPos position, State state, String name) {
      var assembly = RoboticArmAssemblyBlockEntity.at(source.getLevel(), position);
      if (assembly == null || !assembly.transitionTo(state)) {
         source.sendFailure(Component.translatable("command.crysismod.arms_denied"));
         return 0;
      }
      source.sendSuccess(() -> Component.translatable("command.crysismod.arms_changed", name), false);
      return 1;
   }
}
