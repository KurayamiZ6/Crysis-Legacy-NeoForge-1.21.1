package local.crysisqa;

import io.github.painworks.crysismod.registry.ModItems;
import io.github.painworks.crysismod.nanosuit.*;
import io.github.painworks.crysismod.nanosuit.client.*;
import io.github.painworks.crysismod.nanosuit.config.NanosuitClientConfig;
import io.github.painworks.crysismod.nanosuit.energy.NanosuitCapabilities;
import io.github.painworks.crysismod.nanosuit.network.NanosuitSyncManager;
import io.github.painworks.crysismod.nanovisor.client.*;
import io.github.painworks.crysismod.nanovisor.config.NanoVisorSettings;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.client.event.*;

@Mod("crysiszoomthemeqa")
public final class ZoomThemeQa {
 private volatile int phase,settled;private int serverPhase=-1,failures,slot;private boolean action,complete,localeStarted;private volatile boolean localeReady;private long started;
 private boolean scrollCanceled;private java.lang.reflect.Method mouseScroll;
 public ZoomThemeQa(){
  NeoForge.EVENT_BUS.addListener(this::server);
  NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,(RenderGuiEvent.Post e)->{if(Minecraft.getInstance().screen==null)step(e.getGuiGraphics());});
  NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,(ScreenEvent.Render.Post e)->step(e.getGuiGraphics()));
  NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,true,InputEvent.MouseScrollingEvent.class,e->scrollCanceled=e.isCanceled());
 }
 private void server(ServerTickEvent.Post e){var ps=e.getServer().getPlayerList().getPlayers();if(ps.isEmpty()||complete)return;var p=ps.getFirst();
  if(serverPhase!=phase){serverPhase=phase;settled=0;boolean n2=phase<6;
   p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);p.setNoGravity(true);p.connection.teleport(.5,190,.5,0,0);
   p.setItemSlot(EquipmentSlot.HEAD,new ItemStack(n2?ModItems.NANOSUIT_2_HELMET.get():ModItems.NANOGEO_NANOSUIT_HELMET.get()));p.setItemSlot(EquipmentSlot.CHEST,new ItemStack(n2?ModItems.NANOSUIT_2_CHESTPLATE.get():ModItems.NANOGEO_NANOSUIT_CHESTPLATE.get()));p.setItemSlot(EquipmentSlot.LEGS,new ItemStack(n2?ModItems.NANOSUIT_2_LEGGINGS.get():ModItems.NANOGEO_NANOSUIT_LEGGINGS.get()));p.setItemSlot(EquipmentSlot.FEET,new ItemStack(n2?ModItems.NANOSUIT_2_BOOTS.get():ModItems.NANOGEO_NANOSUIT_BOOTS.get()));
   var d=NanosuitCapabilities.get(p);d.setRecoveryTicks(-1);d.setEnergyWithoutStartingRecovery(100);d.setMode(NanosuitMode.ARMOR);NanosuitSyncManager.syncToClient(p);
  }settled++;
 }
 private void press(String caption){var screen=Minecraft.getInstance().screen;var button=screen.children().stream().filter(c->c instanceof Button).map(c->(Button)c).filter(b->b.getMessage().getString().equals(caption)).findFirst().orElseThrow(()->new IllegalStateException("Missing button "+caption));if(!button.active)throw new IllegalStateException("Disabled "+caption);button.onPress();}
 private EditBox edit(String key){return Minecraft.getInstance().screen.children().stream().filter(c->c instanceof EditBox).map(c->(EditBox)c).filter(b->b.getMessage().getString().equals(ClassicThemeScreen.text(key).getString())).findFirst().orElseThrow();}
 private void scroll(double delta){try{var mc=Minecraft.getInstance();if(mouseScroll==null){mouseScroll=MouseHandler.class.getDeclaredMethod("onScroll",long.class,double.class,double.class);mouseScroll.setAccessible(true);}scrollCanceled=false;mouseScroll.invoke(mc.mouseHandler,mc.getWindow().getWindow(),0d,delta);}catch(Exception ex){throw new RuntimeException(ex);}}
 private void step(GuiGraphics g){var mc=Minecraft.getInstance();if(complete||mc.player==null)return;
  if(!localeStarted){localeStarted=true;mc.getLanguageManager().setSelected("fr_fr");mc.reloadResourcePacks().thenRun(()->localeReady=true);return;}
  // Native settings screens intentionally pause the integrated world. Subsequent editor steps are client-only.
  if(!localeReady||mc.getOverlay()!=null||phase<=6&&(serverPhase!=phase||settled<16))return;
  if(!action){action=true;started=System.nanoTime();
   switch(phase){
    case 0 -> {slot=mc.player.getInventory().selected;KeyMapping.click(NanoVisorKeyMappings.TOGGLE_NANO_VISOR.getKey());}
    case 1,2 -> scroll(1);
    case 3 -> scroll(-1);
    case 4 -> KeyMapping.click(NanoVisorKeyMappings.TOGGLE_NANO_VISOR.getKey());
    case 5 -> scroll(1);
    case 6 -> mc.setScreen(new CrysisSettingsScreen(null));
    case 7 -> {press(ClassicThemeScreen.text("title").getString());edit("hex").setValue("#3388CC");edit("name").setValue("Océan");press(ClassicThemeScreen.text("save").getString());}
    case 8 -> {press(ClassicThemeScreen.text("apply").getString());mc.setScreen(null);}
    case 9 -> {mc.setScreen(new ClassicThemeScreen(null));edit("hex").setValue("#CC5533");press(ClassicThemeScreen.text("load").getString());if(!edit("hex").getValue().equals("#3388CC"))failures++;press(ClassicThemeScreen.text("default").getString());press(ClassicThemeScreen.text("apply").getString());}
    case 10 -> {mc.setScreen(new ClassicThemeScreen(null));edit("hex").setValue("#GGGGGG");var buttons=mc.screen.children().stream().filter(c->c instanceof Button).map(c->(Button)c).toList();if(buttons.stream().filter(b->b.getMessage().getString().equals(ClassicThemeScreen.text("apply").getString())).anyMatch(b->b.active))failures++;press(net.minecraft.network.chat.Component.translatable("gui.cancel").getString());}
    case 11 -> {mc.options.guiScale().set(4);mc.setScreen(new ClassicThemeScreen(null));mc.resizeDisplay();}
    case 12 -> {press(ClassicThemeScreen.text("delete").getString());press(ClassicThemeScreen.text("apply").getString());}
   }return;
  }
  if(System.nanoTime()-started<1_000_000_000L)return;
  boolean pass=true;double actual=NanoVisorZoomHandler.getCurrentZoomMultiplier();
  double lo=Math.min(NanoVisorSettings.Zoom.minFovMultiplier(),NanoVisorSettings.Zoom.maxFovMultiplier()),hi=Math.max(NanoVisorSettings.Zoom.minFovMultiplier(),NanoVisorSettings.Zoom.maxFovMultiplier());
  switch(phase){
   case 0,3 -> pass=NanoVisorState.isVisorActive()&&Math.abs(actual-hi)<.025&&(phase==0||scrollCanceled)&&mc.player.getInventory().selected==slot;
   case 1,2 -> pass=NanoVisorState.isVisorActive()&&Math.abs(actual-lo)<.025&&scrollCanceled&&mc.player.getInventory().selected==slot;
   case 4 -> pass=!NanoVisorState.isVisorActive();
   case 5 -> pass=!scrollCanceled&&mc.player.getInventory().selected!=slot;
   case 6 -> pass=mc.screen instanceof CrysisSettingsScreen;
   case 7 -> pass=mc.screen instanceof ClassicThemeScreen&&NanosuitClientConfig.classicInterfaceColor()==ClassicHudPalette.DEFAULT;
   case 8 -> {try{var saved=java.nio.file.Files.readString(mc.gameDirectory.toPath().resolve("config/nanosuit-client.toml"));pass=NanosuitClientConfig.classicInterfaceColor()==0x3388cc&&NanosuitClientConfig.classicColorPresets().contains("Océan|#3388CC")&&saved.contains("#3388CC")&&saved.contains("Océan");}catch(Exception ex){throw new RuntimeException(ex);}}
   case 9,10 -> pass=NanosuitClientConfig.classicInterfaceColor()==ClassicHudPalette.DEFAULT&&NanosuitClientConfig.classicColorPresets().size()==1;
   case 11 -> pass=mc.screen instanceof ClassicThemeScreen&&mc.getWindow().getGuiScaledHeight()>=240;
   case 12 -> pass=NanosuitClientConfig.classicColorPresets().isEmpty()&&NanosuitClientConfig.classicInterfaceColor()==ClassicHudPalette.DEFAULT;
  }
  if(!pass)failures++;System.out.println("ZOOM_THEME phase="+phase+" pass="+pass+" zoom="+actual+" canceled="+scrollCanceled+" slot="+mc.player.getInventory().selected+" color="+Integer.toHexString(NanosuitClientConfig.classicInterfaceColor())+" presets="+NanosuitClientConfig.classicColorPresets());
  if(phase==1||phase==3||phase==6||phase==7||phase==8||phase==11){g.flush();Screenshot.grab(mc.gameDirectory,"zoom-theme-"+phase+".png",mc.getMainRenderTarget(),ignored->{});}
  if(++phase==13){complete=true;System.out.println("ZOOM_THEME_COMPLETE failures="+failures);mc.stop();}action=false;
 }
}
