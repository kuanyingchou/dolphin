package api.jpraat;

public class NUM {

   /*
    * public static Object NUMmatrix (long elementSize, long row1, long row2,
    * long col1, long col2) { long i, nrow = row2 - row1 + 1, ncol = col2 - col1
    * + 1, colSize = ncol elementSize; char[][] result, dum; Melder_assert
    * (sizeof (char) == 1); for (;;) { if (! (result = _Melder_malloc (nrow
    * sizeof (char)))) Assume all pointers have same size. return NULL; if
    * (result -= row1) break; This will normally succeed at the first try.
    * (void) Melder_realloc (result + row1, 1); Make sure that second try will
    * succeed. } for (;;) { if (! ((result [row1] = _Melder_calloc (nrow ncol,
    * elementSize))!=0)) { dum = result + row1; Melder_free (dum); return NULL;
    * } if (result [row1] -= col1 elementSize != 0) break; This will normally
    * succeed at the first try. (void) Melder_realloc (result [row1] + col1
    * elementSize, 1); Make sure that second try will succeed. } for (i = row1 +
    * 1; i <= row2; i++) result [i] = result [i - 1] + colSize;
    * theTotalNumberOfArrays += 1; return result; } public static double
    * NUMdmatrix(long row1, long row2, long col1, long col2) { return NUMmatrix
    * (sizeof (type), row1, row2, col1, col2); }
    */
   public static final int NUM_VALUE_INTERPOLATE_SINC70  =70;
   public static final int NUM_VALUE_INTERPOLATE_SINC700  =700;
   public static double NUMbessel_i0_f(double x) {
      double t;
      if(x < 0.0)
         return NUMbessel_i0_f(-x);
      if(x < 3.75) {
         /* Formula 9.8.1. Accuracy 1.6e-7. */
         t=x / 3.75;
         t*=t;
         return 1.0
               + t
               * (3.5156229 + t
                     * (3.0899424 + t
                           * (1.2067492 + t
                                 * (0.2659732 + t * (0.0360768 + t * 0.0045813)))));
      }
      /*
       * otherwise: x >= 3.75
       */
      /* Formula 9.8.2. Accuracy of the polynomial factor 1.9e-7. */
      t=3.75 / x; /* <= 1.0 */
      return Math.exp(x)
            / Math.sqrt(x)
            * (0.39894228 + t
                  * (0.01328592 + t
                        * (0.00225319 + t
                              * (-0.00157565 + t
                                    * (0.00916281 + t
                                          * (-0.02057706 + t
                                                * (0.02635537 + t
                                                      * (-0.01647633 + t * 0.00392377))))))));
   }
   // [ >>> arraycopy
   /*
    * void NUMvector_copyElements (long elementSize, voidv, voidto, long lo,
    * long hi) { long offset = lo elementSize; Melder_assert (v != NULL && to !=
    * NULL); if (hi >= lo) memcpy ((char) to + offset, (char) v + offset, (hi -
    * lo + 1) elementSize); } void NUMdvector_copyElements (double[] v, double[]
    * to, long lo, long hi) { NUMvector_copyElements (sizeof (type), (void) v,
    * to, lo, hi); }
    */

   /*
    * double NUMdvector (long lo, long hi) { return NUMvector (sizeof (type),
    * lo, hi); } Object NUMvector (long elementSize, long lo, long hi) { char
    * result; Melder_assert (sizeof (char) == 1); for (;;) { Not very infinite:
    * 99.999 % of the time once, 0.001 % twice. if (! (result = _Melder_calloc
    * (hi - lo + 1, elementSize))) return Melder_errorp
    * ("(NUMvector:) Not created."); if (result -= lo elementSize) break; This
    * will normally succeed at the first try. (void) Melder_realloc (result + lo
    * elementSize, 1); Make sure that second try will succeed. }
    * theTotalNumberOfArrays += 1; return result; }
    */
   public static final int NUM_VALUE_INTERPOLATE_NEAREST=  0;
   public static final int NUM_VALUE_INTERPOLATE_LINEAR=  1;
   public static final int NUM_VALUE_INTERPOLATE_CUBIC=  2;
   
   public static double NUM_interpolate_sinc(double y[], int yIdx, int nx,
         double x, int maxDepth) {
      int ix, midleft = (int) Math.floor (x), midright = midleft + 1, left, right;
      double result = 0.0, a, halfsina, aa, daa;
      if (nx < 1) return (Double.NaN); 
      if (x > nx) return y[yIdx+nx]; 
      if (x < 1) return y[yIdx+1]; 
      if (x == midleft) return y[yIdx+midleft]; 
      /* 1 < x < nx && x not integer: interpolate. */ 
      if (maxDepth > midright - 1) maxDepth = midright - 1; 
      if (maxDepth > nx - midleft) maxDepth = nx - midleft; 
      if (maxDepth <= 0) return y[yIdx+(int) Math.floor (x + 0.5)]; 
      if (maxDepth == 1) return y[yIdx+midleft] + (x - midleft) * (y[yIdx+midright] - y[yIdx+midleft]); 
      if (maxDepth == 2) { 
            double yl = y[yIdx+midleft], yr = y[yIdx+midright]; 
            double dyl = 0.5 * (yr - y[yIdx+midleft - 1]), dyr = 0.5 * (y[yIdx+midright + 1] - yl); 
            double fil = x - midleft, fir = midright - x; 
            return yl * fir + yr * fil - fil * fir * (0.5 * (dyr - dyl) + (fil - 0.5) * (dyl + dyr - 2 * (yr - yl))); 
      }
      left = midright - maxDepth;
      right = midleft + maxDepth;
      a = Math.PI * (x - midleft);
      halfsina = 0.5 * Math.sin (a);
      aa = a / (x - left + 1);
      daa = Math.PI / (x - left + 1);
      for (ix = midleft; ix >= left; ix --) {
            double d = halfsina / a * (1.0 + Math.cos (aa));
            result += y[yIdx+ix] * d;
            a += Math.PI;
            aa += daa;
            halfsina = - halfsina;
      }
      a = Math.PI * (midright - x);
      halfsina = 0.5 * Math.sin (a);
      aa = a / (right - x + 1);
      daa = Math.PI / (right - x + 1); 
      for (ix = midright; ix <= right; ix ++) {
            double d = halfsina / a * (1.0 + Math.cos (aa));
            result += y[yIdx+ix] * d;
            a += Math.PI;
            aa += daa;
            halfsina = - halfsina;
      }
      return result;
      /*int ix, midleft=(int) Math.floor(x), midright=midleft + 1, left, right;
      double result=0.0, a, halfsina, aa, daa, cosaa, sinaa, cosdaa, sindaa;
      if(nx < 1)
         return Double.NaN;
      if(x > nx)
         return y[yIdx+nx];
      if(x < 1)
         return y[yIdx+1];
      if(x == midleft)
         return y[yIdx+midleft];
       1 < x < nx && x not integer: interpolate. 
      if(maxDepth > midright - 1)
         maxDepth=midright - 1;
      if(maxDepth > nx - midleft)
         maxDepth=nx - midleft;
      if(maxDepth <= NUM_VALUE_INTERPOLATE_NEAREST)
         return y[yIdx+(int) Math.floor(x + 0.5)];
      if(maxDepth == NUM_VALUE_INTERPOLATE_LINEAR)
         return y[yIdx+midleft] + (x - midleft) * (y[yIdx+midright] - y[yIdx+midleft]);
      if(maxDepth == NUM_VALUE_INTERPOLATE_CUBIC) {
         double yl=y[yIdx+midleft], yr=y[yIdx+midright];
         double dyl=0.5 * (yr - y[yIdx+midleft - 1]), dyr=0.5 * (y[yIdx+midright + 1] - yl);
         double fil=x - midleft, fir=midright - x;
         return yl
               * fir
               + yr
               * fil
               - fil
               * fir
               * (0.5 * (dyr - dyl) + (fil - 0.5) * (dyl + dyr - 2 * (yr - yl)));
      }
      left=midright - maxDepth;
      right=midleft + maxDepth;
      a=Math.PI * (x - midleft);
      halfsina=0.5 * Math.sin(a);
      aa=a / (x - left + 1);
      cosaa=Math.cos(aa);
      sinaa=Math.sin(aa);
      daa=Math.PI / (x - left + 1);
      cosdaa=Math.cos(daa);
      sindaa=Math.sin(daa);
      for(ix=midleft; ix >= left; ix--) {
         double d=halfsina / a * (1.0 + cosaa), help;
         result+=y[yIdx+ix] * d;
         a+=Math.PI;
         help=cosaa * cosdaa - sinaa * sindaa;
         sinaa=cosaa * sindaa + sinaa * cosdaa;
         cosaa=help;
         halfsina=-halfsina;
      }
      a=Math.PI * (midright - x);
      halfsina=0.5 * Math.sin(a);
      aa=a / (right - x + 1);
      cosaa=Math.cos(aa);
      sinaa=Math.sin(aa);
      daa=Math.PI / (right - x + 1);
      cosdaa=Math.cos(daa);
      sindaa=Math.sin(daa);
      for(ix=midright; ix <= right; ix++) {
         double d=halfsina / a * (1.0 + cosaa), help;
         result+=y[yIdx+ix] * d;
         a+=Math.PI;
         help=cosaa * cosdaa - sinaa * sindaa;
         sinaa=cosaa * sindaa + sinaa * cosdaa;
         cosaa=help;
         halfsina=-halfsina;
      }
      return result;*/
   }
   
