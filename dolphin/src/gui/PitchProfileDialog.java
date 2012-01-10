package gui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;


import api.audio.PitchListener;
import api.audio.PitchProfile;
import api.audio.SoundAnalyzer;
import api.model.Key;
import api.util.LineDiagram;
import api.util.Util;




public class PitchProfileDialog extends JDialog implements PitchListener {
   private final MainFrame mainFrame;
   private final JTextField[] freqFields; 
   //private LineDiagram pitchDiagram=new LineDiagram();
   
   public PitchProfileDialog(MainFrame mf) {
      mainFrame=mf;
      
      setTitle("Pitch Profile");
      
      final JPanel centerPane=new JPanel();
      centerPane.setLayout(new BoxLayout(centerPane, BoxLayout.PAGE_AXIS));
      
      freqFields=new JTextField[mf.pitchProfile.size()];
      for(int i=0; i<freqFields.length; i++) {
         freqFields[i]=new JTextField(
               String.valueOf(mainFrame.pitchProfile.get(i)));
         //freqFields[i].setEditable(false);
         centerPane.add(freqFields[i]);
      }
      add(centerPane, BorderLayout.CENTER);
      
      //pitchDiagram.setPreferredSize(new Dimension(400, 300));
      //add(pitchDiagram, BorderLayout.NORTH);
      
      //page end
      final JButton loadButton=new JButton("Import...");
      loadButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            final JFileChooser jfc=new JFileChooser(Util.curDir);
            jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            //jfc.setFileFilter(
            //      new FileNameExtensionFilter("midi files", "mid"));
            final int ret=jfc.showOpenDialog(PitchProfileDialog.this);
            if(ret==JFileChooser.APPROVE_OPTION) {
               mainFrame.pitchProfile=PitchProfile.load(jfc.getSelectedFile().getPath());
               for(int i=0; i<freqFields.length; i++) {
                  freqFields[i].setText(
                        String.valueOf(mainFrame.pitchProfile.get(i)));
               }   
            }
            
         }
      });
      final JButton saveButton=new JButton("Export...");
      saveButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            final JFileChooser jfc=new JFileChooser(Util.curDir);
            jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            //jfc.setFileFilter(
            //      new FileNameExtensionFilter("midi files", "mid"));
            final int ret=jfc.showSaveDialog(PitchProfileDialog.this);
            if(ret==JFileChooser.APPROVE_OPTION) {
               final PitchProfile profile=new PitchProfile(60, 8);
               for(int i=0; i<freqFields.length; i++) {
                  profile.set(i, 
                        Double.parseDouble(freqFields[i].getText()));
               }
               profile.save(jfc.getSelectedFile().getPath()); 
               //>>> mess...mutability of pitchProfile
               JOptionPane.showMessageDialog(PitchProfileDialog.this, "Exported To "
                     +jfc.getSelectedFile().getPath());
            }
            
         }
      });
      final JButton adjustButton=new JButton("New");
      adjustButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            freqFields[index%freqFields.length].setBackground(Color.yellow);
            if(mainFrame.soundAnalyzer!=null) {
               mainFrame.soundAnalyzer.stopRecording();
            }
            MainFrame.soundAnalyzer=new SoundAnalyzer(); //>>>
            MainFrame.soundAnalyzer.addFrequencyListener(PitchProfileDialog.this);
            MainFrame.soundAnalyzer.start();
         }
      });
      final JButton clearButton=new JButton("Clear All");
      clearButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            for(int i=0; i<freqFields.length; i++) {
               freqFields[i].setText(String.valueOf(0.0));
               freqFields[index%freqFields.length].setBackground(Color.white);
               index=0;
            }
         }
      });
      final JButton cancelButton=new JButton("Cancel");
      cancelButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            if(MainFrame.soundAnalyzer!=null) {
               MainFrame.soundAnalyzer.stopRecording();
            }
            dispose();
         }
      });
      final JButton okButton=new JButton("OK");
      okButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            if(MainFrame.soundAnalyzer!=null) {
               MainFrame.soundAnalyzer.stopRecording();
            }
            for(int i=0; i<freqFields.length; i++) {
               mainFrame.pitchProfile.set(i, 
                     Double.parseDouble(freqFields[i].getText()));
            }
            mainFrame.pitchProfile.saveDefault();
            dispose();
         }
      });
      final JPanel endPane=new JPanel();
      endPane.setLayout(new FlowLayout(FlowLayout.CENTER));
      endPane.add(loadButton);
      endPane.add(saveButton);
      endPane.add(adjustButton);
      endPane.add(clearButton);
      endPane.add(cancelButton);
      endPane.add(okButton);
      add(endPane, BorderLayout.SOUTH);
      
      pack();
      setLocation(mainFrame.getX()+(mainFrame.getWidth()-this.getWidth())/2,
            mainFrame.getY()+(mainFrame.getHeight()-this.getHeight())/2);
      //setVisible(true);
   }

   int index=0;
   @Override
   public void gotPitch(double freq, double len) {
//      final double oldFreq=Double.parseDouble(
//            freqFields[index%freqFields.length].getText());
//      double newFreq=freq;
//      if(oldFreq!=0) { 
//         newFreq=(newFreq+oldFreq)/2.0;
//      }
//      freqFields[(index)%freqFields.length].setText(String.valueOf(newFreq));
      final double pitch=Util.frequencyToPitch(freq);
      freqFields[(index)%freqFields.length].setText(String.valueOf(pitch));
      //if(index%freqFields.length==0) pitchDiagram.clearData();
      //pitchDiagram.addData((float)pitch);
      //pitchDiagram.repaint();
      
      
      freqFields[index%freqFields.length].setBackground(Color.white);
      index++;
      freqFields[index%freqFields.length].setBackground(Color.yellow);
      
      
   }
}
