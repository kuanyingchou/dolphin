package api.audio;

import java.io.IOException;
import java.io.File;
import java.util.ArrayList;

import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.AudioFileFormat;
import javax.swing.SwingUtilities;

import api.jpraat.Sound;
import api.jpraat.SoundEditor;
import api.midi.DumpReceiver;



import api.model.*;
import api.util.LineDiagram;
import api.util.Util;

public class SoundAnalyzer extends Thread {
   public static class Setting {
      public int floor=50;
      public int ceiling=1100;
      @Deprecated public int compensation=30;
      public int noiseLevel=70;
      public boolean veryAccurate=true;
      public boolean enableCompensation=true;
   } 
   public static final boolean DEBUG=false;
   //public static int INTENSITY_THRESHOLD=80; 
   public static final int MIN_SAMPLE_LENGTH_MS=100; //>>> adjustable
   public static final int SAMPLE_RATE=44100;
   //] should be adjustable
   
   private TargetDataLine m_line;
   private AudioFileFormat.Type m_targetType;
   private AudioInputStream m_audioInputStream;
   public static final AudioFormat audioFormat=new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
         44100.0F, 16, 1, 2, 44100.0F, false);
   boolean stopped=true;
   //MainFrame mainFrame;
   //final LineDiagram diagram=new LineDiagram();
   private final java.util.List<PitchListener> freqListener
      =new ArrayList<PitchListener>();
   public void addFrequencyListener(PitchListener lis) {
      freqListener.add(lis);
   }
   private final java.util.List<IntensityListener> itenListener
   =new ArrayList<IntensityListener>();
   public void addIntensityListener(IntensityListener lis) {
      itenListener.add(lis);
   }

   public static final Setting settings=new Setting();
   public SoundAnalyzer() {
//      addIntensityListener(new IntensityListener() {
//         @Override
//         public void gotIntensity(double iten) {
//            System.err.println(iten);
//            
//         }
//      });
//      addFrequencyListener(new FrequencyListener() {
//         @Override
//         public void gotFrequency(double f, double l) {
//            System.err.println(f+", "+l);
//            
//         }
//      });
   }
   
   public void start() {
      if(DEBUG) System.err.println("start sound input");
      AudioFileFormat.Type targetType=AudioFileFormat.Type.WAVE;
      DataLine.Info info=new DataLine.Info(TargetDataLine.class, audioFormat);
      TargetDataLine line=null;
      try {
         line=(TargetDataLine) AudioSystem.getLine(info);
         line.open(audioFormat);
      } catch(LineUnavailableException e) {
         if(DEBUG) System.err.println("unable to get a recording line");
         e.printStackTrace();
         //System.exit(1);
      }
      m_line=line;
      m_audioInputStream=new AudioInputStream(line);
      m_targetType=targetType;
      
      m_line.start();

      stopped=false;
      super.start();
   }

   public void stopRecording() {
      try {
         m_audioInputStream.close();
      } catch(IOException e) {
         e.printStackTrace();
      }
      if(m_line.isRunning()) {
         m_line.stop();
      }
      if(m_line.isOpen()) {
         m_line.close();
      }
      stopped=true;
      if(DEBUG) System.err.println("stop sound input");
   }

   //public static final File outFile=new File("out.wav");
   public void run() {
      /*try {
         AudioSystem.write(m_audioInputStream, m_targetType, outFile);
      } catch(IOException e) {
         e.printStackTrace();
      }*/
      //diagram.display();
      
      final java.util.List<Double> samples=new ArrayList<Double>();
      int nBytesRead=0;
      final int EXTERNAL_BUFFER_SIZE=(int)Math.pow(2, 13); //128000;
      byte[] abData=new byte[EXTERNAL_BUFFER_SIZE];
      //boolean isOn=false;
      while(nBytesRead != -1 && !stopped) {
         try {
            nBytesRead=m_audioInputStream.read(abData, 0, abData.length);
         } catch(IOException e) {
            e.printStackTrace();
         }
         if(nBytesRead > 0) {
            //System.err.println("data captured: "+nBytesRead);
            final double[] in=new double[nBytesRead / 2+1]; // >>> 16 bit
            int j=0;
            for(int i=0; i < nBytesRead - 1; i+=2) {
               byte left=abData[i];
               int right=abData[i + 1];
               right<<=8;
               right|=(left & 0xFF);
               in[j+1]=right / 65536.0; // >>>65535=2^16
               j++;
            }

            double averageIntensity=0;
            try {
               final Sound intensitySound=Sound.Sound_createSimple(1, (in.length-1) / (double)SAMPLE_RATE, SAMPLE_RATE);
               intensitySound.z[1]=in;
               final SoundEditor edit=SoundEditor.SoundEditor_create("", intensitySound);
               edit.computeIntensity();
               averageIntensity=edit.computeAverageIntensity();
            } catch(Exception e) {
               e.printStackTrace();
               continue; //>>>?
            }
            final double ai=averageIntensity;
            SwingUtilities.invokeLater(new Runnable() { //>>> not good
               public void run() {
                  for(IntensityListener lis: itenListener) {
                     lis.gotIntensity(ai); //notify listeners
                  }
               }
            });

            /*if(INTENSITY_THRESHOLD<0) {
               INTENSITY_THRESHOLD=(int)ai+20;
               System.err.println("t:"+ai);
            } else {*/
//System.err.println(ai);
               if(ai>=settings.noiseLevel) { //filter weak noises
                  //>>> too long? 
                  for(int i=1; i<in.length; i++) {
                     samples.add(in[i]);
                  }
               } else {
                  analyzePitch(samples);
                  samples.clear();
               }
            //}
         }
      }
      //System.err.println("got sample");
      
      
//System.err.println("done");
   }
   
   private void analyzePitch(java.util.List<Double> samples) {
      //[ filter strong and short noises
      
      if(samples.isEmpty()) return;
      if(samples.size()<=SAMPLE_RATE*MIN_SAMPLE_LENGTH_MS/1000) {
         //System.err.println("samples too short: "+samples.size());
         return;
      }
      final Sound pitchSound=Sound.Sound_createSimple(
            1, (double)samples.size() / SAMPLE_RATE, SAMPLE_RATE);
      for(int i=1; i < pitchSound.z[1].length; i++) {
         pitchSound.z[1][i]=samples.get(i - 1);
      }
      
      final SoundEditor editor=SoundEditor.SoundEditor_create("", pitchSound);
      editor.pitch.floor=settings.floor;
      editor.pitch.ceiling=settings.ceiling;
      //editor.pitch.veryAccurate=1;
      
      if(DEBUG) System.err.print("analyzing "+samples.size()+" samples("
            +editor.pitch.floor+"-"+editor.pitch.ceiling+")...");
      final long start=System.nanoTime();
      try {
         editor.computePitch();
      } catch(Exception e) {
         e.printStackTrace();
         return;
      }
      if(DEBUG) System.err.println("complete in "+(System.nanoTime()-start)/1000000.0+"ms.");
      
      
      //[ compute average pitch
      final java.util.List<Double> pitches=editor.getPitchesInFrequency();
      
//      diagram.clearData();
//      for(double s: pitches) {
//         diagram.addData((float)s);   
//      }
//      diagram.setMaximum(500);
//      diagram.repaint();
//      
      double freqSum=0;
      int pitchCount=0;
      for(double p : pitches) {
         if(Double.isNaN(p) || p<=0) continue;
         freqSum+=p;
         pitchCount++;
      }
      final double averageFreq=freqSum/pitchCount;         
      if(Double.isNaN(averageFreq)) {
         if(DEBUG) System.err.println("can't recognize.");
         return;
      }
//System.err.println(averageFreq);      
      
      //[ compute length
      final double lengthMs=samples.size()*1000.0/SAMPLE_RATE;
      
      SwingUtilities.invokeLater(new Runnable() { //>>> not good
         public void run() {
            for(PitchListener lis: freqListener) {
               lis.gotPitch(averageFreq, lengthMs); //notify listeners
            }
         }
      });
      
//System.err.println("add "+ DumpReceiver.getKeyName((int) averagePitch));
   }
}



/*** SimpleAudioRecorder.java ***/

