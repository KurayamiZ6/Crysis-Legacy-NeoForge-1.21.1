package io.github.painworks.crysismod.nanosuit.client;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.painworks.crysismod.client.minimap.ClassicMinimapRenderer;
import io.github.painworks.crysismod.nanovisor.client.ClassicBinoculars;
import io.github.painworks.crysismod.nanosuit.config.NanosuitClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/** N1 ammo frame aligned to the already approved status panel. */
@EventBusSubscriber(modid="crysismod",value=Dist.CLIENT)
public final class ClassicWeaponHud {
   private static final ResourceLocation FRAME=asset("frame"),BULLET=asset("bullet"),INFINITY=asset("infinity"),STRIP=asset("strip");
   private static final ResourceLocation AUTO=asset("auto"),BURST=asset("burst");
   private static java.lang.ref.WeakReference<Object> level=new java.lang.ref.WeakReference<>(null);
   private static ResourceLocation lastGun,previousIcon;private static long switchedAt;
   private ClassicWeaponHud(){}
   private static ResourceLocation asset(String name){return ResourceLocation.fromNamespaceAndPath("crysismod","textures/gui/classic_weapon/"+name+".png");}
   public static boolean canReplace(){var mc=Minecraft.getInstance();return ClassicMinimapRenderer.isClassic()&&!mc.options.hideGui&&NanosuitHudOverlay.isHudEnabled()
      &&NanosuitClientConfig.taczHudOverrideEnabled()&&NanosuitClientConfig.taczHideOriginalHud();}
   public static boolean optic(){return ClassicBinoculars.progress()>0;}
   /** Melee convention (0/0/infinity), not a real ammunition inventory. Never classify a known gun as melee. */
   public static boolean isMeleeItem(net.minecraft.world.item.ItemStack stack){
      if(stack.isEmpty())return true;
      if(ClassicTaczData.isGun(stack))return false;
      var item=stack.getItem();
      if(item instanceof net.minecraft.world.item.SwordItem || item instanceof net.minecraft.world.item.AxeItem
         || item instanceof net.minecraft.world.item.MaceItem || item instanceof net.minecraft.world.item.TridentItem)return true;
      if(item instanceof net.minecraft.world.item.ProjectileWeaponItem || stack.is(net.neoforged.neoforge.common.Tags.Items.RANGED_WEAPON_TOOLS))return false;
      if(stack.is(net.neoforged.neoforge.common.Tags.Items.MELEE_WEAPON_TOOLS)
         || stack.is(net.neoforged.neoforge.common.Tags.Items.TOOLS_SPEAR) || stack.is(net.neoforged.neoforge.common.Tags.Items.TOOLS_MACE))return true;
      boolean[] damage={false};
      stack.forEachModifier(net.minecraft.world.entity.EquipmentSlot.MAINHAND,(attribute,modifier)->{
         if(attribute.is(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE)&&modifier.amount()>0)damage[0]=true;
      });
      return damage[0];
   }
   @SubscribeEvent(priority=EventPriority.LOW) public static void render(RenderGuiEvent.Post event){
      var mc=Minecraft.getInstance();if(!ClassicMinimapRenderer.isClassic()){level.clear();lastGun=null;previousIcon=null;return;}
      if(mc.options.hideGui||!NanosuitHudOverlay.isHudEnabled())return;
      var state=canReplace()?ClassicTaczData.read(mc.player):null;
      boolean melee=state==null&&isMeleeItem(mc.player.getMainHandItem());
      if(state==null&&!melee&&!optic())return;
      GuiGraphics g=event.getGuiGraphics();float base=g.guiHeight()/1080f,s=base*.7f*NanosuitClientConfig.classicHudScale();
      float x=g.guiWidth()-129*base-440*s,y=g.guiHeight()-24*base-255*s;
      int rgb=NanosuitClientConfig.classicInterfaceColor();long now=System.nanoTime();
      if(level.get()!=mc.level){level=new java.lang.ref.WeakReference<>(mc.level);lastGun=state==null?null:state.gunId();previousIcon=state==null?null:state.icon();switchedAt=0;}
      if(state!=null&&!state.gunId().equals(lastGun)){lastGun=state.gunId();switchedAt=now;}
      g.pose().pushPose();g.pose().translate(x,y,0);g.pose().scale(s,s,1);
      layer(g,FRAME,0,0,440,125,rgb,1);
      if(melee&&!optic()){
         int textColor=rgb==ClassicHudPalette.DEFAULT?0xc0efa6:rgb;
         g.pose().pushPose();g.pose().mulPose(new org.joml.Matrix4f().m01(.045f));
         for(int i=0;i<3;i++)ClassicHudText.draw(g,"0",149+i*17,39,35,textColor,i<2?.3f:1,false);
         ClassicHudText.draw(g,"0",234,30,27,textColor,1,false);
         g.pose().popPose();layer(g,INFINITY,286,14,68,34,rgb,1);
      }
      if(state!=null&&!optic()){
         int textColor=rgb==ClassicHudPalette.DEFAULT?0xc0efa6:rgb;
         g.pose().pushPose();g.pose().mulPose(new org.joml.Matrix4f().m01(.045f));
         String loaded=String.format(java.util.Locale.ROOT,"%03d",state.loaded());
         for(int i=0;i<loaded.length();i++)ClassicHudText.draw(g,loaded.substring(i,i+1),149+i*17,39,35,textColor,leading(loaded,i)?.3f:1,false);
         if(state.unlimited())layer(g,INFINITY,224,8,45,34,rgb,1);
         else ClassicHudText.draw(g,Integer.toString(state.reserve()),234,30,27,textColor,1,false);
         g.pose().popPose();
         switch(state.fireMode()){
            case "SEMI" -> layer(g,BULLET,307,7,18,48,rgb,1);
            case "AUTO" -> layer(g,AUTO,294,7,60,48,rgb,1);
            case "BURST" -> layer(g,BURST,294,7,60,48,rgb,1);
            default -> {} // Unknown fire selector data is not presented as semi-automatic.
         }
         if(switchedAt!=0&&now-switchedAt<1_000_000_000L){
            float alpha=Math.min(1,(1_000_000_000L-(now-switchedAt))/200_000_000f);
            layer(g,STRIP,130,-121,230,125,rgb,alpha);
            if(state.icon()!=null)layer(g,state.icon(),245,-106,90,30,rgb,alpha);
            if(previousIcon!=null)layer(g,previousIcon,245,-39,90,30,rgb,alpha);
         }else previousIcon=state.icon();
      }
      g.pose().popPose();g.setColor(1,1,1,1);
   }
   private static boolean leading(String value,int index){for(int i=0;i<=index;i++)if(value.charAt(i)!='0')return false;return index<value.length()-1;}
   private static void layer(GuiGraphics g,ResourceLocation texture,int x,int y,int w,int h,int rgb,float a){
      g.setColor((rgb>>16&255)/255f,(rgb>>8&255)/255f,(rgb&255)/255f,a);RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();RenderSystem.disableDepthTest();
      g.blit(texture,x,y,w,h,0,0,w*2,h*2,w*2,h*2);g.setColor(1,1,1,1);
   }
}
