package gui;
import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

//import tablet.TabletManager;
import view.ScoreView;
import api.audio.PitchProfile;
import api.audio.SoundAnalyzer;
import api.midi.BasicScorePlayer;
import api.midi.OutDeviceManager;
import api.midi.RealTimeScorePlayer;
import api.midi.ScorePlayer;
import api.model.Note;
import api.model.Part;
import api.model.Score;
import api.util.Util;

public class MainFrame extends JFrame {
      
   public static final String APP_NAME="Dolphin";//"Dolphin Music Editor";
   
   public static PitchProfile pitchProfile=PitchProfile.loadDefault();  
   public static SoundAnalyzer soundAnalyzer;
   
   //[ static because it is shared among multiple MainFrames
   public static final List<Note> clipBoard=new ArrayList<Note>();
   
   public final ScoresTabbedPane desktop=new ScoresTabbedPane(this); 
   
   final IntensityHistory intensityHistory=new IntensityHistory();
   
   public static final ScorePlayer player=new BasicScorePlayer(); //should it be here? 
   
   
   /*public static class KeyInputStatus extends JLabel { //>>>
      final StringBuilder sb=new StringBuilder();
      public void append(String s) {
         sb.append(s);
         setText(sb.toString());
      }
      public void clear() { 
         sb.setLength(0);
         setText("");
      }
   }
   public final KeyInputStatus keyInputStatus=new KeyInputStatus();*/
   //final PartPane partPane=new PartPane(this);
   //final TabBar tabBar=new TabBar();
   
   private static int frameCount=0; 
   
   
   
   //final PartPane partPane=new PartPane(this);
   
   public MainFrame() {
      //sheet.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      frameCount++;
      setTitle(APP_NAME);
      setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); //>>>>>>>>release resources, determine whether to shutdown
      //setIconImage(new ImageIcon("music.png").getImage());
      
      basicToolBar.setAlignmentX(LEFT_ALIGNMENT);
      //basicToolBar.setVisible(false);
      
      modifyToolBar.setAlignmentX(LEFT_ALIGNMENT);
      modifyToolBar.setVisible(false);
      
//      noteToolBar.setAlignmentX(LEFT_ALIGNMENT);
//      noteToolBar.setVisible(false);
//      
//      partToolBar.setAlignmentX(LEFT_ALIGNMENT);
//      partToolBar.setVisible(false);
//      
//      scoreToolBar.setAlignmentX(LEFT_ALIGNMENT);
//      scoreToolBar.setVisible(false);
      
      soundInputToolBar.setVisible(false);
      soundInputToolBar.setAlignmentX(LEFT_ALIGNMENT);
      
      final JPanel topToolPane=new JPanel();
      topToolPane.setLayout(new BoxLayout(topToolPane, BoxLayout.PAGE_AXIS));
      //topToolPane.setLayout(new FlowLayout(FlowLayout.LEADING));
      topToolPane.add(basicToolBar);
      topToolPane.add(modifyToolBar);
//      topToolPane.add(scoreToolBar);
//      topToolPane.add(partToolBar);
//      topToolPane.add(noteToolBar);
      topToolPane.add(soundInputToolBar);
      add(topToolPane, BorderLayout.NORTH);
      
      final JPanel centerPane=new JPanel();
      centerPane.setLayout(new BorderLayout());
      //centerPane.add(tabBar, BorderLayout.NORTH); //>>> do it later
      
      centerPane.add(desktop, BorderLayout.CENTER);
      //centerPane.add(new ToolBar2(this), BorderLayout.NORTH);

      add(centerPane, BorderLayout.CENTER);
//      final JSplitPane splitPane=new JSplitPane(
//            JSplitPane.HORIZONTAL_SPLIT, 
//            partPane, centerPane);
//      add(splitPane, BorderLayout.CENTER);
      
      
      //[ bottoms
      playToolBar.setAlignmentX(LEFT_ALIGNMENT);
      playToolBar.setVisible(true);
      
      outputDeviceToolBar.setAlignmentX(LEFT_ALIGNMENT);
      outputDeviceToolBar.setVisible(false);
      
