package api.midi;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;

public class InstrumentMessage extends ShortMessage {
   public InstrumentMessage(int channel, int instrument) {
      try {
         setMessage(0xC0, channel, instrument, 0);
      } catch (InvalidMidiDataException e) {
         e.printStackTrace();
      }
   }
}
