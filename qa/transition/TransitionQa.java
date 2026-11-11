package local.crysisqa;

import io.github.painworks.crysismod.nanosuit.NanosuitMode;
import io.github.painworks.crysismod.nanosuit.NanosuitProfile;
import io.github.painworks.crysismod.nanosuit.energy.NanosuitCapabilities;
import io.github.painworks.crysismod.nanosuit.handler.NanosuitPlayerHandler;
import io.github.painworks.crysismod.nanosuit.handler.NanosuitSwimBoost;
import io.github.painworks.crysismod.nanosuit.network.NanosuitSyncManager;
import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import io.github.painworks.crysismod.registry.ModItems;
import java.lang.reflect.Field;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@Mod("crysistransitionqa")
public final class TransitionQa {
    private static final Field RUNTIME_STATES = field(NanosuitPlayerHandler.class, "PLAYER_RUNTIME_STATE");
    private static final NanosuitProfile RESTRICTED = NanosuitProfile.QA_RESTRICTED;
    private static final NanosuitProfile SHARED = NanosuitProfile.QA_SHARED;
    private int ticks;
    private int clientTicks;
    private int failures;
    private int sprintEndedTick;
    private int mantleSwitchTick;
    private float mantleEnergy;
    private int recoveryBeforeSwap;
    private Drive drive = Drive.NONE;
    private boolean mantleArmed;
    private boolean reported;
    private volatile boolean clientReady;
    private volatile boolean complete;
    private volatile Snapshot client;

    public TransitionQa() {
        if (!Boolean.getBoolean("crysis.transition.qa")) {
            throw new IllegalStateException("Transition QA needs the isolated QA launch");
        }
        NeoForge.EVENT_BUS.addListener(this::tickClient);
        NeoForge.EVENT_BUS.addListener(this::tickServer);
        NeoForge.EVENT_BUS.addListener(this::drivePlayer);
    }

