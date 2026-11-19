package io.github.painworks.crysismod.nanosuit.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;
import io.github.painworks.crysismod.nanosuit.config.NanosuitClientConfig;
import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent.Post;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent.Pre;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import org.joml.Matrix4f;
import org.slf4j.Logger;

@EventBusSubscriber(modid = "crysismod", value = Dist.CLIENT, bus = Bus.GAME)
public final class NanosuitTaczHudOverlay {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static boolean configInitialized;
   private static boolean taczPresent;
   private static final NanosuitTaczHudOverlay.TaczReflectionBridge BRIDGE = new NanosuitTaczHudOverlay.TaczReflectionBridge();

   private NanosuitTaczHudOverlay() {
   }

   @SubscribeEvent
   public static void onPreOverlay(Pre event) {
      if (io.github.painworks.crysismod.client.minimap.ClassicMinimapRenderer.isClassic()) {
         if (event.getName().toString().equals("tacz:tac_gun_hud_overlay")
               && ClassicWeaponHud.canReplace() && (ClassicWeaponHud.optic() || ClassicTaczData.read(Minecraft.getInstance().player) != null)) event.setCanceled(true);
         return;
      }
      ensureInitialized();
      if (taczPresent && NanosuitClientConfig.taczHudOverrideEnabled() && NanosuitClientConfig.taczHideOriginalHud()) {
         Minecraft minecraft = Minecraft.getInstance();
         LocalPlayer player = minecraft.player;
         if (player != null && !minecraft.options.hideGui && NanosuitArmorUtil.hasFullSet(player)) {
            if (event.getName().getNamespace().equals("tacz")) {
               event.setCanceled(true);
            }
         }
      }
   }

   @SubscribeEvent
   public static void onRenderHud(Post event) {
      if (NanosuitHudMasterCompositor.shouldLegacyRenderTaczHud()) {
         renderHotbarOverlay(event);
      }
   }

   public static void renderFromMaster(Post event) {
      renderHotbarOverlay(event);
   }

   private static void renderHotbarOverlay(Post event) {
      if (io.github.painworks.crysismod.client.minimap.ClassicMinimapRenderer.isClassic()) return;
      if (event.getName().equals(VanillaGuiLayers.HOTBAR)) {
         ensureInitialized();
         if (taczPresent && NanosuitClientConfig.taczHudOverrideEnabled()) {
            Minecraft minecraft = Minecraft.getInstance();
            LocalPlayer player = minecraft.player;
            if (player != null && !minecraft.options.hideGui && NanosuitArmorUtil.hasFullSet(player)) {
               NanosuitTaczHudOverlay.WeaponHudState state = BRIDGE.readWeaponState(player);
               if (state.isHoldingTaczGun) {
                  GuiGraphics gui = event.getGuiGraphics();
                  int screenW = net.minecraft.client.Minecraft.getInstance().getWindow().getGuiScaledWidth();
                  int screenH = net.minecraft.client.Minecraft.getInstance().getWindow().getGuiScaledHeight();
                  int centerX = screenW / 2;
                  int centerY = screenH / 2;
                  NanosuitHudMasterCompositor.prepareSharedHudFrame(event);
                  float baseX = NanosuitHudOverlay.getRightHudGroupCenterX(screenW) + NanosuitClientConfig.taczHudGroupOffsetX();
                  float baseY = NanosuitHudOverlay.getRightHudGroupCenterY(screenH) + NanosuitClientConfig.taczHudGroupOffsetY();
                  gui.pose().pushPose();
                  NanosuitHudOverlay.applySharedRightHudInertiaTransform(gui, centerX, centerY);
                  gui.pose().translate(baseX, baseY, 0.0F);
                  gui.pose().mulPose(Axis.ZP.rotationDegrees(NanosuitClientConfig.taczHudGroupRotation()));
                  gui.pose().scale(NanosuitClientConfig.taczHudGroupScaleX(), NanosuitClientConfig.taczHudGroupScaleY(), 1.0F);
                  applyVerticalSkew(gui, NanosuitClientConfig.taczHudGroupVerticalSkewDown());
                  drawWeaponIcon(gui, state);
                  drawSeparator(gui);
                  drawAmmoInGun(gui, minecraft.font, state);
                  drawAmmoInInventory(gui, minecraft.font, state);
                  gui.pose().popPose();
               }
            }
         }
      }
   }

