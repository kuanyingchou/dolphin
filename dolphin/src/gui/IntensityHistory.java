package gui;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;


import api.audio.IntensityListener;
import api.audio.SoundAnalyzer;
import api.util.Util;

public class IntensityHistory extends JComponent implements IntensityListener {
   private float[] data;
   private int nextIndex=0;
   private float maximum=512;

   public IntensityHistory() {
      data=new float[128];
      setMaximum(256);
      final Dimension size=new Dimension(data.length, 32);
      setPreferredSize(size);
      setMinimumSize(size);
      setMaximumSize(size);
   }
   public void addData(float d) {
      data[nextIndex++]=d;
      if(nextIndex==data.length) nextIndex=0;
      repaint();
   }
//   public void addData(float[] d) {
//      for(int i=0; i < d.length; i++) {
//         data[index++]=d[i];
//      }
//      if(index==data.length) index=0;
//      repaint();
//   }
   public void setMaximum(float max) {
      this.maximum=max;
   }
//   public void setThreshold(int v) {
//      threshold=v;
//   }
   private static final Color bg=new Color(242, 135, 5);
   private static final Color fg=new Color(242, 203, 5);
   @Override
   protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      
      final int threshold=SoundAnalyzer.settings.noiseLevel;
      
      //[ background
      g.setColor(bg);
      g.fillRect(0, 0, getWidth(), getHeight());
      
      //if(nextIndex==0) return;
      final Rectangle vRect=getVisibleRect();
      //float max=Collections.max(data);
      //if(maximum<0) maximum=Collections.max(Arrays.asList(data));
      //System.err.println(max);
      /*float pMin=Collections.min(data);
      float max=pMax-pMin;*/
      int lastY=-1;
      int lastX=-1;
      
      for(int i=0; i < data.length; i++) {
         //if(data.get(i)<0) System.err.println(data.get(i));
         int current=(i+nextIndex)%data.length;
//         if(data[current]>=threshold) g.setColor(Color.white);
//         else 
         g.setColor(fg);
         final int y=(int)(getHeight() - (data[current]/maximum*getHeight()));
         final int x=(int)((float)(i)*getWidth()/data.length);
         //if(x==lastX && y==lastY) continue;
         //g.drawLine(x, getHeight(), x, y);
         if(lastY>=0) {
            if(x>=vRect.x && x<vRect.x+vRect.width &&
               y>=vRect.y && y<vRect.y+vRect.height) {
                  g.drawLine(lastX, lastY, x, y);
            }
         }
         lastY=y;
         lastX=x;
      }
      final int ty=(int)(getHeight() - (threshold/maximum*getHeight()));
      g.setColor(fg);
      g.drawLine(0, ty, getWidth(), ty);
      
      g.setColor(fg);
      g.drawRect(0, 0, getWidth()-1, getHeight()-1);
   }
   
   @Override
   public void gotIntensity(double intensity) {
      addData((float)intensity);
   }
}
