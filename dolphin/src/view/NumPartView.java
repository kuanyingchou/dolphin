package view;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import api.model.Key;
import api.model.Note;
import api.model.Part;



public class NumPartView extends PartView {
   //int cellWidth=20;
   private final int fontSize=32;
   private final int fontHeight=32;
   private final int fontWidth=32;
   private final int fontBase=64;
   
   private Font textFont=new Font(Font.MONOSPACED, Font.PLAIN, 12);
   
   public NumPartView(ScoreView sheet, Part part) {
      super(sheet, part);
   }
   
   
   //private final static int absKey=60; //part.getScore().getKey().getName();
   public final static int[] numPitchIndex= {0, 2, 4, 5, 7, 9, 11}; 
   private int[] getPitchOctaveAccidental(int pitch) {
      if(pitch<0||pitch>=128) return new int[] {0, 0, 0};
      final int absKey=this.scoreView.score.getKeySignature().getPitch(4);
      int ap=pitch-absKey;
      if(ap<0) {
         ap+=(-ap/12+1)*12;
      }
      final int mod=(ap)%12;
      int n=-1;
      int acci=0;
      for(int i=numPitchIndex.length-1; i >= 0 ; i--) {
         if(numPitchIndex[i]==mod) {
            n=i+1;
            break;
         } else if(numPitchIndex[i]<mod) {
            acci=1; //>>> use flat or sharp? 
            n=i+1;
            break;
         }
      }
      if(n<0) throw new RuntimeException();
      int o=0;
      if(pitch<absKey) {
         //>>> check this 
         o=-((absKey-pitch)/12+(((absKey-pitch)%12==0)?0:1)); //-((absKey-pitch)/12+((absKey-pitch)/12>0?0:1));
         //System.err.printf("p:%d, k:%d, o:%d - %d %n", pitch, absKey, (absKey-pitch)/12, o);
      } else {
         o=(pitch-absKey)/12;
      }
      //System.err.println(+n+", "+o+", "+acci);
      return new int[] { n, o, acci};
   }
   
   private int getDashCount(Note note) {
      final int ld=note.getActualLength();
      final int dash=ld/(Note.WHOLE_LENGTH/4)-1;
      if(dash<0) return 0;
      else return dash;
   }
   private int getUnderScoreCount(int index) {
      final Note note=part.get(index);
      int underScore=0;
      int len=note.length;
      if(len==0) throw new RuntimeException();
      while(len<Note.WHOLE_LENGTH/4) {
         len*=2;
         underScore++;
      }
      return underScore;
   }
   private int getLengthDotCount(Note note) {
      final int dash=getDashCount(note);
      int rest=0;
      if(dash>0) {
         rest=note.getActualLength()-(Note.WHOLE_LENGTH/4)*(dash+1);
      } else {
         return note.dot;
      }
      if(rest==0) return 0;
      int dot=0;
      int dotLen=note.length;
      while(rest>0) {
         if(dotLen<=rest) {
            rest-=dotLen;
            dot++;
         } else {
            dotLen/=2;
            if(dotLen<=0) throw new RuntimeException();
         }
      }
      if(rest!=0) throw new RuntimeException(""+rest);
      return dot;
   }
   
   @Override
   public void adjust(Graphics2D g) {
      float width=0;
      for(int noteIndex=0; noteIndex<part.noteCount(); noteIndex++) {
         width+=getNoteWidth(part.get(noteIndex));
      }
      setSize((int)width, (int)(150*scoreView.zoom));
      
      //setSize((int)((getNoteWidth(new Note(0, Note.WHOLE_LENGTH))+getNoteGap())*getPart().size()+20), 
      //      (int)(150*scoreView.zoom)); //>>>
      textFont=textFont.deriveFont(fontSize*scoreView.zoom);
   }

   @Override
   public void drawNote(Graphics2D g, int noteIndex, float currentX) {
      final Note note=getPart().get(noteIndex);
      float verticalInterval=10*scoreView.zoom;      
      g.setColor(Color.black);
      g.setFont(textFont);
      float ny=fontBase*scoreView.zoom;
      String noteSym=null;
      /*if(note.pitch<0 || note.pitch>=128) { //rest
         noteSym=String.valueOf(0);
      } else {*/
         final int[] poa=getPitchOctaveAccidental(note.pitch);
         noteSym=String.valueOf(poa[0]);
         //noteSym+=":"+String.valueOf(poa[1]);
         
         //[ acci
         if(poa[2]>0) { 
            noteSym="s"+noteSym;
         } else if(poa[2]<0) {
            noteSym="b"+noteSym;
         }
         
         
         
      //}
      //[ dash
      final int dash=getDashCount(note);
      for(int i=0; i<dash; i++) {
         noteSym+="-";
      }
      
      //[ lenDot
      final int lenDot=getLengthDotCount(note);
      for(int i=0; i<lenDot; i++) {
         noteSym+=".";
      }
      
      //[ underScore
      final int underScore=getUnderScoreCount(noteIndex);
      float bottom=ny+verticalInterval;
      for(int i=0; i<underScore; i++) {
         bottom+=verticalInterval;
         g.drawString("-", currentX, bottom);
      }
      g.drawString(noteSym, currentX, ny);
      
      //[ octave
      if(poa[1]>0) {
         for(int i=0; i<poa[1]; i++) {
            g.drawString(".", currentX, ny-fontHeight*scoreView.zoom-(i+1)*verticalInterval);
         }
      } else if(poa[1]<0) {
         for(int i=0; i< -poa[1]; i++) {
            bottom+=verticalInterval;
            g.drawString(".", currentX, bottom);
         }
      }
      
      if(DEBUG) {
         g.setColor(Color.red);
         g.drawLine(x(), (int)ny, x()+w(), (int)ny);
      }
      
      
   }

   @Override
   public float getNoteGap() {
      return 0;
   }

//   @Override
//   public float getNoteWidth(Note n) {
//      //final int beat=Note.WHOLE_LENGTH/part.getScore().denominator;
//      //final int numCell=n.getLengthWithDots()/beat+(n.getLengthWithDots()%beat==0?0:1);
//      //return numCell*cellWidth;
//      return fontWidth*(getDashCount(n)+1/*sym*/+1/*dots*/)*scoreView.zoom;
//   }

   @Override
   public float drawHeader(Graphics2D g) {
      return 0;
   }

   public int getBarHeight() {
      return h();
   }

   public int getBarY() {
      return 0;
   }
   
   @Override
   public void drawBarLine(Graphics2D g, float currentX, int barNumber) {
      final int lineStart=getBarY();//getYByLine(0);
      final int lineEnd=getBarHeight(); //getYByLine(4);
      final int x=(int)(currentX);
      g.setColor(Color.black);
      g.drawLine(x, lineStart, x, lineStart+lineEnd);
      g.setFont(scoreView.getFont()); //>>> use a better font
      g.drawString(String.valueOf(barNumber), x, lineStart-12*scoreView.zoom);
   }
   
   public static void main(String[] args) {
      for(int i=0; i<128; i++) {
         System.err.print(i+" - ");
         //getPitchOctaveAccidental(i);
      }
   }

}
