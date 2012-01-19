package view;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.SwingUtilities;

import api.model.Note;
import api.model.Part;
import api.util.Util;



class SonataSymbol {
   public static final String G_CLEF="\ud834\udd1e";
   public static final String C_CLEF="\ud834\udd21";
   public static final String F_CLEF="\ud834\udd22";
   
   public static final String MULTI_REST="\uD834\uDD3A";
   public static final String WHOLE_REST="\ud834\udd3b";
   public static final String HALF_REST="\ud834\udd3c";
   public static final String QUARTER_REST="\ud834\udd3d";
   public static final String EIGHTH_REST="\ud834\udd3e";
   public static final String SIXTEENTH_REST="\uD834\uDD3F";
   public static final String THIRTY_SECOND_REST="\uD834\uDD40";
   public static final String SIXTY_FOURTH_REST="\uD834\uDD41";
   public static final String HUNDRED_TWENTY_EIGHTH_REST="\uD834\uDD42";
   
   public static final String BREVE="\uD834\uDD5C";
   public static final String WHOLE_NOTE="\uD834\uDD5D";
   public static final String HALF_NOTE="\uD834\uDD5E";
   public static final String QUARTER_NOTE="\uD834\uDD5F";
   public static final String EIGHTH_NOTE="\uD834\uDD60";
   public static final String SIXTEENTH_NOTE="\uD834\uDD61";
   public static final String THIRTY_SECOND_NOTE="\uD834\uDD62";
   public static final String SIXTY_FOURTH_NOTE="\uD834\uDD63";
   public static final String HUNDRED_TWENTY_EIGHTH_NOTE="\uD834\uDD64";
   
   public static final String REVERSE_HALF_NOTE="\uE01D";
   public static final String REVERSE_QUARTER_NOTE="\uE020";
   public static final String REVERSE_EIGHTH_NOTE="\uE026";
   public static final String REVERSE_SIXTEENTH_NOTE="\uE029";
   public static final String REVERSE_THIRTY_SECOND_NOTE="\uE02D";
   public static final String REVERSE_SIXTY_FOURTH_NOTE="\uE02F";
   public static final String REVERSE_HUNDRED_TWENTY_EIGHTH_NOTE="\uE030";
   
   public static final String FLAT="\uE015";
   public static final String NATURAL="\uE016";
   public static final String SHARP="\uE017";
   public static final String TEMPO_EQUALS="\uE023";
   
   public static final String DOUBLE_SHARP="\uD834\uDD2A";
   public static final String DOUBLE_FLAT="\uD834\uDD2B";
   
}
class Clef {
   public static final Clef G=new Clef(67);
   public static final Clef C=new Clef(60);
   public static final Clef F=new Clef(53);
   
   private final int pitch;
   private Clef(int p) {
      pitch=p;
   }
   
   public int getPitch() {
      return pitch;
   }
   
   @Deprecated
   public int getPitch(int sf) {
      if(sf>0) {
         for(int i=0; i<sf; i++) {
            if(pitch%12==StaffPartView.sharpIndices[i]) {
               return pitch+1;
            }
         }   
      } else if(sf<0) {
         for(int i=0; i>sf; i--) {
            if(pitch%12==StaffPartView.flatIndices[-i]) {
               return pitch-1;
            }
         }
      }
      return pitch;
   }
} 

public class StaffPartView extends PartView {
   public static final int[] sharpIndices={5, 0, 7, 2, 9, 4, 11};
   public static final int[] flatIndices={11, 4, 9, 2, 7, 0, 5};
   
   private int numVisiblePosition=9;
   private int startPosition;
   private int visiblePositions=30;//30; //: max: 75
   
   private Clef clef=Clef.G;
   //private Key key=Key.C; //>>>
   private int clefLine;
   
   final java.util.List<Integer> positions=new ArrayList<Integer>();
   final int[] sharpFlats;
   final int[] accidentals;
   
