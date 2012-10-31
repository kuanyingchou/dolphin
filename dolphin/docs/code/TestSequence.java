import java.io.File;
import java.io.IOException;

import javax.sound.midi.Sequence;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.InvalidMidiDataException;

public class TestSequence {

   public static void main(String[] args) 
         throws MidiUnavailableException, 
                InvalidMidiDataException, 
                IOException {
      final Sequence sequence=createSequence(); 
      MidiSystem.write(sequence, 0, new File("test.mid"));
   }

   public static Sequence createSequence() throws InvalidMidiDataException {
      final Sequence sequence=new Sequence(Sequence.PPQ, 1);
      final Track track = sequence.createTrack();
      track.add(createNoteEvent(0, ShortMessage.NOTE_ON, 0, 60, 64));
      track.add(createNoteEvent(2, ShortMessage.NOTE_ON, 0, 60, 0));
      return sequence;
   }

   public static MidiEvent createNoteEvent(
         long tick, int command, int channel, int key, int velocity) 
         throws InvalidMidiDataException {
      final ShortMessage  message = new ShortMessage();
      message.setMessage(command, channel, key, velocity);
      final MidiEvent event = new MidiEvent(message, tick);
      return event;
   }
}


