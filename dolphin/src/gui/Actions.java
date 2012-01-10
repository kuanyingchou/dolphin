package gui;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import api.audio.SoundAnalyzer;
import api.midi.DumpSequence;
import api.midi.InDeviceManager;
import api.model.Note;
import api.model.Part;
import api.model.Score;
import api.util.Util;

import view.GridPartView;
import view.NumPartView;
import view.ScoreView;
import view.StaffPartView;


class ExportAction extends AbstractAction {
   private final MainFrame mainFrame;
   
   public ExportAction(MainFrame mf) {
      mainFrame=mf;
      putValue(Action.NAME, "Export...");
      putValue(Action.LARGE_ICON_KEY, Util.getImageIcon("images/document_export.png"));
      putValue(Action.SMALL_ICON, Util.getImageIcon("images/export.png"));
   }
   
   @Override
   public void actionPerformed(ActionEvent e) {
      //System.err.println("saving...");
      final JFileChooser jfc=new JFileChooser(Util.curDir);
      jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
      jfc.setFileFilter(
            new FileNameExtensionFilter("midi files", "mid"));
      final int ret=jfc.showSaveDialog(mainFrame);
      
      if(ret==JFileChooser.APPROVE_OPTION) {
         final File file=jfc.getSelectedFile();
         if(file.exists()) {
            final int r=JOptionPane.showConfirmDialog(
                  mainFrame, "Override "+file.getName()+"?");
            if(r!=JOptionPane.YES_OPTION) {
               return;
            }
         }
         final ViewPane vf=((ViewPane)mainFrame.desktop.getSelectedComponent());
         if(vf==null) return; //>>>
         final ScoreView scoreView=vf.scoreView;
         scoreView.export(file.getPath());
         
      }
   }
}
class ImportAction extends AbstractAction {
   private final MainFrame mainFrame;
   
   public ImportAction(MainFrame mf) {
      mainFrame=mf;
      putValue(Action.NAME, "Import...");
      putValue(Action.LARGE_ICON_KEY, Util.getImageIcon("images/fileimport.png"));
      putValue(Action.SMALL_ICON, Util.getImageIcon("images/import.png"));
   }
   @Override
   public void actionPerformed(ActionEvent e) {
      //System.err.println("saving...");
      mainFrame.importMidi();
   }
}
class NewAction extends AbstractAction {
   private final MainFrame mainFrame;
   public NewAction(MainFrame mf) {
      mainFrame=mf;
      putValue(Action.NAME, "New");
      putValue(Action.LARGE_ICON_KEY, Util.getImageIcon("images/new.gif"));
      putValue(Action.SMALL_ICON, Util.getImageIcon("images/new.png"));
   }
   @Override
   public void actionPerformed(ActionEvent e) {
      final Score score=new Score();
      score.add(new Part());
      score.setLogging(true);
      mainFrame.addSheet(score);
   }
}

class NewViewAction extends AbstractAction{
   private final MainFrame mainFrame;
   
   public NewViewAction(MainFrame mf) {
      mainFrame=mf;
      putValue(Action.NAME, "New Tab");
//      putValue(Action.LARGE_ICON_KEY, Util.getImageIcon("images/newview.png"));
      putValue(Action.SMALL_ICON, Util.getImageIcon("images/newview-.png"));
   }
   
   @Override
   public void actionPerformed(ActionEvent e) {
      final ViewPane vf=((ViewPane)mainFrame.desktop.getSelectedComponent());
      if(vf==null) return;
      final ScoreView scoreView=vf.scoreView;
      
      mainFrame.addSheet(scoreView.score);
   }
   
}
/*class PlayAction extends AbstractAction {
   private final MainFrame mainFrame;
   
   public PlayAction(MainFrame mf) {
      mainFrame=mf;
      putValue(Action.NAME, "Play");
      putValue(Action.LARGE_ICON_KEY, new ImageIcon("Play24.gif"));
      //putValue(Action.SMALL_ICON, new ImageIcon("Copy16.gif"));
   } 
   public void actionPerformed(ActionEvent e) {
      final ViewFrame vf=((ViewFrame)mainFrame.desktop.getSelectedComponent());
      if(vf==null) return;
      final Sheet scoreView=vf.scoreView;
      scoreView.score.play();
   }
}*/

class InsertAction extends AbstractAction {
   //private final MainFrame mainFrame;
   //public enum MouseMode { INSERT, SELECT}
   //public boolean enabled=false;
   
   public InsertAction() {
      //mainFrame=mf;
      //putValue(Action.NAME, "Insert");
      putValue(Action.SELECTED_KEY, false);
      putValue(Action.LARGE_ICON_KEY, Util.getImageIcon("images/insert.png"));
      //putValue(Action.SMALL_ICON, new ImageIcon("Copy16.gif"));
   }
   
   @Override
   public void actionPerformed(ActionEvent e) {
//      final ViewPane vf=((ViewPane)mainFrame.desktop.getSelectedComponent());
//      if(vf==null) return;
//      final ScoreView scoreView=vf.scoreView;
//      final Score score=scoreView.score;
//      //System.err.println(scoreView.cursor);
//      
//      scoreView.insertAdapter.setEnabled(true);
//      scoreView.selectAdapter.setEnabled(false);
//      scoreView.selection=0;
//      putValue(Action.SELECTED_KEY, !((Boolean)getValue(Action.SELECTED_KEY)));
    
      
      
   }
}
//class SelectAction extends AbstractAction {
//   private final MainFrame mainFrame;
//   
//   public SelectAction(MainFrame mf) {
//      mainFrame=mf;
//      putValue(Action.NAME, "Select");
//      //putValue(Action.LARGE_ICON_KEY, Util.getImageIcon("images/select.png"));
//      //putValue(Action.SMALL_ICON, new ImageIcon("Copy16.gif"));
//   }
//   
//   @Override
//   public void actionPerformed(ActionEvent e) {
//      final ViewPane vf=((ViewPane)mainFrame.desktop.getSelectedComponent());
//      if(vf==null) return;
//      final ScoreView scoreView=vf.scoreView;
//      final Score score=scoreView.score;
//      //System.err.println(scoreView.cursor);
//      scoreView.insertAdapter.setEnabled(false);
//      scoreView.selectAdapter.setEnabled(true);
//   }
//}
class ShowScorePropertyAction extends AbstractAction {
   private final MainFrame mainFrame;
   
   public ShowScorePropertyAction(MainFrame mf) {
      mainFrame=mf;
      putValue(Action.NAME, "Score Properties...");
      putValue(Action.LARGE_ICON_KEY, Util.getImageIcon("images/score.png"));
      putValue(Action.SMALL_ICON, Util.getImageIcon("images/scoreproper.png"));
   }
   