   private final int positionGap=4;
   public float getPositionSize() {
      return positionGap*scoreView.zoom;
   }
   
   /*private int sharpFlatCount=0; //: -7 ~ +7
   private int nominator=4;
   private int denominator=4;
   private int tempo=120;*/
   /*private final java.util.List<Note> notes=new ArrayList<Note>();
   
   public void addNote(Note n) { notes.add(n); }*/
   
   public Font textFont=null;
   public Font clefFont=null;
   public Font acciFont=null;
   public Font rhythmFont=null;
   public Font tempoFont=null;
   public Font otherFont=null;
   public Font sonataFont=null;
   
   public static Font TEXT_FONT=null;
   public static Font CLEF_FONT=null;
   public static Font ACCI_FONT=null;
   public static Font RHYTHM_FONT=null;
   public static Font OTHER_FONT=null;
   public static Font SONATA_FONT=null;
   public static Font TEMPO_FONT=null;
   static {
      try {
         TEXT_FONT=new Font(Font.SERIF, Font.PLAIN, 12);
         CLEF_FONT=Font.createFont(Font.TRUETYPE_FONT, Util.getResourceAsStream("fonts/CLEFS___.TTF"));
         ACCI_FONT=Font.createFont(Font.TRUETYPE_FONT, Util.getResourceAsStream("fonts/Accidentals.ttf"));
         RHYTHM_FONT=Font.createFont(Font.TRUETYPE_FONT, Util.getResourceAsStream("fonts/RHYTHMS_.TTF"));
         TEMPO_FONT=Font.createFont(Font.TRUETYPE_FONT, Util.getResourceAsStream("fonts/TEMPILTR.TTF"));
         OTHER_FONT=Font.createFont(Font.TRUETYPE_FONT, Util.getResourceAsStream("fonts/FIGUBM__.TTF"));
         SONATA_FONT=Font.createFont(Font.TRUETYPE_FONT, Util.getResourceAsStream("fonts/SonataStd.ttf"));
      } catch (IOException ioe) {
         ioe.printStackTrace();
      } catch (FontFormatException ffe) {
         ffe.printStackTrace();
      }
   }
   public void updateMusicFonts() {
      final float MAG_RATIO=7.85f;
      textFont=TEXT_FONT.deriveFont(getPositionSize() * MAG_RATIO / 3.0f);
      clefFont=CLEF_FONT.deriveFont(getPositionSize() * MAG_RATIO);
      acciFont=ACCI_FONT.deriveFont(getPositionSize() * MAG_RATIO);
      rhythmFont=RHYTHM_FONT.deriveFont(getPositionSize() * MAG_RATIO);
      tempoFont=TEMPO_FONT.deriveFont(getPositionSize() * MAG_RATIO / 2.0f);
      otherFont=OTHER_FONT.deriveFont(getPositionSize() * MAG_RATIO);
      sonataFont=SONATA_FONT.deriveFont(getPositionSize() * MAG_RATIO);
   }
   
