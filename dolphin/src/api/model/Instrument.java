package api.model;

import java.util.ArrayList;
import java.util.List;

public class Instrument {
   private final String name;
   private final int value;
   private final boolean isPercussion;
   private Instrument(String n, int v) { this(n, v, false); }
   private Instrument(String n, int v, boolean p) { name=n.trim(); value=v-1; isPercussion=p; }
   public String getName() { return name; }
   public int getValue() { return value; }
   public boolean isPercussion() { return isPercussion; }
   public String toString() { return name+" ("+(value+1)+")"; }
   
   //[ factories
   
   //[ value: zero-based
   public static Instrument getInstance(int value) {
      if(value<0 || value>=all.size()) throw new IllegalArgumentException();
      return all.get(value);
   }
   public static Instrument getInstance(int v, boolean p) {
      if(v<0 || v>=all.size()) throw new IllegalArgumentException();
      for(int i=0; i < all.size(); i++) {
         final Instrument inst=all.get(i);
         if(inst.value==v && inst.isPercussion==p) return inst;
      }
      return null;
   }
   public static Instrument getInstance(String name) {
      for(int i=0; i < all.size(); i++) {
         if(all.get(i).getName().equals(name)) return all.get(i);
      }
      return null;
   }
   
   /*private static Field[] instFields=Instrument.class.getFields();*/
   public static final List<Instrument> all=new ArrayList<Instrument>();
   static {
      all.add(new Instrument( "Acoustic Grand Piano", 1));
      all.add(new Instrument( "Bright Acoustic Piano", 2));
      all.add(new Instrument( "Electric Grand Piano", 3  ));
      all.add(new Instrument( "Honkey-Tonk Piano", 4  ));
      all.add(new Instrument( "Electric Piano 1", 5  ));
      all.add(new Instrument( "Electric Piano 2", 6  ));
      all.add(new Instrument( "Harpsichord", 7  ));
      all.add(new Instrument( "Clav", 8  ));
      all.add(new Instrument( "Celesta", 9  ));
      all.add(new Instrument( "Glockenspiel", 10 ));
      all.add(new Instrument( "Music Box", 11 ));
      all.add(new Instrument( "Vibraphone", 12 ));
      all.add(new Instrument( "Marimba", 13 ));
      all.add(new Instrument( "Xylophone", 14 ));
      all.add(new Instrument( "Tubular Bells", 15 ));
      all.add(new Instrument( "Dulcimer", 16 ));
      all.add(new Instrument( "Drawbar Organ", 17 ));
      all.add(new Instrument( "Percussive Organ", 18 ));
      all.add(new Instrument( "Rock Organ", 19 ));
      all.add(new Instrument( "Church Organ", 20 ));
      all.add(new Instrument( "Reed Organ", 21 ));
      all.add(new Instrument( "Accordion", 22 ));
      all.add(new Instrument( "Harmonica", 23 ));
      all.add(new Instrument( "Tango Accordion", 24 ));
      all.add(new Instrument( "Acoustic Guitar(Nylon)", 25 ));
      all.add(new Instrument( "Acoustic Guitar(Steel)", 26 ));
      all.add(new Instrument( "Electric Guitar (Jazz)", 27 ));
      all.add(new Instrument( "Electric Guitarc (Clean)", 28 ));
      all.add(new Instrument( "Electric Guitar(Muted )", 29 ));
      all.add(new Instrument( "Overdriven Guitar", 30 ));
      all.add(new Instrument( "Distortion Guitar", 31 ));
      all.add(new Instrument( "Guitar Harmonics", 32 ));
      all.add(new Instrument( "Acoustic Bass", 33 ));
      all.add(new Instrument( "ElectricBass(finger)", 34 ));
      all.add(new Instrument( "ElectricBass (Pick)", 35 ));
      all.add(new Instrument( "Fretless Bass", 36 ));
      all.add(new Instrument( "Slap Bass 1", 37 ));
      all.add(new Instrument( "Slap Bass 2", 38 ));
      all.add(new Instrument( "Synth Bass 1", 39 ));
      all.add(new Instrument( "Synth Bass 2", 40 ));
      all.add(new Instrument( "Violin", 41 ));
      all.add(new Instrument( "Viola", 42 ));
      all.add(new Instrument( "Cello", 43 ));
      all.add(new Instrument( "Contrabass", 44 ));
      all.add(new Instrument( "Tremolo Strings", 45 ));
      all.add(new Instrument( "Pizzicato Strings", 46 ));
      all.add(new Instrument( "Orchestral Harp", 47 ));
      all.add(new Instrument( "Timpani", 48 ));
      all.add(new Instrument( "String Ensemble 1", 49 ));
      all.add(new Instrument( "String Ensemble 2", 50 ));
      all.add(new Instrument( "Synth Strings 1", 51 ));
      all.add(new Instrument( "Synth Strings 2", 52 ));
      all.add(new Instrument( "Choir Aahs", 53 ));
      all.add(new Instrument( "Voice Oohs", 54 ));
      all.add(new Instrument( "Synth Voice", 55 ));
      all.add(new Instrument( "Orchestra Hit", 56 ));
      all.add(new Instrument( "Trumpet", 57 ));
      all.add(new Instrument( "Trombone", 58 ));
      all.add(new Instrument( "Tuba", 59 ));
      all.add(new Instrument( "Muted Trumpet", 60 ));
      all.add(new Instrument( "French Horn", 61 ));
      all.add(new Instrument( "Brass Section", 62 ));
      all.add(new Instrument( "Synth Brass 1", 63 ));
      all.add(new Instrument( "Synth Brass 2", 64 ));
      all.add(new Instrument( "Soprano Sax", 65 ));
      all.add(new Instrument( "Alto Sax", 66 ));
      all.add(new Instrument( "Tenor Sax", 67 ));
      all.add(new Instrument( "Baritone Sax", 68 ));
      all.add(new Instrument( "Oboe", 69 ));
      all.add(new Instrument( "English Horn", 70 ));
      all.add(new Instrument( "Bassoon", 71 ));
      all.add(new Instrument( "Clarinet", 72 ));
      all.add(new Instrument( "Piccolo", 73 ));
      all.add(new Instrument( "Flute", 74 ));
      all.add(new Instrument( "Recorder", 75 ));
      all.add(new Instrument( "Pan Flute", 76 ));
      all.add(new Instrument( "Blown Bottle", 77 ));
      all.add(new Instrument( "Shakuhachi", 78 ));
      all.add(new Instrument( "Whistle", 79 ));
      all.add(new Instrument( "Ocarina", 80 ));
      all.add(new Instrument( "Lead 1 (Square)", 81 ));
      all.add(new Instrument( "Lead 2 (Sawtooth)", 82 ));
      all.add(new Instrument( "Lead 3 (Calliope)", 83 ));
      all.add(new Instrument( "Lead 4 (Chiff)", 84 ));
      all.add(new Instrument( "Lead 5 (Charang)", 85 ));
      all.add(new Instrument( "Lead 6 (Voice)", 86 ));
      all.add(new Instrument( "Lead 7 (Fifths)", 87 ));
      all.add(new Instrument( "Lead 8 (Bass+Lead)", 88 ));
      all.add(new Instrument( "Pad 1 (New Age)", 89 ));
      all.add(new Instrument( "Pad 1 (Warm)", 90 ));
      all.add(new Instrument( "Pad 1 (Polysynth)", 91 ));
      all.add(new Instrument( "Pad 1 (Choir)", 92 ));
      all.add(new Instrument( "Pad 1 (Bowed)", 93 ));
      all.add(new Instrument( "Pad 1 (Metallic)", 94 ));
      all.add(new Instrument( "Pad 1 (Halo)", 95 ));
      all.add(new Instrument( "Pad 1 (Sweep)", 96 ));
      all.add(new Instrument( "FX 1 (Rain)", 97 ));
      all.add(new Instrument( "FX 2 (Soundtrack)", 98 ));
      all.add(new Instrument( "FX 3 (Crystal)", 99 ));
      all.add(new Instrument( "FX 4 (Atmosphere)", 100));
      all.add(new Instrument( "FX 5 (Brightness)", 101));
      all.add(new Instrument( "FX 6 (Goblins)", 102));
      all.add(new Instrument( "FX 7 (Echoes)", 103));
      all.add(new Instrument( "FX 8 (Sci-Fi)", 104));
      all.add(new Instrument( "Sitar", 105));
      all.add(new Instrument( "Banjo", 106));
      all.add(new Instrument( "Shamisen", 107));
      all.add(new Instrument( "Koto", 108));
      all.add(new Instrument( "Kalimba", 109));
      all.add(new Instrument( "Bagpipe", 110));
      all.add(new Instrument( "Fiddle", 111));
      all.add(new Instrument( "Shanai", 112));
      all.add(new Instrument( "Tinkle Bell", 113));
      all.add(new Instrument( "Agogo", 114));
      all.add(new Instrument( "Steel Drums", 115));
      all.add(new Instrument( "Woodblock", 116));
      all.add(new Instrument( "Taiko Drum", 117));
      all.add(new Instrument( "Melodic Drum", 118));
      all.add(new Instrument( "Synth Drum", 119));
      all.add(new Instrument( "Reverse Cymbal", 120));
      all.add(new Instrument( "Guitar Fret Noise", 121));
      all.add(new Instrument( "Breath Noise", 122));
      all.add(new Instrument( "Seashore", 123));
      all.add(new Instrument( "Bird Tweet", 124));
      all.add(new Instrument( "Telephone Ring", 125));
      all.add(new Instrument( "Helicopter", 126));
      all.add(new Instrument( "Applause", 127));
      all.add(new Instrument( "Gunshot", 128));
      
      all.add(new Instrument( "Percussions", 1, true));
      //]  key range of percussions is 27 to 95 when using Java Sound Synthesizer,
      //   35 to 81 in General Midi)
      
////// percussion keys //////
// Bass Drum 2      35 
// Bass Drum 1      36
// Side Stick       37
// Snare Drum 1     38
// Hand Clap        39
// Snare Drum 2     40
// Low Tom 2        41
// Closed Hi-hat    42
// Low Tom 1        43
// Pedal Hi-hat     44
// Mid Tom 2        45
// Open Hi-hat      46
// Mid Tom 1        47
// High Tom 2       48
// Crash Cymbal 1   49
// High Tom 1       50
// Ride Cymbal 1    51
// Chinese Cymbal   52
// Ride Bell        53
// Tambourine       54
// Splash Cymbal    55
// Cowbell          56
// Crash Cymbal 2   57
// Vibra Slap       58
// Ride Cymbal 2    59
// High Bongo       60
// Low Bongo        61
// Mute High Conga  62
// Open High Conga  63
// Low Conga        64
// High Timbale     65
// Low Timbale      66
// High Agogo       67
// Low Agogo        68
// Cabasa           69
// Maracas          70
// Short Whistle    71
// Long Whistle     72
// Short Guiro      73
// Long Guiro       74
// Claves           75
// High Wood Block  76
// Low Wood Block   77
// Mute Cuica       78
// Open Cuica       79
// Mute Triangle    80
// Open Triangle    81

   }
   
}
