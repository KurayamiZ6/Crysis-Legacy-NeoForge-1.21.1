package io.github.painworks.crysismod.nanosuit.client;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.painworks.crysismod.client.minimap.*;
import io.github.painworks.crysismod.client.minimap.render.ThreatGauge;
import io.github.painworks.crysismod.nanosuit.config.NanosuitClientConfig;
import io.github.painworks.crysismod.nanosuit.threat.ThreatLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Presentation of server evidence; no client entity searches or AI changes. */
@EventBusSubscriber(modid="crysismod",value=Dist.CLIENT)
public final class ClassicThreatIndicator {
   private static final ResourceLocation TRIANGLE=ResourceLocation.fromNamespaceAndPath("crysismod","textures/gui/classic_map/threat.png");
   private static Object level,player;
   private static ThreatLevel previous=ThreatLevel.CALM;
   private static long lostAt;
   private ClassicThreatIndicator(){}
   @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
      var mc=Minecraft.getInstance();
      if(!ClassicMinimapRenderer.isClassic() || mc.player!=player || mc.level!=level){
         previous=ThreatLevel.CALM;lostAt=0;player=mc.player;level=mc.level;
      }
      if(!ClassicMinimapRenderer.isClassic() || mc.player==null)return;
      ThreatLevel current=ThreatGauge.currentClassic(mc.player);
      if(previous==ThreatLevel.DETECTED && current!=ThreatLevel.DETECTED)lostAt=System.nanoTime();
      previous=current;
   }
   @SubscribeEvent(priority=EventPriority.LOW) public static void render(RenderGuiEvent.Post event) {
      var mc=Minecraft.getInstance();
      if(!ClassicMinimapRenderer.isClassic() || !NanoMinimapVisibility.shouldRender(mc) || !mc.player.isAlive() || mc.player.isSpectator())return;
      var g=event.getGuiGraphics();var v=ClassicMinimapRenderer.viewport(g.guiWidth(),g.guiHeight());float s=v.width()/232f;
      ThreatLevel threat=ThreatGauge.currentClassic(mc.player);long now=System.nanoTime();
      double sinceLoss=lostAt==0?10:(now-lostAt)/1e9;
      boolean detected=threat==ThreatLevel.DETECTED;
      boolean searching=threat==ThreatLevel.SEARCHING || threat==ThreatLevel.SUSPICIOUS;
      int rgb=detected?0xed3029:searching?(sinceLoss<.5?0xff670c:0xe8d653):NanosuitClientConfig.classicInterfaceColor();
      float scale=detected?1.75f:searching?(float)(1.1+.65*Math.max(0,1-(sinceLoss-.5)/.5)):1.1f;
      scale=Math.min(1.75f,scale);
      float alpha=detected?(float)(.68+.32*(.5+.5*Math.sin(now/1e9*Math.PI*5))):1;
      g.pose().pushPose();g.pose().translate(v.x(),v.y(),0);g.pose().scale(s,s,1);
      // The global alert bar remains red during residual alert, independently of the visibility triangle.
      for(int i=0;i<4;i++)g.fill(3,17+i*24,15,38+i*24,(detected?0xe6:searching?0xc0:0x35)<<24|(detected||searching?0xe54c38:NanosuitClientConfig.classicInterfaceColor()));
      g.flush();g.pose().translate(-1,150,0);g.pose().scale(scale,scale,1);
      g.setColor((rgb>>16&255)/255f,(rgb>>8&255)/255f,(rgb&255)/255f,alpha);
      RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();RenderSystem.disableDepthTest();
      g.blit(TRIANGLE,-12,-12,24,24,0,0,96,96,96,96);
      g.pose().popPose();g.setColor(1,1,1,1);
   }
}

