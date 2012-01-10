package api.audacity;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import api.util.LineDiagram;



public class Wav2Midi {
   private static final int EXTERNAL_BUFFER_SIZE=128000;
   
   public static void main(String[] args) {
      final File soundFile=new File("input.wav");
      AudioInputStream audioInputStream=null;
      try {
         audioInputStream=AudioSystem.getAudioInputStream(soundFile);
      } catch(Exception e) {
         e.printStackTrace();
         System.exit(1);
      }
      final LineDiagram lineDiagram=new LineDiagram();
      int nBytesRead=0;
      byte[] abData=new byte[EXTERNAL_BUFFER_SIZE];
      while(nBytesRead != -1) {
         try {
            nBytesRead=audioInputStream.read(abData, 0, abData.length);
         } catch(IOException e) {
            e.printStackTrace();
         }
         if(nBytesRead >= 0) {
            //int nBytesWritten=line.write(abData, 0, nBytesRead);
            //System.err.println(EXTERNAL_BUFFER_SIZE);
            float[] in=new float[EXTERNAL_BUFFER_SIZE/2]; //>>> 16 bit
            int j=0;
            for(int i=0; i<nBytesRead-1; i+=2) {
               int t=abData[i+1];
               //System.err.print(Integer.toBinaryString(t)+", ");
               t<<=8;
               //System.err.print(Integer.toBinaryString(abData[i+1] & 0xFF)+": ");
               t|=(abData[i] & 0xFF);
               //System.err.println(Integer.toBinaryString(t));
               in[j]=t/65535.0f;
               //System.err.println(in[j]);
               j++;
               //System.exit(0);
            }
            /*final float offset=0.01f;
            for(int i=0; i < in.length; i++) {
               if(Math.abs(in[i])<offset) in[i]=0;
            }*/
            lineDiagram.addData(in);
            //lineDiagram.addData(-1);
         }
      }
      lineDiagram.display();
      
      
      
      
      
      
      
      

      final AudioFormat audioFormat=audioInputStream.getFormat();
      SourceDataLine line=null;
      final DataLine.Info info=new DataLine.Info(SourceDataLine.class, audioFormat);
      try {
         line=(SourceDataLine) AudioSystem.getLine(info);
         line.open(audioFormat);
      } catch(LineUnavailableException e) {
         e.printStackTrace();
         System.exit(1);
      } catch(Exception e) {
         e.printStackTrace();
         System.exit(1);
      }
      line.start();
      
      //[ filter???
      int startIdx=0;
      int silentLen=0;
      for(int i=0; i < lineDiagram.data.size(); i++) {
         final float offset=0.01f;
         if(Math.abs(lineDiagram.data.get(i)) < offset) {
            silentLen++;
            if(silentLen==32) {
               for(int j=0; j<silentLen; j++) {
                  lineDiagram.data.set(j+startIdx, 0.0f);
               }
            }
         } else {
            startIdx=i;
            silentLen=0;
         }
      }
      

      final byte[] output=new byte[EXTERNAL_BUFFER_SIZE];
      int outputIndex=0;
      for(int i=0; i < lineDiagram.data.size(); i++) {
         final float s=lineDiagram.data.get(i);
         final int si=(int)(s*65535.0f);
         //System.err.print(Integer.toBinaryString(si)+", ");
         final byte sbLow=(byte)(si & 0xFF);
         //System.err.print(Integer.toBinaryString(sbLow)+", ");
         final byte sbHi=(byte)(si>>8 & 0xFF);
         //System.err.println(Integer.toBinaryString(sbHi));
         
         output[outputIndex++]=sbLow;
         output[outputIndex++]=sbHi;
         if(outputIndex>=output.length) {
            line.write(output, 0, output.length);
            outputIndex=0;
            
            //[ clear output
            for(int j=0; j < output.length; j++) {
               output[j]=0;
            }
         }
      }
      if(outputIndex>0) {
         line.write(output, 0, outputIndex);
      }
      line.drain();
      line.close();
      //System.exit(0);

   }
}
