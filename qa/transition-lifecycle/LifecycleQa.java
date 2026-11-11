package local.crysisqa.lifecycle;

import io.github.painworks.crysismod.nanosuit.NanosuitMode;
import io.github.painworks.crysismod.nanosuit.NanosuitProfile;
import io.github.painworks.crysismod.nanosuit.energy.NanosuitCapabilities;
import io.github.painworks.crysismod.nanosuit.energy.NanosuitEnergy;
import io.github.painworks.crysismod.nanosuit.handler.NanosuitPlayerHandler;
import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import io.github.painworks.crysismod.registry.ModItems;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@Mod("crysistransitionlifecycleqa")
public final class LifecycleQa {
    private static final int SAVE_RECOVERY_TICK = 36;
    private static final Set<String> PERSISTED_KEYS = Set.of("Energy", "Mode", "RegenDelayTicks", "RecoveryTicks");
    private final boolean reopening = "reopen".equals(System.getProperty("crysis.lifecycle.phase"));
    private final boolean useOverlay = Boolean.getBoolean("crysis.transition.qa");
    private volatile boolean clientReady;
    private volatile boolean prepared;
    private volatile boolean serverRestored;
    private volatile boolean serverFailed;
    private int serverFailures;
    private int clientFailures;
    private int clientTicks;
    private boolean observedClientLock;
    private boolean observedWorld;
    private boolean stopped;

    public LifecycleQa() {
        NeoForge.EVENT_BUS.addListener(this::onLogin);
        NeoForge.EVENT_BUS.addListener(this::onLogout);
        NeoForge.EVENT_BUS.addListener(this::tickServer);
        NeoForge.EVENT_BUS.addListener(this::tickClient);
    }

