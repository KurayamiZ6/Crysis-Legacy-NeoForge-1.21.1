package local.crysisqa;

import io.github.painworks.crysismod.registry.ModItems;
import io.github.painworks.crysismod.nanosuit.*;
import io.github.painworks.crysismod.nanosuit.client.*;
import io.github.painworks.crysismod.nanosuit.config.NanosuitClientConfig;
import io.github.painworks.crysismod.nanosuit.energy.NanosuitCapabilities;
import io.github.painworks.crysismod.nanosuit.network.NanosuitSyncManager;
import net.minecraft.client.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/** Real TACZ data and rendering; throwable bindings use test items only in the copied config. */
@Mod("crysishuddetailsqa")
public final class HudDetailsQa {
 private final String[] names={"default-full","blue-full","amber-half","amber-quarter","amber-empty","rocket","grenade-launcher","smoke-adapter","frag-adapter","shotgun-auto","gui3-half","n2-control"};
 private volatile int phase,settled;private int serverPhase=-1,failures;private boolean action,complete;private long started;
 public HudDetailsQa(){NeoForge.EVENT_BUS.addListener(this::server);NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.LOWEST,this::render);}
 private Object call(Object o,String name,Class<?>[] types,Object... args)throws Exception{return o.getClass().getMethod(name,types).invoke(o,args);}
 private void gun(net.minecraft.server.level.ServerPlayer p,String id,int loaded)throws Exception{
  Class<?> builder=Class.forName("com.tacz.guns.api.item.builder.GunItemBuilder");Object b=builder.getMethod("create").invoke(null);call(b,"setId",new Class<?>[]{ResourceLocation.class},ResourceLocation.parse(id));ItemStack stack=(ItemStack)call(b,"build",new Class<?>[]{net.minecraft.core.HolderLookup.Provider.class},p.registryAccess());
  Class<?> api=Class.forName("com.tacz.guns.api.item.IGun");Object gun=api.getMethod("getIGunOrNull",ItemStack.class).invoke(null,stack);
  api.getMethod("setCurrentAmmoCount",ItemStack.class,int.class).invoke(gun,stack,loaded);api.getMethod("setBulletInBarrel",ItemStack.class,boolean.class).invoke(gun,stack,false);
  Class<?> timeless=Class.forName("com.tacz.guns.api.TimelessAPI");var index=(java.util.Optional<?>)timeless.getMethod("getCommonGunIndex",ResourceLocation.class).invoke(null,ResourceLocation.parse(id));Object data=call(index.get(),"getGunData",new Class<?>[0]);ResourceLocation ammoId=(ResourceLocation)call(data,"getAmmoId",new Class<?>[0]);var modes=(java.util.List<?>)call(data,"getFireModeSet",new Class<?>[0]);Object mode=modes.getFirst();if(phase==9)mode=modes.stream().filter(m->m.toString().equals("AUTO")).findFirst().orElseThrow();
  api.getMethod("setFireMode",ItemStack.class,Class.forName("com.tacz.guns.api.item.gun.FireMode")).invoke(gun,stack,mode);
  p.getInventory().setItem(p.getInventory().selected,stack);p.getInventory().setItem(9,ItemStack.EMPTY);p.getInventory().setItem(10,ItemStack.EMPTY);
  Class<?> ab=Class.forName("com.tacz.guns.api.item.builder.AmmoItemBuilder");Object a=ab.getMethod("create").invoke(null);call(a,"setId",new Class<?>[]{ResourceLocation.class},ammoId);call(a,"setCount",new Class<?>[]{int.class},phase>=5&&phase<=8?7:29);p.getInventory().setItem(9,(ItemStack)call(a,"build",new Class<?>[0]));
 }
 private void server(ServerTickEvent.Post e){var ps=e.getServer().getPlayerList().getPlayers();if(ps.isEmpty()||complete)return;var p=ps.getFirst();boolean n2=phase==11;
  if(serverPhase!=phase){serverPhase=phase;settled=0;
   p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);p.setNoGravity(true);p.connection.teleport(.5,150,.5,0,0);p.setDeltaMovement(0,0,0);
   if(phase==0)p.getInventory().clearContent();
   p.setItemSlot(EquipmentSlot.HEAD,new ItemStack(n2?ModItems.NANOSUIT_2_HELMET.get():ModItems.NANOGEO_NANOSUIT_HELMET.get()));p.setItemSlot(EquipmentSlot.CHEST,new ItemStack(n2?ModItems.NANOSUIT_2_CHESTPLATE.get():ModItems.NANOGEO_NANOSUIT_CHESTPLATE.get()));p.setItemSlot(EquipmentSlot.LEGS,new ItemStack(n2?ModItems.NANOSUIT_2_LEGGINGS.get():ModItems.NANOGEO_NANOSUIT_LEGGINGS.get()));p.setItemSlot(EquipmentSlot.FEET,new ItemStack(n2?ModItems.NANOSUIT_2_BOOTS.get():ModItems.NANOGEO_NANOSUIT_BOOTS.get()));
   if(phase==0){for(int x=-8;x<=8;x++)for(int z=-4;z<=32;z++)p.serverLevel().setBlockAndUpdate(new BlockPos(x,149,z),Blocks.STONE.defaultBlockState());
    var idle=new Zombie(p.serverLevel());idle.setPos(-8,150,12);var detected=new Skeleton(EntityType.SKELETON,p.serverLevel());detected.setPos(8,150,20);detected.setTarget(p);var searching=new Husk(EntityType.HUSK,p.serverLevel());searching.setPos(0,150,28);searching.setLastHurtByMob(p);for(var m:java.util.List.of(idle,detected,searching)){m.setNoAi(true);m.setPersistenceRequired();p.serverLevel().addFreshEntity(m);}
   }
   try{gun(p,phase==5?"tacz:rpg7":phase>=6&&phase<=8?"tacz:m320":phase==9?"tacz:aa12":"tacz:spas_12",phase>=5&&phase<=8?1:7);}catch(Exception ex){throw new RuntimeException(ex);}
   p.setItemSlot(EquipmentSlot.OFFHAND,phase==7?new ItemStack(Items.SNOWBALL,6):phase==8?new ItemStack(Items.EGG,3):ItemStack.EMPTY);
   if(phase==7){p.getInventory().setItem(27,new ItemStack(Items.EGG,16));p.getInventory().setItem(28,new ItemStack(Items.SNOWBALL,4));}
  }
  p.serverLevel().setDayTime(18000);p.setHealth(phase==2?10:phase==3?5:20);p.getFoodData().setFoodLevel(20);var d=NanosuitCapabilities.get(p);d.setRecoveryTicks(-1);d.setEnergyWithoutStartingRecovery(phase==2||phase==10?50:phase==3?25:phase==4?0:100);d.setMode(NanosuitMode.ARMOR);NanosuitSyncManager.syncToClient(p);settled++;
 }
 private void bindings(){try{var f=NanosuitClientConfig.class.getDeclaredField("CLASSIC_THROWABLE_ITEMS");f.setAccessible(true);@SuppressWarnings("unchecked")var v=(net.neoforged.neoforge.common.ModConfigSpec.ConfigValue<java.util.List<String>>)f.get(null);v.set(java.util.List.of("minecraft:snowball|smoke","minecraft:egg|frag"));}catch(Exception e){throw new RuntimeException(e);}}
 private void render(RenderGuiEvent.Post e){var mc=Minecraft.getInstance();if(complete||mc.player==null||serverPhase!=phase||settled<(phase==0?80:16))return;
  mc.options.chatVisibility().set(net.minecraft.world.entity.player.ChatVisiblity.HIDDEN);
  if(!action){action=true;started=System.nanoTime();if(phase==0)NanosuitClientConfig.setClassicInterfaceColor(ClassicHudPalette.DEFAULT);if(phase==1)NanosuitClientConfig.setClassicInterfaceColor(0x3388cc);if(phase==2)NanosuitClientConfig.setClassicInterfaceColor(0xc59a64);if(phase==7)bindings();if(phase==10){mc.options.guiScale().set(3);mc.resizeDisplay();}return;}
  if(System.nanoTime()-started<1_000_000_000L)return;
  var state=ClassicTaczData.read(mc.player);var throwable=ClassicThrowableHud.read(mc.player);
  ClassicAmmoIcons.Kind expected=phase==5?ClassicAmmoIcons.Kind.ROCKET:phase>=6&&phase<=8?ClassicAmmoIcons.Kind.GRENADE_ROUND:ClassicAmmoIcons.Kind.SHELL;
  boolean pass=state!=null&&state.ammoKind()==expected;
  if(phase<11)pass&=io.github.painworks.crysismod.client.minimap.ClassicMinimapRenderer.isClassic();
  if(phase==0)pass&=state.loaded()==7&&state.reserve()==29&&throwable==null;
  if(phase==7)pass&=throwable!=null&&throwable.kind()==ClassicThrowableHud.Kind.SMOKE&&throwable.count()==10;
  if(phase==8)pass&=throwable!=null&&throwable.kind()==ClassicThrowableHud.Kind.FRAG&&throwable.count()==19;
  if(phase==9)pass&=state.fireMode().equals("AUTO");
  if(phase==11)pass&=!io.github.painworks.crysismod.client.minimap.ClassicMinimapRenderer.isClassic();
  if(!pass)failures++;System.out.println("HUD_DETAILS "+names[phase]+" pass="+pass+" state="+state+" throwable="+throwable+" color="+Integer.toHexString(NanosuitClientConfig.classicInterfaceColor()));
  e.getGuiGraphics().flush();Screenshot.grab(mc.gameDirectory,names[phase]+".png",mc.getMainRenderTarget(),ignored->{});
  phase=Boolean.getBoolean("crysis.hud.qa.cleanMapOnly")?(phase==0?2:names.length):Boolean.getBoolean("crysis.hud.qa.mapOnly")?switch(phase){case 0->1;case 1->2;case 2->10;case 10->11;default->names.length;}:phase+1;
  if(phase==names.length){complete=true;System.out.println("HUD_DETAILS_COMPLETE failures="+failures);mc.stop();}action=false;
 }
}
