package local.crysisqa;
import io.github.painworks.crysismod.nanovisor.client.*;
import io.github.painworks.crysismod.nanosuit.NanosuitMode;
import io.github.painworks.crysismod.nanosuit.energy.NanosuitCapabilities;
import io.github.painworks.crysismod.nanosuit.network.NanosuitSyncManager;
import io.github.painworks.crysismod.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
@Mod("crysisbinocularqa")
public final class ClassicBinocularVisualQa {
 private static final String[] NAMES={"opening","zoom4","closing","rapid-open","rapid-close","rapid-reopen","triple-click","entity","occluded","sky","f1","screen","reopen","removed","n2","tacz-hip","tacz-ads","tacz-refuse","tacz-return","tacz-zoom4"};
 private static final int[][] SAMPLES={{0,150,500},{500},{0,150,500},{120},{0,80},{0,150,500},{0,80,500},{600},{150},{150},{150},{150},{500},{200},{500},{1000},{600},{400},{1000},{600}};
 private volatile int phase,serverPhase=-1,settled;private int timeout,sample,clipFrame;private long start,nextClip,clipStarted;private boolean action,done;private ArmorStand stand;
 public ClassicBinocularVisualQa(){if(Boolean.getBoolean("crysis.binocular.qa.optic"))phase=15;NeoForge.EVENT_BUS.addListener(this::server);NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,this::render);NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,this::client);}
 private void server(ServerTickEvent.Post e){
  var list=e.getServer().getPlayerList().getPlayers();if(list.isEmpty()||phase>=NAMES.length)return;var p=list.getFirst();
  if(serverPhase!=phase){serverPhase=phase;settled=0;boolean n2=phase==14;
   p.setItemSlot(EquipmentSlot.HEAD,new ItemStack(n2?ModItems.NANOSUIT_2_HELMET.get():ModItems.NANOGEO_NANOSUIT_HELMET.get()));
   p.setItemSlot(EquipmentSlot.CHEST,phase==13?ItemStack.EMPTY:new ItemStack(n2?ModItems.NANOSUIT_2_CHESTPLATE.get():ModItems.NANOGEO_NANOSUIT_CHESTPLATE.get()));
   p.setItemSlot(EquipmentSlot.LEGS,new ItemStack(n2?ModItems.NANOSUIT_2_LEGGINGS.get():ModItems.NANOGEO_NANOSUIT_LEGGINGS.get()));
   p.setItemSlot(EquipmentSlot.FEET,new ItemStack(n2?ModItems.NANOSUIT_2_BOOTS.get():ModItems.NANOGEO_NANOSUIT_BOOTS.get()));
   if(phase==0){
    for(int x=-2;x<=2;x++)for(int z=-1;z<=10;z++){
     p.serverLevel().setBlockAndUpdate(new BlockPos(x,109,z),Blocks.STONE.defaultBlockState());
     for(int y=110;y<=116;y++)p.serverLevel().setBlockAndUpdate(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState());
    }
    for(int y=111;y<p.serverLevel().getMaxBuildHeight();y++)p.serverLevel().setBlockAndUpdate(new BlockPos(0,y,0),Blocks.AIR.defaultBlockState());
    p.serverLevel().setBlockAndUpdate(new BlockPos(0,111,8),Blocks.STONE.defaultBlockState());
   }
   p.connection.teleport(.5,110,.5,0,phase==9?-90:0);p.setNoGravity(true);
   if(phase==7){stand=new ArmorStand(p.serverLevel(),.5,110,5.5);stand.setNoGravity(true);p.serverLevel().addFreshEntity(stand);}
   if(phase==8)p.serverLevel().setBlockAndUpdate(new BlockPos(0,111,3),Blocks.STONE.defaultBlockState());
   if(phase==15)try {
    var type=Class.forName("com.tacz.guns.api.item.builder.GunItemBuilder");var builder=type.getMethod("create").invoke(null);
    boolean optic=Boolean.getBoolean("crysis.binocular.qa.optic");
    type.getMethod("setId",ResourceLocation.class).invoke(builder,ResourceLocation.fromNamespaceAndPath("tacz",optic?"m4a1":"glock_17"));
    if(optic){
     @SuppressWarnings("unchecked") var attachment=(Class<? extends Enum>)Class.forName("com.tacz.guns.api.item.attachment.AttachmentType");
     @SuppressWarnings("unchecked") var scope=Enum.valueOf(attachment,"SCOPE");
     type.getMethod("putAttachment",attachment,ResourceLocation.class).invoke(builder,scope,ResourceLocation.fromNamespaceAndPath("tacz","sight_552"));
     System.out.println("BINOCULAR_TACZ_OPTIC tacz:m4a1 / tacz:sight_552");
    }
    ItemStack gun=optic?(ItemStack)type.getMethod("build",net.minecraft.core.HolderLookup.Provider.class).invoke(builder,p.registryAccess()):(ItemStack)type.getMethod("build").invoke(builder);
    p.getInventory().setItem(p.getInventory().selected,gun);
    System.out.println("BINOCULAR_TACZ_GUN "+p.getMainHandItem());
   }catch(Exception ex){throw new RuntimeException(ex);}
   else if(phase<15)p.getInventory().setItem(p.getInventory().selected,ItemStack.EMPTY);
  }
  p.setYRot(0);p.setXRot(phase==9?-80:0);p.serverLevel().setDayTime(6000);
  var data=NanosuitCapabilities.get(p);data.setEnergyWithoutStartingRecovery(100);data.setMode(NanosuitMode.ARMOR);NanosuitSyncManager.syncToClient(p);settled++;
 }
 private void click(KeyMapping key){KeyMapping.click(key.getKey());}
 private void aim(boolean enabled){try{var type=Class.forName("com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator");var op=type.getMethod("fromLocalPlayer",net.minecraft.client.player.LocalPlayer.class).invoke(null,Minecraft.getInstance().player);type.getMethod("aim",boolean.class).invoke(op,enabled);}catch(Exception ex){throw new RuntimeException(ex);}}
 private void client(ClientTickEvent.Post e){
  if(++timeout>3200){System.out.println("BINOCULAR_TIMEOUT phase="+phase);Minecraft.getInstance().stop();}
  if(phase==12&&Minecraft.getInstance().screen!=null)Minecraft.getInstance().setScreen(null);
  var mc=Minecraft.getInstance();if(mc.player==null||phase>=NAMES.length||serverPhase!=phase||settled<(phase==0&&Boolean.getBoolean("crysis.binocular.qa.clip")?60:phase>=3&&phase<=6?1:8))return;
  mc.player.setNoGravity(true);mc.player.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
  mc.player.setSprinting(false);
  for(var key:new KeyMapping[]{mc.options.keyAttack,mc.options.keyUse,mc.options.keyUp,mc.options.keyDown,mc.options.keyLeft,mc.options.keyRight,mc.options.keyJump})key.setDown(false);
  if(phase==16||phase==17)aim(true);
  if(phase==18||phase==19)aim(false);
  if(phase==18&&!ClassicBinoculars.eligible())return;
  if(!action&&(phase<13||phase==15)&&!ClassicBinoculars.eligible())return;
  if(!action){action=true;start=System.nanoTime();
   if(phase==0||phase==2||phase==3||phase==4||phase==5||phase==7||phase==12||phase==14||phase==15||phase==17||phase==18)click(NanoVisorKeyMappings.TOGGLE_NANO_VISOR);
   if(phase==1||phase==19){var scroll=new net.neoforged.neoforge.client.event.InputEvent.MouseScrollingEvent(0,1,false,false,false,960,540);NeoForge.EVENT_BUS.post(scroll);System.out.println("BINOCULAR_SCROLL consumed="+scroll.isCanceled());}
   if(phase==6){click(NanoVisorKeyMappings.TOGGLE_NANO_VISOR);click(NanoVisorKeyMappings.TOGGLE_NANO_VISOR);click(NanoVisorKeyMappings.TOGGLE_NANO_VISOR);}
   if(phase==10)mc.options.hideGui=true;
   if(phase==11){mc.options.hideGui=false;mc.setScreen(new PauseScreen(false));}
   if(phase==12)mc.setScreen(null);
   if(phase==16)aim(true);
   if(phase==18)aim(false);
  }
 }
 private void render(RenderGuiEvent.Post e){
  var mc=Minecraft.getInstance();if(done||mc.player==null)return;
  if(Boolean.getBoolean("crysis.binocular.qa.clip")&&phase>=7){System.out.println("BINOCULAR_CLIP_COMPLETE frames="+clipFrame);done=true;mc.stop();return;}
  if(phase>=NAMES.length){System.out.println("BINOCULAR_QA_COMPLETE");done=true;mc.stop();return;}
  mc.player.setYRot(0);mc.player.setXRot(phase==9?-90:0);
  if(!action)return;long ms=(System.nanoTime()-start)/1_000_000;
  if(Boolean.getBoolean("crysis.binocular.qa.clip")&&System.nanoTime()>=nextClip){
   long now=System.nanoTime();if(clipStarted==0)clipStarted=now;nextClip=now+70_000_000L;
   e.getGuiGraphics().flush();String file=String.format(java.util.Locale.ROOT,"clip-%04d.png",clipFrame++);
   Screenshot.grab(mc.gameDirectory,file,mc.getMainRenderTarget(),ignored->{});
   System.out.println("BINOCULAR_CLIP "+file+" ms="+(now-clipStarted)/1_000_000+" phase="+phase);
  }
  if(ms<SAMPLES[phase][sample])return;
  e.getGuiGraphics().flush();String name=NAMES[phase]+"-"+SAMPLES[phase][sample]+".png";
  Screenshot.grab(mc.gameDirectory,name,mc.getMainRenderTarget(),ignored->{});
  System.out.println("BINOCULAR_CAPTURE "+name+" ms="+ms+" state="+ClassicBinoculars.animationState()+" p="+ClassicBinoculars.progress()+" active="+NanoVisorState.isVisorActive()+" zoom="+ClassicBinoculars.zoom()+" baseFov="+ClassicBinoculars.lastBaseFov()+" finalFov="+ClassicBinoculars.lastWorldFov()+" distance="+ClassicBinoculars.targetDistance(1)+" pos="+mc.player.position()+" pitch="+mc.player.getXRot());
  if(++sample==SAMPLES[phase].length){phase++;sample=0;action=false;start=0;}
 }
}
