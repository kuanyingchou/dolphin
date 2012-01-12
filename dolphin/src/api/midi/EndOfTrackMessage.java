package api.midi;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;

public class EndOfTrackMessage extends MetaMessage {
   public EndOfTrackMessage() {
      try {
         setMessage(0x2F, new byte[0], 0);
      } catch (InvalidMidiDataException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }
}
