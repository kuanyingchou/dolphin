package api.midi;


import java.io.File;
import java.util.concurrent.ThreadFactory;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import javax.swing.SwingWorker;

import api.model.Instrument;
import api.model.Note;
import api.model.Part;
import api.model.Path;
import api.model.Score;
import api.util.Util;

public class RealTimeScorePlayer implements Runnable {
   
   private Score score;
   
   enum PlayerState {
      STOPPED,
      PLAYING,
      PAUSED
   }
   private PlayerState state=PlayerState.STOPPED;
   
   PartPlayer[] partPlayers;
   
   private long progress=0;
   
   PlayThread playThread;
   
   private PlayerState playLoopState=PlayerState.STOPPED; //>>>thread safe?
   
   //[ playback methods
   public void play() {
      if(score==null) throw new IllegalStateException();
      if (state == PlayerState.STOPPED) {
         state = PlayerState.PLAYING;

         // [ try to eliminate glitch
         final ShortMessage on = new NoteOnMessage(1, 60, 10);
         OutDeviceManager.instance.send(on, -1);
         Util.sleep(500);
         final ShortMessage off = new NoteOffMessage(1, 60, 10);
         OutDeviceManager.instance.send(off, -1);
         Util.sleep(500);
         // ] try to eliminate glitch

         startPlayLoop();
         
      } else if(state==PlayerState.PAUSED) {
         resumePlayLoop();
      }
      
   }
  
   public void play(Path startPath) {
      if(score==null) throw new IllegalStateException();
      state=PlayerState.PLAYING;
      throw new RuntimeException();  
   }
   
   public void pause() {
      if(score==null) throw new IllegalStateException();
      if(playThread==null) throw new IllegalStateException();
      pausePlayLoop();
      state=PlayerState.PAUSED;
   }
   public void stop() {
      if(score==null) throw new IllegalStateException();
      if(playThread==null) throw new IllegalStateException();
      stopPlayLoop();
      state=PlayerState.STOPPED;
   }
   
   //[ getters & setters
   
   public void setScore(Score s) {
      score=s;
      final int partCount=score.partCount();
      //[ there's only 15 normal channels available, ignore the rest
      partPlayers=new PartPlayer[
             partCount>ChannelManager.NORMAL_CHANNEL_SIZE?
             ChannelManager.NORMAL_CHANNEL_SIZE:
             partCount];
      for(int i=0; i<partPlayers.length; i++) {
         partPlayers[i]=new PartPlayer(
               s.getPart(i), ChannelManager.getNormalChannel(i));
      }
   }
   
   public Score getScore() {
      return score;
   }
   
   public boolean isPlaying() { 
      return state==PlayerState.PLAYING;
   }
   public boolean isStopped() { 
      return state==PlayerState.STOPPED;
   }
   
   public long getTickPosition() {
      throw new RuntimeException();
   }
   public long getMicrosecondPosition() {
      if(playThread==null) throw new RuntimeException();
      return progress;
   } 
   public void setTickPosition(long pos) {
      throw new RuntimeException();
   }
   public long getTickLength() {
      throw new RuntimeException();
   }
   public long getMicrosecondLength() {
      long max=0;
      for (int i = 0; i < partPlayers.length; i++) {
         final long len=partPlayers[i].getMicrosecondLength();
         if(len>max) max=len;
      }
      return max;
   }
   public void setTempoFactor(float tf) {
      throw new RuntimeException();
   }

   static class ChannelManager {
      public static int CHANNEL_SIZE=16;
      public static int NORMAL_CHANNEL_SIZE=CHANNEL_SIZE-1;
      public static int PERCUSSION_CHANNEL=9;
      public static int getNormalChannel(int num) {
         final int res=num>=9?num+1:num;
         if(res>=CHANNEL_SIZE) throw new RuntimeException();
         return res;
      }
      public static int getPercussionChannel() {
         return PERCUSSION_CHANNEL;
      }
   } 
   
   class PartPlayer {
      
      int noteIndex=-1;
      long elapsedTime=0;
      long noteLengthInMillis=0;
      int openNoteCount=0;
      
      Part part;
      final long beatLengthInMillis;
      final long wholeLengthInMillis;
      int channel;
      
      //states: sleeping, started, ended
      public PartPlayer(Part p, int ch) {
         if(p==null || p.getScore()==null) throw new IllegalArgumentException();
         //] >>> if score is absent, use a default value
         part=p;
         if(part.getInstrument().isPercussion()) {
            channel=ChannelManager.PERCUSSION_CHANNEL;
         } else {
            channel=ch;
         }
         beatLengthInMillis=(long)(1000.0f*60/part.getScore().getTempo());
         wholeLengthInMillis=part.getScore().getDenominator()*beatLengthInMillis;
      }
      public long getMicrosecondLength() {
         long sum=0;
         for (int i = 0; i < part.noteCount(); i++) {
            sum+=getNoteLengthInMillis(part.get(i));
         }
         return sum;
      }
      public boolean isDone() { return noteIndex>=part.noteCount(); }
      
