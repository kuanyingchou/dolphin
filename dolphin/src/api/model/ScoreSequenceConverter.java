package api.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import api.midi.DumpSequence;
import api.midi.EndOfTrackMessage;
import api.midi.InstrumentMessage;
import api.midi.NoteOnMessage;
import api.midi.PanMessage;
import api.midi.VolumeMessage;
import api.util.Util;

public class ScoreSequenceConverter {
   
   private static class PitchTickLength {
      int pitch;
      long tick;
      long length;
      public PitchTickLength(int p, long t, long l) {
         pitch=p;
         tick=t;
         length=l;
      }
      public String toString() {
         return Util.getObjectInfo(this);
      }
   }
   
   public Score fromSequence(Sequence sequence) {
      if(sequence==null) throw new IllegalArgumentException();
      if(sequence.getDivisionType()!=Sequence.PPQ) throw new IllegalArgumentException(); //>>>

      DumpSequence.dump(sequence);

      final Score score=new Score();
      getScoreInfos(score, sequence);
      
      //System.err.println(score);
      //System.exit(0);
      
      final int resolution=sequence.getResolution();
      final Track[] tracks=sequence.getTracks();
      
NEXT_TRACK:
      for(int trackIndex=0; trackIndex < tracks.length; trackIndex++) {
         final Part part=new Part();
         getPartInfos(part, tracks[trackIndex]);
         final java.util.List<PitchTickLength> ptls=new java.util.ArrayList<PitchTickLength>();
         
NEXT_EVENT:      
         for(int eventIndex=0; eventIndex < tracks[trackIndex].size(); eventIndex++) {
            final MidiEvent event=tracks[trackIndex].get(eventIndex);
            final MidiMessage message=event.getMessage();
            
            if(message instanceof ShortMessage) {
               final ShortMessage shortMsg=(ShortMessage) message;
               if(shortMsg.getCommand() == 0x90 && shortMsg.getData2() != 0) { //[ note on
                  final int channel=shortMsg.getChannel();
                  final int pitch=shortMsg.getData1();
                  
                  //[ find it's note-off
                  long endTick=event.getTick();
                  for(int i=eventIndex; i<tracks[trackIndex].size(); i++) {
                     final MidiEvent e=tracks[trackIndex].get(i);
                     final MidiMessage m=e.getMessage();
                     if(m instanceof ShortMessage) {
                        final ShortMessage sm=(ShortMessage) m;
                        if(sm.getCommand()== 0x80 || (sm.getCommand() == 0x90 && sm.getData2() == 0)) { //[ note off)
                           if(sm.getData1()==pitch) { //[ found
                              endTick=e.getTick();
                              break;
                           }
                        }
                     }
                  }
                  if(endTick==event.getTick()) { //length==0 or no note-off
                     continue NEXT_EVENT;
                  }
                  ptls.add(new PitchTickLength(pitch, event.getTick(), endTick-event.getTick()));
                  
               }
            } else if(message instanceof MetaMessage) {
               
            }

         }
         
         if(ptls.isEmpty()) continue NEXT_TRACK;
         
         //[ expand notes
//         for(int i=0; i < ptls.size(); i++) {
//            //0.7
//            //0.75
//            //0.8
//            //0.85
//            //0.9
//            //0.95
//            //ptls.get(i).length=(int)Math.round(ptls.get(i).length/0.9375);
//            ptls.get(i).length+=15;
//            //else ptls.get(i).length+=7;
//         }
         
         //[ sort by tick
         Collections.sort(ptls, new Comparator<PitchTickLength>() {
            @Override
            public int compare(PitchTickLength o1, PitchTickLength o2) {
               return (int)(o1.tick-o2.tick);
            }
         });

         long currentTick=0;
         for(int i=0; i<ptls.size(); i++) {
            final PitchTickLength first=ptls.get(i);
            
            //[ fill in rests before this note
            if(currentTick<first.tick) {  
               final long d=first.tick-currentTick;
               part.add(new Note(-1, tickToLength(d, resolution)));
               currentTick=first.tick;
            } else if(currentTick>first.tick) { 
               //found concurrent note(chord at different start position)
               //>>> drop it
               continue;
               //throw new RuntimeException(); //it's an error
            }
            
            //[ find longest note at currentTick
            int j=i+1;
            PitchTickLength longest=first;
            for(; j<ptls.size(); j++) {
               final PitchTickLength next=ptls.get(j);
               if(first.tick==next.tick) {
                  if(next.length>longest.length) {
                     longest=next;
                  }
               } else if(next.tick>longest.tick+longest.length) {
                  final long diff=next.tick-(longest.tick+longest.length);
//                  if(diff<longest.length*0.3) { //>>>right?
//                     longest.length+=diff; //: expand
//                  }  else {//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
//                     //followed by a rest
//                     for(int k=128; k>=1; k/=2) {
//                        final int t=resolution*4/k;
//                        
//                     }
//                  }
                  break;
               }
            }
            
            final Note longestNote=new Note(
                  longest.pitch, tickToLength(longest.length, resolution));
            
            //[ create chord list
            Note runner=longestNote;
            j=i;
            for(; j<ptls.size(); j++) {
               final PitchTickLength next=ptls.get(j);
               if(next==longest) continue;
               if(longest.tick==next.tick) { //chord at same start position
                  runner.chord=new Note(next.pitch, tickToLength(next.length, resolution));
                  runner.chord.delay=0;
               } else if(next.tick>longest.tick && next.tick<longest.tick+longest.length) {
                  //[ >>> asynchronous chord
                  runner.chord=new Note(next.pitch, tickToLength(next.length, resolution));
                  runner.chord.delay=tickToLength(next.tick-longest.tick, resolution);
               } else if(next.tick<longest.tick) {
                  throw new RuntimeException(); //an error
               } else { //[ next.tick>longest.tick+longest.length: not a chord
                  break;
               }
               runner=runner.chord;
            }
            i=j-1;
            
            part.add(longestNote);
            currentTick+=longest.length; //currentTick+=first.length;
//            lastTick=ptl.tick;
         }
         if(part.noteCount()>0) score.add(part);
      }
      separateLongNotes(score); //>>> may conflict with other separateXXX methods
      separateCrossNotes(score); //>>> may conflict with other separateXXX methods
      findTriplets(score);
      findDots(score);
      separateCombinedNotes(score); //>>> may conflict with other separateXXX methods
      //normalizeUnknowNotes(score); //>>>experiments
//System.err.println(score);
      System.err.println(getUnknownNotesCount(score));
      
      return score;
   }
   
