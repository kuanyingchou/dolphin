package api.audacity;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import api.util.LineDiagram;



public class FFT {
   private static int[][] gFFTBitTable;
   private static final int MaxFastBits=16;

   public static boolean IsPowerOfTwo(int x) {
      if(x < 2)
         return false;

      if((x & (x - 1)) != 0) /* Thanks to 'byang' for this cute trick! */
         return false;

      return true;
   }

   public static int NumberOfBitsNeeded(int PowerOfTwo) {
      int i;

      if(PowerOfTwo < 2) {
         System.err.printf("Error: FFT called with size %d\n", PowerOfTwo);
         System.exit(1);
      }

      for(i=0;; i++)
         if((PowerOfTwo & (1 << i)) != 0)
            return i;
   }

   public static int ReverseBits(int index, int NumBits) {
      int i, rev;

      for(i=rev=0; i < NumBits; i++) {
         rev=(rev << 1) | (index & 1);
         index>>=1;
      }

      return rev;
   }

   public static int FastReverseBits(int i, int NumBits) {
      if(NumBits <= MaxFastBits)
         return gFFTBitTable[NumBits - 1][i];
      else
         return ReverseBits(i, NumBits);
   }
   
   public static void InitFFT() {
      gFFTBitTable=new int[MaxFastBits][];

      int len=2;
      for(int b=1; b <= MaxFastBits; b++) {

         gFFTBitTable[b - 1]=new int[len];

         for(int i=0; i < len; i++)
            gFFTBitTable[b - 1][i]=ReverseBits(i, b);

         len<<=1;
      }
   }
   
   public static void fft(int NumSamples, boolean InverseTransform,
         float[] RealIn, float[] ImagIn, float[] RealOut, float[] ImagOut) {
      int NumBits; /* Number of bits needed to store indices */
      int i, j, k, n;
      int BlockSize, BlockEnd;

      double angle_numerator=2.0 * Math.PI;
      double tr, ti; /* temp real, temp imaginary */

      if(!IsPowerOfTwo(NumSamples)) {
         System.err.printf("%d is not a power of two%n", NumSamples);
         System.exit(1);
      }

      if(gFFTBitTable == null)
         InitFFT();

      if(InverseTransform)
         angle_numerator=-angle_numerator;

      NumBits=NumberOfBitsNeeded(NumSamples);

      /*
       * Do simultaneous data copy and bit-reversal ordering into outputs...
       */

      for(i=0; i < NumSamples; i++) {
         j=FastReverseBits(i, NumBits);
         RealOut[j]=RealIn[i];
         ImagOut[j]=(ImagIn == null) ? 0.0f : ImagIn[i];
      }

      /*
       * Do the FFT itself...
       */

      BlockEnd=1;
      for(BlockSize=2; BlockSize <= NumSamples; BlockSize<<=1) {

         double delta_angle=angle_numerator / (double) BlockSize;

         double sm2=Math.sin(-2 * delta_angle);
         double sm1=Math.sin(-delta_angle);
         double cm2=Math.cos(-2 * delta_angle);
         double cm1=Math.cos(-delta_angle);
         double w=2 * cm1;
         double ar0, ar1, ar2, ai0, ai1, ai2;

         for(i=0; i < NumSamples; i+=BlockSize) {
            ar2=cm2;
            ar1=cm1;

            ai2=sm2;
            ai1=sm1;

            for(j=i, n=0; n < BlockEnd; j++, n++) {
               ar0=w * ar1 - ar2;
               ar2=ar1;
               ar1=ar0;

               ai0=w * ai1 - ai2;
               ai2=ai1;
               ai1=ai0;

               k=j + BlockEnd;
               tr=ar0 * RealOut[k] - ai0 * ImagOut[k];
               ti=ar0 * ImagOut[k] + ai0 * RealOut[k];

               RealOut[k]=(float) (RealOut[j] - tr);
               ImagOut[k]=(float) (ImagOut[j] - ti); // >>>cast

               RealOut[j]+=tr;
               ImagOut[j]+=ti;
            }
         }

         BlockEnd=BlockSize;
      }

      /*
       * Need to normalize if inverse transform...
       */

      if(InverseTransform) {
         float denom=(float) NumSamples;

         for(i=0; i < NumSamples; i++) {
            RealOut[i]/=denom;
            ImagOut[i]/=denom;
         }
      }
   }

   public static void printArray(Object o, PrintStream ps) {
      if(!o.getClass().isArray()) throw new IllegalArgumentException();
      final int len=Array.getLength(o);
      if(len==0) return;
      ps.print(Array.get(o, 0));
      for(int i=0; i<len; i++) {
         ps.print(", ");
         ps.print(Array.get(o, i));
      }
   }
   
   public static float findMax(float[] src) {
      float max=Float.MIN_VALUE;
      for(int i=0; i < src.length; i++) {
         if(src[i]>max) max=src[i];
      }
      return max;
   }
   
   public static void main(String[] args) {
      final File soundFile=new File("a880-voice.wav.wav");
      
      AudioInputStream audioInputStream=null;
      try {
         audioInputStream=AudioSystem.getAudioInputStream(soundFile);
      } catch(Exception e) {
         e.printStackTrace();
         System.exit(1);
      }

      AudioFormat audioFormat=audioInputStream.getFormat();
      System.err.println(audioFormat);
      //SourceDataLine line=null;
//      DataLine.Info info=new DataLine.Info(SourceDataLine.class, audioFormat);
//      try {
//         //System.err.println(info);
//         line=(SourceDataLine) AudioSystem.getLine(info);
//         line.open(audioFormat);
//      } catch(LineUnavailableException e) {
//         e.printStackTrace();
//         System.exit(1);
//      } catch(Exception e) {
//         e.printStackTrace();
//         System.exit(1);
//      }

      //line.start();
      LineDiagram lineDiagram=new LineDiagram();
      
      java.util.List<Float> outSamples=new java.util.ArrayList<Float>();
      java.util.List<Float> out2Samples=new java.util.ArrayList<Float>();
      final int EXTERNAL_BUFFER_SIZE=1024;//128000;
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
            
            lineDiagram.addData(in);
            
            float[] out=new float[in.length];
            float[] out2=new float[in.length];
            
            FFT.fft(in.length, false, in, null, out, out2);
            for (j = 0; j < in.length; j++)
               in[j] = (out[j] * out[j]) + (out2[j] * out2[j]);

            for (j = 0; j < in.length; j++)
               in[j] = (float)Math.pow(in[j], 1.0f / 3.0f);
            //System.exit(0);
            FFT.fft(in.length, false, in, null, out, out2);
            
            //lineDiagram.addData(out);
            //lineDiagram.addData(out2);
            /*outSamples.add(findMax(out));
            out2Samples.add(findMax(out2));*/
         }
      }
      /*System.err.println(outSamples);
      System.err.println(out2Samples);*/
      final JFrame jf=new JFrame();
      jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      jf.add(new JScrollPane(lineDiagram));
      //jf.pack();
      jf.setSize(400, 300);
      jf.setVisible(true);
      //line.drain();
      //line.close();
      
      
      //System.exit(0);
   }
}
