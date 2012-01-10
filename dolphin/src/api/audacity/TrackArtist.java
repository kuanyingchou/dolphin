//[ this file is mostly translated from Audacity

package api.audacity;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import api.util.Util;

class SpecCache {
   public SpecCache(int cacheLen, int half, boolean autocorrelation) {
      windowTypeOld=-1;
      windowSizeOld=-1;
      dirty=-1;
      start=-1.0;
      pps=0.0;
      len=cacheLen;
      ac=autocorrelation;
      freq=new float[len * half];
      where=new int[len + 1];
   }

   int windowTypeOld;
   int windowSizeOld;
   int dirty;
   boolean ac;
   int len;
   double start;
   double pps;
   int[] where;
   float[] freq;
};

class SpecPxCache {
   public SpecPxCache(int cacheLen) {
      len=cacheLen;
      values=new float[len];
      valid=false;
   }

   public int len;
   public float[] values;
   public boolean valid;
};

class Sequence {
   public static int int16Sample=0x00020001;
   public static int int24Sample=0x00040001;
   public static int floatSample=0x0004000F;

   java.util.List<Float> samples=new ArrayList<Float>();

   public void addSample(float d) {
      samples.add(d);
   }
   public void addSamples(float[] d) {
      for(int i=0; i < d.length; i++) {
         samples.add(d[i]);
      }
   }
   
   public int GetNumSamples() {
      // TODO Auto-generated method stub
      return samples.size();
   }

   public boolean Get(float[] buffer, int format, int start, int len) {
      if(start < 0 || start > samples.size() || start + len > samples.size())
         return false;
      //int bufferIdx=0;
      for(int i=0; i < len; i++) {
         buffer[i/*bufferIdx++*/]=samples.get(i + start);
      }
      return true;
      /*
       * int b = FindBlock(start);
       * 
       * while (len) { sampleCount blen = mBlock->Item(b)->start +
       * mBlock->Item(b)->f->GetLength() - start; if (blen > len) blen = len;
       * int bstart = (start - (mBlock->Item(b)->start));
       * 
       * Read(buffer, format, mBlock->Item(b), bstart, blen);
       * 
       * len -= blen; buffer += (blen SAMPLE_SIZE(format)); b++; start += blen;
       * }
       * 
       * return true;
       */
   }
}

class WaveTrack {
}

class WaveClip {
   SpecPxCache mSpecPxCache;
   SpecCache mSpecCache;
   
   int mRate;
   Sequence mSequence;

   public WaveClip(Sequence s, int rate) {
      mSequence=s;
      mRate=rate;
      //System.err.println(rate);
   }
   public double GetRate() {
      return mRate;
   }

   public double GetOffset() {
      return 0;
   }

   public int GetNumSamples() {
      return mSequence.GetNumSamples();
   }

   public double GetEndTime() {
      int numSamples = mSequence.GetNumSamples();
      
      double maxLen = (double)(numSamples)/mRate;
      // JS: calculated value is not the length;
      // it is a maximum value and can be negative; no clipping to 0
      
      return maxLen;
   }

   public int GetStartTime() {
      return 0;
   }