   @Override
   public void actionPerformed(ActionEvent e) {
      final ViewPane vf=((ViewPane)mainFrame.desktop.getSelectedComponent());
      if(vf==null) return;
      final ScoreView scoreView=vf.scoreView;
      final Score score=scoreView.score;
      //System.err.println(scoreView.cursor);
      final ScoreDialog dialog=new ScoreDialog(mainFrame, score);
      dialog.setVisible(true);
      /*final JLabel tab=(JLabel)mainFrame.desktop.getTabComponentAt(
            mainFrame.desktop.getSelectedIndex());
      tab.setText(score.getTitle());*/
      //mainFrame.desktop.setTitleAt(mainFrame.desktop.getSelectedIndex(), 
      //      score.title);
      //System.err.println(mainFrame.desktop.getTitleAt(mainFrame.desktop.getSelectedIndex()));
   }
}
class ShowPitchProfileAction extends AbstractAction {
   private final MainFrame mainFrame;
   public ShowPitchProfileAction(MainFrame mf) {
      mainFrame=mf;
      putValue(Action.NAME, "Profile...");
      //putValue(Action.LARGE_ICON_KEY, Util.getImageIcon("images/pitch.png"));
      //putValue(Action.SMALL_ICON, new ImageIcon("Copy16.gif"));
   }
   
   @Override
   public void actionPerformed(ActionEvent e) {
      final PitchProfileDialog dialog=new PitchProfileDialog(mainFrame);
      dialog.setVisible(true);
   }
}









//>>>>>>>>>>>>>>move to ScoreView






class UndoAction extends AbstractAction {
   private final MainFrame mainFrame;
   
   public UndoAction(MainFrame mf) {
      mainFrame=mf;
      putValue(Action.NAME, "Undo");
      putValue(Action.LARGE_ICON_KEY, Util.getImageIcon("images/undo.png"));
      putValue(Action.SMALL_ICON, Util.getImageIcon("images/undo1.png"));
   }
   
   @Override
   public void actionPerformed(ActionEvent e) {
      final ViewPane vf=((ViewPane)mainFrame.desktop.getSelectedComponent());
      if(vf==null) return;
      final ScoreView scoreView=vf.scoreView;
      if(scoreView.score.canUndo()) scoreView.score.undo();
      scoreView.selection=0;
      /*if(!scoreView.score.canRedo()) this.setEnabled(false);
      if(scoreView.score.canUndo()) mainFrame.undoAction.setEnabled(true);*/
   }
}
class RedoAction extends AbstractAction{
   private final MainFrame mainFrame;
   
   public RedoAction(MainFrame mf) {
      mainFrame=mf;
      putValue(Action.NAME, "Redo");
      putValue(Action.LARGE_ICON_KEY, Util.getImageIcon("images/redo.png"));
      putValue(Action.SMALL_ICON, Util.getImageIcon("images/redo1.png"));
   }
   
   @Override
   public void actionPerformed(ActionEvent e) {
      final ViewPane vf=((ViewPane)mainFrame.desktop.getSelectedComponent());
      if(vf==null) return;
      final ScoreView scoreView=vf.scoreView;
      if(scoreView.score.canRedo()) scoreView.score.redo();
      scoreView.selection=0;
   }
   
}
class UndoAllAction extends AbstractAction {
   private final MainFrame mainFrame;
   
   public UndoAllAction(MainFrame mf) {
      mainFrame=mf;
      putValue(Action.NAME, "Undo All");
      putValue(Action.LARGE_ICON_KEY, Util.getImageIcon("images/undoall.gif"));
      putValue(Action.SMALL_ICON, Util.getImageIcon("images/undoall.png"));
   }
   
   @Override
   public void actionPerformed(ActionEvent e) {
      final ViewPane vf=((ViewPane)mainFrame.desktop.getSelectedComponent());
      if(vf==null) return;
      final ScoreView scoreView=vf.scoreView;
      while(scoreView.score.canUndo()) scoreView.score.undo();
      scoreView.selection=0;
      /*if(!scoreView.score.canRedo()) this.setEnabled(false);
      if(scoreView.score.canUndo()) mainFrame.undoAction.setEnabled(true);*/
   }
}
class RedoAllAction extends AbstractAction{
   private final MainFrame mainFrame;
   
   public RedoAllAction(MainFrame mf) {
      mainFrame=mf;
      putValue(Action.NAME, "Redo All");
      putValue(Action.LARGE_ICON_KEY, Util.getImageIcon("images/redoall.gif"));
      putValue(Action.SMALL_ICON, Util.getImageIcon("images/redoall.png"));
   }
   
   @Override
   public void actionPerformed(ActionEvent e) {
      final ViewPane vf=((ViewPane)mainFrame.desktop.getSelectedComponent());
      if(vf==null) return;
      final ScoreView scoreView=vf.scoreView;
      while(scoreView.score.canRedo()) scoreView.score.redo();
      scoreView.selection=0;
   }
   
}
class ZoomInAction extends AbstractAction{
   private final MainFrame mainFrame;
   
   public ZoomInAction(MainFrame mf) {
      mainFrame=mf;
      putValue(Action.NAME, "Larger");
      putValue(Action.LARGE_ICON_KEY, Util.getImageIcon("images/zoom_in.png"));
      putValue(Action.SMALL_ICON, Util.getImageIcon("images/zoomin.png"));
   }
   
   @Override
   public void actionPerformed(ActionEvent e) {
      final ViewPane vf=((ViewPane)mainFrame.desktop.getSelectedComponent());
      if(vf==null) return;
      final ScoreView scoreView=vf.scoreView;
      scoreView.zoomIn();
   }
   
}
class ZoomOutAction extends AbstractAction{
   private final MainFrame mainFrame;
   
   public ZoomOutAction(MainFrame mf) {
      mainFrame=mf;
      putValue(Action.NAME, "Smaller");
      putValue(Action.LARGE_ICON_KEY, Util.getImageIcon("images/zoom_out.png"));
      putValue(Action.SMALL_ICON, Util.getImageIcon("images/zoomout.png"));
   }
   
   @Override
   public void actionPerformed(ActionEvent e) {
      final ViewPane vf=((ViewPane)mainFrame.desktop.getSelectedComponent());
      if(vf==null) return;
      final ScoreView scoreView=vf.scoreView;
      scoreView.zoomOut();
   }
}
class ZoomNormalAction extends AbstractAction{
   private final MainFrame mainFrame;
   
   public ZoomNormalAction(MainFrame mf) {
      mainFrame=mf;
      putValue(Action.NAME, "Normal");
      //putValue(Action.LARGE_ICON_KEY, Util.getImageIcon("images/zoom_in.png"));
      //putValue(Action.SMALL_ICON, Util.getImageIcon("images/zoomin.png"));
   }
   
