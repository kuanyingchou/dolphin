package api.audio;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class PitchProfile {
   private static final int[] pitchAdvance= {2, 2, 1, 2, 2, 2, 1}; 
   private static final int[] pitchCumulation= {0, 2, 4, 5, 7, 9, 11, 12};
   
   private static final String DEFAULT_FILE_NAME="pitch_profile.info";
   //] >>> can only apply to C/a basePitch 
   private final int[] pitchTable;
   
   private final int basePitch;
   private double[] pitches;
   
   public PitchProfile(int base, int len) {
      basePitch=base;
      pitches=new double[len];
      pitchTable=new int[len];
      int pitch=basePitch;
      int advIdx=0;
      for(int i=0; i<pitchTable.length; i++) {
         pitchTable[i]=pitch;
         pitch+=pitchAdvance[(advIdx++)%pitchAdvance.length];
      }
   }
   public void set(int index, double value) { pitches[index]=value; }
   public double get(int index) { return pitches[index]; }
   public int size() { return pitches.length; }
   public double getPitchTable(int index) { return pitches[index]; }
   
   public int getClosestPitch(double pitchIn) {
      
      
      double diff=Double.MAX_VALUE;
      int index=-1;
      double pitchRunner=pitches[0];
      if(pitchIn>=pitchRunner) {
         for(int i=0; i < pitchAdvance.length; i++) {
            final double t=Math.abs(pitchIn-pitchRunner);
            if(t<diff) {
               diff=t;
               index=i;
            }
            pitchRunner+=pitchAdvance[i];
         }
         //final double dRes=freqs[0]+pitchCumulation[index];
         int iRes=basePitch+pitchCumulation[index]; //Math.round((float)dRes);
         if(iRes>127) iRes=127;
         if(iRes<0) iRes=0;
         return iRes;
      } else {
         return basePitch; //>>>
      }
      
      
                      
      /*
      //[ by full pitchTable
      if(freq<=freqs[0]) return basePitch;
      if(freq>=freqs[freqs.length-1]) {
         return pitchTable[freqs.length-1];
      }
      
      int index=-1;
      double minDiff=Double.MAX_VALUE;
      for(int i=0; i<freqs.length; i++) {
         final double diff=Math.abs(freq-freqs[i]);
         if(diff<minDiff) {
            minDiff=diff;
            index=i;
         }
      }
      if(index<0) return basePitch; //>>>
      return pitchTable[index];
      */
   }
   public static PitchProfile load(String fileName) {
      final PitchProfile res=new PitchProfile(60, 8);
      final Properties p=new Properties();
      try {
         p.load(new FileReader(new File(fileName)));
      } catch(FileNotFoundException e) {
         //e.printStackTrace();
         System.err.println("can't find "+DEFAULT_FILE_NAME+", using default profile.");
         res.pitches=new double[] 
         {
               113.39360301583157,
               127.6236746054193,
               143.44776740517904,
               147.94028911563288,
               170.48382158501994,
               190.28214691509078,
               195.57587313703502,
               227.74166248456984
         }; //>>>
         return res;
      } catch(IOException e) {
         throw new RuntimeException(e);
      }
      for(int i=0; i<res.size(); i++) {
         final String valueStr=(String)p.get(String.valueOf(i));
         //System.err.println(valueStr);
         res.set(i, Double.parseDouble(valueStr));
      }
      return res;
   }
   public static PitchProfile loadDefault() {
      return load(DEFAULT_FILE_NAME);
   }
   public void saveDefault() {
      save(DEFAULT_FILE_NAME);
   }
   public void save(String fileName) {
      final Properties p=new Properties();
      for(int i=0; i < pitches.length; i++) {
         p.put(String.valueOf(i), String.valueOf(pitches[i]));   
      }
      try {
         p.store(new FileWriter(new File(fileName)), "pitch profile(Hz)");
      } catch(IOException e) {
         e.printStackTrace();
      }
   }
}