   public boolean GetSpectrogram(float[] freq, int[] where, int numPixels,
         double t0, double pixelsPerSecond, boolean autocorrelation) {
      int windowType=3;
      int windowSize=TrackArtist.WINDOW_SIZE;
      int half=windowSize / 2;

      SpecCache oldCache=mSpecCache;

      mSpecCache=new SpecCache(numPixels, half, autocorrelation);
      mSpecCache.pps=pixelsPerSecond;
      mSpecCache.start=t0;

      int x;

      boolean[] recalc=new boolean[mSpecCache.len + 1];

      for(x=0; x < mSpecCache.len + 1; x++) {
         recalc[x]=true;
         // purposely offset the display 1/2 bin to the left (as compared
         // to waveform display to properly center response of the FFT
         mSpecCache.where[x]=(int) Math.floor((t0 * mRate)
               + (x * mRate / pixelsPerSecond) + 1.);
      }

      float[] buffer=new float[windowSize];

      mSpecCache.windowTypeOld=windowType;
      mSpecCache.windowSizeOld=windowSize;

      for(x=0; x < mSpecCache.len; x++) {
         if(recalc[x]) {

            int start=mSpecCache.where[x];
            int len=windowSize;
            int i;

            if(start <= 0 || start >= mSequence.GetNumSamples()) {

               for(i=0; i < half; i++) {
                  mSpecCache.freq[half * x + i]=0;
               }

            } else {
               float[] adj=buffer;
               start-=windowSize >> 1;
               int adjIndex=0;

               if(start < 0) {
                  for(i=start; i < 0; i++)
                     adj[adjIndex++]=0;
                  len+=start;
                  start=0;
               }
               if(start + len > mSequence.GetNumSamples()) {
                  len=mSequence.GetNumSamples() - start;
                  for(i=len; i < windowSize; i++)
                     adj[i]=0;
               }

               if (len > 0) {
                  boolean r=mSequence.Get(adj, Sequence.floatSample, start, len);
                  //System.err.println(r);
               }
/*System.err.println(Arrays.toString(adj));
dbcount++;
if(dbcount==100) System.exit(0);*/
               ComputeSpectrum(buffer, windowSize, windowSize, mRate,
                     mSpecCache.freq, half * x, autocorrelation, windowType);
            }
         }
      }
/*System.err.println(Arrays.toString(mSpecCache.freq));
dbcount++;
if(dbcount==100) System.exit(0);*/
      // mSpecCache.dirty = mDirty;
      //System.err.println(mSpecCache.freq.length);
      System.arraycopy(mSpecCache.freq, 0, freq, 0, numPixels * half);
      System.arraycopy(mSpecCache.where, 0, where, 0, numPixels + 1);
      return true;
   }
   int dbcount=0;
   public static void WindowFunc(int whichFunction, int NumSamples, float[] in) {
      //if(whichFunction == 3) {
         for(int i=0; i < NumSamples; i++)
            in[i]*=0.50 - 0.50 * Math.cos(2 * Math.PI * i / (NumSamples - 1));
      //}
   }
public static void PowerSpectrum(int NumSamples, float[] In, float[] Out)
{
   int Half = NumSamples / 2;
   int i;

   float theta = (float) (Math.PI / Half);

   float[] tmpReal = new float[Half];
   float[] tmpImag = new float[Half];
   float[] RealOut = new float[Half];
   float[] ImagOut = new float[Half];

   for (i = 0; i < Half; i++) {
      tmpReal[i] = In[2 * i];
      tmpImag[i] = In[2 * i + 1];
   }

   FFT.fft(Half, false, tmpReal, tmpImag, RealOut, ImagOut);

   float wtemp = (float) Math.sin(0.5 * theta);

   float wpr = -2.0f * wtemp * wtemp;
   float wpi = (float) (Math.sin(theta));
   float wr = 1.0f + wpr;
   float wi = wpi;

   int i3;

   float h1r, h1i, h2r, h2i, rt, it;

   for (i = 1; i < Half / 2; i++) {

      i3 = Half - i;

      h1r = 0.5f * (RealOut[i] + RealOut[i3]);
      h1i = 0.5f * (ImagOut[i] - ImagOut[i3]);
      h2r = 0.5f * (ImagOut[i] + ImagOut[i3]);
      h2i = -0.5f * (RealOut[i] - RealOut[i3]);

      rt = h1r + wr * h2r - wi * h2i;
      it = h1i + wr * h2i + wi * h2r;

      Out[i] = rt * rt + it * it;

      rt = h1r - wr * h2r + wi * h2i;
      it = -h1i + wr * h2i + wi * h2r;

      Out[i3] = rt * rt + it * it;

      wr = (wtemp = wr) * wpr - wi * wpi + wr;
      wi = wi * wpr + wtemp * wpi + wi;
   }

   rt = (h1r = RealOut[0]) + ImagOut[0];
   it = h1r - ImagOut[0];
   Out[0] = rt * rt + it * it;

   rt = RealOut[Half / 2];
   it = ImagOut[Half / 2];
   Out[Half / 2] = rt * rt + it * it;

 
} 
   