   private static void drawWeaponIcon(GuiGraphics gui, NanosuitTaczHudOverlay.WeaponHudState state) {
      float x = NanosuitClientConfig.taczHudIconOffsetX();
      float y = NanosuitClientConfig.taczHudIconOffsetY();
      float scale = NanosuitClientConfig.taczHudIconScale();
      gui.pose().pushPose();
      gui.pose().translate(x, y, 0.0F);
      gui.pose().scale(scale, scale, 1.0F);
      int iconX = 0;
      int iconY = 0;
      int iconW = 39;
      int iconH = 13;
      if (state.hudTexture != null) {
         drawHudTexture(gui, state.hudTexture, iconX, iconY, iconW, iconH);
      } else if (!state.weaponStack.isEmpty()) {
         gui.renderItem(state.weaponStack, 30, 4);
      } else {
         gui.fill(8, 11, iconW - 8, 13, NanosuitClientConfig.taczHudDividerColor());
      }

      gui.pose().popPose();
   }

   private static void drawSeparator(GuiGraphics gui) {
      int x = 43;
      int yTop = 0;
      int yBottom = 16;
      gui.fill(x, yTop, x + 1, yBottom, NanosuitClientConfig.taczHudDividerColor());
   }

   private static void drawHudTexture(GuiGraphics gui, ResourceLocation texture, int x, int y, int areaW, int areaH) {
      applyShaderColor(NanosuitClientConfig.taczHudIconColor());
      gui.blit(texture, x, y, 0.0F, 0.0F, areaW, areaH, areaW, areaH);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   private static void drawAmmoInGun(GuiGraphics gui, Font font, NanosuitTaczHudOverlay.WeaponHudState state) {
      float x = NanosuitClientConfig.taczHudAmmoGunOffsetX();
      float y = NanosuitClientConfig.taczHudAmmoGunOffsetY();
      float scale = NanosuitClientConfig.taczHudAmmoGunScale();
      String value = formatAmmo(state.ammoInGun, 3);
      drawCounter(gui, font, x, y, scale, value, NanosuitClientConfig.taczHudAmmoGunColor());
   }

   private static void drawAmmoInInventory(GuiGraphics gui, Font font, NanosuitTaczHudOverlay.WeaponHudState state) {
      float x = NanosuitClientConfig.taczHudAmmoInvOffsetX();
      float y = NanosuitClientConfig.taczHudAmmoInvOffsetY();
      float scale = NanosuitClientConfig.taczHudAmmoInvScale();
      String value = formatAmmo(state.ammoInInventory, 4);
      drawCounter(gui, font, x, y, scale, value, NanosuitClientConfig.taczHudAmmoInvColor());
   }

   private static String formatAmmo(int value, int minDigits) {
      int safe = Math.max(0, value);
      String raw = Integer.toString(safe);
      if (raw.length() >= minDigits) {
         return raw;
      } else {
         StringBuilder sb = new StringBuilder(minDigits);

         for (int i = raw.length(); i < minDigits; i++) {
            sb.append('0');
         }

         sb.append(raw);
         return sb.toString();
      }
   }

   private static void drawCounter(GuiGraphics gui, Font font, float x, float y, float scale, String value, int color) {
      gui.pose().pushPose();
      gui.pose().translate(x, y, 0.0F);
      gui.pose().scale(scale, scale, 1.0F);
      gui.drawString(font, value, 0, 0, color, false);
      gui.pose().popPose();
   }

   private static void applyShaderColor(int argb) {
      float a = (argb >>> 24 & 0xFF) / 255.0F;
      float r = (argb >>> 16 & 0xFF) / 255.0F;
      float g = (argb >>> 8 & 0xFF) / 255.0F;
      float b = (argb & 0xFF) / 255.0F;
      RenderSystem.setShaderColor(r, g, b, a);
   }

   private static String cacheKey(ResourceLocation id) {
      return id == null ? null : id.toString();
   }

   private static void applyVerticalSkew(GuiGraphics gui, float skewDown) {
      if (!(Math.abs(skewDown) < 1.0E-4F)) {
         Matrix4f shear = new Matrix4f().identity().m01(skewDown);
         gui.pose().mulPose(shear);
      }
   }

   private static void ensureInitialized() {
      if (!configInitialized) {
         taczPresent = ModList.get().isLoaded("tacz");
         if (taczPresent) {
            BRIDGE.init();
         }

         configInitialized = true;
      }
   }

   @EventBusSubscriber(modid = "crysismod", value = Dist.CLIENT, bus = Bus.MOD)
   public static final class ClientModEvents {
      private ClientModEvents() {
      }

      @SubscribeEvent
      public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
         event.registerReloadListener((ResourceManagerReloadListener)resourceManager -> NanosuitTaczHudOverlay.BRIDGE.clearCaches());
      }
   }

