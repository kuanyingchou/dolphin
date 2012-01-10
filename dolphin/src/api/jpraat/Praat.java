package api.jpraat;

import java.awt.Graphics;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.swing.JComponent;
import javax.swing.JFrame;

import api.util.Util;


class Data_Description {
   String name;
   int type; /* bytewa..inheritwa, see below */
   int offset; /* The offset of this field in the enveloping struct. */
   int size; /* The size of this field if it is in an array. */
   String tagName; /*
                    * For structs: tag; for classes: class name; for enums: type
                    * name.
                    */
   Object tagType; /*
                    * For structs: offset table; for classes: class pointer; for
                    * enums: enum pointer.
                    */
   int rank; /* 0 = single, 1 = vector, 2 = matrix, -1 = array. */
   String min1, max1; /* For vectors and matrices. */
   String min2, max2; /* For matrices. */
}

class Data {
   Data_Description description;
}

class Function extends Data {
   double xmin, xmax;
}

class ixPair { // for call-by-reference
   int min;
   int max;
}

class NumberOfFrames { // for call-by-reference
   int value;
}

class FirstTime { // for call-by-reference
   double value;
}

class Sampled extends Function {
   int nx;
   double dx, x1;

   public int Sampled_init(double xmin, double xmax, int nx, double dx,
         double x1) {
      this.xmin=xmin;
      this.xmax=xmax;
      this.nx=nx;
      this.dx=dx;
      this.x1=x1;
      return 1;
   }

   public int Sampled_getWindowSamples(double xmin, double xmax, ixPair ix) {
      double rixmin=1.0 + Math.ceil((xmin - this.x1) / this.dx);
      double rixmax=1.0 + Math.floor((xmax - this.x1) / this.dx);
      ix.min=rixmin < 1.0 ? 1 : (int) rixmin;
      ix.max=rixmax > (double) this.nx ? this.nx : (int) rixmax;
      if(ix.min > ix.max)
         return 0;
      return ix.max - ix.min + 1;
   }

   public long Sampled_xToLowIndex(double x) {
      return (long) Math.floor((x - this.x1) / this.dx) + 1;
   }

   public double Sampled_indexToX(long i) {
      return this.x1 + (i - 1) * this.dx;
   }
   int Sampled_xToNearestIndex (double x) {
      return (int) Math.floor ((x - this. x1) / this. dx + 1.5);
   }

   public int Sampled_shortTermAnalysis(double windowDuration, double timeStep,
         NumberOfFrames numberOfFrames, FirstTime firstTime) {
      double myDuration, thyDuration, ourMidTime;
      assert (windowDuration > 0.0);
      assert (timeStep > 0.0);
      myDuration=this.dx * this.nx;
      if(windowDuration > myDuration) {
         // return Melder_error2 (Thing_className (me),
         // " shorter than window length.");
         // System.err.println("error occurred");
         throw new RuntimeException("wd: "+windowDuration+", md:"+myDuration);
      }
      numberOfFrames.value=(int) (Math.floor((myDuration - windowDuration)
            / timeStep) + 1);
      assert (numberOfFrames.value >= 1);
      ourMidTime=this.x1 - 0.5 * this.dx + 0.5 * myDuration;
      thyDuration=numberOfFrames.value * timeStep;
      firstTime.value=ourMidTime - 0.5 * thyDuration + 0.5 * timeStep;
      return 1;
   }
//   public double Sampled_getMean_standardUnit (double xmin, double xmax, int ilevel, int averagingUnit, int interpolate) {
//      return convertSpecialToStandardUnit (Sampled_getMean (xmin, xmax, ilevel, averagingUnit, interpolate), ilevel, averagingUnit);
//   }
}
class Pitch_Candidate {
   public double frequency;
   public double strength;
}

class Pitch_Frame {
   double intensity;
   int nCandidates;
   Pitch_Candidate[] candidate; // =new Pitch_Candidate[nCandidates];

   Pitch_Frame() {
      // nCandidates=n;
      // candidate=new Pitch_Candidate[1+nCandidates];
   }

   public void Pitch_Frame_init(int n) {
      nCandidates=n;
      candidate=new Pitch_Candidate[1 + nCandidates];
      for(int i=1; i < candidate.length; i++) {
         candidate[i]=new Pitch_Candidate();
      }

   }
}

class Pitch extends Sampled {
   double ceiling;
   int maxnCandidates;

   Pitch_Frame[] frame; // =new Pitch_Frame[(int) nx];

