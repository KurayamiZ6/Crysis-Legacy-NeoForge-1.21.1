package io.github.painworks.crysismod.client.nanovision;

/** Render-time easing only. Battery and eligibility remain authoritative elsewhere. */
public final class ClassicVisionTransition {
   private static final double DURATION_NANOS=1_000_000_000.0;
   private float displayed,start;
   private boolean target;
   private long started;
   public float sample(long now,boolean enabled) {
      if(enabled!=target){start=displayed;target=enabled;started=now;}
      double t=Math.max(0,Math.min(1,(now-started)/DURATION_NANOS));
      float eased=(float)(t*t*(3-2*t));
      displayed=start+((target?1:0)-start)*eased;
      return displayed;
   }
   public float displayed(){return displayed;}
   public void reset(){displayed=0;start=0;target=false;started=0;}
}
