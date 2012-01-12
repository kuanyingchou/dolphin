package api.midi;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;

public class VolumeMessage extends ShortMessage {
   public VolumeMessage(int channel, int volume) {
      try {
         setMessage(0xb0, channel, 0x07, volume);
      } catch (InvalidMidiDataException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }
}
