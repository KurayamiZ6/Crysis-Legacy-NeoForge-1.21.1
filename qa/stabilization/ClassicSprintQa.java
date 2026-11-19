package local.crysisqa;

import io.github.painworks.crysismod.registry.ModItems;
import io.github.painworks.crysismod.nanosuit.*;
import io.github.painworks.crysismod.nanosuit.energy.*;
import io.github.painworks.crysismod.nanosuit.handler.NanosuitPlayerHandler;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Actual server movement/energy ticks; energy is prepared only at each boundary case. */
@Mod("crysisclassicsprintqa")
public final class ClassicSprintQa {
 private final float[] energy={100,20,19,1,1,50,25,0,50,100,100};
 private int phase,age,failures;private volatile int emptySounds;private boolean complete;private float before;
 public ClassicSprintQa(){NeoForge.EVENT_BUS.addListener(this::pre);NeoForge.EVENT_BUS.addListener(this::post);NeoForge.EVENT_BUS.addListener(this::sound);}
 private void sound(net.neoforged.neoforge.client.event.sound.PlaySoundEvent event){
  if(event.getSound()!=null&&event.getSound().getLocation().getPath().equals("suit_disabled_state_initial")){emptySounds++;System.out.println("CLASSIC_SPRINT_EMPTY_SOUND count="+emptySounds);}
 }
 private void pre(ServerTickEvent.Pre event){
  var players=event.getServer().getPlayerList().getPlayers();if(players.isEmpty()||complete)return;var p=players.getFirst();
  boolean n2=phase>=9;
  if(age==0){
   p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);p.setNoGravity(true);p.setPos(.5,190,.5);
   p.setItemSlot(EquipmentSlot.HEAD,new ItemStack(n2?ModItems.NANOSUIT_2_HELMET.get():ModItems.NANOGEO_NANOSUIT_HELMET.get()));
   p.setItemSlot(EquipmentSlot.CHEST,new ItemStack(n2?ModItems.NANOSUIT_2_CHESTPLATE.get():ModItems.NANOGEO_NANOSUIT_CHESTPLATE.get()));
   p.setItemSlot(EquipmentSlot.LEGS,new ItemStack(n2?ModItems.NANOSUIT_2_LEGGINGS.get():ModItems.NANOGEO_NANOSUIT_LEGGINGS.get()));
   p.setItemSlot(EquipmentSlot.FEET,new ItemStack(n2?ModItems.NANOSUIT_2_BOOTS.get():ModItems.NANOGEO_NANOSUIT_BOOTS.get()));
   var d=NanosuitCapabilities.get(p);d.setRecoveryTicks(-1);d.setMode(n2?NanosuitMode.ARMOR:NanosuitMode.SPEED);if(phase!=8)d.setEnergyWithoutStartingRecovery(energy[phase]);d.setRegenDelayTicks(0);
  }
  p.getFoodData().setFoodLevel(20);p.setSprinting(phase!=6&&phase!=7);p.setDeltaMovement(.12,0,0);
  if(phase==10&&age==1)NanosuitPlayerHandler.requestPowerSprint(p);
  before=NanosuitCapabilities.get(p).getEnergy();
 }
 private void post(ServerTickEvent.Post event){
  var players=event.getServer().getPlayerList().getPlayers();if(players.isEmpty()||complete)return;var p=players.getFirst();var d=NanosuitCapabilities.get(p);
  if(++age<(phase==7?30:6))return;
  boolean boost=NanosuitPlayerHandler.isLandPowerSprintActive(p);
  boolean pass=switch(phase){
   case 0,5,8 -> boost;
   case 1,2 -> !boost&&d.getEnergy()<energy[phase];
   case 3,4 -> !boost&&d.getEnergy()==0&&!d.isRecoveryLocked();
   case 6,7 -> !boost&&d.getEnergy()>energy[phase];
   case 9 -> !boost;
   case 10 -> boost;
   default -> false;
  };
  if(!pass)failures++;
  System.out.println("CLASSIC_SPRINT phase="+phase+" pass="+pass+" prepared="+energy[phase]+" energy="+d.getEnergy()+" sprinting="+p.isSprinting()+" boost="+boost+" locked="+d.isRecoveryLocked());
  age=0;if(++phase==energy.length){complete=true;if(emptySounds<2)failures++;System.out.println("CLASSIC_SPRINT_COMPLETE failures="+failures+" emptySounds="+emptySounds);net.minecraft.client.Minecraft.getInstance().execute(()->net.minecraft.client.Minecraft.getInstance().stop());}
 }
}


