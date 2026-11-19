package local.crysisqa;

import io.github.painworks.crysismod.nanosuit.NanosuitMode;
import io.github.painworks.crysismod.nanosuit.energy.NanosuitCapabilities;
import io.github.painworks.crysismod.nanosuit.network.NanosuitSyncManager;
import io.github.painworks.crysismod.nanosuit.network.NanosuitNetwork;
import io.github.painworks.crysismod.nanovision.ClassicNightVisionBattery;
import io.github.painworks.crysismod.nanovision.server.ClassicNightVisionServerState;
import io.github.painworks.crysismod.nanovision.network.SyncClassicNightVisionPacket;
import io.github.painworks.crysismod.registry.ModItems;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/** Isolated test mod only; controls the scene, never shipped. */
@Mod("crysisclassichudqa")
public final class ClassicHudQa {
   private static final String[] NAMES={"n1-100","n1-25","n1-15","n1-zero","n1-recharge","nv-active","nv-recharge","n2","no-suit","n1-gui2","n1-strength","n1-speed","n1-cloak","health-half"};
   private volatile int phase,serverPhase=-1,readyTicks;
   private int timeout,settle=-1,finish=-1;
   public ClassicHudQa(){NeoForge.EVENT_BUS.addListener(this::tick);NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.LOWEST,this::render);NeoForge.EVENT_BUS.addListener(this::client);}
   private NanosuitMode mode(){return phase==10?NanosuitMode.STRENGTH:phase==11?NanosuitMode.SPEED:phase==12?NanosuitMode.CLOAK:NanosuitMode.ARMOR;}
   private void tick(ServerTickEvent.Post e){
      if(phase>=NAMES.length||e.getServer().getPlayerList().getPlayers().isEmpty())return;
      var p=e.getServer().getPlayerList().getPlayers().getFirst();
      boolean changed=serverPhase!=phase;
      if(changed){serverPhase=phase;readyTicks=0;
         p.setGameMode(GameType.SURVIVAL);
         p.serverLevel().setDayTime(6000);
         p.serverLevel().getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false,e.getServer());
         p.serverLevel().getGameRules().getRule(GameRules.RULE_NATURAL_REGENERATION).set(false,e.getServer());
         boolean n2=phase==7;
         p.setItemSlot(EquipmentSlot.HEAD,new ItemStack(n2?ModItems.NANOSUIT_2_HELMET.get():ModItems.NANOGEO_NANOSUIT_HELMET.get()));
         p.setItemSlot(EquipmentSlot.CHEST,phase==8?ItemStack.EMPTY:new ItemStack(n2?ModItems.NANOSUIT_2_CHESTPLATE.get():ModItems.NANOGEO_NANOSUIT_CHESTPLATE.get()));
         p.setItemSlot(EquipmentSlot.LEGS,new ItemStack(n2?ModItems.NANOSUIT_2_LEGGINGS.get():ModItems.NANOGEO_NANOSUIT_LEGGINGS.get()));
         p.setItemSlot(EquipmentSlot.FEET,new ItemStack(n2?ModItems.NANOSUIT_2_BOOTS.get():ModItems.NANOGEO_NANOSUIT_BOOTS.get()));
         p.getInventory().setItem(p.getInventory().selected,ItemStack.EMPTY);p.setItemSlot(EquipmentSlot.OFFHAND,ItemStack.EMPTY);
         if(phase==4){var data=NanosuitCapabilities.get(p);data.setEnergyWithoutStartingRecovery(0);data.setRegenDelayTicks(0);}
         if(phase==5||phase==6){
            try {
               var field=ClassicNightVisionServerState.class.getDeclaredField("BATTERIES");field.setAccessible(true);
               @SuppressWarnings("unchecked") var map=(java.util.Map<java.util.UUID,ClassicNightVisionBattery>)field.get(null);
               var b=new ClassicNightVisionBattery();var charge=ClassicNightVisionBattery.class.getDeclaredField("charge");charge.setAccessible(true);charge.setInt(b,600);
               if(phase==5)b.toggle();map.put(p.getUUID(),b);
               NanosuitNetwork.sendToPlayer(p,new SyncClassicNightVisionPacket(b.active(),b.charge()));
            }catch(Exception ex){throw new RuntimeException(ex);}
         }
      }
      var data=NanosuitCapabilities.get(p);
      if(phase!=4){float energy=phase==1?25:phase==2?15:phase==3?0:100;data.setEnergyWithoutStartingRecovery(energy);data.setRegenDelayTicks(100);}
      data.setMode(mode());
      p.setHealth(phase==13?p.getMaxHealth()/2:p.getMaxHealth());
      NanosuitSyncManager.syncToClient(p);readyTicks++;
   }
   private void client(ClientTickEvent.Post e){if(++timeout>3000){System.out.println("CLASSIC_HUD_TIMEOUT");Minecraft.getInstance().stop();}}
   private void render(RenderGuiEvent.Post e){
      var mc=Minecraft.getInstance();if(mc.player==null||mc.screen!=null)return;
      if(phase>=NAMES.length){if(finish<0)finish=mc.player.tickCount;if(mc.player.tickCount-finish>25){System.out.println("CLASSIC_HUD_COMPLETE captures="+NAMES.length);mc.stop();}return;}
      int gui=phase==9?2:3;
      if(mc.options.guiScale().get()!=gui){mc.options.guiScale().set(gui);mc.resizeDisplay();settle=-1;return;}
      mc.options.setCameraType(CameraType.FIRST_PERSON);mc.player.setYRot(0);mc.player.setXRot(0);
      if(serverPhase!=phase||readyTicks<35)return;
      if(settle<0)settle=mc.player.tickCount;
      if(mc.player.tickCount-settle<12)return;
      var data=NanosuitCapabilities.get(mc.player);
      if(data.getMode()!=mode())return;
      String name=NAMES[phase]+".png";
      e.getGuiGraphics().flush();
      Screenshot.grab(mc.gameDirectory,name,mc.getMainRenderTarget(),ignored->{});
      System.out.println("CLASSIC_HUD_CAPTURE "+name+" energy="+data.getEnergy()+" health="+mc.player.getHealth()+" mode="+data.getMode()+" nv="+io.github.painworks.crysismod.client.nanovision.ClassicNightVisionClient.isActive()+" battery="+io.github.painworks.crysismod.client.nanovision.ClassicNightVisionClient.batteryCharge()+" gui="+gui);
      phase++;settle=-1;
   }
}
