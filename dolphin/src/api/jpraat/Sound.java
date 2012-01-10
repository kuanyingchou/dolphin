package api.jpraat;
import api.util.Util;

public class Sound extends Vector {
   public static final int kSound_windowShape_RECTANGULAR=0;
   public static final int kSound_windowShape_TRIANGULAR=1;
   public static final int kSound_windowShape_PARABOLIC=2;
   public static final int kSound_windowShape_HANNING=3;
   public static final int kSound_windowShape_HAMMING=4;
   public static final int kSound_windowShape_GAUSSIAN_1=5;
   public static final int kSound_windowShape_GAUSSIAN_2=6;
   public static final int kSound_windowShape_GAUSSIAN_3=7;
   public static final int kSound_windowShape_GAUSSIAN_4=8;
   public static final int kSound_windowShape_GAUSSIAN_5=9;
   public static final int kSound_windowShape_KAISER_1=10;
   public static final int kSound_windowShape_KAISER_2=11; // ] >>> temp values

   private static final int MAXIMUM_NUMBER_OF_CHANNELS=3;
   public static final int AC_HANNING=0;
   public static final int AC_GAUSS=1;
   public static final int FCC_NORMAL=2;
   public static final int FCC_ACCURATE=3;
   public static final int NUM_PEAK_INTERPOLATE_NONE=0;
   public static final int NUM_PEAK_INTERPOLATE_PARABOLIC=1;
   public static final int NUM_PEAK_INTERPOLATE_CUBIC=2;
   public static final int NUM_PEAK_INTERPOLATE_SINC70=3;
   public static final int NUM_PEAK_INTERPOLATE_SINC700=4;

   private Sound() {
   }
   public Sound(Sound that) {
      this.description=null; //>>>
      this.dx=that.dx;
      this.dy=that.dy;
      this.nx=that.nx;
      this.ny=that.ny;
      this.x1=that.x1;
      this.y1=that.y1;
      this.xmax=that.xmax;
      this.xmin=that.xmin;
      this.ymax=that.ymax;
      this.ymin=that.ymin;
      this.z=new double[that.z.length][];
      for(int i=0; i < this.z.length; i++) {
         this.z[i]=new double[that.z[i].length];
         System.arraycopy(that.z[i], 0, this.z[i], 0, that.z[i].length);
      }
      
   }

   public static Sound Sound_create(int numberOfChannels, double xmin,
         double xmax, int nx, double dx, double x1) {
      Sound res=new Sound();
      if(res.Matrix_init(xmin, xmax, nx, dx, x1, 1, numberOfChannels,
            numberOfChannels, 1, 1) == 0) {
         // forget (res);
      }
      return res;
   }

   public static Sound Sound_createSimple(int numberOfChannels, double duration,
         double samplingFrequency) {
      return Sound_create(numberOfChannels, 0.0, duration, (int) Math
            .floor(duration * samplingFrequency + 0.5), 1 / samplingFrequency,
            0.5 / samplingFrequency);
   }

   /*
    * public static Sound Sound_create_test() { final Sound s=Sound_create(1, 0,
    * 1, 44100, 1/44100.0, 0.5/44100.0); return s; }
    */

