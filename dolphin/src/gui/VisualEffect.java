package gui;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import api.util.Util;


public class VisualEffect extends JComponent implements Receiver {
   public static final int NUM_CHANNEL=16;
   public static final int NUM_PITCH=128;
   private final double[][] bars=new double[NUM_CHANNEL][NUM_PITCH];
   private final double[] f=new double[NUM_PITCH];
   
   public VisualEffect() {
      //setMaximumSize(new Dimension(128, 32));
      final Dimension size=new Dimension(NUM_PITCH, 32);
      setPreferredSize(size);
      setMaximumSize(size);
      setMinimumSize(size);
      
      for(int i=0; i < f.length; i++) {
         f[i]=0.1;
      }
      
      new Thread(new Runnable() {
         public void run() {
            final double a=9.8;
            
            while(true) {
               SwingUtilities.invokeLater(new Runnable() {
                  public void run() {
                     for(int i=0; i < bars.length; i++) {
                        for(int j=0; j<bars[i].length; j++) {
                           if(bars[i][j]>0) bars[i][j]-=(f[j]*=a);
                           else f[j]=1.0;   
                        }
                     }      
                     repaint();
                  }
               });
               Util.sleep(100);
            }
            
         }
      }).start();
   }
   
   @Override
   public void close() {
      
   }

   @Override
   public void send(MidiMessage message, long timeStamp) {
      if(message instanceof ShortMessage) {
         final ShortMessage sm=(ShortMessage)message;
         if(sm.getCommand()==0x90) {
            SwingUtilities.invokeLater(new Runnable() {
               public void run() {
                  updateData(sm.getChannel(), sm.getData1(), sm.getData2());      
               }
            });
            //decreaseAll();
            repaint();
         } 
      }
      
   }
   private void updateData(int channel, int pitch, int velocity) {
      if(velocity>bars[channel][pitch]) bars[channel][pitch]=velocity;
   }
   /*private void decreaseAll() {
      for(int i=0; i < bars.length; i++) {
         if(bars[i]>0) bars[i]--;
      }
   }*/

   final int nCellH=16;
   final int nCellV=8;
   double cellWidth;
   double cellHeight;
   
   private void drawCell(Graphics g, int channel, int pitch, double velocity) {
      int cellIndexY=nCellV-(int)((double)velocity/NUM_PITCH*nCellV);
      final int cellIndexX=(int)((double)pitch/NUM_PITCH*nCellH);
      g.setColor(Util.beachColors[2]);
      g.fillRect((int)(cellIndexX*cellWidth), 
                     (int)(cellIndexY*cellHeight), 
                     (int)cellWidth, 
                     (int)(cellHeight*NUM_PITCH-cellIndexY));
      g.setColor(Color.white);
      g.fillRect((int)(cellIndexX*cellWidth), 
            (int)(cellIndexY*cellHeight), 
            (int)cellWidth, 
            (int)(cellHeight));
   }
   
   @Override
   protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      
      g.setColor(Util.beachColors[0]);
      
      g.fillRect(0, 0, getWidth(), getHeight());
      final double barWidth=(double)getWidth()/NUM_PITCH;
      
      cellWidth=(double)getWidth()/nCellH;
      cellHeight=(double)getHeight()/nCellV;
      
      int colorIndex=0;
      //g.setColor(Color.lightGray);
      for(int i=0; i < bars.length; i++) {
         //g.setColor(Util.colors[(colorIndex++)%Util.colors.length]);
         //g.setColor(Color.green);
         for(int j=0; j < bars[i].length; j++) {
            //final double barHeight=(double)getHeight()/NUM_PITCH*bars[i][j];
            drawCell(g, i, j, bars[i][j]);
            /*g.fillRect((int)(j*barWidth), 
                  (int)(getHeight()-barHeight), 
                  (barWidth)<1?1:(int)barWidth, (int)barHeight);*/   
         } 
      }
      
      //[ draw grids
      g.setColor(Util.beachColors[1]);
      g.drawRect(0, 0, getWidth()-1, getHeight()-1);
      for(int i=0; i<nCellH; i++) {
         g.drawLine((int)(i*cellWidth), 0, (int)(i*cellWidth), getHeight());
      }
      for(int i=0; i<nCellV; i++) {
         g.drawLine(0, (int)(i*cellHeight), getWidth(), (int)(i*cellHeight));
      }
   }
}
