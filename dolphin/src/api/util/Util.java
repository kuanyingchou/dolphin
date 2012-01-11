package api.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Arrays;
import java.util.Vector;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.MidiDevice.Info;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import api.model.Part;
import api.model.Score;


public class Util {
   public static final String curDir=System.getProperty("user.dir");
   public static final String LS=System.getProperty("line.separator");
   public static final char LSET='{';
   public static final char RSET='}';
   
   public static double LOG_2_BASE_10=Math.log(2);
   public static double log2(double a) {
      return Math.log(a)/LOG_2_BASE_10;
   }
   public static double frequencyToPitch(double freq) {
      return frequencyToPitch(freq, 440.0, 69);
   }
   public static double frequencyToPitch(double freq, double base, int basePitch) { 
      return basePitch+12*Util.log2(freq/base);
   }
   /*public static double pitchToFrequency(int pitch) { //>>>
      return Math.pow(10, (pitch-69)*LOG_2_BASE_10/12.0)*440.0;
   }*/
   
   public static String getObjectInfo(Object target) {
      final StringBuilder sb=new StringBuilder();
      final Class<?> cls=target.getClass();
      appendClassType(sb, cls);
      appendClassFields(target, sb, cls);
      return sb.toString();
   }
   private static void appendClassType(final StringBuilder sb,
         final Class<?> cls) {
      sb.append("\n").append(cls.getSimpleName()).append(" {").append("\n");
   }
   private static void appendClassFields(Object target, final StringBuilder sb,
         final Class<?> cls) {
      final Field[] fields=cls.getDeclaredFields();
      try {
         for(int i=0; i < fields.length; i++) {
            fields[i].setAccessible(true);
            if(fields[i].getAnnotation(BackRef.class)!=null) {
               //System.err.println(fields[i].getName());
               continue;
            }
            sb.append(i).append(". ");
            sb.append(fields[i].getName());
            sb.append("=");
            final Object o=fields[i].get(target);
//System.err.println(object);
            if(o==null) {
               //continue; //>>> why null?
               sb.append("null");
            } else {
               if(o.getClass().isArray()) {
                  final int len=Array.getLength(o);
                  sb.append("[");
                  for(int j=0; j<len; j++) {
                     sb.append(Array.get(o, j)).append(" ");
                  }
                  sb.append("]");
                  //sb.append(Arrays.toString((Object[])o));
               } else {
                  sb.append(o);
               }
            }
            sb.append(": ").append(fields[i].getType().getSimpleName()); //: static type 
            //] runtime type: o.getClass().getSimpleName());
            sb.append("\n");
         }
         sb.append("}").append("\n");
      } catch(IllegalArgumentException e) {
         e.printStackTrace();
      } catch(IllegalAccessException e) {
         e.printStackTrace();
      }
   }
   
   public static final Color[] colors=new Color[] {
      new Color(0x7C99F9)
      /*
      new Color(0x5076F7),
      new Color(0xA8BBFC),
      new Color(0xD3DDFD),
      new Color(0xEEF2FF),
      new Color(0x4464D2),
      new Color(0x3C59B9),
      new Color(0x283B7C),
      new Color(0x141E3E),
      new Color(0x080C19)*/
   };
   
   //[ convenient html methods
   public static String tag(String t, String v) {
      return "<"+t+">"+v+"</"+t+">";
   }
   public static String sup(String v) {
      return tag("sup", v);
   }
   public static String sub(String v) {
      return tag("sub", v);
   }
   public static String html(String v) {
      return tag("html", v);
   }
   