   /*
    * #define oo_STRUCT_VECTOR(Type,x,n) oo_STRUCT_VECTOR_FROM (Type, x, 1, n)
    * #define oo_STRUCT_VECTOR_FROM(Type,x,min,max) Type x;
    */

   public static Pitch Pitch_create(double tmin, double tmax, int nt,
         double dt, double t1, double ceiling, int maxnCandidates) {
      Pitch res=new Pitch();
      int it;
      if(res == null || res.Sampled_init(tmin, tmax, nt, dt, t1) == 0)
         throw new RuntimeException();
      res.ceiling=ceiling;
      res.maxnCandidates=maxnCandidates;
      res.frame=new Pitch_Frame[1 + (int) nt];
      // if (! ()) throw new RuntimeException();

      /* Put one candidate in every frame (unvoiced, silent). */
      for(it=1; it <= nt; it++) {
         // if (! Pitch_Frame_init (& , 1)) throw new RuntimeException();
         res.frame[it]=new Pitch_Frame();
         res.frame[it].Pitch_Frame_init(1);
      }

      return res;
      /*
       * error: forget (res); return Melder_errorp ("Pitch not created.");
       */
   }

   int Pitch_getMaxnCandidates() {
      int result=0;
      int i;
      for(i=1; i <= this.nx; i++) {
         int nCandidates=this.frame[i].nCandidates;
         if(nCandidates > result)
            result=nCandidates;
      }
      return result;
   }

   void Pitch_pathFinder(double silenceThreshold, double voicingThreshold,
         double octaveCost, double octaveJumpCost, double voicedUnvoicedCost,
         double ceiling, boolean pullFormants) {
      int iframe;
      int icand, icand1, icand2, maxnCandidates=this.Pitch_getMaxnCandidates(), place;
      double maximum, value;
      double[][] delta;
      int[][] psi;
      double ceiling2=(pullFormants) ? 2 * ceiling : ceiling;
      /* Next three lines 20011015 */
      double timeStepCorrection=0.01 / this.dx;
      octaveJumpCost*=timeStepCorrection;
      voicedUnvoicedCost*=timeStepCorrection;

      this.ceiling=ceiling;
      delta=new double[1 + this.nx][1 + maxnCandidates]; // NUMdmatrix (1, this.
                                                         // nx, 1,
      // maxnCandidates);
      psi=new int[1 + this.nx][1 + maxnCandidates]; // NUMimatrix (1, this. nx,
                                                    // 1,
      // maxnCandidates);
      // if (! delta || ! psi) goto end; //>>>

      for(iframe=1; iframe <= this.nx; iframe++) {
         Pitch_Frame frame=this.frame[iframe];
         double unvoicedStrength=silenceThreshold <= 0 ? 0 : 2
               - frame.intensity / (silenceThreshold / (1 + voicingThreshold));
         unvoicedStrength=voicingThreshold
               + (unvoicedStrength > 0 ? unvoicedStrength : 0);
         for(icand=1; icand <= frame.nCandidates; icand++) {
            Pitch_Candidate candidate=frame.candidate[icand];
            boolean voiceless=candidate.frequency == 0
                  || (candidate.frequency > ceiling2);
            delta[iframe][icand]=voiceless ? unvoicedStrength
                  : candidate.strength - octaveCost
                        * Util.log2(ceiling / candidate.frequency);
         }
      }

      /* Look for the most probable path through the maxima. */
      /* There is a cost for the voiced/unvoiced transition, */
      /* and a cost for a frequency jump. */
      
      for(iframe=2; iframe <= this.nx; iframe++) {
         Pitch_Frame prevFrame=this.frame[iframe - 1]; 
         Pitch_Frame curFrame=this.frame[iframe];
         double[] prevDelta=delta[iframe - 1];
         double[] curDelta=delta[iframe];
         int[] curPsi=psi[iframe];
         for(icand2=1; icand2 <= curFrame.nCandidates; icand2++) {
            double f2=curFrame.candidate[icand2].frequency;
            maximum=-1e308;
            place=0;
            for(icand1=1; icand1 <= prevFrame.nCandidates; icand1++) {
               double f1=prevFrame.candidate[icand1].frequency, transitionCost;
               boolean previousVoiceless=f1 <= 0 || f1 >= ceiling2;
               boolean currentVoiceless=f2 <= 0 || f2 >= ceiling2;
               if(previousVoiceless != currentVoiceless) /* Voice transition. */
                  transitionCost=voicedUnvoicedCost;
               else if(currentVoiceless) /* Both voiceless. */
                  transitionCost=0;
               else
                  /* Both voiced; frequency jump. */
                  transitionCost=octaveJumpCost * Math.abs(Util.log2(f1 / f2));
               value=prevDelta[icand1] - transitionCost + curDelta[icand2];
               if(value > maximum) {
                  maximum=value;
                  place=icand1;
               }
            }
            curDelta[icand2]=maximum;
            curPsi[icand2]=place;
         }
      }

      /* Find the end of the most probable path. */

      maximum=delta[this.nx][place=1];
      for(icand=2; icand <= this.frame[this.nx].nCandidates; icand++)
         if(delta[this.nx][icand] > maximum)
            maximum=delta[this.nx][place=icand];

      /* Backtracking: follow the path backwards. */
      
      for(iframe=this.nx; iframe >= 1; iframe--) {
         Pitch_Frame frame=this.frame[iframe];
         Pitch_Candidate help=frame.candidate[1];
         frame.candidate[1]=frame.candidate[place];
         frame.candidate[place]=help;
         place=psi[iframe][place]; // This assignment is challenging to
         // CodeWarrior 11.
      }

      /*
       * Pull formants: devoice frames with frequencies between ceiling and
       * ceiling2.
       */

      if(ceiling2 > ceiling)
         for(iframe=this.nx; iframe >= 1; iframe--) {
            Pitch_Frame frame=this.frame[iframe];
            Pitch_Candidate winner=frame.candidate[1];
            double f=winner.frequency;
            if(f > ceiling && f <= ceiling2) {
               for(icand=2; icand <= frame.nCandidates; icand++) {
                  Pitch_Candidate loser=frame.candidate[icand];
                  if(loser.frequency == 0.0) {
                     Pitch_Candidate help=winner;
                     winner=loser;
                     loser=help;
                     /**
                      * winner = * loser; loser = help;
                      */
                     break;
                  }
               }
            }
         }

      /*
       * end: NUMdmatrix_free (delta, 1, 1); NUMimatrix_free (psi, 1, 1);
       */// >>>
   }
   /*
    * public double getValueAtSample (long iframe, long ilevel, int unit) { iam
    * (Pitch); double f = my frame [iframe]. candidate [1]. frequency; if (f <=
    * 0.0 || f >= my ceiling) return NUMundefined; Frequency out of range (or
    * NUMundefined)? Voiceless. return our convertStandardToSpecialUnit (me,
    * ilevel == Pitch_LEVEL_FREQUENCY ? f : my frame [iframe]. candidate [1].
    * strength, ilevel, unit); }
    */// >>>
}

