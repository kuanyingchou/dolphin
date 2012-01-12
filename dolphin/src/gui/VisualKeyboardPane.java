package gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;

import api.model.Instrument;
import api.model.Score;
import api.util.FlowPane;
import api.util.Util;

import view.ScoreView;

public class VisualKeyboardPane extends JPanel {
   private final MainFrame mainFrame;
   final VirtualKeyboard visualKeyboard;
   
   public VisualKeyboardPane(MainFrame mf) {
      mainFrame=mf;
      visualKeyboard=new VirtualKeyboard(mf);
      
      final JToggleButton recordButton=
         new JToggleButton("Record");
      
      final Icon recordIcon=Util.getImageIcon("images/record.png");
      final Icon grayIcon=Util.getImageIcon("images/record_gray.png");
      recordButton.setIcon(grayIcon);
      
      recordButton.setSelected(visualKeyboard.isRecording());
      recordButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            final Score score=mainFrame.getScore();
            if(score==null) return;
            visualKeyboard.setRecording(!visualKeyboard.isRecording());
            
            final long interval=(long)(60000.0/score.getTempo()/2);
            
            new Thread(new Runnable() {
               public void run() {
                  while(visualKeyboard.isRecording()) {
                     //System.err.println("beat");
                     recordButton.setIcon(
                           recordButton.getIcon()==grayIcon?recordIcon:grayIcon);
                     Util.sleep(interval);
                  }
                  recordButton.setIcon(grayIcon);
               }
            }).start();
         }
      });
      
      
      final JToggleButton tabletButton=
         new JToggleButton("Enable Tablet");
      tabletButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            /*
            //tablet feature
            if(tabletButton.isSelected()) {
               MainFrame.tabletManager.open();
               visualKeyboard.isMouseEnabled=false;
            } else {
               MainFrame.tabletManager.close();
               visualKeyboard.isMouseEnabled=true;
            }
            */
         }
      });
      
      
      final JComboBox instList=new JComboBox(Instrument.all.toArray());
      instList.setSelectedItem(visualKeyboard.getInstrument());
      instList.addItemListener(new ItemListener() {
         public void itemStateChanged(ItemEvent e) {
            visualKeyboard.setInstrument(
                  (Instrument)instList.getSelectedItem());
         }
      });
      setLayout(new BorderLayout());
      add(new FlowPane(instList, recordButton, tabletButton), 
            BorderLayout.NORTH);
      add(visualKeyboard, BorderLayout.CENTER);
   }
   
   
//   class ToggleRecordingAction extends AbstractAction {
//      
//      public ToggleRecordingAction() {
//         putValue(Action.NAME, "Record");
//         putValue(Action.SELECTED_KEY, visualKeyboard.isRecording());
//      }
//      
//      @Override
//      public void actionPerformed(ActionEvent e) {
//         final Score score=mainFrame.getScore();
//         if(score==null) return;
//         visualKeyboard.setRecording(!visualKeyboard.isRecording());
//         
//         final long interval=(long)(60000.0/score.tempo);
//         
//         new Thread(new Runnable() {
//            public void run() {
//               while(visualKeyboard.isRecording()) {
//                  System.err.println("beat");
//                  try {
//                     Thread.sleep(interval);
//                  } catch(InterruptedException e) {}
//               }
//            }
//         }).start();
//      }
//   }
}
