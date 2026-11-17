package local.crysisqa.classicvision;

import io.github.painworks.crysismod.client.nanovision.ClassicNightVisionClient;
import io.github.painworks.crysismod.client.nanovision.NanovisionKeybinds;
import io.github.painworks.crysismod.client.nanovision.NanovisionState;
import io.github.painworks.crysismod.nanovision.ClassicNightVision;
import io.github.painworks.crysismod.nanovision.ClassicNightVisionBattery;
import io.github.painworks.crysismod.nanovision.network.ToggleClassicNightVisionPacket;
import io.github.painworks.crysismod.nanovision.server.ClassicNightVisionServerState;
import io.github.painworks.crysismod.nanovision.server.NanovisionServerState;
import io.github.painworks.crysismod.nanosuit.NanosuitMode;
import io.github.painworks.crysismod.nanosuit.energy.NanosuitCapabilities;
import io.github.painworks.crysismod.nanosuit.network.NanosuitNetwork;
import io.github.painworks.crysismod.registry.ModItems;
import java.lang.reflect.Field;
import java.util.Map;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@Mod("crysisclassicnightvisionqa")
public final class ClassicNightVisionQa {
   private static final String PREFIX = "CLASSIC_NIGHT_VISION_QA_";
   private int serverTicks;
   private int firstActiveTick = -1;
   private int firstCharge;
   private int emptyTick = -1;
   private int failures;
   private int clientTicks;
   private int activeFrames;
   private boolean sawClientEmpty;
   private boolean requestedThermal;
   private boolean resultReported;
   private volatile boolean finished;
   private volatile boolean ready;
   private volatile boolean switchedToNanosuit2;

   public ClassicNightVisionQa() {
      NeoForge.EVENT_BUS.addListener(this::tickServer);
      NeoForge.EVENT_BUS.addListener(this::tickClient);
      NeoForge.EVENT_BUS.addListener(this::capture);
   }

