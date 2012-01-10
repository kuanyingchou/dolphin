package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.MidiDevice.Info;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;

import api.midi.OutDeviceManager;
import api.util.Util;

class OutputDeviceToolBar extends JToolBar {
   private final MainFrame mainFrame;
   public OutputDeviceToolBar(MainFrame mf) {
      setName("Output Device");
      mainFrame=mf;
      
      //[ visual effect
      final VisualEffect visualEffect=new VisualEffect();
      OutDeviceManager.instance.addReceiver(visualEffect);
      
      //[ device list
      Vector<Info> outInfos=null;
      try {
         outInfos=OutDeviceManager.getOutDeviceInfos();
      } catch(MidiUnavailableException e2) {
         e2.printStackTrace();
      }
      final JComboBox deviceList=new JComboBox(outInfos);
      deviceList.setSelectedItem(OutDeviceManager.instance.getOutDeviceInfo());  
      deviceList.addItemListener(new ItemListener() {
         public void itemStateChanged(ItemEvent e) {
            if(e.getStateChange()!=ItemEvent.SELECTED) return;
            try {
               OutDeviceManager.instance.setOutDeviceByInfo((Info)deviceList.getSelectedItem());
            } catch(MidiUnavailableException e1) {
               //System.err.println(outDevice.getDeviceInfo());
               JOptionPane.showMessageDialog(mainFrame, "Can't use this device");
               deviceList.setSelectedItem(OutDeviceManager.instance.getOutDeviceInfo());
               return;
            }
         }
      });
      final JButton refreshButton=new JButton(Util.getImageIcon("images/refresh.png"));
      //refreshButton.setText("Refresh");
      refreshButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            deviceList.removeAllItems();
            Vector<Info> outInfos=null;
            try {
               outInfos=OutDeviceManager.getOutDeviceInfos();
            } catch(MidiUnavailableException e2) {
               e2.printStackTrace();
            }
            for(Info info: outInfos) {
               deviceList.addItem(info);   
            }
            //if(MainFrame.deviceManager.outDevice!=null)
            //   deviceList.setSelectedItem(MainFrame.deviceManager.outDevice.getDeviceInfo());
            try {
               OutDeviceManager.instance.setOutDevice(MidiSystem.getSynthesizer());
            } catch(MidiUnavailableException e1) {
               e1.printStackTrace();
            }
            deviceList.setSelectedItem(OutDeviceManager.instance.getOutDeviceInfo());
            repaint();
         }
      });
      //this.addSeparator();
      this.add(new JLabel("Output Device: "));
      this.add(deviceList);
      this.add(refreshButton);
      this.addSeparator();
      //visualEffect.setBorder(BorderFactory.createLoweredBevelBorder());
      this.add(visualEffect);
   }
}