package gui;

import javax.swing.JToggleButton;
import javax.swing.JToolBar;

class BasicToolBar extends JToolBar {
   private final MainFrame mainFrame;
   public BasicToolBar(MainFrame mf) {
      mainFrame=mf;
      setName("Basic Tools");
      add(mainFrame.newAction);
      add(mainFrame.importAction);
      add(mainFrame.exportAction);
      addSeparator();
      //add(mainFrame.undoAllAction);
      add(mainFrame.undoAction);
      add(mainFrame.redoAction);
      //add(mainFrame.redoAllAction);
      addSeparator();
      //add(mainFrame.removeAction);
      add(mainFrame.cutAction);
      add(mainFrame.copyAction);
      add(mainFrame.pasteAction);

      addSeparator();
      add(mainFrame.zoomInAction);
      add(mainFrame.zoomOutAction);
      addSeparator();
      //add(mainFrame.playAction);
      final JToggleButton insert=new JToggleButton(mainFrame.insertAction);
      //final Dimension dim=new Dimension(32, 32);
      //insert.setPreferredSize(dim);
      //insert.setMinimumSize(dim);
      //insert.setMaximumSize(dim);
      //final AbstractButton select=new JToggleButton(mainFrame.selectAction);
      //final ButtonGroup group=new ButtonGroup();
      //group.add(insert);
      //group.add(select);
      add(insert);
      //add(select);
      
      addSeparator();
      add(mainFrame.showStereoFieldEditorAction);
      add(mainFrame.showScriptEditorAction);
      
   }
}