   public StaffPartView(ScoreView s, Part part) {
      this(Clef.G, 1, s.score.getKeySignature().getValue(), s, part); //Treble
      //this(Clef.F, 3, s.score.getKey().getValue(), s, part); //Bass
      //this(Clef.C, 2); //Alto
      //this(Clef.C, 3); //Tenor
   }
   public StaffPartView(Clef c, int cl, int sf, ScoreView sheet, Part part) {
      super(sheet, part);
      clef=c;
      clefLine=cl; //(cl+cl-1)-1;
      //sharpFlatCount=acci;
      
      int startPitch=clef.getPitch();
      int pitch=startPitch;
      
      //[ assign the pitch of every position
      while(pitch<128) {
         if(Util.isMajorKey(pitch)) {
            positions.add(pitch);
         }  
         pitch++;
      }
      pitch=startPitch-1;
      while(pitch>=0) {
         if(Util.isMajorKey(pitch)) {
            positions.add(pitch);
         }  
         pitch--;
      }
      Collections.sort(positions); 
      
      //[ accidentials
      sharpFlats=new int[positions.size()];
      if(getPart().getScore().getKeySignature().getValue()>0) {
L1:      for(int j=0; j<positions.size(); j++) {
            final int p=positions.get(j); 
            for(int i=0; i < getPart().getScore().getKeySignature().getValue(); i++) {
               if(p%12==sharpIndices[i]) {
                  //positions.set(j, p+1);
                  sharpFlats[j]=1;
                  continue L1;
               }   
            }   
         }         
      } else if(getPart().getScore().getKeySignature().getValue()<0) {
L1:      for(int j=0; j<positions.size(); j++) {
            final int p=positions.get(j); 
            for(int i=0; i > getPart().getScore().getKeySignature().getValue(); i--) {
               if(p%12==flatIndices[-i]) {
                  //positions.set(j, p-1);
                  sharpFlats[j]=-1;
                  /*if(p-1>0) {
                     System.err.println(keyNames[(p)%12]+": "+keyNames[(p-1)%12]);
                  }*/
                  continue L1;
               }   
            }   
         }         
      }
      //remove illegal pitches
      /*for(int i=0; i<positions.size(); i++) {
         if(positions.get(i)<0 || positions.get(i)>=128) {
            positions.remove(i);
            i--;
         }
      }*/
      
      //[ find startPosition: the first line of visible staff
      int clefPos=getPosByPitch(clef.getPitch());
      startPosition=clefPos-clefLine*2;
      
      accidentals=new int[positions.size()];
   }
   
   public float getNoteGap() {
      return 0;//(getPositionSize()*4);
   }
//   public float getNoteWidth(Note n) {
//      final float min=scoreView.zoom*36;
//      if(n==null) return min; //>>>
//      if(n.length<=Note.WHOLE_LENGTH/8) {
//         return min;
//      } else {
//         return (n.length/(Note.WHOLE_LENGTH/8))*min;
//      }
//      //return getPositionSize()*4; //>>>
//   }
   
   private int getDiffIndex() {
      int index=-1;
      if(clef==Clef.G) index=4;
      else if(clef==Clef.F) index=3;
      else if(clef==Clef.C) index=0;
      return index;
   }
   
   //public static final String[] majorKeyNames= {"C", "D", "E", "F", "G", "A", "B"};
   
   
   private int getPosByLine(int line) {
      return startPosition+line*2;
   }
   public int getYByPos(int pos) {
      //final int sub=(positions.size()-visiblePositions)/2;
      //final int sub=positions.size()-(startPosition+2*5)-(visiblePositions-2*5)/2;
      final int visibleStart=startPosition-(visiblePositions-8)/2;
      return (int)(getPositionSize()*((visiblePositions-(pos-visibleStart))));
      //return (int)(getPositionSize()*(positions.size()-pos);
   }
   private int[] getPosAcciByPitch(int pitch) {
      if(pitch < 0 || pitch >= 128) { // [ rest
         return new int[] {startPosition + 4, 0};
      } 
      for(int i=0; i<positions.size(); i++) {
         final int posPitch=positions.get(i)+sharpFlats[i]+accidentals[i];
         if(pitch==posPitch) { 
            return new int[] {i, 0};
         } 
      }
      for(int i=0; i<positions.size(); i++) {
         final int posPitch=positions.get(i)+sharpFlats[i]+accidentals[i];
         if(pitch==posPitch-1){
            return new int[] {i, -1};
         } else if(pitch==posPitch+1){
            return new int[] {i, 1};
         } 
      }
      //[ >>>why?
      for(int i=0; i<positions.size(); i++) {
         final int posPitch=positions.get(i)+sharpFlats[i]+accidentals[i];
         if(pitch==posPitch-2){
            return new int[] {i, -2};
         } else if(pitch==posPitch+2){
            return new int[] {i, 2};
         } 
      }
      throw new RuntimeException(""+pitch);
   }
   private int getPosByPitch(int pitch) { //didn't count sharp/flat!!! 
      for(int i=positions.size()-1; i>=0; i--) {
         if(positions.get(i)<=pitch) { //>>> may have sharp
            return i; 
         }
      }
      return -1;
   }
   private int getYByLine(int line) {
      return getYByPos(getPosByLine(line));
   }
//   private int getYByPitch(int pitch) {
//      return getYByPos(getPosByPitch(pitch));
//   }
   public int getPosByY(int y) { //y is relative to y of staff
      //final int sub=(positions.size()-visiblePositions)/2;
      final int visibleStart=startPosition-(visiblePositions-8)/2;
      int pos=visiblePositions-((int)(y/getPositionSize()))+visibleStart;
      if(pos<0) pos=0;
      else if(pos>=positions.size()) pos=positions.size()-1;
      return pos;
   }
   
