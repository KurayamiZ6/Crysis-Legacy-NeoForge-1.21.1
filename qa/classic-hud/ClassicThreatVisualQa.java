package local.crysisqa;
import io.github.painworks.crysismod.client.minimap.ClassicWaypoint;
import io.github.painworks.crysismod.client.minimap.render.ThreatGauge;
import io.github.painworks.crysismod.nanosuit.*;
import io.github.painworks.crysismod.nanosuit.energy.NanosuitCapabilities;
import io.github.painworks.crysismod.nanosuit.network.NanosuitSyncManager;
import io.github.painworks.crysismod.nanosuit.threat.ThreatLevel;
import io.github.painworks.crysismod.registry.ModItems;
import net.minecraft.client.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.client.event.*;
@Mod("crysisthreatqa")
public final class ClassicThreatVisualQa {
 private String[] names={"calm","near-idle","targeted","cloak","reacquired","lost-orange","lost-yellow","cleared","multiple","dimension","removed","n2"};
 private volatile int phase,serverPhase=-1,settled;private int timeout;private long start;private boolean action,done;private Zombie zombie,second;
 public ClassicThreatVisualQa(){NeoForge.EVENT_BUS.addListener(this::server);NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,this::client);NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,this::render);}
 private void server(ServerTickEvent.Post e){var ps=e.getServer().getPlayerList().getPlayers();if(ps.isEmpty()||phase>=names.length)return;var p=ps.getFirst();
  if(serverPhase!=phase){serverPhase=phase;settled=0;boolean n2=phase==11;
   p.setItemSlot(EquipmentSlot.HEAD,new ItemStack(n2?ModItems.NANOSUIT_2_HELMET.get():ModItems.NANOGEO_NANOSUIT_HELMET.get()));
   p.setItemSlot(EquipmentSlot.CHEST,phase==10?ItemStack.EMPTY:new ItemStack(n2?ModItems.NANOSUIT_2_CHESTPLATE.get():ModItems.NANOGEO_NANOSUIT_CHESTPLATE.get()));
   p.setItemSlot(EquipmentSlot.LEGS,new ItemStack(n2?ModItems.NANOSUIT_2_LEGGINGS.get():ModItems.NANOGEO_NANOSUIT_LEGGINGS.get()));
   p.setItemSlot(EquipmentSlot.FEET,new ItemStack(n2?ModItems.NANOSUIT_2_BOOTS.get():ModItems.NANOGEO_NANOSUIT_BOOTS.get()));
   if(phase==0){for(int x=-4;x<=4;x++)for(int z=-2;z<=16;z++){p.serverLevel().setBlockAndUpdate(new BlockPos(x,149,z),Blocks.STONE.defaultBlockState());for(int y=150;y<155;y++)p.serverLevel().setBlockAndUpdate(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState());}}
   if(phase==9)p.teleportTo(e.getServer().getLevel(Level.NETHER),.5,150,.5,0,0);else p.connection.teleport(.5,150,.5,0,0);
   p.setNoGravity(true);
   if(phase==1){zombie=new Zombie(p.serverLevel());zombie.setPos(.5,150,7.5);zombie.setNoAi(true);zombie.setNoGravity(true);zombie.setPersistenceRequired();p.serverLevel().addFreshEntity(zombie);}
   if(phase==8){second=new Zombie(p.serverLevel());second.setPos(3.5,150,8.5);second.setNoAi(true);second.setNoGravity(true);p.serverLevel().addFreshEntity(second);}
  }
  var data=NanosuitCapabilities.get(p);data.setEnergyWithoutStartingRecovery(100);data.setMode(phase==3?NanosuitMode.CLOAK:NanosuitMode.ARMOR);NanosuitSyncManager.syncToClient(p);
  if(zombie!=null){zombie.clearFire();if(phase==2||phase==4||phase==8)zombie.setTarget(p);if(phase==5||phase==6||phase==7)zombie.setTarget(null);}
  if(second!=null&&phase==8)second.setTarget(p);
  p.serverLevel().setDayTime(18000);settled++;
 }
 private void client(ClientTickEvent.Post e){var mc=Minecraft.getInstance();if(++timeout>2600){System.out.println("THREAT_TIMEOUT "+phase);mc.stop();}if(mc.player==null||phase>=names.length||serverPhase!=phase||settled<(phase==0?60:phase==7?80:12))return;
  mc.player.setNoGravity(true);mc.player.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);mc.player.setYRot(0);mc.player.setXRot(0);
  for(var key:new KeyMapping[]{mc.options.keyAttack,mc.options.keyUse,mc.options.keyUp,mc.options.keyDown,mc.options.keyLeft,mc.options.keyRight,mc.options.keyJump})key.setDown(false);
  if(!action){if(phase==5&&ThreatGauge.currentClassic(mc.player)!=ThreatLevel.SEARCHING)return;action=true;start=System.nanoTime();if(phase==0)ClassicWaypoint.follow(5,20);}
 }
 private void render(RenderGuiEvent.Post e){var mc=Minecraft.getInstance();if(done||mc.player==null)return;if(phase>=names.length){done=true;System.out.println("THREAT_QA_COMPLETE");mc.stop();return;}if(!action||(System.nanoTime()-start)<(phase==5?0:phase==6?800_000_000L:300_000_000L))return;
  e.getGuiGraphics().flush();Screenshot.grab(mc.gameDirectory,names[phase]+".png",mc.getMainRenderTarget(),ignored->{});
  System.out.println("THREAT_CAPTURE "+names[phase]+" client="+ThreatGauge.current(mc.player)+" target="+(zombie==null?"none":zombie.getTarget())+" dim="+mc.level.dimension().location()+" goal="+ClassicWaypoint.current()+" ZA="+net.neoforged.fml.ModList.get().isLoaded("zombieawareness"));phase++;action=false;
 }
}