   public static boolean ComputeSpectrum(
         float[] data, int width,
         int windowSize, double rate, 
         float[] output, int outOffset, boolean autocorrelation,
         int windowFunc) {
      if(width < windowSize)
         return false;

      if(data == null || output == null)
         return true;

      float[] processed=new float[windowSize];

      int i;
      for(i=0; i < windowSize; i++)
         processed[i]=0.0f;
      int half=windowSize / 2;

      float[] in=new float[windowSize];
      float[] out=new float[windowSize];
      float[] out2=new float[windowSize];

      int start=0;
      int windows=0;
      while(start + windowSize <= width) {
         for(i=0; i < windowSize; i++)
            in[i]=data[start + i];

         WindowFunc(windowFunc, windowSize, in);

         if(autocorrelation) {
            // Take FFT
            FFT.fft(windowSize, false, in, null, out, out2);

            // Compute power
            for(i=0; i < windowSize; i++)
               in[i]=(out[i] * out[i]) + (out2[i] * out2[i]);

            // Tolonen and Karjalainen recommend taking the cube root
            // of the power, instead of the square root

            for(i=0; i < windowSize; i++)
               in[i]=(float) Math.pow(in[i], 1.0f / 3.0f);

            // Take FFT
            FFT.fft(windowSize, false, in, null, out, out2);

         } else {
            PowerSpectrum(windowSize, in, out);
         }
            // Take real part of result
         for(i=0; i < half; i++)
            processed[i]+=out[i];

         start+=half;
         windows++;
      }

      if(autocorrelation) {

         // Peak Pruning as described by Tolonen and Karjalainen, 2000
         /*
          * Combine most of the calculations in a single for loop. It should be
          * safe, as indexes refer only to current and previous elements, that
          * have already been clipped, etc...
          */
         for(i=0; i < half; i++) {
            // Clip at zero, copy to temp array
            if(processed[i] < 0.0)
               processed[i]=0.0f;
            out[i]=processed[i];
            // Subtract a time-doubled signal (linearly interp.) from the
            // original
            // (clipped) signal
            if((i % 2) == 0)
               processed[i]-=out[i / 2];
            else
               processed[i]-=((out[i / 2] + out[i / 2 + 1]) / 2);

            // Clip at zero again
            if(processed[i] < 0.0)
               processed[i]=0.0f;
         }

         // Reverse and scale
         for(i=0; i < half; i++)
            in[i]=processed[i] / (windowSize / 4);
         for(i=0; i < half; i++)
            processed[half - 1 - i]=in[i];
      } else {
         // Convert to decibels
         // But do it safely; -Inf is nobody's friend
         for(i=0; i < half; i++) {
            float temp=(processed[i] / windowSize / windows);
            if(temp > 0.0)
               processed[i]=(float) (10 * Math.log10(temp));
            else
               processed[i]=0;
         }
      }

      for(i=0; i < half; i++)
         output[i+outOffset]=processed[i];

      return true;
   }

   public void SetDisplayRect(Rectangle mid) {
      // TODO Auto-generated method stub

   }
}

class ViewInfo {
   //double sel0;
   //double sel1;

   // Scroll info

   //int vpos; // vertical scroll pos

   double h=0; // h pos in secs
   //double screen; // screen width in secs
   //double total; // total width in secs
   double zoom=TrackArtist.WINDOW_SIZE; // pixels per second
   //double lastZoom;

   // Current horizontal scroll bar positions, in pixels
   //long sbarH;
   //long sbarScreen;
   //long sbarTotal;

   // Internal wxScrollbar positions are only int in range, so multiply
   // the above values with the following member to get the actual
   // scroll bar positions as reported by the horizontal wxScrollbar's members
   //double sbarScale;

   // Vertical scroll step
   //int scrollStep;

   // Other stuff, mainly states (true or false) related to autoscroll and
   // drawing the waveform. Maybe this should be put somewhere else?

   //boolean bRedrawWaveform;
   //boolean bUpdateTrackIndicator;

   //boolean bIsPlaying;
}