   public Sequence toSequence(Score score) throws InvalidMidiDataException {
      final int RESOLUTION=480;
      final Sequence sequence=new Sequence(Sequence.PPQ, RESOLUTION);
      
      final Track infoTrack=sequence.createTrack();
      
      //[ title
      final MetaMessage titleMsg=new MetaMessage();
      final byte[] titleBytes=score.getTitle().getBytes();
      titleMsg.setMessage(3, titleBytes, titleBytes.length);
      infoTrack.add(new MidiEvent(titleMsg, 0));
   
      //[ time sig.
      final MetaMessage timeSigMsg=new MetaMessage();
      timeSigMsg.setMessage(0x58, new byte[] {
            (byte)score.getBeatsPerMeasure(),
            (byte)(Util.log2(score.getNoteValuePerBeat())),
            0, //>>> MIDI clocks per metronome tick
            0  //>>> 1/32 per 24 MIDI clocks
      }, 4);
      infoTrack.add(new MidiEvent(timeSigMsg, 0));
   
      //[ key sig.
      final MetaMessage keySigMsg=new MetaMessage();
      keySigMsg.setMessage(0x59, new byte[] {
            (byte)(score.keySignature.getValue()),
            (byte)(score.keySignature.isMajor()?0:1)
      }, 2);
      infoTrack.add(new MidiEvent(keySigMsg, 0));
      
      //[ tempo
      final int TEMPO_MS=(int)(60000000.0 / score.getTempo());
      final MetaMessage tempoMsg=new MetaMessage();
      tempoMsg.setMessage(0x51, new byte[] {
         (byte) (TEMPO_MS>>>16 & 0xFF),
         (byte) (TEMPO_MS>>>8 & 0xFF),
         (byte) (TEMPO_MS & 0xFF)
      }, 3);
      infoTrack.add(new MidiEvent(tempoMsg, 0));
      
      final int beatLength=Note.WHOLE_LENGTH/score.getNoteValuePerBeat();
      final int barLength=beatLength*score.getBeatsPerMeasure();
      
      final Instrument[] channelInsts=new Instrument[16];
      int channelIndex=0;
      
      for(int partIndex=0; partIndex<score.partCount(); partIndex++) {
         final Part part=score.get(partIndex);
         if(part.isMute()) continue;
         int channel=-1;
         if(part.getInstrument().isPercussion()) {
            channel=9;
         } else {
            //[ find reusable channel >>> may break volume/pan settings
            for(int i=0; i < channelInsts.length; i++) {
               if(channelInsts[i]==part.getInstrument()) {
                  channel=i;
                  break;
               }
            }
            //[ use a new channel
            if(channel<0) {
               if(channelIndex==16) break; //>>> too many channels, give them up
               channel=channelIndex;
               channelInsts[channel]=part.getInstrument();
               channelIndex++;
               if(channelIndex==9) channelIndex++; 
               //] jump percussion channel
            }
         }
         part.channelBinding=channel; //>>> for StereoFieldEditor
         
         final Track track=sequence.createTrack();
         
         //[ instrument
         if(channel!=9) {
            final ShortMessage pc=new InstrumentMessage(channel, part.getInstrument().getValue());
            track.add(new MidiEvent(pc, 0));
         }
         
         //[ volume
         final ShortMessage cc=new VolumeMessage(channel, part.getVolume());
         track.add(new MidiEvent(cc, 0));
         
         //[ pan
         final ShortMessage cv=new PanMessage(channel, part.getPan());
         track.add(new MidiEvent(cv, 0));
         
         //[ for beat strength
         int lengthCount=0;
         int strength=100;
         
         int currentTick=0;
         
         for(int noteIndex=0; noteIndex<part.noteCount(); noteIndex++) {
            final Note firstNote=part.get(noteIndex);
            int firstLength=firstNote.getActualLength();
            
            //[ tie
            if(firstNote.isTieStart()) {
               for(++noteIndex;noteIndex<part.noteCount(); noteIndex++) {
                  final Note n=part.get(noteIndex);
                  firstLength+=n.getActualLength();   
                  if(n.isTieEnd()) break;
               }
            }
            //n.setTick(currentTick);
            
            //[ rhythm
            final int d=lengthCount/beatLength;
            final int r=lengthCount%beatLength;
            if(r==0) {
               strength=score.getBeatStrength(d);
            }
            if(lengthCount%barLength==0) { //first beat boost
               strength+=10;
               if(strength>=128) strength=127;
            }
            lengthCount+=firstLength;//note.getActualLength(); >>> verify it
            
            if(!firstNote.isRest()) {
               Note runner=firstNote;
               boolean firstFlag=true;
               while(true) {
                  final ShortMessage noteOnMsg=new NoteOnMessage(channel, runner.pitch, strength);
                  //System.err.println(strength);
                  runner.binding=noteOnMsg;
                  final long t=currentTick+lengthToTick(runner.delay, RESOLUTION);
                  runner.setTick(t);
                  track.add(new MidiEvent(noteOnMsg, t));
                  final ShortMessage noteOffMsg=new NoteOnMessage(channel, runner.pitch, 0);
                  //currentTick+=lengthToTick(runner.length, RESOLUTION);
                  track.add(new MidiEvent(noteOffMsg, 
                        t+lengthToTick(firstFlag?firstLength:runner.length, RESOLUTION)));
                  if(runner.chord==null) {
                     break;
                  } else {
                     runner=runner.chord;
                  }
                  firstFlag=false;
                  //break; //>>> disable chord
               }   
            }
            currentTick+=lengthToTick(firstLength, RESOLUTION);
         }
         
         final MetaMessage eotMsg=new EndOfTrackMessage();
         infoTrack.add(new MidiEvent(eotMsg, track.get(track.size()-1).getTick()+500));
      }
      
      return sequence;
   }
   private static void separateCrossNotes(Score score) { //[ separate note on bar-lines(cross-note)
      final int barLength=Note.WHOLE_LENGTH/score.getNoteValuePerBeat()*score.getBeatsPerMeasure();
      
      for(int partIndex=0; partIndex<score.partCount(); partIndex++) {
         final Part part=score.get(partIndex);
         int lengthCount=0;
         for(int noteIndex=0; noteIndex<part.noteCount(); noteIndex++) {
            final Note note=part.get(noteIndex);
            lengthCount+=note.length;
            if(lengthCount==barLength) {
               lengthCount=0;
            } else if(lengthCount>barLength) { //found cross-note
               final int newLen=lengthCount-barLength;
               part.remove(noteIndex);
               part.add(
                  new Note[] {
                     new Note(note.pitch, note.length-newLen, 0, note.isRest()?0:1, false),
                     new Note(note.pitch, newLen, 0, note.isRest()?0:-1, false)
                  }, noteIndex);
               
               lengthCount=0;
               noteIndex++;
            } 
            
         }
      }
   }
   private static void separateCombinedNotes(Score score) { 
      for(int partIndex=0; partIndex<score.partCount(); partIndex++) {
         final Part part=score.get(partIndex);
         for(int noteIndex=0; noteIndex<score.get(partIndex).noteCount(); noteIndex++) {
            final Note note=part.get(noteIndex);
            int noteLength=note.length;
            if(!isRegularLength(noteLength)) {
               final List<Note> newNotes=new ArrayList<Note>();
               for(int j=2; j<=128; j*=2) {
                  if(noteLength>=Note.WHOLE_LENGTH/j) {
                     final Note n=new Note(note.pitch, Note.WHOLE_LENGTH/j);
                     newNotes.add(n);
                     noteLength-=n.length;
                     if(noteLength<0) break;
                  }
               }
               if(noteLength==0 && newNotes.size()>1) { //combination found
                  if(!note.isRest()) {
                     newNotes.get(0).tie=1;
                     newNotes.get(newNotes.size()-1).tie=-1;
                  }
                  part.remove(noteIndex);
                  part.add(newNotes.toArray(new Note[newNotes.size()]), noteIndex);
                  //] >>> the order may be different: p41_1
                  noteIndex+=newNotes.size()-1;
               }
            }            
            
         }
      }
   }
   private static void separateLongNotes(Score score) { //[ separate notes longer than whole-length
      for(int partIndex=0; partIndex<score.partCount(); partIndex++) {
         final Part part=score.get(partIndex);
         for(int noteIndex=0; noteIndex<score.get(partIndex).noteCount(); noteIndex++) {
            final Note note=part.get(noteIndex);
            int noteLength=note.length;
            if(noteLength<=Note.WHOLE_LENGTH) continue;
            final List<Note> newNotes=new ArrayList<Note>();
            while(noteLength>Note.WHOLE_LENGTH) {
               final Note n=new Note(note.pitch, Note.WHOLE_LENGTH);
               newNotes.add(n);
               noteLength-=Note.WHOLE_LENGTH;
            } 
            if(newNotes.isEmpty()) throw new RuntimeException();
            if(noteLength>0) { 
               final Note last=new Note(note.pitch, noteLength);
               newNotes.add(last);
            } else if(noteLength==0){
               //fine
            } else {
               throw new RuntimeException();
            }
            
            int lengthSum=0;
            for(int i=0; i < newNotes.size(); i++) {
               lengthSum+=newNotes.get(i).length;
            }
            if(lengthSum!=note.length) throw new RuntimeException();
            
            if(!note.isRest() && newNotes.size()>1) {
               newNotes.get(0).tie=1;
               newNotes.get(newNotes.size()-1).tie=-1;   
            }
            part.remove(noteIndex);
            part.add(newNotes.toArray(new Note[newNotes.size()]), noteIndex);
            noteIndex+=(newNotes.size()-1); //>>>
         }
      }
      
   }
   private static void findDots(Score score) {
      for(int partIndex=0; partIndex<score.partCount(); partIndex++) {
         final Part part=score.get(partIndex);
NEXT_NOTE:  for(int noteIndex=0; noteIndex<part.noteCount(); noteIndex++) {
               final Note note=part.get(noteIndex);
               if(note.isTripletElement) continue NEXT_NOTE;
               for(int i=2; i<=128; i*=2) {
                  final int shorterLength=Note.WHOLE_LENGTH/i;
                  if(note.length>shorterLength) {
                     int d=2;
                     int tryLength=shorterLength;
                     for(int j=0; j<Note.MAX_DOT; j++) {
                        tryLength+=shorterLength/d;
                        if(tryLength==note.length) {
                           note.length=shorterLength;
                           note.dot=j+1;
                           continue NEXT_NOTE;
                        }
                        d*=2;
                     }  
                  }
               }
         }
      }
   }
   private static void findTriplets(Score score) {
      for(int partIndex=0; partIndex<score.partCount(); partIndex++) {
         final Part part=score.get(partIndex);
         for(int noteIndex=0; noteIndex<part.noteCount(); noteIndex++) {
            final Note note=part.get(noteIndex);
            if(!isRegularLength(note.length)) {
               for(int i=128; i>=1; i/=2) {
                  final int longerLength=Note.WHOLE_LENGTH/i;
                  if(note.length<longerLength) {
                     if(note.length==longerLength*2/3) {
                        note.length=longerLength;
                        note.isTripletElement=true;
                     }
                  }
               }
            }
         }
      }
   }
//   private static void normalizeUnknowNotes(Score score) {
//      for(int partIndex=0; partIndex<score.partCount(); partIndex++) {
//         final Part part=score.get(partIndex);
//         for(int noteIndex=0; noteIndex<part.noteCount(); noteIndex++) {
//            final Note note=part.get(noteIndex);
//            if(noteIndex+1<part.noteCount()) { //has next rest
//               final Note next=part.get(noteIndex+1);
//               if(!next.isRest()) continue; //don't know what to do 
//               if(!isRegularLength(note.getActualLength()) || !isRegularLength(next.getActualLength())) {
//                  for(int i=128; i>=1; i/=2) {
//                     final int longerLength=Note.WHOLE_LENGTH/i;
//                     if(note.getActualLength()<longerLength && 
//                           longerLength<note.getActualLength()+next.getActualLength()) {
//                        note.length=longerLength;
//                        note.dot=0; //>>> no dots
//                        next.length=next.length-note.length;
//                        next.dot=0;
//                     }
//                  }
//               }
//            } else { //last note
//               //>>>
//            }
//         }
//      }
//   }
   private static int getUnknownNotesCount(Score score) {
      int count=0;
      for(int partIndex=0; partIndex<score.partCount(); partIndex++) {
         final Part part=score.get(partIndex);
         for(int noteIndex=0; noteIndex<part.noteCount(); noteIndex++) {
            final Note note=part.get(noteIndex);
            if(!isRegularLength(note.length)) count++;
         }
      }
      return count;
   }
   
