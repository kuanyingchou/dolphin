package gui;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;

import api.model.Instrument;
import api.model.Key;
import api.model.Part;
import api.model.Score;




public class PartDialog extends JDialog {
   final MainFrame mainFrame;
   public PartDialog(MainFrame mf, final Part part) {
      super(mf);
      mainFrame=mf;
      setTitle("Part Properties");
      
      final JLabel keyPromptLabel=new JLabel("Key Signature:");
      //keyPromptLabel.setBorder(BorderFactory.createLineBorder(Color.black));
      final JList instList=new JList(Instrument.all.toArray());
      instList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      final JScrollPane instScrollPane=new JScrollPane(instList);
      instList.setSelectedValue(part.getInstrument(), true);
      
      final JCheckBox muteBox=new JCheckBox("Mute");
      muteBox.setSelected(part.isMute());
      
      final JSlider volumeSlider=new JSlider(0, 127);
      volumeSlider.setMajorTickSpacing(30);
      volumeSlider.setMinorTickSpacing(10);
      volumeSlider.setSnapToTicks(true);
      volumeSlider.setPaintTicks(true);
      volumeSlider.setPaintLabels(true);
      volumeSlider.setValue(part.getVolume());
      final Hashtable<Integer, JLabel> volumeLabelTable = new Hashtable<Integer, JLabel>();
      volumeLabelTable.put( new Integer(0), new JLabel("<html><sup>Min</sup></html>") );
      volumeLabelTable.put( new Integer(120), new JLabel("<html><sup>Max</sup></html>") );
      volumeSlider.setLabelTable(volumeLabelTable);
      
      final JSlider panSlider=new JSlider(0, 127);
      panSlider.setMajorTickSpacing(30);
      panSlider.setMinorTickSpacing(10);
      panSlider.setSnapToTicks(true);
      panSlider.setPaintTicks(true);
      panSlider.setPaintLabels(true);
      panSlider.setValue(part.getPan());
      final Hashtable<Integer, JLabel> panLabelTable = new Hashtable<Integer, JLabel>();
      panLabelTable.put( new Integer(0), new JLabel("<html><sup>Left</sup></html>") );
      panLabelTable.put( new Integer(120), new JLabel("<html><sup>Right</sup></html>") );
      panSlider.setLabelTable(panLabelTable);
      
      final JPanel centerPane=new JPanel();
      centerPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
      centerPane.setLayout(new BoxLayout(centerPane, BoxLayout.PAGE_AXIS));
      //centerPane.setLayout(new GridLayout(0, 1));
      centerPane.add(new JLabel("Instrument:"));
      centerPane.add(instScrollPane);
      centerPane.add(new JLabel("Volume:"));
      centerPane.add(volumeSlider);
      centerPane.add(new JLabel("Balance:"));
      centerPane.add(panSlider);
      centerPane.add(muteBox);
      //centerPane.add(tempoUnitLabel);
      //centerPane.add(instLabel);
      //centerPane.add(instCombo);
      //centerPane.add(volumeLabel);
      //centerPane.add(volumeSlider);
      for(Component c: centerPane.getComponents()) {
         ((JComponent)c).setAlignmentX(Component.LEFT_ALIGNMENT);
      }

      //page end
      final JButton cancelButton=new JButton("Cancel");
      cancelButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            dispose();
         }
      });
      final JButton okButton=new JButton("OK");
      okButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            //>>> change property
            part.getScore().setComboMode(true);
            final Instrument inst=(Instrument)instList.getSelectedValue();
            //score.keyQuality=k.getQuality();
            //score.keySignature=k.getSignature();
            part.setInstrument(inst);
            part.setVolume(volumeSlider.getValue());
            part.setPan(panSlider.getValue());
            part.setMute(muteBox.isSelected());
            //mainFrame.partPane.repaint();
            part.getScore().setComboMode(false);
            dispose();
         }
      });
      final JPanel endPane=new JPanel();
      endPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
      endPane.add(cancelButton);
      endPane.add(okButton);

      setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
      setLayout(new BorderLayout());
      add(centerPane, BorderLayout.CENTER);
      add(endPane, BorderLayout.PAGE_END);
      //setContentPane(contentPane);
      pack();
      setLocation(mainFrame.getX()+(mainFrame.getWidth()-this.getWidth())/2,
            mainFrame.getY()+(mainFrame.getHeight()-this.getHeight())/2);
   }
}
