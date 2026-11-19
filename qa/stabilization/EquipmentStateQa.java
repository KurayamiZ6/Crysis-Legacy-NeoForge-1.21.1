package local.crysisqa;

import io.github.painworks.crysismod.registry.ModItems;
import io.github.painworks.crysismod.nanosuit.*;
import io.github.painworks.crysismod.nanosuit.energy.*;
import io.github.painworks.crysismod.nanosuit.handler.NanosuitPlayerHandler;
import io.github.painworks.crysismod.nanosuit.network.NanosuitSyncManager;
import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import io.github.painworks.crysismod.nanovisor.client.*;
import io.github.painworks.crysismod.nanovisor.server.NanoVisorServerState;
import io.github.painworks.crysismod.nanovision.server.*;
import io.github.painworks.crysismod.client.nanovision.*;
import net.minecraft.client.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.client.event.*;

/** Uses real handlers in a copied integrated world; observations also run against the unfixed JAR. */
@Mod("crysisstabilityqa")
public final class EquipmentStateQa {
 private static final EquipmentSlot[] SLOTS={EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET};
 private final String[] names={"none-key","helmet-key","n1-open","removed-chest","incomplete-key-again","n1-reopen","n1-to-n2","n2-open","n2-to-n1","n1-before-dimension","dimension","screen","n1-before-mixed","mixed","night-open","night-removed","thermal-open","thermal-removed","radial-invalid","closing-start","closing-removed","n2-visor-before-screen","n2-screen","n2-screen-removal"};
 private volatile int phase,serverPhase=-1,settled;private int timeout,serverFailures,clientFailures;private boolean matrix,action,done;private long start;
 public EquipmentStateQa(){if(Boolean.getBoolean("crysis.stability.n2screen"))phase=21;NeoForge.EVENT_BUS.addListener(this::server);NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,this::client);NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,this::render);}
 private void equip(ServerPlayer p,int...types){for(int i=0;i<4;i++){Item item=switch(i){case 0->types[i]==1?ModItems.NANOGEO_NANOSUIT_HELMET.get():ModItems.NANOSUIT_2_HELMET.get();case 1->types[i]==1?ModItems.NANOGEO_NANOSUIT_CHESTPLATE.get():ModItems.NANOSUIT_2_CHESTPLATE.get();case 2->types[i]==1?ModItems.NANOGEO_NANOSUIT_LEGGINGS.get():ModItems.NANOSUIT_2_LEGGINGS.get();default->types[i]==1?ModItems.NANOGEO_NANOSUIT_BOOTS.get():ModItems.NANOSUIT_2_BOOTS.get();};p.setItemSlot(SLOTS[i],types[i]==0?ItemStack.EMPTY:new ItemStack(item));}}
 private void check(boolean pass,String name){if(!pass){serverFailures++;System.out.println("STABILITY_SERVER_FAIL "+name);}}
 private void matrix(ServerPlayer p){
  var d=NanosuitCapabilities.get(p);int invalid=0,valid=0;int thermalLeaks=0,nightLeaks=0;
  for(int code=0;code<81;code++){
   int n=code;int[] types=new int[4];for(int i=0;i<4;i++){types[i]=n%3;n/=3;}equip(p,types);
   boolean full=code==40||code==80;d.setRecoveryTicks(-1);d.setEnergyWithoutStartingRecovery(100);d.setMode(NanosuitMode.NONE);d.setSprintBoostReady(false);
   check(NanosuitArmorUtil.hasFullSet(p)==full,"identity code="+code);
   if(full){valid++;d.setMode(code==40?NanosuitMode.STRENGTH:NanosuitMode.NONE);p.setOnGround(true);
    check(NanosuitPlayerHandler.availablePowerJump(p)!=null,"valid jump code="+code);
    if(code==40)check(io.github.painworks.crysismod.nanovision.ClassicNightVision.canUse(p),"valid N1 night");
    else{NanovisionServerState.setActiveFromClient(p,true);check(NanovisionServerState.isActive(p),"valid N2 thermal");NanoVisorServerState.setActiveFromClient(p,true);check(NanoVisorServerState.isActive(p),"valid N2 visor");}
    continue;}invalid++;
   check(!NanosuitPlayerHandler.tryToggleMode(p,NanosuitMode.CLOAK),"mode code="+code);
   check(!NanosuitPlayerHandler.canSpendSuitEnergy(p,10),"spend code="+code);
   p.setOnGround(true);d.setMode(NanosuitMode.STRENGTH);
   check(NanosuitPlayerHandler.availablePowerJump(p)==null,"jump eligibility code="+code);
   NanosuitPlayerHandler.beginEnhancedJumpCharge(p);NanosuitPlayerHandler.requestChargedEnhancedJump(p);NanosuitPlayerHandler.requestInstantClassicJump(p);
   check(d.getEnergy()==100,"jump energy code="+code);
   check(!io.github.painworks.crysismod.nanosuit.handler.NanosuitPowerMelee.beginClassicPunch(p),"melee code="+code);
   p.setSprinting(true);NanosuitPlayerHandler.requestPowerSprint(p);check(!d.isSprintBoostReady(),"sprint code="+code);p.setSprinting(false);
   NanoVisorServerState.setActiveFromClient(p,true);check(!NanoVisorServerState.isActive(p),"visor code="+code);
   NanovisionServerState.setActiveFromClient(p,true);if(NanovisionServerState.isActive(p))thermalLeaks++;
   if(io.github.painworks.crysismod.nanovision.ClassicNightVision.canUse(p))nightLeaks++;
   NanovisionServerState.clear(p.getUUID());NanoVisorServerState.clear(p.getUUID());
  }
  check(thermalLeaks==0,"thermal partial/mixed accepted="+thermalLeaks);check(nightLeaks==0,"night partial/mixed accepted="+nightLeaks);
  System.out.println("STABILITY_MATRIX combinations=81 valid="+valid+" invalid="+invalid+" thermalLeaks="+thermalLeaks+" nightLeaks="+nightLeaks+" failures="+serverFailures);
  equip(p,2,2,2,2);d.setRecoveryTicks(12);d.setEnergyWithoutStartingRecovery(0);equip(p,2,0,2,2);
  check(!NanosuitPlayerHandler.tryToggleMode(p,NanosuitMode.ARMOR)&&d.isRecoveryLocked(),"N2 outage survives unequip request");
  equip(p,2,2,2,2);check(d.getRecoveryTicks()==12,"N2 outage survives re-equip");d.setRecoveryTicks(-1);d.setEnergyWithoutStartingRecovery(100);
  try{var mapField=NanosuitPlayerHandler.class.getDeclaredField("PLAYER_RUNTIME_STATE");mapField.setAccessible(true);Object runtime=((java.util.Map<?,?>)mapField.get(null)).get(p.getUUID());var cd=runtime.getClass().getDeclaredField("modeCooldownUntilTick");cd.setAccessible(true);int before=cd.getInt(runtime);int future=p.tickCount+40;cd.setInt(runtime,future);
   equip(p,2,0,2,2);check(!NanosuitPlayerHandler.tryToggleMode(p,NanosuitMode.CLOAK),"cooldown partial rejected");equip(p,2,2,2,2);check(!NanosuitPlayerHandler.tryToggleMode(p,NanosuitMode.CLOAK)&&cd.getInt(runtime)==future,"cooldown preserved across equipment");cd.setInt(runtime,before);
  }catch(Exception ex){throw new RuntimeException(ex);}
 }
 private void server(ServerTickEvent.Post e){var ps=e.getServer().getPlayerList().getPlayers();if(ps.isEmpty()||phase>=names.length)return;var p=ps.getFirst();if(!matrix){matrix=true;if(!Boolean.getBoolean("crysis.stability.n2screen"))matrix(p);}
  if(serverPhase!=phase){serverPhase=phase;settled=0;
   switch(phase){case 0->equip(p,0,0,0,0);case 1->equip(p,1,0,0,0);case 3,4,15,18,20->equip(p,1,0,1,1);case 6,7,16,21,22->equip(p,2,2,2,2);case 23->equip(p,2,0,2,2);case 13,17->equip(p,1,2,1,2);default->equip(p,1,1,1,1);}
   if(phase==14)ClassicNightVisionServerState.toggle(p);
   if(phase==10)p.teleportTo(e.getServer().getLevel(Level.NETHER),.5,150,.5,0,0);else p.connection.teleport(.5,150,.5,0,0);
   p.setNoGravity(true);p.getInventory().setItem(p.getInventory().selected,ItemStack.EMPTY);
  }var d=NanosuitCapabilities.get(p);d.setEnergyWithoutStartingRecovery(100);d.setMode(NanosuitMode.ARMOR);NanosuitSyncManager.syncToClient(p);p.serverLevel().setDayTime(6000);settled++;
 }
 private void client(ClientTickEvent.Post e){var mc=Minecraft.getInstance();if(++timeout>2600){System.out.println("STABILITY_TIMEOUT "+phase);mc.stop();}if(phase>=names.length||mc.player==null||serverPhase!=phase||settled<(phase==0?60:Boolean.getBoolean("crysis.stability.edges")?1:12))return;
  var expected=(phase==0||phase==1||phase==3||phase==4||phase==13||phase==15||phase==17||phase==18||phase==20||phase==23)?null:(phase==6||phase==7||phase==16||phase==21||phase==22)?NanosuitProfile.NANOSUIT_2:NanosuitProfile.NANOSUIT_1;
  if(NanosuitArmorUtil.getEquippedProfile(mc.player)!=expected)return;
  mc.player.setNoGravity(true);mc.player.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);mc.player.setYRot(0);mc.player.setXRot(0);
  for(var key:new KeyMapping[]{mc.options.keyAttack,mc.options.keyUse,mc.options.keyUp,mc.options.keyDown,mc.options.keyLeft,mc.options.keyRight,mc.options.keyJump})key.setDown(false);
  if(!action){action=true;start=System.nanoTime();
   if(phase==0||phase==1||phase==4){NanoVisorState.setVisorActive(false);KeyMapping.click(NanoVisorKeyMappings.TOGGLE_NANO_VISOR.getKey());}
   if(phase==2||phase==5||phase==7||phase==9||phase==12||phase==19||phase==21)KeyMapping.click(NanoVisorKeyMappings.TOGGLE_NANO_VISOR.getKey());
   if(phase==11||phase==22)mc.setScreen(new net.minecraft.client.gui.screens.inventory.InventoryScreen(mc.player));
   if(phase==12)mc.setScreen(null);
   if(phase==16)KeyMapping.click(NanovisionKeybinds.TOGGLE_NANOVISION.getKey());
   if(phase==18){var original=mc.options.keyUp.getKeyConflictContext();mc.setScreen(new io.github.painworks.crysismod.nanosuit.client.radial.Nanosuit1RadialScreen(NanoVisorKeyMappings.TOGGLE_NANO_VISOR.getKey()));io.github.painworks.crysismod.nanosuit.client.NanosuitClientContext.tick(new ClientTickEvent.Pre());
    boolean closed=mc.screen==null&&mc.options.keyUp.getKeyConflictContext()==original&&(!mc.isWindowActive()||mc.mouseHandler.isMouseGrabbed());if(!closed)clientFailures++;System.out.println("STABILITY_RADIAL_INVALID closed="+closed);
   }
  }
 }
 private void render(RenderGuiEvent.Post e){var mc=Minecraft.getInstance();if(done||mc.player==null)return;if(phase>=names.length){done=true;System.out.println("STABILITY_COMPLETE serverFailures="+serverFailures+" clientFailures="+clientFailures);mc.stop();return;}if(!action||System.nanoTime()-start<(phase==14?1_200_000_000L:Boolean.getBoolean("crysis.stability.edges")&&phase!=19&&phase!=16?80_000_000L:600_000_000L))return;
  boolean expectedActive=phase==2||phase==5||phase==7||phase==9||phase==12||phase==19||phase==21||phase==22;
  boolean active=NanoVisorState.isVisorActive();if(active!=expectedActive)clientFailures++;
  if(phase==14&&!ClassicNightVisionClient.isActive()||phase==15&&(ClassicNightVisionClient.isActive()||ClassicNightVisionClient.visualIntensity()!=0)||phase==16&&!NanovisionController.isActive()||phase==17&&NanovisionController.isActive())clientFailures++;
  double fov=0;try{var method=net.minecraft.client.renderer.GameRenderer.class.getDeclaredMethod("getFov",net.minecraft.client.Camera.class,float.class,boolean.class);method.setAccessible(true);fov=((Number)method.invoke(mc.gameRenderer,mc.gameRenderer.getMainCamera(),1f,true)).doubleValue();}catch(Exception ex){throw new RuntimeException(ex);}
  System.out.println("STABILITY_CLIENT "+names[phase]+" expected="+expectedActive+" active="+active+" classicRunning="+ClassicBinoculars.running()+" animation="+ClassicBinoculars.animationState()+" progress="+ClassicBinoculars.progress()+" zoom="+ClassicBinoculars.zoom()+" legacyFov="+NanoVisorZoomHandler.getCurrentZoomMultiplier()+" actualFov="+fov+" profile="+NanosuitArmorUtil.getEquippedProfile(mc.player)+" night="+ClassicNightVisionClient.isActive()+" intensity="+ClassicNightVisionClient.visualIntensity()+" thermal="+NanovisionController.isActive());
  if(phase==0||phase==3||phase==4||phase==6||phase==10||phase==13||phase>=14&&phase<=17){e.getGuiGraphics().flush();Screenshot.grab(mc.gameDirectory,names[phase]+".png",mc.getMainRenderTarget(),ignored->{});}
  if(phase==19)KeyMapping.click(NanoVisorKeyMappings.TOGGLE_NANO_VISOR.getKey());
  phase++;action=false;
 }
}