    private void tickClient(ClientTickEvent.Post event) {
        var minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null) {
            return;
        }
        client = Snapshot.of(minecraft.player);
        clientReady = ++clientTicks >= 20;
        if (complete && !reported) {
            reported = true;
            System.out.println("TRANSITION_QA_COMPLETE failures=" + failures);
            minecraft.stop();
        }
    }

    private void tickServer(ServerTickEvent.Post event) {
        var players = event.getServer().getPlayerList().getPlayers();
        if (!clientReady || complete || players.isEmpty()) {
            return;
        }
        ServerPlayer player = players.getFirst();
        var energy = NanosuitCapabilities.get(player);
        ticks++;
        if (mantleArmed) {
            watchMantle(player);
        }
        switch (ticks) {
            case 1 -> {
                prepareFloor(player);
                player.setGameMode(GameType.SURVIVAL);
                player.serverLevel().getGameRules().getRule(GameRules.RULE_NATURAL_REGENERATION).set(false, player.server);
                player.setHealth(20.0F);
                player.getFoodData().setFoodLevel(20);
                equip(player);
                resetEnergy(player, 100.0F);
            }
            case 21 -> {
                select(player, SHARED);
                check("FULL_PROFILE", NanosuitArmorUtil.getEquippedProfile(player) == SHARED && energy.getEnergy() == 100.0F,
                    player);
            }
            case 24 -> check("FULL_SYNC", client.energy() == 100.0F && client.mode() == NanosuitMode.NONE, player);
            case 25 -> {
                resetEnergy(player, 44.0F);
                select(player, RESTRICTED);
            }
            case 30 -> {
                check("PARTIAL_ENERGY", energy.getEnergy() == 44.0F && client.energy() == 44.0F
                    && energy.getRegenDelayTicks() == 25, player);
                checkMelee(player);
            }
            case 31 -> {
                select(player, null);
                check("RETURN_LEGACY", NanosuitArmorUtil.getEquippedProfile(player) == NanosuitProfile.LEGACY_CURRENT
                    && energy.getEnergy() == 44.0F, player);
                check("CLOAK_START", NanosuitPlayerHandler.tryToggleMode(player, NanosuitMode.CLOAK), player);
            }
            case 37 -> {
                check("CLOAK_REPLICATED", player.isInvisible() && client.invisible() && client.mode() == NanosuitMode.CLOAK,
                    player);
                select(player, RESTRICTED);
            }
            case 45 -> check("CLOAK_CLEARED", !player.isInvisible() && !client.invisible()
                && energy.getMode() == NanosuitMode.NONE && client.mode() == NanosuitMode.NONE, player);
            case 50 -> {
                select(player, null);
                check("MODE_COOLDOWN_T19", !NanosuitPlayerHandler.tryToggleMode(player, NanosuitMode.ARMOR), player);
            }
            case 51 -> check("MODE_COOLDOWN_T20", NanosuitPlayerHandler.tryToggleMode(player, NanosuitMode.ARMOR), player);
            case 57 -> {
                check("ARMOR_REPLICATED", player.hasEffect(MobEffects.ABSORPTION)
                    && client.mode() == NanosuitMode.ARMOR && client.absorption(), player);
                select(player, RESTRICTED);
            }
            case 64 -> check("ARMOR_CLEARED", !player.hasEffect(MobEffects.ABSORPTION) && !client.absorption()
                && energy.getMode() == NanosuitMode.NONE && client.mode() == NanosuitMode.NONE, player);
            case 65 -> {
                select(player, null);
                resetEnergy(player, 0.0F);
            }
            case 69 -> {
                recoveryBeforeSwap = energy.getRecoveryTicks();
                select(player, RESTRICTED);
                check("LOCK_INITIAL", recoveryBeforeSwap == 4 && energy.isRecoveryLocked() && client.locked(), player);
            }
            case 83 -> check("LOCK_KEPT_AFTER_SWAP", energy.getRecoveryTicks() == recoveryBeforeSwap + 14
                && energy.isRecoveryLocked() && energy.getEnergy() == 0.0F && client.locked()
                && !NanosuitPlayerHandler.tryToggleMode(player, NanosuitMode.ARMOR), player);
            case 165 -> check("LOCK_PARTIAL_CHARGE", energy.isRecoveryLocked() && client.locked()
                && energy.getEnergy() > 0.0F && energy.getEnergy() < 100.0F, player);
            case 195 -> {
                check("LOCK_RESTORED_AT_FULL", energy.getEnergy() == 100.0F && client.energy() == 100.0F
                    && !energy.isRecoveryLocked() && !client.locked(), player);
                select(player, null);
                resetEnergy(player, 100.0F);
            }
            case 198 -> {
                NanosuitPlayerHandler.beginEnhancedJumpCharge(player);
                NanosuitPlayerHandler.requestChargedEnhancedJump(player);
                check("JUMP_CHARGE_STARTED", runtimeInt(player, "enhancedJumpChargeTick") >= 0, player);
            }
            case 200 -> select(player, SHARED);
            case 208 -> check("JUMP_CHARGE_CANCELLED", runtimeInt(player, "enhancedJumpChargeTick") < 0
                && energy.getEnergy() == 100.0F && client.energy() == 100.0F && player.onGround(), player);
            case 224 -> {
                select(player, null);
                resetEnergy(player, 100.0F);
                fill(player, new BlockPos(3, 181, -2), new BlockPos(7, 184, 2), Blocks.GOLD_BLOCK);
                player.teleportTo(player.serverLevel(), 2.69, 181.0, 0.5, java.util.Set.of(), -90.0F, 0.0F);
                player.setDeltaMovement(Vec3.ZERO);
            }
            case 231 -> {
                NanosuitPlayerHandler.beginEnhancedJumpCharge(player);
                NanosuitPlayerHandler.requestChargedEnhancedJump(player);
                mantleArmed = true;
            }
            case 272 -> {
                check("MANTLE_CREATED", mantleSwitchTick > 0, player);
                mantleArmed = false;
                select(player, null);
                player.teleportTo(0.5, 181.0, 0.5);
                player.setDeltaMovement(Vec3.ZERO);
                resetEnergy(player, 100.0F);
                drive = Drive.SPRINT;
            }
            case 277 -> NanosuitPlayerHandler.requestPowerSprint(player);
            case 282 -> {
                check("SPRINT_ACTIVE", NanosuitPlayerHandler.isLandPowerSprintActive(player) && client.sprint(), player);
                select(player, RESTRICTED);
            }
            case 283 -> sprintEndedTick = player.tickCount;
            case 289 -> {
                check("SPRINT_CLEARED", !NanosuitPlayerHandler.isLandPowerSprintActive(player)
                    && !energy.isSprintBoostReady() && !client.sprint(), player);
                select(player, null);
                NanosuitPlayerHandler.requestPowerSprint(player);
                check("SPRINT_ORIGIN_COOLDOWN", runtimeInt(player, "powerSprintCooldownUntilTick") == sprintEndedTick + 20
                    && !energy.isSprintBoostReady(), player);
            }
            case 302 -> {
                NanosuitPlayerHandler.requestPowerSprint(player);
                check("SPRINT_COOLDOWN_T19", !energy.isSprintBoostReady(), player);
            }
            case 303 -> {
                NanosuitPlayerHandler.requestPowerSprint(player);
                check("SPRINT_COOLDOWN_T20", energy.isSprintBoostReady(), player);
                drive = Drive.NONE;
                player.setSprinting(false);
            }
            case 331 -> enterPool(player);
            case 350 -> drive = Drive.SWIM;
            case 359 -> {
                check("SWIM_ACTIVE", NanosuitSwimBoost.isActive(player) && Math.abs(swimSpeed(player) - 1.4) < 0.001
                    && Math.abs(client.swimSpeed() - 1.4) < 0.001, player);
                select(player, RESTRICTED);
            }
            case 366 -> {
                check("SWIM_CLEARED", !NanosuitSwimBoost.isActive(player) && swimSpeed(player) == 1.0
                    && client.swimSpeed() == 1.0, player);
                drive = Drive.NONE;
                player.setSprinting(false);
                player.setSwimming(false);
                player.setNoGravity(false);
                player.teleportTo(0.5, 181.0, 0.5);
                select(player, null);
                resetEnergy(player, 100.0F);
                player.setHealth(10.0F);
            }
            case 387 -> {
                check("PASSIVE_HEAL_SCHEDULED", runtimeInt(player, "nextHealthRecoveryTick") > player.tickCount
                    && player.getHealth() == 10.0F, player);
                player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
            }
            case 397 -> {
                check("PASSIVE_HEAL_CANCELLED", NanosuitArmorUtil.getEquippedProfile(player) == null
                    && runtimeInt(player, "nextHealthRecoveryTick") == -1
                    && player.getHealth() == 10.0F && client.health() == 10.0F, player);
                equip(player);
            }
            case 466 -> check("PASSIVE_HEAL_NO_EARLY_CREDIT", player.getHealth() == 10.0F && client.health() == 10.0F, player);
            case 485 -> {
                check("PASSIVE_HEAL_RESTARTED", player.getHealth() == 11.0F && client.health() == 11.0F
                    && energy.getEnergy() == 100.0F && client.energy() == 100.0F, player);
                complete = true;
            }
            default -> { }
        }
    }

    private void watchMantle(ServerPlayer player) {
        if (mantleSwitchTick == 0 && runtimeValue(player, "enhancedJumpMantlePlan") != null) {
            mantleSwitchTick = ticks;
            mantleEnergy = NanosuitCapabilities.get(player).getEnergy();
            select(player, RESTRICTED);
        } else if (mantleSwitchTick != 0 && ticks == mantleSwitchTick + 1) {
            check("MANTLE_CANCELLED", runtimeValue(player, "enhancedJumpMantlePlan") == null
                && runtimeInt(player, "enhancedJumpMantleUntilTick") == 0, player);
            check("MANTLE_ENERGY_NO_REFUND", mantleEnergy == 90.0F
                && NanosuitCapabilities.get(player).getEnergy() == mantleEnergy, player);
            mantleArmed = false;
        }
    }

    private void checkMelee(ServerPlayer player) {
        var target = EntityType.PIG.create(player.serverLevel());
        target.setNoAi(true);
        target.setPos(player.getX() + 1.0, player.getY(), player.getZ());
        player.serverLevel().addFreshEntity(target);
        player.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        player.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        float before = NanosuitCapabilities.get(player).getEnergy();
        player.attack(target);
        float damage = target.getMaxHealth() - target.getHealth();
        check("RESTRICTED_MELEE_VANILLA", damage == 1.0F && NanosuitCapabilities.get(player).getEnergy() == before,
            player);
        target.discard();
    }

    private void drivePlayer(PlayerTickEvent.Pre event) {
        if (drive != Drive.NONE && event.getEntity() instanceof ServerPlayer player) {
            player.setSprinting(true);
            if (drive == Drive.SWIM) {
                player.setSwimming(true);
            }
        }
    }

    private static void select(ServerPlayer player, NanosuitProfile profile) {
        NanosuitArmorUtil.qaSelectProfile(player, profile);
    }

    private static void resetEnergy(ServerPlayer player, float amount) {
        var energy = NanosuitCapabilities.get(player);
        energy.setEnergy(100.0F);
        energy.setMode(NanosuitMode.NONE);
        energy.setSprintBoostReady(false);
        energy.setEnergy(amount);
        energy.setRegenDelayTicks(30);
        NanosuitSyncManager.syncToClient(player);
    }

    private static void equip(ServerPlayer player) {
        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.NANOGEO_NANOSUIT_HELMET.get()));
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.NANOGEO_NANOSUIT_CHESTPLATE.get()));
        player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.NANOGEO_NANOSUIT_LEGGINGS.get()));
        player.setItemSlot(EquipmentSlot.FEET, new ItemStack(ModItems.NANOGEO_NANOSUIT_BOOTS.get()));
    }

    private static void prepareFloor(ServerPlayer player) {
        fill(player, new BlockPos(-5, 180, -3), new BlockPos(8, 180, 3), Blocks.STONE);
        fill(player, new BlockPos(-2, 181, -2), new BlockPos(7, 187, 2), Blocks.AIR);
        player.teleportTo(0.5, 181.0, 0.5);
        player.setDeltaMovement(Vec3.ZERO);
    }

    private static void enterPool(ServerPlayer player) {
        for (BlockPos pos : BlockPos.betweenClosed(5, 180, 4, 13, 185, 12)) {
            boolean wall = pos.getY() == 180 || pos.getX() == 5 || pos.getX() == 13 || pos.getZ() == 4 || pos.getZ() == 12;
            player.serverLevel().setBlockAndUpdate(pos, (wall ? Blocks.STONE : Blocks.WATER).defaultBlockState());
        }
        resetEnergy(player, 100.0F);
        player.setNoGravity(true);
        player.teleportTo(9.5, 181.1, 8.5);
    }

    private static void fill(ServerPlayer player, BlockPos start, BlockPos end, net.minecraft.world.level.block.Block block) {
        for (BlockPos pos : BlockPos.betweenClosed(start, end)) {
            player.serverLevel().setBlockAndUpdate(pos, block.defaultBlockState());
        }
    }

    private static Field field(Class<?> owner, String name) {
        try {
            Field field = owner.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Missing QA observation " + name, exception);
        }
    }

    private static Object runtimeValue(ServerPlayer player, String name) {
        try {
            var states = (Map<?, ?>) RUNTIME_STATES.get(null);
            Object state = states.get(player.getUUID());
            return field(state.getClass(), name).get(state);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Cannot read QA observation " + name, exception);
        }
    }

    private static int runtimeInt(ServerPlayer player, String name) {
        return (int) runtimeValue(player, name);
    }

    private static double swimSpeed(Player player) {
        return player.getAttributeValue(NeoForgeMod.SWIM_SPEED);
    }

    private void check(String label, boolean pass, ServerPlayer player) {
        if (!pass) {
            failures++;
        }
        System.out.println("TRANSITION_QA_" + label + (pass ? "_PASS" : "_FAIL")
            + " tick=" + ticks + " profile=" + NanosuitArmorUtil.getEquippedProfile(player)
            + " server=" + Snapshot.of(player) + " client=" + client);
    }

    private enum Drive { NONE, SPRINT, SWIM }

    private record Snapshot(float energy, NanosuitMode mode, boolean sprint, boolean locked, boolean invisible,
                            boolean absorption, double swimSpeed, float health, double x, double y) {
        static Snapshot of(Player player) {
            var energy = NanosuitCapabilities.get(player);
            return new Snapshot(energy.getEnergy(), energy.getMode(), energy.isSprintBoostReady(), energy.isRecoveryLocked(),
                player.isInvisible(), player.hasEffect(MobEffects.ABSORPTION), TransitionQa.swimSpeed(player), player.getHealth(),
                player.getX(), player.getY());
        }
    }
}