   @Override
   public void actionPerformed(ActionEvent e) {
      final ViewPane vf=((ViewPane)mainFrame.desktop.getSelectedComponent());
      if(vf==null) return;
      final ScoreView scoreView=vf.scoreView;
      scoreView.zoomNormal();
   }
   
}
class RemoveAction extends AbstractAction {
   private final MainFrame mainFrame;
   
   public RemoveAction(MainFrame mf) {
      mainFrame=mf;
      putValue(Action.NAME, "Remove");
      putValue(Action.LARGE_ICON_KEY, Util.getImageIcon("images/trashcan.gif"));
      putValue(Action.SMALL_ICON, Util.getImageIcon("images/trash.png"));
   }
   
   @Override
   public void actionPerformed(ActionEvent e) {
      final ViewPane vf=((ViewPane)mainFrame.desktop.getSelectedComponent());
      if(vf==null) return;
      final ScoreView scoreView=vf.scoreView;
      final Score score=scoreView.score;
      //System.err.println(scoreView.cursor);
      scoreView.removeSelection();
   }
}
class CutAction extends AbstractAction {
   private final MainFrame mainFrame;
   
   public CutAction(MainFrame mf) {
      mainFrame=mf;
      putValue(Action.NAME, "Cut");
      putValue(Action.LARGE_ICON_KEY, Util.getImageIcon("images/editcut.png"));
      putValue(Action.SMALL_ICON, Util.getImageIcon("images/cut.png"));
   }
   
   @Override
   public void actionPerformed(ActionEvent e) {
      final ViewPane vf=((ViewPane)mainFrame.desktop.getSelectedComponent());
      if(vf==null) return;
      final ScoreView scoreView=vf.scoreView;
      final Score score=scoreView.score;
      //System.err.println(scoreView.cursor);
      scoreView.cut();
   }
}
class CopyAction extends AbstractAction {
   private final MainFrame mainFrame;
   
   public CopyAction(MainFrame mf) {
      mainFrame=mf;
      putValue(Action.NAME, "Copy");
      putValue(Action.LARGE_ICON_KEY, Util.getImageIcon("images/editcopy.gif"));
      putValue(Action.SMALL_ICON, Util.getImageIcon("images/copy.png"));
   }
   
   @Override
   public void actionPerformed(ActionEvent e) {
      final ViewPane vf=((ViewPane)mainFrame.desktop.getSelectedComponent());
      if(vf==null) return;
      final ScoreView scoreView=vf.scoreView;
      final Score score=scoreView.score;
      //System.err.println(scoreView.cursor);
      scoreView.copy();
   }
}

class PasteAction extends AbstractAction {
   private final MainFrame mainFrame;
   
   public PasteAction(MainFrame mf) {
      mainFrame=mf;
      putValue(Action.NAME, "Paste");
      putValue(Action.LARGE_ICON_KEY, Util.getImageIcon("images/editpaste.png"));
      putValue(Action.SMALL_ICON, Util.getImageIcon("images/paste.png"));
   }
   
   @Override
   public void actionPerformed(ActionEvent e) {
      final ViewPane vf=((ViewPane)mainFrame.desktop.getSelectedComponent());
      if(vf==null) return;
      final ScoreView scoreView=vf.scoreView;
      final Score score=scoreView.score;
      //System.err.println(scoreView.cursor);
      scoreView.paste();
   }
}

class NoteHigherAction extends AbstractAction {
   private final MainFrame mainFrame;
   
   public NoteHigherAction(MainFrame mf) {
      mainFrame=mf;
      putValue(Action.NAME, "Higher");
      putValue(Action.LARGE_ICON_KEY, Util.getImageIcon("images/higher.png"));
      putValue(Action.SMALL_ICON, Util.getImageIcon("images/high.png"));
   }
   
   @Override
   public void actionPerformed(ActionEvent e) {
      final ViewPane vf=((ViewPane)mainFrame.desktop.getSelectedComponent());
      if(vf==null) return;
      final ScoreView scoreView=vf.scoreView;
      final Score score=scoreView.score;
      //System.err.println(scoreView.cursor);
      if(scoreView.getAbsSelectionLength()>0) {
         final int oldSelection=scoreView.selection;
         final int oldIndex=scoreView.staticCursor.index;
         score.setComboMode(true);
         final int left=scoreView.getSelectionStartIndex();
         for(int i=scoreView.getAbsSelectionLength()-1; i>=0; i--) {
            final Note n=score.get(scoreView.staticCursor.partIndex).get(left+i);
            score.get(scoreView.staticCursor.partIndex).remove(left+i);
            final Note newNote=new Note(n); //copy is necessary, because events will keep it
            if(!n.isRest()) {
               newNote.pitch+=1;
               if(newNote.pitch>=128) newNote.pitch=127;
            }
            score.get(scoreView.staticCursor.partIndex).add(newNote, left+i);
         }
         score.setComboMode(false);
         //scoreView.staticCursor.index=scoreView.getSelectionStartIndex();
         scoreView.staticCursor.index=oldIndex;
         scoreView.selection=oldSelection;
         
      }
   }
}
class NoteMuchHigherAction extends AbstractAction {
   private final MainFrame mainFrame;
   
   public NoteMuchHigherAction(MainFrame mf) {
      mainFrame=mf;
      putValue(Action.NAME, "+octave");
      putValue(Action.LARGE_ICON_KEY, Util.getImageIcon("images/octaveup.png"));
      putValue(Action.SMALL_ICON, Util.getImageIcon("images/+ovtave+.png"));
   }
   
   @Override
   public void actionPerformed(ActionEvent e) {
      final ViewPane vf=((ViewPane)mainFrame.desktop.getSelectedComponent());
      if(vf==null) return;
      final ScoreView scoreView=vf.scoreView;
      final Score score=scoreView.score;
      //System.err.println(scoreView.cursor);
      if(scoreView.getAbsSelectionLength()>0) {
         final int oldSelection=scoreView.selection;
         final int oldIndex=scoreView.staticCursor.index;
         score.setComboMode(true);
         final int left=scoreView.getSelectionStartIndex();
         for(int i=scoreView.getAbsSelectionLength()-1; i>=0; i--) {
            final Note n=score.get(scoreView.staticCursor.partIndex).get(left+i);
            score.get(scoreView.staticCursor.partIndex).remove(left+i);
            final Note newNote=new Note(n); //copy is necessary, because events will keep it
            if(!n.isRest()) {
               newNote.pitch+=12;
               if(newNote.pitch>=128) newNote.pitch=127;
            }
            score.get(scoreView.staticCursor.partIndex).add(newNote, left+i);
         }
         score.setComboMode(false);
         //scoreView.staticCursor.index=scoreView.getSelectionStartIndex();
         scoreView.staticCursor.index=oldIndex;
         scoreView.selection=oldSelection;
         
      }
   }
}
class NoteMuchLowerAction extends AbstractAction {
   private final MainFrame mainFrame;
   