   void Sound_multiplyByWindow(int windowShape) {
      for(int channel=1; channel <= this.ny; channel++) {
         int i, n=(int) this.nx;
         double[] amp=this.z[channel];
         double imid, edge, onebyedge1, factor;
         switch(windowShape) {
         case kSound_windowShape_RECTANGULAR:
            ;
            break;
         case kSound_windowShape_TRIANGULAR: /* "Bartlett" */
            for(i=1; i <= n; i++) {
               double phase=(double) i / n; /* 0..1 */
               amp[i]*=1.0 - Math.abs((2.0 * phase - 1.0));
            }
            break;
         case kSound_windowShape_PARABOLIC: /* "Welch" */
            for(i=1; i <= n; i++) {
               double phase=(double) i / n;
               amp[i]*=1.0 - (2.0 * phase - 1.0) * (2.0 * phase - 1.0);
            }
            break;
         case kSound_windowShape_HANNING:
            for(i=1; i <= n; i++) {
               double phase=(double) i / n;
               amp[i]*=0.5 * (1.0 - Math.cos(2.0 * Math.PI * phase));
            }
            break;
         case kSound_windowShape_HAMMING:
            for(i=1; i <= n; i++) {
               double phase=(double) i / n;
               amp[i]*=0.54 - 0.46 * Math.cos(2.0 * Math.PI * phase);
            }
            break;
         case kSound_windowShape_GAUSSIAN_1:
            imid=0.5 * (n + 1);
            edge=Math.exp(-3.0);
            onebyedge1=1 / (1.0 - edge); /* -0.5..+0.5 */
            for(i=1; i <= n; i++) {
               double phase=((double) i - imid) / n;
               amp[i]*=(Math.exp(-12.0 * phase * phase) - edge) * onebyedge1;
            }
            break;
         case kSound_windowShape_GAUSSIAN_2:
            imid=0.5 * (double) (n + 1);
            edge=Math.exp(-12.0);
            onebyedge1=1 / (1.0 - edge);
            for(i=1; i <= n; i++) {
               double phase=((double) i - imid) / n;
               amp[i]*=(Math.exp(-48.0 * phase * phase) - edge) * onebyedge1;
            }
            break;
         case kSound_windowShape_GAUSSIAN_3:
            imid=0.5 * (double) (n + 1);
            edge=Math.exp(-27.0);
            onebyedge1=1 / (1.0 - edge);
            for(i=1; i <= n; i++) {
               double phase=((double) i - imid) / n;
               amp[i]*=(Math.exp(-108.0 * phase * phase) - edge) * onebyedge1;
            }
            break;
         case kSound_windowShape_GAUSSIAN_4:
            imid=0.5 * (double) (n + 1);
            edge=Math.exp(-48.0);
            onebyedge1=1 / (1.0 - edge);
            for(i=1; i <= n; i++) {
               double phase=((double) i - imid) / n;
               amp[i]*=(Math.exp(-192.0 * phase * phase) - edge) * onebyedge1;
            }
            break;
         case kSound_windowShape_GAUSSIAN_5:
            imid=0.5 * (double) (n + 1);
            edge=Math.exp(-75.0);
            onebyedge1=1 / (1.0 - edge);
            for(i=1; i <= n; i++) {
               double phase=((double) i - imid) / n;
               amp[i]*=(Math.exp(-300.0 * phase * phase) - edge) * onebyedge1;
            }
            break;
         case kSound_windowShape_KAISER_1:
            imid=0.5 * (double) (n + 1);
            factor=1 / NUM.NUMbessel_i0_f(2 * Math.PI);
            for(i=1; i <= n; i++) {
               double phase=2 * ((double) i - imid) / n; /* -1..+1 */
               double root=1 - phase * phase;
               amp[i]*=root <= 0.0 ? 0.0 : factor
                     * NUM.NUMbessel_i0_f(2 * Math.PI * Math.sqrt(root));
            }
            break;
         case kSound_windowShape_KAISER_2:
            imid=0.5 * (double) (n + 1);
            factor=1 / NUM.NUMbessel_i0_f(2 * Math.PI * Math.PI + 0.5);
            for(i=1; i <= n; i++) {
               double phase=2 * ((double) i - imid) / n; /* -1..+1 */
               double root=1 - phase * phase;
               amp[i]*=root <= 0.0 ? 0.0 : factor
                     * NUM.NUMbessel_i0_f((2 * Math.PI * Math.PI + 0.5)
                           * Math.sqrt(root));
            }
            break;
         default:
            break;
         }
      }
   }

   Sound Sound_extractPart(double t1, double t2, int windowShape,
         double relativeWidth, boolean preserveTimes) {
      Sound thee=null;
      int ix1, ix2;
      /*
       * We do not clip to the Sound's time domain. Any samples outside it are
       * taken to be zero.
       */

      /*
       * Autowindow.
       */
      if(t1 == t2) {
         t1=this.xmin;
         t2=this.xmax;
      }
      ;
      /*
       * Allow window tails outside specified domain.
       */
      if(relativeWidth != 1.0) {
         double margin=0.5 * (relativeWidth - 1) * (t2 - t1);
         t1-=margin;
         t2+=margin;
      }
      /*
       * Determine index range. We use all the real or virtual samples that fit
       * within [t1..t2].
       */
      ix1=1 + (int) Math.ceil((t1 - this.x1) / this.dx);
      ix2=1 + (int) Math.floor((t2 - this.x1) / this.dx);
      if(ix2 < ix1)
         throw new RuntimeException("Extracted Sound would contain no samples.");
      /*
       * Create sound, optionally shifted to [0..t2-t1].
       */
      thee=Sound_create(this.ny, t1, t2, ix2 - ix1 + 1, this.dx, this.x1
            + (ix1 - 1) * this.dx);
      // cherror //>>>
      if(!preserveTimes) {
         thee.xmin=0.0;
         thee.xmax-=t1;
         thee.x1-=t1;
      }
      /*
       * Copy onlyreal samples into the new sound. Thevirtual samples will
       * remain at zero.
       */
      for(int channel=1; channel <= this.ny; channel++) {
         /*
          * NUMdvector_copyElements (this. z [channel], thee. z [channel] + 1 -
          * ix1, ( ix1 < 1 ? 1 : ix1 ), ( ix2 > this. nx ? this. nx : ix2 ));
          */
         final int start=(int) (ix1 < 1 ? 1 : ix1);
         final int end=(int) (ix2 > this.nx ? this.nx : ix2);
         System.arraycopy(this.z[channel], start, thee.z[channel], start
               + (1 - ix1), end - start + 1);
      }
      /*
       * Multiply by a window that extends throughout the target domain.
       */
      thee.Sound_multiplyByWindow(windowShape);
      // end:
      // iferror { forget (thee); return Melder_errorp
      // ("(Sound_extractPart:) Not performed."); } //>>>
      return thee;
   }

