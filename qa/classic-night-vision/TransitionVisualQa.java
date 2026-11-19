package local.crysisqa;
import io.github.painworks.crysismod.client.nanovision.ClassicNightVisionClient;
import io.github.painworks.crysismod.nanovision.ClassicNightVisionBattery;
import io.github.painworks.crysismod.nanovision.server.ClassicNightVisionServerState;
import io.github.painworks.crysismod.nanovision.network.SyncClassicNightVisionPacket;
import io.github.painworks.crysismod.nanosuit.network.NanosuitNetwork;
import io.github.painworks.crysismod.nanosuit.network.NanosuitSyncManager;
import io.github.painworks.crysismod.nanosuit.energy.NanosuitCapabilities;
import io.github.painworks.crysismod.nanosuit.NanosuitMode;
import io.github.painworks.crysismod.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
@Mod("crysisvisiontransitionqa")
public final class TransitionVisualQa {
 private final String[] names={"baseline","entry","exit","rapid-on","rapid-off","rapid-on-again","empty","n2"};
 private final int[][] samples={{0,250},{0,250,500,1050},{0,250,500,1050},{0,350},{0,150},{0,250,1050},{0,200,600,1450},{0,300}};
 private volatile int phase,serverPhase=-1;private int sample,timeout;private long start;private boolean done;
 public TransitionVisualQa(){NeoForge.EVENT_BUS.addListener(this::server);NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,this::render);NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post e)->{if(++timeout>2400)Minecraft.getInstance().stop();});}
 private boolean active(){return phase==1||phase==3||phase==5||phase==6;}
 private void server(ServerTickEvent.Post e){
  var players=e.getServer().getPlayerList().getPlayers();if(players.isEmpty()||phase>=names.length)return;var p=players.getFirst();
  if(serverPhase!=phase){serverPhase=phase;
   boolean n2=phase==7;
   p.setItemSlot(EquipmentSlot.HEAD,new ItemStack(n2?ModItems.NANOSUIT_2_HELMET.get():ModItems.NANOGEO_NANOSUIT_HELMET.get()));
   p.setItemSlot(EquipmentSlot.CHEST,new ItemStack(n2?ModItems.NANOSUIT_2_CHESTPLATE.get():ModItems.NANOGEO_NANOSUIT_CHESTPLATE.get()));
   p.setItemSlot(EquipmentSlot.LEGS,new ItemStack(n2?ModItems.NANOSUIT_2_LEGGINGS.get():ModItems.NANOGEO_NANOSUIT_LEGGINGS.get()));
   p.setItemSlot(EquipmentSlot.FEET,new ItemStack(n2?ModItems.NANOSUIT_2_BOOTS.get():ModItems.NANOGEO_NANOSUIT_BOOTS.get()));
   p.serverLevel().setDayTime(18000);p.getInventory().setItem(p.getInventory().selected,ItemStack.EMPTY);
   try{
    var f=ClassicNightVisionServerState.class.getDeclaredField("BATTERIES");f.setAccessible(true);
    @SuppressWarnings("unchecked")var map=(java.util.Map<java.util.UUID,ClassicNightVisionBattery>)f.get(null);
    var b=new ClassicNightVisionBattery();var charge=ClassicNightVisionBattery.class.getDeclaredField("charge");charge.setAccessible(true);charge.setInt(b,phase==6?8:600);
    if(active())b.toggle();map.put(p.getUUID(),b);NanosuitNetwork.sendToPlayer(p,new SyncClassicNightVisionPacket(b.active(),b.charge()));
   }catch(Exception ex){throw new RuntimeException(ex);}
  }
  var d=NanosuitCapabilities.get(p);d.setEnergyWithoutStartingRecovery(100);d.setMode(NanosuitMode.ARMOR);NanosuitSyncManager.syncToClient(p);
 }
 private void render(RenderGuiEvent.Post e){
  var mc=Minecraft.getInstance();if(done||mc.player==null||mc.screen!=null)return;
  mc.player.setYRot(0);mc.player.setXRot(15);
  if(phase>=names.length){System.out.println("VISION_TRANSITION_COMPLETE");done=true;mc.stop();return;}
  if(serverPhase!=phase)return;
  if(start==0){if(ClassicNightVisionClient.isActive()!=active())return;start=System.nanoTime();}
  long ms=(System.nanoTime()-start)/1_000_000;
  if(ms<samples[phase][sample])return;
  e.getGuiGraphics().flush();String name=names[phase]+"-"+samples[phase][sample]+".png";
  Screenshot.grab(mc.gameDirectory,name,mc.getMainRenderTarget(),ignored->{});
  System.out.println("VISION_TRANSITION_CAPTURE "+name+" elapsed="+ms+" intensity="+ClassicNightVisionClient.visualIntensity()+" active="+ClassicNightVisionClient.isActive()+" charge="+ClassicNightVisionClient.batteryCharge());
  if(++sample==samples[phase].length){phase++;sample=0;start=0;}
 }
}