   private static final class TaczReflectionBridge {
      private Class<?> iGunClass;
      private Class<?> iAmmoClass;
      private Class<?> iAmmoBoxClass;
      private Class<?> clientAssetsManagerClass;
      private Class<?> clientIndexManagerClass;
      private Method getIGunOrNull;
      private Method getGunId;
      private Method getGunDisplayId;
      private Method getCurrentAmmoCount;
      private Method useDummyAmmo;
      private Method getDummyAmmoAmount;
      private Method isAmmoOfGun;
      private Method isAmmoBoxOfGun;
      private Method getAmmoBoxCount;
      private Method isAmmoBoxCreative;
      private Method isAmmoBoxAllTypeCreative;
      private Method getClientGunDisplay;
      private Method getHudTextureLocation;
      private Object clientAssetsInstance;
      private Method getClientGunDisplayInstance;
      private Method getGunDisplayInstanceHudTexture;
      private Method gunHudHandleInventoryAmmo;
      private Field gunHudCacheInventoryAmmoCount;
      private final Map<String, ResourceLocation> hudTextureCache = new ConcurrentHashMap<>();
      private final Map<Class<?>, Object> singletonInstanceCache = new ConcurrentHashMap<>();
      private static final long AMMO_REFRESH_INTERVAL_MS = 50L;
      private long ammoCacheTimestampMs = 0L;
      private String ammoCacheGunKey = null;
      private int ammoCacheValue = 0;
      private boolean warnedMissingCoreApi = false;
      private boolean warnedTaczAmmoBridgeUnavailable = false;
      private boolean warnedReadStateError = false;
      private boolean initialized;

