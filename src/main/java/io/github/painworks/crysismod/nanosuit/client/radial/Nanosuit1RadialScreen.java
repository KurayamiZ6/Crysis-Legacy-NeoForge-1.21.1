package io.github.painworks.crysismod.nanosuit.client.radial;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import io.github.painworks.crysismod.nanosuit.NanosuitMode;
import io.github.painworks.crysismod.nanosuit.NanosuitProfile;
import io.github.painworks.crysismod.nanosuit.config.NanosuitRadialConfig;
import io.github.painworks.crysismod.nanosuit.energy.NanosuitCapabilities;
import io.github.painworks.crysismod.nanosuit.network.NanosuitNetwork;
import io.github.painworks.crysismod.nanosuit.network.ToggleNanosuitModePacket;
import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;
import java.util.IdentityHashMap;
import java.util.Map;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

/** Hold-to-select screen. Only the existing server response changes the active mode. */
public final class Nanosuit1RadialScreen extends Screen {
    private static final NanosuitMode[] MODES = {NanosuitMode.ARMOR, NanosuitMode.STRENGTH, NanosuitMode.SPEED, NanosuitMode.CLOAK};
    private static final double[][] CENTERS = {{-74, 0}, {74, 0}, {0, -74}, {0, 73}};
    private static float lastPointerDegrees;
    private final InputConstants.Key openingKey;
    private final long openedAt = System.nanoTime();
    private NanosuitMode hovered = NanosuitMode.NONE;
    private long hoverStartedAt;
    private final float[] offsetStarts = new float[MODES.length];
    private boolean finished;
    private final boolean sneakBeforeOpen, sprintBeforeOpen;
    private boolean previousSneakHeld, previousSprintHeld;
    private final Map<KeyMapping, IKeyConflictContext> movementContexts = new IdentityHashMap<>();

    public Nanosuit1RadialScreen(InputConstants.Key openingKey) {
        super(Component.translatable("key.crysismod.nanosuit1_radial"));
        this.openingKey = openingKey;
        var options = Minecraft.getInstance().options;
        sneakBeforeOpen = options.keyShift.isDown();
        sprintBeforeOpen = options.keySprint.isDown();
        previousSneakHeld = isHeld(options.keyShift.getKey());
        previousSprintHeld = isHeld(options.keySprint.getKey());
    }

    @Override protected void init() {
        GLFW.glfwSetCursorPos(minecraft.getWindow().getWindow(), minecraft.getWindow().getScreenWidth() / 2.0,
            minecraft.getWindow().getScreenHeight() / 2.0);
        hovered = NanosuitMode.NONE;
        // NeoForge's IN_GAME context is inactive while a Screen is open.
        // Temporarily enable only movement bindings, without forwarding combat input.
        for (KeyMapping key : movementKeys()) enableMovementContext(key);
        enableMovementContext(minecraft.options.keyShift);
        enableMovementContext(minecraft.options.keySprint);
        restoreToggle(minecraft.options.keyShift, minecraft.options.toggleCrouch().get(), sneakBeforeOpen);
        restoreToggle(minecraft.options.keySprint, minecraft.options.toggleSprint().get(), sprintBeforeOpen);
        updateMovementKeys();
    }

    private boolean validContext() {
        return minecraft.player != null && minecraft.level != null && minecraft.getConnection() != null
            && minecraft.player.isAlive() && minecraft.isWindowActive() && minecraft.getOverlay() == null
            && NanosuitArmorUtil.getEquippedProfile(minecraft.player) == NanosuitProfile.NANOSUIT_1;
    }

    private boolean held() {
        return isHeld(openingKey);
    }

    private static boolean isHeld(InputConstants.Key keyMapping) {
        long window = Minecraft.getInstance().getWindow().getWindow();
        if (keyMapping.getType() == InputConstants.Type.MOUSE)
            return GLFW.glfwGetMouseButton(window, keyMapping.getValue()) == GLFW.GLFW_PRESS;
        if (keyMapping.getType() == InputConstants.Type.KEYSYM)
            return keyMapping.getValue() >= 0 && GLFW.glfwGetKey(window, keyMapping.getValue()) == GLFW.GLFW_PRESS;
        for (int key = GLFW.GLFW_KEY_SPACE; key <= GLFW.GLFW_KEY_LAST; key++)
            if (GLFW.glfwGetKeyScancode(key) == keyMapping.getValue())
                return GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS;
        return false;
    }

    private KeyMapping[] movementKeys() {
        var options = minecraft.options;
        return new KeyMapping[]{options.keyUp, options.keyDown, options.keyLeft, options.keyRight, options.keyJump};
    }

    private static void restoreToggle(KeyMapping key, boolean toggle, boolean desired) {
        if (toggle && key.isDown() != desired) key.setDown(true);
    }

    private void enableMovementContext(KeyMapping key) {
        movementContexts.putIfAbsent(key, key.getKeyConflictContext());
        key.setKeyConflictContext(KeyConflictContext.UNIVERSAL);
    }

