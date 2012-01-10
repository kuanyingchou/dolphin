package gui;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JProgressBar;


import api.audio.IntensityListener;
import api.util.Util;


public class IntensityMeter extends JComponent implements IntensityListener {
   int maximum, value, threshold;
   
   public IntensityMeter() {
      setMaximum(500);
      final Dimension size=new Dimension(100, 30);
      setPreferredSize(size);
      setMinimumSize(size);
      setMaximumSize(size);
      
      setBorder(BorderFactory.createLoweredBevelBorder());
   }

   public void setMaximum(int v) {
      maximum=v;
   }
   public void setValue(int v) {
      value=v;
      repaint();
   }
   public void setThreshold(int v) {
      threshold=v;
   }
   
   @Override
   public void gotIntensity(double intensity) {
      setValue((int)intensity);
   }
   
   @Override
   protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      
      final Insets insets=getInsets();
      final int width=getWidth()-insets.left-insets.right;
      final int height=getHeight()-insets.top-insets.bottom;
      g.translate(insets.left, insets.top);
      
      g.setColor(Util.beachColors[1]);
      g.fillRect(0, 0, width, height);
      
      g.setColor(Util.beachColors[0]);
      final int valueWidth=(int)((double)value/maximum*width);
      g.fillRect(0, 0, valueWidth, height);
      g.setColor(Color.red);
      final int thresholdWidth=(int)((double)threshold/maximum*width);
      g.drawLine(thresholdWidth, 0, thresholdWidth, height);
      
      //g.setXORMode(Color.black);
      g.setColor(Color.white);
      g.drawString(String.valueOf(value), 0, height);
      //g.setPaintMode();
      
      g.translate(-insets.left, -insets.top);
      //g.setColor(Color.gray);
      //g.drawRect(0, 0, getWidth()-1, getHeight()-1);
   }
}