      //keyboardToolBar.add(new JScrollPane(visualKeyboard));
      keyboardToolBar.add(new VisualKeyboardPane(this));
      keyboardToolBar.setAlignmentX(LEFT_ALIGNMENT);
      keyboardToolBar.setVisible(false);
      
      inputDeviceToolBar.setVisible(false);
      inputDeviceToolBar.setAlignmentX(LEFT_ALIGNMENT);
      
      final JPanel bottomToolPane=new JPanel();
      bottomToolPane.setLayout(new BoxLayout(bottomToolPane, BoxLayout.PAGE_AXIS));
      bottomToolPane.add(playToolBar);
      bottomToolPane.add(outputDeviceToolBar);
      bottomToolPane.add(keyboardToolBar);
      bottomToolPane.add(inputDeviceToolBar);
      add(bottomToolPane, BorderLayout.SOUTH);
      
      
      //[ menus
      final JMenu fileMenu=new JMenu("File");
      fileMenu.add(newAction);
      fileMenu.add(importAction);
      fileMenu.add(exportAction);
      fileMenu.addSeparator();
      fileMenu.add(exitAction);
      
      final JMenu editMenu=new JMenu("Edit");
      editMenu.add(undoAction);
      editMenu.add(redoAction);
      editMenu.addSeparator();
      editMenu.add(undoAllAction);
      editMenu.add(redoAllAction);
      editMenu.addSeparator();
      editMenu.add(removeAction);
      editMenu.add(cutAction);
      editMenu.add(copyAction);
      editMenu.add(pasteAction);
      //editMenu.add(showPitchProfileAction);
      //editMenu.add(showScorePropertyAction);
      
      final JMenu modifyMenu=new JMenu("Modify");
      modifyMenu.add(noteHigherAction);
      modifyMenu.add(noteLowerAction);
      modifyMenu.add(noteMuchHigherAction);
      modifyMenu.add(noteMuchLowerAction);
      modifyMenu.add(noteLongerAction);
      modifyMenu.add(noteShorterAction);
      modifyMenu.add(noteAddDotAction);
      modifyMenu.add(noteRemoveDotAction);
      modifyMenu.add(noteTieAction);
      modifyMenu.add(noteTripletAction);
      modifyMenu.addSeparator();
      modifyMenu.add(addPartAction);
      modifyMenu.add(removePartAction);
      modifyMenu.add(showPartDialogAction);
      modifyMenu.addSeparator();
      modifyMenu.add(showScorePropertyAction);
      
      final JMenu viewMenu=new JMenu("View");
      viewMenu.add(zoomInAction);
      viewMenu.add(zoomNormalAction);
      viewMenu.add(zoomOutAction);
      viewMenu.addSeparator();
      viewMenu.add(newTabAction);
      viewMenu.add(newWindowAction);
      viewMenu.addSeparator();
      viewMenu.add(toStaffPartViewAction);
      viewMenu.add(toNumPartViewAction);
      viewMenu.add(toGridPartViewAction);
//      final JMenu notationMenu=new JMenu("Part Notation");
//      notationMenu.add(toStaffPartViewAction);
//      notationMenu.add(toNumPartViewAction);
//      notationMenu.add(toMessagePartViewAction);
//      viewMenu.add(notationMenu);
      //viewMenu.addSeparator();
      
      
      final JMenu toolMenu=new JMenu("Tools");
      toolMenu.add(new JCheckBoxMenuItem(toggleBasicToolBarAction));
      toolMenu.add(new JCheckBoxMenuItem(toggleModifyToolBarAction));
//      windowMenu.add(new JCheckBoxMenuItem(toggleScoreToolBarAction));
//      windowMenu.add(new JCheckBoxMenuItem(togglePartToolBarAction));
//      windowMenu.add(new JCheckBoxMenuItem(toggleNoteToolBarAction));
      toolMenu.add(new JCheckBoxMenuItem(togglePlayToolBarAction));
      toolMenu.add(new JCheckBoxMenuItem(toggleOutputDeviceToolBarAction));
      toolMenu.add(new JCheckBoxMenuItem(toggleKeyboardToolBarAction));
      toolMenu.add(new JCheckBoxMenuItem(toggleInputDeviceToolBarAction));
      toolMenu.add(new JCheckBoxMenuItem(toggleSoundInputToolBarAction));
      