      private void init() {
         if (!this.initialized) {
            try {
               this.iGunClass = Class.forName("com.tacz.guns.api.item.IGun");
               this.iAmmoClass = Class.forName("com.tacz.guns.api.item.IAmmo");
               this.iAmmoBoxClass = Class.forName("com.tacz.guns.api.item.IAmmoBox");
               this.clientAssetsManagerClass = Class.forName("com.tacz.guns.client.resource.ClientAssetsManager");
               this.clientIndexManagerClass = Class.forName("com.tacz.guns.client.resource.ClientIndexManager");
               this.getIGunOrNull = this.findMethod(this.iGunClass, "getIGunOrNull", ItemStack.class);
               this.getGunId = this.findMethod(this.iGunClass, "getGunId", ItemStack.class);
               this.getGunDisplayId = this.findMethod(this.iGunClass, "getGunDisplayId", ItemStack.class);
               this.getCurrentAmmoCount = this.findMethod(this.iGunClass, "getCurrentAmmoCount", ItemStack.class);
               this.useDummyAmmo = this.findMethod(this.iGunClass, "useDummyAmmo", ItemStack.class);
               this.getDummyAmmoAmount = this.findMethod(this.iGunClass, "getDummyAmmoAmount", ItemStack.class);
               this.isAmmoOfGun = this.findMethod(this.iAmmoClass, "isAmmoOfGun", ItemStack.class, ItemStack.class);
               this.isAmmoBoxOfGun = this.findMethod(this.iAmmoBoxClass, "isAmmoBoxOfGun", ItemStack.class, ItemStack.class);
               this.getAmmoBoxCount = this.findMethod(this.iAmmoBoxClass, "getAmmoCount", ItemStack.class);
               this.isAmmoBoxCreative = this.findMethod(this.iAmmoBoxClass, "isCreative", ItemStack.class);
               this.isAmmoBoxAllTypeCreative = this.findMethod(this.iAmmoBoxClass, "isAllTypeCreative", ItemStack.class);
               this.getClientGunDisplay = this.findMethod(this.clientAssetsManagerClass, "getGunDisplay", ResourceLocation.class);
               this.clientAssetsInstance = this.findSingletonInstance(this.clientAssetsManagerClass);
               Class<?> gunDisplayClass = Class.forName("com.tacz.guns.client.resource.pojo.display.gun.GunDisplay");
               this.getHudTextureLocation = this.findMethod(gunDisplayClass, "getHudTextureLocation");
               this.getClientGunDisplayInstance = this.findMethod(this.clientIndexManagerClass, "getGunDisplay", ItemStack.class);
               Class<?> gunDisplayInstanceClass = Class.forName("com.tacz.guns.client.resource.GunDisplayInstance");
               this.getGunDisplayInstanceHudTexture = this.findMethod(gunDisplayInstanceClass, "getHUDTexture");
               Class<?> gunHudOverlayClass = Class.forName("com.tacz.guns.client.gui.overlay.GunHudOverlay");
               this.gunHudHandleInventoryAmmo = this.findMethod(gunHudOverlayClass, "handleInventoryAmmo", ItemStack.class, Inventory.class);
               this.gunHudCacheInventoryAmmoCount = this.findField(gunHudOverlayClass, "cacheInventoryAmmoCount");
            } catch (Throwable var4) {
            }

            if (this.getIGunOrNull == null && !this.warnedMissingCoreApi) {
               this.warnedMissingCoreApi = true;
               NanosuitTaczHudOverlay.LOGGER.warn("[{}] TaCZ detected but core API bridge is unavailable. HUD integration will stay disabled.", "crysismod");
            }

            this.initialized = true;
         }
      }

      private NanosuitTaczHudOverlay.WeaponHudState readWeaponState(LocalPlayer player) {
         try {
            if (!this.initialized) {
               this.init();
            }

            if (this.getIGunOrNull == null) {
               return NanosuitTaczHudOverlay.WeaponHudState.EMPTY;
            } else {
               ItemStack gunStack = player.getMainHandItem();
               if (gunStack.isEmpty()) {
                  return NanosuitTaczHudOverlay.WeaponHudState.EMPTY;
               } else {
                  Object iGun = invoke(this.getIGunOrNull, null, gunStack);
                  if (iGun == null) {
                     return NanosuitTaczHudOverlay.WeaponHudState.EMPTY;
                  } else {
                     int ammoInGun = asInt(invoke(this.getCurrentAmmoCount, iGun, gunStack));
                     int ammoInInv = this.resolveInventoryAmmoCount(player, iGun, gunStack);
                     ItemStack iconStack = gunStack.copy();
                     iconStack.setCount(1);
                     ResourceLocation hudTexture = this.resolveHudTexture(iGun, gunStack);
                     return new NanosuitTaczHudOverlay.WeaponHudState(true, ammoInGun, ammoInInv, iconStack, hudTexture);
                  }
               }
            }
         } catch (Throwable var8) {
            if (!this.warnedReadStateError) {
               this.warnedReadStateError = true;
               NanosuitTaczHudOverlay.LOGGER.warn("[{}] Failed reading TaCZ weapon HUD state via reflection. Using safe EMPTY state.", "crysismod");
            }

            return NanosuitTaczHudOverlay.WeaponHudState.EMPTY;
         }
      }

