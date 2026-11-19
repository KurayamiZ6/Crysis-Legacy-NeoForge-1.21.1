package local.crysisqa;

import io.github.painworks.crysismod.registry.ModItems;
import io.github.painworks.crysismod.nanosuit.*;
import io.github.painworks.crysismod.nanosuit.energy.NanosuitCapabilities;
import io.github.painworks.crysismod.nanosuit.network.NanosuitSyncManager;
import io.github.painworks.crysismod.nanosuit.client.ClassicEnemyContacts;
import io.github.painworks.crysismod.nanovisor.client.*;
import net.minecraft.client.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/** Fixed visual scene. Target/memory states are fixture data; no claim of natural AI here. */
@Mod("crysisclassiccontactsqa")
public final class ClassicContactsQa {
 private volatile int phase,settled;private int serverPhase=-1,failures;private boolean action,complete;private long started;
 private Zombie idle;private Husk searching;private Skeleton detected;
 public ClassicContactsQa(){NeoForge.EVENT_BUS.addListener(this::server);NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.LOWEST,this::render);}
 private void server(ServerTickEvent.Post event){
  var ps=event.getServer().getPlayerList().getPlayers();if(ps.isEmpty()||complete)return;var p=ps.getFirst();
  if(serverPhase!=phase){serverPhase=phase;settled=0;
   if(phase==0){
    p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);p.setNoGravity(true);p.connection.teleport(.5,150,.5,0,0);
    for(int x=-12;x<=12;x++)for(int z=-4;z<=20;z++){p.serverLevel().setBlockAndUpdate(new BlockPos(x,149,z),Blocks.STONE.defaultBlockState());for(int y=150;y<=155;y++)p.serverLevel().setBlockAndUpdate(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState());}
    p.serverLevel().setDayTime(18000);
    p.setItemSlot(EquipmentSlot.HEAD,new ItemStack(ModItems.NANOGEO_NANOSUIT_HELMET.get()));p.setItemSlot(EquipmentSlot.CHEST,new ItemStack(ModItems.NANOGEO_NANOSUIT_CHESTPLATE.get()));p.setItemSlot(EquipmentSlot.LEGS,new ItemStack(ModItems.NANOGEO_NANOSUIT_LEGGINGS.get()));p.setItemSlot(EquipmentSlot.FEET,new ItemStack(ModItems.NANOGEO_NANOSUIT_BOOTS.get()));
    idle=new Zombie(p.serverLevel());idle.setPos(-3,150,9);searching=new Husk(EntityType.HUSK,p.serverLevel());searching.setPos(.5,150,10);detected=new Skeleton(EntityType.SKELETON,p.serverLevel());detected.setPos(4,150,9);
    for(var mob:java.util.List.of(idle,searching,detected)){mob.setNoAi(true);mob.setPersistenceRequired();p.serverLevel().addFreshEntity(mob);}
    var d=NanosuitCapabilities.get(p);d.setRecoveryTicks(-1);d.setEnergyWithoutStartingRecovery(100);d.setMode(NanosuitMode.ARMOR);NanosuitSyncManager.syncToClient(p);
   }
   if(phase==1||phase==2){searching.setLastHurtByMob(p);detected.setTarget(p);}
   if(phase==3){for(int x=2;x<=6;x++)for(int y=150;y<=153;y++)p.serverLevel().setBlockAndUpdate(new BlockPos(x,y,5),Blocks.STONE.defaultBlockState());}
   if(phase==4){p.setItemSlot(EquipmentSlot.CHEST,ItemStack.EMPTY);NanosuitSyncManager.syncToClient(p);}
   if(phase==5){p.setItemSlot(EquipmentSlot.HEAD,new ItemStack(ModItems.NANOSUIT_2_HELMET.get()));p.setItemSlot(EquipmentSlot.CHEST,new ItemStack(ModItems.NANOSUIT_2_CHESTPLATE.get()));p.setItemSlot(EquipmentSlot.LEGS,new ItemStack(ModItems.NANOSUIT_2_LEGGINGS.get()));p.setItemSlot(EquipmentSlot.FEET,new ItemStack(ModItems.NANOSUIT_2_BOOTS.get()));NanosuitSyncManager.syncToClient(p);}
  }
  settled++;
 }
 private void render(RenderGuiEvent.Post event){
  var mc=Minecraft.getInstance();if(complete||mc.player==null||serverPhase!=phase||settled<16)return;
  if(!action){
   if(phase==1)KeyMapping.click(NanoVisorKeyMappings.TOGGLE_NANO_VISOR.getKey());
   mc.options.hideGui=phase==2;
   action=true;started=System.nanoTime();return;
  }
  if(System.nanoTime()-started<800_000_000L)return;
  var entities=ClassicEnemyContacts.entities();var colors=entities.stream().map(e->e.getType()+"="+Integer.toHexString(ClassicEnemyContacts.color(e))+"/outline="+ClassicEnemyContacts.outline(e)).sorted().toList();
  boolean pass=phase>=4?entities.isEmpty()&&!ClassicBinoculars.running():entities.size()==3;
  if(phase==1||phase==2){pass &= entities.stream().anyMatch(e->ClassicEnemyContacts.color(e)==0x83c66d)&&entities.stream().anyMatch(e->ClassicEnemyContacts.color(e)==0xe8d653)&&entities.stream().anyMatch(e->ClassicEnemyContacts.color(e)==0xed3029);}
  if(phase==3)pass &= entities.stream().filter(e->e instanceof Skeleton).noneMatch(ClassicEnemyContacts::outline);
  if(phase==2)pass &= entities.stream().noneMatch(ClassicEnemyContacts::outline);
  if(!pass)failures++;
  System.out.println("CLASSIC_CONTACTS phase="+phase+" pass="+pass+" contacts="+colors+" optic="+ClassicBinoculars.running());
  event.getGuiGraphics().flush();Screenshot.grab(mc.gameDirectory,"contacts-"+phase+".png",mc.getMainRenderTarget(),ignored->{});
  if(++phase==6){complete=true;System.out.println("CLASSIC_CONTACTS_COMPLETE failures="+failures);mc.stop();}action=false;
 }
}
