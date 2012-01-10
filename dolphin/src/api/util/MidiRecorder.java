package api.util;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.MidiDevice.Info;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import api.midi.DumpSequence;
import api.midi.InDeviceManager;
import api.midi.OutDeviceManager;



public class MidiRecorder extends JFrame {
   Sequence sequence;
   Sequencer sequencer;
   //Synthesizer synthesizer;
   MidiDevice inDevice=null;
   MidiDevice outDevice=null;
   
   public MidiRecorder() {
      setLayout(new BorderLayout());
      setTitle("Midi Recorder");
      setDefaultCloseOperation(EXIT_ON_CLOSE);
      addWindowListener(new WindowAdapter() {
         public void windowClosing(WindowEvent e) {
            if(sequencer.isOpen()) sequencer.close();
            if(outDevice.isOpen()) outDevice.close();
            if(inDevice.isOpen()) inDevice.close();
         }
      });
      
      try {
         sequence=new Sequence(Sequence.SMPTE_24, 256);
         //sequence=new Sequence(Sequence.PPQ, 480);
         sequence.createTrack();
         sequencer=MidiSystem.getSequencer(false);
         sequencer.setSequence(sequence);
         //synthesizer=MidiSystem.getSynthesizer();
         //sequencer.getTransmitter().setReceiver(synthesizer.getReceiver());
         //sequencer.getTransmitter().setReceiver(new DumpReceiver(System.out));
      } catch(InvalidMidiDataException e) {
         e.printStackTrace();
      } catch(MidiUnavailableException e) {
         e.printStackTrace();
      }
      
      Vector<Info> inInfos=null;
      try {
         inInfos=InDeviceManager.getInDeviceInfos();   
      } catch(MidiUnavailableException e) {
         e.printStackTrace();
      }
      if(inInfos.isEmpty()) throw new RuntimeException();
      final JComboBox inComboBox=new JComboBox(inInfos);
      inComboBox.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            if(inDevice!=null && inDevice.isOpen()) inDevice.close();
            try {
               final Info info=(Info)inComboBox.getSelectedItem();
               System.err.println("opening:"+info);
               inDevice=MidiSystem.getMidiDevice(info);
               inDevice.getTransmitter().setReceiver(sequencer.getReceiver());
               //inDevice.getTransmitter().setReceiver(synthesizer.getReceiver());
               //inDevice.getTransmitter().setReceiver(new DumpReceiver(System.out));
               //inDevice.open(); //>>>can't open too early?
            } catch(MidiUnavailableException e1) {
               e1.printStackTrace();
            }
         }
      });
      inComboBox.setSelectedItem(inInfos.get(0));
      add(inComboBox, BorderLayout.NORTH);
      
      Vector<Info> outInfos=null;
      try {
         outInfos=OutDeviceManager.getOutDeviceInfos();   
      } catch(MidiUnavailableException e) {
         e.printStackTrace();
      }
      if(outInfos.isEmpty()) throw new RuntimeException();
      final JComboBox outComboBox=new JComboBox(outInfos);
      outComboBox.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            if(outDevice!=null && outDevice.isOpen()) outDevice.close();
            try {
               final Info info=(Info)outComboBox.getSelectedItem();
               System.err.println("opening:"+info);
               outDevice=MidiSystem.getMidiDevice(info);
               sequencer.getTransmitter().setReceiver(outDevice.getReceiver());
               inDevice.getTransmitter().setReceiver(outDevice.getReceiver());
            } catch(MidiUnavailableException e1) {
               e1.printStackTrace();
            }
         }
      });
      outComboBox.setSelectedItem(outInfos.get(0));
      add(outComboBox, BorderLayout.CENTER);
      
      final JToolBar toolBar=new JToolBar();
      toolBar.add(new StartRecordActoin());
      toolBar.add(new StopRecordActoin());
      toolBar.add(new PlayActoin());
      add(toolBar, BorderLayout.SOUTH);
      
      pack();
      setVisible(true);
   }
   class StartRecordActoin extends AbstractAction {
      public StartRecordActoin() {
         putValue(Action.SMALL_ICON, new ImageIcon("record.png"));
      }
      public void actionPerformed(ActionEvent e) {
         try {                        
            if(!outDevice.isOpen()) outDevice.open();
            if(!sequencer.isOpen()) sequencer.open();
            if(!inDevice.isOpen()) inDevice.open();
         } catch(MidiUnavailableException ex) {
            ex.printStackTrace();
         }
         sequencer.recordEnable(sequence.getTracks()[0], -1);
         sequencer.startRecording();
         
         //sequencer.start();
         System.err.println("recording...");
      }
      
   }
   class StopRecordActoin extends AbstractAction {
      public StopRecordActoin() {
         putValue(Action.SMALL_ICON, new ImageIcon("stop.png"));
      }
      public void actionPerformed(ActionEvent e) {
         if(sequencer.isRecording()) sequencer.stop();
         /*if(synthesizer.isOpen()) synthesizer.close();
         if(sequencer.isOpen()) sequencer.close();
         if(inDevice.isOpen()) inDevice.close();*/
         System.err.println("stopped");
         //sequencer.stopRecording();
      }
      
   }
   class PlayActoin extends AbstractAction {
      public PlayActoin() {
         putValue(Action.SMALL_ICON, new ImageIcon("play.png"));
      }
      public void actionPerformed(ActionEvent e) {
         try {                        
            if(!outDevice.isOpen()) outDevice.open();
            if(!sequencer.isOpen()) sequencer.open();
            //if(!inDevice.isOpen()) inDevice.open();
         } catch(MidiUnavailableException ex) {
            ex.printStackTrace();
         }
         sequencer.setTickPosition(0);
         DumpSequence.dump(sequence);
         sequencer.start();
         System.err.println("playing...");
      }
      
   }
   
   
   
   public static void main(String[] args) {
      new MidiRecorder();
      
   }
}