class Editor {
   Object data;

   int Editor_init(int x, int y, int width, int height, String title,
         Object data) {
      /*
       * iam (Editor); #if gtk GdkScreenscreen = gtk_window_get_screen
       * (GTK_WINDOW (GuiObject_parent (parent))); int screenWidth =
       * gdk_screen_get_width (screen); int screenHeight = gdk_screen_get_height
       * (screen); #elif motif int screenWidth = WidthOfScreen
       * (DefaultScreenOfDisplay (XtDisplay (parent))); int screenHeight =
       * HeightOfScreen (DefaultScreenOfDisplay (XtDisplay (parent))); #endif
       * int left, right, top, bottom; screenHeight -= Machine_getTitleBarHeight
       * (); if (width < 0) width += screenWidth; if (height < 0) height +=
       * screenHeight; if (width > screenWidth - 10) width = screenWidth - 10;
       * if (height > screenHeight - 10) height = screenHeight - 10; if (x > 0)
       * right = (left = x) + width; else if (x < 0) left = (right = screenWidth
       * + x) - width; else Randomize. right = (left = NUMrandomInteger (4,
       * screenWidth - width - 4)) + width; if (y > 0) bottom = (top = y) +
       * height; else if (y < 0) top = (bottom = screenHeight + y) - height;
       * else Randomize. bottom = (top = NUMrandomInteger (4, screenHeight -
       * height - 4)) + height; #ifndef _WIN32 top += Machine_getTitleBarHeight
       * (); bottom += Machine_getTitleBarHeight (); #endif this. parent =
       * parent; Probably praat.topShell this. dialog = GuiWindow_create
       * (parent, left, top, right - left, bottom - top, title,
       * gui_window_cb_goAway, me, 0); if (! this. dialog) return 0; this. shell
       * = GuiObject_parent (this. dialog); Note that GuiObject_parent (this.
       * shell) will be NULL! Thing_setName (me, title); this. data = data;
       * 
       * Create menus.
       * 
       * this. menus = Ordered_create (); this. menuBar = Gui_addMenuBar (this.
       * dialog); our createMenus (me); Melder_clearError (); TEMPORARY: to
       * protect against CategoriesEditor EditorMenu helpMenu = Editor_addMenu
       * (me, L"Help", 0); our createHelpMenuItems (me, helpMenu);
       * EditorMenu_addCommand (helpMenu, L"-- search --", 0, NULL); this.
       * searchButton = EditorMenu_addCommand (helpMenu, L"Search manual...",
       * 'M', menu_cb_searchManual); if (our scriptable) { Editor_addCommand
       * (me, L"File", L"New editor script", 0, menu_cb_newScript);
       * Editor_addCommand (me, L"File", L"Open editor script...", 0,
       * menu_cb_openScript); Editor_addCommand (me, L"File",
       * L"-- after script --", 0, 0); }
       * 
       * Add the scripted commands.
       * 
       * praat_addCommandsToEditor (me);
       * 
       * Editor_addCommand (me, L"File", L"Close", 'W', menu_cb_close);
       * GuiObject_show (this. menuBar);
       * 
       * our createChildren (me); GuiObject_show (this. dialog); #if gtk
       * GuiWindow_show (this. shell); #elif motif XtRealizeWidget (this.
       * shell); #endif
       * 
       * return 1;
       */
      return 1;
   }
}

