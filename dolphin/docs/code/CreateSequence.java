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

public class CreateSequence {
   private static final int  VELOCITY = 64;

   public static void main(String[] args) 
         throws MidiUnavailableException, InvalidMidiDataException {
      Sequence sequence=null;
      sequence = new Sequence(Sequence.PPQ, 1); //what is PPQ?

      final Track track = sequence.createTrack();

      track.add(createNoteOnEvent(60, 0));
      track.add(createNoteOffEvent(60, 2));

      track.add(createNoteOnEvent(62, 2));
      track.add(createNoteOffEvent(62, 4));

      track.add(createNoteOnEvent(64, 4));
      track.add(createNoteOffEvent(64, 6));

      track.add(createNoteOnEvent(65, 6));
      track.add(createNoteOffEvent(65, 8));

      SimpleSequencePlayer.play(sequence);
   }

   private static MidiEvent createNoteOnEvent(int key, long tick) 
         throws InvalidMidiDataException {
      return createNoteEvent(ShortMessage.NOTE_ON, key, VELOCITY, tick);
   }



   private static MidiEvent createNoteOffEvent(int key, long tick)
         throws InvalidMidiDataException {
      return createNoteEvent(ShortMessage.NOTE_OFF,
                 key,
                 0,
                 tick);
   }

   private static MidiEvent createNoteEvent(
      int command,
      int key,
      int velocity,
      long tick) throws InvalidMidiDataException
   {
      ShortMessage  message = new ShortMessage();
      message.setMessage(command,
               0, // always on channel 1
               key,
               velocity);
      MidiEvent event = new MidiEvent(message, tick);
      return event;
  }

}


