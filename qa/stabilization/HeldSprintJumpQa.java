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

@Mod("crysisholdjumpqa")
public final class HeldSprintJumpQa {
 private int phase,age,failures;private volatile int sounds;private boolean complete;
 public HeldSprintJumpQa(){NeoForge.EVENT_BUS.addListener(this::pre);NeoForge.EVENT_BUS.addListener(this::post);NeoForge.EVENT_BUS.addListener(this::sound);}
 private void sound(net.neoforged.neoforge.client.event.sound.PlaySoundEvent e){if(e.getSound()!=null&&e.getSound().getLocation().getPath().equals("suit_disabled_state_initial"))sounds++;}
 private void pre(ServerTickEvent.Pre e){var ps=e.getServer().getPlayerList().getPlayers();if(ps.isEmpty()||complete)return;var p=ps.getFirst();
  if(age==0){boolean n2=phase==3;
   p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);p.setNoGravity(true);p.setPos(.5,190,.5);
   p.setItemSlot(EquipmentSlot.HEAD,new ItemStack(n2?ModItems.NANOSUIT_2_HELMET.get():ModItems.NANOGEO_NANOSUIT_HELMET.get()));p.setItemSlot(EquipmentSlot.CHEST,new ItemStack(n2?ModItems.NANOSUIT_2_CHESTPLATE.get():ModItems.NANOGEO_NANOSUIT_CHESTPLATE.get()));p.setItemSlot(EquipmentSlot.LEGS,new ItemStack(n2?ModItems.NANOSUIT_2_LEGGINGS.get():ModItems.NANOGEO_NANOSUIT_LEGGINGS.get()));p.setItemSlot(EquipmentSlot.FEET,new ItemStack(n2?ModItems.NANOSUIT_2_BOOTS.get():ModItems.NANOGEO_NANOSUIT_BOOTS.get()));
   var d=NanosuitCapabilities.get(p);if(phase==0||n2){d.setRecoveryTicks(-1);d.setEnergyWithoutStartingRecovery(n2?100:1);d.setRegenDelayTicks(0);}d.setMode(n2?NanosuitMode.ARMOR:NanosuitMode.SPEED);
  }
  p.getFoodData().setFoodLevel(20);p.setSprinting(phase==0||phase==2);p.setDeltaMovement(.12,0,0);
 }
 private void post(ServerTickEvent.Post e){var ps=e.getServer().getPlayerList().getPlayers();if(ps.isEmpty()||complete)return;var p=ps.getFirst();var d=NanosuitCapabilities.get(p);age++;
  if(phase==0&&d.getEnergy()>0&&age>2){failures++;System.out.println("HOLD_JUMP recharge-under-held-sprint energy="+d.getEnergy());}
  int duration=phase==0?80:phase==1?70:phase==2?3:1;if(age<duration)return;
  boolean pass=phase==0?d.getEnergy()==0&&sounds==1&&!d.isRecoveryLocked():phase==1?d.getEnergy()>20&&!p.isSprinting():phase==2?NanosuitPlayerHandler.isLandPowerSprintActive(p):true;
  if(phase==3)try{
   p.setOnGround(true);p.setDeltaMovement(0,0,0);d.setEnergyWithoutStartingRecovery(100);int tick=p.tickCount;
   NanosuitPlayerHandler.beginEnhancedJumpCharge(p);NanosuitPlayerHandler.requestChargedEnhancedJump(p);
   var method=NanosuitPlayerHandler.class.getDeclaredMethod("tickEnhancedJumpCharge",net.minecraft.server.level.ServerPlayer.class);method.setAccessible(true);
   p.tickCount=tick+5;method.invoke(null,p);float at5=d.getEnergy();p.setOnGround(true);
   p.tickCount=tick+6;method.invoke(null,p);float at6=d.getEnergy();p.tickCount=tick;
   pass=at5==100&&at6==90&&NanosuitProfile.NANOSUIT_2.mobility().jump().holdTicks()==6&&NanosuitProfile.NANOSUIT_1.mobility().jump().holdTicks()==0;
   System.out.println("HOLD_JUMP boundary at5="+at5+" at6="+at6+" N1hold="+NanosuitProfile.NANOSUIT_1.mobility().jump().holdTicks());
  }catch(Exception ex){throw new RuntimeException(ex);}
  if(!pass)failures++;System.out.println("HOLD_JUMP phase="+phase+" pass="+pass+" energy="+d.getEnergy()+" sounds="+sounds+" sprint="+p.isSprinting());
  age=0;if(++phase==4){complete=true;System.out.println("HOLD_JUMP_COMPLETE failures="+failures);net.minecraft.client.Minecraft.getInstance().execute(()->net.minecraft.client.Minecraft.getInstance().stop());}
 }
}
