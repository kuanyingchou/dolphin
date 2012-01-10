package api.audacity;

import api.util.LineDiagram;

public class CosWav {
   public static void main(String[] args) {
      final LineDiagram dia=new LineDiagram();
      double A=10.0;
      double w=2*Math.PI/440;
      double f=(-0.4)*Math.PI;
      for(int t=0; t<2560; t++) {
         final double sample=(A*Math.cos(w*t+f));
         dia.addData((float)sample);
         System.err.println(sample);
      }
      dia.display();
   }
}
