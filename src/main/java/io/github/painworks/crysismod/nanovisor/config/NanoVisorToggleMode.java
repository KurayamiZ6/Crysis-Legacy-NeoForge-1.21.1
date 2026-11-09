package io.github.painworks.crysismod.nanovisor.config;

public enum NanoVisorToggleMode {
   TOGGLE("Toggle"),
   HOLD("Hold");

   private final String displayName;

   private NanoVisorToggleMode(String displayName) {
      this.displayName = displayName;
   }

   public String getDisplayName() {
      return this.displayName;
   }
}
