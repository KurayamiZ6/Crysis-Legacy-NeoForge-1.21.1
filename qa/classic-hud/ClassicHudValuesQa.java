import io.github.painworks.crysismod.nanosuit.client.ClassicHudValues;
public class ClassicHudValuesQa {
   public static void main(String[] args) {
      check(ClassicHudValues.alert(25.001f)==0,"above25");
      check(ClassicHudValues.alert(25)==1,"at25");
      check(ClassicHudValues.alert(15.001f)==1,"above15");
      check(ClassicHudValues.alert(15)==2,"at15");
      check(ClassicHudValues.fraction(0,100)==0,"zero");
      check(ClassicHudValues.fraction(.025f,100)>0,"fractional energy");
      check(ClassicHudValues.percent(10,40)==25,"modified max health");
      check(ClassicHudValues.fraction(150,100)==1,"overfill");
      check(ClassicHudValues.fraction(-1,100)==0,"negative");
      check(ClassicHudValues.fraction(Float.NaN,100)==0,"invalid");
      check(ClassicHudValues.fraction(12,0)==0,"zero max");
      check(ClassicHudValues.percent(600,1200)==50,"battery independent");
      System.out.println("PASS: 12 presentation boundary checks");
   }
   static void check(boolean ok,String name){if(!ok)throw new AssertionError(name);}
}
