package io.github.painworks.crysismod.nanosuit.client;

import java.util.Optional;
import java.lang.reflect.Method;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

/** Optional public API adapter. Unknown or unsupported weapons keep their native HUD. */
public final class ClassicTaczData {
   public record State(int loaded, int reserve, boolean unlimited, String fireMode, ResourceLocation gunId, ResourceLocation icon) {}
   private static Class<?> gunApi,ammoApi,boxApi,timelessApi;
   private static boolean initialized,available;
   private static final java.util.Map<String,Method> METHODS=new java.util.HashMap<>();
   private static java.lang.ref.WeakReference<LocalPlayer> cachedPlayer=new java.lang.ref.WeakReference<>(null);
   private static ItemStack cachedStack;
   private static State cached;
   private static long readAt;
   private ClassicTaczData(){}
   public static State read(LocalPlayer player) {
      if(!initialized){initialized=true;if(ModList.get().isLoaded("tacz"))try{
         gunApi=Class.forName("com.tacz.guns.api.item.IGun");ammoApi=Class.forName("com.tacz.guns.api.item.IAmmo");
         boxApi=Class.forName("com.tacz.guns.api.item.IAmmoBox");timelessApi=Class.forName("com.tacz.guns.api.TimelessAPI");available=true;
      }catch(ReflectiveOperationException|LinkageError ignored){}}
      if(!available)return null;
      ItemStack stack=player.getMainHandItem();long now=System.nanoTime();
      if(player==cachedPlayer.get() && stack==cachedStack && now-readAt<50_000_000L)return cached;
      cachedPlayer=new java.lang.ref.WeakReference<>(player);cachedStack=stack;readAt=now;cached=null;
      try{
         Object gun=call(gunApi,"getIGunOrNull",null,new Class<?>[]{ItemStack.class},stack);
         if(gun==null || bool(gunApi,"useInventoryAmmo",gun,stack) || bool(gunApi,"hasHeatData",gun,stack))return null;
         ResourceLocation id=(ResourceLocation)call(gunApi,"getGunId",gun,new Class<?>[]{ItemStack.class},stack);
         var index=(Optional<?>)call(timelessApi,"getCommonGunIndex",null,new Class<?>[]{ResourceLocation.class},id);
         if(index.isEmpty())return null;
         Class<?> indexApi=Class.forName("com.tacz.guns.resource.index.CommonGunIndex");
         Object data=call(indexApi,"getGunData",index.get(),new Class<?>[0]);
         Object bolt=call(data.getClass(),"getBolt",data,new Class<?>[0]);
         int loaded=number(gunApi,"getCurrentAmmoCount",gun,stack);
         if(bool(gunApi,"hasBulletInBarrel",gun,stack) && !bolt.toString().equals("OPEN_BOLT"))loaded++;
         int reserve=0;boolean unlimited=player.getAbilities().instabuild;
         if(bool(gunApi,"useDummyAmmo",gun,stack)){
            reserve=number(gunApi,"getDummyAmmoAmount",gun,stack);
         }else for(int i=0;i<player.getInventory().getContainerSize();i++){
            ItemStack ammo=player.getInventory().getItem(i);if(ammo.isEmpty())continue;
            Object item=ammo.getItem();
            if(ammoApi.isInstance(item) && (Boolean)call(ammoApi,"isAmmoOfGun",item,new Class<?>[]{ItemStack.class,ItemStack.class},stack,ammo))reserve=Math.addExact(reserve,ammo.getCount());
            if(boxApi.isInstance(item)){
               if(bool(boxApi,"isAllTypeCreative",item,ammo)){unlimited=true;continue;}
               if((Boolean)call(boxApi,"isAmmoBoxOfGun",item,new Class<?>[]{ItemStack.class,ItemStack.class},stack,ammo)){
                  unlimited|=bool(boxApi,"isCreative",item,ammo);reserve=Math.addExact(reserve,number(boxApi,"getAmmoCount",item,ammo));
               }
            }
         }
         ResourceLocation icon=null;
         var display=(Optional<?>)call(timelessApi,"getGunDisplay",null,new Class<?>[]{ItemStack.class},stack);
         if(display.isPresent())icon=(ResourceLocation)call(Class.forName("com.tacz.guns.client.resource.GunDisplayInstance"),"getHUDTexture",display.get(),new Class<?>[0]);
         String fireMode=((Enum<?>)call(gunApi,"getFireMode",gun,new Class<?>[]{ItemStack.class},stack)).name();
         cached=new State(Math.max(0,loaded),Math.max(0,reserve),unlimited,fireMode,id,icon);
      }catch(ReflectiveOperationException|LinkageError|RuntimeException unsupported){cached=null;}
      return cached;
   }
   private static Object call(Class<?> owner,String name,Object receiver,Class<?>[] types,Object...args)throws ReflectiveOperationException{
      String key=owner.getName()+"#"+name;Method method=METHODS.get(key);
      if(method==null){method=owner.getMethod(name,types);METHODS.put(key,method);}
      return method.invoke(receiver,args);
   }
   private static boolean bool(Class<?> owner,String name,Object receiver,ItemStack stack)throws ReflectiveOperationException{return (Boolean)call(owner,name,receiver,new Class<?>[]{ItemStack.class},stack);}
   private static int number(Class<?> owner,String name,Object receiver,ItemStack stack)throws ReflectiveOperationException{return ((Number)call(owner,name,receiver,new Class<?>[]{ItemStack.class},stack)).intValue();}
}
