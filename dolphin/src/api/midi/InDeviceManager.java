package api.midi;


import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;
import javax.sound.midi.MidiDevice.Info;


public class InDeviceManager implements Receiver {
   private MidiDevice inDevice=null;
   private Transmitter inDeviceTransmitter=null;
   private List<Receiver> receivers=new ArrayList<Receiver>();
   public static final InDeviceManager instance=new InDeviceManager();
   
   private InDeviceManager() {
      receivers.add(OutDeviceManager.instance); //>>>
   }
   
   public void setDevice(MidiDevice device) throws MidiUnavailableException {
      if(inDevice!=null && inDevice.isOpen()) inDevice.close();
//      if(device==null) {
//         if(inDevice!=null && inDevice.isOpen()) inDevice.close();
//      } else {
//         if(!device.isOpen()) device.open();
//         inDeviceTransmitter=device.getTransmitter();
//         inDeviceTransmitter.setReceiver(this);
//      }
      inDevice=device;
   }
   public void setDeviceByInfo(Info info) throws MidiUnavailableException {
      if(info==null) throw new IllegalArgumentException();
      final MidiDevice device=MidiSystem.getMidiDevice(info);
      if(device==null) throw new RuntimeException();
      setDevice(device);
   }
   
   //[ device forward methods
   public Info getDeviceInfo() {
      if(inDevice==null) return null;
      return inDevice.getDeviceInfo();
   }
   public void enable() {
      if(inDevice==null) return;
      
      try {
         if(!inDevice.isOpen()) inDevice.open();
         inDeviceTransmitter=inDevice.getTransmitter();
      } catch(MidiUnavailableException e) {
         e.printStackTrace();
      }
      inDeviceTransmitter.setReceiver(this);
   }
   public void disable() {
      if(inDevice==null) return;
      if(inDevice.isOpen()) inDevice.close();
   }
   
   public boolean isDeviceOpen() {
      if(inDevice==null) return false;
      return inDevice.isOpen();
   }
//   public void openDevice() throws MidiUnavailableException {
//      if(inDevice!=null) {
//         if(inDevice.isOpen()) return;
//         inDevice.open();
//         inDeviceTransmitter.setReceiver(this);
//      }
//   }
//   public void closeDevice() {
//      if(inDevice!=null && inDevice.isOpen()) inDevice.close();
//   }
   

   public void addReceiver(Receiver r) {
      receivers.add(r);
   }
   public void clearReceivers() {
      receivers.clear(); //>>> transmitter
      receivers.add(OutDeviceManager.instance); //>>>
   }
   
   
   @Override
   public void close() {
      
   }
   @Override
   public void send(MidiMessage message, long timeStamp) {
      for(Receiver receiver: receivers) {
         receiver.send(message, timeStamp);
      }
      //System.err.println(message);
   }

   public static Vector<Info> getInDeviceInfos() throws MidiUnavailableException {
      final Vector<Info> inInfos=new Vector<Info>();
      final Info[] deviceInfos=MidiSystem.getMidiDeviceInfo();
      for(int i=0; i < deviceInfos.length; i++) {
         if(MidiSystem.getMidiDevice(deviceInfos[i]).getMaxTransmitters()!=0) {
            inInfos.add(deviceInfos[i]);
         }
      }
      return inInfos;
   }
}
