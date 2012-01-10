package gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DecimalFormat;
import java.util.Vector;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.MidiDevice.Info;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import api.midi.ScorePlayer;
import api.model.Path;
import api.util.Util;

import view.ScoreView;

public class PlayToolBar extends JToolBar {
   private final MainFrame mainFrame;
   final JSlider progress;
   private static class TimeButton extends JButton {
      long time, length;
      public TimeButton() {
         setTime(0);
      }
      public void setTime(long t) {
         time=t;
         displayTime();
      }
      public void setLength(long l) {
         length=l;
      }
      public void setReverse(boolean b) {
         reverse=b;
      }
//      public void setDisplayRests(boolean b) {
//         
//      }
      private final DecimalFormat df=new DecimalFormat("00");
      private boolean reverse=false;
      private void displayTime() {
         final int minuteCurrent=(int)(time/60000000.0);
         final int secondCurrent=(int)((time/1000000.0)-60*minuteCurrent);
         final int minuteAll=(int)(length/60000000.0);
         final int secondAll=(int)((length/1000000.0)-60*minuteAll);
         if(reverse) {
            final int secondCurrentAll=(int)((length/1000000.0));
            final int restSecondAll=secondCurrentAll-(minuteCurrent*60+secondCurrent);
            
            final int restMinute=restSecondAll/60;
            final int restSecond=restSecondAll-restMinute*60;
            setText(df.format(restMinute)+":"+df.format(restSecond)+
                  "/"+df.format(minuteAll)+":"+df.format(secondAll));   
         } else {
            setText(df.format(minuteCurrent)+":"+df.format(secondCurrent)+
                  "/"+df.format(minuteAll)+":"+df.format(secondAll));
         }         
      }
      public boolean getReverse() {
        return reverse;
      }
   }
   final TimeButton timeButton;
   
   public PlayToolBar(MainFrame mf) {
      mainFrame=mf;
      setName("Play Tools");
      //setLayout(new FlowLayout());
      //[ playback controls
      final JButton playAllButton=new JButton(Util.getImageIcon("images/playall.png"));
      final JButton playButton=new JButton(Util.getImageIcon("images/player_play.png"));
      final JButton pauseButton=new JButton(Util.getImageIcon("images/player_pause.png"));
      final JButton stopButton=new JButton(Util.getImageIcon("images/player_stop.png"));
      
//      playAllButton.setText("Play All");
//      playButton.setText("Play");
//      pauseButton.setText("Pause");
//      stopButton.setText("Stop");
      
      playAllButton.setToolTipText("Play All");
      playButton.setToolTipText("Play");
      pauseButton.setToolTipText("Pause");
      stopButton.setToolTipText("Stop");
      //nextButton.setToolTipText("Next");
      //previousButton.setToolTipText("Previous");
      
      //[ progress slider
      //progress.setOpaque(false);
      progress=new JSlider(0, 0, 0);
      progress.setEnabled(false);
      progress.addChangeListener(new ChangeListener() {
         public void stateChanged(ChangeEvent e) {
            //if(progress.getValueIsAdjusting()) return;
            if(!ScorePlayer.instance.isPlaying()) {
               ScorePlayer.instance.setTickPosition(progress.getValue());
               timeButton.setTime(ScorePlayer.instance.getMicrosecondPosition());
               //System.err.println(progress.getValue());
            }
         }
      });
      
      //[ time
      timeButton=new TimeButton();
      timeButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            timeButton.setReverse(!timeButton.getReverse());
         }
      });
      
      //[ speed
      final JButton tempoLabel=new JButton("1.00x");
      
      final JSlider tempoFactorSlider=new JSlider(1, 200, 100);
      tempoFactorSlider.setPreferredSize(new Dimension(100, 30));
      tempoFactorSlider.setMajorTickSpacing(10);
      tempoFactorSlider.setMinorTickSpacing(1);
      tempoFactorSlider.setSnapToTicks(true);
      tempoFactorSlider.addChangeListener(new ChangeListener() {
         public void stateChanged(ChangeEvent e) {
            final float factor=tempoFactorSlider.getValue()/100.0f;
            tempoLabel.setText(new DecimalFormat("0.00x").format(factor));
            ScorePlayer.instance.setTempoFactor(factor);
         }
      });
      tempoLabel.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            tempoFactorSlider.setValue(100);
         }
      });
      
      
      
      playAllButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            doPlay(false);
         }
      });
      playButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            doPlay(true);
         }
      });
      pauseButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            ScorePlayer.instance.pause();
            repaint();
         }
      });
      stopButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            ScorePlayer.instance.stop();
            progress.setValue(0);
            //progress.setEnabled(false);
            repaint();
         }
      });    
      
      
      
      
      
      
      this.add(playAllButton);
      this.add(playButton);
      this.add(pauseButton);
      this.add(stopButton);
      //this.add(previousButton);
      //this.add(nextButton);
      this.addSeparator();
      this.add(progress);
      this.add(timeButton);
      this.addSeparator();
      this.add(new JLabel("Speed:"));
      this.add(tempoFactorSlider);
      this.add(tempoLabel);
      //>>> add a refresh button
      
   }
   
   private void doPlay(boolean startFromCursor) {
      final ViewPane vf=((ViewPane)mainFrame.desktop.getSelectedComponent());//.getC.getSelectedFrame());
      if(vf==null) return;
      final ScoreView sheet=vf.scoreView;
      if(startFromCursor && sheet.score.isValidSelectPath(sheet.staticCursor)) {
         //final Note currentNote=sheet.score.get(sheet.staticCursor.partIndex).get(sheet.staticCursor.index);
         //if(currentNote==null) throw new RuntimeException();
         ScorePlayer.instance.play(sheet.score, new Path(sheet.staticCursor));
      } else {
         ScorePlayer.instance.play(sheet.score);
      }
      repaint();
      if(!ScorePlayer.instance.isPlaying()) return;
      progress.setEnabled(true);
      progress.setMaximum((int)ScorePlayer.instance.getTickLength()); //>>> cast
      timeButton.setLength(ScorePlayer.instance.getMicrosecondLength());
      new Thread(new Runnable() {
         public void run() {
            while(!ScorePlayer.instance.isStopped()) {
               final long nextTickPos=ScorePlayer.instance.getTickPosition();
               if(ScorePlayer.instance.isPlaying()) {
                  SwingUtilities.invokeLater(new Runnable() {
                     public void run() {
                        progress.setValue((int)nextTickPos);
                        timeButton.setTime(ScorePlayer.instance.getMicrosecondPosition());
                        //setProgressTime(nextMsPos);
                     }
                  });
               }
               try {
                  Thread.sleep(100);
               } catch(InterruptedException e) {}
            }
            SwingUtilities.invokeLater(new Runnable() {
               public void run() {
                  progress.setValue(progress.getMaximum());
                  progress.setValue(0);
                  //progress.setEnabled(false);
            }});
         }
      }).start();
   }
}