   public NoteMuchLowerAction(MainFrame mf) {
      mainFrame=mf;
      putValue(Action.NAME, "-octave");
      putValue(Action.LARGE_ICON_KEY, Util.getImageIcon("images/octavedown.png"));
      putValue(Action.SMALL_ICON, Util.getImageIcon("images/-octave-.png"));
   }
   
   @Override
   public void actionPerformed(ActionEvent e) {
      final ViewPane vf=((ViewPane)mainFrame.desktop.getSelectedComponent());
      if(vf==null) return;
      final ScoreView scoreView=vf.scoreView;
      final Score score=scoreView.score;
      //System.err.println(scoreView.cursor);
      if(scoreView.getAbsSelectionLength()>0) {
         final int oldSelection=scoreView.selection;
         final int oldIndex=scoreView.staticCursor.index;
         score.setComboMode(true);
         final int left=scoreView.getSelectionStartIndex();
         for(int i=scoreView.getAbsSelectionLength()-1; i>=0; i--) {
            final Note n=score.get(scoreView.staticCursor.partIndex).get(left+i);
            score.get(scoreView.staticCursor.partIndex).remove(left+i);
            final Note newNote=new Note(n); //copy is necessary, because events will keep it
            if(!n.isRest()) {
               newNote.pitch-=12;
               if(newNote.pitch>=128) newNote.pitch=127;
            }
            score.get(scoreView.staticCursor.partIndex).add(newNote, left+i);
         }
         score.setComboMode(false);
         //scoreView.staticCursor.index=scoreView.getSelectionStartIndex();
         scoreView.staticCursor.index=oldIndex;
         scoreView.selection=oldSelection;
         
      }
   }
}
class NoteLowerAction extends AbstractAction {
   private final MainFrame mainFrame;
   
   public NoteLowerAction(MainFrame mf) {
      mainFrame=mf;
      putValue(Action.NAME, "Lower");
      putValue(Action.LARGE_ICON_KEY, Util.getImageIcon("images/lower.png"));
      putValue(Action.SMALL_ICON, Util.getImageIcon("images/low.png"));
   }
   
   @Override
   public void actionPerformed(ActionEvent e) {
      final ViewPane vf=((ViewPane)mainFrame.desktop.getSelectedComponent());
      if(vf==null) return;
      final ScoreView scoreView=vf.scoreView;
      final Score score=scoreView.score;
      //System.err.println(scoreView.cursor);
      if(scoreView.getAbsSelectionLength()>0) {
         final int oldSelection=scoreView.selection;
         final int oldIndex=scoreView.staticCursor.index;
         
         score.setComboMode(true);
         final int left=scoreView.getSelectionStartIndex();
         for(int i=scoreView.getAbsSelectionLength()-1; i>=0; i--) {
            final Note n=score.get(scoreView.staticCursor.partIndex).get(left+i);
            score.get(scoreView.staticCursor.partIndex).remove(left+i);
            final Note newNote=new Note(n); //copy is necessary, because events will keep it
            if(!n.isRest()) {
               newNote.pitch-=1;
               if(newNote.pitch<0) newNote.pitch=0;
            }
            score.get(scoreView.staticCursor.partIndex).add(newNote, left+i);
         }
         score.setComboMode(false);
         //scoreView.staticCursor.index=scoreView.getSelectionStartIndex();
         scoreView.staticCursor.index=oldIndex;
         scoreView.selection=oldSelection;
      }
   }
}
class NoteLongerAction extends AbstractAction {
   private final MainFrame mainFrame;
   
   public NoteLongerAction(MainFrame mf) {
      mainFrame=mf;
      putValue(Action.NAME, "Longer");
      putValue(Action.LARGE_ICON_KEY, Util.getImageIcon("images/longer.png"));
      putValue(Action.SMALL_ICON, Util.getImageIcon("images/long.png"));
   }
   
   @Override
   public void actionPerformed(ActionEvent e) {
      final ViewPane vf=((ViewPane)mainFrame.desktop.getSelectedComponent());
      if(vf==null) return;
      final ScoreView scoreView=vf.scoreView;
      final Score score=scoreView.score;
      //System.err.println(scoreView.cursor);
      if(scoreView.getAbsSelectionLength()>0) {
         final int oldSelection=scoreView.selection;
         final int oldIndex=scoreView.staticCursor.index;
         score.setComboMode(true);
         final int left=scoreView.getSelectionStartIndex();
         for(int i=scoreView.getAbsSelectionLength()-1; i>=0; i--) {
            final Note n=score.get(scoreView.staticCursor.partIndex).get(left+i);
            score.get(scoreView.staticCursor.partIndex).remove(left+i);
            final Note newNote=new Note(n); //copy is necessary, because events will keep it 
            newNote.length*=2;
            if(newNote.length>Note.WHOLE_LENGTH) newNote.length=Note.WHOLE_LENGTH;
            score.get(scoreView.staticCursor.partIndex).add(newNote, left+i);
         }
         score.setComboMode(false);
         //scoreView.staticCursor.index=scoreView.getSelectionStartIndex();
         scoreView.staticCursor.index=oldIndex;
         scoreView.selection=oldSelection;
      }
   }
}
class NoteShorterAction extends AbstractAction {
   private final MainFrame mainFrame;
   
   public NoteShorterAction(MainFrame mf) {
      mainFrame=mf;
      putValue(Action.NAME, "Shorter");
      putValue(Action.LARGE_ICON_KEY, Util.getImageIcon("images/shorter.png"));
      putValue(Action.SMALL_ICON, Util.getImageIcon("images/short.png"));
   }
   
   @Override
   public void actionPerformed(ActionEvent e) {
      final ViewPane vf=((ViewPane)mainFrame.desktop.getSelectedComponent());
      if(vf==null) return;
      final ScoreView scoreView=vf.scoreView;
      final Score score=scoreView.score;
      //System.err.println(scoreView.cursor);
      if(scoreView.getAbsSelectionLength()>0) {
         final int oldSelection=scoreView.selection;
         final int oldIndex=scoreView.staticCursor.index;
         score.setComboMode(true);
         final int left=scoreView.getSelectionStartIndex();
         for(int i=scoreView.getAbsSelectionLength()-1; i>=0; i--) {
            final Note n=score.get(scoreView.staticCursor.partIndex).get(left+i);
            score.get(scoreView.staticCursor.partIndex).remove(left+i);
            final Note newNote=new Note(n); //copy is necessary, because events will keep it 
            newNote.length/=2;
            if(newNote.length<Note.WHOLE_LENGTH/128) newNote.length=Note.WHOLE_LENGTH/128;
            if(newNote.length>Note.WHOLE_LENGTH) newNote.length=Note.WHOLE_LENGTH;
            score.get(scoreView.staticCursor.partIndex).add(newNote, left+i);
         }
         score.setComboMode(false);
         //scoreView.staticCursor.index=scoreView.getSelectionStartIndex();
         scoreView.staticCursor.index=oldIndex;
         scoreView.selection=oldSelection;
      }
   }
}
class NoteAddDotAction extends AbstractAction {
   private final MainFrame mainFrame;
   
