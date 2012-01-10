package gui;
//package music;

import javax.swing.*;

import api.model.Key;
import api.model.Score;




import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Field;
import java.util.Hashtable;

import api.model.*;
import api.util.FlowPane;

public class ScoreDialog extends JDialog {
   private Score score;

   /*private static Key[] keys= { Key.Cb, Key.Gb, Key.Db, Key.Ab, Key.Eb, Key.Bb,
         Key.F, Key.C, Key.G, Key.D, Key.A, Key.E, Key.B, Key.Fs, Key.Cs,
         Key.ab, Key.eb, Key.bb, Key.f, Key.c, Key.g, Key.d, Key.a, Key.e,
         Key.b, Key.fs, Key.cs, Key.gs, Key.ds, Key.as };*/
   /*
   private static Key findKey(int v) {
      for(int i=0; i<keys.length; i++) {
         if(keys[i].getValue()==v) return keys[i];
      }
      return null;
   }
   */
   
   public ScoreDialog(Window owner, Score s) {
      super(owner, ModalityType.DOCUMENT_MODAL);
      score=s;
//System.err.println(score);
      setTitle("Score Properties");
      //setSize(300, 400);
      //center
      final JLabel keyPromptLabel=new JLabel("Key Signature:");
      //keyPromptLabel.setBorder(BorderFactory.createLineBorder(Color.black));
      final JTextField titleField=new JTextField(score.getTitle());
      final JComboBox keyCombo=new JComboBox(Key.all.toArray());
      keyCombo.setSelectedItem(score.getKeySignature());
      final JLabel timeSignatureLabel=new JLabel("Time Signature: ");
      final JSpinner timeNumSpinner=new JSpinner(new SpinnerNumberModel(4, 1, 256, 1));
      timeNumSpinner.setPreferredSize(new Dimension(50, 20));
      final JSpinner timeDenSpinner=new JSpinner(new SpinnerListModel(new Integer[] {1, 2, 4, 8, 16, 32, 64, 128}));
      timeDenSpinner.setPreferredSize(new Dimension(50, 20));
      final JLabel tempoPromptLabel=new JLabel("Tempo: ");
      //final JSpinner tempoValueSpinner=new JSpinner(new SpinnerNumberModel(120, 1, 99999, 1));
      final JTextField tempoValueSpinner=new JTextField();
      final JLabel tempoUnitLabel=new JLabel("<html><sup>bpm</sup></html>");
      
      final JPanel centerPane=new JPanel();
      centerPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
      centerPane.setLayout(new BoxLayout(centerPane, BoxLayout.PAGE_AXIS));
      //centerPane.setLayout(new GridLayout(0, 1));
      centerPane.add(new JLabel("Title: "));
      centerPane.add(titleField);
      centerPane.add(keyPromptLabel);
      centerPane.add(keyCombo);
      centerPane.add(timeSignatureLabel);
      centerPane.add(new FlowPane(timeNumSpinner, new JLabel("/"), timeDenSpinner));
      //centerPane.add(timeDenSpinner);
      centerPane.add(tempoPromptLabel);
      centerPane.add(new FlowPane(tempoValueSpinner, new JLabel("BPM")));
      //centerPane.add(tempoUnitLabel);
      //centerPane.add(instLabel);
      //centerPane.add(instCombo);
      //centerPane.add(volumeLabel);
      //centerPane.add(volumeSlider);
      for(Component c: centerPane.getComponents()) {
         ((JComponent)c).setAlignmentX(Component.LEFT_ALIGNMENT);
      }

      //init
      final Key key=score.getKeySignature();
      if(key==null) throw new IllegalArgumentException();
      keyCombo.setSelectedItem(key);
      timeNumSpinner.setValue(score.getNumerator());
      timeDenSpinner.setValue(score.getDenominator());
      //tempoValueSpinner.setValue((int)score.tempo); //>>>the cast may be dangerous
      tempoValueSpinner.setText(String.valueOf(score.getTempo())); //>>> verify input

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
            final Key k=(Key)keyCombo.getSelectedItem();
            //score.keyQuality=k.getQuality();
            //score.keySignature=k.getSignature();
            score.setComboMode(true);
            score.setTitle(titleField.getText());
            score.setKeySignature(k);
            score.setTimeSignature((Integer)timeNumSpinner.getValue(), 
                  (Integer)timeDenSpinner.getValue());
            score.setTempo(Float.parseFloat(tempoValueSpinner.getText().trim()));//((Number)tempoValueSpinner.getValue()).intValue();
            score.setComboMode(false);
            //part.instrument=((InstPair)instCombo.getSelectedItem()).value;
            //part.volume=volumeSlider.getValue();
//System.err.println(score);
            //part.revalidate();
            //part.repaint();
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
      setLocation(owner.getX()+(owner.getWidth()-this.getWidth())/2,
                  owner.getY()+(owner.getHeight()-this.getHeight())/2);
   }
   /*
   public static void main(String[] args) {
      PropertyDialog d=new PropertyDialog(null, new Score());
      d.setVisible(true);
   }
   */
}
