//package music;
package api.model;
import static api.util.Util.*;
import api.util.Util;

public class Key {
   

   public static final java.util.List<Key> all=new java.util.ArrayList<Key>();

   //[ key constants
   public static final Key C=new Key ("C", 0, true, 0, 0);
   public static final Key G= new Key("G", 1, true, 7, 0);
   public static final Key D= new Key("D", 2, true, 2, 0);
   public static final Key A= new Key("A", 3, true, 9, 0);
   public static final Key E= new Key("E", 4, true, 4, 0);
   public static final Key B= new Key("B", 5, true, 11, 0);
   public static final Key Fs=new Key("F#", "F"+sup("#"), 6, true, 5, 1);
   public static final Key Cs=new Key("C#", "C"+sup("#"), 7, true, 0, 1);

   public static final Key F= new Key("F", -1, true, 5, 0);
   public static final Key Bb=new Key("Bb", "B"+sub("b"), -2, true, 11, -1);
   public static final Key Eb=new Key("Eb", "E"+sub("b"), -3, true, 4, -1);
   public static final Key Ab=new Key("Ab", "A"+sub("b"), -4, true, 9, -1);
   public static final Key Db=new Key("Db", "D"+sub("b"), -5, true, 2, -1);
   public static final Key Gb=new Key("Gb", "G"+sub("b"), -6, true, 7, -1);
   public static final Key Cb=new Key("Cb", "C"+sub("b"), -7, true, 0, -1);

   public static final Key a=new Key ("a", 0, false, 0, 0);
   public static final Key e= new Key("e", 1, false, 7, 0);
   public static final Key b= new Key("b", 2, false, 2, 0);
   public static final Key fs=new Key("f#", "f"+sup("#"), 3, false, 9, 0);
   public static final Key cs=new Key("c#", "c"+sup("#"), 4, false, 4, 0);
   public static final Key gs=new Key("g#", "g"+sup("#"), 5, false, 11, 0);
   public static final Key ds=new Key("d#", "d"+sup("#"), 6, false, 5, 1);
   public static final Key as=new Key("a#", "a"+sup("#"), 7, false, 0, 1);
   
   public static final Key d= new Key("d", -1, false, 5, 0);
   public static final Key g= new Key("g", -2, false, 11, -1);
   public static final Key c= new Key("c", -3, false, 4, -1);
   public static final Key f= new Key("f", -4, false, 9, -1);
   public static final Key bb=new Key("bb", "b"+sub("b"), -5, false, 2, -1);
   public static final Key eb=new Key("eb", "e"+sub("b"), -6, false, 7, -1);
   public static final Key ab=new Key("ab", "a"+sub("b"), -7, false, 0, -1);

   private final String name, prettyName;
   private final int value;
   private final boolean isMajor;
   private final int pitchIndex;
   private final int accidental;
   //private int absKey;

   private Key(String n, int v, boolean m, int pi, int a) {
      this(n, n, v, m, pi, a);
   }
   private Key(String n, String p, int v, boolean m, int pi, int a) {
      name=n;
      prettyName=p;
      value=v;
      isMajor=m;
      pitchIndex=pi;
      accidental=a;
      //quality=Character.isUpperCase(name.charAt(0))?0:1; //: 0 is major, 1 is minor, follow jmusic
      //absKey=ak;
      all.add(this);
   }

   public int getValue() { return value; }
   public boolean isMajor() { return isMajor; }
   
   public String toString() {
      //return getSimpleName();
      return getPrettyName();
   }
   public String getPrettyName() {
      return html(prettyName+" ( "+value+" )");
   }
   public String getName() {
      return name;
   }
   public static Key get(int s, boolean q) {  
      for(int i=0; i<all.size(); i++) {
         final Key k=all.get(i);
         if(k.isMajor()==q && k.getValue()==s) return k;
      }
      return null;
   }
   public static Key get(String sm) { //from simple name
      for(int i=0; i<all.size(); i++) {
         final Key k=all.get(i);
         if(k.name.equals(sm)) return k;
      }
      return null;
   }
   public int getPitch(int o) {
      return Util.getPitch(pitchIndex, o, accidental);
   }
   /*public int getAbsKey(Key k) {
      
   }*/
}
