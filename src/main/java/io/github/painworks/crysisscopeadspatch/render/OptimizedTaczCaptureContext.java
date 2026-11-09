package io.github.painworks.crysisscopeadspatch.render;

public final class OptimizedTaczCaptureContext {
   private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);

   private OptimizedTaczCaptureContext() {
   }

   public static void enter() {
      DEPTH.set(DEPTH.get() + 1);
   }

   public static void exit() {
      int depth = DEPTH.get();
      if (depth <= 1) {
         DEPTH.remove();
      } else {
         DEPTH.set(depth - 1);
      }
   }

   public static boolean isActive() {
      return DEPTH.get() > 0;
   }
}
