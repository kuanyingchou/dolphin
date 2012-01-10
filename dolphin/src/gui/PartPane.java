package gui;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;

import api.model.Instrument;
import api.model.Part;
import api.model.Score;
import api.model.ScoreChange;
import api.model.ScoreChangeListener;



//@Deprecated
public class PartPane extends JPanel implements ScoreChangeListener {
   final MainFrame mainFrame;
   Score score;
   final PartListModel partListModel=new PartListModel();
   final JList partList=new JList(partListModel);
   
   public PartPane(MainFrame mf) {
      //setTitle("Parts");
      mainFrame=mf;
      //setBorder(BorderFactory.createTitledBorder("Parts"));
      //setAlwaysOnTop(true);
      //setIconImage(new ImageIcon("note.png").getImage());
      /*partList=new JList();
      partList.setModel(new PartListModel());*/
      partList.setCellRenderer(new PartListCellRenderer());
      setLayout(new BorderLayout());
      add(new JScrollPane(partList), BorderLayout.CENTER);
      final JToolBar toolBar=new JToolBar();
      toolBar.add(mainFrame.addPartAction);
      toolBar.add(mainFrame.removePartAction);
      toolBar.add(mainFrame.showPartDialogAction);
      toolBar.setFloatable(false);
      add(toolBar, BorderLayout.SOUTH);
      
      //pack();
      //setSize(300, 200);
      //setLocation(400, 100);
      //setVisible(true);
   }
   public void setModel(Score score) {
      if(this.score!=null) {
         if(this.score.containsScoreChangeListener(this)) {
            this.score.removeScoreChangeListener(this);
         }
      }
      this.score=score;
      if(score!=null) {
         this.score.addScoreChangeListener(this);
      }
      partListModel.updateModel();
   }
   public void scoreChanged(ScoreChange e) {
      //partList.validate();
      //partList.repaint();
      partListModel.updateModel();
   }
   
   class PartListModel extends AbstractListModel {
      public void updateModel() {
         fireContentsChanged(this, 0, getSize());
      }
      
      public Object getElementAt(int index) {
         return score.get(index);
      }

      public int getSize() {
         return score == null ? 0 : score.partCount();
      }

   }
   class PartListCellRenderer extends JLabel implements ListCellRenderer {
      private ImageIcon partIcon=new ImageIcon("part.png");
      
      public Component getListCellRendererComponent(JList list, // the list
            Object value, // value to display
            int index, // cell index
            boolean isSelected, // is the cell selected
            boolean cellHasFocus) // does the cell have focus
      {
         //String s=value.toString();
         final Part p=(Part)value;
         setText("part#"+index+": "+p.getInstrument());
         setIcon(partIcon);
         //setIcon((s.length() > 10) ? longIcon : shortIcon);
         if(isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
         } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
         }
         setEnabled(list.isEnabled());
         setFont(list.getFont());
         setOpaque(true);
         return this;
      }
   }
   
//   class AddPartAction extends AbstractAction {
//      public AddPartAction() {
//         putValue(Action.NAME, "Add New Part");
//         putValue(Action.LARGE_ICON_KEY, new ImageIcon("New24.gif"));
//      }
//      public void actionPerformed(ActionEvent e) {
//         if(score==null) return;
//         score.add(new Part());
//         partListModel.updateModel();
//      }
//   }
//   class RemovePartAction extends AbstractAction {
//      public RemovePartAction() {
//         putValue(Action.NAME, "Remove Part");
//         putValue(Action.LARGE_ICON_KEY, new ImageIcon("Delete24.gif"));
//      }
//      public void actionPerformed(ActionEvent e) {
//         if(score==null) return;
//         final int idx=partList.getSelectedIndex();
//         if(idx>=0 && idx<score.size()) score.remove(idx);
//         partListModel.updateModel();
//      }
//   }
//   class ShowPartDialogAction extends AbstractAction {
//      public ShowPartDialogAction() {
//         putValue(Action.NAME, "Part Properties...");
//         //putValue(Action.LARGE_ICON_KEY, new ImageIcon("Delete24.gif"));
//      }
//      public void actionPerformed(ActionEvent e) {
//         if(score==null) return;
//         final int idx=partList.getSelectedIndex();
//         if(idx>=0 && idx<score.size()) {
//            final PartDialog pd=new PartDialog(mainFrame, score.get(idx)); //>>>mf?
//            pd.setVisible(true);
//         }
//      }
//   }
}