      private ResourceLocation resolveHudTexture(Object iGun, ItemStack gunStack) {
         ResourceLocation gunId = asResourceLocation(invoke(this.getGunId, iGun, gunStack));
         ResourceLocation displayId = asResourceLocation(invoke(this.getGunDisplayId, iGun, gunStack));
         if (displayId == null) {
            displayId = gunId;
         }

         String gunKey = NanosuitTaczHudOverlay.cacheKey(gunId);
         if (gunKey != null) {
            ResourceLocation cached = this.hudTextureCache.get(gunKey);
            if (cached != null) {
               return cached;
            }
         }

         String displayKey = NanosuitTaczHudOverlay.cacheKey(displayId);
         if (displayKey != null) {
            ResourceLocation cached = this.hudTextureCache.get(displayKey);
            if (cached != null) {
               return cached;
            }
         }

         ResourceLocation directFromGunId = resolveHudByGunIdConvention(gunId);
         if (directFromGunId != null) {
            if (gunKey != null) {
               this.hudTextureCache.put(gunKey, directFromGunId);
            }

            return directFromGunId;
         } else {
            ResourceLocation directFromDisplay = resolveHudByDisplayConvention(displayId);
            if (directFromDisplay != null) {
               if (displayKey != null) {
                  this.hudTextureCache.put(displayKey, directFromDisplay);
               }

               return directFromDisplay;
            } else {
               ResourceLocation runtimeHudTexture = this.resolveRuntimeHudTexture(gunStack);
               if (runtimeHudTexture != null) {
                  ResourceLocation preferred = preferHudTexture(runtimeHudTexture, displayId);
                  if (preferred != null) {
                     return preferred;
                  }
               }

               if (displayId != null && this.getClientGunDisplay != null && this.clientAssetsInstance != null && this.getHudTextureLocation != null) {
                  ResourceLocation cached = this.hudTextureCache.get(displayKey);
                  if (cached != null) {
                     return cached;
                  } else {
                     Object gunDisplay = invoke(this.getClientGunDisplay, this.clientAssetsInstance, displayId);
                     if (gunDisplay == null) {
                        return null;
                     } else {
                        ResourceLocation rawHud = asResourceLocation(invoke(this.getHudTextureLocation, gunDisplay));
                        ResourceLocation resolved = preferHudTexture(rawHud, displayId);
                        if (resolved != null) {
                           this.hudTextureCache.put(displayKey, resolved);
                        }

                        return resolved;
                     }
                  }
               } else {
                  return null;
               }
            }
         }
      }

      private ResourceLocation resolveRuntimeHudTexture(ItemStack gunStack) {
         if (this.getClientGunDisplayInstance != null && this.getGunDisplayInstanceHudTexture != null) {
            try {
               Object displayOptional = invoke(this.getClientGunDisplayInstance, null, gunStack);
               Object displayInstance = unwrapOptional(displayOptional);
               return displayInstance == null ? null : asResourceLocation(invoke(this.getGunDisplayInstanceHudTexture, displayInstance));
            } catch (Throwable var5) {
               return null;
            }
         } else {
            return null;
         }
      }

