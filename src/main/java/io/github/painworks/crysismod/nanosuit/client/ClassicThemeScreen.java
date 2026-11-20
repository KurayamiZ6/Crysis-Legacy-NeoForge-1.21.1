package io.github.painworks.crysismod.nanosuit.client;

import io.github.painworks.crysismod.nanosuit.config.NanosuitClientConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Local editor: preview only until Apply; Cancel/Escape never writes configuration. */
public final class ClassicThemeScreen extends Screen {
   private static final int[] PALETTE={ClassicHudPalette.DEFAULT,0x53b8eb,0x53d3c3,0x9c77df,0xe8e8e8,0xe5b348,0xd16b76,0xa9cc6d};
   private final Screen parent;
   private final List<String> presets=new ArrayList<>(NanosuitClientConfig.classicColorPresets());
   private final ChannelSlider[] channels=new ChannelSlider[3];
   private int color=NanosuitClientConfig.classicInterfaceColor(),selected,left,top,col,right,widthPanel;
   private EditBox hex,name;
   private Button selector,load,delete,save,apply;
   private boolean updating,valid=true;
   private Component status=text("apply_help");
   public static Component text(String key,Object... args){return Component.translatable("screen.crysismod.n1theme."+key,args);}
   public ClassicThemeScreen(Screen parent){super(text("title"));this.parent=parent;}
   @Override protected void init(){
      String oldName=name==null?"":name.getValue();
      widthPanel=Math.min(400,width-24);left=(width-widthPanel)/2;top=Math.max(0,(height-246)/2);
      col=(widthPanel-12)/2;right=left+col+12;
      for(int i=0;i<PALETTE.length;i++){
         int rgb=PALETTE[i],sw=widthPanel/PALETTE.length;
         addRenderableWidget(Button.builder(Component.literal("■").withStyle(s->s.withColor(rgb)),b->setColor(rgb))
            .tooltip(Tooltip.create(Component.literal(String.format(Locale.ROOT,"#%06X",rgb))))
            .bounds(left+i*sw,top+26,sw-3,20).build());
      }
      for(int i=0;i<3;i++){channels[i]=new ChannelSlider(left,top+67+i*24,col,i);addRenderableWidget(channels[i]);}
      hex=new EditBox(font,left,top+139,col,20,text("hex"));hex.setMaxLength(7);hex.setValue(hexValue());
      hex.setResponder(value->{if(!updating){valid=value.matches("#?[0-9a-fA-F]{6}");if(valid)setColor(Integer.parseInt(value.replace("#",""),16));else{status=text("invalid_hex");refresh();}}});addRenderableWidget(hex);
      selector=addRenderableWidget(Button.builder(Component.empty(),b->{selected=(selected+1)%presets.size();refresh();}).bounds(right,top+67,col,20).build());
      load=addRenderableWidget(Button.builder(text("load"),b->{String preset=presets.get(selected);setColor(ClassicHudPalette.parse(preset.substring(preset.lastIndexOf('|')+1)));name.setValue(preset.substring(0,preset.lastIndexOf('|')));}).bounds(right,top+91,col/2-2,20).build());
      delete=addRenderableWidget(Button.builder(text("delete"),b->{presets.remove(selected);selected=0;refresh();}).bounds(right+col/2+2,top+91,col/2-2,20).build());
      name=new EditBox(font,right,top+115,col,20,text("name"));name.setMaxLength(24);name.setHint(text("name"));name.setValue(oldName);name.setResponder(v->refresh());addRenderableWidget(name);
      save=addRenderableWidget(Button.builder(text("save"),b->savePreset()).bounds(right,top+139,col,20).build());
      addRenderableWidget(Button.builder(text("default"),b->setColor(ClassicHudPalette.DEFAULT)).bounds(right,top+175,col,20).build());
      apply=addRenderableWidget(Button.builder(text("apply"),b->{NanosuitClientConfig.setClassicInterfaceColor(color);NanosuitClientConfig.setClassicColorPresets(presets);onClose();}).bounds(left,top+219,col,20).build());
      addRenderableWidget(Button.builder(Component.translatable("gui.cancel"),b->onClose()).bounds(right,top+219,col,20).build());
      setColor(color);refresh();
   }
   private String hexValue(){return String.format(Locale.ROOT,"#%06X",color);}
   private void setColor(int rgb){
      color=rgb&0xffffff;valid=true;updating=true;
      if(hex!=null)hex.setValue(hexValue());
      for(var channel:channels)if(channel!=null)channel.refresh();
      updating=false;status=text("apply_help");refresh();
   }
   private void refresh(){
      if(selector==null||save==null||apply==null)return;
      selector.active=load.active=delete.active=!presets.isEmpty();
      selector.setMessage(presets.isEmpty()?text("none"):Component.literal("‹ "+presets.get(Math.min(selected,presets.size()-1)).split("\\|",2)[0]+" ›"));
      String label=name.getValue().trim();
      save.active=valid&&!label.isEmpty()&&!label.contains("|");apply.active=valid;
   }
   private void savePreset(){
      String label=name.getValue().trim();int found=-1;
      for(int i=0;i<presets.size();i++)if(presets.get(i).split("\\|",2)[0].equalsIgnoreCase(label)){found=i;break;}
      if(found<0&&presets.size()>=16){status=text("full");return;}
      String entry=label+"|"+hexValue();
      if(found>=0){presets.set(found,entry);selected=found;}else{presets.add(entry);selected=presets.size()-1;}
      status=text("saved");refresh();
   }
   @Override public void render(GuiGraphics g,int mouseX,int mouseY,float partial){
      renderBackground(g,mouseX,mouseY,partial);g.fill(left-8,top,left+widthPanel+8,top+245,0xde101719);
      super.render(g,mouseX,mouseY,partial);
      g.drawCenteredString(font,title,width/2,top+8,0xffffff);
      g.drawString(font,text("color"),left,top+54,0xffffff);g.drawString(font,text("presets"),right,top+54,0xffffff);
      g.drawString(font,text("preview"),left,top+163,0xb0bdb8);
      int dark=((color>>16&255)/8)<<16|((color>>8&255)/8)<<8|(color&255)/8;
      g.fill(left,top+175,left+col,top+204,0xff000000|color);
      g.fill(left+1,top+176,left+col-1,top+203,0xff000000|dark);
      g.drawCenteredString(font,Component.literal("NANOSUIT 1"),left+col/2,top+185,color);
      g.drawString(font,status,left,top+207,valid?0xb8c9bf:0xff7070);
   }
   @Override public void onClose(){minecraft.setScreen(parent);}
   private final class ChannelSlider extends AbstractSliderButton {
      private final int channel;
      ChannelSlider(int x,int y,int w,int channel){super(x,y,w,20,Component.empty(),(color>>(16-channel*8)&255)/255.0);this.channel=channel;updateMessage();}
      void refresh(){value=(color>>(16-channel*8)&255)/255.0;updateMessage();}
      @Override protected void updateMessage(){setMessage(text(new String[]{"red","green","blue"}[channel],Math.round(value*255)));}
      @Override protected void applyValue(){if(!updating){int shift=16-channel*8;setColor((color&~(255<<shift))|((int)Math.round(value*255)<<shift));}}
   }
}
