package gui;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


import api.audio.PitchProfile;
import api.audio.SoundAnalyzer;
import api.model.Instrument;



public class SoundInputDialog extends JDialog {
//   public final static SoundAnalyzer.Setting settings
//         =new SoundAnalyzer.Setting();
   
   final MainFrame mainFrame;
   public SoundInputDialog(MainFrame mf) {
      super(mf);
      mainFrame=mf;
      setTitle("Sound-to-Note Settings");
      
      final JSpinner lowerSpinner=new JSpinner(new SpinnerNumberModel(SoundAnalyzer.settings.floor, 50, 500, 1));
      final JSpinner upperSpinner=new JSpinner(new SpinnerNumberModel(SoundAnalyzer.settings.ceiling, 100, 2000, 1));
      
      final JRadioButton femaleButton = new JRadioButton("Female Voice");
      femaleButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            lowerSpinner.setEnabled(false);
            upperSpinner.setEnabled(false);
         }
      });
      final JRadioButton maleButton = new JRadioButton("Male Voice");
      maleButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            lowerSpinner.setEnabled(false);
            upperSpinner.setEnabled(false);
         }
      });
      final JRadioButton customButton = new JRadioButton("Custom: ");
      customButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            lowerSpinner.setEnabled(true);
            upperSpinner.setEnabled(true);
         }
      });
      
      int sex=0;
      if(SoundAnalyzer.settings.floor==100 && SoundAnalyzer.settings.ceiling==500) sex=0;
      else if(SoundAnalyzer.settings.floor==75 && SoundAnalyzer.settings.ceiling==300) sex=1;
      else sex=2;
      
      if(sex==0) {
         femaleButton.setSelected(true);
         lowerSpinner.setEnabled(false);
         upperSpinner.setEnabled(false);
      } else if(sex==1) {
         maleButton.setSelected(true);
         lowerSpinner.setEnabled(false);
         upperSpinner.setEnabled(false);
      } else if(sex==2) {
         customButton.setSelected(true);
         lowerSpinner.setValue(SoundAnalyzer.settings.floor);
         upperSpinner.setValue(SoundAnalyzer.settings.ceiling);
         lowerSpinner.setEnabled(true);
         upperSpinner.setEnabled(true);
      }

      final ButtonGroup group = new ButtonGroup();
      group.add(femaleButton);
      group.add(maleButton);
      group.add(customButton);
      
      final JPanel keyPane=new JPanel();
      keyPane.setBorder(BorderFactory.createTitledBorder("Key Range"));
      keyPane.add(femaleButton);
      keyPane.add(maleButton);
      keyPane.add(customButton);
      keyPane.add(lowerSpinner);
      keyPane.add(new JLabel("to "));
      keyPane.add(upperSpinner);
      keyPane.add(new JLabel("Hz."));
      
      //===========
      final JCheckBox cpsBox=new JCheckBox("Enable Compensation");
      cpsBox.setSelected(SoundAnalyzer.settings.enableCompensation);
//      cpsBox.addChangeListener(new ChangeListener() {
//         public void stateChanged(ChangeEvent e) {
//            settings.enableCompensation=cpsBox.isSelected();
//         }
//         
//      });
      //final JLabel cpsLabel=new JLabel();
      //cpsLabel.setText(new DecimalFormat("00.00").format(60-mainFrame.pitchProfile.get(0))+
      //      " semi-tone(s)");
      //final JButton adjustButton=new JButton();
      //final JLabel noiseLabel=new JLabel();
      //noiseLabel.setAlignmentX(LEFT_ALIGNMENT);
      
      final JSlider noiseSlider=new JSlider(0, 256);
      noiseSlider.setMajorTickSpacing(30);
      noiseSlider.setMinorTickSpacing(10);
      noiseSlider.setSnapToTicks(true);
      noiseSlider.setPaintTicks(true);
      noiseSlider.setPaintLabels(true);
      noiseSlider.setValue(SoundAnalyzer.settings.noiseLevel);
//      final Hashtable<Integer, JLabel> panLabelTable = new Hashtable<Integer, JLabel>();
//      panLabelTable.put( new Integer(0), new JLabel("<html><sup>Low</sup></html>") );
//      panLabelTable.put( new Integer(256), new JLabel("<html><sup>High</sup></html>") );
//      noiseSlider.setLabelTable(panLabelTable);
      noiseSlider.setBorder(BorderFactory.createTitledBorder("Noise Level"));
//      noiseSlider.addChangeListener(new ChangeListener() {
//         public void stateChanged(ChangeEvent e) {
//            noiseLabel.setText(String.valueOf(noiseSlider.getValue()));
//            //noiseSlider.setToolTipText(String.valueOf(noiseSlider.getValue()));
//         }
//      });
      
      
      final JPanel advancedPane=new JPanel();
      advancedPane.setBorder(BorderFactory.createTitledBorder("Compensation"));
      //advancedPane.add(new JLabel("Pitch Compensation: "));
      //advancedPane.add(cpsBox);
      advancedPane.add(cpsBox);
      //advancedPane.add(cpsLabel);
      advancedPane.add(new JButton(mainFrame.showPitchProfileAction));
      //advancedPane.add(new JLabel("semi-tone"));
      
      
      //final JLabel noiseLabel=new JLabel("Noise Level");
      //noiseLabel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
      
      
      final JPanel centerPane=new JPanel();
      centerPane.setLayout(new BoxLayout(centerPane, BoxLayout.PAGE_AXIS));
      centerPane.add(keyPane);
      centerPane.add(advancedPane);
      //centerPane.add(noiseLabel);
      centerPane.add(noiseSlider);
      //centerPane.add(noiseLabel);
      add(centerPane, BorderLayout.CENTER);
      
      //page end ===============
      final JButton cancelButton=new JButton("Cancel");
      cancelButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            dispose();
         }
      });
      final JButton okButton=new JButton("OK");
      okButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            if(femaleButton.isSelected()) {
               SoundAnalyzer.settings.floor=100;
               SoundAnalyzer.settings.ceiling=500;
            } else if(maleButton.isSelected()) {
               SoundAnalyzer.settings.floor=75;
               SoundAnalyzer.settings.ceiling=300;
            } else {
               SoundAnalyzer.settings.floor=(Integer)lowerSpinner.getValue();
               SoundAnalyzer.settings.ceiling=(Integer)upperSpinner.getValue();
            }
            
            //settings.compensation=(Integer)cpsSpinner.getValue();
            SoundAnalyzer.settings.noiseLevel=noiseSlider.getValue();
            SoundAnalyzer.settings.enableCompensation=cpsBox.isSelected();
            
            //mainFrame.meter.setThreshold(SoundAnalyzer.settings.noiseLevel); //>>> here?
            mainFrame.intensityHistory.repaint();
            dispose();
         }
      });
      final JPanel endPane=new JPanel();
      endPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
      endPane.add(cancelButton);
      endPane.add(okButton);
      add(endPane, BorderLayout.SOUTH);

      setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
      pack();
      setLocation(mainFrame.getX()+(mainFrame.getWidth()-this.getWidth())/2,
            mainFrame.getY()+(mainFrame.getHeight()-this.getHeight())/2);
   }
   
   public static void main(String[] args) {
      SoundInputDialog d=new SoundInputDialog(null);
      d.setVisible(true);
      
   }
}
