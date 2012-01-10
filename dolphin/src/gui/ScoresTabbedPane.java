package gui;


import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTabbedPane;

import api.model.Score;

import view.ScoreView;

class ScoresTabbedPane extends JTabbedPane {
      MainFrame mainFrame;
      
      public ScoresTabbedPane(MainFrame mf) {
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
      
      
      public void addScore(Score score) {
         final ScoreView view=new ScoreView(score);
         final ViewPane vPane=new ViewPane(view, mainFrame);
         //desktop.add(vf, 0);
         final int tabIndex=getTabCount();
         addTab(score.getTitle(), vPane); //>>> should be file name
         
         final ScoreTab tabComponent=new ScoreTab(this, vPane);
         tabComponent.setText(score.getTitle());
         view.score.addScoreChangeListener(tabComponent);
         setTabComponentAt(tabIndex, tabComponent);
         
   //desktop.setTitleAt(0, "aaa");      
         tabComponent.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
               final int tabIndex=indexOfTabComponent(tabComponent);
               //] can't be 'index', because tabIndex may change at runtime.
               if(tabIndex<0 || tabIndex >=getTabCount()) return;
               if(e.getClickCount()==1) {
                  setSelectedIndex(tabIndex);
                  vPane.scoreView.requestFocusInWindow();
                  vPane.scoreView.setFocusCycleRoot(true);
               } else if(e.getClickCount()>=2) {
                  removeTabAt(tabIndex);
               }
            }
         });
         setSelectedComponent(vPane);
         
         vPane.scoreView.requestFocusInWindow();
         vPane.scoreView.setFocusCycleRoot(true);
//         vf.setVisible(true);
         //System.err.println(desktop.selectFrame(true));
         
//         try {
//            vf.setMaximum(true);
//            vf.setSelected(true); //>>> no use???
//         } catch(PropertyVetoException e) {
//            e.printStackTrace();
//         }
      }
   }