   Pitch Sound_to_Pitch_any(double dt, double minimumPitch,
         double periodsPerWindow, int maxnCandidates, int method,
         double silenceThreshold, double voicingThreshold, double octaveCost,
         double octaveJumpCost, double voicedUnvoicedCost, double ceiling) {
      
       if(this.ny > MAXIMUM_NUMBER_OF_CHANNELS) {
         throw new RuntimeException();
         // return Melder_errorp
         // ("Pitch analysis cannot yet work with more than two channels.");
         // //>>>
      }
      double duration;
      FirstTime t1=new FirstTime();
      Pitch thee=null;
      int i, j;
      double dt_window; /* Window length in seconds. */
      int nsamp_window, halfnsamp_window; /* Number of samples per window. */
      NumberOfFrames nFrames=new NumberOfFrames();
      int minimumLag, maximumLag;
      int iframe, nsampFFT=0; // >>> initial value?
      int[] imax=null;
      double[][] frame=null;
      double[] ac=null;
      double[] r=null;
      double[] window=null, windowR=null;
      double globalPeak;
      double interpolation_depth=0; // >>> initial value?
      int nsamp_period, halfnsamp_period; /*
                                           * Number of samples in longest
                                           * period.
                                           */
      int brent_ixmax, brent_depth=0; // >>> initial value?
      // double brent_accuracy; /* Obsolete. */
      NUMfft_Table fftTable=new NUMfft_Table(); // { 0 }; //>>>

      assert (maxnCandidates >= 2);
      assert (method >= AC_HANNING && method <= FCC_ACCURATE);

      if(maxnCandidates < ceiling / minimumPitch)
         maxnCandidates=(int) (ceiling / minimumPitch);

      if(dt <= 0.0)
         dt=periodsPerWindow / minimumPitch / 4.0; /*
                                                    * e.g. 3 periods, 75 Hz: 10
                                                    * milliseconds.
                                                    */

      // Melder_progress1 (0.0, "Sound to Pitch..."); //>>>
      switch(method) {
      case AC_HANNING:
         brent_depth=NUM_PEAK_INTERPOLATE_SINC70;
         // brent_accuracy=1e-7;
         interpolation_depth=0.5;
         break;
      case AC_GAUSS:
         periodsPerWindow*=2; /* Because Gaussian window is twice as long. */
         brent_depth=NUM_PEAK_INTERPOLATE_SINC700;
         // brent_accuracy=1e-11;
         interpolation_depth=0.25; /* Because Gaussian window is twice as long. */
         break;
      case FCC_NORMAL:
         brent_depth=NUM_PEAK_INTERPOLATE_SINC70;
         // brent_accuracy=1e-7;
         interpolation_depth=1.0;
         break;
      case FCC_ACCURATE:
         brent_depth=NUM_PEAK_INTERPOLATE_SINC700;
         // brent_accuracy=1e-11;
         interpolation_depth=1.0;
         break;
      }
      duration=this.dx * this.nx;
      if(minimumPitch < periodsPerWindow / duration) {
         throw new RuntimeException();
         // System.err.println("error occurred");
         // error3
         //("For this Sound, the parameter 'minimum pitch' may not be less than "
         // , Melder_single (periodsPerWindow / duration), " Hz.") //>>>
      }
      /*
       * Determine the number of samples in the longest period. We need this to
       * compute the local mean of the sound (looking one period in both
       * directions), and to compute the local peak of the sound (looking half a
       * period in both directions).
       */
      nsamp_period=(int) Math.floor(1 / this.dx / minimumPitch);
      halfnsamp_period=nsamp_period / 2 + 1;

      if(ceiling > 0.5 / this.dx)
         ceiling=0.5 / this.dx;

      /*
       * Determine window length in seconds and in samples.
       */
      dt_window=periodsPerWindow / minimumPitch;
      nsamp_window=(int) Math.floor(dt_window / this.dx);
      halfnsamp_window=nsamp_window / 2 - 1;
      if(halfnsamp_window < 2) {
         throw new RuntimeException("Analysis window too short.");
      }
      nsamp_window=halfnsamp_window * 2;

      /*
       * Determine the minimum and maximum lags.
       */
      minimumLag=(int) Math.floor(1 / this.dx / ceiling);
      if(minimumLag < 2)
         minimumLag=2;
      maximumLag=(int) Math.floor(nsamp_window / periodsPerWindow) + 2;
      if(maximumLag > nsamp_window)
         maximumLag=nsamp_window;

      /*
       * Determine the number of frames. Fit as many frames as possible
       * symmetrically in the total duration. We do this even for the forward
       * cross-correlation method, because that allows us to compare the two
       * methods.
       */
      if(this.Sampled_shortTermAnalysis(method >= FCC_NORMAL ? 1 / minimumPitch
            + dt_window : dt_window, dt, nFrames, t1) != 0) {

         /*
          * Create the resulting pitch contour.
          */
         thee=Pitch.Pitch_create(this.xmin, this.xmax, nFrames.value, dt,
               t1.value, ceiling, maxnCandidates);
         // cherror //>>>

         /*
          * Compute the global absolute peak for determination of silence
          * threshold.
          */
         // System.err.println(z.length+", "+z[1].length);
         /*for(int s=1; s<this.nx; s++) {
            System.err.println(this.z[1][s]);
         }*/
         globalPeak=0.0; //ken: OK
         for(int channel=1; channel <= this.ny; channel++) {
            double mean=0.0;
            for(i=1; i <= this.nx; i++) {
               mean+=this.z[channel][i];
            }
            mean/=this.nx;
            for(i=1; i <= this.nx; i++) {
               double value=Math.abs(this.z[channel][i] - mean);
               if(value > globalPeak)
                  globalPeak=value;
            }
         }
         if(globalPeak == 0.0) {
            // Melder_progress1 (1.0, null); //>>>
            return thee;
         }

         if(method >= FCC_NORMAL) { /* For cross-correlation analysis. */

            /*
             * Create buffer for cross-correlation analysis.
             */
            frame=new double[1 + this.ny][1 + nsamp_window]; // NUM.NUMdmatrix
                                                             // (1,
            // this. ny, 1,
            // nsamp_window);
            // cherror //>>>

            brent_ixmax=(int) (nsamp_window * interpolation_depth);

         } else { /* For autocorrelation analysis. */

            /*
             * Compute the number of samples needed for doing FFT. To avoid edge
             * effects, we have to append zeroes to the window. The maximum lag
             * considered for maxima is maximumLag. The maximum lag used in
             * interpolation is nsamp_window interpolation_depth.
             */
            nsampFFT=1; //ken: ok
            while(nsampFFT < nsamp_window * (1 + interpolation_depth))
               nsampFFT*=2;

            /*
             * Create buffers for autocorrelation analysis.
             */
            frame=new double[1 + this.ny][1 + nsampFFT]; // NUM.NUMdmatrix (1,
            // this. ny, 1,
            // nsampFFT); //cherror
            windowR=new double[1 + nsampFFT]; // cherror
            window=new double[1 + nsamp_window]; // cherror
            fftTable.NUMfft_Table_init(nsampFFT); // cherror
            ac=new double[1 + nsampFFT]; // cherror >>>
            
            /*int index;
            for(index=0; index<fftTable.splitcache.length; index++) {
               System.err.printf("%d\n", fftTable.splitcache[index]);
            }
            for(index=0; index<fftTable.trigcache.length; index++) {
               System.err.printf("%f\n", fftTable.trigcache[index]);
            }*/
            /*
             * A Gaussian or Hanning window is applied against phase effects.
             * The Hanning window is 2 to 5 dB better for 3 periods/window. The
             * Gaussian window is 25 to 29 dB better for 6 periods/window.
             */
            if(method == AC_GAUSS) {  //Gaussian window. 
               double imid=0.5 * (nsamp_window + 1), edge=Math.exp(-12.0);
               for(i=1; i <= nsamp_window; i++)
                  window[i]=(Math.exp(-48.0 * (i - imid) * (i - imid)
                        / (nsamp_window + 1) / (nsamp_window + 1)) - edge)
                        / (1 - edge);
            } else {  //Hanning window. 
               for(i=1; i <= nsamp_window; i++)
                  window[i]=0.5 - 0.5 * Math.cos(i * 2 * Math.PI
                        / (nsamp_window + 1));
            }
            //ken: window ok

            /*
             * Compute the normalized autocorrelation of the window.
             */
            for(i=1; i <= nsamp_window; i++) {
               windowR[i]=window[i];
            }
            fftTable.NUMfft_forward(windowR);
            windowR[1]*=windowR[1]; /* DC component. */
            for(i=2; i < nsampFFT; i+=2) {
               windowR[i]=windowR[i] * windowR[i] + windowR[i + 1]
                     * windowR[i + 1];
               windowR[i + 1]=0.0; /* Power spectrum: square and zero. */
            }
            windowR[nsampFFT]*=windowR[nsampFFT]; /* Nyquist frequency. */

/*int index;
            for(index=0; index<fftTable.splitcache.length; index++) {
               System.err.printf("%d\n", fftTable.splitcache[index]);
            }
            for(index=0; index<fftTable.trigcache.length; index++) {
               System.err.printf("%f\n", fftTable.trigcache[index]);
            }*/
            fftTable.NUMfft_backward(windowR); /* Autocorrelation. */
/*for(int index=1; index<windowR.length; index++) {
            System.err.println(windowR[index]);
         }            */
            //ken: windowR ok
            for(i=2; i <= nsamp_window; i++)
               windowR[i]/=windowR[1]; /* Normalize. */
            windowR[1]=1.0; /* Normalize. */

            brent_ixmax=(int) (nsamp_window * interpolation_depth);
         }
/*for(int index=0; index<windowR.length; index++) {
   System.err.println(windowR[index]);
}*/
         /*int index;
         for(index=0; index<fftTable.splitcache.length; index++) {
            System.err.printf("%d\n", fftTable.splitcache[index]);
         }
         for(index=0; index<fftTable.trigcache.length; index++) {
            System.err.printf("%f\n", fftTable.trigcache[index]);
         }*/
         
         r=new double[1 + nsamp_window + nsamp_window]; // >>>
         imax=new int[1 + maxnCandidates];
         /*
          * if (! (r = NUMdvector (- nsamp_window, nsamp_window)) || ! (imax =
          * NUMlvector (1, maxnCandidates))) { forget (thee); goto end; }
          */// >>>
         for(iframe=1; iframe <= nFrames.value; iframe++) {
            Pitch_Frame pitchFrame=thee.frame[iframe];
            double t=thee.Sampled_indexToX(iframe), localPeak;
            int leftSample=(int) Sampled_xToLowIndex(t);
            int rightSample=leftSample + 1;
            int startSample, endSample;
            /*
             * if (! Melder_progress4 (0.1 + (0.8 iframe) / (nFrames.value + 1),
             * "Sound to Pitch: analysis of frame ", Melder_integer (iframe),
             * " out of ", Melder_integer (nFrames))) { forget (thee); goto end;
             * }
             */// >>>
            double[] localMean=new double[1 + MAXIMUM_NUMBER_OF_CHANNELS];
            for(int channel=1; channel <= this.ny; channel++) {
               /*
                * Compute the local mean; look one longest period to both sides.
                */
               startSample=rightSample - nsamp_period;
               endSample=leftSample + nsamp_period;
               assert (startSample >= 1);
               assert (endSample <= this.nx);
               localMean[channel]=0.0;
               for(i=startSample; i <= endSample; i++) {
                  localMean[channel]+=this.z[channel][i];
               }
               localMean[channel]/=2 * nsamp_period;

               /*
                * Copy a window to a frame and subtract the local mean. We are
                * going to kill the DC component before windowing.
                */
               startSample=rightSample - halfnsamp_window;
               endSample=leftSample + halfnsamp_window;
               assert (startSample >= 1);
               assert (endSample <= this.nx);
               if(method < FCC_NORMAL) {
                  for(j=1, i=startSample; j <= nsamp_window; j++)
                     frame[channel][j]=(this.z[channel][i++] - localMean[channel])
                           * window[j];
                  for(j=nsamp_window + 1; j <= nsampFFT; j++)
                     frame[channel][j]=0.0;
               } else {
                  for(j=1, i=startSample; j <= nsamp_window; j++)
                     frame[channel][j]=this.z[channel][i++]
                           - localMean[channel];
               }
            }

            /*
             * Compute the local peak; look half a longest period to both sides.
             */
            localPeak=0.0;
            if((startSample=halfnsamp_window + 1 - halfnsamp_period) < 1)
               startSample=1;
            if((endSample=halfnsamp_window + halfnsamp_period) > nsamp_window)
               endSample=nsamp_window;
            for(int channel=1; channel <= this.ny; channel++) {
               for(j=startSample; j <= endSample; j++) {
                  double value=Math.abs(frame[channel][j]);
                  if(value > localPeak)
                     localPeak=value;
               }
            }
            pitchFrame.intensity=localPeak > globalPeak ? 1.0 : localPeak
                  / globalPeak;

            /*
             * Compute the correlation into the array 'r'.
             */
            if(method >= FCC_NORMAL) {
               double startTime=t - 0.5 * (1.0 / minimumPitch + dt_window);
               int localSpan=maximumLag + nsamp_window, localMaximumLag, offset;
               if((startSample=(int) this.Sampled_xToLowIndex(startTime)) < 1)
                  startSample=1;
               if(localSpan > this.nx + 1 - startSample)
                  localSpan=this.nx + 1 - startSample;
               localMaximumLag=localSpan - nsamp_window;
               offset=startSample - 1;
               double sumx2=0; /* Sum of squares. */
               for(int channel=1; channel <= this.ny; channel++) {
                  double[] amp=this.z[channel];
                  for(i=1; i <= nsamp_window; i++) {
                     double x=amp[offset + i] - localMean[channel];
                     sumx2+=x * x;
                  }
               }
               double sumy2=sumx2; /* At zero lag, these are still equal. */
               r[nsamp_window + 0]=1.0;
               for(i=1; i <= localMaximumLag; i++) {
                  double product=0.0;
                  for(int channel=1; channel <= this.ny; channel++) {
                     double[] amp=this.z[channel];
                     double y0=amp[offset + i] - localMean[channel];
                     double yZ=amp[offset + i + nsamp_window]
                           - localMean[channel];
                     sumy2+=yZ * yZ - y0 * y0;
                     for(j=1; j <= nsamp_window; j++) {
                        double x=amp[offset + j] - localMean[channel];
                        double y=amp[offset + i + j] - localMean[channel];
                        product+=x * y;
                     }
                  }
                  r[nsamp_window - i]=r[nsamp_window + i]=product
                        / Math.sqrt(sumx2 * sumy2);
               }
            } else {

               /*
                * The FFT of the autocorrelation is the power spectrum.
                */
               for(i=1; i <= nsampFFT; i++) {
                  ac[i]=0.0;
               }
               for(int channel=1; channel <= this.ny; channel++) {
                  fftTable.NUMfft_forward(frame[channel]); /* Complex spectrum. */
                  ac[1]+=frame[channel][1] * frame[channel][1]; /* DC component. */
                  for(i=2; i < nsampFFT; i+=2) {
                     ac[i]+=frame[channel][i] * frame[channel][i]
                           + frame[channel][i + 1] * frame[channel][i + 1]; /*
                                                                             * Power
                                                                             * spectrum
                                                                             * .
                                                                             */
                  }
                  ac[nsampFFT]+=frame[channel][nsampFFT]
                        * frame[channel][nsampFFT]; /* Nyquist frequency. */
               }
               fftTable.NUMfft_backward(ac); /* Autocorrelation. */

               /*
                * Normalize the autocorrelation to the value with zero lag, and
                * divide it by the normalized autocorrelation of the window.
                */
               r[nsamp_window + 0]=1.0;
               for(i=1; i <= brent_ixmax; i++)
                  r[nsamp_window - i]=r[nsamp_window + i]=ac[i + 1]
                        / (ac[1] * windowR[i + 1]);
            }
            /*int ri;
            for(ri=0; ri<r.length; ri++) {
               System.err.println(r[ri]);
            }
            System.exit(0);*/
            /*
             * Create (too much) space for candidates.
             */
            pitchFrame.Pitch_Frame_init(maxnCandidates);
            // pitchFrame=new Pitch_Frame(maxnCandidates); // >>>?
            // cherror //>>>

            /*
             * Register the first candidate, which is always present:
             * voicelessness.
             */
            pitchFrame.nCandidates=1;
            pitchFrame.candidate[1].frequency=0.0; /* Voiceless: always present. */
            pitchFrame.candidate[1].strength=0.0;

            /*
             * Shortcut: absolute silence is always voiceless. Go to next frame.
             */
            if(localPeak == 0)
               continue;

            /*
             * Find the strongest maxima of the correlation of this frame, and
             * register them as candidates.
             */
            imax[1]=0;
            for(i=2; i < maximumLag && i < brent_ixmax; i++)
               if(r[nsamp_window + i] > 0.5 * voicingThreshold && /*
                                                                   * Not too
                                                                   * unvoiced?
                                                                   */
               r[nsamp_window + i] > r[nsamp_window + i - 1]
                     && r[nsamp_window + i] >= r[nsamp_window + i + 1]) /*
                                                                         * Maximum?
                                                                         */
               {
                  int place=0;

                  /*
                   * Use parabolic interpolation for first estimate of
                   * frequency, and sin(x)/x interpolation to compute the
                   * strength of this frequency.
                   */
                  double dr=0.5 * (r[nsamp_window + i + 1] - r[nsamp_window + i
                        - 1]), d2r=2 * r[nsamp_window + i]
                        - r[nsamp_window + i - 1] - r[nsamp_window + i + 1];
                  double frequencyOfMaximum=1 / this.dx / (i + dr / d2r);
                  int offset=-brent_ixmax - 1;
                  double strengthOfMaximum= /* method & 1 ? */
                  NUM.NUM_interpolate_sinc(r, nsamp_window + offset,
                        brent_ixmax - offset, 1 / this.dx / frequencyOfMaximum
                              - offset, 30);
                  /* : r [i] + 0.5 dr dr / d2r */
                  /*
                   * High values due to short windows are to be reflected around
                   * 1.
                   */
                  if(strengthOfMaximum > 1.0)
                     strengthOfMaximum=1.0 / strengthOfMaximum;

                  /*
                   * Find a place for this maximum.
                   */
                  if(pitchFrame.nCandidates < thee.maxnCandidates) { /*
                                                                      * Is there
                                                                      * still a
                                                                      * free
                                                                      * place?
                                                                      */
                     place=++pitchFrame.nCandidates;
                  } else {
                     /* Try the place of the weakest candidate so far. */
                     double weakest=2;
                     int iweak;
                     for(iweak=2; iweak <= thee.maxnCandidates; iweak++) {
                        /* High frequencies are to be favoured */
                        /*
                         * if we want to analyze a perfectly periodic signal
                         * correctly.
                         */
                        double localStrength=pitchFrame.candidate[iweak].strength
                              - octaveCost
                              * Util.log2(minimumPitch
                                    / pitchFrame.candidate[iweak].frequency);
                        if(localStrength < weakest) {
                           weakest=localStrength;
                           place=iweak;
                        }
                     }
                     /*
                      * If this maximum is weaker than the weakest candidate so
                      * far, give it no place.
                      */
                     if(strengthOfMaximum - octaveCost
                           * Util.log2(minimumPitch / frequencyOfMaximum) <= weakest)
                        place=0;
                  }
                  if(place != 0) { /* Have we found a place for this candidate? */
                     pitchFrame.candidate[place].frequency=frequencyOfMaximum;
                     pitchFrame.candidate[place].strength=strengthOfMaximum;
                     imax[place]=i;
                  }
               }

            /*
             * Second pass: for extra precision, maximize sin(x)/x interpolation
             * ('sinc').
             */
            for(i=2; i <= pitchFrame.nCandidates; i++) {
               if(method != AC_HANNING
                     || pitchFrame.candidate[i].frequency > 0.0 / this.dx) {
                  NUM.Ixmid_real xmid=new NUM.Ixmid_real(0);
                  double ymid;
                  int offset=-brent_ixmax - 1;
                  ymid=NUM
                        .NUMimproveMaximum(
                              r,
                              nsamp_window + offset,
                              brent_ixmax - offset,
                              imax[i] - offset,
                              pitchFrame.candidate[i].frequency > 0.3 / this.dx ? NUM_PEAK_INTERPOLATE_SINC700
                                    : brent_depth, xmid);
                  xmid.value+=offset;
                  pitchFrame.candidate[i].frequency=1.0 / this.dx / xmid.value;
                  if(ymid > 1.0)
                     ymid=1.0 / ymid;
                  pitchFrame.candidate[i].strength=ymid;
               }
            }
         } /* Next frame. */

         // Melder_progress1 (0.95, "Sound to Pitch: path finder"); //>>>
         thee.Pitch_pathFinder(silenceThreshold, voicingThreshold, octaveCost,
               octaveJumpCost, voicedUnvoicedCost, ceiling, false);
      }
      /*
       * end: Melder_progress1 (1.0, null); Done. NUMdmatrix_free (frame, 1, 1);
       * NUMdvector_free (ac, 1); NUMdvector_free (window, 1); NUMdvector_free
       * (windowR, 1); NUMdvector_free (r, - nsamp_window); NUMlvector_free
       * (imax, 1); NUMfft_Table_free (& fftTable); iferror forget (thee);
       */// >>>
      return thee;
   }
   
   
   
   
   //[ intensity
   public Intensity Sound_to_Intensity (double minimumPitch, double timeStep, boolean subtractMeanPressure) {
      /*
       * init
       */
      boolean veryAccurate = false;
      Sound save_me = new Sound(this);
      Intensity smooth = null;
      double[] amplitude = null;
      double[] window = null;

      /*
       * Soft preconditions.
       */
      /*if (! NUMdefined (minimumPitch)) error1 (L"(Sound-to-Intensity:) Minimum pitch undefined.")
      if (! NUMdefined (timeStep)) error1 (L"(Sound-to-Intensity:) Time step undefined.")*/ //>>>
      /*
       * Hard preconditions.
       */
      assert (this.dx > 0.0);
      assert (minimumPitch > 0.0);
      /*
       * Defaults.
       */
      if (timeStep <= 0.0) timeStep = 0.8 / minimumPitch;   /* Default: four times oversampling Hanning-wise. */
      
      int i, iframe;
      NumberOfFrames numberOfFrames=new NumberOfFrames();
      double windowDuration = 6.4 / minimumPitch;
      FirstTime thyFirstTime=new FirstTime();
      assert (windowDuration > 0.0);
      double halfWindowDuration = 0.5 * windowDuration;
      final int halfWindowSamples = (int) (halfWindowDuration / this.dx);
      amplitude = new double[halfWindowSamples+halfWindowSamples+1]; //NUMdvector (- halfWindowSamples, halfWindowSamples); cherror
      window = new double[halfWindowSamples+halfWindowSamples+1]; //NUMdvector (- halfWindowSamples, halfWindowSamples); cherror

      for (i = - halfWindowSamples; i <= halfWindowSamples; i ++) {
            double x = i * this.dx / halfWindowDuration, root = 1 - x * x;
            window [halfWindowSamples+i] = root <= 0.0 ? 0.0 : NUM.NUMbessel_i0_f ((2 * Math.PI * Math.PI + 0.5) * Math.sqrt (root));
      }

      /* Step 1: upsample by a factor of two. */
      /*if (veryAccurate) {
            me = Sound_upsample (me);    Because frequency content will be doubled in the next step. 
            cherror
      }*/ //>>>
      
      /* Step 2: smooth and resample. */
      if (Sampled_shortTermAnalysis (windowDuration, timeStep, numberOfFrames, thyFirstTime)==0) {
         throw new RuntimeException();
            //error1 (L"Sound-to-Intensity: the duration of the sound should be at least 6.4 divided by the minimum pitch.")
      }
      smooth = Intensity.Intensity_create (
            this.xmin, this.xmax, numberOfFrames.value, timeStep, thyFirstTime.value); //cherror //>>>
      for (iframe = 1; iframe <= numberOfFrames.value; iframe ++) {
            double midTime = smooth.Sampled_indexToX (iframe);
            int midSample = this.Sampled_xToNearestIndex (midTime);
            int leftSample = midSample - halfWindowSamples, rightSample = midSample + halfWindowSamples;
            double sumxw = 0.0, sumw = 0.0, intensity;
            if (leftSample < 1) leftSample = 1;
            if (rightSample > this.nx) rightSample = this.nx;

            for (int channel = 1; channel <= this. ny; channel ++) {
                  for (i = leftSample; i <= rightSample; i ++) {
                        amplitude [halfWindowSamples+i - midSample] = this. z [channel] [i];
                  }
                  if (subtractMeanPressure) {
                        double sum = 0.0;
                        for (i = leftSample; i <= rightSample; i ++) {
                              sum += amplitude [halfWindowSamples+i - midSample];
                        }
                        double mean = sum / (rightSample - leftSample + 1);
                        for (i = leftSample; i <= rightSample; i ++) {
                              amplitude [halfWindowSamples+i - midSample] -= mean;
                        }
                  }
                  for (i = leftSample; i <= rightSample; i ++) {
                        sumxw += amplitude [halfWindowSamples+i - midSample] * amplitude [halfWindowSamples+i - midSample] * window [halfWindowSamples+i - midSample];
                        sumw += window [halfWindowSamples+i - midSample];
                  }
            }
            intensity = sumxw / sumw;
            if (intensity != 0.0) intensity /= 4e-10;
            smooth.z [1] [iframe] = intensity < 1e-30 ? -300 : 10 * Math.log (intensity);
      }

      /* Clean up and return. */
end:
      /*if (veryAccurate) {
            forget (me);
            me = save_me;
      }*///>>>
      /*NUMdvector_free (amplitude, - halfWindowSamples);
      NUMdvector_free (window, - halfWindowSamples);
      iferror return Melder_errorp ("(Sound-to-Intensity:) Analysis not performed.");*/ //>>>
      return smooth;
}
}