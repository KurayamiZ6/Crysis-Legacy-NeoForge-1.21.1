package local.crysisqa;

import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.client.input.AimKey;
import io.github.painworks.crysismod.client.nanovision.NanovisionConfig;
import io.github.painworks.crysismod.client.nanovision.NanovisionController;
import io.github.painworks.crysismod.client.nanovision.NanovisionMode;
import io.github.painworks.crysismod.nanosuit.NanosuitGeneration;
import io.github.painworks.crysismod.nanosuit.client.render.NanosuitCloakRender;
import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.world.InteractionHand;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod("crysisequipmentrenderqa")
public final class EquipmentRenderQa {
   private static final int START = 80;
   private static final int CYCLE = 270;
   private static final String FRESH_PACK = "file/FreshAnimations_v1.11.2_1.20+.zip";

   private int ticks;
   private int failures;
   private int captures;
   private Capture pending;

   public EquipmentRenderQa() {
      NeoForge.EVENT_BUS.addListener(this::tick);
      NeoForge.EVENT_BUS.addListener(this::capture);
   }

   private void tick(ClientTickEvent.Post event) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null || mc.level == null || mc.screen != null) {
         return;
      }
      if (++ticks == 45) {
         scene(mc);
      }
      if (ticks < START) {
         return;
      }
      if (ticks == START + 2 * CYCLE) {
         System.out.printf("EQUIPMENT_RENDER_QA_COMPLETE captures=%d failures=%d%n", captures, failures);
         mc.stop();
         return;
      }

      int generation = 1 + (ticks - START) / CYCLE;
      switch ((ticks - START) % CYCLE) {
         case 0 -> {
            equipSuit(mc, generation);
            mc.options.setCameraType(CameraType.FIRST_PERSON);
         }
         case 18, 45 -> mc.player.swing(InteractionHand.MAIN_HAND);
         case 20 -> queue(generation, "punchy-fps", CameraType.FIRST_PERSON, false, false, false);
         case 25 -> thermal(true);
         case 47 -> queue(generation, "thermal-fps", CameraType.FIRST_PERSON, true, false, false);
         case 49 -> NanovisionConfig.setThermalMarkerDebugShowRawMarker(true);
         case 58 -> NanovisionConfig.setThermalMarkerDebugShowRawMarker(false);
         case 62 -> command(mc, "fill 4 181 -2 4 183 -1 minecraft:stone");
         case 80 -> queue(generation, "thermal-occlusion", CameraType.FIRST_PERSON, true, false, false);
         case 82 -> command(mc, "fill 4 181 -2 4 183 -1 minecraft:air");
         case 85 -> mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
         case 102 -> queue(generation, "thermal-tps", CameraType.THIRD_PERSON_BACK, true, false, false);
         case 104 -> {
            mc.options.setCameraType(CameraType.FIRST_PERSON);
            command(mc, "item replace entity @s weapon.mainhand with tacz:modern_kinetic_gun[minecraft:custom_data={GunId:\"tacz:m4a1\",GunFireMode:\"AUTO\",AttachmentSCOPE:{id:\"tacz:attachment\",count:1,components:{\"minecraft:custom_data\":{AttachmentId:\"tacz:sight_552\"}}}}]");
         }
         case 139 -> aim(mc, true);
         case 172 -> queue(generation, "thermal-holo", CameraType.FIRST_PERSON, true, false, true);
         case 176 -> {
            aim(mc, false);
            holdPickaxe(mc);
         }
         case 180 -> command(mc, "nanosuit set_mode cloak");
         case 202 -> queue(generation, "thermal-cloak-fps", CameraType.FIRST_PERSON, true, true, false);
         case 208 -> thermal(false);
         case 228 -> queue(generation, "cloak-fps", CameraType.FIRST_PERSON, false, true, false);
         case 232 -> mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
         case 254 -> queue(generation, "cloak-tps", CameraType.THIRD_PERSON_BACK, false, true, false);
         case 260 -> command(mc, "nanosuit set_mode none");
         default -> { }
      }
   }

   private void queue(int generation, String name, CameraType camera, boolean thermal, boolean cloak, boolean aiming) {
      if (pending != null) {
         failures++;
      }
      pending = new Capture(generation, name, camera, thermal, cloak, aiming);
   }

   private void capture(RenderGuiEvent.Post event) {
      Minecraft mc = Minecraft.getInstance();
      if (pending == null || mc.player == null || mc.screen != null) {
         return;
      }
      var shot = pending;
      pending = null;
      var profile = NanosuitArmorUtil.getEquippedProfile(mc.player);
      var generation = profile == null ? null : profile.generation();
      boolean shader = IrisApi.getInstance().isShaderPackInUse();
      boolean fresh = mc.getResourcePackRepository().getSelectedIds().contains(FRESH_PACK);
      boolean thermal = NanovisionController.isActive();
      boolean cloak = NanosuitCloakRender.isActive(mc.player);
      float aim = IClientPlayerGunOperator.fromLocalPlayer(mc.player).getClientAimingProgress(0.5F);
      CameraType camera = mc.options.getCameraType();
      boolean passed = generation == (shot.generation() == 1 ? NanosuitGeneration.NANOSUIT_1 : NanosuitGeneration.NANOSUIT_2)
         && shader && fresh && thermal == shot.thermal() && cloak == shot.cloak()
         && mc.player.isInvisible() == cloak && camera == shot.camera() && (!shot.aiming() || aim >= 0.99F);
      if (!passed) {
         failures++;
      }
      captures++;
      String name = "nanosuit-" + shot.generation() + "-" + shot.name();
      System.out.printf("EQUIPMENT_RENDER_QA_CAPTURE %s %s profile=%s shaders=%s fresh=%s thermal=%s cloak=%s invisible=%s camera=%s aim=%.2f%n",
         name, passed ? "PASS" : "FAIL", generation, shader, fresh, thermal, cloak, mc.player.isInvisible(), camera, aim);
      Screenshot.grab(mc.gameDirectory, name + ".png", mc.getMainRenderTarget(), message -> { });
   }

   private static void scene(Minecraft mc) {
      mc.options.hideGui = false;
      command(mc, "gamerule sendCommandFeedback false");
      command(mc, "gamerule doDaylightCycle false");
      command(mc, "time set 13000");
      command(mc, "weather clear");
      command(mc, "difficulty normal");
      command(mc, "gamerule doMobSpawning false");
      command(mc, "tp @s 0.5 181 0.5 -90 2");
      command(mc, "kill @e[type=!player,distance=..20]");
      command(mc, "fill 4 181 -4 8 184 4 minecraft:air");
      command(mc, "fill 4 184 -4 8 184 4 minecraft:light[level=15]");
      command(mc, "fill 9 181 -5 9 185 5 minecraft:stone_bricks");
      command(mc, "summon minecraft:cow 6.5 181 -1.8 {NoAI:1b,PersistenceRequired:1b,Silent:1b,Rotation:[90.0f,0.0f]}");
      command(mc, "summon minecraft:zombie 6.5 181 0.5 {NoAI:1b,PersistenceRequired:1b,Silent:1b,IsBaby:0b,Rotation:[90.0f,0.0f]}");
      command(mc, "summon minecraft:skeleton 6.5 181 2.8 {NoAI:1b,PersistenceRequired:1b,Silent:1b,Rotation:[90.0f,0.0f]}");
      holdPickaxe(mc);
      command(mc, "item replace entity @s weapon.offhand with minecraft:air");
      command(mc, "nanosuit set_mode none");
      thermal(false);
   }

   private static void equipSuit(Minecraft mc, int generation) {
      String prefix = generation == 1 ? "nanogeo_nanosuit_" : "nanosuit_2_";
      command(mc, "item replace entity @s armor.head with crysismod:" + prefix + "helmet");
      command(mc, "item replace entity @s armor.chest with crysismod:" + prefix + "chestplate");
      command(mc, "item replace entity @s armor.legs with crysismod:" + prefix + "leggings");
      command(mc, "item replace entity @s armor.feet with crysismod:" + prefix + "boots");
   }

   private static void thermal(boolean enabled) {
      NanovisionController.setMode(enabled ? NanovisionMode.THERMAL_MARKER : NanovisionMode.OFF);
   }

   private static void aim(Minecraft mc, boolean value) {
      if (value) {
         mc.setWindowActive(true);
         mc.mouseHandler.grabMouse();
      }
      AimKey.setControllerAimDown(value);
      AimKey.AIM_KEY.setDown(value);
      IClientPlayerGunOperator.fromLocalPlayer(mc.player).aim(value);
   }

   private static void holdPickaxe(Minecraft mc) {
      command(mc, "item replace entity @s weapon.mainhand with minecraft:diamond_pickaxe");
   }

   private static void command(Minecraft mc, String command) {
      mc.getConnection().sendCommand(command);
   }

   private record Capture(int generation, String name, CameraType camera, boolean thermal, boolean cloak, boolean aiming) {
   }
}