   private static String[] keyNames= {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
   
   public static String getPitchName(int pitch) {
      if(pitch<0 || pitch>=128) throw new IllegalArgumentException();
      return keyNames[pitch%12]+(pitch/12-1);
   }
   public static int getPitch(int keyIndex, int octave, int sharpFlat) {
      if(keyIndex<0 || keyIndex>=12) throw new IllegalArgumentException();
      if(octave<-1 || octave>13) throw new IllegalArgumentException();
      if(sharpFlat<-1 || sharpFlat>1) throw new IllegalArgumentException();
      return (octave+1)*12+keyIndex+sharpFlat;
   }
   
   public static Color getContrastColor(Color a) {
      return new Color(255-a.getRed(), 255-a.getGreen(), 255-a.getBlue());
   }
   
   public static ImageIcon getImageIcon(String path) {
      final URL url=getResource(path);
      if(url==null) return null;
      final ImageIcon res=new ImageIcon(url);
      return res;
   }
   public static URL getResource(String path) {
      return ClassLoader.getSystemResource(path);
   }
   public static InputStream getResourceAsStream(String path) {
      final InputStream res=ClassLoader.getSystemResourceAsStream(path);
      if(res==null) System.err.println("resource unavailable: "+path);
      return res;
   }
   
   private static boolean[] majorKeys= {true, false, true, false, true, true, false, true, false, true, false, true};
   public static boolean isMajorKey(int pitch) { //>>> pitch without sharps/flats, rename this later
      return majorKeys[pitch%12];
   }
   
  
   public static Vector<Info> getDeviceInfos() throws MidiUnavailableException {
      final Vector<Info> infos=new Vector<Info>();
      final Info[] deviceInfos=MidiSystem.getMidiDeviceInfo();
      for(int i=0; i < deviceInfos.length; i++) {
         infos.add(deviceInfos[i]);
      }
      return infos;
   }
   public static final Color[] beachColors= {
      new Color(18, 77, 158),
      new Color(111, 186, 255),
      new Color(184, 235, 255),
      new Color(255, 221, 157),
      new Color(201, 146, 68)
   };
   
   public static void setCrossPlatformLookAndFeel() {
      setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
   }
   public static void setSystemLookAndFeel() {
      setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
   }
   
   public static void setLookAndFeel(String className) {
      try {
         UIManager.setLookAndFeel(className);
         //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         
         //[ jtattoo
         //UIManager.setLookAndFeel("com.jtattoo.plaf.hifi.HiFiLookAndFeel");
         //UIManager.setLookAndFeel("com.jtattoo.plaf.smart.SmartLookAndFeel");
         //UIManager.setLookAndFeel("org.jvnet.substance.SubstanceLookAndFeel");
         //UIManager.setLookAndFeel("org.jvnet.substance.skin.SubstanceBusinessLookAndFeel");
         
         //[ substance
         //UIManager.setLookAndFeel("org.jvnet.substance.skin.SubstanceRavenGraphiteLookAndFeel");
         //UIManager.setLookAndFeel("org.jvnet.substance.skin.SubstanceOfficeSilver2007LookAndFeel");
         //UIManager.setLookAndFeel("org.jvnet.substance.skin.SubstanceRavenGraphiteGlassLookAndFeel");
         //UIManager.setLookAndFeel("org.jvnet.substance.skin.SubstanceNebulaLookAndFeel");
         //UIManager.setLookAndFeel("org.jvnet.substance.skin.SubstanceBusinessBlackSteelLookAndFeel");
         //UIManager.setLookAndFeel("org.jvnet.substance.skin.SubstanceBusinessLookAndFeel");
         //UIManager.setLookAndFeel("org.jvnet.substance.skin.SubstanceOfficeBlue2007LookAndFeel");
      } catch(UnsupportedLookAndFeelException e) {
         e.printStackTrace();
      } catch(ClassNotFoundException e) {
         e.printStackTrace();
      } catch(InstantiationException e) {
         e.printStackTrace();
      } catch(IllegalAccessException e) {
         e.printStackTrace();
      }
      //SubstanceLookAndFeel.setFontPolicy(FontPolicies.getLogicalFontsPolicy());
      
      JFrame.setDefaultLookAndFeelDecorated(true);
      JDialog.setDefaultLookAndFeelDecorated(true);
   }
   
   
   /////////////////////////////// Test /////////////////////////////////
   
   public static void testFrequencyToPitch() {
      System.err.println(frequencyToPitch(13, 263, 60));
      //System.err.println(frequencyToPitch(pitchToFrequency(67)));
   }
   public static void testGetObjectInfo() {
      Score score=new Score();
      Part part=new Part();
      score.add(part);
      System.out.println(score);
   }
   public static void main(String[] args) {
      testGetObjectInfo();
   }
   
}

