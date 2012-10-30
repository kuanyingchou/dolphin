/*
 *  CreateSequence.java
 *
 *  This file is part of jsresources.org
 */

/*
 * Copyright (c) 2000 by Matthias Pfisterer
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/*
|<---            this code is formatted to fit into 80 columns             --->|
*/

import java.io.File;
import java.io.IOException;

import javax.sound.midi.Sequence;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import javax.sound.midi.InvalidMidiDataException;


/** <titleabbrev>CreateSequence</titleabbrev>
  <title>Creating a Sequence</title>

  <formalpara><title>Purpose</title>
  <para>
  Shows how to construct a Sequence object with a Track and MidiEvents
  in memory and save it to a Standard MIDI File (SMF).
  </para></formalpara>

  <formalpara><title>Usage</title>
  <para>
  <cmdsynopsis>
  <command>java CreateSequence</command>
  <arg choice="plain"><replaceable class="parameter">midi_file</replaceable></arg>
  </cmdsynopsis>
  </para>
  </formalpara>

  <formalpara><title>Parameters</title>
  <variablelist>
  <varlistentry>
  <term><option><replaceable class="parameter">midi_file</replaceable></option></term>
  <listitem><para>the name of the file to save to as a Standard Midi File.</para></listitem>
  </varlistentry>
  </variablelist>
  </formalpara>

  <formalpara><title>Bugs, limitations</title>
  <para>None
  </para>
  </formalpara>

  <formalpara><title>Source code</title>
  <para>
  <ulink url="CreateSequence.java.html">CreateSequence.java</ulink>
  </para>
  </formalpara>
    
*/
public class CreateSequence
{
  /*  This velocity is used for all notes.
   */
  private static final int  VELOCITY = 64;


  public static void main(String[] args)
  {
    if (args.length != 1)
    {
      printUsageAndExit();
    }
    File outputFile = new File(args[0]);
    Sequence  sequence = null;
    try
    {
      sequence = new Sequence(Sequence.PPQ, 1);
    }
    catch (InvalidMidiDataException e)
    {
      e.printStackTrace();
      System.exit(1);
    }
    /* Track objects cannot be created by invoking their constructor
       directly. Instead, the Sequence object does the job. So we
       obtain the Track there. This links the Track to the Sequence
       automatically.
    */
    Track track = sequence.createTrack();

    // first chord: C major
    track.add(createNoteOnEvent(60, 0));
    track.add(createNoteOnEvent(64, 0));
    track.add(createNoteOnEvent(67, 0));
    track.add(createNoteOnEvent(72, 0));
    track.add(createNoteOffEvent(60, 1));
    track.add(createNoteOffEvent(64, 1));
    track.add(createNoteOffEvent(67, 1));
    track.add(createNoteOffEvent(72, 1));

    // second chord: f minor N
    track.add(createNoteOnEvent(53, 1));
    track.add(createNoteOnEvent(65, 1));
    track.add(createNoteOnEvent(68, 1));
    track.add(createNoteOnEvent(73, 1));
    track.add(createNoteOffEvent(63, 2));
    track.add(createNoteOffEvent(65, 2));
    track.add(createNoteOffEvent(68, 2));
    track.add(createNoteOffEvent(73, 2));

    // third chord: C major 6-4
    track.add(createNoteOnEvent(55, 2));
    track.add(createNoteOnEvent(64, 2));
    track.add(createNoteOnEvent(67, 2));
    track.add(createNoteOnEvent(72, 2));
    track.add(createNoteOffEvent(64, 3));
    track.add(createNoteOffEvent(72, 3));

    // forth chord: G major 7
    track.add(createNoteOnEvent(65, 3));
    track.add(createNoteOnEvent(71, 3));
    track.add(createNoteOffEvent(55, 4));
    track.add(createNoteOffEvent(65, 4));
    track.add(createNoteOffEvent(67, 4));
    track.add(createNoteOffEvent(71, 4));

    // fifth chord: C major
    track.add(createNoteOnEvent(48, 4));
    track.add(createNoteOnEvent(64, 4));
    track.add(createNoteOnEvent(67, 4));
    track.add(createNoteOnEvent(72, 4));
    track.add(createNoteOffEvent(48, 8));
    track.add(createNoteOffEvent(64, 8));
    track.add(createNoteOffEvent(67, 8));
    track.add(createNoteOffEvent(72, 8));

    /* Now we just save the Sequence to the file we specified.
       The '0' (second parameter) means saving as SMF type 0.
       Since we have only one Track, this is actually the only option
       (type 1 is for multiple tracks).
    */
    try
    {
      MidiSystem.write(sequence, 0, outputFile);
    }
    catch (IOException e)
    {
      e.printStackTrace();
      System.exit(1);
    }
  }



  private static MidiEvent createNoteOnEvent(int nKey, long lTick)
  {
    return createNoteEvent(ShortMessage.NOTE_ON,
                 nKey,
                 VELOCITY,
                 lTick);
  }



  private static MidiEvent createNoteOffEvent(int nKey, long lTick)
  {
    return createNoteEvent(ShortMessage.NOTE_OFF,
                 nKey,
                 0,
                 lTick);
  }



  private static MidiEvent createNoteEvent(int nCommand,
                       int nKey,
                       int nVelocity,
                       long lTick)
  {
    ShortMessage  message = new ShortMessage();
    try
    {
      message.setMessage(nCommand,
                 0, // always on channel 1
                 nKey,
                 nVelocity);
    }
    catch (InvalidMidiDataException e)
    {
      e.printStackTrace();
      System.exit(1);
    }
    MidiEvent event = new MidiEvent(message,
                      lTick);
    return event;
  }



  private static void printUsageAndExit()
  {
      out("usage:");
      out("java CreateSequence <midifile>");
      System.exit(1);
  }


  private static void out(String strMessage)
  {
    System.out.println(strMessage);
  }
}



/*** CreateSequence.java ***/
