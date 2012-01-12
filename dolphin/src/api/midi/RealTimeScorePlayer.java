package api.midi;


import java.io.File;
import java.util.concurrent.ThreadFactory;

import javax.sound.midi.ShortMessage;
import javax.swing.SwingWorker;

import api.model.Instrument;
import api.model.Note;
import api.model.Part;
import api.model.Path;
import api.model.Score;
import api.util.Util;

public class RealTimeScorePlayer {
   
   private Score score;
   
   enum State {
      STOP,
      PLAYING,
      PAUSE
   }
   private State state=State.STOP;
   
   PartPlayer[] partPlayers;
   
   private long progress=0;
   
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
      public boolean isDone() { return noteIndex>=part.noteCount(); }
      
      public void play(long increment) {
         elapsedTime+=increment;
         if(noteIndex>=part.noteCount()) {
            throw new RuntimeException();
         }
         if (noteIndex == -1 && part.noteCount()>0) { //: first note
            setInstrument();
            setPan();
            setVolume();
            noteIndex++;
            openNote();
         } 

         if (elapsedTime >= noteLengthInMillis) {
            if(openNoteCount>0) closeNote();
            noteIndex++;
            
            
            elapsedTime -= noteLengthInMillis;
            if(noteIndex<part.noteCount()) {
               openNote();
            }
            //System.err.println("err: "+elapsedTime);
         } else {
            //System.err.print(".");
         }
         
      }
      private void setVolume() {
         final ShortMessage m = new VolumeMessage(
               channel, part.getVolume());
         OutDeviceManager.instance.send(m, -1);
      }
      private void setPan() {
         final ShortMessage m = new PanMessage(
               channel, part.getPan());
         OutDeviceManager.instance.send(m, -1);
      }
      private void setInstrument() {
         final ShortMessage m = new InstrumentMessage(
               channel, part.getInstrument().getValue());
         OutDeviceManager.instance.send(m, -1);
      }
      private void closeNote() {
         //System.err.println("0 "+progress);
         //System.err.print("\\");
         final Note note = part.getNote(noteIndex);
         final ShortMessage off = new NoteOffMessage(channel, note.pitch, 127);
         OutDeviceManager.instance.send(off, -1);
         openNoteCount--;
         //System.err.println("close note("+note.pitch+")");
      }
      int openNoteCount=0;
      private void openNote() {
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
   
   class PlayThread implements Runnable {

      @Override
      public void run() {
         Util.sleep(500); //: eliminate glitch
         System.err.println("start playing...");
         //[ the loop treats notes like multiple lines of customers, and 
         //  exits when all customers are done
         long now=System.currentTimeMillis();
         long interval=0;
         boolean done;
         
         while(true) {
            done=true;
            for(int i=0; i<partPlayers.length; i++) {
               if(partPlayers[i].isDone() || score.get(i).isMute()) continue;
               done=false;
               partPlayers[i].play(interval);
               //System.err.println(increments);
            }
            if(done) break;
            interval=System.currentTimeMillis()-now;
            progress+=interval;
            now+=interval;
            Util.sleep(1);
         }
         Util.sleep(500);
         System.err.println("stop playing");
      }
      
   }
   
   //[ playback methods
   public void play() {
      if(score==null) throw new IllegalStateException();
      
      state=State.PLAYING;
      
      //[ try to eliminate glitch
      final ShortMessage on = new NoteOnMessage(1, 60, 10);
      OutDeviceManager.instance.send(on, -1);
      Util.sleep(500);
      final ShortMessage off = new NoteOffMessage(1, 60, 10);
      OutDeviceManager.instance.send(off, -1);
      Util.sleep(500);
      //] try to eliminate glitch
      
      final Thread playThread=new Thread(new PlayThread());
      final int nearMaxPriority = Thread.NORM_PRIORITY
             + ((Thread.MAX_PRIORITY - Thread.NORM_PRIORITY) * 3) / 4;
      //: the priority is copied from java api

      playThread.setPriority(nearMaxPriority);
      playThread.setDaemon(false); //>>> should be a daemon in gui
      playThread.start();
      
      
   }
  
   public void play(Path startPath) {
      if(score==null) throw new IllegalStateException();
      state=State.PLAYING;
   }
   
   public void pause() {
      if(score==null) throw new IllegalStateException();
      state=State.PAUSE;
   }
   public void stop() {
      if(score==null) throw new IllegalStateException();
      state=State.STOP;
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
      return state==State.PLAYING;
   }
   public boolean isStopped() { 
      return state==State.STOP;
   }
   
   public long getTickPosition() {
      throw new RuntimeException();
   }
   public long getMicrosecondPosition() {
      return progress;
   } 
   public void setTickPosition(long pos) {
      throw new RuntimeException();
   }
   public long getTickLength() {
      throw new RuntimeException();
   }
 
   public void setTempoFactor(float tf) {
      throw new RuntimeException();
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
   public static void main(String[] args) {
      test_volume();
      //test_pan();
      //test_instrument();
      //test_multipart();
      //test_small_file();
      //test_tie();
   }

  
}
