package local.crysisqa;

import io.github.painworks.crysismod.nanosuit.NanosuitMode;
import io.github.painworks.crysismod.nanosuit.energy.NanosuitCapabilities;
import io.github.painworks.crysismod.nanosuit.network.NanosuitSyncManager;
import io.github.painworks.crysismod.registry.ModItems;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@Mod("crysismodeoverlayqa")
public final class ModeOverlayQa {
    private static final String[] NAMES = {"strength-fps", "strength-tps", "speed-fps", "speed-tps",
        "n1-armor-fps", "n1-armor-tps", "n2-armor-fps", "n2-armor-tps"};
    private volatile int phase;
    private int phaseStart = -1;
    private int sample;
    private int endTick = -1;
    private int timeout;
    private boolean finished;

    public ModeOverlayQa() {
        NeoForge.EVENT_BUS.addListener(this::serverTick);
        NeoForge.EVENT_BUS.addListener(this::render);
        NeoForge.EVENT_BUS.addListener(this::clientTick);
    }

    private void clientTick(ClientTickEvent.Post event) {
        if (!finished && ++timeout > 2400) {
            System.out.println("MODE_OVERLAY_QA_TIMEOUT");
            finished = true;
            Minecraft.getInstance().stop();
        }
    }

    private NanosuitMode mode() {
        return phase < 2 ? NanosuitMode.STRENGTH : phase < 4 ? NanosuitMode.SPEED : NanosuitMode.ARMOR;
    }

    private void serverTick(ServerTickEvent.Post event) {
        var players = event.getServer().getPlayerList().getPlayers();
        if (players.isEmpty() || phase >= NAMES.length) return;
        var p = players.getFirst();
        boolean n2 = phase >= 6;
        p.serverLevel().setDayTime(6000);
        p.serverLevel().getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, event.getServer());
        p.setItemSlot(EquipmentSlot.HEAD, new ItemStack(n2 ? ModItems.NANOSUIT_2_HELMET.get() : ModItems.NANOGEO_NANOSUIT_HELMET.get()));
        p.setItemSlot(EquipmentSlot.CHEST, new ItemStack(n2 ? ModItems.NANOSUIT_2_CHESTPLATE.get() : ModItems.NANOGEO_NANOSUIT_CHESTPLATE.get()));
        p.setItemSlot(EquipmentSlot.LEGS, new ItemStack(n2 ? ModItems.NANOSUIT_2_LEGGINGS.get() : ModItems.NANOGEO_NANOSUIT_LEGGINGS.get()));
        p.setItemSlot(EquipmentSlot.FEET, new ItemStack(n2 ? ModItems.NANOSUIT_2_BOOTS.get() : ModItems.NANOGEO_NANOSUIT_BOOTS.get()));
        p.getInventory().setItem(p.getInventory().selected, ItemStack.EMPTY);
        p.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        var data = NanosuitCapabilities.get(p);
        data.setEnergy(100);
        data.setMode(mode());
        NanosuitSyncManager.syncToClient(p);
    }

    private void render(RenderGuiEvent.Post event) {
        var mc = Minecraft.getInstance();
        if (finished || mc.player == null || mc.screen != null) return;
        if (phase >= NAMES.length) {
            if (endTick < 0) endTick = mc.player.tickCount;
            if (mc.player.tickCount - endTick >= 25) {
                finished = true;
                System.out.println("MODE_OVERLAY_QA_COMPLETE captures=24");
                mc.stop();
            }
            return;
        }
        mc.options.setCameraType(phase % 2 == 0 ? CameraType.FIRST_PERSON : CameraType.THIRD_PERSON_FRONT);
        mc.player.setYRot(0);
        mc.player.setXRot(0);
        if (NanosuitCapabilities.get(mc.player).getMode() != mode()) return;
        if (phaseStart < 0) phaseStart = mc.player.tickCount;
        if (mc.player.tickCount - phaseStart < 50 + sample * 12) return;
        String name = NAMES[phase] + "-" + sample + ".png";
        Screenshot.grab(mc.gameDirectory, name, mc.getMainRenderTarget(), ignored -> {});
        System.out.println("MODE_OVERLAY_QA_CAPTURE " + name + " tick=" + mc.player.tickCount);
        if (++sample == 3) {
            phase++;
            phaseStart = -1;
            sample = 0;
        }
    }
}
