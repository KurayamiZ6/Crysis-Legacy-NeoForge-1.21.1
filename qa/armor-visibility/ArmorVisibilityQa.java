package local.crysisqa;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.painworks.crysismod.nanosuit.client.PlayerHeadRenderHandler;
import io.github.painworks.crysismod.registry.ModItems;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import java.util.Arrays;

/** Real player-model Pre/Post checks plus six real third-person captures. No OS input. */
@Mod("crysisarmorvisibilityqa")
public final class ArmorVisibilityQa {
    private static final String[] PHASES={"n1-helmet-only","n1-chest-only","n1-both","n2-helmet-only","n2-chest-only","n2-both","unequipped"};
    private volatile int phase;
    private boolean logicDone,done;
    private long phaseStarted;
    private int ticks,passes,failures;

    public ArmorVisibilityQa() {
        NeoForge.EVENT_BUS.addListener(this::server);
        NeoForge.EVENT_BUS.addListener(this::client);
        NeoForge.EVENT_BUS.addListener(this::render);
    }
    private Minecraft mc(){return Minecraft.getInstance();}
    private Item helmet(boolean n2){return n2?ModItems.NANOSUIT_2_HELMET.get():ModItems.NANOGEO_NANOSUIT_HELMET.get();}
    private Item chest(boolean n2){return n2?ModItems.NANOSUIT_2_CHESTPLATE.get():ModItems.NANOGEO_NANOSUIT_CHESTPLATE.get();}
    private void check(String name,boolean result){if(result)passes++;else failures++;System.out.println("ARMOR_VISIBILITY_QA "+(result?"PASS ":"FAIL ")+name);}
    private void server(ServerTickEvent.Post event) {
        if(event.getServer().getPlayerList().getPlayers().isEmpty())return;
        var p=event.getServer().getPlayerList().getPlayers().getFirst();
        int part=phase%3;boolean n2=phase>=3;
        p.setItemSlot(EquipmentSlot.HEAD,phase<6&&part!=1?new ItemStack(helmet(n2)):ItemStack.EMPTY);
        p.setItemSlot(EquipmentSlot.CHEST,phase<6&&part!=0?new ItemStack(chest(n2)):ItemStack.EMPTY);
        p.setItemSlot(EquipmentSlot.LEGS,ItemStack.EMPTY);p.setItemSlot(EquipmentSlot.FEET,ItemStack.EMPTY);
        p.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);p.setItemSlot(EquipmentSlot.OFFHAND,ItemStack.EMPTY);
        p.serverLevel().setDayTime(6000);
    }
    private ModelPart[] parts(PlayerModel<?> model){return new ModelPart[]{model.head,model.hat,model.body,model.jacket,model.leftArm,model.rightArm,model.leftSleeve,model.rightSleeve,model.leftLeg,model.rightLeg,model.leftPants,model.rightPants};}
    private boolean[] visible(ModelPart[] parts){boolean[] result=new boolean[parts.length];for(int i=0;i<parts.length;i++)result[i]=parts[i].visible;return result;}
    private void testCase(String name,Item head,Item torso,boolean[] initial) {
        var p=mc().player;
        var renderer=(PlayerRenderer)mc().getEntityRenderDispatcher().getRenderer(p);
        var model=renderer.getModel();var parts=parts(model);var old=visible(parts);
        var oldHead=p.getItemBySlot(EquipmentSlot.HEAD);var oldChest=p.getItemBySlot(EquipmentSlot.CHEST);
        try {
            p.setItemSlot(EquipmentSlot.HEAD,new ItemStack(head));p.setItemSlot(EquipmentSlot.CHEST,new ItemStack(torso));
            for(int i=0;i<parts.length;i++)parts[i].visible=initial[i];
            var pose=new PoseStack();var buffer=mc().renderBuffers().bufferSource();
            PlayerHeadRenderHandler.onRenderPlayerPre(new RenderPlayerEvent.Pre(p,renderer,0,pose,buffer,15728880));
            boolean hideHead=head==helmet(false)||head==helmet(true);
            boolean hideBody=torso==chest(false)||torso==chest(true);
            boolean[] expected=initial.clone();
            if(hideHead){expected[0]=false;expected[1]=false;}
            if(hideBody)for(int i=2;i<8;i++)expected[i]=false;
            check(name+"-mask",Arrays.equals(expected,visible(parts)));
            PlayerHeadRenderHandler.onRenderPlayerPost(new RenderPlayerEvent.Post(p,renderer,0,pose,buffer,15728880));
            check(name+"-restoration",Arrays.equals(initial,visible(parts)));
        } finally {
            p.setItemSlot(EquipmentSlot.HEAD,oldHead);p.setItemSlot(EquipmentSlot.CHEST,oldChest);
            for(int i=0;i<parts.length;i++)parts[i].visible=old[i];
        }
    }
    private void logicalChecks() {
        boolean[] all=new boolean[12];Arrays.fill(all,true);
        for(boolean n2:new boolean[]{false,true}) {
            String name=n2?"n2":"n1";
            testCase(name+"-helmet-only",helmet(n2),Items.AIR,all);
            testCase(name+"-chest-only",Items.AIR,chest(n2),all);
            testCase(name+"-both",helmet(n2),chest(n2),all);
            testCase(name+"-helmet-vanilla-chest",helmet(n2),Items.DIAMOND_CHESTPLATE,all);
            testCase(name+"-vanilla-helmet-chest",Items.DIAMOND_HELMET,chest(n2),all);
        }
        testCase("no-armor",Items.AIR,Items.AIR,all);
        testCase("mixed-generations",helmet(false),chest(true),all);
        boolean[] hidden=all.clone();hidden[0]=false;hidden[2]=false;hidden[6]=false;
        testCase("prehidden-parts-preserved",helmet(false),Items.AIR,hidden);
    }
    private void client(ClientTickEvent.Post event) {
        if(done)return;
        if(++ticks>1600){System.out.println("ARMOR_VISIBILITY_QA_TIMEOUT");done=true;mc().stop();return;}
        if(mc().player==null||mc().level==null)return;
        if(mc().screen instanceof PauseScreen)mc().setScreen(null);
        if(mc().screen!=null)return;
        mc().options.setCameraType(CameraType.THIRD_PERSON_FRONT);
        mc().player.setXRot(0);mc().player.setYRot(0);mc().player.yBodyRot=0;mc().player.yHeadRot=0;
        if(!logicDone){logicalChecks();logicDone=true;phaseStarted=System.nanoTime();}
    }
    private void render(RenderGuiEvent.Post event) {
        if(done||!logicDone||mc().player==null||mc().screen!=null)return;
        if(System.nanoTime()-phaseStarted<1_000_000_000L)return;
        boolean n2=phase>=3;int part=phase%3;
        boolean expectedHead=phase<6&&part!=1,expectedChest=phase<6&&part!=0;
        if(mc().player.getItemBySlot(EquipmentSlot.HEAD).is(helmet(n2))!=expectedHead
            || mc().player.getItemBySlot(EquipmentSlot.CHEST).is(chest(n2))!=expectedChest)return;
        Screenshot.grab(mc().gameDirectory,PHASES[phase]+".png",mc().getMainRenderTarget(),ignored->{});
        System.out.println("ARMOR_VISIBILITY_QA_CAPTURE "+PHASES[phase]);
        if(++phase==PHASES.length){done=true;System.out.println("ARMOR_VISIBILITY_QA_COMPLETE pass="+passes+" fail="+failures+" captures="+PHASES.length);mc().stop();return;}
        phaseStarted=System.nanoTime();
    }
}
