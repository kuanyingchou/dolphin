package api.midi;


import javax.sound.midi.ShortMessage;

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
   
   private int resolution=480;
   private int tempo=120;
   
   class Track {
      
      int noteIndex=-1;
      long elapsedTime=0;
      long noteLengthInMillis=0;
      
      Part part;
      final long beatLengthInMillis;
      final long wholeLengthInMillis;
      
      //states: sleeping, started, ended
      public Track(Part p) {
         if(p==null || p.getScore()==null) throw new IllegalArgumentException();
         //] >>> if score is absent, use a default value
         part=p;
         beatLengthInMillis=(long)(1000.0f*60/part.getScore().getTempo());
         wholeLengthInMillis=part.getScore().getDenominator()*beatLengthInMillis;
      }
      public boolean isDone() { return noteIndex>=part.noteCount(); }
      
      public void process(long increment) {
         elapsedTime+=increment;
         if(noteIndex>=part.noteCount()) {
            throw new RuntimeException();
         }
         if (noteIndex == -1) { //: first note
            noteIndex++;
            if(noteIndex<part.noteCount()) {
               openNote();
               //System.err.print("/");
            }
         } 

         if (elapsedTime >= noteLengthInMillis) {
            closeNote();
            //System.err.print("\\");
            noteIndex++;
            
            if(noteIndex<part.noteCount()) {
               openNote();
               //System.err.print("/");
            }
            elapsedTime -= noteLengthInMillis;
            //System.err.println("err: "+elapsedTime);
         } else {
            //System.err.print("/");
         }
         
      }
      private void closeNote() {
         //System.err.println("0 "+progress);
         final Note note = part.getNote(noteIndex);
         final ShortMessage off = new NoteOffMessage(1, note.pitch, 127);
         OutDeviceManager.instance.send(off, 0);
         //System.err.println("close note("+note.pitch+")");
      }
      private void openNote() {
         //System.err.println("1 "+progress);
         final Note note = part.getNote(noteIndex);
         noteLengthInMillis=getNoteLengthInMillis(note);
         final ShortMessage on = new NoteOnMessage(1, note.pitch, 127);
         OutDeviceManager.instance.send(on, 0);
         //System.err.println("open note("+note.pitch+") with length="+noteLengthInMillis);
      }
      private long getNoteLengthInMillis(Note n) {
         return wholeLengthInMillis * n.length / Note.WHOLE_LENGTH;
      }
   }
   Track[] tracks;
   
   private long progress=0;
   
   //[ playback methods
   public void play() {
      if(score==null) throw new IllegalStateException();
      
      System.err.println("start playing...");
      
      //[ the loop treats notes like multiple lines of customers, and 
      //  exits when all customers are done
      final ShortMessage on = new NoteOnMessage(1, 60, 0);
      OutDeviceManager.instance.send(on, 0);
      Util.wait(1000);
      final ShortMessage off = new NoteOffMessage(1, 60, 0);
      OutDeviceManager.instance.send(off, 0);
      Util.wait(1000);
      
      long last=System.currentTimeMillis();
      long increments=0;
      boolean done;
      
      while(true) {
         done=true;
         for(int i=0; i<tracks.length; i++) {
            if(tracks[i].isDone()) continue;
            done=false;
            tracks[i].process(increments);
            //System.err.println(increments);
         }
         if(done) break;
         long now=System.currentTimeMillis();
         increments=now-last;
         progress+=increments;
         last=now;
         Util.wait(1);
      }
      /*
      for(int p=0; p<score.partCount(); p++) {
         final Part part=score.getPart(p); 
         for(int n=0; n<part.noteCount(); n++) {
            final Note note=part.get(n);
            final ShortMessage on=new NoteOnMessage(1, note.pitch, 127);
            final ShortMessage off=new NoteOffMessage(1, note.pitch, 127);
            
            OutDeviceManager.instance.send(on, 0);
            Util.wait(1000);
            OutDeviceManager.instance.send(off, 0);
         }
      } 
      */
      
      
      
      /*
      new Thread() {
         public void run() {
            
         }
      };
      */
      state=State.PLAYING;
      System.err.println("stop playing");
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
      tracks=new Track[score.partCount()];
      for(int i=0; i<tracks.length; i++) {
         tracks[i]=new Track(s.getPart(i));
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

   public static void main(String[] args) {
      Score score=new Score();
      Part part=new Part();
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
      for (int i = 0; i < 10; i++) {
         part2.add(new Note(50, Note.WHOLE_LENGTH/2));
      }
      score.add(part2);
      
      Part part3=new Part();
      for (int i = 0; i < 40; i++) {
         part3.add(new Note(70, Note.WHOLE_LENGTH/8));
      }
      score.add(part3);
      
      RealTimeScorePlayer player=new RealTimeScorePlayer();
      player.setScore(score);
      
      //Util.wait(2000);
      player.play();
      Util.wait(2000);
   }
  
}
