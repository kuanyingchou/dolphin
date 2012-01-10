package api.model;

import java.io.File;
import java.lang.annotation.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import javax.swing.SwingUtilities;

import api.midi.DumpSequence;
import api.util.BackRef;
import api.util.Pair;
import api.util.Util;

public class Score {
   private static int uid=0;
   private final int id;
   
   final java.util.List<Part> parts=new ArrayList<Part>();
   String title;
   Key keySignature=Key.C;
   int numerator;
   int denominator; //>>> group these two?
   float tempo=120;
   
   public static final int STRONG=100;
   public static final int WEAK=80;
   int[] strengthPattern= {STRONG, WEAK, WEAK};
   
   private java.util.List<ScoreChangeListener> scoreListeners
      =new ArrayList<ScoreChangeListener>();
   //private boolean isNotifying=true; //>>> enable/disable change notify
   private boolean isLogging=false;
   private final UndoManager undoManager=new StackUndoManager(); //new ListUndoManager();
   private ComboChange combo=null;
   private static final ScoreSequenceConverter ssConverter
      =new ScoreSequenceConverter();
   
   //[ ctor
   public Score() {
      id=uid++;
      setTitle("untitled"+id);
      setNumerator(4);
      setDenominator(4);
   }
   
   //[ model
   public int getStrength(int beat) {
      return strengthPattern[beat%strengthPattern.length];
   }
   public void add(Part p) {
      if(p==null) throw new IllegalArgumentException();
      add(p, parts.size());
   }
   public void add(Part p, int index) {
      if(p==null) throw new IllegalArgumentException();
      if(index<0 || index>parts.size()) throw new IllegalArgumentException();
      final AddPartChange ape=new AddPartChange(p, index, this);
      ape.perform();
      if(isLogging) logChange(ape);
      notifyScoreChange(ape);
   }
   public void remove(int index) {
      if(index<0 || index>parts.size()) throw new IllegalArgumentException();
      final RemovePartChange ape=new RemovePartChange(index, this);
      ape.perform();
      if(isLogging) logChange(ape);
      notifyScoreChange(ape);
   }

   public Part get(int index) { 
      if(index<0 || index>=parts.size()) throw new IllegalArgumentException();
      return parts.get(index); 
   }
   public int partCount() { return parts.size(); }
   
   
   public void addPart(Part p) {
      add(p);
   }
   public void addPart(Part p, int index) {
      add(p, index);
   }
   public void remmovePart(int index) {
      remove(index);
   }
   public Part getPart(int index) {
      return get(index);
   }
   public Key getKeySignature() { return keySignature; }
   public void setKeySignature(Key newKey) {
      if(newKey==null) throw new IllegalArgumentException();
      final KeySignatureChange ape=new KeySignatureChange(newKey, this);
      ape.perform();
      if(isLogging) logChange(ape);
      notifyScoreChange(ape);
   }
   
   public void setNumerator(int n) {
      setTimeSignature(n, denominator);  
   }
   public void setDenominator(int d) {
      setTimeSignature(numerator, d);
   }
   public void setTimeSignature(int n, int d) {
      if(n==numerator && d==denominator) return;
      final ScoreChange c=new TimeSignatureChange(n, d, this);
      c.perform();
      if(isLogging) logChange(c);
      notifyScoreChange(c);
   }
   public int getNumerator() {
      return numerator;
   }
   public int getDenominator() {
      return denominator;
   }
   
   public void setTempo(float t) {
      if(this.tempo == t) return;
      final ScoreChange c=new TempoChange(t, this);
      c.perform();
      if(isLogging) logChange(c);
      notifyScoreChange(c);
   }

   public float getTempo() {
      return tempo;
   }
   public void setTitle(String t) {
      if(t.equals(title)) return;
      final ScoreChange c=new TitleChange(t, this);
      c.perform();
      if(isLogging) logChange(c);
      notifyScoreChange(c);
   }

   public String getTitle() {
      return title;
   }
   public String toString() {
      return Util.getProperties(this);
   }
   
   public void submitChange(ScoreChange change) { //: for Part
      if(isLogging()) logChange(change);
      notifyScoreChange(change);
   } 
   
   //[ undo/redo
   public void setLogging(boolean isLogging) {
      this.isLogging = isLogging;
   }
   public boolean isLogging() {
      return isLogging;
   }
   
