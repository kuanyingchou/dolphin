package api.midi;


import java.io.File;
import java.util.concurrent.ThreadFactory;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.swing.SwingWorker;

import api.model.Instrument;
import api.model.Note;
import api.model.Part;
import api.model.Path;
import api.model.Score;
import api.util.Util;
// Design Goals
// * A score player can play a Score, pause or stop the playback, and publish playing progress
//   to the clients.
// * A score player can only play one score at any time
// * A score player is a high-level class which can be used directly 
//   by the user of the Dolphin API. Thus it ignores illegal operations
//   instead of throwing exceptions.

public class RealTimeScorePlayer implements Runnable, ScorePlayer {
   
   private Score score;
   
   enum PlayerState {
      STOPPED,
      PLAYING,
      PAUSED
   }
   private PlayerState state=PlayerState.STOPPED;
   
   PartPlayer[] partPlayers;
   
   private long progress=0;
   
   PlayThread playThread; //there is at most one playThread per player at any time
   
   private PlayerState playLoopState=PlayerState.STOPPED; //>>>thread safe?
   // There are two threads involved in this class, one is 
   // the Event-Dispatching Thread or the main Thread, the other is
   // PlayThread. 
    
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
         if(playLoopState!=PlayerState.PAUSED) throw new IllegalStateException();
         now=System.currentTimeMillis();
         playLoopState=PlayerState.PLAYING;
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
      if(playLoopState!=PlayerState.PLAYING) throw new IllegalStateException();
      playLoopState=PlayerState.PAUSED;
      state=PlayerState.PAUSED;
   }
   public void stop() {
      if(score==null) throw new IllegalStateException();
      //if(playThread==null) throw new IllegalStateException(); 
      //] users of this class is not required to remember the state of the player
      if(playLoopState==PlayerState.STOPPED) throw new IllegalStateException();
      playLoopState=PlayerState.STOPPED;
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
   public void setMicrosecondPosition(long pos) {
      progress=pos;
      for (int i = 0; i < partPlayers.length; i++) {
         partPlayers[i].setMicrosecondPosition(pos);
      }
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
   float tempoFactor=1.0f;
   public void setTempoFactor(float tf) {
      if(tf<0.1) tf=0.1f;
      if(tf>10) tf=10;
      tempoFactor=tf;
   }

   private void startPlayLoop() {
      if(playThread!=null) throw new RuntimeException();
      playThread = new PlayThread(this);
      playThread.start();      
   }

   long now;

   @Override
   public void run() {
      if(playLoopState!=PlayerState.STOPPED) throw new IllegalStateException();
      playLoopState=PlayerState.PLAYING;
      
      Util.sleep(500); //: eliminate glitch
      System.err.println("start playing...");
      //[ the loop treats notes like multiple lines of customers, and 
      //  exits when all customers are done
      now=System.currentTimeMillis();
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
               //System.err.println(interval);
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
      OutDeviceManager.instance.closeOutDevice(); //>>>>>>>
      System.err.println("stop playing");
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
      int currentNoteIndex=-1;
      long currentNoteLengthInMillis=0;
      long currentNoteProgress=0;
      boolean[] keys=new boolean[128];
      
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
      public void setMicrosecondPosition(long pos) {
         if(pos<0) throw new IllegalArgumentException();
         long sum=0;
         for (int i = 0; i < part.noteCount(); i++) {
            if(sum>=pos) {
               currentNoteIndex=i;
               currentNoteProgress=sum-pos;
               currentNoteLengthInMillis=getNoteLengthInMillis(part.get(i));
               return; //found it!
            }
            sum+=getNoteLengthInMillis(part.get(i));
         }
         if(sum>=pos) {
            //[ pos is longer than or equal to the part length
            currentNoteIndex=part.noteCount(); //>>> leave the member in illegal state
         }
         //throw new IllegalArgumentException("pos is longer than part length"); 
      }
      public long getMicrosecondLength() {
         long sum=0;
         for (int i = 0; i < part.noteCount(); i++) {
            sum+=getNoteLengthInMillis(part.get(i));
         }
         return sum;
      }
      public boolean isDone() { return currentNoteIndex>=part.noteCount(); }
      
      private void closeUnclosedKeys() {
         for (int i = 0; i < keys.length; i++) {
            if(keys[i]) {
               sendNoteOff(i);
            }
         }
         System.err.println("close unclosed keys");
      }
      
      //usage: isDone() should be checked before calling play()
      //example:
      //|___A___|___B___| ... the part contains notes A and B
      //|=>|              ... t1: start, play note A
      //|=========>|      ... t2: elapsedTime >= noteLength, stop note A
      //        |=>|      ... t2(continued): elapsedTime -= noteLength, play note B 
      //        |=======>|... t3: end, stop note B
      //>>> TODO: what if interval is greater than the length of a note
      public void play(long interval) {
         if(part.noteCount()<=0) { return; } //>>> need to consider progress, ex. getRemainNoteCount()<=0
         if(isDone()) { throw new IllegalStateException(); }
         
         currentNoteProgress+=interval;
         
         if (currentNoteIndex == -1) { //: start
            //[ prepare channel
            sendInstrument();
            sendPan();
            sendVolume();
            //] prepare channel
         } 

         if (currentNoteProgress >= currentNoteLengthInMillis) {
            if(currentNoteIndex>=0) {
               if(!getCurrentNote().isRest())
                  sendNoteOff(getCurrentNote().pitch);
            }
            currentNoteProgress -= currentNoteLengthInMillis;

            currentNoteIndex++;
            if(currentNoteIndex<part.noteCount()) {
               //[ get note length
               final Note note = part.getNote(currentNoteIndex);
               currentNoteLengthInMillis=getNoteLengthInMillis(note);
               
               //[ handle tie
               if(note.isTieStart()) {
                  currentNoteIndex++;
                  while(currentNoteIndex<part.noteCount()) {
                     final Note n=part.getNote(currentNoteIndex);
                     currentNoteLengthInMillis+=getNoteLengthInMillis(n);
                     if(n.isTieEnd()) break;
                     currentNoteIndex++;
                  }
               }
               //] handle tie
               //] get note length
               
               if(!note.isRest()) {
                  sendNoteOn(note.pitch);
               }
            } else {
               //end of part, no more notes to play
               closeUnclosedKeys();
            }
         } else {
            // in the middle of a note, nothing to do here >>>
            if (!getCurrentNote().isRest()) {
               if (keys[getCurrentNote().pitch] != true) {
                  sendNoteOn(getCurrentNote().pitch);
               }
            }
            
         }
         
      }
      
      public void stop() {
         closeUnclosedKeys();
         currentNoteIndex=-1;
         currentNoteProgress=0;
         currentNoteLengthInMillis=0;
      }
      
      public void pause() {
         closeUnclosedKeys();
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
      private Note getCurrentNote() {
         return part.getNote(currentNoteIndex);
      }
      
      private void sendNoteOn(int pitch) {
         //System.err.println("1 "+progress);
         
         final ShortMessage on = new NoteOnMessage(channel, pitch, 127);
         OutDeviceManager.instance.send(on, -1);
         keys[pitch]=true;
         System.err.println("♪(start="+progress+")");
         //System.err.println("open note("+note.pitch+") with length="+noteLengthInMillis+"");
      }
      private void sendNoteOff(int pitch) {
         //System.err.println("0 "+progress);
         //System.err.print("\\");
         final ShortMessage off = new NoteOffMessage(channel, pitch, 127);
         OutDeviceManager.instance.send(off, -1);
         keys[pitch]=false;
         //System.err.println("close note("+note.pitch+")");
      }
     
      private long getNoteLengthInMillis(Note n) {
         return (long)((float)(1/tempoFactor)*wholeLengthInMillis * n.getActualLength() / Note.WHOLE_LENGTH);
      }
   }
   @Override
   public void play(Score s) {
      setScore(s);
      play();
   }

   @Override
   public void play(Score s, Path startPath) {
      setScore(s);
      //>>>startPath
      play();
   }

   @Override
   public void addReceiver(Receiver rec) {
      // TODO Auto-generated method stub
      throw new RuntimeException("not yet");
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
   
   
   ////////////////////////////// Tests ///////////////////////////////////
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
      for (int i = 0; i < 50; i++) {
         part.add(new Note(60, Note.WHOLE_LENGTH/16));
      }
      //part.add(new Note(70, Note.WHOLE_LENGTH/16));
      
      score.add(part);
      RealTimeScorePlayer player=new RealTimeScorePlayer();
      player.setScore(score);
      
      player.play();
      Util.sleep(3000);
      player.pause();
      System.err.print("pause");
      Util.sleep(3000);
      player.play();
   }
   public static void test_scale() {
      Score score=new Score();
      Part part=new Part();
      
      part.add(new Note(60, Note.WHOLE_LENGTH/4));
      part.add(new Note(62, Note.WHOLE_LENGTH/4));
      part.add(new Note(64, Note.WHOLE_LENGTH/4));
      part.add(new Note(65, Note.WHOLE_LENGTH/4));
      part.add(new Note(67, Note.WHOLE_LENGTH/4));
      part.add(new Note(69, Note.WHOLE_LENGTH/4));
      part.add(new Note(71, Note.WHOLE_LENGTH/4));
      part.add(new Note(72, Note.WHOLE_LENGTH/4));
      
      score.add(part);
      RealTimeScorePlayer player=new RealTimeScorePlayer();
      player.setScore(score);
      
      player.play();
   }
   public static void test_set_position() {
      Score score=new Score();
      Part part=new Part();
      
      part.add(new Note(60, Note.WHOLE_LENGTH));
      part.add(new Note(62, Note.WHOLE_LENGTH));
      part.add(new Note(64, Note.WHOLE_LENGTH));
      
      score.add(part);

      RealTimeScorePlayer player=new RealTimeScorePlayer();
      player.setScore(score);
      player.setMicrosecondPosition(0);
      //player.setMicrosecondPosition(1000);
      //player.setMicrosecondPosition(2000);
      //player.setMicrosecondPosition(3000);
      player.setMicrosecondPosition(6000);      
      player.play();
      //Util.sleep(2000);
      
   }
   public static void test_tempo_factor() {
      Score score=new Score();
      Part part=new Part();
      part.add(new Note(60, Note.WHOLE_LENGTH));
      part.add(new Note(62, Note.WHOLE_LENGTH));
      part.add(new Note(64, Note.WHOLE_LENGTH));
      score.add(part);
      
      RealTimeScorePlayer player=new RealTimeScorePlayer();
      player.setScore(score);
      player.setTempoFactor(2.0f);
      player.play();
      
   }
   public static void test_another_file() {
      Score score=Score.fromFile(new File("/Users/ken/Downloads/Willie_Nelson_-_Crazy_AABA.mid"));
      //System.err.println(score);
      
      RealTimeScorePlayer player=new RealTimeScorePlayer();
      player.setScore(score);
      player.setTempoFactor(0.8f);
      player.setMicrosecondPosition(20000);
      player.play();     
   }
   public static void main(String[] args) {
      //test_pause_stop();
      //test_volume();
      //test_pan();
      //test_instrument();
      //test_multipart();
      //test_tie();
      //test_small_file();
      //test_scale();
      //test_set_position();
      //test_tempo_factor();
      test_another_file();
   }

   
   
  

   
}