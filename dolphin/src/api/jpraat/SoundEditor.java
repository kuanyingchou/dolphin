package api.jpraat;


public class SoundEditor extends TimeSoundAnalysisEditor {
   public static SoundEditor SoundEditor_create(String title, Object data) {
      SoundEditor res=new SoundEditor();
      assert (data != null);
      /*
       * res. longSound.data or res. sound.data have to be set before we call
       * FunctionEditor_init, because createMenus expect that one of them is not
       * NULL.
       */
      if(res.TimeSoundAnalysisEditor_init(title, data, data, false) == 0)
         return null;
      if(/* res. longSound.data && */res.endWindow - res.startWindow > 30.0) {
         res.endWindow=res.startWindow + 30.0;
         /*
          * if (res. startWindow == res. tmin) { res. startSelection = res.
          * endSelection = 0.5 (res. startWindow + res. endWindow); }
          * FunctionEditor_marksChanged (res);
          */
      }
      return res;
   }
   public java.util.List<Double> getPitchesInFrequency() {
      final java.util.List<Double> pitches=new java.util.ArrayList<Double>();
      for(int i=1; i < this.pitch.data.frame.length; i++) {

         final Pitch_Candidate c=this.pitch.data.frame[i].candidate[1];
         if(c == null) {
            //pitches.add(-1.0);
         } else {
            double freq=c.frequency;
            if(freq > this.pitch.ceiling || freq < this.pitch.floor) {
               //pitches.add(-1.0);
            } else {
               pitches.add(freq);
            }
         }
      }
      return pitches;
   }
   /*public java.util.List<Integer> getPitches() {
      final java.util.List<Integer> pitches=new java.util.ArrayList<Integer>();
      for(int i=1; i < this.pitch.data.frame.length; i++) {

         final Pitch_Candidate c=this.pitch.data.frame[i].candidate[1];
         if(c == null) {
            pitches.add(-1);
         } else {
            double freq=c.frequency;
            if(freq > this.pitch.ceiling || freq < this.pitch.floor) {
               pitches.add(-1);
            } else {
               final int pitch=Util.frequencyToPitch((float) freq);
               pitches.add(pitch);
            }
         }
      }
      return pitches;
   }*/
   public java.util.List<Double> getIntensities() {
      final java.util.List<Double> intensities=new java.util.ArrayList<Double>();
      System.err.println(this.intensity.data.z.length);
      for(int i=1; i<this.intensity.data.z[1].length; i++) {
         intensities.add(this.intensity.data.z[1][i]);
      }
      return intensities;
   }
   public double computeAverageIntensity() {
      double sum=0;
      int count=0;
      for(int i=1; i<intensity.data.z[1].length; i++) {
         final double iten=intensity.data.z[1][i];//intensity.data.getValue(i); //>>>>>>>>>>>>>
//System.err.println(iten);
         if(iten<0 || Double.isNaN(iten)) continue;
         sum+=iten;
         count++;
      }
      return sum/count;
      
   }
}
