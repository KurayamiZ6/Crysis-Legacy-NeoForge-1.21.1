package io.github.painworks.crysismod.nanosuit.command;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.painworks.crysismod.nanosuit.NanosuitMode;
import io.github.painworks.crysismod.nanosuit.config.NanosuitServerConfig;
import io.github.painworks.crysismod.nanosuit.energy.INanosuitEnergy;
import io.github.painworks.crysismod.nanosuit.energy.NanosuitCapabilities;
import io.github.painworks.crysismod.nanosuit.handler.NanosuitPlayerHandler;
import io.github.painworks.crysismod.nanosuit.network.NanosuitSyncManager;
import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = "crysismod", bus = Bus.GAME)
public final class NanosuitDebugCommands {
   private NanosuitDebugCommands() {
   }

   @SubscribeEvent
   public static void onRegisterCommands(RegisterCommandsEvent event) {
      if (NanosuitServerConfig.debugCommandsEnabled()) {
         LiteralArgumentBuilder<CommandSourceStack> root = (LiteralArgumentBuilder<CommandSourceStack>)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(
                                 "nanosuit"
                              )
                              .requires(source -> source.hasPermission(2)))
                           .then(
                              ((LiteralArgumentBuilder)Commands.literal("status")
                                    .executes(ctx -> executeStatus(ctx, ((CommandSourceStack)ctx.getSource()).getPlayerOrException())))
                                 .then(
                                    Commands.argument("target", EntityArgument.player())
                                       .executes(ctx -> executeStatus(ctx, EntityArgument.getPlayer(ctx, "target")))
                                 )
                           ))
                        .then(
                           Commands.literal("set_energy")
                              .then(
                                 ((RequiredArgumentBuilder)Commands.argument("value", FloatArgumentType.floatArg(0.0F, 100.0F))
                                       .executes(
                                          ctx -> executeSetEnergy(
                                             ctx, ((CommandSourceStack)ctx.getSource()).getPlayerOrException(), FloatArgumentType.getFloat(ctx, "value")
                                          )
                                       ))
                                    .then(
                                       Commands.argument("target", EntityArgument.player())
                                          .executes(
                                             ctx -> executeSetEnergy(ctx, EntityArgument.getPlayer(ctx, "target"), FloatArgumentType.getFloat(ctx, "value"))
                                          )
                                    )
                              )
                        ))
                     .then(
                        Commands.literal("set_mode")
                           .then(
                              ((RequiredArgumentBuilder)Commands.argument("mode", StringArgumentType.word())
                                    .suggests((context, builder) -> suggestModes(builder))
                                    .executes(
                                       ctx -> executeSetMode(
                                          ctx, ((CommandSourceStack)ctx.getSource()).getPlayerOrException(), StringArgumentType.getString(ctx, "mode")
                                       )
                                    ))
                                 .then(
                                    Commands.argument("target", EntityArgument.player())
                                       .executes(ctx -> executeSetMode(ctx, EntityArgument.getPlayer(ctx, "target"), StringArgumentType.getString(ctx, "mode")))
                                 )
                           )
                     ))
                  .then(
                     ((LiteralArgumentBuilder)Commands.literal("refill")
                           .executes(ctx -> executeRefill(ctx, ((CommandSourceStack)ctx.getSource()).getPlayerOrException())))
                        .then(Commands.argument("target", EntityArgument.player()).executes(ctx -> executeRefill(ctx, EntityArgument.getPlayer(ctx, "target"))))
                  ))
               .then(Commands.literal("spark_help").executes(NanosuitDebugCommands::executeSparkHelp)))
            .then(
               ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("profiler")
                           .then(Commands.literal("status").executes(NanosuitDebugCommands::executeProfilerStatus)))
                        .then(Commands.literal("on").executes(NanosuitDebugCommands::executeProfilerOn)))
                     .then(Commands.literal("off").executes(NanosuitDebugCommands::executeProfilerOff)))
                  .then(Commands.literal("reset").executes(NanosuitDebugCommands::executeProfilerReset))
            );
         event.getDispatcher().register(root);
      }
   }

   private static int executeStatus(CommandContext<CommandSourceStack> context, ServerPlayer target) {
      return withEnergy(
         target,
         data -> {
            boolean hasFullSet = NanosuitArmorUtil.hasFullSet(target);
            ((CommandSourceStack)context.getSource())
               .sendSuccess(
                  () -> Component.literal(
                     "Nanosuit["
                        + target.getScoreboardName()
                        + "] energy="
                        + round1(data.getEnergy())
                        + " mode="
                        + data.getMode().name()
                        + " sprintReady="
                        + data.isSprintBoostReady()
                        + " recoveryLocked="
                        + data.isRecoveryLocked()
                        + " fullSet="
                        + hasFullSet
                  ),
                  false
               );
         }
      );
   }

   private static int executeSetEnergy(CommandContext<CommandSourceStack> context, ServerPlayer target, float value) {
      return withEnergy(
         target,
         data -> {
            data.setEnergy(value);
            if (data.getEnergy() <= 0.0F) {
               data.setSprintBoostReady(false);
            }

            NanosuitSyncManager.syncToClient(target);
            ((CommandSourceStack)context.getSource())
               .sendSuccess(() -> Component.literal("Nanosuit[" + target.getScoreboardName() + "] energy set to " + round1(data.getEnergy())), true);
         }
      );
   }

   private static int executeSetMode(CommandContext<CommandSourceStack> context, ServerPlayer target, String rawMode) {
      NanosuitMode mode;
      try {
         mode = NanosuitMode.valueOf(rawMode.toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException var5) {
         context.getSource().sendFailure(Component.literal("Invalid mode. Use none, armor, strength, speed or cloak."));
         return 0;
      }

      var profile = NanosuitArmorUtil.getEquippedProfile(target);
      if (mode != NanosuitMode.NONE && (profile == null || !profile.controls().allowsMode(mode))) {
         context.getSource().sendFailure(Component.literal("The equipped Nanosuit does not support " + mode.name() + "."));
         return 0;
      }

      return withEnergy(
         target,
         data -> {
            data.setMode(mode);
            NanosuitSyncManager.syncToClient(target);
            ((CommandSourceStack)context.getSource())
               .sendSuccess(() -> Component.literal("Nanosuit[" + target.getScoreboardName() + "] mode set to " + data.getMode().name()), true);
         }
      );
   }

   private static int executeRefill(CommandContext<CommandSourceStack> context, ServerPlayer target) {
      return withEnergy(target, data -> {
         data.setEnergy(100.0F);
         NanosuitSyncManager.syncToClient(target);
         ((CommandSourceStack)context.getSource()).sendSuccess(() -> Component.literal("Nanosuit[" + target.getScoreboardName() + "] energy refilled"), true);
      });
   }

   private static int executeSparkHelp(CommandContext<CommandSourceStack> context) {
      ((CommandSourceStack)context.getSource()).sendSuccess(() -> Component.literal("Spark quick commands:"), false);
      ((CommandSourceStack)context.getSource()).sendSuccess(() -> Component.literal("/spark profiler --only-ticks start"), false);
      ((CommandSourceStack)context.getSource()).sendSuccess(() -> Component.literal("/spark profiler --only-ticks stop"), false);
      ((CommandSourceStack)context.getSource()).sendSuccess(() -> Component.literal("/spark heap summary"), false);
      return 1;
   }

   private static int executeProfilerStatus(CommandContext<CommandSourceStack> context) {
      NanosuitPlayerHandler.ProfilingSnapshot s = NanosuitPlayerHandler.profilingSnapshot();
      ((CommandSourceStack)context.getSource())
         .sendSuccess(
            () -> Component.literal(
               "Nanosuit profiler: "
                  + (s.enabled() ? "ON" : "OFF")
                  + " | tickPlayer calls="
                  + s.tickCalls()
                  + " avg="
                  + String.format(Locale.ROOT, "%.2f", s.tickAvgMicros())
                  + "us max="
                  + String.format(Locale.ROOT, "%.2f", s.tickMaxMicros())
                  + "us"
            ),
            false
         );
      ((CommandSourceStack)context.getSource())
         .sendSuccess(
            () -> Component.literal(
               "isMoving calls="
                  + s.isMovingCalls()
                  + " avg="
                  + String.format(Locale.ROOT, "%.2f", s.isMovingAvgMicros())
                  + "us max="
                  + String.format(Locale.ROOT, "%.2f", s.isMovingMaxMicros())
                  + "us moving%="
                  + String.format(Locale.ROOT, "%.1f", s.movingTrueRatioPercent())
            ),
            false
         );
      return 1;
   }

   private static int executeProfilerOn(CommandContext<CommandSourceStack> context) {
      NanosuitPlayerHandler.setProfilingEnabled(true);
      ((CommandSourceStack)context.getSource()).sendSuccess(() -> Component.literal("Nanosuit profiler enabled"), true);
      return 1;
   }

   private static int executeProfilerOff(CommandContext<CommandSourceStack> context) {
      NanosuitPlayerHandler.setProfilingEnabled(false);
      ((CommandSourceStack)context.getSource()).sendSuccess(() -> Component.literal("Nanosuit profiler disabled"), true);
      return 1;
   }

   private static int executeProfilerReset(CommandContext<CommandSourceStack> context) {
      NanosuitPlayerHandler.resetProfiling();
      ((CommandSourceStack)context.getSource()).sendSuccess(() -> Component.literal("Nanosuit profiler counters reset"), true);
      return 1;
   }

   private static int withEnergy(ServerPlayer target, Consumer<INanosuitEnergy> action) {
      return java.util.Optional.<INanosuitEnergy>of(NanosuitCapabilities.get(target)).map(data -> {
         action.accept(data);
         return 1;
      }).orElse(0);
   }

   private static CompletableFuture<Suggestions> suggestModes(SuggestionsBuilder builder) {
      builder.suggest("none");
      builder.suggest("armor");
      builder.suggest("strength");
      builder.suggest("speed");
      builder.suggest("cloak");
      return builder.buildFuture();
   }

   private static String round1(float value) {
      return String.format(Locale.ROOT, "%.1f", value);
   }
}
