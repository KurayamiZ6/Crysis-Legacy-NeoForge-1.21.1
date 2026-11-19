package local.crysisqa;
import io.github.painworks.crysismod.client.minimap.*;
import io.github.painworks.crysismod.registry.ModItems;
import net.minecraft.client.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.client.event.*;
@Mod("crysismapqa")
public final class ClassicMapVisualQa {
 private final String[] names={"south-no-goal","west-goal","north-moved","east-cleared","gui3","f1","removed","n2"};
 private volatile int phase,serverPhase=-1,settled;private int timeout;private long start;private boolean action,done;
 private boolean dioCanceled;private int dioFrames;
 public ClassicMapVisualQa(){if(Boolean.getBoolean("crysis.map.qa.smoke"))phase=4;NeoForge.EVENT_BUS.addListener(this::server);NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,this::client);NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,this::render);NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,true,RenderGuiLayerEvent.Pre.class,e->{if(e.getName().toString().equals("diorama:minimap")){dioCanceled=e.isCanceled();dioFrames++;}});}
 private void server(ServerTickEvent.Post e){var ps=e.getServer().getPlayerList().getPlayers();if(ps.isEmpty()||phase>=names.length)return;var p=ps.getFirst();
  if(serverPhase!=phase){serverPhase=phase;settled=0;boolean n2=phase==7;
   p.setItemSlot(EquipmentSlot.HEAD,new ItemStack(n2?ModItems.NANOSUIT_2_HELMET.get():ModItems.NANOGEO_NANOSUIT_HELMET.get()));
   p.setItemSlot(EquipmentSlot.CHEST,phase==6?ItemStack.EMPTY:new ItemStack(n2?ModItems.NANOSUIT_2_CHESTPLATE.get():ModItems.NANOGEO_NANOSUIT_CHESTPLATE.get()));
   p.setItemSlot(EquipmentSlot.LEGS,new ItemStack(n2?ModItems.NANOSUIT_2_LEGGINGS.get():ModItems.NANOGEO_NANOSUIT_LEGGINGS.get()));
   p.setItemSlot(EquipmentSlot.FEET,new ItemStack(n2?ModItems.NANOSUIT_2_BOOTS.get():ModItems.NANOGEO_NANOSUIT_BOOTS.get()));
   p.connection.teleport(phase>=2?20.5:.5,110,.5,(phase%4)*90,20);p.setNoGravity(true);
  }p.serverLevel().setDayTime(6000);settled++;
 }
 private void client(ClientTickEvent.Post e){var mc=Minecraft.getInstance();if(++timeout>2400){System.out.println("MAP_QA_TIMEOUT "+phase);mc.stop();}if(mc.player==null||phase>=names.length||serverPhase!=phase||settled<40)return;
  mc.player.setNoGravity(true);mc.player.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);mc.player.setYRot(phase%4*90);mc.player.setXRot(20);
  for(var key:new KeyMapping[]{mc.options.keyAttack,mc.options.keyUse,mc.options.keyUp,mc.options.keyDown,mc.options.keyLeft,mc.options.keyRight,mc.options.keyJump})key.setDown(false);
  if(!action){action=true;start=System.nanoTime();if(phase==1)ClassicWaypoint.follow(35,25);if(phase==3)ClassicWaypoint.clear();if(phase==4){mc.options.guiScale().set(3);mc.resizeDisplay();}mc.options.hideGui=phase==5;
   if(net.neoforged.fml.ModList.get().isLoaded("diorama"))try{var f=io.github.painworks.crysismod.nanosuit.config.NanosuitClientConfig.class.getDeclaredField("CLASSIC_MINIMAP_ENABLED");f.setAccessible(true);((net.neoforged.neoforge.common.ModConfigSpec.BooleanValue)f.get(null)).set(phase!=4);}catch(Exception ex){throw new RuntimeException(ex);}
  }
 }
 private void render(RenderGuiEvent.Post e){var mc=Minecraft.getInstance();if(done||mc.player==null)return;if(phase>=names.length){done=true;System.out.println("MAP_QA_COMPLETE");mc.stop();return;}if(!action||(System.nanoTime()-start)<2_000_000_000L)return;
  e.getGuiGraphics().flush();Screenshot.grab(mc.gameDirectory,names[phase]+".png",mc.getMainRenderTarget(),ignored->{});
  System.out.println("MAP_CAPTURE "+names[phase]+" pos="+mc.player.position()+" yaw="+mc.player.getYRot()+" goal="+ClassicWaypoint.current()+" classic="+ClassicMinimapRenderer.isClassic()+" gui="+mc.getWindow().getGuiScaledWidth()+"x"+mc.getWindow().getGuiScaledHeight()+" ready="+NanoMinimapManager.get().classicTerrainReady()+" dioCanceled="+dioCanceled+" dioFrames="+dioFrames);phase++;action=false;
 }
}
