package test;


import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import api.midi.DumpReceiver;
import api.midi.DumpSequence;
import api.util.Util;




public class Test {
   public static void test(final File midiFile) throws Exception {
      final Sequence sequence=MidiSystem.getSequence(midiFile);
      Track t=sequence.getTracks()[0];
      System.err.println("tick length: "+sequence.getTickLength());
      for(int i=0; i < t.size(); i++) {
         final MidiEvent event=t.get(i);
         System.err.println("event#"+i+":");
         System.err.println("   tick: "+event.getTick());
         System.err.println(event.getMessage());
      }
      
   }
   public static void testDumpReceiver(File midiFile) throws MidiUnavailableException, InvalidMidiDataException, IOException {
      final Sequence sequence=MidiSystem.getSequence(midiFile);
      final Sequencer sequencer=MidiSystem.getSequencer();
      sequencer.open();
      sequencer.setSequence(sequence);
      
      DumpReceiver dr=new DumpReceiver(System.out);
      sequencer.getTransmitter().setReceiver(dr);
      
      sequencer.start();
      
      //sequencer.close();

   }
   public static void testDumpSequencer(File midiFile) {
      DumpSequence.main(new String[] {midiFile.getPath()});
      System.err.println("=========================================================================");
   }
   public static void testMidiOut() throws InvalidMidiDataException, IOException, MidiUnavailableException {
      final int RESOLUTION=480;
      final Sequence sequence=new Sequence(Sequence.PPQ, RESOLUTION);
      final Track track=sequence.createTrack();
      
      final int TEMPO=60;
      final int TEMPO_MS=(int)(60000000.0 / TEMPO);
      final MetaMessage tempoMsg=new MetaMessage();
      tempoMsg.setMessage(0x51, new byte[] {
         (byte) (TEMPO_MS>>>16 & 0xFF),
         (byte) (TEMPO_MS>>>8 & 0xFF),
         (byte) (TEMPO_MS & 0xFF)
      }, 3);
      track.add(new MidiEvent(tempoMsg, 0));
      
      /*final byte[] inst="Piccolo".getBytes();
      final MetaMessage instMsg=new MetaMessage();
      instMsg.setMessage(4, inst, inst.length);
      final MetaMessage channelPrefixMsg=new MetaMessage();
      channelPrefixMsg.setMessage(0x20, new byte[] {1}, 1);
      track.add(new MidiEvent(channelPrefixMsg, 0));
      track.add(new MidiEvent(instMsg, 0));*/
      final ShortMessage pc=new ShortMessage();
      pc.setMessage(0xC0, 0, 20, 0);
      track.add(new MidiEvent(pc, 0));
      
      MidiEvent event;
      for(int i=0; i<3; i++) {
         final ShortMessage sm=new ShortMessage();
         sm.setMessage(0x90, 0, 60+i*2, 127);
         event=new MidiEvent(sm, i*RESOLUTION);
         track.add(event);
      }
      for(int i=0; i<3; i++) {
         final ShortMessage sm=new ShortMessage();
         sm.setMessage(0x90, 0, 60+i*2, 0);
         event=new MidiEvent(sm, (i+1)*RESOLUTION);
         track.add(event);
      }
      
      final MetaMessage eotMsg=new MetaMessage();
      eotMsg.setMessage(0x2F, new byte[0], 0);
      track.add(new MidiEvent(eotMsg, RESOLUTION*3));
      
      MidiSystem.write(sequence, 0, new File("testOut.mid"));
      testDumpSequencer(new File("testOut.mid"));
      
      final Sequencer sequencer=MidiSystem.getSequencer();
      sequencer.setSequence(sequence);
      sequencer.getTransmitter().setReceiver(new DumpReceiver(System.out));
      sequencer.open();
      sequencer.start();
      
      //final int[] midiFileTypes=MidiSystem.getMidiFileTypes();
      //System.err.println(Arrays.toString(midiFileTypes));
      
   }
   public static void testShift() {
      final byte i=4;
      System.err.println((i&0xff)<<1);
      System.err.println(i<<1);
      //System.err.println(i>>>1);
   }
   public static void testVersion() {
      final Package p=Package.getPackage("java.lang");
      System.err.println(p.getImplementationVersion());
   }
   public static void testNullCast() {
      Object o=null;
      String s=(String)o;
      System.err.println(s);
   }
   public static void testIf() {
      if(true) {
         
      } else {if(true) {
         
      }}
   }
   
   public static void main(String[] args) throws Exception {
      //test(new File("testToMidi.mid"));
      //testDumpReceiver(new File("/home/ken/Desktop/test_sibelius.mid"));
      /*testDumpSequencer(new File("test/drmf_c44.mid"));
      testDumpSequencer(new File("test/drmf_c48.mid"));
      testDumpSequencer(new File("test/drmf_c44_160.mid"));
      testDumpSequencer(new File("test/drmf_c48_160.mid"));*/
      //Score.fromSequence(MidiSystem.getSequence(new File("testToMidi.mid")));
      //Score.fromSequence(MidiSystem.getSequence(new File("titanic3.mid")));
      
      //testDumpSequencer(new File("test/titanic3.mid"));
      //testMidiOut();
      //testShift();
      //testVersion();
      System.err.println(Font.createFont(Font.TRUETYPE_FONT, Util.getResourceAsStream("CLEFS___.TTF")));
   }
}