   public int getPitchByPos(int pos) { //don't take acci. into account
      return positions.get(pos)+sharpFlats[pos];
   }
//   public int getPitchByPosIndex(int pos, int index) {
//      final int pitch=getPitchByPos(pos);
//      for(int i=0; i<index; i++) //>>>
//   }
   
   /*public static Rectangle getStrRect(FontMetrics fm, String str, int x, int y) {
      return new Rectangle(x, y-fm.getMaxAscent(), 
            SwingUtilities.computeStringWidth(fm, str), fm.getHeight());
   }*/
   
   public String getSonataSymbol(int pitch/* to know rest or normal */, int length, int notePos) { //>>> fixed length
      
      if(pitch < 0 || pitch >= 128) { // [ rest
         switch(length) {
         case Note.WHOLE_LENGTH/128:
            return SonataSymbol.HUNDRED_TWENTY_EIGHTH_REST;
         case Note.WHOLE_LENGTH/64:
            return SonataSymbol.SIXTY_FOURTH_REST;
         case Note.WHOLE_LENGTH/32:
            return SonataSymbol.THIRTY_SECOND_REST;
         case Note.WHOLE_LENGTH/16:
            return SonataSymbol.SIXTEENTH_REST;
         case Note.WHOLE_LENGTH/8:
            return SonataSymbol.EIGHTH_REST;
         case Note.WHOLE_LENGTH/4:
            return SonataSymbol.QUARTER_REST;
         case Note.WHOLE_LENGTH/2:
            return SonataSymbol.HALF_REST;
         case Note.WHOLE_LENGTH:
            return SonataSymbol.WHOLE_REST;
         default:
            return null;//throw new RuntimeException();
         }
         // System.err.println(notes.get(i).length);
      } else {
         if(notePos - startPosition >= 4) {
            switch(length) {
            case Note.WHOLE_LENGTH/128:
               return SonataSymbol.REVERSE_HUNDRED_TWENTY_EIGHTH_NOTE;
            case Note.WHOLE_LENGTH/64:
               return SonataSymbol.REVERSE_SIXTY_FOURTH_NOTE;
            case Note.WHOLE_LENGTH/32:
               return SonataSymbol.REVERSE_THIRTY_SECOND_NOTE;
            case Note.WHOLE_LENGTH/16:
               return SonataSymbol.REVERSE_SIXTEENTH_NOTE;
            case Note.WHOLE_LENGTH/8:
               return SonataSymbol.REVERSE_EIGHTH_NOTE;
            case Note.WHOLE_LENGTH/4:
               return SonataSymbol.REVERSE_QUARTER_NOTE;
            case Note.WHOLE_LENGTH/2:
               return SonataSymbol.REVERSE_HALF_NOTE;
            case Note.WHOLE_LENGTH:
               return SonataSymbol.WHOLE_NOTE;
            default:
               return null;//throw new RuntimeException();
            }
         } else {
            switch(length) {
            case Note.WHOLE_LENGTH/128:
               return SonataSymbol.HUNDRED_TWENTY_EIGHTH_NOTE;
            case Note.WHOLE_LENGTH/64:
               return SonataSymbol.SIXTY_FOURTH_NOTE;
            case Note.WHOLE_LENGTH/32:
               return SonataSymbol.THIRTY_SECOND_NOTE;
            case Note.WHOLE_LENGTH/16:
               return SonataSymbol.SIXTEENTH_NOTE;
            case Note.WHOLE_LENGTH/8:
               return SonataSymbol.EIGHTH_NOTE;
            case Note.WHOLE_LENGTH/4:
               return SonataSymbol.QUARTER_NOTE;
            case Note.WHOLE_LENGTH/2:
               return SonataSymbol.HALF_NOTE;
            case Note.WHOLE_LENGTH:
               return SonataSymbol.WHOLE_NOTE;
            default:
               return null;//throw new RuntimeException();
            }
         }
      }
      //System.err.println(length);
      //return null;
   }
   
