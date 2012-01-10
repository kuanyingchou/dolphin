package gui;

import javax.swing.JToolBar;

class SoundInputToolBar extends JToolBar {
   private final MainFrame mainFrame;
   public SoundInputToolBar(MainFrame mf) {
      setName("Sound Input");
      mainFrame=mf;
      
      //add(mainFrame.showPitchProfileAction); //>>> ?
      add(mainFrame.showSoundToNoteDialogAction);
      add(mainFrame.startSoundInputAction);
      add(mainFrame.stopSoundInputAction);
      addSeparator();
      //add(new JLabel("Volume: "));
      //add(mainFrame.meter);
      add(mainFrame.intensityHistory);
   }
}