    /** Feed only vanilla movement keys; attacks/use and other bindings stay captured by the screen. */
    public void updateMovementKeys() {
        if (!validContext() || finished) return;
        for (KeyMapping key : movementKeys()) key.setDown(isHeld(key.getKey()));
        var options = minecraft.options;
        boolean sneak = isHeld(options.keyShift.getKey());
        boolean sprint = isHeld(options.keySprint.getKey());
        if (!options.toggleCrouch().get()) options.keyShift.setDown(sneak);
        else if (sneak && !previousSneakHeld) options.keyShift.setDown(true);
        if (!options.toggleSprint().get()) options.keySprint.setDown(sprint);
        else if (sprint && !previousSprintHeld) options.keySprint.setDown(true);
        previousSneakHeld = sneak;
        previousSprintHeld = sprint;
    }

    private void toggleMovementPress(InputConstants.Key key) {
        var options = minecraft.options;
        if (options.toggleCrouch().get() && key.equals(options.keyShift.getKey()) && !previousSneakHeld) {
            options.keyShift.setDown(true);
            previousSneakHeld = true;
        }
        if (options.toggleSprint().get() && key.equals(options.keySprint.getKey()) && !previousSprintHeld) {
            options.keySprint.setDown(true);
            previousSprintHeld = true;
        }
    }

    @Override public void tick() {
        if (!validContext()) finish(false);
        else if (!held()) finish(true);
    }

    private void finish(boolean commit) {
        if (finished) return;
        finished = true;
        if (commit && validContext() && hovered != NanosuitMode.NONE
            && hovered != NanosuitCapabilities.get(minecraft.player).getMode())
            NanosuitNetwork.sendToServer(new ToggleNanosuitModePacket(hovered));
        boolean sneak = minecraft.options.keyShift.isDown(), sprint = minecraft.options.keySprint.isDown();
        minecraft.setScreen(null);
        if (minecraft.isWindowActive() && minecraft.player != null) {
            for (KeyMapping key : movementKeys()) key.setDown(isHeld(key.getKey()));
            restoreToggle(minecraft.options.keyShift, minecraft.options.toggleCrouch().get(), sneak);
            restoreToggle(minecraft.options.keySprint, minecraft.options.toggleSprint().get(), sprint);
        }
    }

    @Override public void onClose() { finish(false); }
    @Override public void removed() {
        finished = true;
        for (KeyMapping key : movementKeys()) key.setDown(false);
        minecraft.options.keyShift.setDown(false);
        minecraft.options.keySprint.setDown(false);
        movementContexts.forEach(KeyMapping::setKeyConflictContext);
        movementContexts.clear();
    }
    @Override public boolean isPauseScreen() { return false; }
    @Override public boolean keyPressed(int key, int scan, int modifiers) {
        if (key == GLFW.GLFW_KEY_ESCAPE) finish(false);
        else toggleMovementPress(InputConstants.getKey(key, scan));
        return true;
    }
    @Override public boolean keyReleased(int key, int scan, int modifiers) {
        var released = InputConstants.getKey(key, scan);
        if (released.equals(minecraft.options.keyShift.getKey())) previousSneakHeld = false;
        if (released.equals(minecraft.options.keySprint.getKey())) previousSprintHeld = false;
        if (openingKey.equals(InputConstants.getKey(key, scan))) finish(true);
        return true;
    }
    @Override public boolean mouseClicked(double x, double y, int button) {
        toggleMovementPress(InputConstants.Type.MOUSE.getOrCreate(button));
        return true;
    }
    @Override public boolean mouseReleased(double x, double y, int button) {
        if (openingKey.equals(InputConstants.Type.MOUSE.getOrCreate(button))) {
            updateHover(x, y);
            finish(true);
        }
        return true;
    }
    @Override public boolean mouseScrolled(double x, double y, double horizontal, double vertical) { return true; }
    @Override public void mouseMoved(double x, double y) { updateHover(x, y); }

    private void updateHover(double x, double y) {
        double scale = RadialLayout.scale(height, NanosuitRadialConfig.SCALE.get());
        double dx = (x - width / 2.0) / scale;
        double dy = (y - height / 2.0) / scale;
        lastPointerDegrees = RadialLayout.pointerDegrees(dx, dy, lastPointerDegrees);
        NanosuitMode next = RadialLayout.select(dx, dy);
        if (next != hovered) {
            long now = System.nanoTime();
            for (int i = 0; i < MODES.length; i++) offsetStarts[i] = panelOffset(i, now);
            hovered = next;
            hoverStartedAt = now;
        }
    }

    private float panelOffset(int index, long now) {
        float target = MODES[index] == hovered ? NanosuitRadialConfig.SELECTED_PANEL_OFFSET.get().floatValue() : 0;
        float progress = RadialLayout.panelProgress((now - hoverStartedAt) / 1_000_000_000.0);
        return offsetStarts[index] + (target - offsetStarts[index]) * progress;
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath("crysismod", "textures/gui/nanosuit1_radial/" + name + ".png");
    }