   @Override
   public float drawHeader(Graphics2D g) {
      // [ draw lines
      g.setColor(Color.black);
      g.setStroke(lineStroke);
      if(DEBUG) {
         g.setColor(Color.lightGray);
         for(int i=0; i < positions.size(); i++) {
            int lineY=getYByPos(i);
            g.drawLine((int) (getPositionSize() * 5), lineY,
                  (int) (w() - getPositionSize() * 5), lineY);
            g.drawString(String.valueOf(positions.get(i)+sharpFlats[i])+": "+
                  Util.getPitchName(positions.get(i)+sharpFlats[i]), 
                  getPositionSize()*1, lineY+getPositionSize()/2);
         }
      }
      g.setColor(Color.black);
      for(int i=0; i < positions.size(); i++) {
         if(i < startPosition || i >= startPosition + numVisiblePosition)
            continue;
         g.setColor(Color.black);
         if((i - startPosition) % 2 != 0) {
            if(DEBUG) {
               g.setColor(Color.gray);
            } else {
               continue; // g.setColor(Color.lightGray);
            }
         }
         final Integer p=positions.get(i);
         int lineY=getYByPos(i);
         if(DEBUG) {
            if(p == clef.getPitch())
               g.setColor(Color.red);
         }
         g.drawLine((int) (getPositionSize() * 5), lineY,
               (int) (w() - getPositionSize() * 5), lineY);
         /*if(DEBUG) {
            // g.drawString(keyNames[p % 12] + (p / 12 - 1), positionGap, lineY
            // + 6);
            g.drawString(keyNames[p % 12] + (p / 12 - 1) + ":" + p + "("
                  + accidentals[i] + ")", 1, lineY + 6);
         }*/

      }
      g.setColor(Color.black);
      final int startY=getYByPos(startPosition);
      final int endY=getYByPos(startPosition + numVisiblePosition - 1);
      g.drawLine((int) (getPositionSize() * 5), startY,
            (int) (getPositionSize() * 5), endY);
      g.drawLine((int) (w() - getPositionSize() * 7), startY,
            (int) (w() - getPositionSize() * 7), endY);
      g.drawLine((int) (w() - getPositionSize() * 5), startY,
            (int) (w() - getPositionSize() * 5), endY);
      g.fillRect((int) (w() - getPositionSize() * 6), endY,
            (int) Math.round(getPositionSize()), startY-endY);
      g.setStroke(oldStroke);

      float currentX=getPositionSize() * 7;

      // [ draw clef
      // g.setFont(clefFont);
      g.setFont(sonataFont);
      String clefStr=null;
      if(clef == Clef.G) {
         clefStr=SonataSymbol.G_CLEF; // "y";
      } else if(clef == Clef.F) {
         clefStr=SonataSymbol.F_CLEF; // "f";
      } else if(clef == Clef.C) {
         clefStr=SonataSymbol.C_CLEF; // "n";
      }

      final int clefY=getYByPos(startPosition/* +clefLine2-2 */);
      g.drawString(clefStr, (int) currentX, clefY);
      /*
       * final Rectangle clefRect=getStrRect(g.getFontMetrics(), clefStr,
       * (int)currentX, clefY); g.drawRect(clefRect.x, clefRect.y,
       * clefRect.width, clefRect.height);
       */
      currentX+=SwingUtilities.computeStringWidth(g.getFontMetrics(), clefStr)
            + getPositionSize();

      // [ key signature
      g.setFont(acciFont);
      // g.setFont(sonataFont);
      final int sharpWidth=SwingUtilities.computeStringWidth(
            g.getFontMetrics(), "s");
      final int flatWidth=SwingUtilities.computeStringWidth(g.getFontMetrics(),
            "b");
//System.err.println(part.getScore());
      if(getPart().getScore().getKeySignature().getValue() > 0) {
         L1: for(int i=0; i < getPart().getScore().getKeySignature().getValue(); i++) {
            for(int j=startPosition + numVisiblePosition; j >= startPosition; j--) {
               if(positions.get(j) % 12 == sharpIndices[i]) {
                  // g.drawString(SonataSymbol.SHARP, currentX, getYByPos(j));
                  g.drawString("s", currentX, getYByPos(j));
                  currentX+=sharpWidth;
                  continue L1;
               }
            }

         }
      } else if(getPart().getScore().getKeySignature().getValue() < 0) {
         L1: for(int i=0; i > getPart().getScore().getKeySignature().getValue(); i--) {
            for(int j=startPosition + numVisiblePosition - 2; j >= startPosition; j--) {
               if(positions.get(j) % 12 == flatIndices[-i]) {
                  // g.drawString(SonataSymbol.FLAT, currentX, getYByPos(j));
                  g.drawString("b", currentX, getYByPos(j));
                  currentX+=flatWidth;
                  continue L1;
               }
            }

         }
      }

      // [ time signature
      g.setFont(sonataFont);
      final int numWidth=SwingUtilities.computeStringWidth(g.getFontMetrics(),
            String.valueOf(getPart().getScore().getNoteValuePerBeat()));
      currentX+=numWidth;
      g.drawString(String.valueOf(getPart().getScore().getBeatsPerMeasure()), currentX, getYByLine(3));
      g.drawString(String.valueOf(getPart().getScore().getNoteValuePerBeat()), currentX, getYByLine(1));
      currentX+=numWidth;

      // [ draw tempo
//      g.setFont(tempoFont); final String tempoSymStr="%";
//      g.drawString(tempoSymStr, currentX, getYByLine(5));;
//      currentX+=SwingUtilities.computeStringWidth(g.getFontMetrics(),
//            tempoSymStr); 
//      g.setFont(textFont); 
//      g.drawString("="+getPart().getScore().tempo, currentX, getYByLine(5));;
//      currentX-=SwingUtilities.computeStringWidth(g.getFontMetrics(), tempoSymStr);
      g.setFont(textFont); 
      g.drawString(getPart().getInstrument().getName(), 10*scoreView.zoom, 12*scoreView.zoom);;
      
      currentX+=getPositionSize()*4;
      
      //[ reset acci
      for(int i=0; i < accidentals.length; i++) {
         accidentals[i]=0;
      }
      
      return currentX;
   }
   
