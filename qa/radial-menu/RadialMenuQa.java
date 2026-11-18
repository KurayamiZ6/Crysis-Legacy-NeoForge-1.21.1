package local.crysisqa;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.painworks.crysismod.nanosuit.NanosuitMode;
import io.github.painworks.crysismod.nanosuit.NanosuitProfile;
import io.github.painworks.crysismod.nanosuit.client.NanosuitKeyMappings;
import io.github.painworks.crysismod.nanosuit.client.NanosuitPowerFeedback;
import io.github.painworks.crysismod.nanosuit.config.NanosuitRadialConfig;
import io.github.painworks.crysismod.nanosuit.client.radial.Nanosuit1RadialScreen;
import io.github.painworks.crysismod.nanosuit.client.radial.RadialLayout;
import io.github.painworks.crysismod.nanosuit.energy.NanosuitCapabilities;
import io.github.painworks.crysismod.nanosuit.handler.NanosuitPlayerHandler;
import io.github.painworks.crysismod.nanosuit.network.NanosuitSyncManager;
import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import io.github.painworks.crysismod.registry.ModItems;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;
import org.lwjgl.glfw.GLFW;

/** Isolated QA mod; real OS key events drive the production screen and packets. */
@Mod("crysisradialqa")
public final class RadialMenuQa {
    private record Step(String name, Runnable start, Runnable end, int ticks) {}
    private final ConcurrentLinkedQueue<Consumer<ServerPlayer>> commands = new ConcurrentLinkedQueue<>();
    private final List<Step> steps = new ArrayList<>();
    private InputClient robot;
    private int index, elapsed, timeout;
    private boolean initialized, serverReady, finished;
    private boolean refill = true;
    private volatile NanosuitMode serverMode = NanosuitMode.NONE;
    private volatile int serverChanges;
    private int savedChanges;
    private long spinStart;
    private float yaw, pitch;
    private int armorKey;
    private NanosuitMode expectedN2Shortcut;
    private boolean capturedFlashStart, capturedFlashEnd, capturedSpeedFlash;
    private long flashEntry;
    private final AtomicInteger passes=new AtomicInteger(), failures=new AtomicInteger();
    private final ConcurrentLinkedQueue<String> modeSounds=new ConcurrentLinkedQueue<>();
    private final Set<String> samples=new HashSet<>();
    private long savedFlash;
    private float savedPointer;
    private net.minecraft.world.phys.Vec3 movementStart;
    private int forwardKey, strafeKey, sneakKey, sprintKey, jumpKey;
    private boolean jumpObserved;
    private long stepStartedAt;