      public void play(long increment) {
         elapsedTime+=increment;
         if(noteIndex>=part.noteCount()) {
            throw new RuntimeException();
         }
         if (noteIndex == -1 && part.noteCount()>0) { //: first note
            sendInstrument();
            sendPan();
            sendVolume();
            noteIndex++;
            sendNoteOn();
         } 

         if (elapsedTime >= noteLengthInMillis) {
            if(openNoteCount>0) sendNoteOff();
            noteIndex++;
            
            
            elapsedTime -= noteLengthInMillis;
            if(noteIndex<part.noteCount()) {
               sendNoteOn();
            }
            //System.err.println("err: "+elapsedTime);
         } else {
            //System.err.print(".");
         }
         
      }
      
      public void stop() {
         if(openNoteCount>0) {
            sendNoteOff();
         }
         noteIndex=-1;
         elapsedTime=0;
         noteLengthInMillis=0;
         openNoteCount=0;
      }
      
      public void pause() {
         if(openNoteCount>0) {
            sendNoteOff();
         }
         //if a note is paused while playing, turn off the note,
         //skip to next note after playback is resumed
      }
      
      private void sendVolume() {
         final ShortMessage m = new VolumeMessage(
               channel, part.getVolume());
         OutDeviceManager.instance.send(m, -1);
      }
      private void sendPan() {
         final ShortMessage m = new PanMessage(
               channel, part.getPan());
         OutDeviceManager.instance.send(m, -1);
      }
      private void sendInstrument() {
         final ShortMessage m = new InstrumentMessage(
               channel, part.getInstrument().getValue());
         OutDeviceManager.instance.send(m, -1);
      }
      private void sendNoteOff() {
         //System.err.println("0 "+progress);
         //System.err.print("\\");
         final Note note = part.getNote(noteIndex);
         final ShortMessage off = new NoteOffMessage(channel, note.pitch, 127);
         OutDeviceManager.instance.send(off, -1);
         openNoteCount--;
         //System.err.println("close note("+note.pitch+")");
      }
     
      private void sendNoteOn() {
         //System.err.println("1 "+progress);
         
         final Note note = part.getNote(noteIndex);
         noteLengthInMillis=getNoteLengthInMillis(note);
         if(note.isRest()) return;
         
         if(note.isTieStart()) {
            noteIndex++;
            while(noteIndex<part.noteCount()) {
               final Note n=part.getNote(noteIndex);
               noteLengthInMillis+=getNoteLengthInMillis(n);
               if(n.isTieEnd()) break;
               noteIndex++;
            }
         }
         
         final ShortMessage on = new NoteOnMessage(channel, note.pitch, 127);
         OutDeviceManager.instance.send(on, -1);
         openNoteCount++;
         System.err.print("♪");
         //System.err.println("open note("+note.pitch+") with length="+noteLengthInMillis+"");
      }
      private long getNoteLengthInMillis(Note n) {
         return (long)((float)wholeLengthInMillis * n.getActualLength() / Note.WHOLE_LENGTH);
      }
   }
   
   private void startPlayLoop() {
      playThread = new PlayThread(this);
      playThread.start();      
   }
   
   @Override
   public void run() {
      if(playLoopState!=PlayerState.STOPPED) throw new IllegalStateException();
      playLoopState=PlayerState.PLAYING;
      
      Util.sleep(500); //: eliminate glitch
      System.err.println("start playing...");
      //[ the loop treats notes like multiple lines of customers, and 
      //  exits when all customers are done
      long now=System.currentTimeMillis();
      long interval=0;
      boolean allPlayerDone;
      
      while(true) {
         
         if (playLoopState == PlayerState.PLAYING) {
            allPlayerDone = true;
            for (int i = 0; i < partPlayers.length; i++) {
               if (partPlayers[i].isDone() || score.get(i).isMute())
                  continue;
               allPlayerDone = false;
               partPlayers[i].play(interval);
               // System.err.println(increments);
            }
            if (allPlayerDone)
               break;
            interval = System.currentTimeMillis() - now;
            progress += interval;
            now += interval;
         } else if(playLoopState==PlayerState.STOPPED) {
            for (int i = 0; i < partPlayers.length; i++) {
               partPlayers[i].stop();
            }
            break;
         } else if(playLoopState==PlayerState.PAUSED) {
            for (int i = 0; i < partPlayers.length; i++) {
               partPlayers[i].pause();
            }
         }
         Util.sleep(1);
      }
      //Util.sleep(500);
      System.err.println("stop playing");
   }
    
   
   public void pausePlayLoop() {
      if(playLoopState!=PlayerState.PLAYING) throw new IllegalStateException();
      playLoopState=PlayerState.PAUSED;
   }
   public void resumePlayLoop() {
      if(playLoopState!=PlayerState.PAUSED) throw new IllegalStateException();
      playLoopState=PlayerState.PLAYING;
   }
   
   public void stopPlayLoop() {
      if(playLoopState==PlayerState.STOPPED) throw new IllegalStateException();
      playLoopState=PlayerState.STOPPED;
   }
   
   class PlayThread extends Thread {
      