   public NoteAddDotAction(MainFrame mf) {
      mainFrame=mf;
      putValue(Action.NAME, "Add Dot");
      putValue(Action.LARGE_ICON_KEY, Util.getImageIcon("images/add_dot.png"));
      putValue(Action.SMALL_ICON, Util.getImageIcon("images/adddot.png"));
   }
   
   @Override
   public void actionPerformed(ActionEvent e) {
      final ViewPane vf=((ViewPane)mainFrame.desktop.getSelectedComponent());
      if(vf==null) return;
      final ScoreView scoreView=vf.scoreView;
      final Score score=scoreView.score;
      //System.err.println(scoreView.cursor);
      if(scoreView.getAbsSelectionLength()>0) {
         final int oldSelection=scoreView.selection;
         final int oldIndex=scoreView.staticCursor.index;
         score.setComboMode(true);
         final int left=scoreView.getSelectionStartIndex();
         for(int i=scoreView.getAbsSelectionLength()-1; i>=0; i--) {
            final Note n=score.get(scoreView.staticCursor.partIndex).get(left+i);
            score.get(scoreView.staticCursor.partIndex).remove(left+i);
            final Note newNote=new Note(n); //copy is necessary, because events will keep it 
            
            if(newNote.dot<Note.MAX_DOT) newNote.dot++;
            
            score.get(scoreView.staticCursor.partIndex).add(newNote, left+i);
         }
         score.setComboMode(false);
         //scoreView.staticCursor.index=scoreView.getSelectionStartIndex();
         scoreView.staticCursor.index=oldIndex;
         scoreView.selection=oldSelection;
      }
   }
}
class NoteRemoveDotAction extends AbstractAction {
   private final MainFrame mainFrame;
   
   public NoteRemoveDotAction(MainFrame mf) {
      mainFrame=mf;
      putValue(Action.NAME, "Remove Dot");
      putValue(Action.LARGE_ICON_KEY, Util.getImageIcon("images/remove_dot.png"));
      putValue(Action.SMALL_ICON, Util.getImageIcon("images/removedot.png"));
   }
   
   @Override
   public void actionPerformed(ActionEvent e) {
      final ViewPane vf=((ViewPane)mainFrame.desktop.getSelectedComponent());
      if(vf==null) return;
      final ScoreView scoreView=vf.scoreView;
      final Score score=scoreView.score;
      //System.err.println(scoreView.cursor);
      if(scoreView.getAbsSelectionLength()>0) {
         final int oldSelection=scoreView.selection;
         final int oldIndex=scoreView.staticCursor.index;
         score.setComboMode(true);
         final int left=scoreView.getSelectionStartIndex();
         for(int i=scoreView.getAbsSelectionLength()-1; i>=0; i--) {
            final Note n=score.get(scoreView.staticCursor.partIndex).get(left+i);
            score.get(scoreView.staticCursor.partIndex).remove(left+i);
            final Note newNote=new Note(n); //copy is necessary, because events will keep it 
            
            if(newNote.dot>0) newNote.dot--;
            
            score.get(scoreView.staticCursor.partIndex).add(newNote, left+i);
         }
         score.setComboMode(false);
         //scoreView.staticCursor.index=scoreView.getSelectionStartIndex();
         scoreView.staticCursor.index=oldIndex;
         scoreView.selection=oldSelection;
      }
   }
}
class NoteTieAction extends AbstractAction {
   private final MainFrame mainFrame;
   
   public NoteTieAction(MainFrame mf) {
      mainFrame=mf;
      putValue(Action.NAME, "Tie");
      putValue(Action.LARGE_ICON_KEY, Util.getImageIcon("images/tie.png"));
      //putValue(Action.SMALL_ICON, new ImageIcon("Copy16.gif"));
   }
   
   @Override
   public void actionPerformed(ActionEvent e) {
      final ViewPane vf=((ViewPane)mainFrame.desktop.getSelectedComponent());
      if(vf==null) return;
      final ScoreView scoreView=vf.scoreView;
      final Score score=scoreView.score;
      //System.err.println(scoreView.cursor);
      if(scoreView.getAbsSelectionLength()>1) {
         final int oldSelection=scoreView.selection;
         final int oldIndex=scoreView.staticCursor.index;
         score.setComboMode(true);
         final int left=scoreView.getSelectionStartIndex();
         final int firstPitch=score.get(scoreView.staticCursor.partIndex).get(left).pitch;
         int tie=0;
         for(int i=scoreView.getAbsSelectionLength()-1; i>=0; i--) {
            final Note n=score.get(scoreView.staticCursor.partIndex).get(left+i);
            score.get(scoreView.staticCursor.partIndex).remove(left+i);
            final Note newNote=new Note(n); //copy is necessary, because events will keep it
            newNote.pitch=firstPitch; //>>> ties have same pitch?
            if(tie==0) newNote.tie=--tie;
            if(i==0) newNote.tie=1; //first element
            score.get(scoreView.staticCursor.partIndex).add(newNote, left+i);
         }
         score.setComboMode(false);
         //scoreView.staticCursor.index=scoreView.getSelectionStartIndex();
         scoreView.staticCursor.index=oldIndex;
         scoreView.selection=oldSelection;
      }
   }
}
class NoteTripletAction extends AbstractAction {
   private final MainFrame mainFrame;
   
   public NoteTripletAction(MainFrame mf) {
      mainFrame=mf;
      putValue(Action.NAME, "Triplet");
      putValue(Action.LARGE_ICON_KEY, Util.getImageIcon("images/triplet.png"));
      //putValue(Action.SMALL_ICON, new ImageIcon("Copy16.gif"));
   }
   
   @Override
   public void actionPerformed(ActionEvent e) {
      final ViewPane vf=((ViewPane)mainFrame.desktop.getSelectedComponent());
      if(vf==null) return;
      final ScoreView scoreView=vf.scoreView;
      final Score score=scoreView.score;
      //System.err.println(scoreView.cursor);
      if(scoreView.getAbsSelectionLength()==3) { //>>> only 3 ?
         final int oldSelection=scoreView.selection;
         final int oldIndex=scoreView.staticCursor.index;
         score.setComboMode(true);
         final int left=scoreView.getSelectionStartIndex();
         for(int i=scoreView.getAbsSelectionLength()-1; i>=0; i--) {
            final Note n=score.get(scoreView.staticCursor.partIndex).get(left+i);
            score.get(scoreView.staticCursor.partIndex).remove(left+i);
            final Note newNote=new Note(n); //copy is necessary, because events will keep it 
            newNote.isTripletElement=true;
            score.get(scoreView.staticCursor.partIndex).add(newNote, left+i);
         }
         score.setComboMode(false);
         //scoreView.staticCursor.index=scoreView.getSelectionStartIndex();
         scoreView.staticCursor.index=oldIndex;
         scoreView.selection=oldSelection;
      }
   }
}