   private int getLongerRegularLength(int length) {
      for(int i=128; i>=1; i/=2) {
         if(Note.WHOLE_LENGTH/i>length) {
            return Note.WHOLE_LENGTH/i;
         }
      }
      return Note.WHOLE_LENGTH; //>>> shouldn't run
   }
   
   @Override
   public void drawNote(Graphics2D g, int noteIndex, float currentX) {
      final Note note=getPart().get(noteIndex);
      final int[] posAcci=getPosAcciByPitch(note.pitch);
      final int notePos=posAcci[0];
      final int noteAcci=posAcci[1];
      final int noteY=getYByPos(notePos);
      String noteSym=getSonataSymbol(note.pitch, note.length, notePos);
      
      final int noteSymWidth=SwingUtilities.computeStringWidth(g.getFontMetrics(sonataFont), noteSym);
      final float symbolX=currentX+(getNoteWidth(note)-noteSymWidth)/2;
      
      if(noteSym==null) {
         noteSym=getSonataSymbol(note.pitch, 
               getLongerRegularLength(note.length), notePos);
         if(noteSym==null) {
            final Font oldFont=g.getFont();
            g.setFont(textFont);
            g.setColor(Color.black);
            g.drawString("?", symbolX, noteY);
            g.setFont(oldFont);
            return; //>>>   
         }
      }
      final int ledgerLineWidth=(int)(getPositionSize()*4); //>>>
         //(int) (SwingUtilities.computeStringWidth(g.getFontMetrics(sonataFont), SonataSymbol.WHOLE_NOTE));
      
      //[ draw ledger lines
      g.setColor(Color.black);
      if(notePos < startPosition) {
         for(int k=notePos; k < startPosition; k++) {
            if((k - startPosition) % 2 != 0)
               continue;
            g.drawLine((int) (symbolX - getPositionSize()), getYByPos(k),
                  (int) (symbolX + ledgerLineWidth + getPositionSize()),
                  getYByPos(k));
         }

      } else if(notePos > startPosition + numVisiblePosition) {
         for(int k=notePos; k > startPosition + numVisiblePosition; k--) {
            if((k - startPosition) % 2 != 0)
               continue;
            g.drawLine((int) (symbolX - getPositionSize()), getYByPos(k),
                  (int) (symbolX + ledgerLineWidth + getPositionSize()),
                  getYByPos(k));
         }
      }
      
      //[ draw note itself
      g.setFont(sonataFont);
      g.setColor(Color.black);
      g.drawString(noteSym, symbolX, noteY);
      //System.err.println(noteSym);
      
      if(DEBUG) {
         g.setColor(Color.red);
         g.drawLine((int)symbolX, noteY, (int)(symbolX+noteSymWidth), noteY);
      }
      //[ draw dots
      g.setColor(Color.black);
      g.setFont(textFont);
      for(int i=0; i<getPart().get(noteIndex).dot; i++) {
         final int x=(int)(symbolX+noteSymWidth+(i+1)*getPositionSize());
         g.fillOval(x, (int)(noteY-getPositionSize()/2), (int)(3*scoreView.zoom), (int)(3*scoreView.zoom));
         //g.drawString(".", currentX+(i+3)*getPositionSize(), noteY);
      }
      
      //[ draw tie
      if(note.tie!=0) g.drawString("t", currentX, noteY+20*scoreView.zoom);
      //[ draw triplet
      if(note.isTripletElement) g.drawString("3", currentX, noteY+20*scoreView.zoom);
      
      //[ draw accidentials;
      g.setColor(Color.black);
      g.setFont(acciFont);
      
      final int before=sharpFlats[notePos]+accidentals[notePos];
      if(noteAcci>0) {
         if(before>0) {
            g.drawString("x", symbolX - getPositionSize()*2, noteY); //double sharp
         } else if(before<0) {
            if(before<-1) {
               g.drawString("b", symbolX - getPositionSize()*2, noteY); //restore double flat
            } else {
               g.drawString("n", symbolX - getPositionSize()*2, noteY); //restore
            }
         } else {
            if(noteAcci>1) {
               g.drawString("x", symbolX - getPositionSize()*2, noteY); //double sharp
            } else {
               g.drawString("s", symbolX - getPositionSize()*2, noteY); //sharp
            }
         }
         accidentals[notePos]+=noteAcci;
      } else if(noteAcci<0) {
         if(before>0) {
            if(before>1) {
               g.drawString("s", symbolX - getPositionSize()*2, noteY); //restore double sharp
            } else {
               g.drawString("n", symbolX - getPositionSize()*2, noteY); //restore
            }
         } else if(before<0) {
            g.drawString("B", symbolX - getPositionSize()*2, noteY); //double flat
         } else {
            if(noteAcci<1) {
               g.drawString("B", symbolX - getPositionSize()*2, noteY); //double flat
            } else {
               g.drawString("b", symbolX - getPositionSize()*2, noteY); //flat
            }
         }
         accidentals[notePos]+=noteAcci;
      }
      
   } 
   
