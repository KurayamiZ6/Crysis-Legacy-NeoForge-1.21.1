package local.crysisqa;
import io.github.painworks.crysismod.client.minimap.ClassicWaypoint;
import io.github.painworks.crysismod.nanosuit.*;
import io.github.painworks.crysismod.nanosuit.client.*;
import io.github.painworks.crysismod.nanosuit.energy.NanosuitCapabilities;
import io.github.painworks.crysismod.nanosuit.network.NanosuitSyncManager;
import io.github.painworks.crysismod.nanovisor.client.*;
import io.github.painworks.crysismod.registry.ModItems;
import net.minecraft.client.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.client.event.*;
@Mod("crysisfinalqa")
public final class ClassicFinalVisualQa {
 private String[] names={"exploration","combat","cloak","nightvision","binoculars","speed","waypoint-rifle","theme-blue","theme-purple-gui3","vanilla-fallback","tacz-native","n2"};
 private volatile int phase,serverPhase=-1,settled;private int timeout;private long start;private boolean action,done,gunCanceled,heatCanceled;private Zombie zombie;
 public ClassicFinalVisualQa(){if(Boolean.getBoolean("crysis.final.qa.focus"))phase=5;if(Boolean.getBoolean("crysis.final.qa.fire"))phase=6;NeoForge.EVENT_BUS.addListener(this::server);NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,this::client);NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,this::render);
  NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,true,RenderGuiLayerEvent.Pre.class,e->{if(e.getName().toString().equals("tacz:tac_gun_hud_overlay"))gunCanceled=e.isCanceled();if(e.getName().toString().equals("tacz:tac_heat_bar"))heatCanceled=e.isCanceled();});}
 private static Object call(Class<?> c,String n,Object o,Class<?>[] t,Object...v)throws Exception{return c.getMethod(n,t).invoke(o,v);}
 private void gun(net.minecraft.server.level.ServerPlayer p,String id,int count)throws Exception{
  var builder=Class.forName("com.tacz.guns.api.item.builder.GunItemBuilder");Object b=call(builder,"create",null,new Class<?>[0]);call(builder,"setId",b,new Class<?>[]{ResourceLocation.class},ResourceLocation.parse(id));
  var stack=(ItemStack)call(builder,"build",b,new Class<?>[]{net.minecraft.core.HolderLookup.Provider.class},p.registryAccess());
  var api=Class.forName("com.tacz.guns.api.item.IGun");Object gun=call(api,"getIGunOrNull",null,new Class<?>[]{ItemStack.class},stack);
  call(api,"setCurrentAmmoCount",gun,new Class<?>[]{ItemStack.class,int.class},stack,count);call(api,"setBulletInBarrel",gun,new Class<?>[]{ItemStack.class,boolean.class},stack,true);
  p.getInventory().setItem(p.getInventory().selected,stack);
  Class<?> timeless=Class.forName("com.tacz.guns.api.TimelessAPI");var index=(java.util.Optional<?>)call(timeless,"getCommonGunIndex",null,new Class<?>[]{ResourceLocation.class},ResourceLocation.parse(id));Object data=call(index.get().getClass(),"getGunData",index.get(),new Class<?>[0]);var ammoId=(ResourceLocation)call(data.getClass(),"getAmmoId",data,new Class<?>[0]);
  var modes=(java.util.List<?>)call(data.getClass(),"getFireModeSet",data,new Class<?>[0]);
  call(api,"setFireMode",gun,new Class<?>[]{ItemStack.class,Class.forName("com.tacz.guns.api.item.gun.FireMode")},stack,modes.getFirst());
  var ab=Class.forName("com.tacz.guns.api.item.builder.AmmoItemBuilder");
  for(int slot=9;slot<11;slot++){Object a=call(ab,"create",null,new Class<?>[0]);call(ab,"setId",a,new Class<?>[]{ResourceLocation.class},ammoId);call(ab,"setCount",a,new Class<?>[]{int.class},slot==9?64:14);p.getInventory().setItem(slot,(ItemStack)call(ab,"build",a,new Class<?>[0]));}
 }
 private void server(ServerTickEvent.Post e){var ps=e.getServer().getPlayerList().getPlayers();if(ps.isEmpty()||phase>=names.length)return;var p=ps.getFirst();
  if(serverPhase!=phase){serverPhase=phase;settled=0;boolean n2=phase==11;
   p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
   p.setItemSlot(EquipmentSlot.HEAD,new ItemStack(n2?ModItems.NANOSUIT_2_HELMET.get():ModItems.NANOGEO_NANOSUIT_HELMET.get()));
   p.setItemSlot(EquipmentSlot.CHEST,new ItemStack(n2?ModItems.NANOSUIT_2_CHESTPLATE.get():ModItems.NANOGEO_NANOSUIT_CHESTPLATE.get()));
   p.setItemSlot(EquipmentSlot.LEGS,new ItemStack(n2?ModItems.NANOSUIT_2_LEGGINGS.get():ModItems.NANOGEO_NANOSUIT_LEGGINGS.get()));
   p.setItemSlot(EquipmentSlot.FEET,new ItemStack(n2?ModItems.NANOSUIT_2_BOOTS.get():ModItems.NANOGEO_NANOSUIT_BOOTS.get()));
   if(phase==0||phase==5&&Boolean.getBoolean("crysis.final.qa.focus")||phase==6&&Boolean.getBoolean("crysis.final.qa.fire")){for(int x=-4;x<=4;x++)for(int z=-2;z<=14;z++){p.serverLevel().setBlockAndUpdate(new BlockPos(x,149,z),Blocks.STONE.defaultBlockState());for(int y=150;y<155;y++)p.serverLevel().setBlockAndUpdate(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState());}}
   p.connection.teleport(.5,150,.5,0,0);p.setNoGravity(true);
   try{if(Boolean.getBoolean("crysis.final.qa.fire")){if(phase==6)gun(p,"tacz:hk_mp5a5",20);}
    else if(phase==4)p.getInventory().setItem(p.getInventory().selected,ItemStack.EMPTY);else if(phase==9)p.getInventory().setItem(p.getInventory().selected,new ItemStack(Items.CROSSBOW));else gun(p,phase>=6?"tacz:m4a1":"tacz:glock_17",phase==1?11:20);
    var state=Class.forName("io.github.painworks.crysismod.nanovision.server.ClassicNightVisionServerState");var f=state.getDeclaredField("BATTERIES");f.setAccessible(true);
    @SuppressWarnings("unchecked")var map=(java.util.Map<java.util.UUID,io.github.painworks.crysismod.nanovision.ClassicNightVisionBattery>)f.get(null);
    var battery=new io.github.painworks.crysismod.nanovision.ClassicNightVisionBattery();if(phase==3||phase==4)battery.toggle();map.put(p.getUUID(),battery);
    io.github.painworks.crysismod.nanosuit.network.NanosuitNetwork.sendToPlayer(p,new io.github.painworks.crysismod.nanovision.network.SyncClassicNightVisionPacket(battery.active(),battery.charge()));
   }catch(Exception ex){throw new RuntimeException(ex);}
   if(phase==1){zombie=new Zombie(p.serverLevel());zombie.setPos(.5,150,7.5);zombie.setNoAi(true);zombie.setNoGravity(true);p.serverLevel().addFreshEntity(zombie);zombie.setTarget(p);}
   if(phase==2&&zombie!=null)zombie.setTarget(null);
  }
  var data=NanosuitCapabilities.get(p);data.setEnergyWithoutStartingRecovery(phase==2?73:100);data.setMode(phase==2?NanosuitMode.CLOAK:phase==5?NanosuitMode.SPEED:NanosuitMode.ARMOR);NanosuitSyncManager.syncToClient(p);p.serverLevel().setDayTime(18000);settled++;
 }
 private void config(String key,Object value){try{var f=io.github.painworks.crysismod.nanosuit.config.NanosuitClientConfig.class.getDeclaredField(key);f.setAccessible(true);@SuppressWarnings("unchecked")var v=(net.neoforged.neoforge.common.ModConfigSpec.ConfigValue<Object>)f.get(null);v.set(value);}catch(Exception ex){throw new RuntimeException(ex);}}
 private void client(ClientTickEvent.Post e){var mc=Minecraft.getInstance();if(++timeout>3200){System.out.println("FINAL_TIMEOUT "+phase);mc.stop();}if(mc.player==null||phase>=names.length||serverPhase!=phase||settled<(phase==0?60:15))return;
  mc.player.setNoGravity(true);mc.player.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);mc.player.setYRot(0);mc.player.setXRot(0);
  mc.options.chatVisibility().set(net.minecraft.world.entity.player.ChatVisiblity.HIDDEN);
  try{var f=net.minecraft.client.gui.Gui.class.getDeclaredField("toolHighlightTimer");f.setAccessible(true);f.setInt(mc.gui,0);}catch(Exception ex){throw new RuntimeException(ex);}
  for(var key:new KeyMapping[]{mc.options.keyAttack,mc.options.keyUse,mc.options.keyUp,mc.options.keyDown,mc.options.keyLeft,mc.options.keyRight,mc.options.keyJump})key.setDown(false);
  if(!action){action=true;start=System.nanoTime();if(Boolean.getBoolean("crysis.final.qa.fire")){
    if(phase>6)try{Class<?> op=Class.forName("com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator");Object player=call(op,"fromLocalPlayer",null,new Class<?>[]{net.minecraft.client.player.LocalPlayer.class},mc.player);call(op,"fireSelect",player,new Class<?>[0]);}catch(Exception ex){throw new RuntimeException(ex);}
   }else{if(phase==4)KeyMapping.click(NanoVisorKeyMappings.TOGGLE_NANO_VISOR.getKey());if(phase==5)ClassicBinoculars.reset();if(phase==6)ClassicWaypoint.follow(30,30);if(phase==7)config("CLASSIC_INTERFACE_COLOR","#64B5F6");if(phase==8){config("CLASSIC_INTERFACE_COLOR","#B784E8");mc.options.guiScale().set(3);mc.resizeDisplay();}if(phase==10)config("TACZ_HIDE_ORIGINAL_HUD",false);if(phase==11)config("TACZ_HIDE_ORIGINAL_HUD",true);}}
 }
 private void render(RenderGuiEvent.Post e){var mc=Minecraft.getInstance();if(done||mc.player==null)return;if(phase>=names.length||phase==9&&(Boolean.getBoolean("crysis.final.qa.focus")||Boolean.getBoolean("crysis.final.qa.fire"))){done=true;System.out.println("FINAL_QA_COMPLETE");mc.stop();return;}if(!action||(System.nanoTime()-start)<(phase==6&&!Boolean.getBoolean("crysis.final.qa.fire")?300_000_000L:1_200_000_000L))return;
  e.getGuiGraphics().flush();Screenshot.grab(mc.gameDirectory,names[phase]+".png",mc.getMainRenderTarget(),ignored->{});
  var s=ClassicTaczData.read(mc.player);System.out.println("FINAL_CAPTURE "+names[phase]+" state="+s+" gunCanceled="+gunCanceled+" heatCanceled="+heatCanceled+" optic="+ClassicBinoculars.progress()+" goal="+ClassicWaypoint.current());
  if(Boolean.getBoolean("crysis.final.qa.fire")&&(s==null||!s.fireMode().equals(new String[]{"AUTO","BURST","SEMI"}[phase-6])))throw new AssertionError("Fire selector failed: "+s);
  if(phase==0&&(s==null||s.loaded()!=21||s.reserve()!=78))throw new AssertionError("Real TACZ counts differ from fixture: "+s);
  phase++;action=false;
 }
}
