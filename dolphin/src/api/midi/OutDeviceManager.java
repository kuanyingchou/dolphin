package api.midi;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;
import javax.sound.midi.MidiDevice.Info;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;

public class OutDeviceManager implements Receiver/*, Transmitter*/ {
//] we don't support Transmitter here because Transmitters allow only one receiver.
   
//   private JList deviceInfoList;
   private MidiDevice outDevice=null;
   private Receiver outDeviceReceiver=null;
   private final java.util.List<Receiver> receivers=new ArrayList<Receiver>();
   public static final OutDeviceManager instance=new OutDeviceManager();
   
   private OutDeviceManager() {
      try {
         setOutDevice(MidiSystem.getSynthesizer());
      } catch(MidiUnavailableException e) {
         e.printStackTrace();
      }
   }
   
   public synchronized void setOutDevice(MidiDevice device) throws MidiUnavailableException {
      if(device==null) throw new IllegalArgumentException();
      //clean old
      if(outDeviceReceiver!=null) outDeviceReceiver.close();
      if(outDevice!=null && outDevice.isOpen()) outDevice.close();
      
      //[ set new
      outDevice=device;
      outDevice.open();
      outDeviceReceiver=outDevice.getReceiver();
//      for(Transmitter t: registeredTransmitters) {
//         t.setReceiver(outDeviceReceiver);
//      }
   }
   
   public synchronized Info getOutDeviceInfo() {
      if(outDevice==null) return null;
      else return outDevice.getDeviceInfo();
   }
   
   public synchronized void setOutDeviceByInfo(Info info) throws MidiUnavailableException {
      if(info==null) throw new IllegalArgumentException();
      final MidiDevice device=MidiSystem.getMidiDevice(info);
      if(device==null) throw new RuntimeException();
      setOutDevice(device);
   }
   
   public synchronized boolean isOutDeviceOpen() {
      if(outDevice!=null) return outDevice.isOpen();
      else return false;
   }
   public synchronized void closeOutDevice() {
      if(outDevice!=null && outDevice.isOpen()) outDevice.close();
      System.err.println("midi device closed.");
   }
   public synchronized void close() {}
   public synchronized void send(MidiMessage message, long timeStamp) {
      if(outDeviceReceiver!=null) outDeviceReceiver.send(message, timeStamp);
      for(int i=0; i<receivers.size(); i++) {
         receivers.get(i).send(message, timeStamp);
      }
   }
   
   public synchronized void addReceiver(Receiver receiver) {
      receivers.add(receiver);
   }

   public static Vector<Info> getOutDeviceInfos() throws MidiUnavailableException {
      final Vector<Info> outInfos=new Vector<Info>();
      final Info[] deviceInfos=MidiSystem.getMidiDeviceInfo();
      for(int i=0; i < deviceInfos.length; i++) {
         if(MidiSystem.getMidiDevice(deviceInfos[i]).getMaxReceivers()!=0) {
            outInfos.add(deviceInfos[i]);
         }
      }
      return outInfos;
   }
   
   /*public static void main(String[] args) {
      final JFrame jf=new DeviceManager();
      jf.pack();
      jf.setVisible(true);
   }*/

}