      private static ResourceLocation resolveTexturePath(ResourceLocation raw) {
         if (raw == null) {
            return null;
         } else {
            LinkedHashSet<ResourceLocation> candidates = new LinkedHashSet<>();
            candidates.add(raw);
            String path = raw.getPath();
            boolean hasTexturesPrefix = path.startsWith("textures/");
            boolean hasPngSuffix = path.endsWith(".png");
            String normalizedTexturePath = hasTexturesPrefix ? path : "textures/" + path;
            if (!normalizedTexturePath.endsWith(".png")) {
               normalizedTexturePath = normalizedTexturePath + ".png";
            }

            if (!hasPngSuffix) {
               candidates.add(ResourceLocation.fromNamespaceAndPath(raw.getNamespace(), path + ".png"));
            }

            if (!hasTexturesPrefix) {
               candidates.add(ResourceLocation.fromNamespaceAndPath(raw.getNamespace(), "textures/" + path));
               if (!hasPngSuffix) {
                  candidates.add(ResourceLocation.fromNamespaceAndPath(raw.getNamespace(), "textures/" + path + ".png"));
               }
            }

            candidates.add(ResourceLocation.fromNamespaceAndPath(raw.getNamespace(), "custom/tacz_default_gun/assets/" + raw.getNamespace() + "/" + normalizedTexturePath));

            for (ResourceLocation candidate : candidates) {
               try {
                  if (Minecraft.getInstance().getResourceManager().getResource(candidate).isPresent()) {
                     return candidate;
                  }
               } catch (Throwable var9) {
               }
            }

            return !hasTexturesPrefix && !hasPngSuffix ? ResourceLocation.fromNamespaceAndPath(raw.getNamespace(), "textures/" + path + ".png") : raw;
         }
      }

      private static ResourceLocation preferHudTexture(ResourceLocation rawCandidate, ResourceLocation displayId) {
         ResourceLocation resolved = resolveTexturePath(rawCandidate);
         if (resolved == null) {
            return resolveHudByDisplayConvention(displayId);
         } else {
            String path = resolved.getPath();
            if (path.contains("/slot/")) {
               ResourceLocation hudVariant = ResourceLocation.fromNamespaceAndPath(resolved.getNamespace(), path.replace("/slot/", "/hud/"));
               ResourceLocation normalizedHud = resolveTexturePath(hudVariant);
               if (normalizedHud != null && resourceExists(normalizedHud)) {
                  return normalizedHud;
               }

               ResourceLocation byDisplay = resolveHudByDisplayConvention(displayId);
               if (byDisplay != null) {
                  return byDisplay;
               }
            }

            return resourceExists(resolved) ? resolved : resolveHudByDisplayConvention(displayId);
         }
      }

      private static ResourceLocation resolveHudByDisplayConvention(ResourceLocation displayId) {
         if (displayId == null) {
            return null;
         } else {
            String gunName = displayId.getPath();
            if (gunName.endsWith("_display")) {
               gunName = gunName.substring(0, gunName.length() - "_display".length());
            }

            if (gunName.isBlank()) {
               return null;
            } else {
               ResourceLocation direct = ResourceLocation.fromNamespaceAndPath(displayId.getNamespace(), "gun/hud/" + gunName);
               ResourceLocation resolvedDirect = resolveTexturePath(direct);
               if (resolvedDirect != null && resourceExists(resolvedDirect)) {
                  return resolvedDirect;
               } else {
                  ResourceLocation taczCustom = ResourceLocation.fromNamespaceAndPath(
                     displayId.getNamespace(), "custom/tacz_default_gun/assets/" + displayId.getNamespace() + "/textures/gun/hud/" + gunName + ".png"
                  );
                  return resourceExists(taczCustom) ? taczCustom : null;
               }
            }
         }
      }