      final JMenu helpMenu=new JMenu("Help");
      helpMenu.add(showAboutDialogAction);
      
      final JMenuBar menuBar=new JMenuBar();
      menuBar.add(fileMenu);
      menuBar.add(editMenu);
      menuBar.add(modifyMenu);
      menuBar.add(viewMenu);
      menuBar.add(toolMenu);
      menuBar.add(helpMenu);
      setJMenuBar(menuBar);
      
      
      /*undoAction.setEnabled(false);
      redoAction.setEnabled(false);*/
      
      /*final JDialog moveDialog=new JDialog(this, Dialog.ModalityType.MODELESS);
      moveDialog.setContentPane(new Mover(this));
      moveDialog.setTitle("Move");
      moveDialog.setSize(200, 200);
      moveDialog.setAlwaysOnTop(true);     
      moveDialog.setVisible(true);*/
      
      //setContentPane(new JScrollPane(new Sheet()));

      addWindowListener(new WindowAdapter() {
         @Override
         public void windowClosing(WindowEvent e) {
            super.windowClosing(e);
            frameCount--;
         }
         @Override
         public void windowClosed(WindowEvent e) {
            super.windowClosed(e);
            if(frameCount==0) {
               OutDeviceManager.instance.closeOutDevice();
               System.exit(0);
            }
         }
      });
      