    private void tile(GuiGraphics g, String layer, int index, float r, float green, float b, float alpha) {
        int theme=io.github.painworks.crysismod.nanosuit.config.NanosuitClientConfig.classicInterfaceColor();
        if (theme != io.github.painworks.crysismod.nanosuit.client.ClassicHudPalette.DEFAULT && layer.startsWith("green-")) {
            layer="themed/"+layer;
            r=io.github.painworks.crysismod.nanosuit.client.ClassicHudPalette.red(theme);
            green=io.github.painworks.crysismod.nanosuit.client.ClassicHudPalette.green(theme);
            b=io.github.painworks.crysismod.nanosuit.client.ClassicHudPalette.blue(theme);
        }
        g.setColor(r, green, b, alpha);
        // Transparent sprite rectangles must not occlude subsequent UI layers.
        RenderSystem.disableDepthTest();
        g.blit(texture(layer), -128, -128, index * 256f, 0f, 256, 256, 1024, 256);
    }

    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Screen's integer render coordinates would quantize the otherwise continuous pointer.
        updateHover(minecraft.mouseHandler.xpos() * width / minecraft.getWindow().getScreenWidth(),
            minecraft.mouseHandler.ypos() * height / minecraft.getWindow().getScreenHeight());
        double seconds = (System.nanoTime() - openedAt) / 1_000_000_000.0;
        float flash = RadialLayout.hoverFlash((System.nanoTime() - hoverStartedAt) / 1_000_000_000.0,
            NanosuitRadialConfig.HOVER_FLASH_SECONDS.get());
        float scale = (float) RadialLayout.scale(height, NanosuitRadialConfig.SCALE.get());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        g.pose().pushPose();
        g.pose().translate(width / 2f, height / 2f, 0);
        g.pose().scale(scale, scale, 1);
        for (int i = 0; i < MODES.length; i++) {
            boolean hover = MODES[i] == hovered;
            boolean amberIcon = hover;
            g.pose().pushPose();
            float offset = panelOffset(i, System.nanoTime());
            g.pose().translate((float) CENTERS[i][0] + (float) Math.signum(CENTERS[i][0]) * offset,
                (float) CENTERS[i][1] + (float) Math.signum(CENTERS[i][1]) * offset, 0);
            float panelScale = i == 2 ? 1f : .95f;
            g.pose().scale(panelScale / 3f, panelScale / 3f, 1);
            tile(g, "green-fill", i, 1, 1, 1, NanosuitRadialConfig.BACKGROUND_ALPHA.get().floatValue());
            tile(g, "green-frame", i, 1, 1, 1, .78f);
            if (hover && flash > 0) {
                tile(g, "amber-fill", i, 1, 1, 1, NanosuitRadialConfig.HOVER_BACKGROUND_ALPHA.get().floatValue() * flash);
                tile(g, "flash-fill", i, 1, 1, 1, flash * NanosuitRadialConfig.HOVER_FLASH_INTENSITY.get().floatValue());
                tile(g, "hex-glow", i, 1, 1, 1, NanosuitRadialConfig.HEX_ALPHA.get().floatValue() * flash);
                tile(g, "amber-frame", i, 1, 1, 1, flash);
                tile(g, "flash-frame", i, 1, 1, 1, flash);
            }
            int theme=io.github.painworks.crysismod.nanosuit.config.NanosuitClientConfig.classicInterfaceColor();
            boolean custom=theme!=io.github.painworks.crysismod.nanosuit.client.ClassicHudPalette.DEFAULT;
            tile(g, "icons", i, amberIcon ? 1f : custom?io.github.painworks.crysismod.nanosuit.client.ClassicHudPalette.red(theme):118/255f,
                amberIcon ? 196/255f : custom?io.github.painworks.crysismod.nanosuit.client.ClassicHudPalette.green(theme):150/255f,
                amberIcon ? 60/255f : custom?io.github.painworks.crysismod.nanosuit.client.ClassicHudPalette.blue(theme):117/255f, 1);
            g.pose().popPose();
        }
        g.pose().pushPose();
        g.pose().mulPose(Axis.ZP.rotationDegrees(RadialLayout.ringDegrees(seconds)));
        g.setColor(1, 1, 1, .76f);
        int theme=io.github.painworks.crysismod.nanosuit.config.NanosuitClientConfig.classicInterfaceColor();
        boolean custom=theme!=io.github.painworks.crysismod.nanosuit.client.ClassicHudPalette.DEFAULT;
        if(custom)g.setColor(io.github.painworks.crysismod.nanosuit.client.ClassicHudPalette.red(theme),
            io.github.painworks.crysismod.nanosuit.client.ClassicHudPalette.green(theme),io.github.painworks.crysismod.nanosuit.client.ClassicHudPalette.blue(theme),.76f);
        RenderSystem.disableDepthTest();
        g.blit(texture(custom?"themed/ring":"ring"), -48, -48, 0f, 0f, 96, 96, 96, 96);
        g.pose().popPose();
        g.pose().mulPose(Axis.ZP.rotationDegrees(lastPointerDegrees));
        g.setColor(1, 1, 1, 1);
        RenderSystem.disableDepthTest();
        g.blit(texture("pointer"), -56, -56, 0f, 0f, 112, 112, 112, 112);
        g.pose().popPose();
        g.setColor(1, 1, 1, 1);
    }
}