    private void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!reopening || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        NanosuitEnergy suit = NanosuitCapabilities.get(player);
        check("REOPEN_PERSISTED_LOCK", suit.isRecoveryLocked() && suit.getEnergy() == 0
            && suit.getRecoveryTicks() >= SAVE_RECOVERY_TICK && suit.getRecoveryTicks() < 81
            && suit.getMode() == NanosuitMode.NONE && !suit.isSprintBoostReady(), snapshot(suit));
        check("REOPEN_EQUIPMENT_PROFILE", NanosuitArmorUtil.getEquippedProfile(player) == NanosuitProfile.LEGACY_CURRENT,
            "profile=" + NanosuitArmorUtil.getEquippedProfile(player));
        check("REOPEN_LOCK_DENIES_MODE", !NanosuitPlayerHandler.tryToggleMode(player, NanosuitMode.ARMOR), snapshot(suit));
    }

    private void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!reopening && event.getEntity() instanceof ServerPlayer player) {
            System.out.println("LIFECYCLE_QA_LOGOUT " + snapshot(NanosuitCapabilities.get(player)));
        }
    }

    private void tickServer(ServerTickEvent.Post event) {
        if (!clientReady || serverFailed || event.getServer().getPlayerList().getPlayers().isEmpty()) {
            return;
        }
        ServerPlayer player = event.getServer().getPlayerList().getPlayers().getFirst();
        NanosuitEnergy suit = NanosuitCapabilities.get(player);
        if (reopening) {
            if (!serverRestored && !suit.isRecoveryLocked()) {
                check("REOPEN_SERVER_RESTORED", suit.getEnergy() == 100 && suit.getMode() == NanosuitMode.NONE, snapshot(suit));
                serverRestored = true;
            }
        } else if (!prepared) {
            try {
                prepare(player, suit);
                prepared = true;
            } catch (ReflectiveOperationException | RuntimeException error) {
                check("PREPARE", false, error.toString());
                serverFailed = true;
            }
        }
    }

    private void prepare(ServerPlayer player, NanosuitEnergy suit) throws ReflectiveOperationException {
        player.setGameMode(GameType.SURVIVAL);
        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.NANOGEO_NANOSUIT_HELMET.get()));
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.NANOGEO_NANOSUIT_CHESTPLATE.get()));
        player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.NANOGEO_NANOSUIT_LEGGINGS.get()));
        player.setItemSlot(EquipmentSlot.FEET, new ItemStack(ModItems.NANOGEO_NANOSUIT_BOOTS.get()));
        suit.setEnergy(100);
        check("PREPARE_MODE_ACCEPTED", NanosuitPlayerHandler.tryToggleMode(player, NanosuitMode.ARMOR), snapshot(suit));
        selectTestProfile(player, "QA_SHARED");
        check("COOLDOWN_BEFORE_DIMENSION", !NanosuitPlayerHandler.tryToggleMode(player, NanosuitMode.CLOAK), snapshot(suit));

        ServerLevel overworld = player.serverLevel();
        ServerLevel nether = player.server.getLevel(Level.NETHER);
        nether.setBlockAndUpdate(new BlockPos(8, 179, 2), Blocks.STONE.defaultBlockState());
        nether.setBlockAndUpdate(new BlockPos(8, 180, 2), Blocks.AIR.defaultBlockState());
        nether.setBlockAndUpdate(new BlockPos(8, 181, 2), Blocks.AIR.defaultBlockState());
        boolean enteredNether = player.teleportTo(nether, 8.5, 180, 2.5, Set.of(), 0, 0);
        check("DIMENSION_TRAVEL", enteredNether && player.serverLevel() == nether,
            "dimension=" + player.serverLevel().dimension().location());
        boolean acceptedAfterDimension = NanosuitPlayerHandler.tryToggleMode(player, NanosuitMode.CLOAK);
        check("DIMENSION_HISTORICAL_COOLDOWN_RESET", enteredNether && acceptedAfterDimension && suit.getEnergy() == 100,
            "accepted=" + acceptedAfterDimension + " " + snapshot(suit));

        suit.setEnergy(0);
        suit.setRecoveryTicks(SAVE_RECOVERY_TICK);
        selectTestProfile(player, "QA_RESTRICTED");
        boolean returned = player.teleportTo(overworld, 8.5, 181, 2.5, Set.of(), 0, 0);
        check("DIMENSION_LOCK_RETAINED", returned && player.serverLevel() == overworld
            && suit.isRecoveryLocked() && suit.getEnergy() == 0 && suit.getRecoveryTicks() == SAVE_RECOVERY_TICK
            && !NanosuitPlayerHandler.tryToggleMode(player, NanosuitMode.ARMOR), snapshot(suit));
        check("PERSISTED_KEYS", suit.serializeNBT(player.registryAccess()).getAllKeys().equals(PERSISTED_KEYS),
            "keys=" + suit.serializeNBT(player.registryAccess()).getAllKeys());
    }

    private void selectTestProfile(ServerPlayer player, String name) throws ReflectiveOperationException {
        if (useOverlay) {
            NanosuitProfile profile = NanosuitProfile.valueOf(name);
            NanosuitArmorUtil.class.getMethod("qaSelectProfile", ServerPlayer.class, NanosuitProfile.class)
                .invoke(null, player, profile);
            check("PROFILE_" + name, NanosuitArmorUtil.getEquippedProfile(player) == profile, "profile=" + profile);
        }
    }

    private void tickClient(ClientTickEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        if (stopped) {
            return;
        }
        if (client.player != null && client.level != null) {
            var suit = NanosuitCapabilities.get(client.player);
            if (!observedClientLock && suit.isRecoveryLocked() && suit.getEnergy() < 100 && suit.getMode() == NanosuitMode.NONE) {
                observedClientLock = true;
                System.out.println("LIFECYCLE_QA_CLIENT_LOCK_PASS " + snapshot(suit) + " screen="
                    + (client.screen == null ? "world" : client.screen.getClass().getSimpleName()));
            }
            if (client.screen == null) {
                if (!observedWorld) {
                    observedWorld = true;
                    System.out.println("LIFECYCLE_QA_CLIENT_WORLD " + snapshot(suit));
                }
                clientReady = true;
                if (!reopening && prepared && observedClientLock) {
                    finish(client);
                } else if (reopening && serverRestored && suit.getEnergy() == 100 && !suit.isRecoveryLocked()) {
                    if (!observedClientLock) {
                        clientFailures++;
                        System.out.println("LIFECYCLE_QA_CLIENT_LOCK_FAIL lock packet unobserved");
                    }
                    System.out.println("LIFECYCLE_QA_CLIENT_RESTORED_PASS " + snapshot(suit));
                    finish(client);
                }
            }
        }
        if (!stopped && (serverFailed || ++clientTicks > 1800)) {
            clientFailures++;
            System.out.println("LIFECYCLE_QA_UNFINISHED_FAIL prepared=" + prepared + " restored=" + serverRestored);
            finish(client);
        }
    }

    private void finish(Minecraft client) {
        stopped = true;
        System.out.println("LIFECYCLE_QA_" + (reopening ? "REOPEN" : "PREPARE")
            + "_COMPLETE failures=" + (serverFailures + clientFailures));
        client.stop();
    }

    private void check(String label, boolean passed, String details) {
        if (!passed) {
            serverFailures++;
        }
        System.out.println("LIFECYCLE_QA_" + label + (passed ? "_PASS " : "_FAIL ") + details);
    }

    private static String snapshot(NanosuitEnergy suit) {
        return "energy=" + suit.getEnergy() + " mode=" + suit.getMode() + " lock=" + suit.isRecoveryLocked()
            + " recovery=" + suit.getRecoveryTicks() + " sprint=" + suit.isSprintBoostReady();
    }
}