      /*final JToolBar mouseToolBar=new JToolBar();
      mouseToolBar.setOrientation(JToolBar.VERTICAL);
      mouseToolBar.add(insertAction);
      mouseToolBar.add(selectAction);
      add(mouseToolBar, BorderLayout.WEST);*/
      pack();
      //setSize(800, 600);
   }
   
   public void addScore(Score score) {
      //scoreViews.add(s);
      desktop.addScore(score);
      
      
   }
   
   
   public void importMidi() {
      final JFileChooser jfc=new JFileChooser(Util.curDir);
      jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
      jfc.setFileFilter(new FileNameExtensionFilter("midi files", "mid"));
      
      final int ret=jfc.showOpenDialog(this);
      
      if(ret==JFileChooser.APPROVE_OPTION) {
         this.addScore(Score.fromFile(jfc.getSelectedFile()));
      }
   }
   public void importMidiFromFile(File file) {
      this.addScore(Score.fromFile(file));
   }
   
   public ScoreView getScoreView() {
      final ViewPane vf=((ViewPane)desktop.getSelectedComponent());
      if(vf==null) throw new RuntimeException();
      final ScoreView scoreView=vf.scoreView;
      //final Score score=scoreView.score;
      return scoreView;
   }
   public Score getScore() {
      return getScoreView().score;
   }
   
   public static Score createScore() {
      final Score score=new Score();
      final Part part=new Part();
      //part.add(new Note(60, 32));
      //part.add(new Note(70, 32));
      /*for(int i=0; i<1000; i++) {
         part.add(new Note(60, Note.WHOLE_LENGTH/8));
      }*/
      score.add(part);
      //score.add(new Part());
      score.setLogging(true);
      
      //[ debug
//      score.addScoreChangeListener(new ScoreChangeListener() {
//         public void scoreChanged(ScoreChangeEvent e) {
//            System.out.println(e.getScore());
//         }
//      });
      //part.add(new Note(60, 32));
      //score.undo();
      //part.add(new Note(90, 32));
      
//System.err.println(score.get(0).notes);      
      return score;
   }
   //============================================================[ api for scripting
   public int perform(Action a) {
      try {
         a.actionPerformed(null);
      } catch(Exception e) {
         System.err.println("failed to perform action");
         return -1;
      }
      return 0;
   }
   
   //============================================================
   //[ This main() is reserved for tests, users should call Run.main() instead 
   public static void main(String[] args) {
      Util.setCrossPlatformLookAndFeel(); //>>>
      SwingUtilities.invokeLater(new Runnable() {
         public void run() {
            final MainFrame mf=new MainFrame();
            //jf.addSheet(new Sheet());
            //jf.addSheet(new Sheet());
            /*Sheet s=new Sheet();
            s.addStaff(new Staff(Clef.F, 3, -4));
            jf.addSheet(s);
            
            s=new Sheet();
            s.addStaff(new Staff(Clef.C, 2, 5));
            jf.addSheet(s);
            
            s=new Sheet();
            for(int i=0; i<8; i++) {
               s.addStaff(createStaff2(Clef.G, 1, i));
            }
            for(int i=0; i<8; i++) {
               s.addStaff(createStaff(Clef.G, 1, -i));
            }
            jf.addSheet(s);*/
            mf.addScore(createScore());
            
            mf.setVisible(true);
         }
      });
      
   }

   
   //[ actions
   public final Action newAction=new NewAction(this);
   public final Action importAction=new ImportAction(this);
   public final Action exportAction=new ExportAction(this);
   
   public final Action undoAction=new UndoAction(this);
   public final Action redoAction=new RedoAction(this);
   public final Action undoAllAction=new UndoAllAction(this);
   public final Action redoAllAction=new RedoAllAction(this);
   
   public final Action zoomInAction=new ZoomInAction(this);
   public final Action zoomOutAction=new ZoomOutAction(this);
   public final Action zoomNormalAction=new ZoomNormalAction(this);
   //final Action playAction=new PlayAction(this);
   public final Action startSoundInputAction=new StartSoundInputAction(this);
   public final Action stopSoundInputAction=new StopSoundInputAction(this);
   public final Action removeAction=new RemoveAction(this);
   public final Action cutAction=new CutAction(this);
   public final Action copyAction=new CopyAction(this);
   public final Action pasteAction=new PasteAction(this);
   
   public final Action showScorePropertyAction=new ShowScorePropertyAction(this);
   public final Action showPitchProfileAction=new ShowPitchProfileAction(this); //>>> should be here?
   public final Action noteHigherAction=new NoteHigherAction(this);
   public final Action noteLowerAction=new NoteLowerAction(this);
   public final Action noteMuchHigherAction=new NoteMuchHigherAction(this);
   public final Action noteMuchLowerAction=new NoteMuchLowerAction(this);
   public final Action noteLongerAction=new NoteLongerAction(this);
   public final Action noteShorterAction=new NoteShorterAction(this);
   public final Action noteAddDotAction=new NoteAddDotAction(this);
   public final Action noteRemoveDotAction=new NoteRemoveDotAction(this);
   public final Action noteTieAction=new NoteTieAction(this);
   public final Action noteTripletAction=new NoteTripletAction(this);
   public final Action showSoundToNoteDialogAction=new ShowSoundToNoteDialogAction(this);
   public final Action addPartAction=new AddPartAction(this);
   public final Action removePartAction=new RemovePartAction(this);
   public final Action showPartDialogAction=new ShowPartDialogAction(this);
   public final Action newTabAction=new NewViewAction(this);
   public final Action newWindowAction=new NewWindowAction(this);
   public final Action toStaffPartViewAction=new ToStaffPartViewAction(this);
   public final Action toNumPartViewAction=new ToNumPartViewAction(this);
   public final Action toGridPartViewAction=new ToMessagePartViewAction(this);
   public final Action showAboutDialogAction=new ShowAboutDialogAction(this);
   //final Action showInputDeviceDialogAction=new ShowInputDeviceDialogAction(this);
   //final Action stopRecordAction=new StopMIDIRecordAction(this);
   public final Action toggleInputDeviceRecordAction=new ToggleInputDeviceRecordAction(this);
   public final Action toggleInputDeviceEnableAction=new ToggleInputDeviceEnableAction(this);
   
   public final Action toggleBasicToolBarAction=new ToggleBasicToolBarAction(this);
   public final Action toggleModifyToolBarAction=new ToggleModifyToolBarAction(this);
//   final Action toggleScoreToolBarAction=new ToggleScoreToolBarAction(this);
//   final Action togglePartToolBarAction=new TogglePartToolBarAction(this);
//   final Action toggleNoteToolBarAction=new ToggleNoteToolBarAction(this);
   public final Action togglePlayToolBarAction=new TogglePlayToolBarAction(this);
   public final Action toggleOutputDeviceToolBarAction=new ToggleOutputDeviceToolBarAction(this);
   public final Action toggleKeyboardToolBarAction=new ToggleKeyboardToolBarAction(this);
   public final Action toggleInputDeviceToolBarAction=new ToggleInputDeviceToolBarAction(this);
   public final Action toggleSoundInputToolBarAction=new ToggleSoundInputToolBarAction(this);
   
   public final static Action insertAction=new InsertAction();
   //final Action selectAction=new SelectAction(this);
   
   public final Action exitAction=new ExitAction(this); //>>> close in multi-window?
   public final Action showScriptEditorAction=new ShowScriptEditorAction(this);
   public final Action showStereoFieldEditorAction=new ShowStereoFieldEditorAction(this);
   
 //[ tool bars
   final BasicToolBar basicToolBar=new BasicToolBar(this);
   final ModifyToolBar modifyToolBar=new ModifyToolBar(this);
