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


public class BasicScorePlayer extends ScorePlayer implements Receiver, MetaEventListener {
      private boolean paused=false;
      private final java.util.List<Receiver> receivers=new ArrayList<Receiver>();
      //private static final ScorePlayer instance=new BasicScorePlayer();
      //private DumpReceiver debugDump=new DumpReceiver(System.err);
      //private ScoreView sv;
      //private Receiver output=null;
      
      public BasicScorePlayer() {
         // TODO Auto-generated constructor stub
      }
      
      /*public void setOutput(Receiver r) {
         output=r;
      }*/
      
      //usage:
      //play: playing
      //pause: paused, not playing
      //play: playing
      //stop: not paused, not playing, stopped
      /* (non-Javadoc)
       * @see api.midi.ScorePlayer#isPlaying()
       */
      @Override
      public boolean isPlaying() { return sequencer==null?false:sequencer.isRunning(); }
      /* (non-Javadoc)
       * @see api.midi.ScorePlayer#isStopped()
       */
      @Override
      public boolean isStopped() { return isPlaying()?false:(paused?false:true); }
      //public void setScoreView(ScoreView s) { sv=s; }
      
      private Sequencer sequencer=null;
      /* (non-Javadoc)
       * @see api.midi.ScorePlayer#play(api.model.Score)
       */
      @Override
      public void play(Score s) {
         play(s, new Path(0, 0));
      }
      private Score score=null;
      /* (non-Javadoc)
       * @see api.midi.ScorePlayer#getScore()
       */
      @Override
      public Score getScore() {
         return score;
      }
      /* (non-Javadoc)
       * @see api.midi.ScorePlayer#play(api.model.Score, api.model.Path)
       */
      @Override
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
               final long startTick=score.get(startPath.partIndex).get(startPath.noteIndex).getTick();
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
      
      /* (non-Javadoc)
       * @see api.midi.ScorePlayer#pause()
       */
      @Override
      public void pause() {
         if(sequencer==null || !sequencer.isRunning()) return;
         sequencer.stop();
         paused=true;
      }
      /* (non-Javadoc)
       * @see api.midi.ScorePlayer#stop()
       */
      @Override
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
      /* (non-Javadoc)
       * @see api.midi.ScorePlayer#setTempoFactor(float)
       */
      @Override
      public void setTempoFactor(float tf) {
         tempoFactor=tf;
         if(sequencer!=null) sequencer.setTempoFactor(tempoFactor);
      }
      
      
      /* (non-Javadoc)
       * @see api.midi.ScorePlayer#addReceiver(javax.sound.midi.Receiver)
       */
      @Override
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

      /* (non-Javadoc)
       * @see api.midi.ScorePlayer#getMicrosecondLength()
       */
      @Override
      public long getMicrosecondLength() {
         if(sequencer==null) return 0;
         return sequencer.getMicrosecondLength();
      }

      //private static ScorePlayer getInstance() {
      //   return instance;
      //}

      @Override
      public void setVolume(int partIndex, int volume) {
         // TODO Auto-generated method stub
         
      }

      @Override
      public void setPan(int part, int pan) {
         // TODO Auto-generated method stub
         
      }

      @Override
      public void setMicrosecondPosition(long pos) {
         if(sequencer!=null) {
            sequencer.setMicrosecondPosition(pos);
         }
      }

      @Override
      public void reset() {
         // TODO Auto-generated method stub
         
      }
     
     
   } 