   public static final double NUM_goldenSection=0.6180339887498948482045868343656381177203;
   public static double NUMminimize_brent (
         //double (*f) (double x, void *closure),
         Improve_evaluate_funObj f, 
         double a, 
         double b,
         improve_params closure, 
         double tol, 
         Result fx)
   {
         double x, v, fv, w, fw;
         final double golden = 1 - NUM_goldenSection;
         final double sqrt_epsilon = Math.sqrt(2.2204460492503131e-16); //sqrt (NUMfpp.eps); //>>>
         long iter, itermax = 60;
         
         assert (tol > 0 && a < b);
     
         /* First step - golden section */
         
         v = a + golden * (b - a);
         fv = f.execute(v, closure);
         x = v;  w = v;
         fx.value = fv;  fw = fv;

         for(iter = 1; iter <= itermax; iter++)
         {
               double range = b - a;
               double middle_range = (a + b) / 2;
               double tol_act = sqrt_epsilon * Math.abs(x) + tol / 3;
               double new_step; /* Step at this iteration */

          

               if (Math.abs(x - middle_range) + range / 2 <= 2 * tol_act)
               {
                     return x;
               }

               /* Obtain the golden section step */
               
               new_step = golden * (x < middle_range ? b - x : a - x);


               /* Decide if the parabolic interpolation can be tried */
               
               if (Math.abs(x - w) >= tol_act)
               {
                     /*
                           Interpolation step is calculated as p/q; 
                           division operation is delayed until last moment.
                     */
                     
                     double p, q, t;

                     t = (x - w) * (fx.value - fv);
                     q = (x - v) * (fx.value - fw);
                     p = (x - v) * q - (x - w) * t;
                     q = 2 * (q - t);

                     if( q > 0)
                     {
                           p = -p;
                     }
                     else
                     {
                           q = -q;
                     }

                     /*
                           If x+p/q falls in [a,b], not too close to a and b,
                           and isn't too large, it is accepted.
                           If p/q is too large then the golden section procedure can
                           reduce [a,b] range.
                     */
                     
                     if( Math.abs (p) < Math.abs (new_step * q) &&
                           p > q * (a - x + 2 * tol_act) &&
                           p < q * (b - x - 2 * tol_act))
                     {
                           new_step = p / q;
                     }
               }

               /* Adjust the step to be not less than tolerance. */
               
               if (Math.abs(new_step) < tol_act)
               {
                     new_step = new_step > 0 ? tol_act : - tol_act;
               }

               /* Obtain the next approximation to min   and reduce the enveloping range */
               
               {
                     double t = x + new_step;      /* Tentative point for the min      */
                     double ft = f.execute(t, closure);

                     /*
                           If t is a better approximation, reduce the range so that
                           t would fall within it. If x remains the best, reduce the range
                           so that x falls within it.
                     */
                                       
                     if (ft <= fx.value)
                     {
                           if( t < x )
                           {
                                 b = x;
                           }
                           else
                           {
                                 a = x;
                           }
         
                           v = w;  w = x;  x = t;
                           fv = fw;  fw = fx.value;  fx.value = ft;
                     }
                     else
                     {                              
                           if (t < x)
                           {
                                 a = t;
                           }                   
                           else
                           {
                                 b = t;
                           }
         
                           if (ft <= fw || w == x)
                           {
                                 v = w; w = t;
                                 fv = fw; fw = ft;
                           }
                           else if (ft <= fv || v == x || v == w)
                           {
                                 v = t;
                                 fv=ft;
                           }
                     }
               }
         }
         //Melder_warning3 ("NUMminimize_brent: maximum number of iterations (", Melder_integer (itermax), ") exceeded."); //>>>
         return x;
   }
   
   static class Improve_evaluate_funObj {
      public double execute(double x, improve_params closure) {
         return NUM.improve_evaluate(x, closure);
      }
   }
   public static double improve_evaluate (double x, improve_params closure) {
      improve_params me = closure;
      double y = NUM_interpolate_sinc (me.y, me.yIdx, me.ixmax, x, me.depth);
      return (me.isMaximum!=0) ? - y : y;
   }
   static class improve_params {
      int depth;
      double[] y;
      int yIdx;
      int ixmax;
      int isMaximum;
   }
   static class Ixmid_real { //for call-by-reference
      double value;
      public Ixmid_real(double v) {
         value=v;
      }
   }
   public static final int NUM_PEAK_INTERPOLATE_NONE=  0;
   public static final int NUM_PEAK_INTERPOLATE_PARABOLIC=  1;
   public static final int NUM_PEAK_INTERPOLATE_CUBIC=  2;
   public static final int NUM_PEAK_INTERPOLATE_SINC70=  3;
   public static final int NUM_PEAK_INTERPOLATE_SINC700=  4;
   static class Result { //for call-by-reference
      double value;
   }
   public static double NUMimproveExtremum(double[] y, int yIdx, int nx, int ixmid,
         int interpolation, Ixmid_real ixmid_real, int isMaximum) {
      if(ixmid <= 1) {
         ixmid_real.value=1;
         return y[yIdx+1];
      }
      if(ixmid >= nx) {
         ixmid_real.value=nx;
         return y[yIdx+nx];
      }
      if(interpolation <= NUM_PEAK_INTERPOLATE_NONE) {
         ixmid_real.value=ixmid;
         return y[yIdx+ixmid];
      }
      if(interpolation == NUM_PEAK_INTERPOLATE_PARABOLIC) {
         double dy=0.5 * (y[yIdx+ixmid + 1] - y[yIdx+ixmid - 1]);
         double d2y=2 * y[yIdx+ixmid] - y[yIdx+ixmid - 1] - y[yIdx+ixmid + 1];
         ixmid_real.value=ixmid + dy / d2y;
         return y[yIdx+ixmid] + 0.5 * dy * dy / d2y;
      }
      /* Sinc interpolation. */
      final improve_params params=new improve_params();
      params.y=y;
      params.yIdx=yIdx;
      params.depth=interpolation == NUM_PEAK_INTERPOLATE_SINC70 ? 70 : 700;
      params.ixmax=nx;
      params.isMaximum=isMaximum;

      Result result=new Result();
      ixmid_real.value=NUMminimize_brent(new Improve_evaluate_funObj(), ixmid - 1,
            ixmid + 1, params, 1e-10, result);
      return (isMaximum != 0) ? -result.value : result.value;
   }
   public static double NUMimproveMaximum (double[] y, int yIdx, int nx, int ixmid, int interpolation, Ixmid_real ixmid_real) { 
      return NUMimproveExtremum (y, yIdx, nx, ixmid, interpolation, ixmid_real, 1); 
   }
   
}