public class TrackArtist {
   public static final int WINDOW_SIZE=1024;
   public static void drawClipSpectrum(WaveTrack track, WaveClip clip, Graphics dc,
         Rectangle r, ViewInfo viewInfo, boolean autocorrelation) {
      double h=viewInfo.h;
      double pps=viewInfo.zoom;
      /*
       * double sel0 = viewInfo.sel0; double sel1 = viewInfo.sel1;
       */

      int numSamples=clip.GetNumSamples();
      double tOffset=clip.GetOffset();
      double rate=clip.GetRate();
      double sps=1. / rate;
      
      
      // if nothing is on the screen
      if((int) (h * rate + 0.5) >= numSamples)
         return;
      
      /*
       * if (!track.GetSelected()) sel0 = sel1 = 0.0;
       */

      double tpre=h - tOffset;
      double tstep=1.0 / pps;
      double tpost=tpre + (r.width * tstep);
      double trackLen=clip.GetEndTime() - clip.GetStartTime();

      boolean showIndividualSamples=(pps / rate > 0.5); // zoomed in a lot
      double t0=(tpre >= 0.0 ? tpre : 0.0);
      double t1=(tpost < trackLen - sps * .99 ? tpost : trackLen - sps * .99);
      if(showIndividualSamples)
         t1+=2. / pps; // for display consistency
      // with Waveform display

      // Make sure t1 (the right bound) is greater than 0
      if(t1 < 0.0)
         t1=0.0;

      // Make sure t1 is greater than t0
      if(t0 > t1)
         t0=t1;

      /*
       * long ssel0 = Math.max(0, (int)((sel0 - tOffset) rate + .99)); long
       * ssel1 = Math.max(0, (int)((sel1 - tOffset) rate + .99));
       */

      // trim selection so that it only contains the actual samples
      /*
       * if (ssel0 != ssel1 && ssel1 > (int)(0.5+trackLenrate)) ssel1 =
       * (int)(0.5+trackLenrate);
       */
      
      // The variable "mid" will be the rectangle containing the
      // actual waveform, as opposed to any blank area before
      // or after the track.
      Rectangle mid=r;

      // dc.SetPen(*wxTRANSPARENT_PEN);

      // If the left edge of the track is to the right of the left
      // edge of the display, then there's some blank area to the
      // left of the track. Reduce the "mid"
      // rect by size of the blank area.
      if(tpre < 0) {
         // Fill in the area to the left of the track
         Rectangle pre=r;
         if(t0 < tpost)
            pre.width=(int) ((t0 - tpre) * pps);

         // Offset the rectangle containing the waveform by the width
         // of the area we just erased.
         mid.x+=pre.width;
         mid.width-=pre.width;
      }
      
      // If the right edge of the track is to the left of the the right
      // edge of the display, then there's some blank area to the right
      // of the track. Reduce the "mid" rect by the
      // size of the blank area.
      if(tpost > t1) {
         Rectangle post=r;
         if(t1 > tpre)
            post.x+=(int) ((t1 - tpre) * pps);
         post.width=r.width - (post.x - r.x);

         // Reduce the rectangle containing the waveform by the width
         // of the area we just erased.
         mid.width-=post.width;
      }

      // The "mid" rect contains the part of the display actually
      // containing the waveform. If it's empty, we're done.

      if(mid.width <= 0) {
         return;
      }
      BufferedImage image=new BufferedImage(mid.width, mid.height,
            BufferedImage.TYPE_INT_RGB);
      
      if(image == null) {
         return;
      }
      
      // unsigned char *data = image->GetData();

      int windowSize=WINDOW_SIZE;
      int half=windowSize / 2;
      float[] freq=new float[mid.width * half];
      int[] where=new int[mid.width + 1];

      boolean updated=clip.GetSpectrogram(freq, where, mid.width, t0, pps,
            autocorrelation);
      boolean isGrayscale=false;
      int ifreq=(int) Math.round(rate / 2); // lrint???

      int maxFreq=ifreq;
      if(maxFreq > ifreq)
         maxFreq=ifreq;
      int minFreq=0;
//System.err.println(maxFreq);
      // boolean usePxCache = false;

      clip.mSpecPxCache=new SpecPxCache(mid.width * mid.height);

      int minSamples=(int) (minFreq * windowSize / rate + 0.5); // units are fft
                                                                // bins
      int maxSamples=(int) (maxFreq * windowSize / rate + 0.5);
      float binPerPx=(float) (maxSamples - minSamples) / (float) (mid.height);

      int x=0;
      int w1=(int) ((t0 * rate + x * rate * tstep) + .5);
      
      while (x < mid.width) 
      {
         int w0 = w1;
         w1 = (int) ((t0*rate + (x+1) *rate *tstep) + .5);

            for (int yy = 0; yy < mid.height; yy++) {
            //bool selflag = (ssel0 <= w0 && w1 < ssel1);
            //int rv, gv, bv;
            float value;

            
               float bin0 = (float) (yy) * binPerPx + minSamples;
               float bin1 = (float) (yy + 1) * binPerPx + minSamples;
//System.err.println(bin0+" - "+bin1);

               float fbin0=0;
               if(half * x + (int) (bin0) < freq.length) {
                  fbin0=freq[half * x + (int) (bin0)];      
               }
               float fbin1=0;
               if(half * x + (int) (bin1) < freq.length) {
                  fbin1=freq[half * x + (int) (bin1)];      
               }
               if ((int) (bin1) == (int) (bin0)) {
                  value = fbin0;
               } else {
                  float binwidth= bin1 - bin0;
                  value = fbin0 * 
                          (1.f - bin0 + (int)bin0);

                  bin0 = 1 + (int) (bin0);

                  while (bin0 < (int) (bin1)) {
                     value += fbin0;
                     bin0 += 1.0;
                  }
                  
                  value += fbin1 * 
                           (bin1 - (int) (bin1));

                  value /= binwidth;
               }
               //System.err.println(value);
               if (!autocorrelation) {
                  // Last step converts dB to a 0.0-1.0 range
                  value = (value + 80.0f) / 80.0f;
               }

               if (value > 1.0) {
                  value = 1.0f;
               }
               if (value < 0.0) {
                  value = 0.0f;
               }
               clip.mSpecPxCache.values[x * mid.height + yy] = value;
            
            
            final Color cg=GetColorGradient(value, false, isGrayscale);

            //int px = ((mid.height - 1 - yy) * mid.width + x) * 3;
            
            image.setRGB(x, yy, cg.getRGB());
            //System.err.println(x+", "+yy+"="+cg.getRGB());
         }
         x++;
      }
      //clip.SetDisplayRect(mid);
      dc.drawImage(image, 0, 0, null);
      /*
       * wxBitmap converted = wxBitmap(image);
       * 
       * wxMemoryDC memDC;
       * 
       * memDC.SelectObject(converted);
       * 
       * dc.Blit(mid.x, mid.y, mid.width, mid.height, &memDC, 0, 0, wxCOPY,
       * FALSE);
       */
      dc.setColor(Color.black);
      dc.drawRect(0, 0, image.getWidth()-1, image.getHeight()-1);
      int steps=128;
      float heightStep=image.getHeight()/steps;
      float freqStep=(float)(maxFreq-minFreq)/steps;
      for(int i=0; i < steps; i++) {
         final int y=(int)(i*heightStep);
         dc.drawLine(0, y, 10, y);
         final float f=freqStep*i;
         final int p=(int)(69+12*Util.log2(f/440.0f));
         dc.drawString(String.valueOf(p+":"+f+"Hz"), 
               10, y);
      }
   }
   
