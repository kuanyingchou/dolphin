package api.midi;

import java.util.ArrayList;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.MidiDevice.Info;
import javax.swing.SwingUtilities;

import api.model.Path;
import api.model.Score;


import api.model.*;


public class ScorePlayer implements Receiver, MetaEventListener {
      private boolean paused=false;
      private final java.util.List<Receiver> receivers=new ArrayList<Receiver>();
      public static final ScorePlayer instance=new ScorePlayer();
      //private DumpReceiver debugDump=new DumpReceiver(System.err);
      //private ScoreView sv;
      //private Receiver output=null;
      
      private ScorePlayer() {
         
      }
      /*public void setOutput(Receiver r) {
         output=r;
      }*/
      
      //usage:
      //play: playing
      //pause: paused, not playing
      //play: playing
      //stop: not paused, not playing, stopped
      public boolean isPlaying() { return sequencer==null?false:sequencer.isRunning(); }
      public boolean isStopped() { return isPlaying()?false:(paused?false:true); }
      //public void setScoreView(ScoreView s) { sv=s; }
      
      private Sequencer sequencer=null;
      public void play(Score s) {
         play(s, new Path(0, 0));
      }
      private Score score=null;
      public Score getScore() {
         return score;
      }
      public void play(Score score, Path startPath) {
         if(score==null) return; //>>>is null score an error?
         if(!score.isValidSelectPath(startPath)) {
            //throw new IllegalArgumentException();
            //>>> empty score
            return;
         }
         this.score=score;
         
         if(paused) {
            sequencer.setTempoFactor(tempoFactor);
            sequencer.start();
         } else {
            if(!isStopped()) stop();
            try {
               final Sequence sequence = score.toSequence();
//DumpSequence.dump(sequence);
               final long startTick=score.get(startPath.partIndex).get(startPath.index).getTick();
               sequencer = MidiSystem.getSequencer(false);
               sequencer.setSequence(sequence);
               //sequencer.getTransmitter().setReceiver(DeviceManager.outDevice.getReceiver());
               //MainFrame.deviceManager.register(sequencer.getTransmitter());
               sequencer.getTransmitter().setReceiver(OutDeviceManager.instance);
               sequencer.getTransmitter().setReceiver(this); //>>>
               //sequencer.getTransmitter().setReceiver(debugDump); //>>>
               
               for(int i=0; i<receivers.size(); i++) {
                  sequencer.getTransmitter().setReceiver(receivers.get(i));
               }
               sequencer.addMetaEventListener(this);
               /*sequencer.addMetaEventListener(new MetaEventListener() {
                  public void meta(MetaMessage meta) {
                     if(meta.getType()==47) {
                        stop();
                     }
                  }
               });*/ //>>> may not play last note
               // DumpReceiver(System.out));
               if (!sequencer.isOpen())
                  sequencer.open();
               if (sequencer.isRunning())
                  sequencer.stop();
               //if(!MainFrame.deviceManager.outDevice.isOpen()) 
               //   MainFrame.deviceManager.outDevice.open(); //>>>???
               
               sequencer.setTickPosition(startTick);
               sequencer.setTempoFactor(tempoFactor);
               sequencer.start();
            } catch (InvalidMidiDataException e1) {
               e1.printStackTrace();
            } catch (MidiUnavailableException e2) {
               e2.printStackTrace();
            }
         }
      }
      
      public void pause() {
         if(sequencer==null || !sequencer.isRunning()) return;
         sequencer.stop();
         paused=true;
      }
      public void stop() {
         if(sequencer!=null) {
            if(sequencer.isRunning()) {     
               sequencer.stop();
            }
            if(sequencer.isOpen()) {
               sequencer.close();
            }
         }
         //if(DeviceManager.outDevice.isOpen()) DeviceManager.outDevice.close(); //>>>
         paused=false;
      }
      
      public long getTickPosition() {
         return sequencer.getTickPosition();
      }
      public long getMicrosecondPosition() {
         return sequencer.getMicrosecondPosition();
      } 
      public void setTickPosition(long pos) {
         sequencer.setTickPosition(pos);
      }
      public long getTickLength() {
         if(sequencer==null) return 0; //>>>
         return sequencer.getTickLength();
      }
      
      float tempoFactor=1.0f;
      public void setTempoFactor(float tf) {
         tempoFactor=tf;
         if(sequencer!=null) sequencer.setTempoFactor(tempoFactor);
      }
      
      
      public void addReceiver(Receiver rec) {
         receivers.add(rec);
      }
      
      //[ implements interfaces
      @Override
      public void close() {
         
      }
      @Override
      public void send(final MidiMessage msg, long timestamp) {
         //System.err.println(Thread.currentThread());
         /* >>> move to a 'PlayListener'
         SwingUtilities.invokeLater(new Runnable() {
            public void run() { //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> need a better implementation
               if(!(msg instanceof ShortMessage)) return;
               final ShortMessage sm=(ShortMessage)msg;
               //for (int j=0; j<score.score.size(); j++) {
               if(sv.score.isValidSelectPath(sv.staticCursor));
                  final Part p=sv.score.get(sv.staticCursor.partIndex);
                  for (int i = 0; i < p.noteCount(); i++) {
                     final Note n = p.get(i);
                     if (n.binding == msg) {
                        p.playIndex = i;
                     }
                  }
                  sv.score.notifyScoreChange(new PlayUpdateChange(sv.score));
               //} 

            }
         });
         */
      }
      @Override
      public void meta(MetaMessage meta) {
         /*if (meta.getType() == 47) {
            System.err.println("stopped");
         }*/
         
         if (meta.getType() == 47) {
            SwingUtilities.invokeLater(new Runnable() {
               public void run() {
                  /*for (int j=0; j<sv.score.partCount(); j++) {
                     final Part p=sv.score.get(j);
                     p.playIndex = 0;
                  }*/
                  stop();
               }
            });
            
         }
         //sv.score.notifyScoreChange(new PlayUpdateChange(sv.score));
         
      }

      public long getMicrosecondLength() {
         if(sequencer==null) return 0;
         return sequencer.getMicrosecondLength();
      }
     
     
   } 