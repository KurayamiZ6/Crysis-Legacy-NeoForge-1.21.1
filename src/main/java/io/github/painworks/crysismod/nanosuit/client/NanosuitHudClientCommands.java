package io.github.painworks.crysismod.nanosuit.client;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = "crysismod", value = Dist.CLIENT, bus = Bus.GAME)
public final class NanosuitHudClientCommands {
   private NanosuitHudClientCommands() {
   }

   @SubscribeEvent
   public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
      event.getDispatcher()
         .register(
            (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(
                                                      "nshud"
                                                   )
                                                   .then(
                                                      Commands.literal("x")
                                                         .then(
                                                            Commands.argument("value", IntegerArgumentType.integer(-2000, 2000))
                                                               .executes(ctx -> setX(IntegerArgumentType.getInteger(ctx, "value")))
                                                         )
                                                   ))
                                                .then(
                                                   Commands.literal("y")
                                                      .then(
                                                         Commands.argument("value", IntegerArgumentType.integer(-2000, 2000))
                                                            .executes(ctx -> setY(IntegerArgumentType.getInteger(ctx, "value")))
                                                      )
                                                ))
                                             .then(
                                                Commands.literal("move")
                                                   .then(
                                                      Commands.argument("dx", IntegerArgumentType.integer(-2000, 2000))
                                                         .then(
                                                            Commands.argument("dy", IntegerArgumentType.integer(-2000, 2000))
                                                               .executes(
                                                                  ctx -> move(
                                                                     IntegerArgumentType.getInteger(ctx, "dx"), IntegerArgumentType.getInteger(ctx, "dy")
                                                                  )
                                                               )
                                                         )
                                                   )
                                             ))
                                          .then(Commands.literal("reset").executes(ctx -> reset())))
                                       .then(Commands.literal("pos").executes(ctx -> printPos())))
                                    .then(Commands.literal("status").executes(ctx -> printHudStatus())))
                                 .then(
                                    ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("visible")
                                                .executes(ctx -> toggleVisible()))
                                             .then(Commands.literal("on").executes(ctx -> setVisible(true))))
                                          .then(Commands.literal("off").executes(ctx -> setVisible(false))))
                                       .then(Commands.literal("status").executes(ctx -> printVisibleStatus()))
                                 ))
                              .then(
                                 ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("scale")
                                             .then(Commands.literal("status").executes(ctx -> printScaleStatus())))
                                          .then(Commands.literal("reset").executes(ctx -> resetScale())))
                                       .then(
                                          Commands.literal("set")
                                             .then(
                                                Commands.argument("value", FloatArgumentType.floatArg(0.05F, 5.0F))
                                                   .executes(ctx -> setScale(FloatArgumentType.getFloat(ctx, "value")))
                                             )
                                       ))
                                    .then(
                                       Commands.literal("add")
                                          .then(
                                             Commands.argument("delta", FloatArgumentType.floatArg(-2.0F, 2.0F))
                                                .executes(ctx -> addScale(FloatArgumentType.getFloat(ctx, "delta")))
                                          )
                                    )
                              ))
                           .then(
                              ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("offalpha")
                                          .then(Commands.literal("status").executes(ctx -> printOffAlphaStatus())))
                                       .then(Commands.literal("reset").executes(ctx -> resetOffAlpha())))
                                    .then(
                                       Commands.literal("set")
                                          .then(
                                             Commands.argument("value", FloatArgumentType.floatArg(0.0F, 1.0F))
                                                .executes(ctx -> setOffAlpha(FloatArgumentType.getFloat(ctx, "value")))
                                          )
                                    ))
                                 .then(
                                    Commands.literal("add")
                                       .then(
                                          Commands.argument("delta", FloatArgumentType.floatArg(-1.0F, 1.0F))
                                             .executes(ctx -> addOffAlpha(FloatArgumentType.getFloat(ctx, "delta")))
                                       )
                                 )
                           ))
                        .then(Commands.literal("walkrange").executes(ctx -> openWalkRangeUi())))
                     .then(Commands.literal("position").executes(ctx -> openPositionUi())))
                  .then(
                     ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(
                                          "smoothing"
                                       )
                                       .then(Commands.literal("status").executes(ctx -> printSmoothingStatus())))
                                    .then(Commands.literal("nearest").executes(ctx -> setSmoothing(false, false, "nearest"))))
                                 .then(Commands.literal("linear").executes(ctx -> setSmoothing(true, false, "linear"))))
                              .then(Commands.literal("nearest_mipmap").executes(ctx -> setSmoothing(false, true, "nearest_mipmap"))))
                           .then(Commands.literal("linear_mipmap").executes(ctx -> setSmoothing(true, true, "linear_mipmap"))))
                        .then(
                           Commands.literal("set")
                              .then(
                                 Commands.argument("smooth", BoolArgumentType.bool())
                                    .then(
                                       Commands.argument("mipmap", BoolArgumentType.bool())
                                          .executes(
                                             ctx -> setSmoothing(BoolArgumentType.getBool(ctx, "smooth"), BoolArgumentType.getBool(ctx, "mipmap"), "custom")
                                          )
                                    )
                              )
                        )
                  ))
               .then(
                  ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("compare").executes(ctx -> toggleCompare()))
                           .then(Commands.literal("on").executes(ctx -> setCompare(true))))
                        .then(Commands.literal("off").executes(ctx -> setCompare(false))))
                     .then(Commands.literal("status").executes(ctx -> printCompareStatus()))
               )
         );
   }

   private static int setX(int value) {
      NanosuitHudOverlay.setHudOffsetX(value);
      sendClientMessage("Nanosuit HUD offset X = " + value);
      return 1;
   }

   private static int setY(int value) {
      NanosuitHudOverlay.setHudOffsetY(value);
      sendClientMessage("Nanosuit HUD offset Y = " + value);
      return 1;
   }

   private static int move(int dx, int dy) {
      NanosuitHudOverlay.moveHudOffset(dx, dy);
      printPos();
      return 1;
   }

   private static int reset() {
      NanosuitHudOverlay.resetHudOffset();
      sendClientMessage("Nanosuit HUD offset reset");
      return 1;
   }

   private static int printPos() {
      sendClientMessage("Nanosuit HUD offset: x=" + NanosuitHudOverlay.getHudOffsetX() + ", y=" + NanosuitHudOverlay.getHudOffsetY());
      return 1;
   }

   private static int toggleVisible() {
      boolean enabled = NanosuitHudOverlay.toggleHudEnabled();
      sendClientMessage("Nanosuit HUD visible: " + (enabled ? "ON" : "OFF"));
      return 1;
   }

   private static int setVisible(boolean enabled) {
      NanosuitHudOverlay.setHudEnabled(enabled);
      sendClientMessage("Nanosuit HUD visible: " + (enabled ? "ON" : "OFF"));
      return 1;
   }

   private static int printVisibleStatus() {
      sendClientMessage("Nanosuit HUD visible: " + (NanosuitHudOverlay.isHudEnabled() ? "ON" : "OFF"));
      return 1;
   }

   private static int printHudStatus() {
      sendClientMessage(
         "Nanosuit HUD status: pos=("
            + NanosuitHudOverlay.getHudOffsetX()
            + ","
            + NanosuitHudOverlay.getHudOffsetY()
            + "), visible="
            + (NanosuitHudOverlay.isHudEnabled() ? "ON" : "OFF")
            + ", scale="
            + String.format(Locale.ROOT, "%.3f", NanosuitHudOverlay.getHudScale())
            + ", offAlpha="
            + String.format(Locale.ROOT, "%.3f", NanosuitHudOverlay.getOffLayerAlpha())
            + ", smooth="
            + NanosuitHudOverlay.isTextureSmooth()
            + ", mipmap="
            + NanosuitHudOverlay.isTextureMipmap()
            + ", compare="
            + (EnergyBarCompareOverlay.isEnabled() ? "ON" : "OFF")
      );
      return 1;
   }

   private static int printScaleStatus() {
      sendClientMessage("Nanosuit HUD scale = " + String.format(Locale.ROOT, "%.3f", NanosuitHudOverlay.getHudScale()));
      return 1;
   }

   private static int setScale(float value) {
      NanosuitHudOverlay.setHudScale(value);
      sendClientMessage("Nanosuit HUD scale set to " + String.format(Locale.ROOT, "%.3f", NanosuitHudOverlay.getHudScale()));
      return 1;
   }

   private static int addScale(float delta) {
      float current = NanosuitHudOverlay.addHudScale(delta);
      sendClientMessage("Nanosuit HUD scale now " + String.format(Locale.ROOT, "%.3f", current));
      return 1;
   }

   private static int resetScale() {
      NanosuitHudOverlay.resetHudScale();
      sendClientMessage("Nanosuit HUD scale reset to " + String.format(Locale.ROOT, "%.3f", NanosuitHudOverlay.getHudScale()));
      return 1;
   }

   private static int printOffAlphaStatus() {
      sendClientMessage("Nanosuit HUD off layer alpha = " + String.format(Locale.ROOT, "%.3f", NanosuitHudOverlay.getOffLayerAlpha()));
      return 1;
   }

   private static int setOffAlpha(float value) {
      NanosuitHudOverlay.setOffLayerAlpha(value);
      sendClientMessage("Nanosuit HUD off layer alpha set to " + String.format(Locale.ROOT, "%.3f", NanosuitHudOverlay.getOffLayerAlpha()));
      return 1;
   }

   private static int addOffAlpha(float delta) {
      float current = NanosuitHudOverlay.addOffLayerAlpha(delta);
      sendClientMessage("Nanosuit HUD off layer alpha now " + String.format(Locale.ROOT, "%.3f", current));
      return 1;
   }

   private static int resetOffAlpha() {
      NanosuitHudOverlay.resetOffLayerAlpha();
      sendClientMessage("Nanosuit HUD off layer alpha reset to " + String.format(Locale.ROOT, "%.3f", NanosuitHudOverlay.getOffLayerAlpha()));
      return 1;
   }

   private static int openWalkRangeUi() {
      Minecraft minecraft = Minecraft.getInstance();
      minecraft.execute(() -> minecraft.setScreen(new NanosuitWalkRangeSettingsScreen(minecraft.screen)));
      return 1;
   }

   private static int openPositionUi() {
      Minecraft minecraft = Minecraft.getInstance();
      minecraft.execute(() -> minecraft.setScreen(new NanosuitHudPositionSettingsScreen(minecraft.screen)));
      return 1;
   }

   private static int setSmoothing(boolean smooth, boolean mipmap, String profileName) {
      NanosuitHudOverlay.setTextureFilter(smooth, mipmap);
      sendClientMessage("Nanosuit HUD smoothing = " + profileName + " (smooth=" + smooth + ", mipmap=" + mipmap + ")");
      return 1;
   }

   private static int printSmoothingStatus() {
      sendClientMessage("Nanosuit HUD smoothing status: smooth=" + NanosuitHudOverlay.isTextureSmooth() + ", mipmap=" + NanosuitHudOverlay.isTextureMipmap());
      return 1;
   }

   private static int toggleCompare() {
      boolean enabled = EnergyBarCompareOverlay.toggle();
      sendClientMessage("Nanosuit compare overlay: " + (enabled ? "ON" : "OFF"));
      return 1;
   }

   private static int setCompare(boolean enabled) {
      EnergyBarCompareOverlay.setEnabled(enabled);
      sendClientMessage("Nanosuit compare overlay: " + (enabled ? "ON" : "OFF"));
      return 1;
   }

   private static int printCompareStatus() {
      sendClientMessage("Nanosuit compare overlay: " + (EnergyBarCompareOverlay.isEnabled() ? "ON" : "OFF"));
      return 1;
   }

   private static void sendClientMessage(String text) {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.player != null) {
         minecraft.player.sendSystemMessage(Component.literal(text));
      }
   }
}
