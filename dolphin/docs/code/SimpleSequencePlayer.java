import java.io.File;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.InvalidMidiDataException;

class SimpleSequencePlayer {
   public static void play(Sequence sequence) throws MidiUnavailableException, InvalidMidiDataException {
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