class NUMfft_Table {
   int n;
   double[] trigcache;
   int[] splitcache;

   int NUMfft_Table_init(int n) {
      int status=0;

      this.n=n;

      this.trigcache=new double[3 * n - 1+1];
      // if (this. trigcache == null) return 0;

      this.splitcache=new int[31+1];
      // if (this. splitcache == NULL) throw new RuntimeException(); //goto end;

      NUMrffti(n, this.trigcache, this.splitcache);
      status=1;

      /*
       * end:
       * 
       * if (Melder_hasError ()) NUMfft_Table_free (me);
       */

      return status;
   }

   static void NUMrffti(int n, double[] wsave, int[] ifac) {
      if(n == 1)
         return;
      drfti1(n, wsave, (int) n, ifac);
   }

   static int[] ntryh= { 4, 2, 3, 5 };
   static double tpi=6.28318530717958647692528676655900577;

   static void drfti1(int n, double[] wa, int waIdx, int[] ifac) {
      double arg, argh, argld, fi;
      int ntry=0;
      int i;
      int j=-1;
      int k1, l1, l2, ib;
      int ld, ii, ip, is, nq, nr;
      int ido, ipm, nfm1;
      int nl=(int) n;
      int nf=0;
      boolean FIRST_FLAG=true; // ken

      // L101:
      do {
         do {
            if(FIRST_FLAG) {
               j++;
               if(j < 4) {
                  ntry=ntryh[j];
               } else {
                  ntry+=2;
               }
            }
            // L104:
            nq=nl / ntry;
            nr=nl - ntry * nq;
            FIRST_FLAG=true;
         } while(nr != 0);
         // goto L101;
         FIRST_FLAG=false;

         nf++;
         ifac[nf + 1]=ntry;
         nl=nq;
         if(ntry == 2 && nf != 1) {
            /*
             * goto L107; if (nf == 1) goto L107;
             */

            for(i=1; i < nf; i++) {
               ib=nf - i + 1;
               ifac[ib + 1]=ifac[ib];
            }
            ifac[2]=2;
         }
         // L107:
      } while(nl != 1);
      // goto L104;
      ifac[0]=n;
      ifac[1]=nf;
      argh=tpi / n;
      is=0;
      nfm1=nf - 1;
      l1=1;

      if(nfm1 == 0)
         return;

      for(k1=0; k1 < nfm1; k1++) {
         ip=(int) ifac[k1 + 2];
         ld=0;
         l2=l1 * ip;
         ido=(int) (n / l2);
         ipm=ip - 1;

         for(j=0; j < ipm; j++) {
            ld+=l1;
            i=is;
            argld=(double) ld * argh;

            fi=0.;
            for(ii=2; ii < ido; ii+=2) {
               fi+=1.;
               arg=fi * argld;
               wa[waIdx + (i++)]=Math.cos(arg);
               wa[waIdx + (i++)]=Math.sin(arg);
            }
            is+=ido;
         }
         l1=l2;
      }
   }

