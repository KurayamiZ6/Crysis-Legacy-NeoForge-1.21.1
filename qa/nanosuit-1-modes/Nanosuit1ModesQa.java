package local.crysisqa;

import io.github.painworks.crysismod.nanosuit.NanosuitGeneration;
import io.github.painworks.crysismod.nanosuit.NanosuitMode;
import io.github.painworks.crysismod.nanosuit.energy.NanosuitCapabilities;
import io.github.painworks.crysismod.nanosuit.handler.NanosuitPlayerHandler;
import io.github.painworks.crysismod.nanosuit.handler.NanosuitSwimBoost;
import io.github.painworks.crysismod.nanosuit.network.NanosuitSyncManager;
import io.github.painworks.crysismod.nanosuit.network.NanosuitNetwork;
import io.github.painworks.crysismod.nanosuit.network.RequestEnhancedJumpPacket;
import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@Mod("crysisnanosuit1modesqa")
public final class Nanosuit1ModesQa {
    private static final NanosuitGeneration FIRST = NanosuitGeneration.NANOSUIT_1;
    private static final NanosuitGeneration SECOND = NanosuitGeneration.NANOSUIT_2;
    private static final EquipmentSlot[] SLOTS = {
        EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };
    private static final String[] PIECES = {"helmet", "chestplate", "leggings", "boots"};
    private static final float SAVED_ENERGY = 61.0F;

    private final boolean reopening = "reopen".equals(System.getProperty("crysis.n1modes.phase"));
    private final Set<NanosuitMode> hudCaptures = EnumSet.noneOf(NanosuitMode.class);
    private int ticks;
    private int clientTicks;
    private int failures;
    private int recoveryBeforeSwap;
    private double baseWalk;
    private float energyBeforeReturn;
    private float drainStart;
    private volatile boolean instantJump;
    private volatile Drive drive = Drive.NONE;
    private volatile Snapshot client;
    private volatile boolean ready;
    private volatile boolean complete;
    private boolean reported;

    public Nanosuit1ModesQa() {
        if (System.getProperty("crysis.n1modes.phase") == null) {
            throw new IllegalStateException("Nanosuit 1 QA requires an isolated launch");
        }
        NeoForge.EVENT_BUS.addListener(this::onLogin);
        NeoForge.EVENT_BUS.addListener(this::clientTick);
        NeoForge.EVENT_BUS.addListener(this::captureHud);
        NeoForge.EVENT_BUS.addListener(this::serverTick);
        NeoForge.EVENT_BUS.addListener(this::drivePlayer);
    }

    private void captureHud(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (reopening || !ready || minecraft.screen != null || minecraft.player == null) {
            return;
        }
        NanosuitMode mode = NanosuitCapabilities.get(minecraft.player).getMode();
        if ((mode == NanosuitMode.STRENGTH || mode == NanosuitMode.SPEED) && hudCaptures.add(mode)) {
            Screenshot.grab(minecraft.gameDirectory, "n1-mode-" + mode.name().toLowerCase(java.util.Locale.ROOT) + ".png",
                minecraft.getMainRenderTarget(), message -> { });
        }
    }

