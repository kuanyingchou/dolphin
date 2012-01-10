package gui;

import java.awt.Dimension;

import javax.swing.JTabbedPane;

class Desktop extends JTabbedPane {
      MainFrame mainFrame;
      public Desktop(MainFrame mf) {
         mainFrame=mf;
         setTabPlacement(JTabbedPane.TOP);
         setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
         //setBackground(Color.gray);
         setPreferredSize(new Dimension(320, 320));
         
//         addChangeListener(new ChangeListener() {
//            public void stateChanged(ChangeEvent e) {
//               final Desktop d=(Desktop)e.getSource();
//               final ViewPane vf=((ViewPane)d.getSelectedComponent());
//               if(vf==null) return;
//               final ScoreView scoreView=vf.scoreView;
//               mainFrame.partPane.setModel(scoreView.score);
//            }
//            
//         });
      }
   }