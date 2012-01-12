package api.midi;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;

public class NoteOffMessage extends ShortMessage {

   public NoteOffMessage(int channel, int pitch, int velocity) {
      try {
         setMessage(ShortMessage.NOTE_OFF, channel, pitch, velocity);
      } catch (InvalidMidiDataException e) {
         e.printStackTrace();
      }
   }

}
