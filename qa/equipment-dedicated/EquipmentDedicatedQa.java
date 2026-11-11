package local.crysisqa.equipmentdedicated;

import io.github.painworks.crysismod.nanosuit.NanosuitMode;
import io.github.painworks.crysismod.nanosuit.client.render.NanosuitCloakRender;
import io.github.painworks.crysismod.nanosuit.energy.NanosuitCapabilities;
import io.github.painworks.crysismod.nanosuit.handler.NanosuitPlayerHandler;
import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@Mod("crysisequipmentdedicatedqa")
public final class EquipmentDedicatedQa {
    private static final String OWNER = "GearWearer";
    private static final String OBSERVER = "GearObserver";
    private static final String PREFIX = "EQUIPMENT_DEDICATED_QA_";
    private static final String[] PIECES = {"helmet", "chestplate", "leggings", "boots"};
    private static final EquipmentSlot[] SLOTS = {
        EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    private int ticks;
    private int failures;

    public EquipmentDedicatedQa() {
        if (!Boolean.getBoolean("crysis.equipment.dedicated.qa")) {
            return;
        }
        if (FMLEnvironment.dist == Dist.DEDICATED_SERVER) {
            NeoForge.EVENT_BUS.addListener(this::serverTick);
        } else {
            ClientProbe.register();
        }
    }

    private void serverTick(ServerTickEvent.Post event) {
        var server = event.getServer();
        if (ticks >= 475) {
            if (++ticks == 510) {
                server.halt(false);
            }
            return;
        }
        var players = server.getPlayerList();
        ServerPlayer owner = players.getPlayerByName(OWNER);
        ServerPlayer observer = players.getPlayerByName(OBSERVER);
        if (owner == null || observer == null) {
            return;
        }
        var state = NanosuitCapabilities.get(owner);
        switch (++ticks) {
            case 20 -> {
                check("DEDICATED_TWO_CLIENTS", server instanceof DedicatedServer && players.getPlayerCount() == 2);
                owner.setGameMode(GameType.SURVIVAL);
                observer.setGameMode(GameType.SURVIVAL);
                var level = owner.serverLevel();
                level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, server);
                level.setDayTime(6000);
                for (int x = -2; x <= 2; x++) {
                    for (int z = -2; z <= 6; z++) {
                        level.setBlockAndUpdate(new BlockPos(x, 180, z), Blocks.STONE.defaultBlockState());
                        level.setBlockAndUpdate(new BlockPos(x, 181, z), Blocks.AIR.defaultBlockState());
                        level.setBlockAndUpdate(new BlockPos(x, 182, z), Blocks.AIR.defaultBlockState());
                    }
                }
                owner.connection.teleport(0.5, 181.0, 0.5, 0, 0);
                observer.connection.teleport(0.5, 181.0, 4.5, 180, 0);
                state.setEnergy(72.0F);
                state.setMode(NanosuitMode.NONE);
                state.setRegenDelayTicks(1000);
                equip(owner, 1);
                check("SERVER_NANOSUIT_1", wears(owner, 1));
            }
            case 160 -> {
                equip(owner, 2);
                check("SERVER_NANOSUIT_2", wears(owner, 2));
                check("ENERGY_PRESERVED", state.getEnergy() == 72.0F);
            }
            case 250 -> check("CLOAK_ACCEPTED", NanosuitPlayerHandler.tryToggleMode(owner, NanosuitMode.CLOAK));
            case 255 -> check("CLOAK_DRAIN", state.getMode() == NanosuitMode.CLOAK
                && owner.isInvisible() && state.getEnergy() < 72.0F && state.getEnergy() > 0.0F);
            case 340 -> {
                float before = state.getEnergy();
                equip(owner, 1);
                check("SWITCH_BACK_ACTIVE", wears(owner, 1) && state.getEnergy() == before
                    && state.getMode() == NanosuitMode.CLOAK && owner.isInvisible());
            }
            case 430 -> check("CLOAK_DISABLED", NanosuitPlayerHandler.tryToggleMode(owner, NanosuitMode.CLOAK));
            case 435 -> check("CLOAK_CLEARED", state.getMode() == NanosuitMode.NONE && !owner.isInvisible());
            case 449 -> {
                equip(owner, 2);
                check("COOLDOWN_T19", !NanosuitPlayerHandler.tryToggleMode(owner, NanosuitMode.ARMOR));
            }
            case 450 -> check("COOLDOWN_T20", NanosuitPlayerHandler.tryToggleMode(owner, NanosuitMode.ARMOR));
            case 465 -> check("ARMOR_ACTIVE", wears(owner, 2) && state.getMode() == NanosuitMode.ARMOR
                && owner.hasEffect(MobEffects.ABSORPTION) && state.getEnergy() > 0.0F);
            case 470 -> check("ARMOR_DISABLED", NanosuitPlayerHandler.tryToggleMode(owner, NanosuitMode.ARMOR));
            case 475 -> {
                check("ARMOR_CLEARED", state.getMode() == NanosuitMode.NONE && !owner.hasEffect(MobEffects.ABSORPTION));
                System.out.println(PREFIX + "SERVER_COMPLETE failures=" + failures);
            }
            default -> { }
        }
    }

