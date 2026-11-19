package local.crysisqa;
import io.github.painworks.crysismod.registry.ModItems;
import io.github.painworks.crysismod.nanosuit.*;
import io.github.painworks.crysismod.nanosuit.energy.NanosuitCapabilities;
import io.github.painworks.crysismod.nanosuit.handler.NanosuitPlayerHandler;
import io.github.painworks.crysismod.nanosuit.network.NanosuitSyncManager;
import io.github.painworks.crysismod.client.minimap.render.ThreatGauge;
import net.minecraft.client.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.client.event.*;
@Mod("crysiszaqa")
public final class ZombieAwarenessQa {
 private final String[] names={"hidden-wall","pursuit","n1-cloak","n1-search-observation","n1-revealed","multiple","n1-cloak-retreat","n1-removed","n2-cloak-retreat","n2-search-observation","n2-revealed","n2-recloak","n2-removed"};
 private final java.util.List<Zombie> zombies=new java.util.ArrayList<>();private volatile int phase,age,targets;private int configured=-1,timeout,failures;private volatile boolean shot,complete;
 public ZombieAwarenessQa(){NeoForge.EVENT_BUS.addListener(this::server);NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,this::render);NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post e)->{var mc=Minecraft.getInstance();if(++timeout>4400)mc.stop();if(mc.player!=null){mc.player.setYRot(0);mc.player.setXRot(0);mc.options.chatVisibility().set(net.minecraft.world.entity.player.ChatVisiblity.HIDDEN);for(var k:new KeyMapping[]{mc.options.keyAttack,mc.options.keyUse,mc.options.keyUp,mc.options.keyDown,mc.options.keyLeft,mc.options.keyRight,mc.options.keyJump})k.setDown(false);}});}
 private void zombie(net.minecraft.server.level.ServerLevel level,double x,double z){var mob=new Zombie(level);mob.setPos(x,150,z);mob.setPersistenceRequired();level.addFreshEntity(mob);zombies.add(mob);}
 private void server(ServerTickEvent.Post e){var ps=e.getServer().getPlayerList().getPlayers();if(ps.isEmpty()||complete)return;var p=ps.getFirst();if(configured!=phase){configured=phase;age=0;boolean n2=phase>=8;
   p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);p.setHealth(20);p.getFoodData().setFoodLevel(20);
   p.setItemSlot(EquipmentSlot.HEAD,new ItemStack(n2?ModItems.NANOSUIT_2_HELMET.get():ModItems.NANOGEO_NANOSUIT_HELMET.get()));p.setItemSlot(EquipmentSlot.CHEST,phase==7||phase==12?ItemStack.EMPTY:new ItemStack(n2?ModItems.NANOSUIT_2_CHESTPLATE.get():ModItems.NANOGEO_NANOSUIT_CHESTPLATE.get()));p.setItemSlot(EquipmentSlot.LEGS,new ItemStack(n2?ModItems.NANOSUIT_2_LEGGINGS.get():ModItems.NANOGEO_NANOSUIT_LEGGINGS.get()));p.setItemSlot(EquipmentSlot.FEET,new ItemStack(n2?ModItems.NANOSUIT_2_BOOTS.get():ModItems.NANOGEO_NANOSUIT_BOOTS.get()));p.getInventory().setItem(p.getInventory().selected,ItemStack.EMPTY);
   if(phase==0){for(int x=-7;x<=7;x++)for(int z=-36;z<=20;z++){p.serverLevel().setBlockAndUpdate(new BlockPos(x,149,z),Blocks.STONE.defaultBlockState());for(int y=150;y<=154;y++)p.serverLevel().setBlockAndUpdate(new BlockPos(x,y,z),(x==-7||x==7?Blocks.STONE:Blocks.AIR).defaultBlockState());}for(int x=-7;x<=7;x++)for(int y=150;y<=153;y++)p.serverLevel().setBlockAndUpdate(new BlockPos(x,y,6),Blocks.STONE.defaultBlockState());zombie(p.serverLevel(),.5,16.5);p.connection.teleport(.5,150,.5,0,0);p.setNoGravity(true);var d=NanosuitCapabilities.get(p);d.setRecoveryTicks(-1);d.setEnergyWithoutStartingRecovery(100);d.setMode(NanosuitMode.ARMOR);}
   if(phase==1)for(int x=-7;x<=7;x++)for(int y=150;y<=153;y++)p.serverLevel().setBlockAndUpdate(new BlockPos(x,y,6),Blocks.AIR.defaultBlockState());
   if(phase==5){zombie(p.serverLevel(),-3,14.5);zombie(p.serverLevel(),3,14.5);}
   if(phase==6||phase==8||phase==11)p.connection.teleport(.5,150,phase==6?-10:phase==8?-20:-30,0,0);
   if(phase==8){var d=NanosuitCapabilities.get(p);d.setEnergyWithoutStartingRecovery(100);d.setMode(NanosuitMode.NONE);}
   if(phase==2||phase==6||phase==8||phase==11)NanosuitPlayerHandler.tryToggleMode(p,NanosuitMode.CLOAK);
   if(phase==4||phase==10)NanosuitPlayerHandler.tryToggleMode(p,NanosuitMode.ARMOR);
   NanosuitSyncManager.syncToClient(p);
  }
  p.serverLevel().setDayTime(18000);targets=(int)zombies.stream().filter(z->z.getTarget()==p).count();
  int wait=phase==0?40:phase==3||phase==9?100:phase==1?((targets>0&&age>=4)?age:60):phase==4||phase==7||phase==10||phase==12?40:phase==5?40:20;
  if(++age>=wait&&!shot){
   boolean expected=phase==1||phase==4||phase==5||phase==7||phase==10||phase==12;
   long hiddenLeaks=zombies.stream().filter(z->z.getTarget()==p&&io.github.painworks.crysismod.nanosuit.handler.NanosuitCloak.hidesFrom(z,p)).count();
   // Close-range detection remains legal under cloak; only targets actually hidden by its rules must clear.
   boolean passed=expected?targets>0:phase==0?targets==0:hiddenLeaks==0;
   if(phase==5)passed=targets>=2;
   if(!passed)failures++;
   System.out.println("ZA_SERVER "+names[phase]+" pass="+passed+" targets="+targets+" hiddenLeaks="+hiddenLeaks+" invisible="+p.isInvisible()+" mode="+NanosuitCapabilities.get(p).getMode()+" energy="+NanosuitCapabilities.get(p).getEnergy()+" health="+p.getHealth()+" distances="+zombies.stream().map(z->String.format(java.util.Locale.ROOT,"%.1f",z.distanceTo(p))).toList()+" navigating="+zombies.stream().map(z->!z.getNavigation().isDone()).toList()+" ZA="+net.neoforged.fml.ModList.get().isLoaded("zombieawareness"));shot=true;
  }
 }
 private void render(RenderGuiEvent.Post e){var mc=Minecraft.getInstance();if(complete||mc.player==null||!shot)return;
  System.out.println("ZA_CLIENT "+names[phase]+" threat="+ThreatGauge.current(mc.player)+" targets="+targets);
  if(phase==0||phase==2||phase==4||phase==7||phase==8||phase==12){e.getGuiGraphics().flush();Screenshot.grab(mc.gameDirectory,names[phase]+".png",mc.getMainRenderTarget(),ignored->{});}
  if(++phase==names.length){complete=true;System.out.println("ZA_COMPLETE failures="+failures);mc.stop();}shot=false;
 }
}
