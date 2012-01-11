package api.midi;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;

public class NoteOnMessage extends ShortMessage {
   public NoteOnMessage(int channel, int pitch, int velocity) {
      try {
         setMessage(0x90, channel, pitch, velocity);
      } catch (InvalidMidiDataException e) {
         e.printStackTrace();
      }
   }
}