   public int getBarY() { return getYByLine(4); }
   public int getBarHeight() { return getYByLine(0)-getYByLine(4); }
   
   @Override
   public void drawBarLine(Graphics2D g, float currentX, int barNumber) {
      final int lineStart=getBarY();//getYByLine(0);
      final int lineEnd=getBarHeight(); //getYByLine(4);
      final int x=(int)(currentX);
      g.setColor(Color.black);
      g.drawLine(x, lineStart, x, lineStart+lineEnd);
      g.setFont(textFont); //>>> use a better font
      g.drawString(String.valueOf(barNumber), x, lineStart-12*scoreView.zoom);
      
      //[ reset acci
      for(int i=0; i < accidentals.length; i++) {
         accidentals[i]=0;
      }
   }
   
//   public int getPosByPitch(int pitch) { //>>> getYByPitch?
//      int notePos=-1;
//      if(pitch < 0 || pitch >= 128) { // [ rest
//         notePos=startPosition + 4;
//      } else {
//         for(int j=positions.size() - 1; j >= 0; j--) {
//            if(pitch >= positions.get(j) + sharpFlats[j]) {
//               notePos=j;
//               break;
//            }
//         }
//      }
//      return notePos;
//   }
   /*public int getNoteWidth(int index) {
      final Graphics2D g=(Graphics2D)sheet.getGraphics();
      if(g==null) throw new RuntimeException();
      final Note n=part.get(index);
      final String noteSym=getSonataSymbol(n.pitch, n.length, getNoteYPosition(n));
      final int noteW=SwingUtilities.computeStringWidth(g.getFontMetrics(sonataFont), noteSym);
      return noteW;
   }*/
   