class FunctionEditor extends Editor {
   double tmin, tmax;
   double endWindow;
   double startWindow;

   // longestAnalysis; //>>>
   int FunctionEditor_init(String title, Object data) {
      // iam (FunctionEditor);
      this.tmin=((Function) data).xmin; /*
                                         * Set before adding children (see group
                                         * button).
                                         */
      this.tmax=((Function) data).xmax;
      // if (! this.Editor_init (0, 0, preferences.shellWidth,
      // preferences.shellHeight, title, data)) return 0; //>>>

      this.startWindow=this.tmin;
      this.endWindow=this.tmax;
      /*
       * this. startSelection = this. endSelection = 0.5 (this. tmin + this.
       * tmax); this. graphics = Graphics_create_xmdrawingarea (this.
       * drawingArea); Graphics_setFontSize (this. graphics, 10);
       * 
       * struct structGuiDrawingAreaResizeEvent event = { this. drawingArea, 0
       * }; event. width = GuiObject_getWidth (this. drawingArea); event. height
       * = GuiObject_getHeight (this. drawingArea); gui_drawingarea_cb_resize
       * (me, & event);
       * 
       * our updateText (me); if (group_equalDomain (this. tmin, this. tmax))
       * gui_checkbutton_cb_group (me, NULL); // BUG: NULL this. enableUpdates =
       * TRUE; this. arrowScrollStep = preferences.arrowScrollStep;
       */
      return 1;
   }
}

class Matrix extends Sampled {
   double ymin, ymax;
   int ny;
   double dy, y1;
   public double[][] z;

   public int Matrix_init(double xmin, double xmax, int nx, double dx,
         double x1, double ymin, double ymax, int ny, double dy, double y1) {
      // System.err.println(ny+", "+nx);
      if(Sampled_init(xmin, xmax, nx, dx, x1) == 0)
         return 0;
      this.ymin=ymin;
      this.ymax=ymax;
      this.ny=ny;
      this.dy=dy;
      this.y1=y1;

      this.z=new double[1 + this.ny][1 + this.nx];
      // if (()==null) return 0;
      return 1;
   }
}

class Vector extends Matrix {
   public static final int Vector_CHANNEL_AVERAGE =0;
   public static final int Vector_CHANNEL_1  =1;
   public static final int Vector_CHANNEL_2  =2;
   public static final int Vector_VALUE_INTERPOLATION_NEAREST  =0;
   public static final int Vector_VALUE_INTERPOLATION_LINEAR  =1;
   public static final int Vector_VALUE_INTERPOLATION_CUBIC  =2;
   public static final int Vector_VALUE_INTERPOLATION_SINC70  =3;
   public static final int Vector_VALUE_INTERPOLATION_SINC700=4;
   