   public static Color GetColorGradient(float value, boolean selected,
         boolean grayscale) {
      float r, g, b;

      if(grayscale) {
         r=g=b=0.84f - 0.84f * value;
      } else {
         final int gsteps=4;
         float[][] gradient=new float[][] { 
               { (0.75f), (0.75f), (0.75f) }, // lt gray
               { (0.30f), (0.60f), (1.00f) }, // lt blue
               { (0.90f), (0.10f), (0.90f) }, // violet
               { (1.00f), (0.00f), (0.00f) }, // red
               { (1.00f), (1.00f), (1.00f) } // white
         };

         int left=(int) (value * gsteps);
         int right=(left == gsteps ? gsteps : left + 1);

         float rweight=(value * gsteps) - left;
         float lweight=1.0f - rweight;

         r=(gradient[left][0] * lweight) + (gradient[right][0] * rweight);
         g=(gradient[left][1] * lweight) + (gradient[right][1] * rweight);
         b=(gradient[left][2] * lweight) + (gradient[right][2] * rweight);
      }

      /* if (selected) { r *= 0.77f; g *= 0.77f; b *= 0.885f; } */

      return new Color((int) (255 * r), (int) (255 * g), (int) (255 * b));

   }
   
   public static void main(String[] args) {
      final int EXTERNAL_BUFFER_SIZE=128000;
      final File soundFile=new File("a880-voice.wav.wav");
      AudioInputStream audioInputStream=null;
      try {
         audioInputStream=AudioSystem.getAudioInputStream(soundFile);
      } catch(Exception e) {
         e.printStackTrace();
         System.exit(1);
      }
      final Sequence sequence=new Sequence();
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
            sequence.addSamples(in);
            //lineDiagram.addData(-1);
         }
      }
      //System.err.println(sequence.samples);
      AudioFormat audioFormat=audioInputStream.getFormat();
      final WaveClip clip=new WaveClip(sequence, (int)audioFormat.getSampleRate());
      final ViewInfo viewInfo=new ViewInfo();
      
      final JFrame jf=new JFrame();
      jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      jf.add(new JComponent() {
         @Override
         protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            TrackArtist.drawClipSpectrum(
                  null, clip, g, getBounds(), viewInfo, false);
         }
      });
      jf.setSize(400, 300);
      jf.setVisible(true);
      
      final int p=(int)(69+12*Util.log2(435.9/440.0f));
      System.err.println(p);
   }
}