class AddPartAction extends AbstractAction {
   private final MainFrame mainFrame;

   public AddPartAction(MainFrame mf) {
      mainFrame=mf;
      putValue(Action.NAME, "Add New Part");
      putValue(Action.LARGE_ICON_KEY, Util.getImageIcon("images/addpart.png"));
      putValue(Action.SMALL_ICON, Util.getImageIcon("images/addpart_small.png"));
   }
    
   public void actionPerformed(ActionEvent e) {
      final ViewPane vf=((ViewPane)mainFrame.desktop.getSelectedComponent());
      if(vf==null) return;
      final ScoreView scoreView=vf.scoreView;
      final Score score=scoreView.score;
      score.add(new Part(), ++scoreView.staticCursor.partIndex);
   }
}
class RemovePartAction extends AbstractAction {
   private final MainFrame mainFrame;
   public RemovePartAction(MainFrame mf) {
      mainFrame=mf;
      putValue(Action.NAME, "Remove Part");
      putValue(Action.LARGE_ICON_KEY, Util.getImageIcon("images/removepart.png"));
      putValue(Action.SMALL_ICON, Util.getImageIcon("images/removepart_small.png"));
   }
   public void actionPerformed(ActionEvent e) {
      final ViewPane vf=((ViewPane)mainFrame.desktop.getSelectedComponent());
      if(vf==null) return;
      final ScoreView scoreView=vf.scoreView;
      final Score score=scoreView.score;
      final int index=scoreView.staticCursor.partIndex;
      if(score.partCount()==1) return; //>>> empty score?
      if(index>=0 && index<score.partCount()) score.remove(index);
   }
}
class ToNumPartViewAction extends AbstractAction {
   private final MainFrame mainFrame;
   public ToNumPartViewAction(MainFrame mf) {
      mainFrame=mf;
      putValue(Action.NAME, "Numerical View");
      //putValue(Action.LARGE_ICON_KEY, new ImageIcon("Delete24.gif"));
   }
   public void actionPerformed(ActionEvent e) {
      final ViewPane vf=((ViewPane)mainFrame.desktop.getSelectedComponent());
      if(vf==null) return;
      final ScoreView scoreView=vf.scoreView;
      final Score score=scoreView.score;
      final int index=scoreView.staticCursor.partIndex;
      if(index>=0 && index<score.partCount()) {
         scoreView.changePartViewType(index, NumPartView.class);
      }
   }
}
class ToStaffPartViewAction extends AbstractAction {
   private final MainFrame mainFrame;
   public ToStaffPartViewAction(MainFrame mf) {
      mainFrame=mf;
      putValue(Action.NAME, "Staff View");
      //putValue(Action.LARGE_ICON_KEY, new ImageIcon("Delete24.gif"));
   }
   public void actionPerformed(ActionEvent e) {
      final ViewPane vf=((ViewPane)mainFrame.desktop.getSelectedComponent());
      if(vf==null) return;
      final ScoreView scoreView=vf.scoreView;
      final Score score=scoreView.score;
      final int index=scoreView.staticCursor.partIndex;
      if(index>=0 && index<score.partCount()) {
         scoreView.changePartViewType(index, StaffPartView.class);
      }
   }
}
class ToMessagePartViewAction extends AbstractAction {
   private final MainFrame mainFrame;
   public ToMessagePartViewAction(MainFrame mf) {
      mainFrame=mf;
      putValue(Action.NAME, "Grid View");
      putValue(Action.LARGE_ICON_KEY, Util.getImageIcon("images/gridview.png"));
   }
   public void actionPerformed(ActionEvent e) {
      final ViewPane vf=((ViewPane)mainFrame.desktop.getSelectedComponent());
      if(vf==null) return;
      final ScoreView scoreView=vf.scoreView;
      final Score score=scoreView.score;
      final int index=scoreView.staticCursor.partIndex;
      if(index>=0 && index<score.partCount()) {
         scoreView.changePartViewType(index, GridPartView.class);
      }
   }
}


