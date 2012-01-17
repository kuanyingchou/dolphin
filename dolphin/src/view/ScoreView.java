package view;

import gui.MainFrame;
import gui.SoundInputDialog;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Path2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import api.audio.PitchListener;
import api.audio.SoundAnalyzer;
import api.midi.ScorePlayer.PlayerListener;
import api.model.AddNoteChange;
import api.model.AddPartChange;
import api.model.KeySignatureChange;
import api.model.Note;
import api.model.Part;
import api.model.Path;
import api.model.RemoveNoteChange;
import api.model.RemovePartChange;
import api.model.Score;
import api.model.ScoreChange;
import api.model.ScoreChangeListener;
import api.util.Pair;
import api.util.Util;



public class ScoreView extends JComponent implements 
   ScoreChangeListener, PitchListener, Receiver, PlayerListener
{
   private java.util.List<PartView> partViews=new ArrayList<PartView>();
   //final MainFrame mainFrame;
   public final Score score;
   float zoom=1.0f;
   //public InsertAdapter insertAdapter=new InsertAdapter();
   //public SelectAdapter selectAdapter=new SelectAdapter();
   
   private int leftBorder=20;
   private int rightBorder=20;
   private int topBorder=20;
   private int bottomBorder=20;

   //[ cursor & selection
   final Path cursor=new Path();
   public final Path staticCursor=new Path(); //0<=partIndex<score.size(), 0<=index<=part.size()
   
   public int selection=0; //can be negative, relative to staticCursor
   
   
   public ScoreView(/*MainFrame mf,*/ Score s) {
      if(/*mf==null ||*/ s==null) throw new IllegalArgumentException();
      //mainFrame=mf;
      score=s;
      score.addScoreChangeListener(this);
      setBackground(Color.white);
      for(int i=0; i<score.partCount(); i++) {
         addPartView(new StaffPartView(this, score.get(i)), i);
         //addStaff(new GridPartView(this, score.get(i)), i); //>>>
      }
      //setSize(400, 300);
      
      /*final JPopupMenu popup=new JPopupMenu();
      popup.add(new AbstractAction() {
         public void actionPerformed(ActionEvent e) {
            System.err.println("hi");
         }
      });

      addMouseListener(new MouseAdapter() {
         public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                popup.show(e.getComponent(),
                           e.getX(), e.getY());
            }
        }
      });*/
  

      addMouseListener(new MouseAdapter() {
         public void mousePressed(MouseEvent e) {
            requestFocusInWindow();
        }
      });
      //addMouseAdapter(insertAdapter); //default
      //addMouseAdapter(selectAdapter); 
      //insertAdapter.setEnabled(false); //>>> a better way?
      addMouseAdapter(new MouseAdapterChanger());
      
      setFocusable(true);
      //setFocusCycleRoot(true); //>>>>>
      addKeyListener(new KeyInputAdapter());
   }
   
   public int getSelectionStartIndex() {
      return selection<0?staticCursor.noteIndex+selection:staticCursor.noteIndex;
   }
   public int getAbsSelectionLength() { //positive
      return selection<0? -selection: selection;
   }
   
   public int indexOf(PartView s) {
      return partViews.indexOf(s);
   }
   
   public Rectangle getStaticCursorBounds() {
      int y=topBorder;
      for(int i=0; i<staticCursor.partIndex; i++) {
         y+=partViews.get(i).h();
      }
      final PartView pv=partViews.get(staticCursor.partIndex);
      float x=leftBorder;
      for(int i=0; i<staticCursor.noteIndex; i++) {
         x+=pv.getNoteWidth(score.get(staticCursor.partIndex).get(i))+pv.getNoteGap(); //>>>header
      }
      //System.err.println(x);
      int w=1;
      int h=partViews.get(staticCursor.partIndex).h();
      final Rectangle res=new Rectangle((int)x, y, w, h);
      return res;
   }
   
   public void addMouseAdapter(MouseAdapter a) {
      addMouseListener(a);
      addMouseMotionListener(a);
      addMouseWheelListener(a);
   }
   
   public void addPartView(PartView s, int index) {
      partViews.add(index, s);
      //s.sheet=this;
      validate();
   }
   public void removePartView(int index) {
      final PartView s=partViews.remove(index);
      s.scoreView=null;
      validate();
   }
   @Override
   public void validate() { //>>> don't use validate
      int height=(int)(topBorder*zoom);
      for(PartView s: partViews) {
         //System.err.println(s.sheet);
         s.adjust((Graphics2D)getGraphics());
         s.y(height);
         s.x((int)(leftBorder*zoom));
         height+=s.h();
      }
      super.validate();
   }
   
   private boolean overlapStaffs=false;
   @Override
   public Dimension getPreferredSize() {
      if(overlapStaffs) {
         return new Dimension(partViews.get(0).w(), partViews.get(0).h());   
      } else {
         int width=0;
         int height=0;
         for(PartView s: partViews) {
            if(s.w()>width) width=s.w();
            height+=s.h();
         }         
         return new Dimension(width+(int)((leftBorder+rightBorder)*zoom), 
                              height+(int)((topBorder+bottomBorder)*zoom));
      }
      
   }
   
   public void paintComponent(Graphics g) {
      /*final Insets inset=getInsets();
      final int x=inset.left;
      final int y=inset.top;
      final int width=getWidth()-inset.left-inset.right;
      final int height=getHeight()-inset.top-inset.bottom;*/
      super.paintComponent(g);
      final int x=0, y=0, width=getWidth(), height=getHeight();
      g.setColor(Color.white);
      g.fillRect(x, y, width, height);
      /*g.setColor(Color.black);
      g.drawRect(x, y, width, height);*/
      final Graphics2D g2d=(Graphics2D) g;
      for(PartView s: partViews) {
         s.draw(g2d);  
      }
      
   }
   
   public void zoomIn() {
      final Rectangle visibleRect=getVisibleRect();
      final double oldWidth=getPreferredSize().getWidth();
      
      zoom*=(5.0f/4.0f);

      for(PartView s: partViews) {
         s.adjust((Graphics2D)getGraphics());  
      }
      
      final double newWidth=getPreferredSize().getWidth();
      final double ratio=newWidth/oldWidth;
      
      final int newX=(int)(ratio*visibleRect.getX()+visibleRect.getWidth()*(ratio-1)/2);
      final int newY=(int)(ratio*visibleRect.getY()+visibleRect.getHeight()*(ratio-1)/2);
      visibleRect.x=newX;
      visibleRect.y=newY;
      
      validate();
      revalidate();
      repaint();
      scrollRectToVisible(visibleRect);
   }
   public void zoomOut() {
      final Rectangle visibleRect=getVisibleRect();
      final double oldWidth=getPreferredSize().getWidth();

      zoom*=(4.0f/5.0f);

      for(PartView s: partViews) {
         s.adjust((Graphics2D)getGraphics());  
      }

      final double newWidth=getPreferredSize().getWidth();
      final double newHeight=getPreferredSize().getHeight();

      final double ratio=newWidth / oldWidth;
      if(newWidth<visibleRect.getWidth()) {
         visibleRect.x=0;
      } else {
         visibleRect.x=(int) (((visibleRect.getX() - (visibleRect.getWidth()
               / ratio - visibleRect.getWidth()) / 2)) * ratio);   
      }
      if(newHeight<visibleRect.getHeight()) {
         visibleRect.x=0;
      } else {
         visibleRect.y=(int) (((visibleRect.getY() - (visibleRect.getHeight()
               / ratio - visibleRect.getHeight()) / 2)) * ratio);   
      }
      
      validate();
      revalidate();
      repaint();
      scrollRectToVisible(visibleRect);
   }
   public void zoomNormal() {
      zoom=1.0f;
      validate();
      revalidate();
      repaint();
   }
   
   private int getLengthFromBeat(double b) { //len in beat
      return Note.WHOLE_LENGTH / 4; //>>>>>>>>>>>>>>>>>>>>>>>>>>>>
      /*
      final int beatLength=Note.WHOLE_LENGTH/score.getDenominator();
      if(b>=1) {
         final int rb=(int)(Math.ceil(b));
         System.err.println(rb);
         return rb*beatLength;
      } else {
         return Note.WHOLE_LENGTH/4; //>>>
      }
      */
   }
   
   
   private int getClosestPitch(double pitch) {
      final int basePitch=score.getKeySignature().getPitch(4);
      final double diff=pitch-MainFrame.pitchProfile.getPitchTable(0);
      final double newPitch=basePitch+diff;
      //>>> find closest key?
      int res=Math.round((float)newPitch);
      if(res>127) res=127;
      if(res<0) res=0;
      //System.err.println(res);
      return res;
   }
   
   @Override
   public void gotPitch(double freq, double len) {
      int finalPitch=0; 
      
      if(SoundAnalyzer.settings.enableCompensation) {
         finalPitch=
            //(int)Math.round(60-MainFrame.pitchProfile.get(0)+Util.frequencyToPitch(freq));
         //MainFrame.pitchProfile.getClosestPitch(Util.frequencyToPitch(freq));
         getClosestPitch(Util.frequencyToPitch(freq));
         //(int)Math.round(Util.frequencyToPitch(freq));
         //Util.frequencyToPitch(freq, MainFrame.pitchProfile.get(0),60);
         //Util.frequencyToPitch(freq+263-MainFrame.pitchProfile.get(0)); //>>> this is wrong
         //Util.frequencyToPitch(freq)+(60-Util.frequencyToPitch(mainFrame.pitchProfile.get(0))); //: not bad
         //Util.frequencyToPitch(freq)+SoundToNoteDialog.settings.compensation;
      } else {
         finalPitch=(int)Math.round(Util.frequencyToPitch(freq));
         System.err.println(Util.frequencyToPitch(freq));
      }
      //System.err.println(freq+":"+finalPitch);
      final int[] lenDot=findClosestRegularLengthDot(msToLength(len));
      if(lenDot[0]==0) return;
      score.get(staticCursor.partIndex).add(
            //new Note((int) finalPitch, lenDot[0], lenDot[1]), staticCursor.index);
            new Note((int) finalPitch, Note.WHOLE_LENGTH/4), staticCursor.noteIndex);
      staticCursor.noteIndex++;   
   }
   private int msToLength(double ms) {
      //System.err.println(ms*score.tempo/60000.0);
      return (int)Math.round((Note.WHOLE_LENGTH/4) * (ms*score.getTempo()/60000.0));
   }
   private int[] findClosestRegularLengthDot(int len) {
      if(len>Note.WHOLE_LENGTH) return new int[] {Note.WHOLE_LENGTH, 0};
      for(int i=1; i<=128; i*=2) {
         int length=Note.WHOLE_LENGTH/i;
         if(length==len) {
            return new int[] {length, 0};
         } else if(length<len) {
            int d=2, dot=0;
            for(int j=0; j<Note.MAX_DOT; j++) {
               if(length+length/d>len) break;
               length+=length/d;
               dot++;
               d*=2;
            }
            if(Note.WHOLE_LENGTH/i*2<=Note.WHOLE_LENGTH && 
                  len-length < Note.WHOLE_LENGTH/i*2-len) {
               return new int[] {Note.WHOLE_LENGTH/i, dot};
            } else {
               return new int[] {Note.WHOLE_LENGTH/i*2, 0};
            }
         }
      }
      if(len>Note.WHOLE_LENGTH/128-len) {
         return new int[] {Note.WHOLE_LENGTH/128, 0};   
      } else {
         return new int[] {0, 0};
      }
      
   }
   
   @Override
   public void scoreChanged(ScoreChange e) {
      if(e instanceof AddPartChange) {
         final AddPartChange event=(AddPartChange) e;
         addPartView(new StaffPartView(this, event.part), event.index);
         if(event.index < staticCursor.partIndex) staticCursor.partIndex++;
      } else if(e instanceof RemovePartChange) {
         final RemovePartChange event=(RemovePartChange) e;
         removePartView(event.index);
         if(event.index <= staticCursor.partIndex) staticCursor.partIndex--;
      } else if(e instanceof KeySignatureChange) {
         partViews.clear();
         for(int i=0; i<score.partCount(); i++) {
            addPartView(new StaffPartView(this, score.get(i)), i);
         }
         validate(); //>>>need to call this?
      } else if(e instanceof AddNoteChange) {
         final AddNoteChange ane=(AddNoteChange)e;
         if(ane.part==score.get(staticCursor.partIndex) && 
               ane.index < staticCursor.noteIndex) {
            staticCursor.noteIndex++;
            /*if(selection<0 ) {
               selection--;
            }*/ //>>>
         }
      } else if(e instanceof RemoveNoteChange) {
         final RemoveNoteChange rne=(RemoveNoteChange)e;
         if(rne.part==score.get(staticCursor.partIndex) && 
               rne.index < staticCursor.noteIndex) {
            staticCursor.noteIndex--;
            /*if(selection<0) {
               selection++;
            } else if(selection>0) {
               selection--;
            }*///>>> determine whether rne.index inside selection
         }
         //selection=0;
      } 
      
      if(!score.isValidInsertPath(cursor)) cursor.setBy(0, 0);
      if(!score.isValidInsertPath(staticCursor)) {
         staticCursor.setBy(0, 0);
         selection=0;
      }
      
      //[ move selection only if it's it's invalid;
      final int sStart=getSelectionStartIndex();
      final int sLen=getAbsSelectionLength();
      if(sStart<0 || sStart>score.get(staticCursor.partIndex).noteCount() ||
            sStart+sLen<0 || sStart+sLen>score.get(staticCursor.partIndex).noteCount()) {
         selection=0;
      }
      
      validate();
      revalidate();
      repaint(); //>>>
   }
   /*public static void main(String[] args) {
      final JFrame jf=new JFrame();
      final Sheet sheet=new Sheet();
      //sheet.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      jf.add(new JScrollPane(sheet));
      jf.pack();
      jf.setSize(400, 300);
      jf.setVisible(true);
   }*/
   
   public void changePartViewType(int partIndex, Class<? extends PartView> type) {
      if(partViews.get(partIndex).getClass() == type) return;
      final PartView s=partViews.remove(partIndex);
      if(type == NumPartView.class) {
         partViews.add(partIndex, new NumPartView(ScoreView.this, score.get(partIndex)));
      } else if(type == StaffPartView.class){
         partViews.add(partIndex, new StaffPartView(ScoreView.this, score.get(partIndex)));
      } else if(type == GridPartView.class) {
         partViews.add(partIndex, new GridPartView(ScoreView.this, score.get(partIndex)));
      } else {
         throw new RuntimeException();
      }
      validate();
      revalidate();
      repaint();
   }
   
   
   public void cut() {
      if(this.getAbsSelectionLength()>0) {
         MainFrame.clipBoard.clear();
         final int left=this.getSelectionStartIndex();
         for(int i=0; i<this.getAbsSelectionLength(); i++) {
            MainFrame.clipBoard.add(new Note(score.get(this.staticCursor.partIndex).get(left+i)));
         }
         score.get(this.staticCursor.partIndex).remove(left, this.getAbsSelectionLength());
         this.staticCursor.noteIndex=left;
         this.selection=0;
      }
   }
   public void copy() {
      if(this.getAbsSelectionLength()>0) {
         MainFrame.clipBoard.clear();
         final int left=this.getSelectionStartIndex();
         for(int i=0; i<this.getAbsSelectionLength(); i++) {
            MainFrame.clipBoard.add(new Note(score.get(this.staticCursor.partIndex).get(left+i)));
         }
         //score.get(this.cursor.partIndex).remove(left, this.getAbsSelectionLength());
      }
   }
   public void paste() {
      final Note[] copy=new Note[MainFrame.clipBoard.size()];
      for(int i=0; i<MainFrame.clipBoard.size(); i++) {
         copy[i]=new Note(MainFrame.clipBoard.get(i));
      }
      score.get(this.staticCursor.partIndex).add(copy, this.staticCursor.noteIndex);
      this.staticCursor.noteIndex+=copy.length;
      this.selection=0; //>>> remove selection while pasting
   }
   public void export(String path) {
      try {
         final Sequence sequence=score.toSequence();
         //System.err.println(Arrays.toString(MidiSystem.getMidiFileTypes(sequence)));
         MidiSystem.write(sequence, 1, new File(path));
      } catch(IOException e1) {
         e1.printStackTrace();
      } catch(InvalidMidiDataException e1) {
         e1.printStackTrace();
      }
   }
   public void removeSelection() {
      if(this.getAbsSelectionLength()>0) {
         final int left=this.getSelectionStartIndex();
         score.get(this.staticCursor.partIndex).remove(left, this.getAbsSelectionLength());
         this.staticCursor.noteIndex=left;
         this.selection=0;
      }
   }
   
   //[ mouse adapters ============================================================================
   public class MouseAdapterChanger extends MouseAdapter {
      private final ScoreViewAdapter insertAdapter=new InsertAdapter();
      private final ScoreViewAdapter selectAdapter=new SelectAdapter();
      
      private boolean isInserting() {
         return (Boolean)MainFrame.insertAction.getValue(Action.SELECTED_KEY);
      }
      public final void mouseClicked(MouseEvent e) {
         if(isInserting()) {
            insertAdapter.mouseClicked(e);
         } else {
            selectAdapter.mouseClicked(e);
         }
      }
      @Override
      public final void mouseEntered(MouseEvent e) {
         if(isInserting()) {
            insertAdapter.mouseEntered(e);
         } else {
            selectAdapter.mouseEntered(e);
         }
      }
      @Override
      public final void mouseExited(MouseEvent e) {
         if(isInserting()) {
            insertAdapter.mouseExited(e);
         } else {
            selectAdapter.mouseExited(e);
         }
      }
      @Override
      public final void mousePressed(MouseEvent e) {
         if(isInserting()) {
            insertAdapter.mousePressed(e);
         } else {
            selectAdapter.mousePressed(e);
         }
      }
      @Override
      public final void mouseReleased(MouseEvent e) {
         if(isInserting()) {
            insertAdapter.mouseReleased(e);
         } else {
            selectAdapter.mouseReleased(e);
         }
      }
      @Override
      public final void mouseWheelMoved(MouseWheelEvent e) {
         if(isInserting()) {
            insertAdapter.mouseWheelMoved(e);
         } else {
            selectAdapter.mouseWheelMoved(e);
         }
      }
      @Override
      public final void mouseDragged(MouseEvent e) {
         if(isInserting()) {
            insertAdapter.mouseDragged(e);
         } else {
            selectAdapter.mouseDragged(e);
         }
      }
      @Override
      public final void mouseMoved(MouseEvent e) {
         if(isInserting()) {
            insertAdapter.mouseMoved(e);
         } else {
            selectAdapter.mouseMoved(e);
         }
      }
   }
   public class ScoreViewAdapter extends MouseAdapter {
//      private boolean enabled=true;
//      public void setEnabled(boolean b) { enabled=b; }
//      @Override
      
      
      
      //[ for overrides
//      public void mClicked(MouseEvent e) {
//         
//      }
//      public void mEntered(MouseEvent e) {
//         
//      }
//      public void mExited(MouseEvent e) {
//         
//      }
//      public void mPressed(MouseEvent e) {
//         
//      }
//      public void mReleased(MouseEvent e) {
//         
//      }
//      public void mWheelMoved(MouseWheelEvent e) {
//         
//      }
//      public void mDragged(MouseEvent e) {
//         
//      }
//      public void mMoved(MouseEvent e) {
//         
//      }
      
      int getStaffIndex(int x, int y) {
         for(int i=0; i<partViews.size(); i++) {
            final PartView t=partViews.get(i);
            if(t.contains(x, y)) {
               return i;
            }
         }
         return -1;
      }
      public Pair<Path, Integer> getPath(int x, int y) {
         Path path=new Path();
         
         //[ find staff
         final int staffIndex=getStaffIndex(x, y);
         if(staffIndex<0 || staffIndex>=partViews.size()) {
            path.partIndex=-1;
            return null;
         }
         final PartView s=partViews.get(staffIndex);
         path.partIndex=staffIndex;
         
         //[ find note
         float currentX=s.x()+s.HEAD_END_MARK;
         int cursorX=-1;
         if(x>=currentX) {
            for (int j = 0; j < s.getPart().noteCount(); j++) { //>>> improve this to visible notes
               final Note n=s.getPart().get(j);
               final float noteW=s.getNoteWidth(n);
               final float step=noteW+s.getNoteGap();
               //System.err.println(noteW);
               final float diff=x-currentX;
               if(diff>=0 && diff<noteW+s.getNoteGap()) {
                  //inside a note
                  if(diff<step/2.0) { //left
                     path.noteIndex=j;
                     cursorX=(int)currentX;
                  } else {            //right
                     path.noteIndex=j+1;
                     cursorX=(int)(currentX+step);
                  }
                  break;
               }
               currentX+=step;
            }
            if(cursorX==-1) {
               cursorX=(int)currentX;
               path.noteIndex=s.getPart().noteCount();
            }
         } else {
            cursorX=(int)currentX;
            path.noteIndex=0;
         }
         return new Pair<Path, Integer>(path, cursorX);
      }
    
   }
   public class InsertAdapter extends ScoreViewAdapter { //bind to complex-Staff
      int lastX=-1;
      int lastY=-1;
      private int lastDX, lastDY;
      private StaffPartView lastStaff;
      private String lastSym=null;
      
      private boolean isDirty=false;
      private Color xorColor=Color.green;
      
      //final InputAdapter inputAdapter;
      private Note dummyNote=new Note(0, Note.WHOLE_LENGTH/4);
      //final InsertAdapter insertAdapter=new InsertAdapter();
      /*DisplayAdapter(InputAdapter ia) {
         inputAdapter=ia;
      }*/
      /*public void mouseExited(MouseEvent e) {
         final Graphics2D g=(Graphics2D)getGraphics();
         if(g==null) return;
         for(int i=0; i<staffs.size(); i++) {
            final Staff s=staffs.get(i);
            if(s.contains(e.getX(), e.getY())) {
               drawCursorInfo(g, s, lastX, lastY);
               break;
            }
         }
      }*/
      private void clearLast() {
         lastX=-1;
         lastY=-1;
         //lastStaff=null;
      }
      
      @Override
      public void mouseWheelMoved(MouseWheelEvent e) {
         final int rot=e.getWheelRotation();
         if(e.isShiftDown()) {
            if(rot>0) {
               for(int i=0; i<rot; i++) {
                  dummyNote.dot--;
               }
               if(dummyNote.dot<0) {
                  dummyNote.dot=0;
               }
            } else if(rot<0) { //[ forward
               for(int i=0; i<-rot; i++) {
                  dummyNote.dot++;
               }
               if(dummyNote.dot>Note.MAX_DOT) {
                  dummyNote.dot=Note.MAX_DOT;
               }
            }
         } else {
            if(rot>0) {
               for(int i=0; i<rot; i++) {
                  dummyNote.length/=2;
               }
               if(dummyNote.length<Note.WHOLE_LENGTH/128) {
                  dummyNote.length=Note.WHOLE_LENGTH/128;
               }
            } else if(rot<0) { //[ forward
               for(int i=0; i<-rot; i++) {
                  dummyNote.length*=2;
               }
               if(dummyNote.length>Note.WHOLE_LENGTH) dummyNote.length=Note.WHOLE_LENGTH;
            }  
         }
System.err.println("1/"+(double)Note.WHOLE_LENGTH/dummyNote.length+"+ "+dummyNote.dot+" dots");
         mouseMoved(e);
      }
   
      public void mousePressed(MouseEvent e) {
         //clearLast();
         if(isDirty) {
            final Graphics2D g=(Graphics2D)getGraphics();
            if(g==null) return;
            clearCursorInfo(g); //: clear old info
            isDirty=false;
         }
         if(e.getButton()==MouseEvent.BUTTON1) {
            staticCursor.setBy(cursor);
            for(int i=0; i<partViews.size(); i++) {
               final PartView gs=partViews.get(i);
               if(gs.contains(e.getX(), e.getY())) {
                  if(!(gs instanceof StaffPartView)) return;
                  final StaffPartView s=(StaffPartView) gs;
                  final int pos=s.getPosByY(e.getY()-s.y());
                  //System.err.println(s.positions.get(pos));
                  if(dummyNote.pitch>=0) {
                     score.get(i).add(
                        new Note(s.positions.get(pos)+s.sharpFlats[pos], 
                                 dummyNote.length, dummyNote.dot), s.scoreView.staticCursor.noteIndex);
                  } else {
                     score.get(i).add(
                           new Note(-1, dummyNote.length, dummyNote.dot), s.scoreView.staticCursor.noteIndex);
                  }
                  selection=0;
                  break;
               }
            }   
         } else {
            if(dummyNote.pitch>=0) dummyNote.pitch=-1;
            else dummyNote.pitch=0; //>>> 0 is not good
         }
         //mouseMoved(e);
      }
      public void mouseExited(MouseEvent e) {
         if(isDirty) {
            final Graphics2D g=(Graphics2D)getGraphics();
            if(g==null) return;
            clearCursorInfo(g); //: clear old info
            isDirty=false;
         }
      }
      public void mouseMoved(MouseEvent e) {
         final Graphics2D g=(Graphics2D)getGraphics();
         if(g==null) return;
         
         /*//[ find staff
         final int staffIndex=getStaffIndex(e.getX(), e.getY());
         if(staffIndex<0 || staffIndex>=staffs.size()) return;
         final PartView gs=staffs.get(staffIndex);
         if(!(gs instanceof StaffPartView)) return;
         final StaffPartView s=(StaffPartView) gs;*/
         
         
         final Pair<Path, Integer> p=getPath(e.getX(), e.getY());
         if(p==null) return;  
         if(!score.isValidInsertPath(p.getLeft())) {
            System.err.println(p.getLeft().partIndex+" - "+p.getLeft().noteIndex);
            return; //>>> check this later
         }
//System.err.println(p.getLeft().partIndex);            
         cursor.setBy(p.getLeft().partIndex, p.getLeft().noteIndex);
         
         //[ find x
         int newX=p.getRight();
         //int newX=updateCursor(s, e, g);
         
         final PartView pv=partViews.get(p.getLeft().partIndex);
         if(!(pv instanceof StaffPartView)) return;
         final StaffPartView s=(StaffPartView)pv;
         
         //[ find y
         final int newPos=s.getPosByY(e.getY()-s.y());
         final int newY=s.getYByPos(newPos)+s.y();
         
         if(isDirty) { //clean old
            clearCursorInfo(g);
         }
         //drawCursorInfo(g, e.getX(), e.getY());
         /*drawCursorInfo(g, s, e.getX(), e.getY());
         if(lastX>=0 && lastY>=0) {
            drawCursorInfo(g, lastX, lastY); //: clear old info
            clearLast();
         }*/
         g.setXORMode(xorColor);
         final String noteSym=s.getSonataSymbol(
               dummyNote.pitch, dummyNote.length, newPos);
         //System.err.println(noteSym);
         final Font oldFont=g.getFont();
         g.setFont(s.sonataFont);
         g.drawString(noteSym, newX, newY); //>>> x is not correct
         lastSym=noteSym;
         lastDX=newX;
         lastDY=newY;
         isDirty=true;
         lastStaff=s;
         lastX=e.getX();
         lastY=e.getY();
         g.setFont(oldFont);
      }
      
      //private Staff lastStaff=null;
      private void clearCursorInfo(Graphics2D g) { //>>> improve this
         g.setXORMode(xorColor);
         final Font oldFont=g.getFont();
         g.setFont(lastStaff.sonataFont);
         g.drawString(lastSym, lastDX, lastDY);
         g.setFont(oldFont);
      }
   }
   
   public class SelectAdapter extends ScoreViewAdapter {
      private boolean buttonPressed=false;
      
      @Override
      public void mouseClicked(MouseEvent e) {
//         if(e.getClickCount()==2) {
//            final Pair<Path, Integer> r=getPath(e.getX(), e.getY());
//            if(r==null) return;
//            final Path path=r.getLeft();
//            if(!score.isValidInsertPath(path)) return;
//            
//            final PartView s=staffs.remove(path.partIndex);
//            if(s instanceof StaffPartView) {
//               staffs.add(path.partIndex, new NumPartView(ScoreView.this, score.get(path.partIndex)));
//            } else {
//               staffs.add(path.partIndex, new StaffPartView(ScoreView.this, score.get(path.partIndex)));
//            }
//            revalidate();
//            repaint();
//         }
      }
      @Override
      public void mouseMoved(MouseEvent e) {
         final Graphics2D g=(Graphics2D)getGraphics();
         if(g==null) return;
         
         final Pair<Path, Integer> r=getPath(e.getX(), e.getY());
         if(r==null) return;
         final Path path=r.getLeft();
         if(!score.isValidInsertPath(path)) return;
         //final int pathX=r.getRight();

         if(buttonPressed) { 
            selection=cursor.noteIndex-path.noteIndex;
            staticCursor.setBy(r.getLeft());
         } else {
            //if(getAbsSelectionLength()<=0) {
            cursor.setBy(path);
            //}
         }
         repaint();
         //System.err.print(cursor);
         //System.err.println(selection);
      }
      
      @Override
      public void mouseDragged(MouseEvent e) {
         mouseMoved(e);
      }
      
      @Override
      public void mousePressed(MouseEvent e) {
         //mMoved(e);
         buttonPressed=true;
         
         final Pair<Path, Integer> r=getPath(e.getX(), e.getY());
         if(r==null) return;
         if(!score.isValidInsertPath(r.getLeft())) return; //>>> check this later
         staticCursor.setBy(r.getLeft());
         selection=0;
         repaint();
      }
      @Override
      public void mouseReleased(MouseEvent e) {
         buttonPressed=false;
         repaint();
      }
      
//      @Override
//      public void setEnabled(boolean arg0) {
//         super.setEnabled(arg0);
//         /*if(!arg0) {
//            selection=0;
//         }*/
//      }
   }
   
   class KeyInputAdapter extends KeyAdapter {
      private Note dummyNote=new Note(60, Note.WHOLE_LENGTH/4);
      private boolean inputMode=false;
      @Override
      public void keyPressed(KeyEvent e) {
         //System.err.println(e.getKeyCode()+" pressed.");
         if(e.getKeyCode()==KeyEvent.VK_LEFT) {
            if(inputMode) {
               if(e.isShiftDown()) {
                  if(dummyNote.dot>0) dummyNote.dot--;
               } else {
                  dummyNote.length/=2;   
               }
               if(dummyNote.length<Note.WHOLE_LENGTH/128) {
                  dummyNote.length=Note.WHOLE_LENGTH/128;
               }
               return;
            }
            //>>> modify note
            if(staticCursor.noteIndex>0) {
               staticCursor.noteIndex--;
               if(e.isShiftDown()) {
                  selection++;
               } else {
                  selection=0;
               }
            }
         } else if(e.getKeyCode()==KeyEvent.VK_RIGHT) {
            if(inputMode) {
               if(e.isShiftDown()) {
                  if(dummyNote.dot<Note.MAX_DOT) dummyNote.dot++;
               } else {
                  dummyNote.length*=2;
               }
               if(dummyNote.length>Note.WHOLE_LENGTH) {
                  dummyNote.length=Note.WHOLE_LENGTH;
               }
               return;
            }
            if(staticCursor.noteIndex<score.get(staticCursor.partIndex).noteCount()) {
               staticCursor.noteIndex++;
               if(e.isShiftDown()) {
                  selection--;
               } else {
                  selection=0;
               }
            }
         } else if(e.getKeyCode()==KeyEvent.VK_UP) {
            if(inputMode) {
               if(e.isShiftDown()) {
                  dummyNote.pitch++;
               } else {
                  dummyNote.pitch+=12;
               }
               if(dummyNote.pitch>=128) {
                  dummyNote.pitch=127;
               }
               return;
            }
            if(staticCursor.partIndex>0) {
               staticCursor.partIndex--;
               if(staticCursor.noteIndex>score.get(staticCursor.partIndex).noteCount()) {
                  staticCursor.noteIndex=score.get(staticCursor.partIndex).noteCount();
               }
               selection=0;
            }
         } else if(e.getKeyCode()==KeyEvent.VK_DOWN) {
            if(inputMode) {
               if(e.isShiftDown()) {
                  dummyNote.pitch--;
               } else {
                  dummyNote.pitch-=12;   
               }
               if(dummyNote.pitch<0) {
                  dummyNote.pitch=0;
               }
               return;
            }
            if(staticCursor.partIndex<score.partCount()-1) {
               staticCursor.partIndex++;
               if(staticCursor.noteIndex>score.get(staticCursor.partIndex).noteCount()) {
                  staticCursor.noteIndex=score.get(staticCursor.partIndex).noteCount();
               }
               selection=0;
            }
         } else if(e.getKeyCode()==KeyEvent.VK_HOME) {
            if(staticCursor.noteIndex>0) {
               if(e.isShiftDown()) {
                  selection+=staticCursor.noteIndex;
               } else {
                  selection=0;
               }
               staticCursor.noteIndex=0;
               System.err.println("jump to start");
            }
         } else if(e.getKeyCode()==KeyEvent.VK_END) {
            if(staticCursor.noteIndex<score.get(staticCursor.partIndex).noteCount()) {
               if(e.isShiftDown()) {
                  selection-=score.get(staticCursor.partIndex).noteCount()-staticCursor.noteIndex;
               } else {
                  selection=0;
               }
               staticCursor.noteIndex=score.get(staticCursor.partIndex).noteCount();
               System.err.println("jump to end");
            }
         } else if(Character.isDigit(e.getKeyChar())) { //[ start input
            final int input=Character.getNumericValue(e.getKeyChar());
            if(input<0 || input>7) return;
            if(input==0) {
               dummyNote.pitch=-1;
            } else {
               dummyNote.pitch=score.getKeySignature().getPitch(4)+NumPartView.numPitchIndex[input-1];
            }
            inputMode=true;
            //mainFrame.keyInputStatus.append(String.valueOf(input)); //>>>
            System.err.println(""+input);
         } else if(e.getKeyCode()==KeyEvent.VK_BACK_SPACE) { 
            if(getAbsSelectionLength()>0) {
               removeSelection();
            } else {
               if(staticCursor.noteIndex>0) {
                  score.get(staticCursor.partIndex).remove(staticCursor.noteIndex-1);
               }
            }
         } else if(e.getKeyCode()==KeyEvent.VK_ESCAPE) {
            inputMode=false;
         } else if(e.getKeyCode()==KeyEvent.VK_DELETE) {
            if(getAbsSelectionLength()>0) {
               removeSelection();
            } else {
               if(staticCursor.noteIndex<score.get(staticCursor.partIndex).noteCount()) {
                  score.get(staticCursor.partIndex).remove(staticCursor.noteIndex);
               }   
            }
         } else if(e.getKeyCode()==KeyEvent.VK_Z && e.isControlDown()) {
            if(score.canUndo()) {
               score.undo();
               System.err.println("undo");
            }
         } else if(e.getKeyCode()==KeyEvent.VK_Y && e.isControlDown()) {
            if(score.canRedo()) {
               score.redo();
               System.err.println("redo");
            }
         } else if(e.getKeyCode()==KeyEvent.VK_X && e.isControlDown()) {
            cut();
         } else if(e.getKeyCode()==KeyEvent.VK_C && e.isControlDown()) {
            copy();
         } else if(e.getKeyCode()==KeyEvent.VK_V && e.isControlDown()) {
            paste();
         } else if(e.getKeyCode()==KeyEvent.VK_ENTER || e.getKeyCode()==KeyEvent.VK_SPACE) {
            if(!inputMode) return;
            score.get(staticCursor.partIndex).add(new Note(dummyNote), staticCursor.noteIndex);
            selection=0;
            staticCursor.noteIndex++; //>>> no need to save old index?
            inputMode=false;
            dummyNote=new Note(60, Note.WHOLE_LENGTH/4);
         } 
         Rectangle cursorBounds=getStaticCursorBounds();
         scrollRectToVisible(cursorBounds);
         repaint();
         
         /*if(inputMode) {
            System.err.println(dummyNote);
         }*/
      }
   }

   @Override
   public void close() {
      // TODO Auto-generated method stub
      
   }

   
   private final long[] lastPitchTime=new long[128];
   private long lastNoteOff=System.currentTimeMillis();
   {
      for(int i=0; i < lastPitchTime.length; i++) {
         lastPitchTime[i]=-1;
      }
   }
   private static class TimeNote implements Comparable<TimeNote> {
      private final long time;
      private final Note note;
      TimeNote(long t, Note n) {
         note=n;
         time=t;
      }
      @Override
      public int compareTo(TimeNote that) {
         return (int)(this.time-that.time);
      }
   }
   private final SortedSet<TimeNote> buffer=new TreeSet<TimeNote>();
   //private long lastMessageTime=-1; //>>> lastMsg[128]
   @Override
   public synchronized void send(MidiMessage message, long timeStamp) { //>>> rest notes
      if(message instanceof ShortMessage) {
         final ShortMessage sm=(ShortMessage) message;
         final long currentTime=System.currentTimeMillis();
         if(sm.getCommand()==0x90 && sm.getData2()>0) { //note on
            final int pitch=sm.getData1();
            if(lastNoteOff>=0) {
               //[ create rest
               final long timeElapsed=
                  currentTime-lastNoteOff;
               if(timeElapsed>0) {
                  final int[] lenDot=findClosestRegularLengthDot(msToLength(timeElapsed));
                  if(lenDot[0]>0) {
                     final Note rest=new Note(-1, lenDot[0], lenDot[1]);
                     buffer.add(new TimeNote(lastNoteOff, rest));
                  }
               }
            }
            lastPitchTime[pitch]=currentTime;
            
         } else if(sm.getCommand()==0x80 || 
               (sm.getCommand()==0x90 && sm.getData2()==0)) { //note off
            final int pitch=sm.getData1();
            if(lastPitchTime[pitch]<0) return;
            
            //[ create note
            final long timeElapsed=
               currentTime-lastPitchTime[pitch];
            final int[] lenDot=findClosestRegularLengthDot(msToLength(timeElapsed));
            final Note note=new Note(pitch, lenDot[0], lenDot[1]);
            
            //[ count noteOn
            int noteOnCount=0;
            for(int i=0; i < lastPitchTime.length; i++) {
               if(lastPitchTime[i]>=0) noteOnCount++;
            }
            
            if(noteOnCount>1) { //has other open notes
               if(lenDot[0]>0) buffer.add(new TimeNote(lastPitchTime[pitch], note));
            } else {
               //[ clear buffer
               for(TimeNote tn: buffer) {
                  score.get(staticCursor.partIndex).add(
                        tn.note,
                        staticCursor.noteIndex);
                  staticCursor.noteIndex++;
               }
               buffer.clear();
               
               //[ add last
               if(lenDot[0]>0) {
                  score.get(staticCursor.partIndex).add(
                        note,
                        staticCursor.noteIndex);
                  staticCursor.noteIndex++;
               }
            }
            
            lastPitchTime[pitch]=-1;
            lastNoteOff=currentTime;
         }
         
      }
      
   }

   @Override
   public void notePlayed(Path path) {
      this.partViews.get(path.partIndex).notePlayed(path.noteIndex);
      repaint();
   }
}