      public PlayThread(Runnable target) {
         super(target);
         
         final int nearMaxPriority = Thread.NORM_PRIORITY
               + ((Thread.MAX_PRIORITY - Thread.NORM_PRIORITY) * 3) / 4;
         //: the priority is copied from java api

         setPriority(nearMaxPriority);
         setDaemon(false); //>>> should be a daemon in gui
      }
      
   }
   
   
   ////////////////////////////// Test ///////////////////////////////////
   public static void test_multipart() {
      Score score=new Score();
      Part part=new Part();
//      part.setInstrument(Instrument.getInstance(8));
      for (int i = 0; i < 20; i++) {
         part.add(new Note(60, Note.WHOLE_LENGTH/4));
      }
//      part.add(new Note(60, Note.WHOLE_LENGTH/8));
//      part.add(new Note(62, Note.WHOLE_LENGTH/8));
//      part.add(new Note(64, Note.WHOLE_LENGTH/8));
//      part.add(new Note(65, Note.WHOLE_LENGTH/8));
//      part.add(new Note(67, Note.WHOLE_LENGTH/8));
//      part.add(new Note(69, Note.WHOLE_LENGTH/8));
//      part.add(new Note(71, Note.WHOLE_LENGTH/8));
//      part.add(new Note(72, Note.WHOLE_LENGTH/8));
      
      score.add(part);
      
      Part part2=new Part();
//      part2.setInstrument(Instrument.getInstance(16));
      for (int i = 0; i < 10; i++) {
         part2.add(new Note(50, Note.WHOLE_LENGTH/2));
      }
      score.add(part2);
      
      Part part3=new Part();
//      part3.setInstrument(Instrument.getInstance(0));
      for (int i = 0; i < 40; i++) {
         part3.add(new Note(70, Note.WHOLE_LENGTH/8));
      }
      score.add(part3);
      
      RealTimeScorePlayer player=new RealTimeScorePlayer();
      player.setScore(score);
      
      //Util.wait(2000);
      player.play();
      //Util.wait(2000);
   }
   public static void test_tie() {
      Score score=new Score();
      Part part=new Part();
      Note one=new Note(60);
      one.tie=1;
      Note two=new Note(60);
      two.tie=-1;
      Note three=new Note(60);
      three.tie=0;
      part.add(one);
      part.add(two);
      part.add(three);
      part.add(new Note(60));
      score.add(part);
      
      RealTimeScorePlayer player=new RealTimeScorePlayer();
      player.setScore(score);
      player.play();
      //Util.wait(2000);
   }
   public static void test_small_file() {
      Score score=Score.fromFile(new File("/home/ken/Desktop/Willie Nelson - Crazy"));
      //System.err.println(score);
      
      RealTimeScorePlayer player=new RealTimeScorePlayer();
      player.setScore(score);
      
      player.play();
      //Util.wait(2000);
      
   }
   public static void test_instrument() {
      Score score=new Score();
      Part part=new Part();
      part.setInstrument(Instrument.getInstance(128));
//      part.setInstrument(Instrument.getInstance(8));
      for (int i = 0; i < 100; i++) {
         part.add(new Note(38, Note.WHOLE_LENGTH/16));
      }
      score.add(part);
      RealTimeScorePlayer player=new RealTimeScorePlayer();
      player.setScore(score);
      
      player.play();
      //Util.wait(2000);
   }
   public static void test_pan() {
      Score score=new Score();
      Part part=new Part();
      //part.setInstrument(Instrument.getInstance(128));
//      part.setInstrument(Instrument.getInstance(8));
      part.setPan(127);
      for (int i = 0; i < 100; i++) {
         part.add(new Note(38, Note.WHOLE_LENGTH/16));
      }
      score.add(part);
      RealTimeScorePlayer player=new RealTimeScorePlayer();
      player.setScore(score);
      
      player.play();
      //Util.wait(2000);
   }
   public static void test_volume() {
      Score score=new Score();
      Part part=new Part();
      //part.setInstrument(Instrument.getInstance(128));
//      part.setInstrument(Instrument.getInstance(8));
      part.setVolume(100);
      for (int i = 0; i < 100; i++) {
         part.add(new Note(60, Note.WHOLE_LENGTH/16));
      }
      score.add(part);
      RealTimeScorePlayer player=new RealTimeScorePlayer();
      player.setScore(score);
      
      player.play();
      //Util.wait(2000);
   }
   public static void test_pause_stop() {
      Score score=new Score();
      Part part=new Part();
      //part.setInstrument(Instrument.getInstance(128));
//      part.setInstrument(Instrument.getInstance(8));
      part.setVolume(100);
      for (int i = 0; i < 100; i++) {
         part.add(new Note(60, Note.WHOLE_LENGTH/16));
      }
      part.add(new Note(70, Note.WHOLE_LENGTH/16));
      
      score.add(part);
      RealTimeScorePlayer player=new RealTimeScorePlayer();
      player.setScore(score);
      
      player.play();
      Util.sleep(3000);
      player.pause();
      Util.sleep(3000);
      player.play();
   }
   public static void main(String[] args) {
      //test_pause_stop();
      //test_volume();
      //test_pan();
      //test_instrument();
      //test_multipart();
      //test_tie();
      test_small_file();
   }

  

  
}