   //[ modified from Vector_getValueAtX
   double Vector_getValueAtIndex (int x, int ilevel, int interpolation) { 
   //double leftEdge = this. x1 - 0.5 * this. dx, rightEdge = leftEdge + this. nx * this. dx;
   //if (x <  leftEdge || x > rightEdge) return Double.NaN;
   if (ilevel > Vector_CHANNEL_AVERAGE) {
         assert (ilevel <= this. ny);
         return NUM.NUM_interpolate_sinc (this. z [ilevel], 0, this. nx, x,
               interpolation == Vector_VALUE_INTERPOLATION_SINC70 ? NUM.NUM_VALUE_INTERPOLATE_SINC70 :
               interpolation == Vector_VALUE_INTERPOLATION_SINC700 ? NUM.NUM_VALUE_INTERPOLATE_SINC700 :
               interpolation);
   }
   double sum = 0.0;
   for (int channel = 1; channel <= this. ny; channel ++) {
         sum += NUM.NUM_interpolate_sinc (this. z [channel], 0, this. nx, x,
               interpolation == Vector_VALUE_INTERPOLATION_SINC70 ? NUM.NUM_VALUE_INTERPOLATE_SINC70 :
               interpolation == Vector_VALUE_INTERPOLATION_SINC700 ? NUM.NUM_VALUE_INTERPOLATE_SINC700 :
               interpolation);
   }
   return sum / this. ny;
   }
   
   public double getValue(int index) {
      return Vector_getValueAtIndex(index, Vector_CHANNEL_1, Vector_VALUE_INTERPOLATION_LINEAR);
   }
}



class TimeSoundEditor_sound {
   /* KEEP IN SYNC WITH PREFS. */
   Sound data;
   boolean autoscaling;
   double minimum, maximum;
};

/*class LongSound extends Sampled {
   // structMelderFile file; \
   // FILE *f; \
   // int audioFileType,
   int numberOfChannels=2;
   // int encoding;
   // int numberOfBytesPerSamplePoint;
   double sampleRate;
   // int startOfData;
   // double bufferLength;
   // short *buffer;
   int imin, imax, nmax;
   // struct FLAC__StreamDecoder *flacDecoder;
   // struct _MP3_FILE *mp3f;
   // int compressedMode;
   // long compressedSamplesLeft;
   // double *compressedFloats [2];
   // short *compressedShorts;

   final java.util.List<Float> samples=new ArrayList<Float>();

   public void addSample(float d) {
      samples.add(d);
      updateFields();
   }

   public void addSamples(float[] d) {
      for(int i=0; i < d.length; i++) {
         samples.add(d[i]);
      }
      updateFields();
   }

   private void updateFields() {
      numberOfChannels=2;
      sampleRate=44100;
      nx=samples.size();
      // this.nmax=my bufferLength * my numberOfChannels * my sampleRate * (1 +
      // 3 * MARGIN);
      this.xmin=0.0;
      this.dx=1 / this.sampleRate;
      this.xmax=this.nx * this.dx;
      this.x1=0.5 * this.dx;
      this.imin=1;
      this.imax=0;
      super.Sampled_init(imin, imax, nmax, imin, imax);
   }

   public LongSound() {
      updateFields();

   }

   public Sound extractPart(double tmin, double tmax, boolean preserveTimes) {
      Sound thee=null;
      ixPair i=new ixPair(); // old: long imin, imax;
      int n;
      if(tmax <= tmin) {
         tmin=this.xmin;
         tmax=this.xmax;
      }
      if(tmin < this.xmin)
         tmin=this.xmin;
      if(tmax > this.xmax)
         tmax=this.xmax;
      n=this.Sampled_getWindowSamples(tmin, tmax, i);
      if(n < 1)
         throw new RuntimeException("Less than 1 sample in window.");
      thee=Sound.Sound_create(this.numberOfChannels, tmin, tmax, n, this.dx,
            this.x1 + (i.min - 1) * this.dx);
      // cherror //>>>
      if(!preserveTimes) {
         thee.xmin=0.0;
         thee.xmax-=tmin;
         thee.x1-=tmin;
      }
      LongSound_readAudioToFloat(thee.z[1], thee.ny == 1 ? null : thee.z[2],
            i.min, n); // >>>z index?
      // cherror //>>>
      // end:
      // iferror { forget (thee); Melder_error1
      // ("Sound not extracted from LongSound."); } //>>>
      return thee;
   }

   int LongSound_readAudioToFloat(double[] leftBuffer, double[] rightBuffer,
         int firstSample, int numberOfSamples) {
     
      for(int i=0; i < numberOfSamples; i++) {
         leftBuffer[i]=samples.get(firstSample + i);
         rightBuffer[i]=samples.get(firstSample + i);
      }
      return 0; // >>>>>>>>>>>>>>>>>>
   }
}*/

class TimeSoundEditor extends FunctionEditor {
   boolean ownSound;
   TimeSoundEditor_sound sound=new TimeSoundEditor_sound(); // >>>