//   final ScoreToolBar scoreToolBar=new ScoreToolBar(this);
//   final PartToolBar partToolBar=new PartToolBar(this);
//   final NoteToolBar noteToolBar=new NoteToolBar(this);
   final PlayToolBar playToolBar=new PlayToolBar(this);
   final OutputDeviceToolBar outputDeviceToolBar=new OutputDeviceToolBar(this);
   final JToolBar keyboardToolBar=new JToolBar("Virtual Keyboard");
   final JToolBar inputDeviceToolBar=new InputDeviceToolBar(this);
   final SoundInputToolBar soundInputToolBar=new SoundInputToolBar(this);
}

//class ScoreToolBar extends JToolBar {
//   private final MainFrame mainFrame;
//   public ScoreToolBar(MainFrame mf) {
//      mainFrame=mf;
//      setName("Score Tools");
//      add(mainFrame.showScorePropertyAction);
//      addSeparator();
//      add(mainFrame.addPartAction);
//      add(mainFrame.removePartAction);
//      addSeparator();
//      add(mainFrame.newViewAction);
//   }
//   
//}
//class NoteToolBar extends JToolBar {
//   private final MainFrame mainFrame;
//   public NoteToolBar(MainFrame mf) {
//      mainFrame=mf;
//      setName("Note Tools");
//      add(mainFrame.noteHigherAction);
//      add(mainFrame.noteLowerAction);
//      addSeparator();
//      add(mainFrame.noteMuchHigherAction);
//      add(mainFrame.noteMuchLowerAction);
//      addSeparator();
//      add(mainFrame.noteShorterAction);
//      add(mainFrame.noteLongerAction);
//      addSeparator();
//      add(mainFrame.noteRemoveDotAction);
//      add(mainFrame.noteAddDotAction);
//      addSeparator();
//      add(mainFrame.noteTieAction);
//      add(mainFrame.noteTripletAction);
//      addSeparator();
//      
//   }
//}
//class PartToolBar extends JToolBar {
//   private final MainFrame mainFrame;
//   public PartToolBar(MainFrame mf) {
//      setName("Part Tools");
//      mainFrame=mf;
//      add(mainFrame.showPartDialogAction);
//      addSeparator();
//      add(mainFrame.toStaffPartViewAction);
//      add(mainFrame.toNumPartViewAction);
//      add(mainFrame.toMessagePartViewAction);
//   }
//}


//[ unkown comments

/*private static Staff createStaff(Clef clef, int cLine, int k) {
final Staff s=new Staff(clef, cLine, k);
int count=0;
int[] advance= { 2, 2, 1, 2, 2, 2, 1 };
int pitch=60;
int advanceIndex=0;
for(int i=0; i < 8; i++) {
   //s.addNote(new Note(-1, (int) (Math.pow(2, 7 - count))));
   s.addNote(new Note(pitch, (int)(Math.pow(2, 7-count))));
   pitch+=advance[advanceIndex++ % advance.length];
   count++;
   count%=8;
}
return s;
}
private static Staff createStaff2(Clef clef, int cLine, int k) {
final Staff s=new Staff(clef, cLine, k);
int count=0;
int[] advance= { 2, 2, 1, 2, 2, 2, 1 };
int pitch=60;
int advanceIndex=0;
for(int i=0; i < 8; i++) {
   s.addNote(new Note(-1, (int) (Math.pow(2, 7 - count))));
   //s.addNote(new Note(pitch, (int)(Math.pow(2, 7-count))));
   pitch+=advance[advanceIndex++ % advance.length];
   count++;
   count%=8;
}
return s;
}*/
