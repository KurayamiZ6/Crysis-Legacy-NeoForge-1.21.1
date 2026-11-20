package io.github.painworks.crysismod.nanosuit.client;

import io.github.painworks.crysismod.nanosuit.config.NanosuitClientConfig;
import java.lang.ref.WeakReference;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.player.Player;

/** Optional display adapter, not a grenade mechanic. No item-name or translated-name guessing. */
public final class ClassicThrowableHud {
   public enum Kind { FRAG, SMOKE, FLASH, INCENDIARY }
   public record State(Kind kind,int count){}
   private static WeakReference<Player> owner=new WeakReference<>(null);
   private static WeakReference<Object> level=new WeakReference<>(null);
   private static Kind selected;
   private static State cached;
   private static long readAt;
   private ClassicThrowableHud(){}
   public static Kind classify(ItemStack stack){
      if(stack.isEmpty())return null;
      String id=BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
      for(String entry:NanosuitClientConfig.classicThrowableItems()){
         String[] pair=entry.split("\\|",2);
         if(pair.length==2&&pair[0].equals(id))return Kind.valueOf(pair[1].toUpperCase(java.util.Locale.ROOT));
      }
      for(Kind kind:Kind.values())if(stack.is(TagKey.create(Registries.ITEM,ResourceLocation.fromNamespaceAndPath("crysismod","hud/throwables/"+kind.name().toLowerCase(java.util.Locale.ROOT)))))return kind;
      return null;
   }
   public static State read(Player player){
      long now=System.nanoTime();
      if(owner.get()==player&&level.get()==player.level()&&now-readAt<50_000_000L)return cached;
      if(owner.get()!=player||level.get()!=player.level()){owner=new WeakReference<>(player);level=new WeakReference<>(player.level());selected=null;}
      readAt=now;
      Kind held=classify(player.getMainHandItem());if(held==null)held=classify(player.getOffhandItem());
      int[] counts=new int[Kind.values().length];
      for(int i=0;i<player.getInventory().getContainerSize();i++){
         ItemStack stack=player.getInventory().getItem(i);Kind kind=classify(stack);
         if(kind!=null)counts[kind.ordinal()]+=stack.getCount();
      }
      if(held!=null)selected=held;
      if(selected==null||counts[selected.ordinal()]==0){selected=null;for(int i=0;i<9;i++){Kind kind=classify(player.getInventory().getItem(i));if(kind!=null){selected=kind;break;}}}
      if(selected==null)for(Kind kind:Kind.values())if(counts[kind.ordinal()]>0){selected=kind;break;}
      return cached=selected==null?null:new State(selected,counts[selected.ordinal()]);
   }
}