   @Override
   public void adjust(Graphics2D g) {
      float width=0;
      for(int noteIndex=0; noteIndex<part.noteCount(); noteIndex++) {
         width+=getNoteWidth(part.get(noteIndex));
      }
      width+=getPositionSize()*48; //>>> header width + append width
      setSize((int)width, (int)(getPositionSize()*(visiblePositions+1)));
      //setSize((int)(getPositionSize()*200), (int)(getPositionSize()*(numPos+1)));
      //setSize((int)((getNoteWidth(null)+getNoteGap())*getPart().size()+getPositionSize()*64), //>>>>>>>>>>>>>>not accurate 
      //        (int)(getPositionSize()*(visiblePositions+1)));
      //setSize(2480, 3508); //: A4
      //clefFont=null;
      updateMusicFonts();
      //System.err.println(zoom);
      lineStroke=new BasicStroke(scoreView.zoom);
   }
   /*public void zoomIn() {
      //setPositionGap(getPositionGap() * 2);
      zoom*=(5.0f/4.0f);
      //System.err.println("zoom: "+zoom);
      adjust();
   }
   public void zoomOut() {
      //setPositionGap(getPositionGap() / 2);
      zoom*=(4.0f/5.0f);
      //System.err.println("zoom: "+zoom);
      //if(getPositionGap()<=0) setPositionGap(1);
      adjust();
   }*/
}