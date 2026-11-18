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
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

/** Hold-to-select screen. Only the existing server response changes the active mode. */
public final class Nanosuit1RadialScreen extends Screen {
    private static final NanosuitMode[] MODES = {NanosuitMode.ARMOR, NanosuitMode.STRENGTH, NanosuitMode.SPEED, NanosuitMode.CLOAK};
    private static final double[][] CENTERS = {{-74, 0}, {74, 0}, {0, -90}, {0, 73}};
    private final InputConstants.Key openingKey;
    private final long openedAt = System.nanoTime();
    private NanosuitMode hovered = NanosuitMode.NONE;
    private long hoverStartedAt;
    private boolean finished;

    public Nanosuit1RadialScreen(InputConstants.Key openingKey) {
        super(Component.translatable("key.crysismod.nanosuit1_radial"));
        this.openingKey = openingKey;
    }

    @Override protected void init() {
        GLFW.glfwSetCursorPos(minecraft.getWindow().getWindow(), minecraft.getWindow().getScreenWidth() / 2.0,
            minecraft.getWindow().getScreenHeight() / 2.0);
        hovered = NanosuitMode.NONE;
    }

    private boolean validContext() {
        return minecraft.player != null && minecraft.level != null && minecraft.getConnection() != null
            && minecraft.player.isAlive() && minecraft.isWindowActive() && minecraft.getOverlay() == null
            && NanosuitArmorUtil.getEquippedProfile(minecraft.player) == NanosuitProfile.NANOSUIT_1;
    }

    private boolean held() {
        long window = minecraft.getWindow().getWindow();
        if (openingKey.getType() == InputConstants.Type.MOUSE)
            return GLFW.glfwGetMouseButton(window, openingKey.getValue()) == GLFW.GLFW_PRESS;
        if (openingKey.getType() == InputConstants.Type.KEYSYM)
            return openingKey.getValue() >= 0 && GLFW.glfwGetKey(window, openingKey.getValue()) == GLFW.GLFW_PRESS;
        for (int key = GLFW.GLFW_KEY_SPACE; key <= GLFW.GLFW_KEY_LAST; key++)
            if (GLFW.glfwGetKeyScancode(key) == openingKey.getValue())
                return GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS;
        return false;
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
        minecraft.setScreen(null);
    }

    @Override public void onClose() { finish(false); }
    @Override public void removed() { finished = true; }
    @Override public boolean isPauseScreen() { return false; }
    @Override public boolean keyPressed(int key, int scan, int modifiers) {
        if (key == GLFW.GLFW_KEY_ESCAPE) finish(false);
        return true;
    }
    @Override public boolean keyReleased(int key, int scan, int modifiers) {
        if (openingKey.equals(InputConstants.getKey(key, scan))) finish(true);
        return true;
    }
    @Override public boolean mouseClicked(double x, double y, int button) { return true; }
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
        NanosuitMode next = RadialLayout.select((x - width / 2.0) / scale, (y - height / 2.0) / scale);
        if (next != hovered) {
            hovered = next;
            hoverStartedAt = System.nanoTime();
        }
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath("crysismod", "textures/gui/nanosuit1_radial/" + name + ".png");
    }

    private void tile(GuiGraphics g, String layer, int index, float r, float green, float b, float alpha) {
        g.setColor(r, green, b, alpha);
        // Transparent sprite rectangles must not occlude subsequent UI layers.
        RenderSystem.disableDepthTest();
        g.blit(texture(layer), -128, -128, index * 256f, 0f, 256, 256, 1024, 256);
    }

    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        updateHover(mouseX, mouseY);
        double seconds = (System.nanoTime() - openedAt) / 1_000_000_000.0;
        float flash = RadialLayout.hoverFlash((System.nanoTime() - hoverStartedAt) / 1_000_000_000.0,
            NanosuitRadialConfig.HOVER_FLASH_SECONDS.get());
        NanosuitMode active = minecraft.player == null ? NanosuitMode.NONE : NanosuitCapabilities.get(minecraft.player).getMode();
        float scale = (float) RadialLayout.scale(height, NanosuitRadialConfig.SCALE.get());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        g.pose().pushPose();
        g.pose().translate(width / 2f, height / 2f, 0);
        g.pose().scale(scale, scale, 1);
        for (int i = 0; i < MODES.length; i++) {
            boolean hover = MODES[i] == hovered;
            boolean amberIcon = hover || MODES[i] == active;
            g.pose().pushPose();
            g.pose().translate((float) CENTERS[i][0], (float) CENTERS[i][1], 0);
            float panelScale = i == 2 ? 1f : .95f;
            g.pose().scale(panelScale / 3f, panelScale / 3f, 1);
            tile(g, hover ? "amber-fill" : "green-fill", i, 1, 1, 1,
                (float) (hover ? NanosuitRadialConfig.HOVER_BACKGROUND_ALPHA.get() : NanosuitRadialConfig.BACKGROUND_ALPHA.get()).doubleValue());
            if (hover && flash > 0) {
                tile(g, "flash-fill", i, 1, 1, 1, flash * NanosuitRadialConfig.HOVER_FLASH_INTENSITY.get().floatValue());
                tile(g, "hex", i, 1, 1, 1, NanosuitRadialConfig.HEX_ALPHA.get().floatValue() * flash);
            }
            tile(g, hover ? "amber-frame" : "green-frame", i, 1, 1, 1, hover ? 1 : .78f);
            tile(g, "icons", i, amberIcon ? 1f : 118/255f, amberIcon ? 196/255f : 150/255f,
                amberIcon ? 60/255f : 117/255f, 1);
            g.pose().popPose();
        }
        g.pose().pushPose();
        g.pose().mulPose(Axis.ZP.rotationDegrees(RadialLayout.ringDegrees(seconds)));
        g.setColor(1, 1, 1, .76f);
        RenderSystem.disableDepthTest();
        g.blit(texture("ring"), -48, -48, 0f, 0f, 96, 96, 96, 96);
        g.pose().popPose();
        float pointer = switch (hovered) { case ARMOR -> -90; case STRENGTH -> 90; case CLOAK -> 180; default -> 0; };
        g.pose().mulPose(Axis.ZP.rotationDegrees(pointer));
        g.setColor(1, 1, 1, 1);
        RenderSystem.disableDepthTest();
        g.blit(texture("pointer"), -56, -56, 0f, 0f, 112, 112, 112, 112);
        g.pose().popPose();
        g.setColor(1, 1, 1, 1);
    }
}