      private static ResourceLocation resolveHudByGunIdConvention(ResourceLocation gunId) {
         if (gunId != null && !gunId.getPath().isBlank()) {
            String gunName = gunId.getPath();
            ResourceLocation direct = ResourceLocation.fromNamespaceAndPath(gunId.getNamespace(), "gun/hud/" + gunName);
            ResourceLocation resolvedDirect = resolveTexturePath(direct);
            if (resolvedDirect != null && resourceExists(resolvedDirect)) {
               return resolvedDirect;
            } else {
               ResourceLocation taczCustom = ResourceLocation.fromNamespaceAndPath(
                  gunId.getNamespace(), "custom/tacz_default_gun/assets/" + gunId.getNamespace() + "/textures/gun/hud/" + gunName + ".png"
               );
               return resourceExists(taczCustom) ? taczCustom : null;
            }
         } else {
            return null;
         }
      }

      private static boolean resourceExists(ResourceLocation id) {
         if (id == null) {
            return false;
         } else {
            try {
               return Minecraft.getInstance().getResourceManager().getResource(id).isPresent();
            } catch (Throwable var2) {
               return false;
            }
         }
      }

      private int resolveInventoryAmmoCount(LocalPlayer player, Object iGun, ItemStack gunStack) {
         if (player != null && player.getAbilities().instabuild) {
            return 9999;
         } else {
            boolean usesDummyAmmo = asBoolean(invoke(this.useDummyAmmo, iGun, gunStack));
            if (usesDummyAmmo) {
               return Math.max(0, asInt(invoke(this.getDummyAmmoAmount, iGun, gunStack)));
            } else {
               String gunKey = NanosuitTaczHudOverlay.cacheKey(asResourceLocation(invoke(this.getGunId, iGun, gunStack)));
               long now = System.currentTimeMillis();
               if (gunKey != null && gunKey.equals(this.ammoCacheGunKey) && now - this.ammoCacheTimestampMs <= 50L) {
                  return this.ammoCacheValue;
               } else {
                  Integer taczCount = this.resolveInventoryAmmoCountViaTacz(gunStack, player.getInventory());
                  if (taczCount != null) {
                     int value = Math.max(0, taczCount);
                     this.ammoCacheGunKey = gunKey;
                     this.ammoCacheTimestampMs = now;
                     this.ammoCacheValue = value;
                     return value;
                  } else {
                     int total = 0;
                     int size = player.getInventory().getContainerSize();

                     for (int slot = 0; slot < size; slot++) {
                        ItemStack stack = player.getInventory().getItem(slot);
                        if (!stack.isEmpty()) {
                           Object itemObj = stack.getItem();
                           if (this.isAmmoOfGun != null
                              && this.iAmmoClass != null
                              && this.iAmmoClass.isInstance(itemObj)
                              && asBoolean(invoke(this.isAmmoOfGun, itemObj, gunStack, stack))) {
                              total += stack.getCount();
                           }

                           if (this.isAmmoBoxOfGun != null
                              && this.getAmmoBoxCount != null
                              && this.iAmmoBoxClass != null
                              && this.iAmmoBoxClass.isInstance(stack.getItem())
                              && asBoolean(invoke(this.isAmmoBoxOfGun, itemObj, gunStack, stack))) {
                              boolean creative = asBoolean(invoke(this.isAmmoBoxAllTypeCreative, itemObj, stack))
                                 || asBoolean(invoke(this.isAmmoBoxCreative, itemObj, stack));
                              if (creative) {
                                 return 9999;
                              }

                              total += Math.max(0, asInt(invoke(this.getAmmoBoxCount, itemObj, stack)));
                           }
                        }
                     }

                     this.ammoCacheGunKey = gunKey;
                     this.ammoCacheTimestampMs = now;
                     this.ammoCacheValue = total;
                     return total;
                  }
               }
            }
         }
      }

