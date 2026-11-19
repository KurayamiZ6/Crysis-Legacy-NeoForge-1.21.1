package local.crysisqa;

import io.github.painworks.crysismod.registry.ModItems;
import io.github.painworks.crysismod.nanosuit.*;
import io.github.painworks.crysismod.nanosuit.energy.NanosuitCapabilities;
import io.github.painworks.crysismod.nanosuit.network.NanosuitSyncManager;
import io.github.painworks.crysismod.nanovision.server.ClassicNightVisionServerState;
import io.github.painworks.crysismod.client.nanovision.*;
import net.minecraft.client.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.effect.*;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.client.event.*;

@Mod("crysisnightlightqa")
public final class NightLightQa {
 private final String[] names={"dark","entry","lit","milk-active","real-potion","off-with-potion","milk-off","entry-again","removed","n2-control","n2-thermal","n2-removed","n1-armor","n1-strength","n1-speed","n1-cloak","n2-armor","n2-cloak","tacz-binoculars","tacz-zoom4","tacz-removed"};
 private volatile int phase,serverPhase=-1,settled;private int timeout,failures;private boolean action,done;private long start;
 public NightLightQa(){NeoForge.EVENT_BUS.addListener(this::server);NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,this::client);NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,this::render);}
 private void server(ServerTickEvent.Post e){var ps=e.getServer().getPlayerList().getPlayers();if(ps.isEmpty()||phase>=names.length)return;var p=ps.getFirst();if(serverPhase!=phase){serverPhase=phase;settled=0;boolean n2=phase>=9&&phase<=11||phase==16||phase==17;
  p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);p.setItemSlot(EquipmentSlot.HEAD,new ItemStack(n2?ModItems.NANOSUIT_2_HELMET.get():ModItems.NANOGEO_NANOSUIT_HELMET.get()));p.setItemSlot(EquipmentSlot.CHEST,phase==8||phase==11||phase==20?ItemStack.EMPTY:new ItemStack(n2?ModItems.NANOSUIT_2_CHESTPLATE.get():ModItems.NANOGEO_NANOSUIT_CHESTPLATE.get()));p.setItemSlot(EquipmentSlot.LEGS,new ItemStack(n2?ModItems.NANOSUIT_2_LEGGINGS.get():ModItems.NANOGEO_NANOSUIT_LEGGINGS.get()));p.setItemSlot(EquipmentSlot.FEET,new ItemStack(n2?ModItems.NANOSUIT_2_BOOTS.get():ModItems.NANOGEO_NANOSUIT_BOOTS.get()));
  if(phase==0){
   for(int x=-6;x<=6;x++)for(int z=-4;z<=17;z++)for(int y=149;y<=156;y++)p.serverLevel().setBlockAndUpdate(new BlockPos(x,y,z),(x==-6||x==6||z==-4||z==17||y==149||y==156?Blocks.STONE:Blocks.AIR).defaultBlockState());
   for(int x=-4;x<=4;x++)for(int y=150;y<=154;y++)p.serverLevel().setBlockAndUpdate(new BlockPos(x,y,16),(x%2==0?Blocks.WHITE_CONCRETE:Blocks.RED_CONCRETE).defaultBlockState());
   for(int x=2;x<=4;x++)for(int y=150;y<=153;y++)p.serverLevel().setBlockAndUpdate(new BlockPos(x,y,5),Blocks.STONE.defaultBlockState());
   var zombie=new Zombie(p.serverLevel());zombie.setPos(-3,150,9);zombie.setNoAi(true);zombie.setPersistenceRequired();p.serverLevel().addFreshEntity(zombie);
   var skeleton=new Skeleton(EntityType.SKELETON,p.serverLevel());skeleton.setPos(3,150,9);skeleton.setNoAi(true);skeleton.setPersistenceRequired();p.serverLevel().addFreshEntity(skeleton);
   var cow=new Cow(EntityType.COW,p.serverLevel());cow.setPos(.5,150,12);cow.setNoAi(true);cow.setPersistenceRequired();p.serverLevel().addFreshEntity(cow);
   p.removeAllEffects();
  }
  p.connection.teleport(.5,150,phase>=12?-15:.5,0,0);p.setNoGravity(true);p.getInventory().setItem(p.getInventory().selected,ItemStack.EMPTY);
  if(phase>=18)try{var builder=Class.forName("com.tacz.guns.api.item.builder.GunItemBuilder");Object b=builder.getMethod("create").invoke(null);builder.getMethod("setId",net.minecraft.resources.ResourceLocation.class).invoke(b,net.minecraft.resources.ResourceLocation.parse("tacz:glock_17"));var gun=(ItemStack)builder.getMethod("build",net.minecraft.core.HolderLookup.Provider.class).invoke(b,p.registryAccess());p.getInventory().setItem(p.getInventory().selected,gun);}catch(Exception ex){throw new RuntimeException(ex);}
  var d=NanosuitCapabilities.get(p);d.setEnergyWithoutStartingRecovery(100);d.setMode(phase==13?NanosuitMode.STRENGTH:phase==14?NanosuitMode.SPEED:phase==15||phase==17?NanosuitMode.CLOAK:NanosuitMode.ARMOR);NanosuitSyncManager.syncToClient(p);
  if(phase==1||phase==5||phase==7||phase==19)ClassicNightVisionServerState.toggle(p);
  if(phase==3||phase==6)new ItemStack(Items.MILK_BUCKET).finishUsingItem(p.serverLevel(),p);
  if(phase==4)p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION,2000,0,false,false,false));
 }p.serverLevel().setDayTime(18000);p.serverLevel().setWeatherParameters(0,0,false,false);settled++;}
 private void client(ClientTickEvent.Post e){var mc=Minecraft.getInstance();if(++timeout>4200){System.out.println("NIGHT_LIGHT_TIMEOUT "+phase);mc.stop();}if(mc.player==null||phase>=names.length||serverPhase!=phase||settled<(phase==0?80:12))return;
  mc.player.setNoGravity(true);mc.player.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);mc.player.setYRot(0);mc.player.setXRot(0);mc.options.chatVisibility().set(net.minecraft.world.entity.player.ChatVisiblity.HIDDEN);
  for(var k:new KeyMapping[]{mc.options.keyAttack,mc.options.keyUse,mc.options.keyUp,mc.options.keyDown,mc.options.keyLeft,mc.options.keyRight,mc.options.keyJump})k.setDown(false);
  if(!action){action=true;start=System.nanoTime();mc.options.setCameraType(phase>=12&&phase<18?CameraType.THIRD_PERSON_FRONT:CameraType.FIRST_PERSON);if(phase==10)KeyMapping.click(NanovisionKeybinds.TOGGLE_NANOVISION.getKey());if(phase==18)KeyMapping.click(io.github.painworks.crysismod.nanovisor.client.NanoVisorKeyMappings.TOGGLE_NANO_VISOR.getKey());if(phase==19)NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.client.event.InputEvent.MouseScrollingEvent(0,1,false,false,false,960,540));}
 }
 private void render(RenderGuiEvent.Post e){var mc=Minecraft.getInstance();if(done||mc.player==null)return;if(phase>=names.length){done=true;System.out.println("NIGHT_LIGHT_COMPLETE failures="+failures);mc.stop();return;}if(!action||System.nanoTime()-start<(phase==1||phase==7?200_000_000L:1_200_000_000L))return;
  float light=ClassicNightVisionClient.lightIntensity();boolean potion=mc.player.hasEffect(MobEffects.NIGHT_VISION),shader=false;float uniform=-1;
  try{Class<?> api=Class.forName("net.irisshaders.iris.api.v0.IrisApi");Object instance=api.getMethod("getInstance").invoke(null);shader=(Boolean)api.getMethod("isShaderPackInUse").invoke(instance);var m=Class.forName("net.irisshaders.iris.uniforms.CommonUniforms").getDeclaredMethod("getNightVision");m.setAccessible(true);uniform=((Number)m.invoke(null)).floatValue();}catch(ClassNotFoundException absent){}catch(Exception ex){throw new RuntimeException(ex);}
  if(Boolean.getBoolean("crysis.qa.expectShader")&&!shader)failures++;
  if(phase==0&&potion||phase==2&&(light<.99||potion)||phase==3&&(light<.99||potion)||phase==4&&!potion||phase==5&&(!potion||light>.01)||phase==6&&(potion||light>.01)||phase==8&&light!=0||phase==9&&light!=0||phase==10&&!NanovisionController.isActive()||phase==11&&NanovisionController.isActive())failures++;
  if(shader&&(phase==2||phase==3)&&uniform<.99)failures++;
  if(phase==18&&io.github.painworks.crysismod.nanovisor.client.ClassicBinoculars.progress()<.99||phase==19&&io.github.painworks.crysismod.nanovisor.client.ClassicBinoculars.zoom()!=4||phase==20&&io.github.painworks.crysismod.nanovisor.client.NanoVisorState.isVisorActive())failures++;
  if((phase==2||phase==10)&&net.neoforged.fml.ModList.get().isLoaded("entity_model_features"))try{
   Class<?> emf=Class.forName("traben.entity_model_features.models.IEMFModel");var method=emf.getMethod("emf$isEMFModel");java.util.Map<String,Boolean> models=new java.util.TreeMap<>();
   for(var entity:mc.level.entitiesForRendering())if(entity instanceof Zombie||entity instanceof Skeleton||entity instanceof Cow){var renderer=mc.getEntityRenderDispatcher().getRenderer(entity);if(renderer instanceof net.minecraft.client.renderer.entity.LivingEntityRenderer<?,?> living){Object model=living.getModel();models.put(entity.getType().toString(),emf.isInstance(model)&&Boolean.TRUE.equals(method.invoke(model)));}}
   System.out.println("NIGHT_LIGHT_EMF "+models);if(models.size()<3||models.containsValue(false))failures++;
  }catch(Exception ex){throw new RuntimeException(ex);}
  System.out.println("NIGHT_LIGHT "+names[phase]+" intensity="+light+" potion="+potion+" iris="+shader+" uniform="+uniform+" thermal="+NanovisionController.isActive()+" blockLight="+mc.level.getBrightness(net.minecraft.world.level.LightLayer.BLOCK,mc.player.blockPosition())+" sky="+mc.level.getBrightness(net.minecraft.world.level.LightLayer.SKY,mc.player.blockPosition())+" fresh="+mc.getResourcePackRepository().getSelectedIds());
  if(phase!=4&&phase!=7&&phase!=9&&phase!=11){e.getGuiGraphics().flush();Screenshot.grab(mc.gameDirectory,names[phase]+".png",mc.getMainRenderTarget(),ignored->{});}
  phase=Boolean.getBoolean("crysis.qa.modelsOnly")?switch(phase){case 0->1;case 1->2;case 2->9;case 9->10;default->names.length;}:phase==17&&!Boolean.getBoolean("crysis.qa.combined")?names.length:phase+1;action=false;
 }
}
