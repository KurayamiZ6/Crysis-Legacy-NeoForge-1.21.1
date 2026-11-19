import io.github.painworks.crysismod.nanovisor.client.ClassicBinocularAnimation;
public class ClassicBinocularAnimationQa {
 public static void main(String[] args){
  var a=new ClassicBinocularAnimation();long t=1_000_000_000L;
  a.toggle(t);check(a.sample(t)==0);check(a.sample(t+200_000_000L)>0);
  a.toggle(t+200_000_000L);check(a.sample(t+200_000_000L)==1);check(!a.requestedOpen());
  a.toggle(t+250_000_000L);check(a.sample(t+250_000_000L)==0);check(a.requestedOpen());
  a.toggle(t+250_000_000L);check(a.sample(t+250_000_000L)==1);
  a.toggle(t+250_000_000L);check(a.sample(t+250_000_000L)==0);
  check(a.sample(t+700_000_000L)==1);a.toggle(t+710_000_000L);check(a.sample(t+1_160_000_000L)==0);
  check(Math.abs(Math.tan(Math.toRadians(70)/2)/Math.tan(Math.toRadians(ClassicBinocularAnimation.zoomedFov(70,2))/2)-2)<1e-6);
  check(Math.abs(Math.tan(Math.toRadians(90)/2)/Math.tan(Math.toRadians(ClassicBinocularAnimation.zoomedFov(90,4))/2)-4)<1e-6);
  a.reset();check(a.sample(t+2_000_000_000L)==0);
  System.out.println("PASS: 13 animation and projection assertions");
 }
 static void check(boolean ok){if(!ok)throw new AssertionError();}
}
