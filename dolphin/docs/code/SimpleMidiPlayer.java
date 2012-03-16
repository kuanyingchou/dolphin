import java.io.File;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MetaEventListener;

class SimpleMidiPlayer {
   public static void play(String filename) throws Exception {
      final File midiFile=new File(filename);
      final Sequence sequence=MidiSystem.getSequence(midiFile);
      final Sequencer sequencer=MidiSystem.getSequencer();
      sequencer.open();
      sequencer.setSequence(sequence);
      final Synthesizer synthesizer=MidiSystem.getSynthesizer();
      synthesizer.open();
      final Transmitter transmitter=sequencer.getTransmitter();
      final Receiver receiver=synthesizer.getReceiver();
      transmitter.setReceiver(receiver);
      sequencer.start();
   }
}