      private Integer resolveInventoryAmmoCountViaTacz(ItemStack gunStack, Inventory inventory) {
         if (this.gunHudHandleInventoryAmmo != null && this.gunHudCacheInventoryAmmoCount != null) {
            try {
               invoke(this.gunHudHandleInventoryAmmo, null, gunStack, inventory);
               return this.gunHudCacheInventoryAmmoCount.getInt(null);
            } catch (Throwable var4) {
               return null;
            }
         } else {
            if (!this.warnedTaczAmmoBridgeUnavailable) {
               this.warnedTaczAmmoBridgeUnavailable = true;
               NanosuitTaczHudOverlay.LOGGER.warn("[{}] TaCZ internal ammo bridge is unavailable; using fallback ammo scan.", "crysismod");
            }

            return null;
         }
      }

      private static Object invoke(Method method, Object receiver, Object... args) {
         if (method == null) {
            return null;
         } else {
            try {
               Object target = Modifier.isStatic(method.getModifiers()) ? null : receiver;
               if (target == null && !Modifier.isStatic(method.getModifiers())) {
                  target = NanosuitTaczHudOverlay.BRIDGE.findSingletonInstance(method.getDeclaringClass());
               }

               return target == null && !Modifier.isStatic(method.getModifiers()) ? null : method.invoke(target, args);
            } catch (Throwable var4) {
               return null;
            }
         }
      }

      private static int asInt(Object value) {
         return value instanceof Number number ? number.intValue() : 0;
      }

      private static boolean asBoolean(Object value) {
         return value instanceof Boolean bool ? bool : false;
      }

      private static ResourceLocation asResourceLocation(Object value) {
         if (value instanceof ResourceLocation resourceLocation) {
            return resourceLocation;
         } else if (value instanceof String str && !str.isBlank()) {
            try {
               return ResourceLocation.parse(str);
            } catch (Throwable var3) {
               return null;
            }
         } else {
            return null;
         }
      }

      private void clearCaches() {
         this.hudTextureCache.clear();
         this.singletonInstanceCache.clear();
         this.ammoCacheTimestampMs = 0L;
         this.ammoCacheGunKey = null;
         this.ammoCacheValue = 0;
      }

      private static Object unwrapOptional(Object value) {
         return value instanceof Optional<?> optional ? optional.orElse(null) : value;
      }

      private Method findMethod(Class<?> owner, String name, Class<?>... params) {
         try {
            Method method = owner.getMethod(name, params);
            method.setAccessible(true);
            return method;
         } catch (Throwable var6) {
            try {
               Method methodx = owner.getDeclaredMethod(name, params);
               methodx.setAccessible(true);
               return methodx;
            } catch (Throwable var5) {
               return null;
            }
         }
      }

      private Object findSingletonInstance(Class<?> owner) {
         Object cached = this.singletonInstanceCache.get(owner);
         if (cached != null) {
            return cached;
         } else {
            String[] fieldCandidates = new String[]{"INSTANCE", "instance"};

            for (String fieldName : fieldCandidates) {
               try {
                  Field field = owner.getDeclaredField(fieldName);
                  field.setAccessible(true);
                  Object value = field.get(null);
                  if (value != null) {
                     this.singletonInstanceCache.put(owner, value);
                     return value;
                  }
               } catch (Throwable var10) {
               }
            }

            return null;
         }
      }

      private Field findField(Class<?> owner, String name) {
         try {
            Field field = owner.getField(name);
            this.setAccessibleSafely(field);
            return field;
         } catch (Throwable var5) {
            try {
               Field fieldx = owner.getDeclaredField(name);
               this.setAccessibleSafely(fieldx);
               return fieldx;
            } catch (Throwable var4) {
               return null;
            }
         }
      }

      private void setAccessibleSafely(AccessibleObject object) {
         try {
            object.setAccessible(true);
         } catch (Throwable var3) {
         }
      }
   }

   private record WeaponHudState(boolean isHoldingTaczGun, int ammoInGun, int ammoInInventory, ItemStack weaponStack, ResourceLocation hudTexture) {
      private static final NanosuitTaczHudOverlay.WeaponHudState EMPTY = new NanosuitTaczHudOverlay.WeaponHudState(false, 0, 0, ItemStack.EMPTY, null);
   }
}
