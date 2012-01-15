package view;

import gui.MainFrame;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import api.midi.BasicScorePlayer;
import api.model.Note;
import api.model.Part;
import api.util.Util;




public abstract class PartView extends AbstractCpn {
   public static final boolean DEBUG=false;
   public static String[] keyNames= {"C", "C#/Db", "D", "D#/Eb", "E", "F", "F#/Gb", "G", "G#/Ab", "A", "A#/Bb", "B"};
   ScoreView scoreView; //>>> dangerous
   final Part part;
   //int partIndex;
   public BasicStroke lineStroke=new BasicStroke(1.0f);
   public BasicStroke bordLineStroke=new BasicStroke(3.0f);
   public static Color selectionColor=new Color(232, 242, 254);
      //Util.getContrastColor(new Color(232, 242, 254)); //Color.green;
   
   //public GeneralStaff(Sheet s, int index) { //bug; index may change
   public PartView(ScoreView s, Part p) {
      scoreView=s;
      this.part=p;
   }
   public Part getPart() {
      //return sheet.score.get(partIndex);
      return part;
   }
   
   int HEAD_END_MARK=0;
   
   Font oldFont;
   Stroke oldStroke;
   
   @Override
   public void draw(Graphics2D  g) {
      g.translate(x(), y());
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
      oldFont=g.getFont();
      oldStroke=g.getStroke();
      
      /*
       * g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
       * RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
       */
      if(scoreView.indexOf(this)==scoreView.staticCursor.partIndex) {
         g.setColor(Color.lightGray);
         final Stroke oldStroke=g.getStroke();
         g.setStroke(bordLineStroke);
         g.drawRoundRect(0, 0, w(), h(), 10, 10);
         g.setStroke(oldStroke);
      }

      // if(clefFont==null) updateMusicFonts();

      // System.err.println(musicFont.getName());
      float currentX=drawHeader(g);
      
      
      
      // [ draw notes ================================================================================================
      // final int noteH=(int)(getPositionSize()*1.8);
      HEAD_END_MARK=(int)currentX;
//g.drawLine(HEAD_END_MARK, 0, HEAD_END_MARK, h());      
      g.setStroke(lineStroke);
      
      final int periodLength=(Note.WHOLE_LENGTH/getPart().getScore().getDenominator())*getPart().getScore().getNumerator();
      int time=0;
      int periodCount=1; //>>>1 base?
      
      final Rectangle visibleRect=scoreView.getVisibleRect();
      int selectionStart=0;
      int selectionEnd=0;
      
      for(int noteIndex=0; noteIndex < getPart().noteCount(); noteIndex++) {
         final Note note=getPart().get(noteIndex);
         //final Rectangle noteRect=new Rectangle(0, 0, , 0); //getStrRect(g.getFontMetrics(sonataFont), noteSym,(int) currentX, noteY);
         final int noteWidth=(int)getNoteWidth(note);
         time+=note.getActualLength();
         
         if(currentX>=visibleRect.x-x()-noteWidth && 
               currentX<visibleRect.x+visibleRect.width-x()) { //>>> left selection disappear
            
            drawNote(g, noteIndex, currentX);
            
            //[ draw bar-line
            if(time>=periodLength) {
               drawBarLine(g, currentX+noteWidth+getNoteGap()/2, periodCount);
            }
            
            //[ draw static cursor
            if(scoreView.indexOf(this) == scoreView.staticCursor.partIndex &&
                  noteIndex==scoreView.staticCursor.noteIndex) {
               //g.setXORMode(Color.green);
               g.setColor(Color.blue);
               g.drawLine((int)currentX, 0, (int)currentX, h());
               //g.setPaintMode();
            } 
            
            //[ draw play index
            if(!BasicScorePlayer.getInstance().isStopped() &&
                  scoreView.indexOf(this) == scoreView.staticCursor.partIndex &&
                  noteIndex==getPart().playIndex) {
               //final int y0=getYByLine(0);
               //final int y1=getYByLine(4);
               g.setColor(Color.red);
               g.drawLine((int)currentX, 0, (int)currentX, h());
            }
            
         }
         if(time>=periodLength) {
            periodCount++;
            time=0;
         }
         
         //[ find selection
         if(scoreView.indexOf(this) == scoreView.staticCursor.partIndex && 
               scoreView.getAbsSelectionLength()>0) {
            final int start=scoreView.getSelectionStartIndex();
            if(noteIndex==start) {
               selectionStart=(int)currentX;
            } 
            if(noteIndex==start+scoreView.getAbsSelectionLength()-1) {
               selectionEnd=(int)(currentX+noteWidth+getNoteGap());
            }
         }
         
         currentX+=noteWidth+getNoteGap(); //getPositionSize()*8; //noteW * 3;
      }
      
      //[ draw static cursor at end
      if(scoreView.indexOf(this) == scoreView.staticCursor.partIndex &&
            getPart().noteCount() == scoreView.staticCursor.noteIndex) {
         //g.setXORMode(Color.blue);
         g.setColor(Color.blue);
         g.drawLine((int)currentX, 0, (int)currentX, h());
         //g.setPaintMode();
      } 
      
      //[ draw selection
      if(scoreView.indexOf(this) == scoreView.staticCursor.partIndex &&
            scoreView.getAbsSelectionLength()>0) {
         g.setColor(Color.black);
         g.setXORMode(selectionColor);
         //g.setColor(selectionColor);
         g.fillRect(selectionStart, 0, selectionEnd-selectionStart, h());
         g.setPaintMode();
      }
      
      g.setStroke(oldStroke);
      g.setFont(oldFont);
      g.translate(-x(), -y());
   }
   public abstract void drawNote(Graphics2D g, int index, float currentX);
   public abstract float getNoteGap();
   public float getNoteWidth(Note n) {
      final float min=scoreView.zoom*32;
      if(n==null) return min; //>>>
      if(n.length<=Note.WHOLE_LENGTH/8) {
         return min;
      } else {
         return (n.length/(Note.WHOLE_LENGTH/8))*min;
      }
   }
   public abstract float drawHeader(Graphics2D g);
   //public abstract int getBarY();
   //public abstract int getBarHeight();
   public abstract void drawBarLine(Graphics2D g, float x, int barNumber);
   
}
