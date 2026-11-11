package local.crysisqa;

import io.github.painworks.crysismod.nanosuit.NanosuitGeneration;
import io.github.painworks.crysismod.nanosuit.NanosuitMode;
import io.github.painworks.crysismod.nanosuit.energy.NanosuitCapabilities;
import io.github.painworks.crysismod.nanosuit.handler.NanosuitPlayerHandler;
import io.github.painworks.crysismod.nanosuit.handler.NanosuitSwimBoost;
import io.github.painworks.crysismod.nanosuit.network.NanosuitSyncManager;
import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
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

@Mod("crysisequipmentqa")
public final class EquipmentQa {
    private static final NanosuitGeneration FIRST = NanosuitGeneration.NANOSUIT_1;
    private static final NanosuitGeneration SECOND = NanosuitGeneration.NANOSUIT_2;
    private static final EquipmentSlot[] SLOTS = {
        EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };
    private static final String[] SUFFIXES = { "helmet", "chestplate", "leggings", "boots" };
    private static final Field RUNTIMES = field(NanosuitPlayerHandler.class, "PLAYER_RUNTIME_STATE");
    private static final int STORED_LEGACY_SLOT = 27;
    private static final float STORED_ENERGY = 61.0F;
    private final boolean reopening = "reopen".equals(System.getProperty("crysis.equipment.phase"));
    private final boolean renderOnly = "captures".equals(System.getProperty("crysis.equipment.phase"));
    private final ItemStack[] historicalGear = new ItemStack[SLOTS.length];
    private int ticks;
    private int clientTicks;
    private int failures;
    private int sprintEndTick;
    private int captureFrame;
    private float cloakEnergyBeforeSwap;
    private int recoveryBeforeSwap;
    private NanosuitGeneration captureProfile;
    private volatile NanosuitGeneration captureRequest;
    private volatile NanosuitGeneration capturedGeneration;
    private volatile Drive drive = Drive.NONE;
    private volatile Snapshot client;
    private volatile boolean clientReady;
    private volatile boolean complete;
    private boolean reported;

    public EquipmentQa() {
        if (System.getProperty("crysis.equipment.phase") == null) {
            throw new IllegalStateException("Equipment QA requires an isolated launch");
        }
        NeoForge.EVENT_BUS.addListener(this::onLogin);
        NeoForge.EVENT_BUS.addListener(this::tickClient);
        NeoForge.EVENT_BUS.addListener(this::capture);
        NeoForge.EVENT_BUS.addListener(this::tickServer);
        NeoForge.EVENT_BUS.addListener(this::drivePlayer);
    }

