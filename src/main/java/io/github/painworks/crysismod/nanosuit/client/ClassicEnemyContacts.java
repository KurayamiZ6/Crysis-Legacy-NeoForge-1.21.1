package io.github.painworks.crysismod.nanosuit.client;

import io.github.painworks.crysismod.nanosuit.NanosuitProfile;
import io.github.painworks.crysismod.nanosuit.network.SyncClassicThreatsPacket.Contact;
import io.github.painworks.crysismod.nanosuit.threat.ThreatLevel;
import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import io.github.painworks.crysismod.nanovisor.client.ClassicBinoculars;
import java.lang.ref.WeakReference;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/** One color source for the N1 map and binoculars. No entity flags, teams or potion mutation. */
@net.neoforged.fml.common.EventBusSubscriber(modid="crysismod",value=net.neoforged.api.distmarker.Dist.CLIENT)
public final class ClassicEnemyContacts {
   private record Seen(Contact contact, int lostAt) {}
   private static final Map<Integer,Seen> contacts = new HashMap<>();
   private static final Set<Integer> visible = new HashSet<>();
   private static WeakReference<Player> owner = new WeakReference<>(null);
   private static Object level;
   private static int receivedAt;
   private ClassicEnemyContacts() {}
   @net.neoforged.bus.api.SubscribeEvent
   public static void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
      if(!active()){contacts.clear();visible.clear();level=null;owner.clear();}
   }
   public static void update(Player player, List<Contact> snapshot) {
      if (player==null) return;
      if (owner.get()!=player || level!=player.level()) contacts.clear();
      var next = new HashMap<Integer,Seen>();
      visible.clear();
      for (Contact contact : snapshot) {
         Seen old = contacts.get(contact.id());
         boolean same = old!=null && old.contact.uuid().equals(contact.uuid());
         int lost = same ? old.lostAt : Integer.MIN_VALUE;
         if (same && old.contact.threat()==ThreatLevel.DETECTED && contact.threat()!=ThreatLevel.DETECTED) lost=player.tickCount;
         next.put(contact.id(),new Seen(contact,lost));
         Entity entity=player.level().getEntity(contact.id());
         if(entity!=null && entity.getUUID().equals(contact.uuid()) && player.hasLineOfSight(entity)) visible.add(contact.id());
      }
      contacts.clear(); contacts.putAll(next);
      owner=new WeakReference<>(player); level=player.level(); receivedAt=player.tickCount;
   }
   public static boolean active() {
      var p=Minecraft.getInstance().player;
      return p!=null && p==owner.get() && p.level()==level && p.tickCount-receivedAt<=30
         && NanosuitArmorUtil.getUsableProfile(p)==NanosuitProfile.NANOSUIT_1;
   }
   public static List<Entity> entities() {
      if(!active())return List.of();
      var world=Minecraft.getInstance().level;
      return contacts.values().stream().map(s->world.getEntity(s.contact.id()))
         .filter(Objects::nonNull).filter(ClassicEnemyContacts::known).toList();
   }
   public static boolean known(Entity entity) {
      if(!active() || !entity.isAlive())return false;
      Seen seen=contacts.get(entity.getId());
      return seen!=null && seen.contact.uuid().equals(entity.getUUID());
   }
   public static int color(Entity entity) {
      Seen seen=contacts.get(entity.getId());
      if(seen==null)return 0x83c66d;
      return switch(seen.contact.threat()) {
         case DETECTED -> 0xed3029;
         case SEARCHING, SUSPICIOUS -> seen.lostAt!=Integer.MIN_VALUE && owner.get().tickCount-seen.lostAt<10 ? 0xff670c : 0xe8d653;
         default -> 0x83c66d;
      };
   }
   public static boolean outline(Entity entity) {
      var mc=Minecraft.getInstance();
      return !mc.options.hideGui && mc.screen==null && ClassicBinoculars.running() && ClassicBinoculars.progress()>.05f
         && known(entity) && visible.contains(entity.getId());
   }
}