   public boolean isComboMode() { return combo!=null;}
   //] while in combo mode, all events will log in this combo change
   public void setComboMode(boolean cm) {
      if(cm) {
         //[ entering combo mode
         //comboMode=true;
         if(isComboMode()) throw new IllegalArgumentException("already in combo mode");
         combo=new ComboChange(Score.this);
      } else {
         if(!isComboMode()) return; //: not in combo mode
         //[ exiting combo mode
         //comboMode=false;
         if(combo.isEmpty()) return;
         undoManager.logChange(combo);
         combo=null;   
      }
   }
   public void logChange(ScoreChange e) {
      if(e==null) throw new IllegalArgumentException();
      if(isComboMode()) {
         combo.add(e);
      } else {
         undoManager.logChange(e);
      }
   }
   
   public boolean canUndo() {
      if(isComboMode()) return false;
      return undoManager.canUndo();
   }

   public boolean canRedo() {
      if(isComboMode()) return false;
      return undoManager.canRedo();
   }

   public void undo() {
      final ScoreChange change=undoManager.undo();
      notifyScoreChange(change);
   }

   public void redo() {
      final ScoreChange change=undoManager.redo();
      notifyScoreChange(change);
   }
   
   //[ notify
   public void addScoreChangeListener(ScoreChangeListener lis) {
      scoreListeners.add(lis);
   }
   public boolean containsScoreChangeListener(ScoreChangeListener lis) {
      return scoreListeners.contains(lis);
   }
   public void removeScoreChangeListener(ScoreChangeListener lis) {
      scoreListeners.remove(lis);
   }
   /*private void setNotifying(boolean isNotifying) {
      this.isNotifying = isNotifying;
   }
   private boolean isNotifying() {
      return isNotifying;
   }*/
   private void notifyComboChange(ComboChange combo) { //: process nested combo change
      for(ScoreChange e: combo.events) {
         if(e instanceof ComboChange) {
            notifyComboChange((ComboChange)e);
         } else {
            for(ScoreChangeListener lis: scoreListeners) {
               lis.scoreChanged(e);
            }   
         }
      }   
   }
   public void notifyScoreChange(ScoreChange change) {
      if(change instanceof ComboChange) { //[ separate to small events
         final ComboChange combo=(ComboChange)change;
         notifyComboChange(combo);
      } else {
         for(ScoreChangeListener lis: scoreListeners) {
            lis.scoreChanged(change);
         }
      }
   }
   

   public static Score fromSequence(Sequence sequence) {
      final Score score=ssConverter.fromSequence(sequence);
      score.isLogging=true; //>>> should be here?
      return score;
   }
   public Sequence toSequence() throws InvalidMidiDataException  {
      return ssConverter.toSequence(this);
   }
   
   
   
   public boolean isValidSelectPath(Path path) {
      if(path.partIndex<0 || path.partIndex>=this.partCount()) return false;
      final Part part=this.get(path.partIndex);
      if(path.index<0 || path.index>=part.noteCount()) return false;
      /*final int start=getSelectionStartIndex();
      if(start<0 || start>=part.size()) return false;
      final int last=start+getAbsSelectionLength();
      if(last<0 || last>=part.size()) return false;*/
      return true;
   }
   public boolean isValidInsertPath(Path path) {
      if(path.partIndex<0 || path.partIndex>=this.partCount()) return false;
      final Part part=this.get(path.partIndex);
      if(path.index<0 || path.index>part.noteCount()) return false;
      /*final int start=getSelectionStartIndex();
      if(start<0 || start>part.size()) return false;
      final int last=start+getAbsSelectionLength();
      if(last<0 || last>part.size()) return false;*/
      return true;
   }
   
   
   //=====================================================================================
   public static void main(String[] args) {
      final Score score=new Score();
      final Part part=new Part();
      final Note note=new Note(60, 32);
      //part.add(note);
      score.add(part);
      score.setLogging(true);
      score.addScoreChangeListener(new ScoreChangeListener() {
         public void scoreChanged(ScoreChange e) {
            System.out.println(e.getScore());
         }
      });
      part.add(new Note(60, 32));
      part.add(new Note(80, 32));
      score.undo();
      part.add(new Note(90, 32));
      

      /*final Part anotherPart=new Part();
      score.add(anotherPart);
      System.out.print(score);
      score.undo();
      score.redo();
      System.out.print(score);*/
   }

   

   
}