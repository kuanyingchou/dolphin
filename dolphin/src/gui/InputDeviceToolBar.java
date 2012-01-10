package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.MidiDevice.Info;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import api.midi.InDeviceManager;

class InputDeviceToolBar extends JToolBar {
   private final MainFrame mainFrame;
   public InputDeviceToolBar(MainFrame mf) {
      setName("Input Device");
      mainFrame=mf;
      
      final JToggleButton enableButton=new JToggleButton(mainFrame.toggleInputDeviceEnableAction);
      final JToggleButton recordButton=new JToggleButton(mainFrame.toggleInputDeviceRecordAction);
      
      Vector<Info> inInfos=null;
      try {
         inInfos=InDeviceManager.getInDeviceInfos();
      } catch(MidiUnavailableException e2) {
         e2.printStackTrace();
      }
      final JComboBox deviceList=new JComboBox(inInfos);
      deviceList.addItemListener(new ItemListener() {
         public void itemStateChanged(ItemEvent e) {
            //if(e.getStateChange()!=ItemEvent.SELECTED) return;
            enableButton.setSelected(false);
            recordButton.setSelected(false);
            if(deviceList.getSelectedItem()==null) return;
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
      deviceList.setSelectedItem(null);
//      if(!inInfos.isEmpty()) {
//         deviceList.setSelectedItem(inInfos.get(0));   
//      }
      
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
            if(!inInfos.isEmpty()) {
               deviceList.setSelectedItem(inInfos.get(0));
            }
//            try {
//               MainFrame.inDeviceManager.setDevice(null);
//            } catch(MidiUnavailableException e1) {
//               e1.printStackTrace();
//            }
//            deviceList.setSelectedItem(MainFrame.inDeviceManager.getDeviceInfo());
            enableButton.setSelected(false);
            recordButton.setSelected(false);
            repaint();
         }
      });
      
      
      //add(mainFrame.showInputDeviceDialogAction);
      add(new JLabel("Input Device: "));
      add(deviceList);
      add(refreshButton);
      add(enableButton);
      add(recordButton);
      //add(mainFrame.stopRecordAction);
   }
   
   

   
}