   // struct { LongSound data; } longSound; \
   /*static class AnanymousLongSound {
      LongSound data;// =new LongSound();
   }

   AnanymousLongSound longSound=new AnanymousLongSound();*/

   int TimeSoundEditor_init(String title, Object data, Object sound,
         boolean ownSound) {
      // iam (TimeSoundEditor);
      this.ownSound=ownSound;
      if(sound != null) {
         if(ownSound) {
            // Melder_assert (Thing_member (sound, classSound));
            this.sound.data=(Sound) sound; // Data_copy (sound); cherror // Deep
                                           // copy; ownership transferred. //>>>
            // Matrix_getWindowExtrema (sound, 1, this. sound.data -> nx, 1,
            // this. sound.data -> ny, & this. sound.minimum, & this.
            // sound.maximum);
         } else if(sound instanceof Sound) {
            this.sound.data=(Sound) sound; // Reference copy; ownership not
                                           // transferred.
            // Matrix_getWindowExtrema (sound, 1, this. sound.data -> nx, 1,
            // this. sound.data -> ny, & this. sound.minimum, & this.
            // sound.maximum);
         /*} else if(sound instanceof LongSound) {
            this.longSound.data=(LongSound) sound;
            this.sound.minimum=-1.0;
            this.sound.maximum=1.0;*/
         } else {
            // Melder_fatal ("Invalid sound class in TimeSoundEditor_init.");
            throw new RuntimeException();
         }
      }
      if(FunctionEditor_init(title, data) == 0)
         return 0;
      // this. sound.autoscaling = preferences.sound.autoscaling;
      // end:
      // iferror return 0;
      return 1;
   }
}

class Spectrogram extends Matrix {

}

class FunctionEditor_spectrogram {
   Spectrogram data;
   boolean show;
   /* Spectrogram settings: */
   double viewFrom, viewTo; /* Hertz */
   double windowLength; /* seconds */
   double dynamicRange; /* dB */
   /* Advanced spectrogram settings: */
   long timeSteps, frequencySteps;
   // enum kSound_to_Spectrogram_method method;
   // enum kSound_to_Spectrogram_windowShape windowShape;
   boolean autoscaling;
   double maximum; /* dB/Hz */
   double preemphasis; /* dB/octave */
   double dynamicCompression; /* 0..1 */
   /* Dynamic information: */
   double cursor;
};

/*class Intensity extends Vector {

}*/

class FunctionEditor_intensity {
   public Intensity data;
   boolean show=true;
   /* Intensity settings: */
   public double viewFrom=50, viewTo=100;
   // enum kTimeSoundAnalysisEditor_intensity_averagingMethod averagingMethod;
   boolean subtractMeanPressure=true;
};

class Formant extends Sampled {

}

class FunctionEditor_formant {
   Formant data;
   boolean show=false;
   /* Formant settings: */
   double maximumFormant=5500;
   long numberOfPoles=10;
   double windowLength=0.025;
   double dynamicRange=30, dotSize=1;
   /* Advanced formant settings: */
   // enum kTimeSoundAnalysisEditor_formant_analysisMethod method;
   double preemphasisFrom=50;
};

class PointProcess extends Function {
   long maxnt, nt;
   double[] t;
}

class FunctionEditor_pulses {
   PointProcess data;
   boolean show=false;
   /* Pulses settings: */
   double maximumPeriodFactor=1.3, maximumAmplitudeFactor=1.6;
}

class Sound_to_Pitch {
   // moved to Sound
}

class TimeSoundAnalysisEditor extends TimeSoundEditor {
   double longestAnalysis;
   int timeStepStrategy;
   double fixedTimeStep;
   long numberOfTimeStepsPerView;
   //FunctionEditor_spectrogram spectrogram;
   public FunctionEditor_pitch pitch=new FunctionEditor_pitch();
   public FunctionEditor_intensity intensity=new FunctionEditor_intensity();
   FunctionEditor_formant formant=new FunctionEditor_formant();
   FunctionEditor_pulses pulses=new FunctionEditor_pulses();

   public Sound extractSound(double tmin, double tmax) {
      Sound sound=null;
      /*if(this.longSound.data != null) {
         if(tmin < this.longSound.data.xmin)
            tmin=this.longSound.data.xmin;
         if(tmax > this.longSound.data.xmax)
            tmax=this.longSound.data.xmax;
         sound=this.longSound.data.extractPart(tmin, tmax, true);
      } else*/ if(this.sound.data != null) {
         if(tmin < this.sound.data.xmin)
            tmin=this.sound.data.xmin;
         if(tmax > this.sound.data.xmax)
            tmax=this.sound.data.xmax;
         sound=this.sound.data.Sound_extractPart(tmin, tmax,
               Sound.kSound_windowShape_RECTANGULAR, 1.0, true);
      }
      return sound;
   }

