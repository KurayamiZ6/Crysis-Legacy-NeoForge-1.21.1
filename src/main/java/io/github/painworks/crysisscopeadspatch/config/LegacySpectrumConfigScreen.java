package io.github.painworks.crysisscopeadspatch.config;

import io.github.painworks.crysismod.nanosuit.config.NanosuitClientConfig;
import io.github.painworks.crysismod.nanosuit.config.NanosuitSoundVolumes;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class LegacySpectrumConfigScreen {
   private LegacySpectrumConfigScreen() {
   }

   public static Screen create(Screen parent) {
      ConfigBuilder builder = ConfigBuilder.create().setParentScreen(parent).setTitle(Component.literal("Crysis Legacy"));
      ConfigCategory category = builder.getOrCreateCategory(Component.literal("Legacy first-person spectrum"));
      ConfigEntryBuilder entries = builder.entryBuilder();
      category.addEntry(
         entries.startBooleanToggle(Component.literal("Optimized TACZ thermal capture"), LegacySpectrumConfig.optimizedTaczCapture())
            .setDefaultValue(true)
            .setTooltip(
               new Component[]{
                  Component.literal("Reduces TACZ held-weapon capture bandwidth."),
                  Component.literal("Scope/ocular passes and unsupported formats stay on legacy."),
                  Component.literal("Applies immediately; disable to return to legacy capture.")
               }
            )
            .setSaveConsumer(LegacySpectrumConfig::setOptimizedTaczCapture)
            .build()
      );
      category.addEntry(
         entries.startDoubleField(Component.literal("Held-item thermal opacity"), LegacySpectrumConfig.heldItemOpacity())
            .setDefaultValue(1.0)
            .setMin(0.0)
            .setMax(1.0)
            .setTooltip(
               new Component[]{
                  Component.literal("Controls only first-person item and weapon spectrum."),
                  Component.literal("Mob spectrum and first-person arms keep their existing opacity."),
                  Component.literal("Applies immediately when saved.")
               }
            )
            .setSaveConsumer(LegacySpectrumConfig::setHeldItemOpacity)
            .build()
      );
      ConfigCategory audio = builder.getOrCreateCategory(Component.literal("Nanosuit audio"));
      for (NanosuitSoundVolumes.Category group : NanosuitSoundVolumes.Category.values()) {
         audio.addEntry(entries.startIntSlider(Component.literal(group.title()), Math.round(group.gain() * 100.0F), 0, 100)
            .setDefaultValue(100)
            .setSaveConsumer(group::setPercent)
            .build());
      }
      builder.setSavingRunnable(NanosuitClientConfig.SPEC::save);
      return builder.build();
   }
}
