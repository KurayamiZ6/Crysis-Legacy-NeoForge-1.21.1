package io.github.painworks.crysismod.client.minimap;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.commands.Commands;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.api.distmarker.Dist;

/** One explicitly followed client-session goal. Never populated from nearby entities. */
@EventBusSubscriber(modid="crysismod",value=Dist.CLIENT)
public final class ClassicWaypoint {
   public record Goal(ResourceKey<Level> dimension,double x,double z){}
   private static Goal followed;
   private static Object session;
   private ClassicWaypoint(){}
   public static void follow(double x,double z){var mc=Minecraft.getInstance();if(mc.level!=null){session=mc.getConnection();followed=new Goal(mc.level.dimension(),x,z);}}
   public static void clear(){followed=null;session=null;}
   @SubscribeEvent public static void disconnected(net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut event){clear();}
   public static Goal current(){var mc=Minecraft.getInstance();if(mc.getConnection()!=session){clear();return null;}return followed!=null&&mc.level!=null&&followed.dimension().equals(mc.level.dimension())?followed:null;}
   @SubscribeEvent public static void commands(RegisterClientCommandsEvent event){
      event.getDispatcher().register(Commands.literal("crysiswaypoint")
         .then(Commands.literal("clear").executes(c->{clear();return 1;}))
         .then(Commands.literal("follow").then(Commands.argument("x",DoubleArgumentType.doubleArg(-30_000_000,30_000_000))
            .then(Commands.argument("z",DoubleArgumentType.doubleArg(-30_000_000,30_000_000)).executes(c->{follow(DoubleArgumentType.getDouble(c,"x"),DoubleArgumentType.getDouble(c,"z"));return 1;})))));
   }
}
