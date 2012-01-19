package view;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import api.model.Note;
import api.model.Part;




public class GridPartView extends PartView {
   
   private static final int NUM_POS=128;
   private static final int WHOLE_WIDTH=100;
   int noteHeight=5;
   int beatLength=Note.WHOLE_LENGTH/part.getScore().getNoteValuePerBeat();
   int barLength=beatLength*part.getScore().getBeatsPerMeasure();
   
   public GridPartView(ScoreView s, Part p) {
      super(s, p);
   }
   @Override
   public void drawBarLine(Graphics2D g, float x, int barNumber) { //>>>count on notes
//      g.setColor(Color.gray);
//      g.drawLine((int)(x), 0, (int)(x), h());
   }

   @Override
   public float drawHeader(Graphics2D g) {
      final Rectangle visibleRect=scoreView.getVisibleRect();
      g.setColor(Color.white);
      g.fillRect(0, 0, w(), h());
      
      g.setColor(Color.lightGray);
      //[ horizontal lines
      for(int i=0; i<=NUM_POS; i++) {
         g.drawLine(0, (int)(i*noteHeight*scoreView.zoom), w(), (int)(i*noteHeight*scoreView.zoom));
         //g.drawLine(visibleRect.x , (int)(i*noteHeight*sheet.zoom), w()<visibleRect.width?w():visibleRect.width, (int)(i*noteHeight*sheet.zoom));
      }
      //[ vertical lines
      g.setColor(Color.lightGray);
      float lengthCount=0;
      
      for(int i=0;; i+=beatLength) {
         final int currentX=(int)(lengthToWidth(lengthCount)*scoreView.zoom);
         if(currentX>w()) break;
         if(currentX>=visibleRect.x && currentX<visibleRect.x+visibleRect.width) {
            if(lengthCount%barLength==0) { //a bar
               g.setColor(Color.gray);
            } else {
               g.setColor(Color.lightGray);      
            }
            g.drawLine(currentX, 0, currentX, h());
         }
         lengthCount+=beatLength;
      }
      return 0;
   }

   @Override
   public void drawNote(Graphics2D g, int index, float currentX) { ///>>>tie
      final Note note=part.get(index);
      if(note.isRest()) return;
      g.setColor(Color.orange);
      final int nx=(int)(currentX), ny=(int)((128-note.pitch)*noteHeight*scoreView.zoom);
      final int nw=(int)(getNoteWidth(note)), nh=(int)(noteHeight*scoreView.zoom);
      final int round=0; //(int)(noteHeight*sheet.zoom);
      g.fillRoundRect(nx, ny, nw, nh, round, round);
      g.setColor(Color.black);
      g.drawRoundRect(nx, ny, nw, nh, round, round);
   }

   @Override
   public float getNoteGap() {
      // TODO Auto-generated method stub
      return 0;
   }

   
   private static float lengthToWidth(float length) {
      return (float)length*WHOLE_WIDTH/Note.WHOLE_LENGTH;
   }
   
   @Override
   public float getNoteWidth(Note n) {
      return lengthToWidth(n.getActualLength())*scoreView.zoom;
   }

   @Override
   public void adjust(Graphics2D g) {
      float width=0;
      for(int i=0; i<part.noteCount(); i++) {
         width+=getNoteWidth(part.get(i));
      }
      setSize((int)(width+10*lengthToWidth(barLength)), //make it wider to add new notes 
              (int)(noteHeight*NUM_POS*scoreView.zoom));
   }

}