class ShowPartDialogAction extends AbstractAction {
   private final MainFrame mainFrame;
   public ShowPartDialogAction(MainFrame mf) {
      mainFrame=mf;
      putValue(Action.NAME, "Part Properties...");
      putValue(Action.LARGE_ICON_KEY, Util.getImageIcon("images/partproperties.png"));
      putValue(Action.SMALL_ICON, Util.getImageIcon("images/partpro.png"));
   }
   public void actionPerformed(ActionEvent e) {
      final ViewPane vf=((ViewPane)mainFrame.desktop.getSelectedComponent());
      if(vf==null) return;
      final ScoreView scoreView=vf.scoreView;
      final Score score=scoreView.score;
      final int index=scoreView.staticCursor.partIndex;
      if(index>=0 && index<score.partCount()) {
         final PartDialog pd=new PartDialog(mainFrame, score.get(index));
         pd.setVisible(true);
      }
   }
}
class ShowSoundToNoteDialogAction extends AbstractAction {
   private final MainFrame mainFrame;
   public ShowSoundToNoteDialogAction(MainFrame mf) {
      mainFrame=mf;
      putValue(Action.NAME, "Sound-to-Note Preferences...");
      putValue(Action.LARGE_ICON_KEY, Util.getImageIcon("images/preference.png"));
   }
   public void actionPerformed(ActionEvent e) {
      SoundInputDialog d=new SoundInputDialog(mainFrame);
      d.setVisible(true);
   }
}
class NewWindowAction extends AbstractAction {
   private final MainFrame mainFrame;
   public NewWindowAction(MainFrame mf) {
      mainFrame=mf;
      putValue(Action.NAME, "New Window");
      //putValue(Action.LARGE_ICON_KEY, new ImageIcon("Delete24.gif"));
   }
   public void actionPerformed(ActionEvent e) {
      final ViewPane vf=((ViewPane)mainFrame.desktop.getSelectedComponent());
      if(vf==null) return;
      final ScoreView scoreView=vf.scoreView;
      final Score score=scoreView.score;
      
      final MainFrame nmf=new MainFrame();
      nmf.setLocation(mainFrame.getX()+50, mainFrame.getY()+50);
      nmf.setVisible(true);
      nmf.addSheet(scoreView.score);
   }
}
class ShowAboutDialogAction extends AbstractAction {
   private final MainFrame mainFrame;
   public ShowAboutDialogAction(MainFrame mf) {
      mainFrame=mf;
      putValue(Action.NAME, "About...");
      putValue(Action.SMALL_ICON, Util.getImageIcon("images/help.png"));
   }
   public void actionPerformed(ActionEvent e) {
      final AboutDialog aboutDialog=new AboutDialog(mainFrame);
      aboutDialog.setVisible(true);
   }
} 
class ShowInputDeviceDialogAction extends AbstractAction {
   private final MainFrame mainFrame;
   public ShowInputDeviceDialogAction(MainFrame mf) {
      mainFrame=mf;
      putValue(Action.NAME, "Show Input Device Dialog");
      //putValue(Action.LARGE_ICON_KEY, new ImageIcon("Delete24.gif"));
   }
   public void actionPerformed(ActionEvent e) {
      final InputDeviceDialog inDialog=new InputDeviceDialog(mainFrame);
      inDialog.setVisible(true);
   }
} 
class ToggleInputDeviceRecordAction extends AbstractAction {
   private final MainFrame mainFrame;
   public ToggleInputDeviceRecordAction(MainFrame mf) {
      mainFrame=mf;
      putValue(Action.NAME, "Record");
      //putValue(Action.LARGE_ICON_KEY, Util.getImageIcon("images/startrecording.png"));
      putValue(Action.SELECTED_KEY, false);
   }
   public void actionPerformed(ActionEvent e) {
      final ViewPane vf=((ViewPane)mainFrame.desktop.getSelectedComponent());
      if(vf==null) return;
      final ScoreView scoreView=vf.scoreView;
      final Score score=scoreView.score;
      if((Boolean)getValue(Action.SELECTED_KEY)) {
         InDeviceManager.instance.addReceiver(scoreView);   
      } else {
         InDeviceManager.instance.clearReceivers();
      }
   }
}  
class StopMIDIRecordAction extends AbstractAction {
   private final MainFrame mainFrame;
   public StopMIDIRecordAction(MainFrame mf) {
      mainFrame=mf;
      putValue(Action.NAME, "Stop Recording");
      //putValue(Action.LARGE_ICON_KEY, new ImageIcon("Delete24.gif"));
   }
   public void actionPerformed(ActionEvent e) {
      //MainFrame.inDeviceManager.setReceiver(null);
      //MainFrame.inDeviceManager.closeDevice();
      InDeviceManager.instance.clearReceivers();
      
   }
}
class ToggleInputDeviceEnableAction extends AbstractAction {
   private final MainFrame mainFrame;
   public ToggleInputDeviceEnableAction(MainFrame mf) {
      mainFrame=mf;
      putValue(Action.NAME, "Enable");
      putValue(Action.SELECTED_KEY, false);
      //putValue(Action.LARGE_ICON_KEY, new ImageIcon("Delete24.gif"));
   }
   public void actionPerformed(ActionEvent e) {
      if((Boolean)getValue(Action.SELECTED_KEY)) InDeviceManager.instance.enable();
      else InDeviceManager.instance.disable();
   }
}

class ToggleBasicToolBarAction extends AbstractAction {
   private final MainFrame mainFrame;
   public ToggleBasicToolBarAction(MainFrame mf) {
      mainFrame=mf;
      putValue(Action.NAME, "Basic");
      putValue(Action.SELECTED_KEY, true);
      //putValue(Action.LARGE_ICON_KEY, new ImageIcon("Delete24.gif"));
   }
   public void actionPerformed(ActionEvent e) {
      mainFrame.basicToolBar.setVisible(!mainFrame.basicToolBar.isVisible());
   }
}
class ToggleModifyToolBarAction extends AbstractAction {
   private final MainFrame mainFrame;
   public ToggleModifyToolBarAction(MainFrame mf) {
      mainFrame=mf;
      putValue(Action.NAME, "Modify");
      //putValue(Action.LARGE_ICON_KEY, new ImageIcon("Delete24.gif"));
   }
   public void actionPerformed(ActionEvent e) {
      mainFrame.modifyToolBar.setVisible(!mainFrame.modifyToolBar.isVisible());
   }
}
//class ToggleScoreToolBarAction extends AbstractAction {
//   private final MainFrame mainFrame;
//   public ToggleScoreToolBarAction(MainFrame mf) {
//      mainFrame=mf;
//      putValue(Action.NAME, "Score Tools");
//      //putValue(Action.LARGE_ICON_KEY, new ImageIcon("Delete24.gif"));
//   }
//   public void actionPerformed(ActionEvent e) {
//      mainFrame.scoreToolBar.setVisible(!mainFrame.scoreToolBar.isVisible());
//   }
//}
//class TogglePartToolBarAction extends AbstractAction {
//   private final MainFrame mainFrame;
//   public TogglePartToolBarAction(MainFrame mf) {
//      mainFrame=mf;
//      putValue(Action.NAME, "Part Tools");
//      //putValue(Action.LARGE_ICON_KEY, new ImageIcon("Delete24.gif"));
//   }
//   public void actionPerformed(ActionEvent e) {
//      mainFrame.partToolBar.setVisible(!mainFrame.partToolBar.isVisible());
//   }
//}
//class ToggleNoteToolBarAction extends AbstractAction {
//   private final MainFrame mainFrame;
//   public ToggleNoteToolBarAction(MainFrame mf) {
//      mainFrame=mf;
//      putValue(Action.NAME, "Note Tools");
//      //putValue(Action.LARGE_ICON_KEY, new ImageIcon("Delete24.gif"));
//   }
//   public void actionPerformed(ActionEvent e) {
//      mainFrame.noteToolBar.setVisible(!mainFrame.noteToolBar.isVisible());
//   }
//}
class TogglePlayToolBarAction extends AbstractAction {
   private final MainFrame mainFrame;
   public TogglePlayToolBarAction(MainFrame mf) {
      mainFrame=mf;
      putValue(Action.NAME, "Play");
      putValue(Action.SELECTED_KEY, false);
      //putValue(Action.LARGE_ICON_KEY, new ImageIcon("Delete24.gif"));
   }
   public void actionPerformed(ActionEvent e) {
      mainFrame.playToolBar.setVisible(!mainFrame.playToolBar.isVisible());
   }
}
class ToggleOutputDeviceToolBarAction extends AbstractAction {
   private final MainFrame mainFrame;
   public ToggleOutputDeviceToolBarAction(MainFrame mf) {
      mainFrame=mf;
      putValue(Action.NAME, "Output Device");
      putValue(Action.SELECTED_KEY, false); //>>>
      //putValue(Action.LARGE_ICON_KEY, new ImageIcon("Delete24.gif"));
   }
   public void actionPerformed(ActionEvent e) {
      mainFrame.outputDeviceToolBar.setVisible(!mainFrame.outputDeviceToolBar.isVisible());
   }
}
class ToggleKeyboardToolBarAction extends AbstractAction {
   private final MainFrame mainFrame;
   public ToggleKeyboardToolBarAction(MainFrame mf) {
      mainFrame=mf;
      putValue(Action.NAME, "Virtual Keyboard");
      //putValue(Action.LARGE_ICON_KEY, new ImageIcon("Delete24.gif"));
   }
   public void actionPerformed(ActionEvent e) {
      mainFrame.keyboardToolBar.setVisible(!mainFrame.keyboardToolBar.isVisible());
   }
}
class ToggleInputDeviceToolBarAction extends AbstractAction {
   private final MainFrame mainFrame;
   public ToggleInputDeviceToolBarAction(MainFrame mf) {
      mainFrame=mf;
      putValue(Action.NAME, "Input Device");
      //putValue(Action.LARGE_ICON_KEY, new ImageIcon("Delete24.gif"));
   }
   public void actionPerformed(ActionEvent e) {
      mainFrame.inputDeviceToolBar.setVisible(!mainFrame.inputDeviceToolBar.isVisible());
   }
}
class ToggleSoundInputToolBarAction extends AbstractAction {
   private final MainFrame mainFrame;
   public ToggleSoundInputToolBarAction(MainFrame mf) {
      mainFrame=mf;
      putValue(Action.NAME, "Sound Input");
      //putValue(Action.LARGE_ICON_KEY, new ImageIcon("Delete24.gif"));
   }
   public void actionPerformed(ActionEvent e) {
      mainFrame.soundInputToolBar.setVisible(!mainFrame.soundInputToolBar.isVisible());
   }
}
class ExitAction extends AbstractAction {
   private final MainFrame mainFrame;
   public ExitAction(MainFrame mf) {
      mainFrame=mf;
      putValue(Action.NAME, "Exit");
      //putValue(Action.LARGE_ICON_KEY, new ImageIcon("Delete24.gif"));
   }
   public void actionPerformed(ActionEvent e) {
      mainFrame.setVisible(false);
      mainFrame.dispose();
      System.exit(0); //>>> is there a better way?
   }
}