   static void dradfg(int ido, int ip, int l1, int idl1, double[] cc,
         int ccIdx, double[] c1, int c1Idx, double[] c2, int c2Idx,
         double[] ch, int chIdx, double[] ch2, int ch2Idx, double[] wa,
         int waIdx) {

      // static double tpi = 6.28318530717958647692528676655900577;
      int idij, ipph, i, j, k, l, ic, ik, is;
      int t0, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10;
      double dc2, ai1, ai2, ar1, ar2, ds2;
      int nbd;
      double dcp, arg, dsp, ar1h, ar2h;
      int idp2, ipp2;

      arg=tpi / (double) ip;
      dcp=Math.cos(arg);
      dsp=Math.sin(arg);
      ipph=(ip + 1) >> 1;
      ipp2=ip;
      idp2=ido;
      nbd=(ido - 1) >> 1;
      t0=l1 * ido;
      t10=ip * ido;

      if(ido != 1) {
         // goto L119;
         for(ik=0; ik < idl1; ik++)
            ch2[ch2Idx + ik]=c2[c2Idx + ik];

         t1=0;
         for(j=1; j < ip; j++) {
            t1+=t0;
            t2=t1;
            for(k=0; k < l1; k++) {
               ch[chIdx + t2]=c1[c1Idx + t2];
               t2+=ido;
            }
         }

         is=-ido;
         t1=0;
         if(nbd > l1) {
            for(j=1; j < ip; j++) {
               t1+=t0;
               is+=ido;
               t2=-ido + t1;
               for(k=0; k < l1; k++) {
                  idij=is - 1;
                  t2+=ido;
                  t3=t2;
                  for(i=2; i < ido; i+=2) {
                     idij+=2;
                     t3+=2;
                     ch[chIdx + t3 - 1]=wa[waIdx + idij - 1]
                           * c1[c1Idx + t3 - 1] + wa[waIdx + idij]
                           * c1[c1Idx + t3];
                     ch[chIdx + t3]=wa[waIdx + idij - 1] * c1[c1Idx + t3]
                           - wa[waIdx + idij] * c1[c1Idx + t3 - 1];
                  }
               }
            }
         } else {

            for(j=1; j < ip; j++) {
               is+=ido;
               idij=is - 1;
               t1+=t0;
               t2=t1;
               for(i=2; i < ido; i+=2) {
                  idij+=2;
                  t2+=2;
                  t3=t2;
                  for(k=0; k < l1; k++) {
                     ch[chIdx + t3 - 1]=wa[waIdx + idij - 1]
                           * c1[c1Idx + t3 - 1] + wa[waIdx + idij]
                           * c1[c1Idx + t3];
                     ch[chIdx + t3]=wa[waIdx + idij - 1] * c1[c1Idx + t3]
                           - wa[waIdx + idij] * c1[c1Idx + t3 - 1];
                     t3+=ido;
                  }
               }
            }
         }

         t1=0;
         t2=ipp2 * t0;
         if(nbd < l1) {
            for(j=1; j < ipph; j++) {
               t1+=t0;
               t2-=t0;
               t3=t1;
               t4=t2;
               for(i=2; i < ido; i+=2) {
                  t3+=2;
                  t4+=2;
                  t5=t3 - ido;
                  t6=t4 - ido;
                  for(k=0; k < l1; k++) {
                     t5+=ido;
                     t6+=ido;
                     c1[c1Idx + t5 - 1]=ch[chIdx + t5 - 1] + ch[chIdx + t6 - 1];
                     c1[c1Idx + t6 - 1]=ch[chIdx + t5] - ch[chIdx + t6];
                     c1[c1Idx + t5]=ch[chIdx + t5] + ch[chIdx + t6];
                     c1[c1Idx + t6]=ch[chIdx + t6 - 1] - ch[chIdx + t5 - 1];
                  }
               }
            }
         } else {
            for(j=1; j < ipph; j++) {
               t1+=t0;
               t2-=t0;
               t3=t1;
               t4=t2;
               for(k=0; k < l1; k++) {
                  t5=t3;
                  t6=t4;
                  for(i=2; i < ido; i+=2) {
                     t5+=2;
                     t6+=2;
                     c1[c1Idx + t5 - 1]=ch[chIdx + t5 - 1] + ch[chIdx + t6 - 1];
                     c1[c1Idx + t6 - 1]=ch[chIdx + t5] - ch[chIdx + t6];
                     c1[c1Idx + t5]=ch[chIdx + t5] + ch[chIdx + t6];
                     c1[c1Idx + t6]=ch[chIdx + t6 - 1] - ch[chIdx + t5 - 1];
                  }
                  t3+=ido;
                  t4+=ido;
               }
            }
         }
      }
      L119: for(ik=0; ik < idl1; ik++)
         c2[c2Idx + ik]=ch2[ch2Idx + ik];

      t1=0;
      t2=ipp2 * idl1;
      for(j=1; j < ipph; j++) {
         t1+=t0;
         t2-=t0;
         t3=t1 - ido;
         t4=t2 - ido;
         for(k=0; k < l1; k++) {
            t3+=ido;
            t4+=ido;
            c1[c1Idx + t3]=ch[chIdx + t3] + ch[chIdx + t4];
            c1[c1Idx + t4]=ch[chIdx + t4] - ch[chIdx + t3];
         }
      }

      ar1=1.;
      ai1=0.;
      t1=0;
      t2=ipp2 * idl1;
      t3=(ip - 1) * idl1;
      for(l=1; l < ipph; l++) {
         t1+=idl1;
         t2-=idl1;
         ar1h=dcp * ar1 - dsp * ai1;
         ai1=dcp * ai1 + dsp * ar1;
         ar1=ar1h;
         t4=t1;
         t5=t2;
         t6=t3;
         t7=idl1;

         for(ik=0; ik < idl1; ik++) {
            ch2[ch2Idx + t4++]=c2[c2Idx + ik] + ar1 * c2[c2Idx + t7++];
            ch2[ch2Idx + t5++]=ai1 * c2[c2Idx + t6++];
         }

         dc2=ar1;
         ds2=ai1;
         ar2=ar1;
         ai2=ai1;

         t4=idl1;
         t5=(ipp2 - 1) * idl1;
         for(j=2; j < ipph; j++) {
            t4+=idl1;
            t5-=idl1;

            ar2h=dc2 * ar2 - ds2 * ai2;
            ai2=dc2 * ai2 + ds2 * ar2;
            ar2=ar2h;

            t6=t1;
            t7=t2;
            t8=t4;
            t9=t5;
            for(ik=0; ik < idl1; ik++) {
               ch2[ch2Idx + t6++]+=ar2 * c2[c2Idx + t8++];
               ch2[ch2Idx + t7++]+=ai2 * c2[c2Idx + t9++];
            }
         }
      }

      t1=0;
      for(j=1; j < ipph; j++) {
         t1+=idl1;
         t2=t1;
         for(ik=0; ik < idl1; ik++)
            ch2[ch2Idx + ik]+=c2[c2Idx + t2++];
      }

      if(ido >= l1) {
         // goto L132;

         t1=0;
         t2=0;
         for(k=0; k < l1; k++) {
            t3=t1;
            t4=t2;
            for(i=0; i < ido; i++)
               cc[ccIdx + t4++]=ch[chIdx + t3++];
            t1+=ido;
            t2+=t10;
         }

         // goto L135;
      } else {
         L132: for(i=0; i < ido; i++) {
            t1=i;
            t2=i;
            for(k=0; k < l1; k++) {
               cc[ccIdx + t2]=ch[chIdx + t1];
               t1+=ido;
               t2+=t10;
            }
         }
      }
      L135: t1=0;
      t2=ido << 1;
      t3=0;
      t4=ipp2 * t0;
      for(j=1; j < ipph; j++) {

         t1+=t2;
         t3+=t0;
         t4-=t0;

         t5=t1;
         t6=t3;
         t7=t4;

         for(k=0; k < l1; k++) {
            cc[ccIdx + t5 - 1]=ch[chIdx + t6];
            cc[ccIdx + t5]=ch[chIdx + t7];
            t5+=t10;
            t6+=ido;
            t7+=ido;
         }
      }

      if(ido == 1)
         return;
      if(nbd >= l1) {
         // goto L141;

         t1=-ido;
         t3=0;
         t4=0;
         t5=ipp2 * t0;
         for(j=1; j < ipph; j++) {
            t1+=t2;
            t3+=t2;
            t4+=t0;
            t5-=t0;
            t6=t1;
            t7=t3;
            t8=t4;
            t9=t5;
            for(k=0; k < l1; k++) {
               for(i=2; i < ido; i+=2) {
                  ic=idp2 - i;
                  cc[ccIdx + i + t7 - 1]=ch[chIdx + i + t8 - 1]
                        + ch[chIdx + i + t9 - 1];
                  cc[ccIdx + ic + t6 - 1]=ch[chIdx + i + t8 - 1]
                        - ch[chIdx + i + t9 - 1];
                  cc[ccIdx + i + t7]=ch[chIdx + i + t8] + ch[chIdx + i + t9];
                  cc[ccIdx + ic + t6]=ch[chIdx + i + t9] - ch[chIdx + i + t8];
               }
               t6+=t10;
               t7+=t10;
               t8+=ido;
               t9+=ido;
            }
         }
         return;
      }
      L141:

      t1=-ido;
      t3=0;
      t4=0;
      t5=ipp2 * t0;
      for(j=1; j < ipph; j++) {
         t1+=t2;
         t3+=t2;
         t4+=t0;
         t5-=t0;
         for(i=2; i < ido; i+=2) {
            t6=idp2 + t1 - i;
            t7=i + t3;
            t8=i + t4;
            t9=i + t5;
            for(k=0; k < l1; k++) {
               cc[ccIdx + t7 - 1]=ch[chIdx + t8 - 1] + ch[chIdx + t9 - 1];
               cc[ccIdx + t6 - 1]=ch[chIdx + t8 - 1] - ch[chIdx + t9 - 1];
               cc[ccIdx + t7]=ch[chIdx + t8] + ch[chIdx + t9];
               cc[ccIdx + t6]=ch[chIdx + t9] - ch[chIdx + t8];
               t6+=t10;
               t7+=t10;
               t8+=ido;
               t9+=ido;
            }
         }
      }
   }

   static void dradf2(int ido, int l1, double[] cc, int ccIdx, double[] ch,
         int chIdx, double[] wa1, int walIdx) {
      int i, k;
      double ti2, tr2;
      int t0, t1, t2, t3, t4, t5, t6;

      t1=0;
      t0=(t2=l1 * ido);
      t3=ido << 1;
      for(k=0; k < l1; k++) {
         ch[chIdx + (t1 << 1)]=cc[ccIdx + t1] + cc[ccIdx + t2];
         ch[chIdx + ((t1 << 1) + t3 - 1)]=cc[ccIdx + t1] - cc[ccIdx + t2];
         t1+=ido;
         t2+=ido;
      }

      if(ido < 2)
         return;
      if(ido != 2) {
         // goto L105;

         t1=0;
         t2=t0;
         for(k=0; k < l1; k++) {
            t3=t2;
            t4=(t1 << 1) + (ido << 1);
            t5=t1;
            t6=t1 + t1;
            for(i=2; i < ido; i+=2) {
               t3+=2;
               t4-=2;
               t5+=2;
               t6+=2;
               tr2=wa1[walIdx + i - 2] * cc[ccIdx + t3 - 1]
                     + wa1[walIdx + i - 1] * cc[ccIdx + t3];
               ti2=wa1[walIdx + i - 2] * cc[ccIdx + t3] - wa1[walIdx + i - 1]
                     * cc[ccIdx + t3 - 1];
               ch[chIdx + t6]=cc[ccIdx + t5] + ti2;
               ch[chIdx + t4]=ti2 - cc[ccIdx + t5];
               ch[chIdx + t6 - 1]=cc[ccIdx + t5 - 1] + tr2;
               ch[chIdx + t4 - 1]=cc[ccIdx + t5 - 1] - tr2;
            }
            t1+=ido;
            t2+=ido;
         }

         if(ido % 2 == 1) {
            return;
         }
      }
      // L105:
      t3=(t2=(t1=ido) - 1);
      t2+=t0;
      for(k=0; k < l1; k++) {
         ch[chIdx + t1]=-cc[ccIdx + t2];
         ch[chIdx + t1 - 1]=cc[ccIdx + t3];
         t1+=ido << 1;
         t2+=ido;
         t3+=ido;
      }
   }

