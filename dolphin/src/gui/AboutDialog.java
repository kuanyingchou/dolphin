package gui;
import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;


public class AboutDialog extends JDialog {
   final MainFrame mainFrame;
   public AboutDialog(MainFrame mf) {
      super(mf);
      mainFrame=mf;
      setTitle("About");
      setLayout(new BorderLayout());
      
      final JPanel centerPane=new JPanel();
      centerPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      centerPane.setLayout(new BorderLayout());
      
      centerPane.add(new JLabel(MainFrame.APP_NAME), BorderLayout.NORTH);
      //centerPane.add(new JLabel("Dedicated to Ruby."), BorderLayout.CENTER);
      add(centerPane, BorderLayout.CENTER);
      
      pack();
      setLocation(mainFrame.getX()+(mainFrame.getWidth()-this.getWidth())/2,
            mainFrame.getY()+(mainFrame.getHeight()-this.getHeight())/2);
   }
}
