package api.midi;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;

public class PanMessage extends ShortMessage {
   public PanMessage(int channel, int pan) {
      try {
         setMessage(0xb0, channel, 0x0A, pan);
      } catch (InvalidMidiDataException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }
}