   static double hsqt2=.70710678118654752440084436210485;

   static void dradf4(int ido, int l1, double[] cc, int ccIdx, double[] ch,
         int chIdx, double[] wa1, int wa1Idx, double[] wa2, int wa2Idx,
         double[] wa3, int wa3Idx) {

      int i, k, t0, t1, t2, t3, t4, t5, t6;
      double ci2, ci3, ci4, cr2, cr3, cr4, ti1, ti2, ti3, ti4, tr1, tr2, tr3, tr4;

      t0=l1 * ido;

      t1=t0;
      t4=t1 << 1;
      t2=t1 + (t1 << 1);
      t3=0;

      for(k=0; k < l1; k++) {
         tr1=cc[ccIdx + t1] + cc[ccIdx + t2];
         tr2=cc[ccIdx + t3] + cc[ccIdx + t4];
         ch[chIdx + (t5=t3 << 2)]=tr1 + tr2;
         ch[chIdx + ((ido << 2) + t5 - 1)]=tr2 - tr1;
         ch[chIdx + ((t5+=(ido << 1)) - 1)]=cc[ccIdx + t3] - cc[ccIdx + t4];
         ch[chIdx + t5]=cc[ccIdx + t2] - cc[ccIdx + t1];

         t1+=ido;
         t2+=ido;
         t3+=ido;
         t4+=ido;
      }

      if(ido < 2)
         return;
      if(ido != 2) {

         t1=0;
         for(k=0; k < l1; k++) {
            t2=t1;
            t4=t1 << 2;
            t5=(t6=ido << 1) + t4;
            for(i=2; i < ido; i+=2) {
               t3=(t2+=2);
               t4+=2;
               t5-=2;

               t3+=t0;
               cr2=wa1[wa1Idx + i - 2] * cc[ccIdx + t3 - 1]
                     + wa1[wa1Idx + i - 1] * cc[ccIdx + t3];
               ci2=wa1[wa1Idx + i - 2] * cc[ccIdx + t3] - wa1[wa1Idx + i - 1]
                     * cc[ccIdx + t3 - 1];
               t3+=t0;
               cr3=wa2[wa2Idx + i - 2] * cc[ccIdx + t3 - 1]
                     + wa2[wa2Idx + i - 1] * cc[ccIdx + t3];
               ci3=wa2[wa2Idx + i - 2] * cc[ccIdx + t3] - wa2[wa2Idx + i - 1]
                     * cc[ccIdx + t3 - 1];
               t3+=t0;
               cr4=wa3[wa3Idx + i - 2] * cc[ccIdx + t3 - 1]
                     + wa3[wa3Idx + i - 1] * cc[ccIdx + t3];
               ci4=wa3[wa3Idx + i - 2] * cc[ccIdx + t3] - wa3[wa3Idx + i - 1]
                     * cc[ccIdx + t3 - 1];

               tr1=cr2 + cr4;
               tr4=cr4 - cr2;
               ti1=ci2 + ci4;
               ti4=ci2 - ci4;
               ti2=cc[ccIdx + t2] + ci3;
               ti3=cc[ccIdx + t2] - ci3;
               tr2=cc[ccIdx + t2 - 1] + cr3;
               tr3=cc[ccIdx + t2 - 1] - cr3;

               ch[chIdx + t4 - 1]=tr1 + tr2;
               ch[chIdx + t4]=ti1 + ti2;

               ch[chIdx + t5 - 1]=tr3 - ti4;
               ch[chIdx + t5]=tr4 - ti3;

               ch[chIdx + t4 + t6 - 1]=ti4 + tr3;
               ch[chIdx + t4 + t6]=tr4 + ti3;

               ch[chIdx + t5 + t6 - 1]=tr2 - tr1;
               ch[chIdx + t5 + t6]=ti1 - ti2;
            }
            t1+=ido;
         }
         if(ido % 2 == 1) {
            return;
         }
      }
      // L105:

      t2=(t1=t0 + ido - 1) + (t0 << 1);
      t3=ido << 2;
      t4=ido;
      t5=ido << 1;
      t6=ido;

      for(k=0; k < l1; k++) {
         ti1=-hsqt2 * (cc[ccIdx + t1] + cc[ccIdx + t2]);
         tr1=hsqt2 * (cc[ccIdx + t1] - cc[ccIdx + t2]);
         ch[chIdx + t4 - 1]=tr1 + cc[ccIdx + t6 - 1];
         ch[chIdx + t4 + t5 - 1]=cc[ccIdx + t6 - 1] - tr1;
         ch[chIdx + t4]=ti1 - cc[ccIdx + t1 + t0];
         ch[chIdx + t4 + t5]=ti1 + cc[ccIdx + t1 + t0];
         t1+=ido;
         t2+=ido;
         t4+=t3;
         t6+=ido;
      }
   }

   static void drftf1(int n, double[] c, int cIdx, double[] ch, double[] wa,
         int waIdx, int[] ifac) {
      int i, k1, l1, l2;
      int na, kh, nf;
      int ip, iw, ido, idl1, ix2, ix3;

      nf=(int) ifac[1];
      na=1;
      l2=n;
      iw=n;

      for(k1=0; k1 < nf; k1++) {
         kh=nf - k1;
         ip=(int) ifac[kh + 1];
         l1=l2 / ip;
         ido=n / l2;
         idl1=ido * l1;
         iw-=(ip - 1) * ido;
         na=1 - na;

         if(ip == 4) {
            // goto L102;

            ix2=iw + ido;
            ix3=ix2 + ido;
            if(na != 0)
               dradf4(ido, l1, ch, 0, c, cIdx, wa, waIdx + iw - 1, wa, waIdx
                     + ix2 - 1, wa, waIdx + ix3 - 1);
            else
               dradf4(ido, l1, c, cIdx, ch, 0, wa, waIdx + iw - 1, wa, waIdx
                     + ix2 - 1, wa, waIdx + ix3 - 1);
            // goto L110;
         } else {
            L102: if(ip != 2) {
               // goto L104;
               L104: if(ido == 1)
                  na=1 - na;
               if(na != 0) {
                  // goto L109;
                  L109: dradfg(ido, ip, l1, idl1, ch, 0, ch, 0, ch, 0, c, cIdx,
                        c, cIdx, wa, waIdx + iw - 1);
                  na=0;
               } else {
                  dradfg(ido, ip, l1, idl1, c, cIdx, c, cIdx, c, cIdx, ch, 0,
                        ch, 0, wa, waIdx + iw - 1);
                  na=1;
                  // goto L110;
               }

            } else if(na != 0) {
               // goto L103;
               L103: dradf2(ido, l1, ch, 0, c, cIdx, wa, waIdx + iw - 1);
               // goto L110;
            } else {
               dradf2(ido, l1, c, cIdx, ch, 0, wa, waIdx + iw - 1);
               // goto L110;
            }

         }
         L110: l2=l1;
      }

      if(na == 1)
         return;

      for(i=0; i < n; i++)
         c[cIdx + i]=ch[i];
   }

   void NUMfft_forward(double[] data) {
      if(this.n == 1)
         return;
      drftf1(this.n, data, 1, this.trigcache, this.trigcache, this.n,
            this.splitcache);
   }

