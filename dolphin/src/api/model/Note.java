package api.model;

import javax.sound.midi.MidiMessage;

import api.util.Util;


public class Note {
   public static final int MAX_DOT=1;
   public static final int WHOLE_LENGTH=128*2*2*2*3; //: (int)(128*Math.pow(2, MAX_DOT));
   //] *2*2*2 is for dots, *3 is for triplets
   
   public int pitch; //0~127, -1: rest
   public int length; //relative to WHOLE_LENGTH
   public int dot;
   
   public Note chord=null;
   public int delay=0;
   
   //[ length modifiers
   public boolean isTripletElement=false;
   public int tie; //0: no tie, 1: tie start, -1: tie end >>> what about in tie?
   
   public Note() {
      this(60, Note.WHOLE_LENGTH/4, 0);
   }
   public Note(int p) {
      this(p, Note.WHOLE_LENGTH/4, 0);
   }
   public Note(int p, int l) {
      this(p, l, 0);
   }
   public Note(int p, int l, int d) {
      this(p, l, d, 0, false);
   }
   public Note(int p, int l, int d, int t, boolean triplet) {
      pitch=p;
      length=l;
      dot=d;
      tie=t;
      isTripletElement=triplet;
   }
   public Note(Note that) {
      this.pitch=that.pitch;
      this.length=that.length;
      this.dot=that.dot;
      this.tie=tie;
      this.isTripletElement=that.isTripletElement;
   }
   
   public boolean isTieStart() { return tie>0; }
   public boolean isTieEnd() { return tie<0; }
   
   public int getActualLength() {
      int res=length;
      
      if(isTripletElement) {
         res=res*2/3;
      } else {
         int addon=length;
         for(int i=0; i<dot; i++) {
            addon/=2;
            res+=addon;
         }   
      }
      return res;
   }
   
   //[ for play progress
   public volatile MidiMessage binding=null; 
   private volatile long tick=-1;
   public long getTick() {
      return tick;
   }
   void setTick(long t) {
      tick=t;
   }
   
   public String toString() {
      return Util.getProperties(this);
   }
   public boolean isRest() {
      return pitch<0 || pitch>=128;
   }
}