    public RadialMenuQa() throws Exception {
        robot = new InputClient();
        NeoForge.EVENT_BUS.addListener(this::server);
        NeoForge.EVENT_BUS.addListener(this::client);
        NeoForge.EVENT_BUS.addListener(this::render);
        NeoForge.EVENT_BUS.addListener(this::renderActivation);
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.client.event.InputEvent.Key event)->{
            if(event.getKey()==GLFW.GLFW_KEY_F7)System.out.println("RADIAL_QA_F7 action="+event.getAction()+" focus="+mc().isWindowActive()+" screen="+mc().screen);
        });
        NeoForge.EVENT_BUS.addListener((ScreenEvent.Opening event)->System.out.println("RADIAL_QA_SCREEN "+event.getNewScreen()));
        NeoForge.EVENT_BUS.addListener((PlaySoundEvent event)-> {
            if(event.getSound()!=null) {
                String name=event.getSound().getLocation().toString();
                if(name.startsWith("crysismod:classic_") || name.equals("crysismod:cloak_activation")
                    || name.equals("crysismod:cloak_deactivation") || name.equals("crysismod:cloak_engaged")) {
                    modeSounds.add(name);
                    System.out.println("RADIAL_QA_SOUND "+name);
                }
            }
        });
    }

    private void check(String name, boolean pass) {
        (pass?passes:failures).incrementAndGet();
        System.out.println("RADIAL_QA " + (pass ? "PASS " : "FAIL ") + name);
    }
    private Minecraft mc() { return Minecraft.getInstance(); }
    private void equip(ServerPlayer p, boolean n2) {
        p.setItemSlot(EquipmentSlot.HEAD, new ItemStack(n2 ? ModItems.NANOSUIT_2_HELMET.get() : ModItems.NANOGEO_NANOSUIT_HELMET.get()));
        p.setItemSlot(EquipmentSlot.CHEST, new ItemStack(n2 ? ModItems.NANOSUIT_2_CHESTPLATE.get() : ModItems.NANOGEO_NANOSUIT_CHESTPLATE.get()));
        p.setItemSlot(EquipmentSlot.LEGS, new ItemStack(n2 ? ModItems.NANOSUIT_2_LEGGINGS.get() : ModItems.NANOGEO_NANOSUIT_LEGGINGS.get()));
        p.setItemSlot(EquipmentSlot.FEET, new ItemStack(n2 ? ModItems.NANOSUIT_2_BOOTS.get() : ModItems.NANOGEO_NANOSUIT_BOOTS.get()));
    }
    private void server(ServerTickEvent.Post e) {
        if (e.getServer().getPlayerList().getPlayers().isEmpty()) return;
        var p = e.getServer().getPlayerList().getPlayers().getFirst();
        if (!serverReady) {
            equip(p, false);
            p.getInventory().setItem(p.getInventory().selected, ItemStack.EMPTY);
            p.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
            p.serverLevel().setDayTime(6000);
            p.serverLevel().getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, e.getServer());
            p.serverLevel().getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, e.getServer());
            NanosuitCapabilities.get(p).setEnergy(100);
            NanosuitCapabilities.get(p).setMode(NanosuitMode.ARMOR);
            NanosuitSyncManager.syncToClient(p);
            serverReady = true;
        }
        Consumer<ServerPlayer> command;
        while ((command = commands.poll()) != null) command.accept(p);
        var data = NanosuitCapabilities.get(p);
        if (refill) data.setEnergyWithoutStartingRecovery(100);
        if (data.getMode() != serverMode) {
            serverMode = data.getMode();
            serverChanges++;
            System.out.println("RADIAL_QA_SERVER_MODE " + serverMode + " tick=" + p.tickCount);
        }
    }
    private void step(String name, int ticks, Runnable start, Runnable end) { steps.add(new Step(name, start, end, ticks)); }
    private void point(int dx, int dy) {
        var w = mc().getWindow();
        if(mc().screen instanceof VisualRadial visual) {
            visual.mouseX=w.getScreenWidth()/2.0+dx;visual.mouseY=w.getScreenHeight()/2.0+dy;return;
        }
        GLFW.glfwSetCursorPos(w.getWindow(), w.getScreenWidth()/2.0 + dx, w.getScreenHeight()/2.0 + dy);
    }
    private void hold() {
        if(Boolean.getBoolean("radialqa.feedbackvisualonly")) {mc().setScreen(new VisualRadial());return;}
        GLFW.glfwFocusWindow(mc().getWindow().getWindow());
        System.out.println("RADIAL_QA_INPUT hold focus="+mc().isWindowActive());
        if (mc().isWindowActive()) robot.keyPress(KeyEvent.VK_F7);
    }
    private void release() {
        if(Boolean.getBoolean("radialqa.feedbackvisualonly")) {if(mc().screen instanceof VisualRadial visual)visual.onClose();return;}
        robot.keyRelease(KeyEvent.VK_F7);
    }
    private Nanosuit1RadialScreen radial() {
        return mc().screen instanceof VisualRadial visual?visual.delegate:mc().screen instanceof Nanosuit1RadialScreen screen?screen:null;
    }
    private void capture(String name) {
        Screenshot.grab(mc().gameDirectory, name + ".png", mc().getMainRenderTarget(), ignored -> {});
        System.out.println("RADIAL_QA_CAPTURE " + name + " size=" + mc().getWindow().getWidth() + "x" + mc().getWindow().getHeight());
    }
    private void render(ScreenEvent.Render.Post e) {
        var radial=radial();
        if (radial==null || index>=steps.size()) return;
        String step=steps.get(index).name();
        if (!step.equals("strength-hover-3") && !step.equals("speed-hover-3")) return;
        try {
            var field=Nanosuit1RadialScreen.class.getDeclaredField("hoverStartedAt");
            field.setAccessible(true);
            var hover=Nanosuit1RadialScreen.class.getDeclaredField("hovered");
            hover.setAccessible(true);
            if (hover.get(radial)!=(step.equals("strength-hover-3")?NanosuitMode.STRENGTH:NanosuitMode.SPEED)) return;
            if (field.getLong(radial)==0) return;
            double seconds=(System.nanoTime()-field.getLong(radial))/1e9;
            float flash=RadialLayout.hoverFlash(seconds,.50);
            if (step.equals("strength-hover-3")) {
                for(double mark:new double[]{.08,.25,.55}) {
                    if(seconds>=mark && seconds<mark+.08 && samples.add("menu-"+mark)) {
                        capture("gui3-strength-t"+Math.round(mark*1000));
                        System.out.println("RADIAL_QA_PANEL seconds="+seconds+" offset="+offset(1)+" flash="+flash);
                    }
                }
                if (!capturedFlashStart && flash>.5) {capture("gui3-strength-flash");capturedFlashStart=true;flashEntry=field.getLong(radial);System.out.println("RADIAL_QA_FLASH start="+flash+" seconds="+seconds);}
                if (!capturedFlashEnd && seconds>.55 && capturedFlashStart) {capture("gui3-strength-after-flash");capturedFlashEnd=true;check("hover-stationary-does-not-retrigger",flashEntry==field.getLong(radial)&&flash==0);check("selected-panel-outward",Math.abs(offset(1)-NanosuitRadialConfig.SELECTED_PANEL_OFFSET.get())<.001&&offset(0)==0&&offset(2)==0&&offset(3)==0);System.out.println("RADIAL_QA_FLASH end="+flash+" seconds="+seconds);}
            } else if (!capturedSpeedFlash && flash>.5) {capture("gui3-speed-flash");capturedSpeedFlash=true;System.out.println("RADIAL_QA_FLASH speed="+flash+" seconds="+seconds);}
        } catch (ReflectiveOperationException ex) {throw new RuntimeException(ex);}
    }
    private void mode(String name, NanosuitMode expected) {
        check(name, serverMode == expected && NanosuitCapabilities.get(mc().player).getMode() == expected);
    }
    private Object field(Class<?> type,String name,Object instance) {
        try {var f=type.getDeclaredField(name);f.setAccessible(true);return f.get(instance);}
        catch(ReflectiveOperationException ex) {throw new RuntimeException(ex);}
    }
    private long flashStart() {return (long)field(NanosuitPowerFeedback.class,"flashStart",null);}
    private float pointer() {return (float)field(Nanosuit1RadialScreen.class,"lastPointerDegrees",null);}
    private float offset(int i) {
        try {var m=Nanosuit1RadialScreen.class.getDeclaredMethod("panelOffset",int.class,long.class);m.setAccessible(true);return (float)m.invoke(radial(),i,System.nanoTime());}
        catch(ReflectiveOperationException ex) {throw new RuntimeException(ex);}
    }
    private void expectedSounds(String name,String... sounds) {
        List<String> actual=new ArrayList<>(modeSounds);actual.sort(String::compareTo);
        List<String> expected=new ArrayList<>();for(String sound:sounds)expected.add("crysismod:"+sound);expected.sort(String::compareTo);
        check(name,actual.equals(expected));
        System.out.println("RADIAL_QA_SOUND_LIST "+name+" "+actual);
    }
    private void renderActivation(RenderGuiEvent.Post event) {
        if(mc().player==null || mc().screen!=null || index>=steps.size())return;
        String step=steps.get(index).name();
        if(!step.startsWith("commit-") && !step.equals("n2-force-feedback"))return;
        NanosuitMode mode=NanosuitCapabilities.get(mc().player).getMode();
        if(step.startsWith("commit-") && !step.equals("commit-"+mode))return;
        long start=flashStart();
        double seconds=(System.nanoTime()-start)/1e9;
        if(mode==NanosuitMode.CLOAK && step.startsWith("commit-")) {
            if(samples.add("activation-cloak")) {check("cloak-no-mode-flash",start==0);capture("activation-cloak");}
            return;
        }
        String label=step.equals("n2-force-feedback")?"n2-force":mode.name().toLowerCase();
        if(start!=0 && seconds<.18 && samples.add("early-"+label)) {
            int color=(int)field(NanosuitPowerFeedback.class,"flashColor",null);
            check("activation-color-"+label,color==(label.equals("strength")?0xFF4B42:label.equals("speed")?0xFFD44A:0x73BAFF));
            capture("activation-"+label+"-early");
            System.out.println("RADIAL_QA_ACTIVATION "+label+" rgb="+Integer.toHexString(color)+" seconds="+seconds);
        }
        if(start!=0 && seconds>.30 && seconds<.48 && !label.equals("n2-force") && samples.add("middle-"+label))capture("activation-"+label+"-middle");
        if(start!=0 && seconds>.55 && !label.equals("n2-force") && samples.add("late-"+label))capture("activation-"+label+"-late");
    }
    private void buildSteps() {
        check("default-unbound", NanosuitKeyMappings.OPEN_NANOSUIT_1_RADIAL.isUnbound());
        check("dead-zone", RadialLayout.select(0, 0) == NanosuitMode.NONE && RadialLayout.select(41.9, 0) == NanosuitMode.NONE);
        check("four-cardinals", RadialLayout.select(-80, 0) == NanosuitMode.ARMOR && RadialLayout.select(80, 0) == NanosuitMode.STRENGTH
            && RadialLayout.select(0, -80) == NanosuitMode.SPEED && RadialLayout.select(0, 80) == NanosuitMode.CLOAK);
        check("ring-10s", RadialLayout.ringDegrees(10) == 0 && RadialLayout.ringDegrees(5) == 180 && RadialLayout.ringDegrees(2.5) == 90);
        check("single-flash-fades",RadialLayout.hoverFlash(0,.5)==1 && Math.abs(RadialLayout.hoverFlash(.25,.5)-.25)<.0001
            && RadialLayout.hoverFlash(.5,.5)==0 && RadialLayout.hoverFlash(1.5,.5)==0);
        check("configured-half-second",NanosuitRadialConfig.HOVER_FLASH_SECONDS.get()==.5);
        check("pointer-continuous",Math.abs(RadialLayout.pointerDegrees(120,-60,0)-63.43495)<.001 && RadialLayout.pointerDegrees(0,0,63)==63);
        check("quadrant-boundaries",RadialLayout.select(100,-99)==NanosuitMode.STRENGTH && RadialLayout.select(99,-100)==NanosuitMode.SPEED);
        NanosuitKeyMappings.OPEN_NANOSUIT_1_RADIAL.setKey(InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_F7));
        String localName=GLFW.glfwGetKeyName(NanosuitKeyMappings.TOGGLE_ARMOR.getKey().getValue(),0);
        armorKey=KeyEvent.getExtendedKeyCodeForChar(Character.toUpperCase(localName.charAt(0)));
        KeyMapping.resetMapping();
        if(Boolean.getBoolean("radialqa.feedbackonly")) {buildFeedbackSteps();return;}
        if (Boolean.getBoolean("radialqa.n2only")) {
            step("n2-with-armor-active",20,()->commands.add(p->{equip(p,true);NanosuitCapabilities.get(p).setMode(NanosuitMode.ARMOR);NanosuitSyncManager.syncToClient(p);}),()->{});
            step("n2-menu-blocked",10,this::hold,()->{check("n2-radial-blocked",mc().screen==null);release();});
            n2ShortcutSteps();
            step("n2-cooldown-wait",22,()->{},()->{});
            n2ShortcutSteps();
            return;
        }
        for (int gui : new int[]{2,3}) {
            step("gui-"+gui, 15, () -> { mc().options.guiScale().set(gui); mc().resizeDisplay(); }, () -> {});
            step("neutral-"+gui, 12, this::hold, () -> {check("open-"+gui, mc().screen instanceof Nanosuit1RadialScreen); capture("gui"+gui+"-neutral");});
            step("strength-hover-"+gui, 20, () -> {point(135, 0);savedFlash=flashStart();modeSounds.clear();}, () -> {mode("hover-no-change-"+gui, NanosuitMode.ARMOR);check("hover-no-activation-flash-"+gui,flashStart()==savedFlash);expectedSounds("hover-silent-"+gui);capture("gui"+gui+"-strength-hover-armor-active");});
            step("speed-hover-"+gui, 14, () -> point(0,-135), () -> {check("old-panel-returned-"+gui,offset(1)==0);capture("gui"+gui+"-speed-hover");});
            step("neutral-release-"+gui, 4, () -> point(0,0), this::release);
            step("neutral-check-"+gui, 8, () -> {}, () -> {mode("no-selection-keeps-armor-"+gui,NanosuitMode.ARMOR); check("cursor-restored-"+gui,mc().screen==null && mc().mouseHandler.isMouseGrabbed());});
        }
        step("free-pointer-open",8,this::hold,()->{});
        step("free-pointer-angle",10,()->point(120,-60),()->{savedPointer=pointer();check("free-63-degree-pointer",Math.abs(savedPointer-63.43495)<.01);capture("pointer-63deg");mc().screen.onClose();release();});
        step("close-before-reopen",8,()->{},()->{});
        step("pointer-reopen",8,this::hold,()->{check("pointer-orientation-retained",Math.abs(pointer()-savedPointer)<.01);capture("pointer-retained-neutral");});
        step("boundary-strength",8,()->point(100,-99),()->{check("boundary-right",field(Nanosuit1RadialScreen.class,"hovered",mc().screen)==NanosuitMode.STRENGTH);capture("boundary-strength");});
        step("boundary-speed",10,()->point(99,-100),()->{check("boundary-top",field(Nanosuit1RadialScreen.class,"hovered",mc().screen)==NanosuitMode.SPEED);check("boundary-old-panel-returned",offset(1)==0);capture("boundary-speed");});
        step("pointer-cancel",8,()->mc().screen.onClose(),this::release);
        if(Boolean.getBoolean("radialqa.visualonly")) {
            step("visual-proof-complete",8,()->{},()->check("all-flash-captures",capturedFlashStart&&capturedFlashEnd&&capturedSpeedFlash));
            return;
        }
        for (NanosuitMode mode : new NanosuitMode[]{NanosuitMode.STRENGTH,NanosuitMode.SPEED,NanosuitMode.CLOAK,NanosuitMode.ARMOR}) {
            step("open-"+mode, 8, this::hold, () -> {});
            step("hover-"+mode, 10, () -> {
                point(mode==NanosuitMode.STRENGTH?135:mode==NanosuitMode.ARMOR?-135:0, mode==NanosuitMode.SPEED?-135:mode==NanosuitMode.CLOAK?135:0);
                savedChanges=serverChanges; yaw=mc().player.getYRot(); pitch=mc().player.getXRot();
                modeSounds.clear();
            }, () -> {check("no-commit-before-release-"+mode,serverChanges==savedChanges); check("camera-blocked-"+mode,mc().player.getYRot()==yaw&&mc().player.getXRot()==pitch); release();});
            step("commit-"+mode, 10, () -> {}, () -> {
                mode("release-"+mode,mode);check("one-mode-transition-"+mode,serverChanges==savedChanges+1);
                switch(mode) {
                    case STRENGTH -> expectedSounds("strength-voice-only","classic_maximum_strength");
                    case SPEED -> expectedSounds("speed-voice-and-effect","classic_maximum_speed","classic_speed_activation");
                    case CLOAK -> expectedSounds("cloak-voice-and-effect","classic_cloak_on","classic_cloak_activation");
                    case ARMOR -> expectedSounds("cloak-exit-and-armor-voice","classic_cloak_deactivation","classic_maximum_armor");
                    default -> {}
                }
            });
        }
        step("n1-expenditure-feedback",8,()->{
            savedFlash=flashStart();NanosuitPowerFeedback.triggerForce();NanosuitPowerFeedback.setSustainedForce(true);
            check("n1-power-no-new-flash",flashStart()==savedFlash);check("n1-force-hud-kept",NanosuitPowerFeedback.isForceActive());
            NanosuitPowerFeedback.setSustainedForce(false);
        },()->capture("n1-power-no-flash"));
        step("same-open",8,this::hold,()->{});
        step("same-hover",8,()->{point(-135,0);savedChanges=serverChanges;savedFlash=flashStart();modeSounds.clear();},this::release);
        step("same-check",8,()->{},()->{check("same-mode-no-change",savedChanges==serverChanges);check("same-mode-no-flash",savedFlash==flashStart());expectedSounds("same-mode-no-audio");});
        step("escape-open",8,this::hold,()->point(135,0));
        step("escape-cancel",8,()->{robot.keyPress(KeyEvent.VK_ESCAPE);robot.keyRelease(KeyEvent.VK_ESCAPE);},this::release);
        step("escape-check",8,()->{},()->{mode("escape-keeps-mode",NanosuitMode.ARMOR);check("escape-closes",mc().screen==null);});
        step("equipment-open",8,this::hold,()->point(135,0));
        step("equipment-remove",12,()->commands.add(p->p.setItemSlot(EquipmentSlot.HEAD,ItemStack.EMPTY)),this::release);
        step("equipment-check",8,()->{},()->check("equipment-removal-cancels",mc().screen==null));
        step("repair-n1",12,()->commands.add(p->{equip(p,false);NanosuitSyncManager.syncToClient(p);}),()->{});
        step("n1-before-generation-swap",8,this::hold,()->point(135,0));
        step("n2-equip",15,()->commands.add(p->{equip(p,true); NanosuitSyncManager.syncToClient(p);}),()->{check("n2-swap-cancels-open-menu",mc().screen==null);release();});
        step("n2-open-refused",10,this::hold,()->{check("n2-radial-blocked",!(mc().screen instanceof Nanosuit1RadialScreen));release();});
        step("n2-force-feedback",10,()->{NanosuitPowerFeedback.triggerForce();check("n2-force-flash-retained",flashStart()!=0&&!(boolean)field(NanosuitPowerFeedback.class,"classicModeFlash",null));},()->{});
        n2ShortcutSteps();
        step("restore-n1",15,()->commands.add(p->{equip(p,false);NanosuitCapabilities.get(p).setMode(NanosuitMode.ARMOR);NanosuitSyncManager.syncToClient(p);}),()->{});
        step("other-screen-open",8,this::hold,()->point(135,0));
        step("other-screen-replace",8,()->mc().setScreen(new PauseScreen(false)),this::release);
        step("other-screen-check",8,()->{},()->{check("other-screen-no-commit",serverMode==NanosuitMode.ARMOR&&!(mc().screen instanceof Nanosuit1RadialScreen));mc().setScreen(null);});
        step("low-energy-prepare",8,()->commands.add(p->{refill=false;var d=NanosuitCapabilities.get(p);d.setEnergyWithoutStartingRecovery(5);d.setRegenDelayTicks(200);NanosuitSyncManager.syncToClient(p);}),()->{});
        step("low-energy-open",8,()->{hold();savedFlash=flashStart();modeSounds.clear();},()->point(0,135));
        step("low-energy-release",8,()->{},this::release);
        step("low-energy-check",8,()->{},()->{mode("low-energy-radial-refused-no-optimistic-state",NanosuitMode.ARMOR);check("refused-mode-no-flash",savedFlash==flashStart());expectedSounds("refused-mode-no-audio");commands.add(p->{refill=true;NanosuitCapabilities.get(p).setEnergyWithoutStartingRecovery(100);});});
        step("server-constraints",15,()->commands.add(p->{
            var d=NanosuitCapabilities.get(p);
            d.setEnergyWithoutStartingRecovery(5);
            check("server-refuses-low-energy-cloak",!NanosuitPlayerHandler.tryToggleMode(p,NanosuitMode.CLOAK));
            d.setEnergyWithoutStartingRecovery(100);
            check("server-accepts-strength",NanosuitPlayerHandler.tryToggleMode(p,NanosuitMode.STRENGTH));
            check("server-refuses-same-tick-cooldown",!NanosuitPlayerHandler.tryToggleMode(p,NanosuitMode.SPEED));
            p.setItemSlot(EquipmentSlot.HEAD,ItemStack.EMPTY);
            check("server-refuses-incomplete",!NanosuitPlayerHandler.tryToggleMode(p,NanosuitMode.ARMOR));
            equip(p,false);
        }),()->{});
        step("spin-open",8,this::hold,()->{point(135,0);spinStart=System.nanoTime();capture("rotation-a");});
        step("spin-later",50,()->{},()->{capture("rotation-b");System.out.println("RADIAL_QA_ROTATION elapsed="+((System.nanoTime()-spinStart)/1e9));});
        step("flash-captures-check",20,()->{},()->{check("flash-entry-and-end-captured",capturedFlashStart&&capturedFlashEnd&&capturedSpeedFlash);release();});
    }
    private void n2ShortcutSteps() {
        step("n2-existing-key",8,()->{
            expectedN2Shortcut=serverMode==NanosuitMode.ARMOR?NanosuitMode.NONE:NanosuitMode.ARMOR;
            robot.keyPress(armorKey);robot.keyRelease(armorKey);
        },()->{});
        step("n2-existing-check",8,()->{},()->mode("n2-armor-shortcut-"+expectedN2Shortcut,expectedN2Shortcut));
    }
    private int awt(KeyMapping mapping) {
        int code=mapping.getKey().getValue();
        String name=GLFW.glfwGetKeyName(code,0);
        if(name!=null)return KeyEvent.getExtendedKeyCodeForChar(Character.toUpperCase(name.charAt(0)));
        return switch(code) {
            case GLFW.GLFW_KEY_LEFT_SHIFT,GLFW.GLFW_KEY_RIGHT_SHIFT -> KeyEvent.VK_SHIFT;
            case GLFW.GLFW_KEY_LEFT_CONTROL,GLFW.GLFW_KEY_RIGHT_CONTROL -> KeyEvent.VK_CONTROL;
            case GLFW.GLFW_KEY_SPACE -> KeyEvent.VK_SPACE;
            default -> throw new IllegalArgumentException("Unsupported QA binding "+mapping.getKey());
        };
    }
    private void buildFeedbackSteps() {
        var o=mc().options;
        forwardKey=awt(o.keyUp);strafeKey=awt(o.keyRight);sneakKey=awt(o.keyShift);sprintKey=awt(o.keySprint);jumpKey=awt(o.keyJump);
        step("gui-3",15,()->{o.guiScale().set(3);mc().resizeDisplay();},()->{});
        step("neutral-3",12,this::hold,()->{
            if(radial()==null) {
                check("menu-open-required",false);System.out.println("RADIAL_QA_INPUT_ABORT raw="+GLFW.glfwGetKey(mc().getWindow().getWindow(),GLFW.GLFW_KEY_F7)+" focus="+mc().isWindowActive());
                release();robot.close();finished=true;mc().stop();return;
            }
            capture("feedback-neutral");
        });
        step("strength-hover-3",20,()->point(135,0),()->capture("feedback-strength-after-flash"));
        step("speed-hover-3",20,()->point(0,-135),()->{check("speed-selected-radius",Math.abs(offset(2)-16)<.001);capture("feedback-speed-after-flash");});
        step("armor-hover-feedback",16,()->point(-135,0),()->{check("armor-travel-matches-speed",Math.abs(offset(0)-16)<.001);capture("feedback-armor-after-flash");});
        step("cloak-hover-feedback",16,()->point(0,135),()->{check("cloak-travel-matches-speed",Math.abs(offset(3)-16)<.001);capture("feedback-cloak-after-flash");});
        var oldContexts=new java.util.IdentityHashMap<KeyMapping,net.neoforged.neoforge.client.settings.IKeyConflictContext>();
        for(var key:new KeyMapping[]{o.keyUp,o.keyDown,o.keyLeft,o.keyRight,o.keyJump,o.keySprint,o.keyShift})oldContexts.put(key,key.getKeyConflictContext());
        step("visual-cancel",8,()->mc().screen.onClose(),()->{release();check("movement-contexts-restored",oldContexts.entrySet().stream().allMatch(entry->entry.getKey().getKeyConflictContext()==entry.getValue()));});
        if(Boolean.getBoolean("radialqa.feedbackvisualonly")) {
            step("feedback-visual-finish",8,()->{},()->check("feedback-visual-captures",capturedFlashStart&&capturedFlashEnd&&capturedSpeedFlash));
            return;
        }
        step("walk-before-menu",10,()->robot.keyPress(forwardKey),()->movementStart=mc().player.position());
        step("walk-open-menu",12,this::hold,()->{check("walking-through-opening",mc().screen instanceof Nanosuit1RadialScreen&&mc().player.position().distanceTo(movementStart)>.2);movementStart=mc().player.position();yaw=mc().player.getYRot();pitch=mc().player.getXRot();});
        step("walk-select",12,()->point(135,0),()->{check("walk-while-selecting",mc().player.position().distanceTo(movementStart)>.2);check("camera-still-captured",mc().player.getYRot()==yaw&&mc().player.getXRot()==pitch);robot.keyRelease(forwardKey);});
        step("walk-stop",8,()->{},()->{check("release-movement-key",mc().player.input.forwardImpulse==0&&!o.keyUp.isDown());movementStart=mc().player.position();});
        step("strafe-in-menu",10,()->robot.keyPress(strafeKey),()->{check("strafe-in-menu",mc().player.position().distanceTo(movementStart)>.2);robot.keyRelease(strafeKey);});
        step("crouch-in-menu",8,()->robot.keyPress(sneakKey),()->{check("crouch-in-menu",mc().player.input.shiftKeyDown);robot.keyRelease(sneakKey);});
        step("sprint-in-menu",10,()->{robot.keyPress(forwardKey);robot.keyPress(sprintKey);},()->{check("sprint-in-menu",mc().player.isSprinting());robot.keyRelease(sprintKey);robot.keyRelease(forwardKey);});
        step("settle",8,()->{},()->{});
        step("jump-in-menu",8,()->{jumpObserved=false;robot.keyPress(jumpKey);},()->{check("jump-in-menu",jumpObserved);robot.keyRelease(jumpKey);});
        step("block-attacks",8,()->robot.mousePress(InputEvent.BUTTON1_DOWN_MASK),()->{check("attack-blocked",!o.keyAttack.isDown()&&!mc().player.swinging);robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);});
        step("block-use",8,()->robot.mousePress(InputEvent.BUTTON3_DOWN_MASK),()->{check("use-blocked",!o.keyUse.isDown()&&!mc().player.isUsingItem());robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);});
        step("toggle-crouch-on",6,()->{o.toggleCrouch().set(true);robot.keyPress(sneakKey);},()->robot.keyRelease(sneakKey));
        step("toggle-crouch-kept",8,()->{},()->check("toggle-crouch-not-repeated",o.keyShift.isDown()&&mc().player.input.shiftKeyDown));
        step("toggle-crouch-off",6,()->robot.keyPress(sneakKey),()->robot.keyRelease(sneakKey));
        step("toggle-crouch-finished",6,()->{},()->{check("toggle-crouch-off",!o.keyShift.isDown());o.toggleCrouch().set(false);});
        step("commit-STRENGTH",16,()->{robot.keyPress(forwardKey);movementStart=mc().player.position();release();},()->{
            check("walking-through-close",mc().screen==null&&mc().player.position().distanceTo(movementStart)>.2);
            robot.keyRelease(forwardKey);mode("mode-while-moving",NanosuitMode.STRENGTH);capture("strength-flash-finished");
        });
        for(NanosuitMode mode:new NanosuitMode[]{NanosuitMode.SPEED,NanosuitMode.ARMOR}) {
            step("open-"+mode,8,this::hold,()->{});
            step("choose-"+mode,10,()->point(mode==NanosuitMode.ARMOR?-135:0,mode==NanosuitMode.SPEED?-135:0),this::release);
            step("commit-"+mode,16,()->{},()->mode("confirmed-"+mode,mode));
        }
        step("n2-equip",15,()->commands.add(p->{equip(p,true);NanosuitSyncManager.syncToClient(p);}),()->{});
        step("n2-force-feedback",14,()->NanosuitPowerFeedback.triggerForce(),()->check("n2-remains-blue",(int)field(NanosuitPowerFeedback.class,"flashColor",null)==0x73BAFF));
        step("check-feedback-captures",8,()->{},()->check("feedback-captures",capturedFlashStart&&capturedFlashEnd&&capturedSpeedFlash&&samples.contains("middle-speed")&&samples.contains("late-speed")));
    }
    private void client(ClientTickEvent.Post e) {
        if (finished) return;
        if (++timeout>3200) { System.out.println("RADIAL_QA_TIMEOUT");release();robot.close();finished=true;mc().stop();return; }
        if (mc().player==null || !serverReady) return;
        if (!initialized) {
            if(Boolean.getBoolean("radialqa.feedbackvisualonly")&&mc().screen instanceof PauseScreen)mc().setScreen(null);
            if (mc().screen!=null || NanosuitArmorUtil.getEquippedProfile(mc().player)!=NanosuitProfile.NANOSUIT_1) return;
            if(!Boolean.getBoolean("radialqa.feedbackvisualonly")) {
                GLFW.glfwFocusWindow(mc().getWindow().getWindow());
                if (!mc().isWindowActive()) return;
            }
            initialized=true; buildSteps();
        }
        if (!mc().isWindowActive()&&!Boolean.getBoolean("radialqa.feedbackvisualonly")) {
            release();
            GLFW.glfwFocusWindow(mc().getWindow().getWindow());
            return;
        }
        if (index==steps.size()) {release();robot.close();finished=true;System.out.println("RADIAL_QA_COMPLETE steps="+index+" pass="+passes.get()+" fail="+failures.get());mc().stop();return;}
        Step step=steps.get(index);
        if(step.name().equals("jump-in-menu")&&mc().player.getDeltaMovement().y>.1)jumpObserved=true;
        if (elapsed==0) {stepStartedAt=System.nanoTime();System.out.println("RADIAL_QA_STEP "+step.name());step.start().run();}
        if (++elapsed>=step.ticks() && System.nanoTime()-stepStartedAt>=step.ticks()*50_000_000L) {step.end().run();index++;elapsed=0;}
    }

    /** Render-only fixture: keep the real production screen open without OS keys.
     * Its drawing, hover and animation methods are unchanged; only the hold gate
     * is replaced. Actual movement/hold/release is tested by feedbackonly without this flag.
     */
    private final class VisualRadial extends net.minecraft.client.gui.screens.Screen {
        final Nanosuit1RadialScreen delegate=new Nanosuit1RadialScreen(InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_F7));
        double mouseX,mouseY;
        VisualRadial(){super(net.minecraft.network.chat.Component.literal("Radial visual QA"));}
        @Override protected void init(){delegate.init(mc(),width,height);mouseX=mc().getWindow().getScreenWidth()/2.0;mouseY=mc().getWindow().getScreenHeight()/2.0;}
        @Override public boolean isPauseScreen(){return false;}
        @Override public void render(net.minecraft.client.gui.GuiGraphics g,int x,int y,float partial){
            try {
                var xf=mc().mouseHandler.getClass().getDeclaredField("xpos");var yf=mc().mouseHandler.getClass().getDeclaredField("ypos");xf.setAccessible(true);yf.setAccessible(true);
                double oldX=xf.getDouble(mc().mouseHandler),oldY=yf.getDouble(mc().mouseHandler);
                try {xf.setDouble(mc().mouseHandler,mouseX);yf.setDouble(mc().mouseHandler,mouseY);delegate.render(g,x,y,partial);}
                finally {xf.setDouble(mc().mouseHandler,oldX);yf.setDouble(mc().mouseHandler,oldY);}
            } catch(ReflectiveOperationException ex){throw new RuntimeException(ex);}
        }
        @Override public void onClose(){delegate.onClose();}
        @Override public void removed(){delegate.removed();}
    }

    // Minecraft forces AWT headless in Main. Keep OS input in a plain JRE child.
    private static final class InputClient {
        private final Process process;
        private final PrintWriter commands;
        InputClient() throws Exception {
            process = new ProcessBuilder(Path.of(System.getProperty("java.home"),"bin/javaw.exe").toString(),
                "-Djava.awt.headless=false", "-cp", Path.of("mods/mode-overlay-qa.jar").toAbsolutePath().toString(),
                "local.crysisqa.RadialMenuQa$InputBridge").redirectError(Path.of("input-bridge-error.log").toFile()).start();
            String ready=new BufferedReader(new InputStreamReader(process.getInputStream())).readLine();
            if (!"READY".equals(ready)) throw new IllegalStateException("Input bridge failed: "+ready);
            commands=new PrintWriter(process.getOutputStream(),true);
        }
        void keyPress(int key) { commands.println("press "+key); }
        void keyRelease(int key) { commands.println("release "+key); }
        void mousePress(int mask) {commands.println("mousepress "+mask);}
        void mouseRelease(int mask) {commands.println("mouserelease "+mask);}
        void close() {commands.println("quit 0");commands.close();}
    }
    public static final class InputBridge {
        public static void main(String[] args) throws Exception {
            var robot=new Robot();
            var scanner=new Scanner(System.in);
            System.out.println("READY");System.out.flush();
            Set<Integer> heldKeys=new HashSet<>(),heldMouse=new HashSet<>();
            try {
                while(scanner.hasNext()) {
                    String command=scanner.next();int key=scanner.nextInt();
                    if(command.equals("quit"))return;
                    if(command.equals("press")){robot.keyPress(key);heldKeys.add(key);}
                    else if(command.equals("release")){robot.keyRelease(key);heldKeys.remove(key);}
                    else if(command.equals("mousepress")){robot.mousePress(key);heldMouse.add(key);}
                    else if(command.equals("mouserelease")){robot.mouseRelease(key);heldMouse.remove(key);}
                }
            } finally {for(int key:heldKeys)robot.keyRelease(key);for(int mask:heldMouse)robot.mouseRelease(mask);}
        }
    }
}