   static void dradb2(int ido, int l1, double[] cc, int ccIdx, double[] ch,
         int chIdx, double[] wa1, int wa1Idx) {
      int i, k, t0, t1, t2, t3, t4, t5, t6;
      double ti2, tr2;

      t0=l1 * ido;

      t1=0;
      t2=0;
      t3=(ido << 1) - 1;
      for(k=0; k < l1; k++) {
         ch[chIdx + t1]=cc[ccIdx + t2] + cc[ccIdx + t3 + t2];
         ch[chIdx + t1 + t0]=cc[ccIdx + t2] - cc[ccIdx + t3 + t2];
         t2=(t1+=ido) << 1;
      }

      if(ido < 2)
         return;
      if(ido != 2) {
         // goto L105;

         t1=0;
         t2=0;
         for(k=0; k < l1; k++) {
            t3=t1;
            t5=(t4=t2) + (ido << 1);
            t6=t0 + t1;
            for(i=2; i < ido; i+=2) {
               t3+=2;
               t4+=2;
               t5-=2;
               t6+=2;
               ch[chIdx + t3 - 1]=cc[ccIdx + t4 - 1] + cc[ccIdx + t5 - 1];
               tr2=cc[ccIdx + t4 - 1] - cc[ccIdx + t5 - 1];
               ch[chIdx + t3]=cc[ccIdx + t4] - cc[ccIdx + t5];
               ti2=cc[ccIdx + t4] + cc[ccIdx + t5];
               ch[chIdx + t6 - 1]=wa1[wa1Idx + i - 2] * tr2
                     - wa1[wa1Idx + i - 1] * ti2;
               ch[chIdx + t6]=wa1[wa1Idx + i - 2] * ti2 + wa1[wa1Idx + i - 1]
                     * tr2;
            }
            t2=(t1+=ido) << 1;
         }

         if(ido % 2 == 1)
            return;
      }
      L105: t1=ido - 1;
      t2=ido - 1;
      for(k=0; k < l1; k++) {
         ch[chIdx + t1]=cc[ccIdx + t2] + cc[ccIdx + t2];
         ch[chIdx + t1 + t0]=-(cc[ccIdx + t2 + 1] + cc[ccIdx + t2 + 1]);
         t1+=ido;
         t2+=ido << 1;
      }
   }

   static double taur=-0.5;
   static double taui=0.86602540378443864676372317075293618;

   static void dradb3(int ido, int l1, double[] cc, int ccIdx, double[] ch,
         int chIdx, double[] wa1, int wa1Idx, double[] wa2, int wa2Idx) {

      int i, k, t0, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10;
      double ci2, ci3, di2, di3, cr2, cr3, dr2, dr3, ti2, tr2;

      t0=l1 * ido;

      t1=0;
      t2=t0 << 1;
      t3=ido << 1;
      t4=ido + (ido << 1);
      t5=0;
      for(k=0; k < l1; k++) {
         tr2=cc[ccIdx + t3 - 1] + cc[ccIdx + t3 - 1];
         cr2=cc[ccIdx + t5] + (taur * tr2);
         ch[chIdx + t1]=cc[ccIdx + t5] + tr2;
         ci3=taui * (cc[ccIdx + t3] + cc[ccIdx + t3]);
         ch[chIdx + t1 + t0]=cr2 - ci3;
         ch[chIdx + t1 + t2]=cr2 + ci3;
         t1+=ido;
         t3+=t4;
         t5+=t4;
      }

      if(ido == 1)
         return;

      t1=0;
      t3=ido << 1;
      for(k=0; k < l1; k++) {
         t7=t1 + (t1 << 1);
         t6=(t5=t7 + t3);
         t8=t1;
         t10=(t9=t1 + t0) + t0;

         for(i=2; i < ido; i+=2) {
            t5+=2;
            t6-=2;
            t7+=2;
            t8+=2;
            t9+=2;
            t10+=2;
            tr2=cc[ccIdx + t5 - 1] + cc[ccIdx + t6 - 1];
            cr2=cc[ccIdx + t7 - 1] + (taur * tr2);
            ch[chIdx + t8 - 1]=cc[ccIdx + t7 - 1] + tr2;
            ti2=cc[ccIdx + t5] - cc[ccIdx + t6];
            ci2=cc[ccIdx + t7] + (taur * ti2);
            ch[chIdx + t8]=cc[ccIdx + t7] + ti2;
            cr3=taui * (cc[ccIdx + t5 - 1] - cc[ccIdx + t6 - 1]);
            ci3=taui * (cc[ccIdx + t5] + cc[ccIdx + t6]);
            dr2=cr2 - ci3;
            dr3=cr2 + ci3;
            di2=ci2 + cr3;
            di3=ci2 - cr3;
            ch[chIdx + t9 - 1]=wa1[wa1Idx + i - 2] * dr2 - wa1[wa1Idx + i - 1]
                  * di2;
            ch[chIdx + t9]=wa1[wa1Idx + i - 2] * di2 + wa1[wa1Idx + i - 1]
                  * dr2;
            ch[chIdx + t10 - 1]=wa2[wa2Idx + i - 2] * dr3 - wa2[wa2Idx + i - 1]
                  * di3;
            ch[chIdx + t10]=wa2[wa2Idx + i - 2] * di3 + wa2[wa2Idx + i - 1]
                  * dr3;
         }
         t1+=ido;
      }
   }

   static double sqrt2=1.4142135623730950488016887242097;

   static void dradb4(int ido, int l1, double[] cc, int ccIdx, double[] ch,
         int chIdx, double[] wa1, int wa1Idx, double[] wa2, int wa2Idx,
         double[] wa3, int wa3Idx) {

      int i, k, t0, t1, t2, t3, t4, t5, t6, t7, t8;
      double ci2, ci3, ci4, cr2, cr3, cr4, ti1, ti2, ti3, ti4, tr1, tr2, tr3, tr4;

      t0=l1 * ido;

      t1=0;
      t2=ido << 2;
      t3=0;
      t6=ido << 1;
      for(k=0; k < l1; k++) {
         t4=t3 + t6;
         t5=t1;
         tr3=cc[ccIdx + t4 - 1] + cc[ccIdx + t4 - 1];
         tr4=cc[ccIdx + t4] + cc[ccIdx + t4];
         tr1=cc[ccIdx + t3] - cc[ccIdx + (t4+=t6) - 1];
         tr2=cc[ccIdx + t3] + cc[ccIdx + t4 - 1];
         ch[chIdx + t5]=tr2 + tr3;
         ch[chIdx + (t5+=t0)]=tr1 - tr4;
         ch[chIdx + (t5+=t0)]=tr2 - tr3;
         ch[chIdx + (t5+=t0)]=tr1 + tr4;
         t1+=ido;
         t3+=t2;
      }

      if(ido < 2)
         return;
      if(ido != 2) {
         // goto L105;

         t1=0;
         for(k=0; k < l1; k++) {
            t5=(t4=(t3=(t2=t1 << 2) + t6)) + t6;
            t7=t1;
            for(i=2; i < ido; i+=2) {
               t2+=2;
               t3+=2;
               t4-=2;
               t5-=2;
               t7+=2;
               ti1=cc[ccIdx + t2] + cc[ccIdx + t5];
               ti2=cc[ccIdx + t2] - cc[ccIdx + t5];
               ti3=cc[ccIdx + t3] - cc[ccIdx + t4];
               tr4=cc[ccIdx + t3] + cc[ccIdx + t4];
               tr1=cc[ccIdx + t2 - 1] - cc[ccIdx + t5 - 1];
               tr2=cc[ccIdx + t2 - 1] + cc[ccIdx + t5 - 1];
               ti4=cc[ccIdx + t3 - 1] - cc[ccIdx + t4 - 1];
               tr3=cc[ccIdx + t3 - 1] + cc[ccIdx + t4 - 1];
               ch[chIdx + t7 - 1]=tr2 + tr3;
               cr3=tr2 - tr3;
               ch[chIdx + t7]=ti2 + ti3;
               ci3=ti2 - ti3;
               cr2=tr1 - tr4;
               cr4=tr1 + tr4;
               ci2=ti1 + ti4;
               ci4=ti1 - ti4;

               ch[chIdx + (t8=t7 + t0) - 1]=wa1[wa1Idx + i - 2] * cr2
                     - wa1[wa1Idx + i - 1] * ci2;
               ch[chIdx + t8]=wa1[wa1Idx + i - 2] * ci2 + wa1[wa1Idx + i - 1]
                     * cr2;
               ch[chIdx + (t8+=t0) - 1]=wa2[wa2Idx + i - 2] * cr3
                     - wa2[wa2Idx + i - 1] * ci3;
               ch[chIdx + t8]=wa2[wa2Idx + i - 2] * ci3 + wa2[wa2Idx + i - 1]
                     * cr3;
               ch[chIdx + (t8+=t0) - 1]=wa3[wa3Idx + i - 2] * cr4
                     - wa3[wa3Idx + i - 1] * ci4;
               ch[chIdx + t8]=wa3[wa3Idx + i - 2] * ci4 + wa3[wa3Idx + i - 1]
                     * cr4;
            }
            t1+=ido;
         }

         if(ido % 2 == 1)
            return;
      }
      L105:

      t1=ido;
      t2=ido << 2;
      t3=ido - 1;
      t4=ido + (ido << 1);
      for(k=0; k < l1; k++) {
         t5=t3;
         ti1=cc[ccIdx + t1] + cc[ccIdx + t4];
         ti2=cc[ccIdx + t4] - cc[ccIdx + t1];
         tr1=cc[ccIdx + t1 - 1] - cc[ccIdx + t4 - 1];
         tr2=cc[ccIdx + t1 - 1] + cc[ccIdx + t4 - 1];
         ch[chIdx + t5]=tr2 + tr2;
         ch[chIdx + (t5+=t0)]=sqrt2 * (tr1 - ti1);
         ch[chIdx + (t5+=t0)]=ti2 + ti2;
         ch[chIdx + (t5+=t0)]=-sqrt2 * (tr1 + ti1);

         t3+=ido;
         t1+=t2;
         t4+=t2;
      }
   }

