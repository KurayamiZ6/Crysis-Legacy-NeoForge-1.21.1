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
   private static final ResourceLocation FRAME_OUTLINE=asset("frame-outline"),STRIP_OUTLINE=asset("strip-outline");
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
      if(ClassicMalfunction.active()){
         renderMalfunction(event.getGuiGraphics());
         return;
      }
      var state=canReplace()?ClassicTaczData.read(mc.player):null;
      var throwable=canReplace()?ClassicThrowableHud.read(mc.player):null;
      // An unreadable TACZ gun must retain its native frame, even when grenades are present.
      if(state==null&&ClassicTaczData.isGun(mc.player.getMainHandItem())&&!optic())return;
      boolean melee=state==null&&ClassicThrowableHud.classify(mc.player.getMainHandItem())==null&&isMeleeItem(mc.player.getMainHandItem());
      if(state==null&&throwable==null&&!melee&&!optic())return;
      GuiGraphics g=event.getGuiGraphics();float base=g.guiHeight()/1080f,s=base*.7f*NanosuitClientConfig.classicHudScale();
      float x=g.guiWidth()-129*base-440*s,y=g.guiHeight()-24*base-255*s;
      int rgb=NanosuitClientConfig.classicInterfaceColor();long now=System.nanoTime();
      if(level.get()!=mc.level){level=new java.lang.ref.WeakReference<>(mc.level);lastGun=state==null?null:state.gunId();previousIcon=state==null?null:state.icon();switchedAt=0;}
      if(state!=null&&!state.gunId().equals(lastGun)){lastGun=state.gunId();switchedAt=now;}
      g.pose().pushPose();g.pose().translate(x,y,0);g.pose().scale(s,s,1);
      layer(g,FRAME,0,0,440,125,0xffffff,1);
      layer(g,FRAME_OUTLINE,0,0,440,125,rgb,1);
      if(!optic()){
         g.pose().pushPose();
         ClassicHudText.alignWithPanel(g,0);
         int textColor=ClassicHudPalette.textColor(rgb);
         if(melee||state!=null){
            boolean inventoryFed=!melee&&state.inventoryFed();
            if(inventoryFed&&state.unlimited())layer(g,INFINITY,155,7,58,32,rgb,1);
            else ClassicHudText.digits(g,melee?0:state.loaded(),211,4,39,73,ClassicHudPalette.loadedAmmoColor(rgb),3);
            if(!inventoryFed){
               if(!melee&&state.unlimited())layer(g,INFINITY,230,3,45,26,rgb,1);
               else ClassicHudText.digits(g,melee?0:state.reserve(),274,2,29,59,textColor,0);
            }
            if(melee)layer(g,INFINITY,288,8,68,34,rgb,1);
            else renderFireMode(g,state,rgb);
         }
         if(throwable!=null){
            ClassicHudText.digits(g,throwable.count(),274,62,39,125,textColor,0);
            layer(g,asset("throwable-"+throwable.kind().name().toLowerCase(java.util.Locale.ROOT)),302,61,40,40,rgb,1);
         }
         g.pose().popPose();
      }
      if(state!=null&&!optic()){
         if(switchedAt!=0&&now-switchedAt<1_000_000_000L){
            float alpha=Math.min(1,(1_000_000_000L-(now-switchedAt))/200_000_000f);
            layer(g,STRIP,130,-121,230,125,0xffffff,alpha);
            layer(g,STRIP_OUTLINE,130,-121,230,125,rgb,alpha);
            if(state.icon()!=null)layer(g,state.icon(),245,-106,90,30,rgb,alpha);
            if(previousIcon!=null)layer(g,previousIcon,245,-39,90,30,rgb,alpha);
         }else previousIcon=state.icon();
      }
      g.pose().popPose();g.setColor(1,1,1,1);
   }
   private static void renderMalfunction(GuiGraphics g){
      float base=g.guiHeight()/1080f,s=base*.7f*NanosuitClientConfig.classicHudScale();
      g.pose().pushPose();
      try {
         g.pose().translate(g.guiWidth()-129*base-440*s,g.guiHeight()-24*base-255*s,0);
         g.pose().scale(s,s,1);
         layer(g,FRAME,0,0,440,125,0xffffff,1);
         layer(g,FRAME_OUTLINE,0,0,440,125,ClassicMalfunction.RED,1);
         ClassicHudText.alignWithPanel(g,0);
         ClassicHudText.draw(g,"MALFUNCTION",152,32,20,ClassicMalfunction.labelColor(0),1,false);
         ClassicHudText.draw(g,"MALFUNCTION",152,88,20,ClassicMalfunction.labelColor(420),1,false);
      } finally {g.pose().popPose();}
      var mc=Minecraft.getInstance();
      var message=net.minecraft.network.chat.Component.translatable("hud.crysismod.n1.malfunction");
      int x=g.guiWidth()/2,y=Math.round(g.guiHeight()*.89f);
      int half=mc.font.width(message)/2;
      g.fill(x-half-5,y-3,x+half+5,y+11,0x80202020);
      g.drawCenteredString(mc.font,message,x,y,0xfff2f3);
   }

   private static void renderFireMode(GuiGraphics g,ClassicTaczData.State state,int rgb){
      ResourceLocation bulletSymbol=switch(state.fireMode()){
         case "SEMI" -> BULLET;
         case "AUTO" -> AUTO;
         case "BURST" -> BURST;
         default -> null;
      };
      if(bulletSymbol==null)return;
      ResourceLocation symbol=state.ammoKind()==ClassicAmmoIcons.Kind.BULLET?bulletSymbol:
         asset(state.ammoKind().name().toLowerCase(java.util.Locale.ROOT)+"-"+state.fireMode().toLowerCase(java.util.Locale.ROOT));
      layer(g,symbol,292,1,60,48,rgb,1);
   }
   private static void layer(GuiGraphics g,ResourceLocation texture,int x,int y,int w,int h,int rgb,float a){
      g.setColor((rgb>>16&255)/255f,(rgb>>8&255)/255f,(rgb&255)/255f,a);RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();RenderSystem.disableDepthTest();
      int sourceWidth=texture==INFINITY?90:w*2;
      int sourceHeight=texture==INFINITY?68:h*2;
      g.blit(texture,x,y,w,h,0,0,sourceWidth,sourceHeight,sourceWidth,sourceHeight);
      g.setColor(1,1,1,1);
   }
}