   public static final int kTimeSoundAnalysisEditor_pitch_analysisMethod_AUTOCORRELATION=1;
   public static final int kTimeSoundAnalysisEditor_timeStepStrategy_FIXED=2;
   public static final int kTimeSoundAnalysisEditor_timeStepStrategy_VIEW_DEPENDENT=3;

   public void computePitch_inside() {
      Sound sound = null;
      double margin = this. pitch.veryAccurate!=0 ? 3.0 / this. pitch.floor : 1.5 / this. pitch.floor;
      //forget (this. pitch.data); //>>>
      sound = extractSound (this. startWindow - margin, this. endWindow + margin);
      if (sound != null) {
            double pitchTimeStep =
                  this. timeStepStrategy == 2 ? this. fixedTimeStep :
                  this. timeStepStrategy == 3 ? (this. endWindow - this. startWindow) / this. numberOfTimeStepsPerView :
                  0.0;   /* The default: determined by pitch floor. */
            this. pitch.data = sound.Sound_to_Pitch_any (pitchTimeStep,
                  this. pitch.floor, this. pitch.method == 1 ? 3.0 : 1.0, this. pitch.maximumNumberOfCandidates,
                  (this. pitch.method - 1) * 2 + this. pitch.veryAccurate,
                  this. pitch.silenceThreshold, this. pitch.voicingThreshold,
                  this. pitch.octaveCost, this. pitch.octaveJumpCost, this. pitch.voicedUnvoicedCost, this. pitch.ceiling);
            if (this. pitch.data != null) {
               this. pitch.data.xmin = this. startWindow;
               this. pitch.data.xmax = this. endWindow;
            }
            else {
               //Melder_clearError ();
            }
            //forget (sound);
      } else {
         //Melder_clearError ();
      }
   }

   public void computePitch() {
      if(this.pitch.show
            && this.endWindow - this.startWindow <= this.longestAnalysis
            && (this.pitch.data == null
                  || this.pitch.data.xmin != this.startWindow || this.pitch.data.xmax != this.endWindow)) {
         this.computePitch_inside();
      } else {
         throw new RuntimeException();//System.err.println("no compute"); //>>>
      }
   }

   int TimeSoundAnalysisEditor_init(String title, Object data, Object sound,
         boolean ownSound) {
      // iam (TimeSoundAnalysisEditor);
      if(this.TimeSoundEditor_init(title, data, sound, ownSound) == 0)
         return 0;
      this.longestAnalysis=10;
      /*
       * if (preferences.log[0].toLogFile == false &&
       * preferences.log[0].toInfoWindow == false) {
       * preferences.log[0].toLogFile = true; preferences.log[0].toInfoWindow =
       * true; } if (preferences.log[1].toLogFile == false &&
       * preferences.log[1].toInfoWindow == false) {
       * preferences.log[1].toLogFile = true; preferences.log[1].toInfoWindow =
       * true; }
       */// >>>
      this.timeStepStrategy=10; // preferences.timeStepStrategy;
      this.fixedTimeStep=0.01; // preferences.fixedTimeStep;
      this.numberOfTimeStepsPerView=100; //preferences.numberOfTimeStepsPerView;
      // this. spectrogram = preferences.spectrogram;
      // this. pitch = preferences.pitch;
      // this. intensity = preferences.intensity;
      // this. formant = preferences.formant;
      // this. pulses = preferences.pulses; //] >>>
      return 1;
   }
   
   public void computeIntensity () { //[ FunctionEditor_SoundAnalysis_computeIntensity(I)
      //iam (FunctionEditor);
      //Melder_progressOff ();
      if (this. intensity.show && this. endWindow - this. startWindow <= this. longestAnalysis &&
            (this. intensity.data == null || this. intensity.data.xmin != this. startWindow || this. intensity.data. xmax != this. endWindow))
      {
            Sound sound = null;
            double margin = 3.2 / this. pitch.floor;
            //forget (this. intensity.data); //>>>
            sound = this.extractSound (this. startWindow - margin, this. endWindow + margin);
            if (sound != null) {
                  this. intensity.data = sound.Sound_to_Intensity (this. pitch.floor,
                        this. endWindow - this. startWindow > this. longestAnalysis ? (this. endWindow - this. startWindow) / 100 : 0.0,
                        this. intensity.subtractMeanPressure);
                  if (this. intensity.data != null) {
                     this. intensity.data . xmin = this. startWindow;
                     this. intensity.data . xmax = this. endWindow;
                  }
                  else {
                     System.err.println("sound not set");
                     //Melder_clearError ();
                  }
                  //forget (sound);//>>>
            } else {
               System.err.println("sound is null");
               //Melder_clearError ();
            }
      } else {
         System.err.println("no cimpute");
      }
      //Melder_progressOn ();
   }
   
