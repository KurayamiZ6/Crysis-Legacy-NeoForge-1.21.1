package local.crysisqa;
import io.github.painworks.crysismod.registry.ModItems;
import io.github.painworks.crysismod.nanosuit.client.ClassicWeaponHud;
import io.github.painworks.crysismod.nanovisor.client.*;
import net.minecraft.client.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.client.event.*;
@Mod("crysismeleehudqa")
public final class MeleeHudQa {
 private final String[] names={"fists","sword","axe","mace","trident","component-weapon","block","bow","crossbow","binoculars-melee","n2"};
 private volatile int phase,serverPhase=-1,settled;private int timeout,failures;private boolean action,done;private long start;
 public MeleeHudQa(){NeoForge.EVENT_BUS.addListener(this::server);NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,this::client);NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,this::render);}
 private void server(ServerTickEvent.Post e){var ps=e.getServer().getPlayerList().getPlayers();if(ps.isEmpty()||phase>=names.length)return;var p=ps.getFirst();if(serverPhase!=phase){serverPhase=phase;settled=0;boolean n2=phase==10;
  p.setItemSlot(EquipmentSlot.HEAD,new ItemStack(n2?ModItems.NANOSUIT_2_HELMET.get():ModItems.NANOGEO_NANOSUIT_HELMET.get()));p.setItemSlot(EquipmentSlot.CHEST,new ItemStack(n2?ModItems.NANOSUIT_2_CHESTPLATE.get():ModItems.NANOGEO_NANOSUIT_CHESTPLATE.get()));p.setItemSlot(EquipmentSlot.LEGS,new ItemStack(n2?ModItems.NANOSUIT_2_LEGGINGS.get():ModItems.NANOGEO_NANOSUIT_LEGGINGS.get()));p.setItemSlot(EquipmentSlot.FEET,new ItemStack(n2?ModItems.NANOSUIT_2_BOOTS.get():ModItems.NANOGEO_NANOSUIT_BOOTS.get()));
  Item item=switch(phase){case 1,9->Items.IRON_SWORD;case 2->Items.IRON_AXE;case 3->Items.MACE;case 4->Items.TRIDENT;case 5->Items.STICK;case 6->Items.STONE;case 7->Items.BOW;case 8->Items.CROSSBOW;default->Items.AIR;};ItemStack stack=item==Items.AIR?ItemStack.EMPTY:new ItemStack(item);
  if(phase==5)stack.set(DataComponents.ATTRIBUTE_MODIFIERS,ItemAttributeModifiers.builder().add(Attributes.ATTACK_DAMAGE,new AttributeModifier(ResourceLocation.parse("crysismeleehudqa:spear_damage"),5,AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND).build());
  p.getInventory().setItem(p.getInventory().selected,stack);p.connection.teleport(.5,150,.5,0,0);p.setNoGravity(true);
 }p.serverLevel().setDayTime(6000);settled++;}
 private void client(ClientTickEvent.Post e){var mc=Minecraft.getInstance();if(++timeout>1800){System.out.println("MELEE_HUD_TIMEOUT");mc.stop();}if(mc.player==null||phase>=names.length||serverPhase!=phase||settled<(phase==0?60:12))return;mc.player.setNoGravity(true);mc.player.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);mc.player.setYRot(0);mc.player.setXRot(0);mc.options.chatVisibility().set(net.minecraft.world.entity.player.ChatVisiblity.HIDDEN);for(var k:new KeyMapping[]{mc.options.keyAttack,mc.options.keyUse,mc.options.keyUp,mc.options.keyDown,mc.options.keyJump})k.setDown(false);if(!action){action=true;start=System.nanoTime();if(phase==9)KeyMapping.click(NanoVisorKeyMappings.TOGGLE_NANO_VISOR.getKey());}}
 private void render(RenderGuiEvent.Post e){var mc=Minecraft.getInstance();if(done||mc.player==null)return;if(phase>=names.length){done=true;System.out.println("MELEE_HUD_COMPLETE failures="+failures);mc.stop();return;}if(!action||System.nanoTime()-start<700_000_000L)return;
  boolean melee=ClassicWeaponHud.isMeleeItem(mc.player.getMainHandItem()),expected=phase<=5||phase==9||phase==10;if(melee!=expected||phase==9&&!ClassicWeaponHud.optic()||phase==10&&ClassicWeaponHud.optic())failures++;
  System.out.println("MELEE_HUD "+names[phase]+" melee="+melee+" optic="+ClassicWeaponHud.optic());
  if(phase==0||phase==1||phase==5||phase==7||phase==9||phase==10){e.getGuiGraphics().flush();Screenshot.grab(mc.gameDirectory,names[phase]+".png",mc.getMainRenderTarget(),ignored->{});}phase++;action=false;
 }
}