//[ experiments ========================================================================================
class StartSoundInputAction extends AbstractAction {
   private final MainFrame mainFrame;
   
   public StartSoundInputAction(MainFrame mf) {
      mainFrame=mf;
      putValue(Action.NAME, "Start Sound Input");
      putValue(Action.LARGE_ICON_KEY, Util.getImageIcon("images/startsound.png"));
      putValue(Action.SELECTED_KEY, false);
   }
   
   @Override
   public void actionPerformed(ActionEvent e) {
      final ViewPane vf=((ViewPane)mainFrame.desktop.getSelectedComponent());
      if(vf==null) return;
      final ScoreView scoreView=vf.scoreView;
      final Score score=scoreView.score;
      
      if(mainFrame.soundAnalyzer!=null) {
         mainFrame.soundAnalyzer.stopRecording();
      }
      mainFrame.soundAnalyzer=new SoundAnalyzer();
      mainFrame.soundAnalyzer.addFrequencyListener(scoreView);
      //mainFrame.soundAnalyzer.addIntensityListener(mainFrame.meter);
      mainFrame.soundAnalyzer.addIntensityListener(mainFrame.intensityHistory); //>>> a better place?
      //mainFrame.meter.setThreshold(SoundAnalyzer.settings.noiseLevel);
      mainFrame.soundAnalyzer.start(); //>>>check
      //mainFrame.addSheet(new Sheet(scoreView.score));
      
   }
}
class StopSoundInputAction extends AbstractAction {
   private final MainFrame mainFrame;
   
   public StopSoundInputAction(MainFrame mf) {
      mainFrame=mf;
      putValue(Action.NAME, "Stop Sound Input");
      putValue(Action.LARGE_ICON_KEY, Util.getImageIcon("images/stopsound.png"));
      //putValue(Action.SMALL_ICON, new ImageIcon("Copy16.gif"));
   }
   
   @Override
   public void actionPerformed(ActionEvent e) {
      /*final ViewFrame vf=((ViewFrame)mainFrame.desktop.getSelectedComponent());
      if(vf==null) return;
      final Sheet scoreView=vf.scoreView;
      final Score score=scoreView.score;*/
      if(mainFrame.soundAnalyzer==null) return;
      mainFrame.soundAnalyzer.stopRecording();
      //mainFrame.meter.setValue(0);
      
      
   }
}
class ShowScriptEditorAction extends AbstractAction {
   private final MainFrame mainFrame;
   
   public ShowScriptEditorAction(MainFrame mf) {
      mainFrame=mf;
      putValue(Action.NAME, "Script Editor");
      putValue(Action.LARGE_ICON_KEY, Util.getImageIcon("images/script.png"));
      //putValue(Action.SMALL_ICON, new ImageIcon("Copy16.gif"));
   }
   
   @Override
   public void actionPerformed(ActionEvent e) {
      final ViewPane vf=((ViewPane)mainFrame.desktop.getSelectedComponent());
      if(vf==null) return;
      final ScoreView scoreView=vf.scoreView;
      final Score score=scoreView.score;
      final ScriptEditor editor=new ScriptEditor();
      
      final JFrame jf=new JFrame("Script Editor");
      jf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      jf.add(editor);
      jf.pack();
      jf.setVisible(true);
      
      //editor.addBinding("_s", score);
      //editor.addBinding("_sv", scoreView);
      editor.addBinding("dp", mainFrame);
      editor.addBinding("_e", editor);
      //editor.addBinding("_f", jf);
      
      editor.execute("importPackage(Packages.api.model);", true);
      editor.execute("importPackage(Packages.gui);", true);
      editor.execute("importPackage(Packages.view);", true);
      //[ log for demo
      //editor.addLog("importPackage(Packages.api.model);");
      editor.addLog(
            "for(i=0; i<100; i++) {\n   dp.getScore().getPart(0).add(new Note(60));\n}");
      editor.addLog(
      "dp.perform(dp.undoAction);");
   }
}

class ShowStereoFieldEditorAction extends AbstractAction {
   private final MainFrame mainFrame;
   
   public ShowStereoFieldEditorAction(MainFrame mf) {
      mainFrame=mf;
      putValue(Action.NAME, "Stereo Field Editor");
      //putValue(Action.LARGE_ICON_KEY, Util.getImageIcon("images/script.png"));
      //putValue(Action.SMALL_ICON, new ImageIcon("Copy16.gif"));
   }
   
   @Override
   public void actionPerformed(ActionEvent e) {
      final ViewPane vf=((ViewPane)mainFrame.desktop.getSelectedComponent());
      if(vf==null) return;
      final ScoreView scoreView=vf.scoreView;
      final Score score=scoreView.score;
      final StereoFieldEditor editor=new StereoFieldEditor(mainFrame, score);
      
      
   }
}