   /*
    * public void draw_analysis() { FunctionEditor_SoundAnalysis_computePitch
    * (me); if (this. pitch.show && this. pitch.data != NULL) { double
    * periodsPerAnalysisWindow = this. pitch.method ==
    * kTimeSoundAnalysisEditor_pitch_analysisMethod_AUTOCORRELATION ? 3.0 : 1.0;
    * double greatestNonUndersamplingTimeStep = 0.5 periodsPerAnalysisWindow /
    * this. pitch.floor; double defaultTimeStep = 0.5
    * greatestNonUndersamplingTimeStep; double timeStep = this. timeStepStrategy
    * == kTimeSoundAnalysisEditor_timeStepStrategy_FIXED ? this. fixedTimeStep :
    * this. timeStepStrategy ==
    * kTimeSoundAnalysisEditor_timeStepStrategy_VIEW_DEPENDENT ? (this.
    * endWindow - this. startWindow) / this. numberOfTimeStepsPerView :
    * defaultTimeStep; int undersampled = timeStep >
    * greatestNonUndersamplingTimeStep; long numberOfVisiblePitchPoints = (long)
    * ((this. endWindow - this. startWindow) / timeStep); Graphics_setColour
    * (this. graphics, Graphics_CYAN); Graphics_setLineWidth (this. graphics,
    * 3.0); if ((this. pitch.drawingMethod ==
    * kTimeSoundAnalysisEditor_pitch_drawingMethod_AUTOMATIC && (undersampled ||
    * numberOfVisiblePitchPoints < 101)) || this. pitch.drawingMethod ==
    * kTimeSoundAnalysisEditor_pitch_drawingMethod_SPECKLE) { Pitch_drawInside
    * (this. pitch.data, this. graphics, this. startWindow, this. endWindow,
    * pitchViewFrom_overt, pitchViewTo_overt, 2, this. pitch.unit); } if ((this.
    * pitch.drawingMethod ==
    * kTimeSoundAnalysisEditor_pitch_drawingMethod_AUTOMATIC && ! undersampled)
    * || this. pitch.drawingMethod ==
    * kTimeSoundAnalysisEditor_pitch_drawingMethod_CURVE) { Pitch_drawInside
    * (this. pitch.data, this. graphics, this. startWindow, this. endWindow,
    * pitchViewFrom_overt, pitchViewTo_overt, FALSE, this. pitch.unit); }
    * Graphics_setColour (this. graphics, Graphics_BLUE); Graphics_setLineWidth
    * (this. graphics, 1.0); if ((this. pitch.drawingMethod ==
    * kTimeSoundAnalysisEditor_pitch_drawingMethod_AUTOMATIC && (undersampled ||
    * numberOfVisiblePitchPoints < 101)) || this. pitch.drawingMethod ==
    * kTimeSoundAnalysisEditor_pitch_drawingMethod_SPECKLE) { Pitch_drawInside
    * (this. pitch.data, this. graphics, this. startWindow, this. endWindow,
    * pitchViewFrom_overt, pitchViewTo_overt, 1, this. pitch.unit); } if ((this.
    * pitch.drawingMethod ==
    * kTimeSoundAnalysisEditor_pitch_drawingMethod_AUTOMATIC && ! undersampled)
    * || this. pitch.drawingMethod ==
    * kTimeSoundAnalysisEditor_pitch_drawingMethod_CURVE) { Pitch_drawInside
    * (this. pitch.data, this. graphics, this. startWindow, this. endWindow,
    * pitchViewFrom_overt, pitchViewTo_overt, FALSE, this. pitch.unit); }
    * Graphics_setColour (this. graphics, Graphics_BLACK); } } void
    * Pitch_drawInside (Pitch me, Graphics g, double xmin, double xmax, double
    * fmin, double fmax, int speckle, int unit) { Sampled_drawInside (me, g,
    * xmin, xmax, fmin, fmax, speckle, Pitch_LEVEL_FREQUENCY, unit); }
    */
   // >>>>>>>>>>>>>
   
   
}



public class Praat {
   
}