   static void dradbg(int ido, int ip, int l1, int idl1, double[] cc,
         int ccIdx, double[] c1, int c1Idx, double[] c2, int c2Idx,
         double[] ch, int chIdx, double[] ch2, int ch2Idx, double[] wa,
         int waIdx) {
      // static double tpi = 6.28318530717958647692528676655900577;
      int idij, ipph, i, j, k, l, ik, is, t0, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12;
      double dc2, ai1, ai2, ar1, ar2, ds2;
      int nbd;
      double dcp, arg, dsp, ar1h, ar2h;
      int ipp2;

      t10=ip * ido;
      t0=l1 * ido;
      arg=tpi / (double) ip;
      dcp=Math.cos(arg);
      dsp=Math.sin(arg);
      nbd=(ido - 1) >> 1;
      ipp2=ip;
      ipph=(ip + 1) >> 1;
      if(ido >= l1) {
         // goto L103;

         t1=0;
         t2=0;
         for(k=0; k < l1; k++) {
            t3=t1;
            t4=t2;
            for(i=0; i < ido; i++) {
               ch[chIdx + t3]=cc[ccIdx + t4];
               t3++;
               t4++;
            }
            t1+=ido;
            t2+=t10;
         }
         // goto L106;
      } else {
         L103: t1=0;
         for(i=0; i < ido; i++) {
            t2=t1;
            t3=t1;
            for(k=0; k < l1; k++) {
               ch[chIdx + t2]=cc[ccIdx + t3];
               t2+=ido;
               t3+=t10;
            }
            t1++;
         }
      }
      L106: t1=0;
      t2=ipp2 * t0;
      t7=(t5=ido << 1);
      for(j=1; j < ipph; j++) {
         t1+=t0;
         t2-=t0;
         t3=t1;
         t4=t2;
         t6=t5;
         for(k=0; k < l1; k++) {
            ch[chIdx + t3]=cc[ccIdx + t6 - 1] + cc[ccIdx + t6 - 1];
            ch[chIdx + t4]=cc[ccIdx + t6] + cc[ccIdx + t6];
            t3+=ido;
            t4+=ido;
            t6+=t10;
         }
         t5+=t7;
      }

      if(ido == 1) {
         // goto L116;
      } else if(nbd < l1) {
         // goto L112;
         L112: t1=0;
         t2=ipp2 * t0;
         t7=0;
         for(j=1; j < ipph; j++) {
            t1+=t0;
            t2-=t0;
            t3=t1;
            t4=t2;
            t7+=(ido << 1);
            t8=t7;
            t9=t7;
            for(i=2; i < ido; i+=2) {
               t3+=2;
               t4+=2;
               t8+=2;
               t9-=2;
               t5=t3;
               t6=t4;
               t11=t8;
               t12=t9;
               for(k=0; k < l1; k++) {
                  ch[chIdx + t5 - 1]=cc[ccIdx + t11 - 1] + cc[ccIdx + t12 - 1];
                  ch[chIdx + t6 - 1]=cc[ccIdx + t11 - 1] - cc[ccIdx + t12 - 1];
                  ch[chIdx + t5]=cc[ccIdx + t11] - cc[ccIdx + t12];
                  ch[chIdx + t6]=cc[ccIdx + t11] + cc[ccIdx + t12];
                  t5+=ido;
                  t6+=ido;
                  t11+=t10;
                  t12+=t10;
               }
            }
         }
      } else {

         t1=0;
         t2=ipp2 * t0;
         t7=0;
         for(j=1; j < ipph; j++) {
            t1+=t0;
            t2-=t0;
            t3=t1;
            t4=t2;

            t7+=(ido << 1);
            t8=t7;
            for(k=0; k < l1; k++) {
               t5=t3;
               t6=t4;
               t9=t8;
               t11=t8;
               for(i=2; i < ido; i+=2) {
                  t5+=2;
                  t6+=2;
                  t9+=2;
                  t11-=2;
                  ch[chIdx + t5 - 1]=cc[ccIdx + t9 - 1] + cc[ccIdx + t11 - 1];
                  ch[chIdx + t6 - 1]=cc[ccIdx + t9 - 1] - cc[ccIdx + t11 - 1];
                  ch[chIdx + t5]=cc[ccIdx + t9] - cc[ccIdx + t11];
                  ch[chIdx + t6]=cc[ccIdx + t9] + cc[ccIdx + t11];
               }
               t3+=ido;
               t4+=ido;
               t8+=t10;
            }
         }
         // goto L116;
      }

      L116: ar1=1.;
      ai1=0.;
      t1=0;
      t9=(t2=ipp2 * idl1);
      t3=(ip - 1) * idl1;
      for(l=1; l < ipph; l++) {
         t1+=idl1;
         t2-=idl1;

         ar1h=dcp * ar1 - dsp * ai1;
         ai1=dcp * ai1 + dsp * ar1;
         ar1=ar1h;
         t4=t1;
         t5=t2;
         t6=0;
         t7=idl1;
         t8=t3;
         for(ik=0; ik < idl1; ik++) {
            c2[c2Idx + t4++]=ch2[ch2Idx + t6++] + ar1 * ch2[ch2Idx + t7++];
            c2[c2Idx + t5++]=ai1 * ch2[ch2Idx + t8++];
         }
         dc2=ar1;
         ds2=ai1;
         ar2=ar1;
         ai2=ai1;

         t6=idl1;
         t7=t9 - idl1;
         for(j=2; j < ipph; j++) {
            t6+=idl1;
            t7-=idl1;
            ar2h=dc2 * ar2 - ds2 * ai2;
            ai2=dc2 * ai2 + ds2 * ar2;
            ar2=ar2h;
            t4=t1;
            t5=t2;
            t11=t6;
            t12=t7;
            for(ik=0; ik < idl1; ik++) {
               c2[c2Idx + t4++]+=ar2 * ch2[ch2Idx + t11++];
               c2[c2Idx + t5++]+=ai2 * ch2[ch2Idx + t12++];
            }
         }
      }

      t1=0;
      for(j=1; j < ipph; j++) {
         t1+=idl1;
         t2=t1;
         for(ik=0; ik < idl1; ik++)
            ch2[ch2Idx + ik]+=ch2[ch2Idx + t2++];
      }

      t1=0;
      t2=ipp2 * t0;
      for(j=1; j < ipph; j++) {
         t1+=t0;
         t2-=t0;
         t3=t1;
         t4=t2;
         for(k=0; k < l1; k++) {
            ch[chIdx + t3]=c1[c1Idx + t3] - c1[c1Idx + t4];
            ch[chIdx + t4]=c1[c1Idx + t3] + c1[c1Idx + t4];
            t3+=ido;
            t4+=ido;
         }
      }

      if(ido == 1) {
         // goto L132;
      } else if(nbd < l1) {
         // goto L128;
         L128: t1=0;
         t2=ipp2 * t0;
         for(j=1; j < ipph; j++) {
            t1+=t0;
            t2-=t0;
            t3=t1;
            t4=t2;
            for(i=2; i < ido; i+=2) {
               t3+=2;
               t4+=2;
               t5=t3;
               t6=t4;
               for(k=0; k < l1; k++) {
                  ch[chIdx + t5 - 1]=c1[c1Idx + t5 - 1] - c1[c1Idx + t6];
                  ch[chIdx + t6 - 1]=c1[c1Idx + t5 - 1] + c1[c1Idx + t6];
                  ch[chIdx + t5]=c1[c1Idx + t5] + c1[c1Idx + t6 - 1];
                  ch[chIdx + t6]=c1[c1Idx + t5] - c1[c1Idx + t6 - 1];
                  t5+=ido;
                  t6+=ido;
               }
            }
         }
      } else {

         t1=0;
         t2=ipp2 * t0;
         for(j=1; j < ipph; j++) {
            t1+=t0;
            t2-=t0;
            t3=t1;
            t4=t2;
            for(k=0; k < l1; k++) {
               t5=t3;
               t6=t4;
               for(i=2; i < ido; i+=2) {
                  t5+=2;
                  t6+=2;
                  ch[chIdx + t5 - 1]=c1[c1Idx + t5 - 1] - c1[c1Idx + t6];
                  ch[chIdx + t6 - 1]=c1[c1Idx + t5 - 1] + c1[c1Idx + t6];
                  ch[chIdx + t5]=c1[c1Idx + t5] + c1[c1Idx + t6 - 1];
                  ch[chIdx + t6]=c1[c1Idx + t5] - c1[c1Idx + t6 - 1];
               }
               t3+=ido;
               t4+=ido;
            }
         }
         // goto L132;
      }

      L132: if(ido == 1)
         return;

      for(ik=0; ik < idl1; ik++)
         c2[c2Idx + ik]=ch2[ch2Idx + ik];

      t1=0;
      for(j=1; j < ip; j++) {
         t2=(t1+=t0);
         for(k=0; k < l1; k++) {
            c1[c1Idx + t2]=ch[chIdx + t2];
            t2+=ido;
         }
      }

      if(nbd <= l1) {
         // goto L139;

         is=-ido - 1;
         t1=0;
         for(j=1; j < ip; j++) {
            is+=ido;
            t1+=t0;
            idij=is;
            t2=t1;
            for(i=2; i < ido; i+=2) {
               t2+=2;
               idij+=2;
               t3=t2;
               for(k=0; k < l1; k++) {
                  c1[c1Idx + t3 - 1]=wa[waIdx + idij - 1] * ch[chIdx + t3 - 1]
                        - wa[waIdx + idij] * ch[chIdx + t3];
                  c1[c1Idx + t3]=wa[waIdx + idij - 1] * ch[chIdx + t3]
                        + wa[waIdx + idij] * ch[chIdx + t3 - 1];
                  t3+=ido;
               }
            }
         }
         return;
      }
      L139: is=-ido - 1;
      t1=0;
      for(j=1; j < ip; j++) {
         is+=ido;
         t1+=t0;
         t2=t1;
         for(k=0; k < l1; k++) {
            idij=is;
            t3=t2;
            for(i=2; i < ido; i+=2) {
               idij+=2;
               t3+=2;
               c1[c1Idx + t3 - 1]=wa[waIdx + idij - 1] * ch[chIdx + t3 - 1]
                     - wa[waIdx + idij] * ch[chIdx + t3];
               c1[c1Idx + t3]=wa[waIdx + idij - 1] * ch[chIdx + t3]
                     + wa[waIdx + idij] * ch[chIdx + t3 - 1];
            }
            t2+=ido;
         }
      }
   }

