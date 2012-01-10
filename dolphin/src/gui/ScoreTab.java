package gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;

import api.model.ScoreChange;
import api.model.ScoreChangeListener;
import api.model.TitleChange;
import api.util.FlowPane;
import api.util.Util;

class ScoreTab extends FlowPane implements ScoreChangeListener {
   JLabel textLabel=new JLabel();
   JButton closeButton=new JButton(" x ");
   ScoresTabbedPane parent;
   ViewPane viewPane;
   
   public ScoreTab(ScoresTabbedPane d, ViewPane vPane) {
      parent=d;
      viewPane=vPane;
      setupCloseButton();
      add(textLabel);
      add(closeButton);
   }
   private void setupCloseButton() {
      closeButton.setContentAreaFilled(false);
      closeButton.setForeground(Color.gray);
      //closeButton.setBorder(BorderFactory.createLineBorder(Color.black));
      closeButton.setBorder(BorderFactory.createEmptyBorder());
      //closeButton.setIcon(Util.getImageIcon("images/close.png"));
      closeButton.addMouseListener(new MouseAdapter() {
         @Override
         public void mouseEntered(MouseEvent e) {
            closeButton.setForeground(Color.black);
         }
         @Override
         public void mouseExited(MouseEvent e) {
            closeButton.setForeground(Color.gray);
         }
      });
      closeButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            parent.remove(viewPane);
         }
      });
   }

   public void setText(String title) {
      textLabel.setText(title);
   }
   
   public void scoreChanged(ScoreChange e) {
      if(e instanceof TitleChange) {
         setText(e.getScore().getTitle());
      }
   }     
}