    private void check(String name, boolean result) {
        if (!result) {
            failures++;
        }
        System.out.println(PREFIX + name + (result ? "_PASS" : "_FAIL"));
    }

    private static ResourceLocation itemId(int generation, int piece) {
        String base = generation == 1 ? "nanogeo_nanosuit_" : "nanosuit_2_";
        return ResourceLocation.fromNamespaceAndPath("crysismod", base + PIECES[piece]);
    }

    private static void equip(Player player, int generation) {
        for (int i = 0; i < SLOTS.length; i++) {
            player.setItemSlot(SLOTS[i], new ItemStack(BuiltInRegistries.ITEM.get(itemId(generation, i))));
        }
    }

    private static boolean wears(Player player, int generation) {
        var profile = NanosuitArmorUtil.getEquippedProfile(player);
        if (profile == null || !profile.generation().name().equals("NANOSUIT_" + generation)) {
            return false;
        }
        for (int i = 0; i < SLOTS.length; i++) {
            if (!BuiltInRegistries.ITEM.getKey(player.getItemBySlot(SLOTS[i]).getItem()).equals(itemId(generation, i))) {
                return false;
            }
        }
        return true;
    }

    private static final class ClientProbe {
        private static final int[] GENERATIONS = {1, 2, 2, 1, 1, 2};
        private int phase;
        private int stable;
        private int delay;
        private int pendingCapture = -1;
        private int ticks;
        private boolean connected;

        private static void register() {
            ClientProbe probe = new ClientProbe();
            NeoForge.EVENT_BUS.addListener(probe::tick);
            NeoForge.EVENT_BUS.addListener(probe::capture);
        }

        private void tick(ClientTickEvent.Post event) {
            Minecraft minecraft = Minecraft.getInstance();
            if (phase == GENERATIONS.length) {
                if (++delay == 25) {
                    minecraft.stop();
                }
                return;
            }
            if (connected && minecraft.getConnection() == null) {
                System.out.println(PREFIX + "CLIENT_DISCONNECTED_FAIL phase=" + phase);
                minecraft.stop();
                return;
            }
            if (minecraft.player == null || minecraft.level == null || minecraft.screen != null) {
                return;
            }
            connected = true;
            boolean owner = OWNER.equals(minecraft.player.getGameProfile().getName());
            String side = owner ? "OWNER" : "OBSERVER";
            AbstractClientPlayer wearer = owner ? minecraft.player : minecraft.level.players().stream()
                .filter(player -> OWNER.equals(player.getGameProfile().getName()))
                .findFirst().orElse(null);
            boolean cloaked = phase == 2 || phase == 3;
            if (!owner && ++ticks % 100 == 0 && wearer != null) {
                System.out.println(PREFIX + "OBSERVER_STATE phase=" + phase + " profile="
                    + NanosuitArmorUtil.getEquippedProfile(wearer) + " invisible=" + wearer.isInvisible()
                    + " distance=" + minecraft.player.distanceTo(wearer));
            }
            if (wearer == null || !wears(wearer, GENERATIONS[phase]) || wearer.isInvisible() != cloaked
                    || (!owner && NanosuitCloakRender.isActive(wearer) != cloaked)) {
                stable = 0;
                return;
            }
            var state = NanosuitCapabilities.get(wearer);
            if (owner) {
                NanosuitMode expectedMode = cloaked ? NanosuitMode.CLOAK : phase == 5 ? NanosuitMode.ARMOR : NanosuitMode.NONE;
                boolean expectedEnergy = phase < 2 ? state.getEnergy() == 72
                    : state.getEnergy() < 72 && state.getEnergy() > 0;
                if (state.getMode() != expectedMode || !expectedEnergy) {
                    stable = 0;
                    return;
                }
            }
            minecraft.options.setCameraType(CameraType.FIRST_PERSON);
            if (++stable < 3) {
                return;
            }
            System.out.println(PREFIX + side + "_PHASE_" + phase + "_PASS generation=" + GENERATIONS[phase]
                + " invisible=" + wearer.isInvisible() + (owner ? " energy=" + state.getEnergy() + " mode=" + state.getMode() : ""));
            if (!owner && phase <= 2) {
                minecraft.player.lookAt(EntityAnchorArgument.Anchor.EYES, wearer.getEyePosition());
                pendingCapture = phase;
            }
            stable = 0;
            if (++phase == GENERATIONS.length) {
                System.out.println(PREFIX + side + "_COMPLETE failures=0");
            }
        }

        private void capture(RenderGuiEvent.Post event) {
            Minecraft minecraft = Minecraft.getInstance();
            if (pendingCapture >= 0 && minecraft.screen == null) {
                Screenshot.grab(minecraft.gameDirectory, "dedicated-equipment-" + pendingCapture + ".png",
                    minecraft.getMainRenderTarget(), message -> { });
                pendingCapture = -1;
            }
        }
    }
}