   static void drftb1(int n, double[] c, int cIdx, double[] ch, double[] wa,
         int waIdx, int[] ifac) {
      int i, k1, l1, l2;
      int na;
      int nf, ip, iw, ix2, ix3, ido, idl1;

      nf=ifac[1];
      na=0;
      l1=1;
      iw=1;

      for(k1=0; k1 < nf; k1++) {
         ip=ifac[k1 + 2];
         l2=ip * l1;
         ido=n / l2;
         idl1=ido * l1;
         if(ip == 4) {
            // goto L103;
            ix2=iw + ido;
            ix3=ix2 + ido;

            if(na != 0)
               dradb4(ido, l1, ch, 0, c, cIdx, wa, waIdx + iw - 1, wa, waIdx
                     + ix2 - 1, wa, waIdx + ix3 - 1);
            else
               dradb4(ido, l1, c, cIdx, ch, 0, wa, waIdx + iw - 1, wa, waIdx
                     + ix2 - 1, wa, waIdx + ix3 - 1);
            na=1 - na;
            // goto L115;
         } else {
            L103: if(ip == 2) {
               // goto L106;

               if(na != 0)
                  dradb2(ido, l1, ch, 0, c, cIdx, wa, waIdx + iw - 1);
               else
                  dradb2(ido, l1, c, cIdx, ch, 0, wa, waIdx + iw - 1);
               na=1 - na;
               // goto L115;
            } else {
               L106: if(ip == 3) {
                  // goto L109;

                  ix2=iw + ido;
                  if(na != 0)
                     dradb3(ido, l1, ch, 0, c, cIdx, wa, waIdx + iw - 1, wa,
                           waIdx + ix2 - 1);
                  else
                     dradb3(ido, l1, c, cIdx, ch, 0, wa, waIdx + iw - 1, wa,
                           waIdx + ix2 - 1);
                  na=1 - na;
                  // goto L115;
               } else {
                  L109:
                  /* The radix five case can be translated later..... */
                  /*
                   * if(ip!=5)goto L112;
                   * 
                   * ix2=iw+ido; ix3=ix2+ido; ix4=ix3+ido; if(na!=0)
                   * dradb5(ido,l1,ch,c,wa+iw-1,wa+ix2-1,wa+ix3-1,wa+ix4-1);
                   * else
                   * dradb5(ido,l1,c,ch,wa+iw-1,wa+ix2-1,wa+ix3-1,wa+ix4-1);
                   * na=1-na; goto L115;
                   * 
                   * L112:
                   */
                  if(na != 0)
                     dradbg(ido, ip, l1, idl1, ch, 0, ch, 0, ch, 0, c, cIdx, c,
                           cIdx, wa, waIdx + iw - 1);
                  else
                     dradbg(ido, ip, l1, idl1, c, cIdx, c, cIdx, c, cIdx, ch,
                           0, ch, 0, wa, waIdx + iw - 1);
                  if(ido == 1)
                     na=1 - na;
               }
            }
         }
         L115: l1=l2;
         iw+=(ip - 1) * ido;
      }

      if(na == 0)
         return;

      for(i=0; i < n; i++)
         c[cIdx + i]=ch[i];
   }

   void NUMfft_backward(double[] data) {
      if(this.n == 1)
         return;
      drftb1(this.n, data, 1, this.trigcache, this.trigcache, this.n,
            this.splitcache);
   }
   
   
}
