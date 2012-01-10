package gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.MidiDevice.Info;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import api.midi.InDeviceManager;
import api.model.Instrument;
import api.util.FlowPane;



public class InputDeviceDialog extends JDialog {
   private final MainFrame mainFrame;
   
   public InputDeviceDialog(MainFrame mf) {
      mainFrame=mf;
      setTitle("Input Device Dialog");
      
      Vector<Info> inInfos=null;
      try {
         inInfos=InDeviceManager.getInDeviceInfos();
      } catch(MidiUnavailableException e2) {
         e2.printStackTrace();
      }
      final JComboBox deviceList=new JComboBox(inInfos);
      deviceList.setSelectedItem(InDeviceManager.instance.getDeviceInfo());  
      deviceList.addItemListener(new ItemListener() {
         public void itemStateChanged(ItemEvent e) {
            if(e.getStateChange()!=ItemEvent.SELECTED) return;
            try {
               InDeviceManager.instance.setDeviceByInfo((Info)deviceList.getSelectedItem());
            } catch(MidiUnavailableException e1) {
               //System.err.println(outDevice.getDeviceInfo());
               JOptionPane.showMessageDialog(mainFrame, "Can't use this device");
               deviceList.setSelectedItem(InDeviceManager.instance.getDeviceInfo());
               return;
            }
         }
      });
      final JButton refreshButton=new JButton("Refresh");
      refreshButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            deviceList.removeAllItems();
            Vector<Info> inInfos=null;
            try {
               inInfos=InDeviceManager.getInDeviceInfos();
            } catch(MidiUnavailableException e2) {
               e2.printStackTrace();
            }
            for(Info info: inInfos) {
               deviceList.addItem(info);   
            }
            //if(MainFrame.deviceManager.outDevice!=null)
            //   deviceList.setSelectedItem(MainFrame.deviceManager.outDevice.getDeviceInfo());
            try {
               InDeviceManager.instance.setDevice(null);
            } catch(MidiUnavailableException e1) {
               e1.printStackTrace();
            }
            deviceList.setSelectedItem(InDeviceManager.instance.getDeviceInfo());
            repaint();
         }
      });
      
//      final JCheckBox enableBox=new JCheckBox("Enable External Input Device");
//      enableBox.addChangeListener(new ChangeListener() {
//         public void stateChanged(ChangeEvent e) {
//            if(enableBox.isSelected()) {
//               deviceList.setEnabled(true);
//               refreshButton.setEnabled(true);
//            } else {
//               deviceList.setEnabled(false);
//               refreshButton.setEnabled(false);
//               //MainFrame.inDeviceManager.closeDevice();
//            }
//         }
//      });
      
      final JPanel centerPane=new JPanel();
      centerPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
      centerPane.setLayout(new BoxLayout(centerPane, BoxLayout.PAGE_AXIS));
      //centerPane.add(enableBox);
      centerPane.add(new FlowPane(deviceList, refreshButton));
      //deviceList.setEnabled(false);
      //refreshButton.setEnabled(false);
      
      for(Component c: centerPane.getComponents()) {
         ((JComponent)c).setAlignmentX(Component.LEFT_ALIGNMENT);
      }

      //page end
//      final JButton cancelButton=new JButton("Cancel");
//      cancelButton.addActionListener(new ActionListener() {
//         public void actionPerformed(ActionEvent e) {
//            dispose();
//         }
//      });
      final JButton okButton=new JButton("OK");
      okButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            dispose();
         }
      });
      final JPanel endPane=new JPanel();
      endPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
      //endPane.add(cancelButton);
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
