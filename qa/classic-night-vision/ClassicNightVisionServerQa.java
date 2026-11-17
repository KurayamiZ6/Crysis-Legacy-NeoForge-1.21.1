package local.crysisqa.classicvision;

import com.mojang.authlib.GameProfile;
import io.github.painworks.crysismod.nanovision.ClassicNightVision;
import io.github.painworks.crysismod.nanovision.ClassicNightVisionBattery;
import io.github.painworks.crysismod.nanovision.server.ClassicNightVisionServerState;
import io.github.painworks.crysismod.nanovision.server.NanovisionServerState;
import io.github.painworks.crysismod.nanosuit.energy.NanosuitCapabilities;
import io.github.painworks.crysismod.registry.ModItems;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@Mod("crysisclassicnightvisionserverqa")
public final class ClassicNightVisionServerQa {
   private static final String PREFIX = "CLASSIC_NIGHT_VISION_SERVER_QA_";
   private int failures;

   public ClassicNightVisionServerQa() {
      NeoForge.EVENT_BUS.addListener(this::test);
   }

   private void test(ServerStartedEvent event) {
      try {
         var server = event.getServer();
         check("DEDICATED", server instanceof DedicatedServer);
         var player = new FakePlayer(server.overworld(), new GameProfile(UUID.randomUUID(), "ClassicVisionQA"));
         ClassicNightVisionServerState.toggle(player);
         check("NO_ARMOR_REFUSES", battery(player) == null);

         equip(player, true);
         var suit = NanosuitCapabilities.get(player);
         suit.setEnergy(0);
         ClassicNightVisionServerState.toggle(player);
         var battery = battery(player);
         check("N1_ACTIVATES_WITHOUT_MAIN_ENERGY", battery != null && battery.active() && suit.getEnergy() == 0.0F);
         if (battery == null) {
            throw new IllegalStateException("Battery was never created");
         }
         suit.setEnergy(100);
         NanovisionServerState.setActiveFromClient(player, true);
         check("N1_THERMAL_DISABLED", !NanovisionServerState.isActive(player));
         suit.setEnergy(37);
         tick(player, 1199);
         check("LAST_ACTIVE_TICK", battery.active() && battery.charge() == 1);
         tick(player, 1);
         check("EMPTY_AUTO_OFF", !battery.active() && battery.charge() == 0);
         ClassicNightVisionServerState.toggle(player);
         check("EMPTY_REJECTS_TOGGLE", !battery.active() && battery.charge() == 0);
         tick(player, 299);
         check("RECHARGE_T299", !battery.active() && battery.charge() == 1196);
         tick(player, 1);
         check("RECHARGE_T300", !battery.active() && battery.charge() == 1200 && battery(player) == null);
         check("MAIN_ENERGY_UNCHANGED", suit.getEnergy() == 37.0F);

         ClassicNightVisionServerState.toggle(player);
         tick(player, 50);
         battery = battery(player);
         equip(player, false);
         tick(player, 1);
         check("EQUIPMENT_CHANGE_REMOVES_CLASSIC", !battery.active() && battery.charge() == 1154);
         ClassicNightVisionServerState.toggle(player);
         check("N2_CLASSIC_REFUSED", !battery.active());
         NanovisionServerState.setActiveFromClient(player, true);
         check("N2_THERMAL_ACCEPTED", NanovisionServerState.isActive(player));
         equip(player, true);
         ClassicNightVisionServerState.toggle(player);
         check("REARM_USES_SESSION_BATTERY", battery.active() && battery.charge() == 1154);
      } catch (ReflectiveOperationException | RuntimeException failure) {
         failures++;
         failure.printStackTrace();
      } finally {
         System.out.println(PREFIX + "COMPLETE failures=" + failures);
         event.getServer().halt(false);
      }
   }

   private static void equip(ServerPlayer player, boolean classic) {
      player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(
         classic ? ModItems.NANOGEO_NANOSUIT_HELMET.get() : ModItems.NANOSUIT_2_HELMET.get()));
      player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(
         classic ? ModItems.NANOGEO_NANOSUIT_CHESTPLATE.get() : ModItems.NANOSUIT_2_CHESTPLATE.get()));
      player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(
         classic ? ModItems.NANOGEO_NANOSUIT_LEGGINGS.get() : ModItems.NANOSUIT_2_LEGGINGS.get()));
      player.setItemSlot(EquipmentSlot.FEET, new ItemStack(
         classic ? ModItems.NANOGEO_NANOSUIT_BOOTS.get() : ModItems.NANOSUIT_2_BOOTS.get()));
   }

   private static void tick(ServerPlayer player, int amount) {
      for (int i = 0; i < amount; i++) {
         ClassicNightVisionServerState.onPlayerTick(new PlayerTickEvent.Post(player));
      }
   }

   private static ClassicNightVisionBattery battery(ServerPlayer player) throws ReflectiveOperationException {
      var states = ClassicNightVisionServerState.class.getDeclaredField("BATTERIES");
      states.setAccessible(true);
      return (ClassicNightVisionBattery)((Map<?, ?>)states.get(null)).get(player.getUUID());
   }

   private void check(String test, boolean pass) {
      if (!pass) {
         failures++;
      }
      System.out.println(PREFIX + test + (pass ? "_PASS" : "_FAIL"));
   }
}
