package io.github.painworks.crysismod.nanosuit.client;

import io.github.painworks.crysismod.nanovisor.client.NanoVisorConfigScreen;
import io.github.painworks.crysisscopeadspatch.config.LegacySpectrumConfigScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Discoverable entry points from the standard Mods > Configure button. */
public final class CrysisSettingsScreen extends Screen {
   private final Screen parent;
   public CrysisSettingsScreen(Screen parent){super(Component.literal("Crysis Legacy"));this.parent=parent;}
   @Override protected void init(){
      int w=Math.min(330,width-24),x=(width-w)/2,y=height/2-52;
      addRenderableWidget(Button.builder(ClassicThemeScreen.text("title"),b->minecraft.setScreen(new ClassicThemeScreen(this))).bounds(x,y,w,24).build());
      addRenderableWidget(Button.builder(ClassicThemeScreen.text("all_settings"),b->minecraft.setScreen(NanoVisorConfigScreen.create(this))).bounds(x,y+32,w,24).build());
      addRenderableWidget(Button.builder(ClassicThemeScreen.text("thermal_audio"),b->minecraft.setScreen(LegacySpectrumConfigScreen.create(this))).bounds(x,y+64,w,24).build());
      addRenderableWidget(Button.builder(Component.translatable("gui.done"),b->onClose()).bounds(x,y+100,w,20).build());
   }
   @Override public void render(GuiGraphics g,int mouseX,int mouseY,float partial){
      renderBackground(g,mouseX,mouseY,partial);super.render(g,mouseX,mouseY,partial);
      g.drawCenteredString(font,title,width/2,height/2-80,0xffffff);
   }
   @Override public void onClose(){minecraft.setScreen(parent);}
}