    private void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        var energy = NanosuitCapabilities.get(player);
        if (reopening) {
            check("SAVED_NANOSUIT_2", hasGear(player, SECOND) && generation(player) == SECOND, player);
            for (int i = 0; i < SLOTS.length; i++) {
                check("PERSISTED_SLOT_" + SLOTS[i].getName().toUpperCase(java.util.Locale.ROOT),
                    player.getItemBySlot(SLOTS[i]).getDamageValue() == i + 1
                        && id(player.getInventory().getItem(STORED_LEGACY_SLOT + i)).equals(itemId(FIRST, i)),
                    player);
            }
            check("SAVED_ENERGY", energy.getEnergy() == STORED_ENERGY && energy.getMode() == NanosuitMode.NONE
                && !energy.isRecoveryLocked(), player);
        } else {
            check("OLD_SAVE_NANOSUIT_1", hasGear(player, FIRST) && generation(player) == FIRST, player);
            check("OLD_SAVE_ENERGY_NBT", energy.serializeNBT(player.registryAccess()).getAllKeys()
                .equals(Set.of("Energy", "Mode", "RegenDelayTicks", "RecoveryTicks")), player);
            for (int i = 0; i < SLOTS.length; i++) {
                historicalGear[i] = player.getItemBySlot(SLOTS[i]).copy();
            }
        }
    }

    private void tickClient(ClientTickEvent.Post event) {
        var minecraft = Minecraft.getInstance();
        if (reported) {
            return;
        }
        if (minecraft.player != null && minecraft.screen == null) {
            client = Snapshot.of(minecraft.player);
            clientReady = ++clientTicks >= 12;
            if (complete) {
                finish(minecraft);
            }
        }
    }

    private void capture(RenderGuiEvent.Post event) {
        var minecraft = Minecraft.getInstance();
        NanosuitGeneration requested = captureRequest;
        if (requested == null || requested == capturedGeneration || minecraft.player == null
            || minecraft.level == null || minecraft.screen != null || generation(minecraft.player) != requested
            || minecraft.level.getDayTime() % 24000 < 6000 || minecraft.level.getDayTime() % 24000 > 8000) {
            return;
        }
        if (captureProfile != requested) {
            captureProfile = requested;
            captureFrame = 0;
            minecraft.options.setCameraType(CameraType.FIRST_PERSON);
        }
        String prefix = requested == FIRST ? "nanosuit-1" : "nanosuit-2";
        switch (++captureFrame) {
            case 6 -> saveScreenshot(minecraft, prefix + "-first-person.png");
            case 9 -> minecraft.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
            case 15 -> saveScreenshot(minecraft, prefix + "-third-person.png");
            case 18 -> {
                minecraft.options.setCameraType(CameraType.FIRST_PERSON);
                capturedGeneration = requested;
            }
            default -> { }
        }
    }

    private static void saveScreenshot(Minecraft minecraft, String name) {
        Screenshot.grab(minecraft.gameDirectory, name, minecraft.getMainRenderTarget(), message -> {});
    }

    private void finish(Minecraft minecraft) {
        reported = true;
        String phase = reopening ? "REOPEN" : renderOnly ? "CAPTURES" : "PLAY";
        System.out.println("EQUIPMENT_QA_" + phase + "_COMPLETE failures=" + failures);
        minecraft.stop();
    }

    private void tickServer(ServerTickEvent.Post event) {
        var players = event.getServer().getPlayerList().getPlayers();
        if (!clientReady || complete || players.isEmpty()) {
            return;
        }
        ServerPlayer player = players.getFirst();
        if (reopening) {
            check("REOPEN_CLIENT", client.generation() == SECOND && client.energy() == STORED_ENERGY, player);
            complete = true;
            return;
        }
        if (renderOnly) {
            if (captureRequest == null) {
                prepareFirstScene(player);
            } else if (capturedGeneration == FIRST && captureRequest == FIRST) {
                equip(player, SECOND);
                captureRequest = SECOND;
            } else if (capturedGeneration == SECOND) {
                complete = true;
            }
            return;
        }
        if ((ticks == 8 && capturedGeneration != FIRST) || (ticks == 55 && capturedGeneration != SECOND)) {
            return;
        }
        var energy = NanosuitCapabilities.get(player);
        switch (++ticks) {
            case 1 -> prepareFirstScene(player);
            case 8 -> check("FULL_NANOSUIT_1", hasGear(player, FIRST) && generation(player) == FIRST
                && client.generation() == FIRST, player);
            case 35 -> player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
            case 41 -> {
                check("MISSING_PIECE", generation(player) == null && client.generation() == null
                    && !NanosuitPlayerHandler.tryToggleMode(player, NanosuitMode.ARMOR), player);
                player.setItemSlot(EquipmentSlot.HEAD, stack(SECOND, 0));
            }
            case 47 -> {
                check("MIXED_SET", generation(player) == null && client.generation() == null
                    && !NanosuitPlayerHandler.tryToggleMode(player, NanosuitMode.CLOAK)
                    && !NanosuitPlayerHandler.canChargeEnhancedJump(player), player);
                equip(player, SECOND);
                captureRequest = SECOND;
            }
            case 55 -> check("FULL_NANOSUIT_2", hasGear(player, SECOND) && generation(player) == SECOND
                && client.generation() == SECOND, player);
            case 90 -> {
                setEnergy(player, 42.0F);
                equip(player, FIRST);
            }
            case 96 -> check("NANOSUIT_2_TO_1", hasGear(player, FIRST) && generation(player) == FIRST
                && client.generation() == FIRST && energy.getEnergy() == 42.0F && client.energy() == 42.0F, player);
            case 100 -> check("NANOSUIT_1_CLOAK_START",
                NanosuitPlayerHandler.tryToggleMode(player, NanosuitMode.CLOAK), player);
            case 106 -> {
                check("NANOSUIT_1_CLOAK", player.isInvisible() && client.invisible()
                    && client.mode() == NanosuitMode.CLOAK, player);
                cloakEnergyBeforeSwap = energy.getEnergy();
                equip(player, SECOND);
                check("DIRECT_SWAP_ENERGY", energy.getEnergy() == cloakEnergyBeforeSwap, player);
            }
            case 112 -> check("NANOSUIT_1_TO_2_WHILE_CLOAKED", generation(player) == SECOND
                && client.generation() == SECOND && player.isInvisible() && client.invisible()
                && energy.getMode() == NanosuitMode.CLOAK && client.mode() == NanosuitMode.CLOAK
                && energy.getEnergy() < cloakEnergyBeforeSwap, player);
            case 119 -> check("CLOAK_ARMOR_COOLDOWN_T19",
                !NanosuitPlayerHandler.tryToggleMode(player, NanosuitMode.ARMOR), player);
            case 120 -> check("CLOAK_ARMOR_COOLDOWN_T20",
                NanosuitPlayerHandler.tryToggleMode(player, NanosuitMode.ARMOR), player);
            case 126 -> check("NANOSUIT_2_ARMOR", player.hasEffect(MobEffects.ABSORPTION)
                && client.absorption() && client.mode() == NanosuitMode.ARMOR, player);
            case 140 -> check("NANOSUIT_2_CLOAK_START",
                NanosuitPlayerHandler.tryToggleMode(player, NanosuitMode.CLOAK), player);
            case 145 -> player.setItemSlot(EquipmentSlot.HEAD, stack(FIRST, 0));
            case 151 -> {
                check("MIXED_CLEARS_CLOAK", generation(player) == null && client.generation() == null
                    && energy.getMode() == NanosuitMode.NONE && client.mode() == NanosuitMode.NONE
                    && !player.isInvisible() && !client.invisible(), player);
                equip(player, FIRST);
            }
            case 157 -> check("NO_CLOAK_RESUME", generation(player) == FIRST && client.generation() == FIRST
                && energy.getMode() == NanosuitMode.NONE && !player.isInvisible() && !client.invisible(), player);
            case 160 -> setEnergy(player, 0.0F);
            case 166 -> {
                recoveryBeforeSwap = energy.getRecoveryTicks();
                equip(player, SECOND);
            }
            case 177 -> check("RECOVERY_SURVIVES_SWAP", generation(player) == SECOND && client.generation() == SECOND
                && energy.getRecoveryTicks() == recoveryBeforeSwap + 11 && energy.isRecoveryLocked()
                && client.locked() && energy.getEnergy() == 0.0F
                && !NanosuitPlayerHandler.tryToggleMode(player, NanosuitMode.ARMOR), player);
            case 265 -> check("PARTIAL_RECOVERY_STILL_LOCKED", energy.getEnergy() > 0.0F && energy.getEnergy() < 100.0F
                && energy.isRecoveryLocked() && client.locked(), player);
            case 288 -> check("RECOVERY_FINISHES", energy.getEnergy() == 100.0F && client.energy() == 100.0F
                && !energy.isRecoveryLocked() && !client.locked(), player);
            case 290 -> {
                NanosuitPlayerHandler.beginEnhancedJumpCharge(player);
                NanosuitPlayerHandler.requestChargedEnhancedJump(player);
                check("NANOSUIT_2_JUMP_CHARGED", runtimeInt(player, "enhancedJumpChargeTick") >= 0, player);
            }
            case 292 -> equip(player, FIRST);
            case 299 -> check("JUMP_CHARGE_CANCELLED", runtimeInt(player, "enhancedJumpChargeTick") < 0
                && generation(player) == FIRST && energy.getEnergy() == 100.0F && client.energy() == 100.0F
                && player.onGround(), player);
            case 310 -> {
                NanosuitPlayerHandler.beginEnhancedJumpCharge(player);
                NanosuitPlayerHandler.requestChargedEnhancedJump(player);
            }
            case 321 -> check("FRESH_JUMP_AFTER_SWAP", energy.getEnergy() == 90.0F && client.energy() == 90.0F
                && player.getY() > 182.0, player);
            case 344 -> {
                player.teleportTo(0.5, 181.0, 0.5);
                player.setDeltaMovement(Vec3.ZERO);
                setEnergy(player, 100.0F);
                drive = Drive.SPRINT;
            }
            case 349 -> NanosuitPlayerHandler.requestPowerSprint(player);
            case 356 -> {
                check("NANOSUIT_1_POWER_SPRINT", NanosuitPlayerHandler.isLandPowerSprintActive(player)
                    && client.sprint() && energy.getEnergy() < 100.0F, player);
                equip(player, SECOND);
            }
            case 357 -> sprintEndTick = player.tickCount;
            case 363 -> check("BOOST_CANCELLED_ON_SWAP", generation(player) == SECOND && client.generation() == SECOND
                && !energy.isSprintBoostReady() && !client.sprint() && !player.hasEffect(MobEffects.MOVEMENT_SPEED)
                && runtimeInt(player, "powerSprintCooldownUntilTick") == sprintEndTick + 20, player);
            case 376 -> {
                NanosuitPlayerHandler.requestPowerSprint(player);
                check("SPRINT_COOLDOWN_T19", !energy.isSprintBoostReady(), player);
            }
            case 377 -> {
                NanosuitPlayerHandler.requestPowerSprint(player);
                check("SPRINT_COOLDOWN_T20", energy.isSprintBoostReady(), player);
                drive = Drive.NONE;
                player.setSprinting(false);
            }
            case 405 -> enterPool(player);
            case 423 -> drive = Drive.SWIM;
            case 432 -> {
                check("NANOSUIT_2_POWER_SWIM", NanosuitSwimBoost.isActive(player)
                    && Math.abs(client.swimSpeed() - 1.4) < 0.001, player);
                equip(player, FIRST);
            }
            case 439 -> check("SWIM_BONUS_CANCELLED", generation(player) == FIRST && client.generation() == FIRST
                && !NanosuitSwimBoost.isActive(player) && player.getAttributeValue(NeoForgeMod.SWIM_SPEED) == 1.0
                && client.swimSpeed() == 1.0, player);
            case 445 -> prepareSave(player);
            case 454 -> {
                check("SAVE_NANOSUIT_2", hasGear(player, SECOND) && generation(player) == SECOND
                    && client.generation() == SECOND && energy.getEnergy() == STORED_ENERGY, player);
                complete = true;
            }
            default -> { }
        }
    }

    private void drivePlayer(PlayerTickEvent.Pre event) {
        if (drive != Drive.NONE && event.getEntity() instanceof ServerPlayer player) {
            player.setSprinting(true);
            if (drive == Drive.SWIM) {
                player.setSwimming(true);
            }
        }
    }

    private void prepareFirstScene(ServerPlayer player) {
        prepareFloor(player);
        player.setGameMode(GameType.SURVIVAL);
        player.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        player.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        setEnergy(player, 100.0F);
        captureRequest = FIRST;
    }

    private void prepareSave(ServerPlayer player) {
        drive = Drive.NONE;
        player.setSprinting(false);
        player.setSwimming(false);
        player.setNoGravity(false);
        player.teleportTo(0.5, 181.0, 0.5);
        equip(player, SECOND);
        for (int i = 0; i < SLOTS.length; i++) {
            player.getItemBySlot(SLOTS[i]).setDamageValue(i + 1);
            player.getInventory().setItem(STORED_LEGACY_SLOT + i, historicalGear[i].copy());
        }
        setEnergy(player, STORED_ENERGY);
        NanosuitCapabilities.get(player).setRegenDelayTicks(1200);
    }

    private static void setEnergy(ServerPlayer player, float value) {
        var energy = NanosuitCapabilities.get(player);
        energy.setEnergy(100.0F);
        energy.setMode(NanosuitMode.NONE);
        energy.setSprintBoostReady(false);
        energy.setEnergy(value);
        energy.setRegenDelayTicks(30);
        NanosuitSyncManager.syncToClient(player);
    }

    private static void equip(ServerPlayer player, NanosuitGeneration generation) {
        for (int i = 0; i < SLOTS.length; i++) {
            player.setItemSlot(SLOTS[i], stack(generation, i));
        }
    }

    private static ItemStack stack(NanosuitGeneration generation, int slot) {
        var key = ResourceLocation.parse(itemId(generation, slot));
        return new ItemStack(BuiltInRegistries.ITEM.getOptional(key).orElseThrow());
    }

    private static boolean hasGear(Player player, NanosuitGeneration generation) {
        for (int i = 0; i < SLOTS.length; i++) {
            if (!id(player.getItemBySlot(SLOTS[i])).equals(itemId(generation, i))) {
                return false;
            }
        }
        return true;
    }

    private static String itemId(NanosuitGeneration generation, int slot) {
        return "crysismod:" + (generation == FIRST ? "nanogeo_nanosuit_" : "nanosuit_2_") + SUFFIXES[slot];
    }

    private static String id(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }

    private static NanosuitGeneration generation(Player player) {
        var profile = NanosuitArmorUtil.getEquippedProfile(player);
        return profile == null ? null : profile.generation();
    }

    private static void prepareFloor(ServerPlayer player) {
        fill(player, new BlockPos(-5, 180, -3), new BlockPos(8, 180, 3), Blocks.STONE);
        fill(player, new BlockPos(-2, 181, -2), new BlockPos(7, 187, 2), Blocks.AIR);
        player.serverLevel().setDayTime(6000);
        player.teleportTo(player.serverLevel(), 0.5, 181.0, 0.5, Set.of(), 0.0F, 10.0F);
        player.setDeltaMovement(Vec3.ZERO);
    }

    private static void enterPool(ServerPlayer player) {
        for (BlockPos pos : BlockPos.betweenClosed(5, 180, 4, 13, 185, 12)) {
            boolean wall = pos.getY() == 180 || pos.getX() == 5 || pos.getX() == 13 || pos.getZ() == 4 || pos.getZ() == 12;
            player.serverLevel().setBlockAndUpdate(pos, (wall ? Blocks.STONE : Blocks.WATER).defaultBlockState());
        }
        setEnergy(player, 100.0F);
        player.setNoGravity(true);
        player.teleportTo(9.5, 181.1, 8.5);
    }

    private static void fill(ServerPlayer player, BlockPos start, BlockPos end, Block block) {
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

    private static int runtimeInt(ServerPlayer player, String name) {
        try {
            var states = (Map<?, ?>) RUNTIMES.get(null);
            Object state = states.get(player.getUUID());
            return field(state.getClass(), name).getInt(state);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Cannot read QA observation " + name, exception);
        }
    }

    private void check(String label, boolean pass, ServerPlayer player) {
        if (!pass) {
            failures++;
        }
        System.out.println("EQUIPMENT_QA_" + label + (pass ? "_PASS" : "_FAIL")
            + " tick=" + ticks + " server=" + Snapshot.of(player) + " client=" + client);
    }

    private enum Drive { NONE, SPRINT, SWIM }

    private record Snapshot(NanosuitGeneration generation, float energy, NanosuitMode mode, boolean sprint,
                            boolean locked, boolean invisible, boolean absorption, double swimSpeed) {
        static Snapshot of(Player player) {
            var energy = NanosuitCapabilities.get(player);
            return new Snapshot(EquipmentQa.generation(player), energy.getEnergy(), energy.getMode(),
                energy.isSprintBoostReady(), energy.isRecoveryLocked(), player.isInvisible(),
                player.hasEffect(MobEffects.ABSORPTION), player.getAttributeValue(NeoForgeMod.SWIM_SPEED));
        }
    }
}
