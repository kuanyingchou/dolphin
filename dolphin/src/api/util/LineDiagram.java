package api.util;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

public class LineDiagram extends JComponent {
   public java.util.List<Float> data=new ArrayList<Float>();
   //public java.util.List<Integer> sIndices=new ArrayList<Integer>();
   private float maximum=-1;
   
   public void addData(float d) {
      data.add(d);
      //setPreferredSize(new Dimension(data.size(), getHeight()));
   }
   public void addData(float[] d) {
      for(int i=0; i < d.length; i++) {
         data.add(d[i]);
      }
      //setPreferredSize(new Dimension(data.size(), getHeight()));
   }
   public void setMaximum(float max) {
      this.maximum=max;
   }
   
   @Override
   protected void paintComponent(Graphics g) {      
      super.paintComponent(g);
      
      //[ background
      g.setColor(Color.black);
      g.fillRect(0, 0, getWidth(), getHeight());
      
      //[ middle horizontal line
      g.setColor(Color.gray);
      g.drawLine(0, getHeight()/2, getWidth(), getHeight()/2);
      
      final Rectangle vRect=getVisibleRect();
      if(data.isEmpty()) return;
      //float max=Collections.max(data);
      if(maximum<0) maximum=Collections.max(data);
      //System.err.println(max);
      /*float pMin=Collections.min(data);
      float max=pMax-pMin;*/
      int lastY=-1;
      int lastX=-1;
      g.setColor(Color.green);
      for(int i=0; i < data.size(); i++) {
         //if(data.get(i)<0) System.err.println(data.get(i));
         final int y=(int)(getHeight()/2 - (data.get(i)/maximum*getHeight()/2));
         final int x=(int)((float)i*getWidth()/data.size());
         if(x==lastX && y==lastY) continue;
         if(lastY>=0) {
            if(x>=vRect.x && x<vRect.x+vRect.width &&
               y>=vRect.y && y<vRect.y+vRect.height) {
                  g.drawLine(lastX, lastY, x, y);
            }
         }
         lastY=y;
         lastX=x;
      }
   }
   public void display() {
      final JFrame jf=new JFrame();
      //jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      jf.add(new JScrollPane(this));
      //jf.pack();
      jf.setSize(400, 300);
      jf.setVisible(true);
   }
   public void clearData() {
      data.clear();
   }
}
