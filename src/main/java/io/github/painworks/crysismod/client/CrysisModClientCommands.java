package io.github.painworks.crysismod.client;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.painworks.crysismod.client.gui.CrysisHudEditScreen;
import io.github.painworks.crysismod.client.nanovision.debug.NanovisionPerformanceProfiler;
import io.github.painworks.crysismod.config.CrysisModConfigDefaults;
import io.github.painworks.crysismod.nanovisor.client.NanoVisorHudOverlay;
import io.github.painworks.crysismod.nanovisor.config.NanoVisorClientConfig;
import java.util.Locale;
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
public final class CrysisModClientCommands {
   private CrysisModClientCommands() {
   }

   @SubscribeEvent
   public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
      event.getDispatcher()
         .register(
            (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(
                                 "crysismod"
                              )
                              .then(Commands.literal("ui_edit").executes(ctx -> openUiEditor())))
                           .then(
                              ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("nanovision_profile")
                                          .executes(ctx -> nanovisionProfileStatus((CommandSourceStack)ctx.getSource())))
                                       .then(
                                          ((LiteralArgumentBuilder)Commands.literal("start")
                                                .executes(
                                                   ctx -> startNanovisionProfile(
                                                      (CommandSourceStack)ctx.getSource(), NanovisionPerformanceProfiler.defaultFrames()
                                                   )
                                                ))
                                             .then(
                                                Commands.argument("frames", IntegerArgumentType.integer(60, 3600))
                                                   .executes(
                                                      ctx -> startNanovisionProfile(
                                                         (CommandSourceStack)ctx.getSource(), IntegerArgumentType.getInteger(ctx, "frames")
                                                      )
                                                   )
                                             )
                                       ))
                                    .then(Commands.literal("stop").executes(ctx -> stopNanovisionProfile((CommandSourceStack)ctx.getSource()))))
                                 .then(Commands.literal("status").executes(ctx -> nanovisionProfileStatus((CommandSourceStack)ctx.getSource())))
                           ))
                        .then(
                           ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("nanovision_mob_profile")
                                       .executes(ctx -> nanovisionMobProfileStatus((CommandSourceStack)ctx.getSource())))
                                    .then(
                                       ((LiteralArgumentBuilder)Commands.literal("start")
                                             .executes(
                                                ctx -> startNanovisionMobProfile(
                                                   (CommandSourceStack)ctx.getSource(), NanovisionPerformanceProfiler.defaultFrames()
                                                )
                                             ))
                                          .then(
                                             Commands.argument("frames", IntegerArgumentType.integer(60, 3600))
                                                .executes(
                                                   ctx -> startNanovisionMobProfile(
                                                      (CommandSourceStack)ctx.getSource(), IntegerArgumentType.getInteger(ctx, "frames")
                                                   )
                                                )
                                          )
                                    ))
                                 .then(Commands.literal("stop").executes(ctx -> stopNanovisionProfile((CommandSourceStack)ctx.getSource()))))
                              .then(Commands.literal("status").executes(ctx -> nanovisionMobProfileStatus((CommandSourceStack)ctx.getSource())))
                        ))
                     .then(
                        ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("nanovision_first_person_profile")
                                    .executes(ctx -> nanovisionFirstPersonProfileStatus((CommandSourceStack)ctx.getSource())))
                                 .then(
                                    ((LiteralArgumentBuilder)Commands.literal("start")
                                          .executes(
                                             ctx -> startNanovisionFirstPersonProfile(
                                                (CommandSourceStack)ctx.getSource(), NanovisionPerformanceProfiler.defaultFrames()
                                             )
                                          ))
                                       .then(
                                          Commands.argument("frames", IntegerArgumentType.integer(60, 3600))
                                             .executes(
                                                ctx -> startNanovisionFirstPersonProfile(
                                                   (CommandSourceStack)ctx.getSource(), IntegerArgumentType.getInteger(ctx, "frames")
                                                )
                                             )
                                       )
                                 ))
                              .then(Commands.literal("stop").executes(ctx -> stopNanovisionProfile((CommandSourceStack)ctx.getSource()))))
                           .then(Commands.literal("status").executes(ctx -> nanovisionFirstPersonProfileStatus((CommandSourceStack)ctx.getSource())))
                     ))
                  .then(
                     ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("visor_zoom_arrow")
                                 .then(
                                    Commands.literal("start")
                                       .then(
                                          Commands.argument("x", DoubleArgumentType.doubleArg(-4000.0, 4000.0))
                                             .then(
                                                Commands.argument("y", DoubleArgumentType.doubleArg(-4000.0, 4000.0))
                                                   .executes(
                                                      ctx -> setVisorZoomArrowStart(
                                                         (CommandSourceStack)ctx.getSource(),
                                                         DoubleArgumentType.getDouble(ctx, "x"),
                                                         DoubleArgumentType.getDouble(ctx, "y")
                                                      )
                                                   )
                                             )
                                       )
                                 ))
                              .then(
                                 Commands.literal("end")
                                    .then(
                                       Commands.argument("x", DoubleArgumentType.doubleArg(-4000.0, 4000.0))
                                          .then(
                                             Commands.argument("y", DoubleArgumentType.doubleArg(-4000.0, 4000.0))
                                                .executes(
                                                   ctx -> setVisorZoomArrowEnd(
                                                      (CommandSourceStack)ctx.getSource(),
                                                      DoubleArgumentType.getDouble(ctx, "x"),
                                                      DoubleArgumentType.getDouble(ctx, "y")
                                                   )
                                                )
                                          )
                                    )
                              ))
                           .then(Commands.literal("print").executes(ctx -> printVisorZoomArrow((CommandSourceStack)ctx.getSource()))))
                        .then(Commands.literal("reset").executes(ctx -> resetVisorZoomArrow((CommandSourceStack)ctx.getSource())))
                  ))
               .then(Commands.literal("config").then(Commands.literal("reset_all").executes(ctx -> resetAllConfigs((CommandSourceStack)ctx.getSource()))))
         );
   }

   private static int openUiEditor() {
      Minecraft minecraft = Minecraft.getInstance();
      minecraft.execute(() -> minecraft.setScreen(new CrysisHudEditScreen(minecraft.screen)));
      return 1;
   }

   private static int resetAllConfigs(CommandSourceStack source) {
      CrysisModConfigDefaults.ResetResult result = CrysisModConfigDefaults.resetAllRegisteredConfigs();
      if (result.success()) {
         source.sendSuccess(() -> Component.literal("Crysis Mod configs reset to defaults (" + result.resetValues() + " values)."), false);
      } else {
         source.sendFailure(Component.literal("Crysis Mod configs reset with " + result.errors().size() + " warning(s); see log/chat details."));

         for (String error : result.errors()) {
            source.sendFailure(Component.literal(" - " + error));
         }
      }

      return 1;
   }

   private static int startNanovisionProfile(CommandSourceStack source, int frames) {
      String message = NanovisionPerformanceProfiler.start(frames);
      source.sendSuccess(() -> Component.literal(message), false);
      return 1;
   }

   private static int stopNanovisionProfile(CommandSourceStack source) {
      String message = NanovisionPerformanceProfiler.requestStop();
      source.sendSuccess(() -> Component.literal(message), false);
      return 1;
   }

   private static int startNanovisionMobProfile(CommandSourceStack source, int frames) {
      String message = NanovisionPerformanceProfiler.startMobModels(frames);
      source.sendSuccess(() -> Component.literal(message), false);
      return 1;
   }

   private static int nanovisionMobProfileStatus(CommandSourceStack source) {
      source.sendSuccess(() -> Component.literal(NanovisionPerformanceProfiler.status()), false);
      return 1;
   }

   private static int startNanovisionFirstPersonProfile(CommandSourceStack source, int frames) {
      String message = NanovisionPerformanceProfiler.startFirstPerson(frames);
      source.sendSuccess(() -> Component.literal(message), false);
      return 1;
   }

   private static int nanovisionFirstPersonProfileStatus(CommandSourceStack source) {
      source.sendSuccess(() -> Component.literal(NanovisionPerformanceProfiler.status()), false);
      return 1;
   }

   private static int nanovisionProfileStatus(CommandSourceStack source) {
      String message = NanovisionPerformanceProfiler.status();
      source.sendSuccess(() -> Component.literal(message), false);
      return 1;
   }

   private static int setVisorZoomArrowStart(CommandSourceStack source, double x, double y) {
      NanoVisorClientConfig.setVisorZoomArrowStartX(x);
      NanoVisorClientConfig.setVisorZoomArrowStartY(y);
      NanoVisorHudOverlay.resetArrowInterpolation();
      source.sendSuccess(() -> Component.literal("Visor zoom arrow start set to x=" + formatTwoDecimals(x) + ", y=" + formatTwoDecimals(y)), false);
      return 1;
   }

   private static int setVisorZoomArrowEnd(CommandSourceStack source, double x, double y) {
      NanoVisorClientConfig.setVisorZoomArrowEndX(x);
      NanoVisorClientConfig.setVisorZoomArrowEndY(y);
      NanoVisorHudOverlay.resetArrowInterpolation();
      source.sendSuccess(() -> Component.literal("Visor zoom arrow end set to x=" + formatTwoDecimals(x) + ", y=" + formatTwoDecimals(y)), false);
      return 1;
   }

   private static int printVisorZoomArrow(CommandSourceStack source) {
      source.sendSuccess(
         () -> Component.literal(
            "Visor zoom arrow: start=("
               + formatTwoDecimals(NanoVisorClientConfig.visorZoomArrowStartX())
               + ", "
               + formatTwoDecimals(NanoVisorClientConfig.visorZoomArrowStartY())
               + "), end=("
               + formatTwoDecimals(NanoVisorClientConfig.visorZoomArrowEndX())
               + ", "
               + formatTwoDecimals(NanoVisorClientConfig.visorZoomArrowEndY())
               + ")"
         ),
         false
      );
      return 1;
   }

   private static int resetVisorZoomArrow(CommandSourceStack source) {
      NanoVisorClientConfig.setVisorZoomArrowStartX(702.0);
      NanoVisorClientConfig.setVisorZoomArrowStartY(568.0);
      NanoVisorClientConfig.setVisorZoomArrowEndX(702.0);
      NanoVisorClientConfig.setVisorZoomArrowEndY(493.0);
      NanoVisorHudOverlay.resetArrowInterpolation();
      source.sendSuccess(() -> Component.literal("Visor zoom arrow positions reset to defaults."), false);
      return 1;
   }

   private static String formatTwoDecimals(double value) {
      return String.format(Locale.ROOT, "%.2f", value);
   }
}
