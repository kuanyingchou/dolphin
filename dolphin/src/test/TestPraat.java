package test;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import api.jpraat.Sound;
import api.jpraat.SoundEditor;
import api.midi.DumpReceiver;
import api.model.Note;
import api.model.Part;
import api.model.Score;
import api.util.LineDiagram;






public class TestPraat {
   public static void main(String[] args) {
      // LongSound sequence=new LongSound();
      final java.util.List<Double> samples=new ArrayList<Double>();

      final int EXTERNAL_BUFFER_SIZE=128000;
      // final File soundFile=new File("drm.wav");
      final File soundFile=new File("out.wav");
      AudioInputStream audioInputStream=null;
      try {
         audioInputStream=AudioSystem.getAudioInputStream(soundFile);
      } catch(Exception e) {
         e.printStackTrace();
         System.exit(1);
      }
      // final Sequence samples=new Sequence();
      int nBytesRead=0;
      byte[] abData=new byte[EXTERNAL_BUFFER_SIZE];
      while(nBytesRead != -1) {
         try {
            nBytesRead=audioInputStream.read(abData, 0, abData.length);
         } catch(IOException e) {
            e.printStackTrace();
         }
         if(nBytesRead >= 0) {
            // int nBytesWritten=line.write(abData, 0, nBytesRead);
            // System.err.println(EXTERNAL_BUFFER_SIZE);
            Double[] in=new Double[nBytesRead / 2]; // >>> 16 bit
            int j=0;
            for(int i=0; i < nBytesRead - 1; i+=2) {
               byte left=abData[i];
               int right=abData[i + 1];
               // System.err.print(Integer.toBinaryString(t)+", ");
               right<<=8;
               // System.err.print(Integer.toBinaryString(abData[i+1] &
               // 0xFF)+": ");
               right|=(left & 0xFF);
               // System.err.println(Integer.toBinaryString(t));
               in[j]=right / 32768.0; // >>>65535=2^16
               // System.err.println(in[j]);
               j++;
               // System.exit(0);
            }
            /*
             * final float offset=0.01f; for(int i=0; i < in.length; i++) {
             * if(Math.abs(in[i])<offset) in[i]=0; }
             */
            for(int i=0; i < j; i++) {
               samples.add(in[i]);
               // samples.addAll(Arrays.asList(in));
            }
            // lineDiagram.addData(-1);
         }
      }
      // System.err.println(samples.size());
      Sound testSound=Sound.Sound_createSimple(1, samples.size() / 44100.0,
            44100.0);
      // testSound.z[1]=new double[1+samples.size()];
      //System.err.println(testSound.z[1].length);
      for(int i=1; i < testSound.z[1].length; i++) {
         testSound.z[1][i]=samples.get(i - 1);
         //System.err.println(testSound.z[1][i]);
      }
      
      //for(int i=1; i <= testSound.z.length; i++) { 
      //   for(int j=1; j <testSound.z[1].length; j++) { 
      //      System.err.println(testSound.z[1][j]); 
      //   }
      //}
       

      SoundEditor editor=SoundEditor.SoundEditor_create("hello world",
            testSound);
      
      editor.computePitch();
      editor.computeIntensity();
      
      System.err.println("finish");
      
      //printPitches(editor.getPitches());
      printIntensities(editor.getIntensities());
     
      
      // Object o=editor.pitch.data;
      

      // System.err.println(sequence.samples);
      /*
       * AudioFormat audioFormat=audioInputStream.getFormat(); final WaveClip
       * clip=new WaveClip(sequence, (int)audioFormat.getSampleRate()); final
       * ViewInfo viewInfo=new ViewInfo();
       * 
       * final JFrame jf=new JFrame();
       * jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); jf.add(new
       * JComponent() {
       * 
       * @Override protected void paintComponent(Graphics g) {
       * super.paintComponent(g); TrackArtist.drawClipSpectrum( null, clip, g,
       * getBounds(), viewInfo, false); } }); jf.setSize(400, 300);
       * jf.setVisible(true);
       * 
       * final int p=(int)(69+12Util.log2(435.9/440.0f)); System.err.println(p);
       */
   }

   private static void printPitches(List<Integer> pitches) {
      // TODO Auto-generated method stub
      //final java.util.List<Integer> pitches=editor.getPitches();
      double averagePitch=0;
      int pitchCount=0;
      final Part part=new Part();
      for(int p: pitches) {
         if(p>=0 && p<128) {
            System.err.println(p+": "+DumpReceiver.getKeyName(p));
            averagePitch+=p;
            pitchCount++;
         } else {
            System.err.println("none");
            if(pitchCount==0) continue;
            averagePitch/=pitchCount;
            part.add(new Note((int)averagePitch, Note.WHOLE_LENGTH/4));
            averagePitch=0;
            pitchCount=0;
         }
         //System.err.println(editor.pitch.data.frame[i].candidate[1].strength);
      }
      
      final Score score=new Score();
      score.add(part);
      
      System.err.println("to Score done");
      System.err.println(score);
      Sequence sequence;
      try {
         sequence=score.toSequence();
         final Sequencer sequencer=MidiSystem.getSequencer();
         sequencer.setSequence(sequence);
         //sequencer.getTransmitter().setReceiver(new DumpReceiver(System.out));
         if(!sequencer.isOpen()) sequencer.open();
         if(sequencer.isRunning()) sequencer.stop();
         sequencer.start();
      } catch(InvalidMidiDataException e) {
         e.printStackTrace();
      } catch(MidiUnavailableException e) {
         e.printStackTrace();
      }
   }
   private static void printIntensities(List<Double> intensities) {
      final LineDiagram diagram=new LineDiagram();
      for(double d: intensities) {
         final float f=(float)(d);
         diagram.addData(f);
         System.err.println(f);
      }
      diagram.display();
   }
}