   private void tickServer(ServerTickEvent.Post event) {
      if (!ready || finished || event.getServer().getPlayerList().getPlayers().isEmpty()) {
         return;
      }
      ServerPlayer player = event.getServer().getPlayerList().getPlayers().getFirst();
      if (++serverTicks == 1) {
         var level = player.serverLevel();
         level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, event.getServer());
         level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, event.getServer());
         level.setDayTime(18000);
         for (int x = -2; x <= 2; x++) {
            for (int y = 181; y <= 184; y++) {
               level.setBlockAndUpdate(new BlockPos(x, y, 7), (x == 0 ? Blocks.OAK_PLANKS : Blocks.STONE_BRICKS).defaultBlockState());
            }
         }
         player.connection.teleport(0.5, 181.0, 0.5, 0, 0);
         player.setGameMode(GameType.SURVIVAL);
         player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.NANOGEO_NANOSUIT_HELMET.get()));
         player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.NANOGEO_NANOSUIT_CHESTPLATE.get()));
         player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.NANOGEO_NANOSUIT_LEGGINGS.get()));
         player.setItemSlot(EquipmentSlot.FEET, new ItemStack(ModItems.NANOGEO_NANOSUIT_BOOTS.get()));
         var data = NanosuitCapabilities.get(player);
         data.setEnergy(30.0F);
         data.setRegenDelayTicks(2400);
         data.setMode(NanosuitMode.ARMOR);
         check("N1_ALLOWED", ClassicNightVision.canUse(player), "eligible classic equipment");
         return;
      }

      Battery battery = readBattery(player);
      if (firstActiveTick < 0 && battery.active()) {
         firstActiveTick = serverTicks;
         firstCharge = battery.charge();
         check("ACTIVATION", firstCharge >= 1199, "battery=" + firstCharge);
         check("N1_THERMAL_REJECTED", !NanovisionServerState.isActive(player), "thermal=false");
      }
      if (firstActiveTick >= 0 && emptyTick < 0 && !battery.active()) {
         emptyTick = serverTicks;
         int elapsed = emptyTick - firstActiveTick;
         check("EXHAUSTION", battery.charge() == 0 && elapsed == firstCharge,
            "elapsed=" + elapsed + " firstObservedCharge=" + firstCharge + " remaining=" + battery.charge());
         check("ENERGY_INDEPENDENT", NanosuitCapabilities.get(player).getEnergy() == 30.0F, "suit energy=30");
      }
      if (emptyTick >= 0 && !switchedToNanosuit2 && battery.charge() == ClassicNightVisionBattery.FULL_CHARGE) {
         int elapsed = serverTicks - emptyTick;
         check("RECHARGE", elapsed == 300, "elapsed=" + elapsed);
         player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.NANOSUIT_2_HELMET.get()));
         player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.NANOSUIT_2_CHESTPLATE.get()));
         player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.NANOSUIT_2_LEGGINGS.get()));
         player.setItemSlot(EquipmentSlot.FEET, new ItemStack(ModItems.NANOSUIT_2_BOOTS.get()));
         NanosuitCapabilities.get(player).setMode(NanosuitMode.NONE);
         switchedToNanosuit2 = true;
      }
      if (switchedToNanosuit2 && NanovisionServerState.isActive(player)) {
         check("N2_THERMAL_SERVER", !ClassicNightVision.canUse(player) && !battery.active(), "thermal=true classic=false");
         finished = true;
      }
   }

   private void tickClient(ClientTickEvent.Post event) {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.player == null || minecraft.screen != null) {
         return;
      }
      ready = true;
      clientTicks++;
      if (clientTicks == 45) {
         KeyMapping.click(NanovisionKeybinds.TOGGLE_NANOVISION.getKey());
      }
      if (clientTicks > 45 && !ClassicNightVisionClient.isActive() && ClassicNightVisionClient.batteryCharge() == 0) {
         sawClientEmpty = true;
      }
      if (switchedToNanosuit2 && !ClassicNightVision.canUse(minecraft.player) && !requestedThermal) {
         requestedThermal = true;
         NanosuitNetwork.sendToServer(ToggleClassicNightVisionPacket.INSTANCE);
         KeyMapping.click(NanovisionKeybinds.TOGGLE_NANOVISION.getKey());
      }
      if (finished && !resultReported) {
         check("N1_EMPTY_SYNC", sawClientEmpty, "client saw automatic shutdown");
         check("N2_THERMAL_CLIENT", NanovisionState.isActive() && !ClassicNightVisionClient.isActive(), "thermal=true classic=false");
         resultReported = true;
         System.out.println(PREFIX + "COMPLETE failures=" + failures);
         minecraft.stop();
      } else if (clientTicks > 1900 && !resultReported) {
         resultReported = true;
         System.out.println(PREFIX + "COMPLETE failures=1 timeout");
         minecraft.stop();
      }
   }

   private void capture(RenderGuiEvent.Post event) {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.player == null || minecraft.level == null || minecraft.screen != null) {
         return;
      }
      if (clientTicks == 30 && activeFrames == 0) {
         activeFrames = -1;
         Screenshot.grab(minecraft.gameDirectory, "classic-night-vision-off.png", minecraft.getMainRenderTarget(), message -> {});
      }
      if (ClassicNightVisionClient.isActive() && ++activeFrames == 15) {
         Screenshot.grab(minecraft.gameDirectory, "classic-night-vision-on.png", minecraft.getMainRenderTarget(), message -> {});
      }
   }

   private void check(String test, boolean pass, String details) {
      if (!pass) {
         failures++;
      }
      System.out.println(PREFIX + test + (pass ? "_PASS " : "_FAIL ") + details);
   }

   private static Battery readBattery(ServerPlayer player) {
      try {
         Field states = ClassicNightVisionServerState.class.getDeclaredField("BATTERIES");
         states.setAccessible(true);
         var map = (Map<?, ?>)states.get(null);
         if (map.get(player.getUUID()) instanceof ClassicNightVisionBattery battery) {
            return new Battery(battery.active(), battery.charge());
         }
         return new Battery(false, ClassicNightVisionBattery.FULL_CHARGE);
      } catch (ReflectiveOperationException exception) {
         throw new IllegalStateException(exception);
      }
   }

   private record Battery(boolean active, int charge) {}
}
