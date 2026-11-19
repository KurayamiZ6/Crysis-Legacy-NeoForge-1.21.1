package io.github.painworks.crysismod.nanovisor.client;

/** Crysis N1 toggle semantics, deliberately not a reversible interpolation. */
public final class ClassicBinocularAnimation {
   public enum State { CLOSED, OPENING, OPEN, CLOSING }
   private State state=State.CLOSED;
   private long started;
   public void toggle(long now){
      sample(now);
      state=(state==State.OPEN||state==State.OPENING)?State.CLOSING:State.OPENING;
      started=now;
   }
   public float sample(long now){
      float t=(float)Math.max(0,Math.min(1,(now-started)/450_000_000.0));
      float p=t*t*(3-2*t);
      if(state==State.OPENING&&t==1)state=State.OPEN;
      if(state==State.CLOSING&&t==1)state=State.CLOSED;
      return switch(state){case CLOSED->0;case OPEN->1;case OPENING->p;case CLOSING->1-p;};
   }
   public boolean requestedOpen(){return state==State.OPENING||state==State.OPEN;}
   public State state(){return state;}
   public void reset(){state=State.CLOSED;started=0;}
   public static double zoomedFov(double fov,double magnification){return Math.toDegrees(2*Math.atan(Math.tan(Math.toRadians(fov)/2)/magnification));}
}
