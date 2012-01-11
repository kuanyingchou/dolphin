package api.model;

import java.util.ArrayList;
import java.util.List;

import api.util.BackRef;
import api.util.Util;



public class Part {
   final java.util.List<Note> notes=new ArrayList<Note>();
   @BackRef private Score score; //nullable???
   Instrument instrument=Instrument.getInstance(0);
   int volume=100;
   boolean mute=false;
   int pan=64;
   public int playIndex=0; //>>> move to view
   public int channelBinding=-1; //>>> restrict accessibility
   
   public Part() {}
   
   public void add(Note n) {
      add(n, notes.size());
   }
   public void add(Note n, int index) { 
      final AddNoteChange event=new AddNoteChange(n, this, index, score);
      event.perform();
      submitChange(event);
   }
   private void submitChange(ScoreChange change) {
      if(score==null) return;
      score.submitChange(change);
   } 
   public void add(Note[] notes, int index) { 
      //final java.util.List<ScoreChange> events=new ArrayList<ScoreChange>();
      final ComboChange event=new ComboChange(score);
      for(int i=notes.length-1; i>=0; i--) {
         final Note n=notes[i];
         final AddNoteChange e=new AddNoteChange(n, this, index, score);
         event.add(e);
      }
//      for(int i=0; i<notes.length; i++) {
//         final Note n=notes[i];
//         final AddNoteEvent e=new AddNoteEvent(n, this, index++, score);
//         events.add(e);
//      }
      event.perform();
      submitChange(event);
   }
   public void remove(int i) { 
      final RemoveNoteChange event=new RemoveNoteChange(i, this, score);
      event.perform();
      submitChange(event);
   }
   
   public void remove(int index, int length) {
      if(length<0) throw new IllegalArgumentException();
      //final java.util.List<ScoreChange> events=new ArrayList<ScoreChange>();
      final ComboChange event=new ComboChange(score);
      final int right=index+length;
      for(int i=right-1; i>=index; i--) {
         final RemoveNoteChange e=new RemoveNoteChange(i, this, score);
         event.add(e);
      }
      event.perform();
      submitChange(event);
   }
   
   public int noteCount() { return notes.size(); }
   public Note get(int i) { return notes.get(i); }
   
   void setScore(Score s) {
      score=s;
   }
   public Score getScore() { return score; }
   
   public String toString() {
      return Util.getObjectInfo(this);
   }

   public void setInstrument(Instrument instrument) {
      if(this.instrument==instrument) return;
      final PartChange c=new InstrumentChange(instrument, this, score);
      c.perform();
      submitChange(c);
   }

   public Instrument getInstrument() {
      return instrument;
   }

   public void setVolume(int volume) {
      if(this.volume==volume) return;
      final PartChange c=new VolumeChange(volume, this, score);
      c.perform();
      submitChange(c);
   }

   public int getVolume() {
      return volume;
   }

   public void setPan(int pan) {
      if(this.pan==pan) return;
      final PartChange c=new PanChange(pan, this, score);
      c.perform();
      submitChange(c);
   }
   public void setMute(boolean m) {
      if(this.mute==m) return;
      final PartChange c=new MuteChange(this, score);
      c.perform();
      submitChange(c);
   }
   public boolean isMute() { return mute; }
   public int getPan() {
      return pan;
   }
   
}