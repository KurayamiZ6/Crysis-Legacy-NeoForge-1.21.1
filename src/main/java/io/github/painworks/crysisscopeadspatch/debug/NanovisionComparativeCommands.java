package io.github.painworks.crysisscopeadspatch.debug;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class NanovisionComparativeCommands {
   private NanovisionComparativeCommands() {
   }

   public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
      dispatcher.register(
         (LiteralArgumentBuilder)Commands.literal("crysismod")
            .then(
               ((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("nanovision_compare")
                        .executes(context -> reply((CommandSourceStack)context.getSource(), NanovisionComparativeProfiler.statusV3())))
                     .then(
                        ((LiteralArgumentBuilder)Commands.literal("start")
                              .executes(context -> reply((CommandSourceStack)context.getSource(), NanovisionComparativeProfiler.startV3(300))))
                           .then(
                              Commands.argument("frames", IntegerArgumentType.integer(60, 1800))
                                 .executes(
                                    context -> reply(
                                       (CommandSourceStack)context.getSource(),
                                       NanovisionComparativeProfiler.startV3(IntegerArgumentType.getInteger(context, "frames"))
                                    )
                                 )
                           )
                     ))
                  .then(Commands.literal("stop").executes(context -> reply((CommandSourceStack)context.getSource(), NanovisionComparativeProfiler.stopV3())))
            )
      );
   }

   private static int reply(CommandSourceStack source, String message) {
      source.sendSuccess(() -> Component.literal(message), false);
      return 1;
   }
}
