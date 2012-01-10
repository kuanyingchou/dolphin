package gui;

import javax.swing.JToolBar;

class ModifyToolBar extends JToolBar {
   private final MainFrame mainFrame;
   public ModifyToolBar(MainFrame mf) {
      mainFrame=mf;
      setName("Modify Tools");
      add(mainFrame.showScorePropertyAction);
      addSeparator();
      add(mainFrame.addPartAction);
      add(mainFrame.removePartAction);
      addSeparator();
      add(mainFrame.showPartDialogAction);
      addSeparator();
      add(mainFrame.noteHigherAction);
      add(mainFrame.noteLowerAction);
      addSeparator();
      add(mainFrame.noteMuchHigherAction);
      add(mainFrame.noteMuchLowerAction);
      addSeparator();
      add(mainFrame.noteShorterAction);
      add(mainFrame.noteLongerAction);
      addSeparator();
      add(mainFrame.noteRemoveDotAction);
      add(mainFrame.noteAddDotAction);
      addSeparator();
      add(mainFrame.noteTieAction);
      add(mainFrame.noteTripletAction);
   }
   
}