    private void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        var data = NanosuitCapabilities.get(player);
        check(reopening ? "PERSISTED_EQUIPMENT" : "HISTORICAL_EQUIPMENT", wearsFirst(player), player);
        check("ENERGY_NBT_SCHEMA", data.serializeNBT(player.registryAccess()).getAllKeys()
            .equals(Set.of("Energy", "Mode", "RegenDelayTicks", "RecoveryTicks")), player);
        if (reopening) {
            check("PERSISTED_STRENGTH_SERVER", data.getMode() == NanosuitMode.STRENGTH
                && near(data.getEnergy(), SAVED_ENERGY) && !data.isRecoveryLocked(), player);
        }
    }

    private void clientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (reported) {
            return;
        }
        if (minecraft.player != null && minecraft.screen == null) {
            client = Snapshot.of(minecraft.player);
            if (instantJump) {
                instantJump = false;
                NanosuitNetwork.sendToServer(new RequestEnhancedJumpPacket(RequestEnhancedJumpPacket.Phase.INSTANT));
            }
            ready = ++clientTicks >= 14;
            if (complete) {
                reported = true;
                System.out.println("N1_MODES_QA_" + (reopening ? "REOPEN" : "PLAY") + "_COMPLETE failures=" + failures);
                minecraft.stop();
            }
        }
    }

    private void serverTick(ServerTickEvent.Post event) {
        var players = event.getServer().getPlayerList().getPlayers();
        if (!ready || complete || players.isEmpty()) {
            return;
        }
        ServerPlayer player = players.getFirst();
        var data = NanosuitCapabilities.get(player);
        if (reopening) {
            check("PERSISTED_STRENGTH_CLIENT", modesMatch(player, NanosuitMode.STRENGTH)
                && client.generation() == FIRST && near(client.energy(), SAVED_ENERGY), player);
            complete = true;
            return;
        }
        switch (++ticks) {
            case 1 -> prepareFloor(player);
            case 10 -> {
                check("DEFAULT_ARMOR_SYNC", modesMatch(player, NanosuitMode.ARMOR)
                    && !player.hasEffect(MobEffects.ABSORPTION) && !client.absorption(), player);
                check("ARMOR_NO_IDLE_COST", near(data.getEnergy(), 100.0), player);
                check("ARMOR_JUMP_DENIED", !NanosuitPlayerHandler.canChargeEnhancedJump(player), player);
                baseWalk = player.getAttributeValue(Attributes.MOVEMENT_SPEED);
                var attacker = Objects.requireNonNull(EntityType.ZOMBIE.create(player.serverLevel()));
                float before = player.getHealth();
                player.hurt(player.damageSources().mobAttack(attacker), 6.0F);
                check("ARMOR_ENERGY_PROTECTION", near(player.getHealth(), before)
                    && near(data.getEnergy(), 97.0), player);
                setEnergy(player, 100.0F);
            }
            case 14 -> punch(player, "ARMOR_PUNCH_UNBOOSTED", 1.0F, 0.0F);
            case 24 -> select(player, NanosuitMode.STRENGTH);
            case 30 -> {
                check("STRENGTH_SYNC_AND_EXCLUSIVITY", modesMatch(player, NanosuitMode.STRENGTH)
                    && !player.hasEffect(MobEffects.ABSORPTION) && !client.absorption() && !player.isInvisible(), player);
                punch(player, "STRENGTH_PUNCH", 2.0F, 10.0F);
                setEnergy(player, 100.0F);
                drive = Drive.SPRINT;
            }
            case 34 -> {
                check("STRENGTH_SPRINT_DENIED", !NanosuitPlayerHandler.isLandPowerSprintActive(player)
                    && !player.hasEffect(MobEffects.MOVEMENT_SPEED) && near(data.getEnergy(), 100.0), player);
                drive = Drive.NONE;
                player.setSprinting(false);
                check("STRENGTH_JUMP_ALLOWED", NanosuitPlayerHandler.canChargeEnhancedJump(player), player);
                instantJump = true;
            }
            case 42 -> check("STRENGTH_JUMP_COST_AND_IMPULSE", near(data.getEnergy(), 85.0)
                && near(client.energy(), 85.0) && player.getY() > 181.5, player);
            case 44 -> select(player, NanosuitMode.SPEED);
            case 51 -> {
                check("SPEED_SYNC_AND_JUMP_DENIED", modesMatch(player, NanosuitMode.SPEED)
                    && !NanosuitPlayerHandler.canChargeEnhancedJump(player) && !player.isInvisible()
                    && !player.hasEffect(MobEffects.ABSORPTION), player);
                check("SPEED_WALK_WITHOUT_DRAIN", near(player.getAttributeValue(Attributes.MOVEMENT_SPEED), baseWalk * 2.0)
                    && near(client.walkSpeed(), baseWalk * 2.0) && near(data.getEnergy(), 85.0), player);
                drive = Drive.SPRINT;
            }
            case 59 -> check("SPEED_SPRINT_WITHOUT_SECOND_PRESS", NanosuitPlayerHandler.isLandPowerSprintActive(player)
                && near(data.getEnergy(), 85.0 - 8 * 0.9), player);
            case 64 -> select(player, NanosuitMode.CLOAK);
            case 72 -> {
                check("CLOAK_SYNC_AND_BOOST_REMOVED", modesMatch(player, NanosuitMode.CLOAK)
                    && player.isInvisible() && client.invisible() && !player.hasEffect(MobEffects.MOVEMENT_SPEED)
                    && !client.speedEffect() && !NanosuitPlayerHandler.canChargeEnhancedJump(player), player);
                drive = Drive.NONE;
                player.setSprinting(false);
            }
            case 85 -> {
                NanosuitPlayerHandler.tryToggleMode(player, NanosuitMode.CLOAK);
                check("CLOAK_RESELECT_IDEMPOTENT", data.getMode() == NanosuitMode.CLOAK, player);
                check("LEAVING_SPEED_REMOVES_WALK_BONUS", near(player.getAttributeValue(Attributes.MOVEMENT_SPEED), baseWalk)
                    && near(client.walkSpeed(), baseWalk), player);
            }
            case 88 -> select(player, NanosuitMode.ARMOR);
            case 95 -> {
                check("ARMOR_RESTORED_CLOAK_CLEARED", modesMatch(player, NanosuitMode.ARMOR)
                    && !player.isInvisible() && !client.invisible() && !player.hasEffect(MobEffects.ABSORPTION), player);
                player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
            }
            case 102 -> {
                check("INCOMPLETE_SET_NONE", generation(player) == null && client.generation() == null
                    && modesMatch(player, NanosuitMode.NONE) && !player.hasEffect(MobEffects.ABSORPTION)
                    && !NanosuitPlayerHandler.tryToggleMode(player, NanosuitMode.STRENGTH), player);
                player.setItemSlot(EquipmentSlot.HEAD, gear(SECOND, 0));
            }
            case 108 -> {
                check("MIXED_SET_NONE", generation(player) == null && client.generation() == null
                    && modesMatch(player, NanosuitMode.NONE) && !NanosuitPlayerHandler.canChargeEnhancedJump(player)
                    && !NanosuitPlayerHandler.tryToggleMode(player, NanosuitMode.SPEED), player);
                equip(player, SECOND);
                setEnergy(player, 62.0F);
            }
            case 115 -> {
                check("N2_REJECTS_MANUAL_STRENGTH", generation(player) == SECOND && client.generation() == SECOND
                    && modesMatch(player, NanosuitMode.NONE) && NanosuitPlayerHandler.canChargeEnhancedJump(player)
                    && !NanosuitPlayerHandler.tryToggleMode(player, NanosuitMode.STRENGTH), player);
                select(player, NanosuitMode.CLOAK);
            }
            case 121 -> {
                check("N2_CLOAK_SYNC", modesMatch(player, NanosuitMode.CLOAK), player);
                energyBeforeReturn = data.getEnergy();
                equip(player, FIRST);
                check("N2_TO_N1_NO_ENERGY_RESET", near(data.getEnergy(), energyBeforeReturn), player);
            }
            case 128 -> check("N2_TO_N1_DEFAULT_ARMOR", generation(player) == FIRST
                && client.generation() == FIRST && modesMatch(player, NanosuitMode.ARMOR)
                && near(data.getEnergy(), energyBeforeReturn) && !player.isInvisible(), player);
            case 132 -> {
                setEnergy(player, 0.0F);
                data.setRegenDelayTicks(2);
            }
            case 137 -> {
                check("CLASSIC_ZERO_RECHARGES_WITHOUT_LOCK", !data.isRecoveryLocked() && client != null && !client.locked()
                    && data.getEnergy() > 0.0F && data.getEnergy() < 10.0F && modesMatch(player, NanosuitMode.ARMOR), player);
                equip(player, SECOND);
            }
            case 138 -> setEnergy(player, 0.0F);
            case 140 -> {
                check("LOCKOUT_REJECTS_SELECTION", modesMatch(player, NanosuitMode.NONE)
                    && data.isRecoveryLocked() && client.locked() && !player.hasEffect(MobEffects.ABSORPTION)
                    && !NanosuitPlayerHandler.tryToggleMode(player, NanosuitMode.STRENGTH), player);
                recoveryBeforeSwap = data.getRecoveryTicks();
                equip(player, FIRST);
            }
            case 145 -> equip(player, SECOND);
            case 150 -> equip(player, FIRST);
            case 162 -> check("LOCKOUT_SURVIVES_BOTH_SWAPS", generation(player) == FIRST
                && modesMatch(player, NanosuitMode.NONE) && data.isRecoveryLocked() && client.locked()
                && data.getRecoveryTicks() == recoveryBeforeSwap + 22 && near(data.getEnergy(), 0.0), player);
            case 226 -> check("PARTIAL_RECHARGE_REMAINS_LOCKED", data.getEnergy() > 0.0F
                && data.getEnergy() < 100.0F && data.isRecoveryLocked() && client.locked()
                && modesMatch(player, NanosuitMode.NONE), player);
            case 263 -> {
                check("FULL_RECOVERY_DEFAULTS_ARMOR", near(data.getEnergy(), 100.0)
                    && near(client.energy(), 100.0) && !data.isRecoveryLocked() && !client.locked()
                    && modesMatch(player, NanosuitMode.ARMOR) && !player.hasEffect(MobEffects.ABSORPTION), player);
                enterPool(player);
                drive = Drive.SWIM;
            }
            case 280 -> {
                check("ARMOR_SWIM_UNBOOSTED", !NanosuitSwimBoost.isActive(player)
                    && near(player.getAttributeValue(NeoForgeMod.SWIM_SPEED), 1.0), player);
                select(player, NanosuitMode.SPEED);
            }
            case 289 -> check("SPEED_SWIM_BOOST", modesMatch(player, NanosuitMode.SPEED)
                && NanosuitSwimBoost.isActive(player) && near(client.swimSpeed(), 1.4)
                && data.getEnergy() < 100.0F, player);
            case 300 -> select(player, NanosuitMode.STRENGTH);
            case 307 -> {
                check("LEAVING_SPEED_REMOVES_SWIM", modesMatch(player, NanosuitMode.STRENGTH)
                    && !NanosuitSwimBoost.isActive(player) && near(client.swimSpeed(), 1.0), player);
                drive = Drive.NONE;
                player.setSprinting(false);
                player.setSwimming(false);
                player.setNoGravity(false);
                player.teleportTo(0.5, 181.0, 0.5);
                player.setDeltaMovement(Vec3.ZERO);
                setEnergy(player, 100.0F);
            }
            case 319 -> {
                check("STRENGTH_CAN_QUEUE_JUMP_BEFORE_SWITCH", NanosuitPlayerHandler.canChargeEnhancedJump(player), player);
                NanosuitPlayerHandler.beginEnhancedJumpCharge(player);
                NanosuitPlayerHandler.requestChargedEnhancedJump(player);
            }
            case 320 -> select(player, NanosuitMode.ARMOR);
            case 329 -> check("MODE_CHANGE_CANCELS_QUEUED_JUMP", modesMatch(player, NanosuitMode.ARMOR)
                && near(data.getEnergy(), 100.0) && player.onGround(), player);
            case 340 -> select(player, NanosuitMode.STRENGTH);
            case 348 -> equip(player, SECOND);
            case 355 -> {
                check("N1_TO_N2_CLEARS_UNSUPPORTED_MODE", generation(player) == SECOND
                    && client.generation() == SECOND && modesMatch(player, NanosuitMode.NONE)
                    && NanosuitPlayerHandler.canChargeEnhancedJump(player), player);
                equip(player, FIRST);
            }
            case 366 -> {
                select(player, NanosuitMode.STRENGTH);
                setEnergy(player, SAVED_ENERGY);
            }
            case 378 -> {
                select(player, NanosuitMode.CLOAK);
                player.setSprinting(false);
                player.setShiftKeyDown(false);
                drainStart = data.getEnergy();
            }
            case 388 -> {
                check("CLASSIC_CLOAK_STILL_DRAIN", Math.abs(drainStart - data.getEnergy() - 0.10F) < 0.01F, player);
                player.setForcedPose(Pose.SWIMMING);
                drainStart = data.getEnergy();
            }
            case 398 -> {
                check("CLASSIC_CLOAK_PRONE_DRAIN", Math.abs(drainStart - data.getEnergy() - 0.225F) < 0.01F, player);
                player.setForcedPose(null);
                setEnergy(player, 10.05F);
            }
            case 411 -> {
                check("CLASSIC_CLOAK_TEN_PERCENT", near(data.getEnergy(), 10.0)
                    && modesMatch(player, NanosuitMode.ARMOR) && !player.isInvisible(), player);
                select(player, NanosuitMode.SPEED);
                player.setShiftKeyDown(true);
                setEnergy(player, 11.0F);
            }
            case 415 -> {
                check("CLASSIC_SPEED_CROUCH_MULTIPLIER", near(player.getAttributeValue(Attributes.MOVEMENT_SPEED), baseWalk * 2.0)
                    && near(data.getEnergy(), 11.0), player);
                player.setShiftKeyDown(false);
                player.setForcedPose(Pose.SWIMMING);
            }
            case 419 -> {
                check("CLASSIC_SPEED_PRONE_MULTIPLIER", near(player.getAttributeValue(Attributes.MOVEMENT_SPEED), baseWalk * 2.0)
                    && near(data.getEnergy(), 11.0), player);
                player.setForcedPose(null);
                drive = Drive.SPRINT;
            }
            case 427 -> {
                check("CLASSIC_SPRINT_TEN_PERCENT", near(data.getEnergy(), 10.0) && !NanosuitPlayerHandler.isLandPowerSprintActive(player)
                    && !data.isRecoveryLocked(), player);
                drive = Drive.NONE;
                player.setSprinting(false);
                select(player, NanosuitMode.STRENGTH);
                setEnergy(player, SAVED_ENERGY);
            }
            case 439 -> {
                check("STRENGTH_SAVE_STATE", wearsFirst(player) && modesMatch(player, NanosuitMode.STRENGTH)
                    && near(data.getEnergy(), SAVED_ENERGY) && near(client.energy(), SAVED_ENERGY), player);
                complete = true;
            }
            default -> { }
        }
    }

    private void drivePlayer(PlayerTickEvent.Pre event) {
        if (event.getEntity() instanceof ServerPlayer player && drive != Drive.NONE) {
            player.setSprinting(true);
            if (drive == Drive.SWIM) {
                player.setSwimming(true);
            }
        }
    }

    private void select(ServerPlayer player, NanosuitMode mode) {
        check("SELECT_" + mode, NanosuitPlayerHandler.tryToggleMode(player, mode)
            && NanosuitCapabilities.get(player).getMode() == mode, player);
    }

    private void punch(ServerPlayer player, String label, float expectedDamage, float expectedCost) {
        var target = Objects.requireNonNull(EntityType.PIG.create(player.serverLevel()));
        target.setNoAi(true);
        target.setNoGravity(true);
        target.moveTo(player.getX(), player.getY(), player.getZ() + 1.0D, 0.0F, 0.0F);
        if (!player.serverLevel().addFreshEntity(target)) {
            throw new IllegalStateException("Could not place melee target");
        }
        float startingEnergy = NanosuitCapabilities.get(player).getEnergy();
        player.attack(target);
        check(label, near(target.getMaxHealth() - target.getHealth(), expectedDamage)
            && near(startingEnergy - NanosuitCapabilities.get(player).getEnergy(), expectedCost), player);
        target.discard();
    }

    private boolean modesMatch(ServerPlayer player, NanosuitMode mode) {
        return NanosuitCapabilities.get(player).getMode() == mode && client.mode() == mode;
    }

    private static void prepareFloor(ServerPlayer player) {
        player.setGameMode(GameType.SURVIVAL);
        player.serverLevel().getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, player.server);
        player.serverLevel().setDayTime(6000);
        for (BlockPos pos : BlockPos.betweenClosed(-3, 180, -3, 4, 186, 4)) {
            player.serverLevel().setBlockAndUpdate(pos, (pos.getY() == 180 ? Blocks.STONE : Blocks.AIR).defaultBlockState());
        }
        player.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        player.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        player.teleportTo(0.5, 181.0, 0.5);
        player.setDeltaMovement(Vec3.ZERO);
        setEnergy(player, 100.0F);
    }

    private static void enterPool(ServerPlayer player) {
        for (BlockPos pos : BlockPos.betweenClosed(5, 180, 4, 13, 185, 12)) {
            boolean wall = pos.getY() == 180 || pos.getX() == 5 || pos.getX() == 13 || pos.getZ() == 4 || pos.getZ() == 12;
            player.serverLevel().setBlockAndUpdate(pos, (wall ? Blocks.STONE : Blocks.WATER).defaultBlockState());
        }
        player.setNoGravity(true);
        player.teleportTo(9.5, 181.1, 8.5);
    }

    private static void setEnergy(ServerPlayer player, float value) {
        var data = NanosuitCapabilities.get(player);
        if (generation(player) == FIRST && !data.isRecoveryLocked()) {
            data.setEnergyWithoutStartingRecovery(value);
        } else {
            data.setEnergy(value);
        }
        data.setRegenDelayTicks(1200);
        NanosuitSyncManager.syncToClient(player);
    }

    private static void equip(ServerPlayer player, NanosuitGeneration generation) {
        for (int i = 0; i < SLOTS.length; i++) {
            player.setItemSlot(SLOTS[i], gear(generation, i));
        }
    }

    private static ItemStack gear(NanosuitGeneration generation, int slot) {
        return new ItemStack(BuiltInRegistries.ITEM.getOptional(itemId(generation, slot)).orElseThrow());
    }

    private static ResourceLocation itemId(NanosuitGeneration generation, int slot) {
        return ResourceLocation.fromNamespaceAndPath("crysismod",
            (generation == FIRST ? "nanogeo_nanosuit_" : "nanosuit_2_") + PIECES[slot]);
    }

    private static NanosuitGeneration generation(Player player) {
        var profile = NanosuitArmorUtil.getEquippedProfile(player);
        return profile == null ? null : profile.generation();
    }

    private static boolean wearsFirst(ServerPlayer player) {
        for (int i = 0; i < SLOTS.length; i++) {
            if (!BuiltInRegistries.ITEM.getKey(player.getItemBySlot(SLOTS[i]).getItem()).equals(itemId(FIRST, i))) {
                return false;
            }
        }
        return generation(player) == FIRST;
    }

    private static boolean near(double value, double target) {
        return Math.abs(value - target) < 0.001;
    }

    private void check(String label, boolean pass, ServerPlayer player) {
        if (!pass) {
            failures++;
        }
        System.out.println("N1_MODES_QA_" + label + (pass ? "_PASS" : "_FAIL") + " tick=" + ticks
            + " server=" + Snapshot.of(player) + " client=" + client);
    }

    private enum Drive { NONE, SPRINT, SWIM }

    private record Snapshot(NanosuitGeneration generation, NanosuitMode mode, float energy, boolean locked,
                            boolean invisible, boolean absorption, boolean speedEffect, double swimSpeed, double walkSpeed) {
        static Snapshot of(Player player) {
            var data = NanosuitCapabilities.get(player);
            return new Snapshot(Nanosuit1ModesQa.generation(player), data.getMode(), data.getEnergy(),
                data.isRecoveryLocked(), player.isInvisible(), player.hasEffect(MobEffects.ABSORPTION),
                player.hasEffect(MobEffects.MOVEMENT_SPEED), player.getAttributeValue(NeoForgeMod.SWIM_SPEED),
                player.getAttributeValue(Attributes.MOVEMENT_SPEED));
        }
    }
}
