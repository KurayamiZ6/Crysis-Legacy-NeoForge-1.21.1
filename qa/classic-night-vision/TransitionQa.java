import io.github.painworks.crysismod.client.nanovision.ClassicVisionTransition;
public class TransitionQa {
 public static void main(String[] args){
  var t=new ClassicVisionTransition();long now=10_000_000_000L;
  check(t.sample(now,true)==0);check(Math.abs(t.sample(now+500_000_000L,true)-.5)<.00001);
  float prior=t.displayed();check(t.sample(now+510_000_000L,false)==prior);
  check(t.sample(now+710_000_000L,false)<prior);
  prior=t.displayed();check(t.sample(now+720_000_000L,true)==prior);
  check(t.sample(now+1_720_000_000L,true)==1);
  check(t.sample(now+1_730_000_000L,false)==1);check(t.sample(now+2_730_000_000L,false)==0);
  t.reset();check(t.displayed()==0);
  // Same timestamps, independent of the number of intervening render frames.
  var a=new ClassicVisionTransition();var b=new ClassicVisionTransition();a.sample(now,true);b.sample(now,true);
  for(int i=1;i<=60;i++)a.sample(now+i*10_000_000L,true);
  check(Math.abs(a.sample(now+700_000_000L,true)-b.sample(now+700_000_000L,true))<.00001);
  System.out.println("PASS: 10 transition checks");
 }
 static void check(boolean ok){if(!ok)throw new AssertionError();}
}
