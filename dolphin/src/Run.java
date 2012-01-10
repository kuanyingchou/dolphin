import gui.MainFrame;

import javax.swing.SwingUtilities;

import api.util.Util;


public class Run {
   public static void main(String[] args) {
      Util.setLookAndFeel(); // >>>

      final MainFrame mf = new MainFrame();

      mf.addScore(MainFrame.createScore());

      mf.setVisible(true);
      
   }
}
