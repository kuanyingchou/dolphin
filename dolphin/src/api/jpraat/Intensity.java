package api.jpraat;

import api.util.Util;

public class Intensity extends Vector {
   public int Intensity_init(double tmin, double tmax, int nt, double dt,
         double t1) {
      if(Matrix_init(tmin, tmax, nt, dt, t1, 1.0, 1.0, 1, 1.0, 1.0) == 0)
         return 0;
      return 1;
   }

   public static Intensity Intensity_create(double tmin, double tmax, int nt,
         double dt, double t1) {
      Intensity me=new Intensity();
      if(/* ! me || */me.Intensity_init(tmin, tmax, nt, dt, t1) == 0) {
         // forget (me);
      }
      return me;
   }
//   double Intensity_getAverage (double tmin, double tmax, int averagingMethod) {
//      return Sampled_getMean_standardUnit (tmin, tmax, 0, averagingMethod, true);
//   }
//   public double convertSpecialToStandardUnit (double value, long ilevel, int unit) {
//      return
//            unit == 1 ?
//                  10.0 * Math.log (value) :   /* value = energy */
//            unit == 2 ?
//                  10.0 * Util.log2 (value) :   /* value = sones */
//            value;   /* value = dB */
//   }
}