   //>>> findLengthDotGreaterThanEquals(int len);
   //>>> findLengthDotLessThanEquals(int len);
   //>>> findLengthDotInclusiveBetween(int len);
   
   private static void getScoreInfos(Score score, Sequence sequence) {
      final Track[] tracks=sequence.getTracks();
      if(tracks.length <= 0) return;
      for(int j=0; j < tracks[0].size(); j++) {
         final MidiEvent event=tracks[0].get(j);
         if(event.getTick() != 0) break;
         final MidiMessage message=event.getMessage();
         if(message instanceof MetaMessage) {
            final MetaMessage mm=(MetaMessage) message;
            byte[] abData=mm.getData();
            switch(mm.getType()) {
            case 3:
               final String title=new String(abData);
               if(!score.getTitle().isEmpty()) score.setTitle(title);
               break;
            case 0x51:
               float nTempo=((abData[0] & 0xFF) << 16)
                     | ((abData[1] & 0xFF) << 8) | (abData[2] & 0xFF);
               if(nTempo <= 0) {
                  nTempo=0.1f;
               }
               score.setTempo(60000000.0f / nTempo);
               break;
            case 0x58:
               score.setBeatsPerMeasure((abData[0] & 0xFF));
//System.err.println("in: "+(1 << (abData[1] & 0xFF)));               
               score.setNoteValuePerBeat((1 << (abData[1] & 0xFF)));
               break;
            case 0x59:
               final boolean isMajorKey=(abData[1] != 1);
               final int keyValue=abData[0];
               score.keySignature=Key.get(keyValue, isMajorKey);
               if(score.keySignature==null) {
                  throw new RuntimeException(keyValue+": "+isMajorKey);
               }
               break;

            }
         }

      }
   }
   private static void getPartInfos(Part part, Track track) {
      for(int eventIndex=0; eventIndex<track.size(); eventIndex++) {
         final MidiEvent midiEvent=track.get(eventIndex);
         if(midiEvent.getTick()!=0) break;
         final MidiMessage midiMessage=midiEvent.getMessage();
         if(midiMessage instanceof ShortMessage) {
            final ShortMessage sm=(ShortMessage) midiMessage;
            switch(sm.getCommand()) {
            case 0xC0:
               Instrument inst=null;
               if(sm.getChannel()==9) {
                  inst=Instrument.getInstance(0, true);
                  //if(inst==null) inst=Instrument.getInstance(34, true);
               } else {
                  inst=Instrument.getInstance(sm.getData1());
               }
               if(inst==null) inst=Instrument.getInstance(0);
               part.setInstrument(inst);
               break;
            case 0x90: 
               //>>> for percussion, what if first note is not at tick 0? 
               if(sm.getChannel()==9) {
                  inst=Instrument.getInstance(0, true);
                  part.setInstrument(inst);
               }
               break;
            case 0xB0:
               if(sm.getData1()==0x07) {
                  part.setVolume(sm.getData2());
               } else if(sm.getData1()==0x0A) {
                  part.setPan(sm.getData2());
               }
               break;
            }
            
         }
      }
   }
   private static int tickToLength(long tick, int resolution) {
      return (int)Math.round(((double)tick/resolution)*(Note.WHOLE_LENGTH/4));
   }
   private static int lengthToTick(int length, int resolution) {
      return (int)Math.round((double)length*resolution/(Note.WHOLE_LENGTH/4));
   }
//   private static boolean isNormal(long tickLen, int resolution) {
//      final long noteLen=tickLen*Note.WHOLE_LENGTH/(resolution*4);
//      for(int i=Note.WHOLE_LENGTH; i>=1; i/=2) {
//         if(noteLen==i) return true;
//      }
//      return false;
//   }
   private static boolean isRegularLength(int length) {
      for(int i=1; i<=128; i*=2) {
         if(Note.WHOLE_LENGTH/i==length) return true;
      }
      return false;
   }
   public static boolean like(double a, double b) {
      return a==b;
      /*if(Math.abs(a-b)<0.00001) {
         return true;
      } else {
         return false;
      }*/
   }
   public static void printLength(double len) {
      double temp=16;
      for(int i=0; i<10; i++) {
         if(like(len, temp)) {
            System.err.println(temp);
            return;
         }
         temp/=2;
      } 
      System.err.println("no match: "+len);
   }
}
