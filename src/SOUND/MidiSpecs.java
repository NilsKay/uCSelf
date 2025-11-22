package SOUND;


public class MidiSpecs {

/*

=========================================================================

Table 1: MIDI 1.0 Specification Message Summary
             Updated 1995 By the MIDI Manufacturers Association


Status       Data Byte(s)     Description
D7----D0     D7----D0
-------------------------------------------------------------------------
Channel Voice Messages
-------------------------------------------------------------------------
1000cccc     0kkkkkkk         Note Off event.
             0vvvvvvv         This message is sent when a
                              note is released (ended).
                              (kkkkkkk) is the key (note) number.
                              (vvvvvvv) is the velocity.

1001cccc     0kkkkkkk         Note On event.
             0vvvvvvv         This message is sent when a
                              note is depressed (start).
                              (kkkkkkk) is the key (note) number.
                              (vvvvvvv) is the velocity.

1010cccc     0kkkkkkk         Polyphonic Key Pressure (Aftertouch).
             0vvvvvvv         This message is most often sent by
      pressing down on the key after it
      "bottoms out".
                              (kkkkkkk) is the key (note) number.
                              (vvvvvvv) is the pressure value.

1011cccc     0ccccccc         Control Change.
             0vvvvvvv         This message is sent when a controller
                              value changes.  Controllers include devices
                              such as pedals and levers.
                              Certain controller numbers are reserved
                              for specific purposes. See Channel Mode Messages.
                              (ccccccc) is the controller number.
                              (vvvvvvv) is the new value.

1100cccc     0ppppppp         Program Change.
                              This message sent when the patch number changes.
                              (ppppppp) is the new program number.

1101nnnn     0vvvvvvv         Channel Pressure (After-touch).
                              This message is most often sent by pressing down
      on the key after it "bottoms out". This message
      is different from polyphonic after-touch. Use
      this message to send the single greatest
                              pressure value (of all the current depressed keys).
                              (vvvvvvv) is the pressure value.

1110nnnn     0lllllll         Pitch Wheel Change.
             0mmmmmmm         This message is sent to indicate a change in the
                              pitch wheel.  The pitch wheel is measured by a
                              fourteen bit value. Center (no pitch change) is
                              2000H.  Sensitivity is a function of the
                              transmitter.
                              (llllll) are the least significant 7 bits.
                              (mmmmmm) are the most significant 7 bits.

-------------------------------------------------------------------------
Channel Mode Messages  (See also Control Change, above)
-------------------------------------------------------------------------
1011nnnn     0ccccccc         Channel Mode Messages.
             0vvvvvvv         This the same code as the Control
                              Change (above), but implements Mode
                              control by using reserved controller
                              numbers.  The numbers are:

                              Local Control.
                              When Local Control is Off, all devices
                              on a given channel will respond only to
                              data received over MIDI.  Played data, etc.
                              will be ignored.  Local Control On
                              restores the functions of the normal
                              controllers.
                              c = 122, v =   0: Local Control Off
                              c = 122, v = 127: Local Control On

                              All Notes Off.
                              When an All Notes Off is received,
                              all oscillators will turn off.
                              c = 123, v =   0: All Notes Off
                             (See text for description of actual
                               mode commands.)

=============================================================================

         Table 4: Summary of MIDI Note Numbers for Different Octaves
 (adapted from "MIDI by the Numbers" by D. Valenti - Electronic Musician 2/88)
               Updated 1995 By the MIDI Manufacturers Association


Octave||                     Note Numbers
   #  ||
      || C   | C#  | D   | D#  | E   | F   | F#  | G   | G#  | A   | A#  | B
------------------------------------------------------------------------------
  -1  ||   0 |   1 |   2 |   3 |   4 |   5 |   6 |   7 |   8 |   9 |  10 |  11
   0  ||  12 |  13 |  14 |  15 |  16 |  17 |  18 |  19 |  20 |  21 |  22 |  23
   1  ||  24 |  25 |  26 |  27 |  28 |  29 |  30 |  31 |  32 |  33 |  34 |  35
   2  ||  36 |  37 |  38 |  39 |  40 |  41 |  42 |  43 |  44 |  45 |  46 |  47
   3  ||  48 |  49 |  50 |  51 |  52 |  53 |  54 |  55 |  56 |  57 |  58 |  59
   4  ||  60 |  61 |  62 |  63 |  64 |  65 |  66 |  67 |  68 |  69 |  70 |  71
   5  ||  72 |  73 |  74 |  75 |  76 |  77 |  78 |  79 |  80 |  81 |  82 |  83
   6  ||  84 |  85 |  86 |  87 |  88 |  89 |  90 |  91 |  92 |  93 |  94 |  95
   7  ||  96 |  97 |  98 |  99 | 100 | 101 | 102 | 103 | 104 | 105 | 106 | 107
   8  || 108 | 109 | 110 | 111 | 112 | 113 | 114 | 115 | 116 | 117 | 118 | 119
   9  || 120 | 121 | 122 | 123 | 124 | 125 | 126 | 127 |

------------------------------------------------------------------------------

         Table 3: Controller and Mode Changes (Status Bytes 176-191)
  (adapted from "MIDI by the Numbers" by D. Valenti-Electronic Musician 2/88)
              Updated 1995 By the MIDI Manufacturers Association
    2nd Byte Value |              Function                  |  3rd Byte
  Binary  |Hex|Dec |                                        | Value  |  Use
 - - - - -|- -|- - | - - - - - - - - - - - - - - - - - - - -|- - - - | - - - -
 00000000= 00=   0 | Bank Select                            | 0-127  |  MSB
 00000001= 01=   1 | Modulation wheel                       | 0-127  |  MSB
 00000010= 02=   2 | Breath control                         | 0-127  |  MSB
 00000011= 03=   3 | Undefined                              | 0-127  |  MSB
 00000100= 04=   4 | Foot controller                        | 0-127  |  MSB
 00000101= 05=   5 | Portamento time                        | 0-127  |  MSB
 00000110= 06=   6 | Data Entry                             | 0-127  |  MSB
 00000111= 07=   7 | Channel Volume (formerly Main Volume)  | 0-127  |  MSB
 00001000= 08=   8 | Balance , coarse                       | 0-127  |  MSB
 00001001= 09=   9 | Undefined                              | 0-127  |  MSB
 00001010= 0A=  10 | Pan                                    | 0-127  |  MSB
 00001011= 0B=  11 | Expression Controller                  | 0-127  |  MSB
 00001100= 0C=  12 | Effect control 1                       | 0-127  |  MSB
 00001101= 0D=  13 | Effect control 2                       | 0-127  |  MSB
 00001110= 0E=  14 | Undefined                              | 0-127  |  MSB
 00001111= 0F=  15 | Undefined                              | 0-127  |  MSB
 00010000= 10=  16 | General Purpose Controller #1          | 0-127  |  MSB
 00010001= 11=  17 | General Purpose Controller #2          | 0-127  |  MSB
 00010010= 12=  18 | General Purpose Controller #3          | 0-127  |  MSB
 00010011= 13=  19 | General Purpose Controller #4          | 0-127  |  MSB
 00010100= 14=  20 | Undefined                              | 0-127  |  MSB
 00010101= 15=  21 | Undefined                              | 0-127  |  MSB
 00010110= 16=  22 | Undefined                              | 0-127  |  MSB
 00010111= 17=  23 | Undefined                              | 0-127  |  MSB
 00011000= 18=  24 | Undefined                              | 0-127  |  MSB
 00011001= 19=  25 | Undefined                              | 0-127  |  MSB
 00011010= 1A=  26 | Undefined                              | 0-127  |  MSB
 00011011= 1B=  27 | Undefined                              | 0-127  |  MSB
 00011100= 1C=  28 | Undefined                              | 0-127  |  MSB
 00011101= 1D=  29 | Undefined                              | 0-127  |  MSB
 00011110= 1E=  30 | Undefined                              | 0-127  |  MSB
 00011111= 1F=  31 | Undefined                              | 0-127  |  MSB
 00100000= 20=  32 | Bank Select                            | 0-127  |  LSB
 00100001= 21=  33 | Modulation wheel                       | 0-127  |  LSB
 00100010= 22=  34 | Breath control                         | 0-127  |  LSB
 00100011= 23=  35 | Undefined                              | 0-127  |  LSB
 00100100= 24=  36 | Foot controller                        | 0-127  |  LSB
 00100101= 25=  37 | Portamento time                        | 0-127  |  LSB
 00100110= 26=  38 | Data entry                             | 0-127  |  LSB
 00100111= 27=  39 | Channel Volume (formerly Main Volume)  | 0-127  |  LSB
 00101000= 28=  40 | Balance   fine                         | 0-127  |  LSB
 00101001= 29=  41 | Undefined                              | 0-127  |  LSB
 00101010= 2A=  42 | Pan                                    | 0-127  |  LSB
 00101011= 2B=  43 | Expression Controller                  | 0-127  |  LSB
 00101100= 2C=  44 | Effect control 1                       | 0-127  |  LSB
 00101101= 2D=  45 | Effect control 2                       | 0-127  |  LSB
 00101110= 2E=  46 | Undefined                              | 0-127  |  LSB
 00101111= 2F=  47 | Undefined                              | 0-127  |  LSB
 00110000= 30=  48 | General Purpose Controller #1          | 0-127  |  LSB
 00110001= 31=  49 | General Purpose Controller #2          | 0-127  |  LSB
 00110010= 32=  50 | General Purpose Controller #3

GM Device Features:

To be GM compatible, a GM sound generating device (keyboard, sound module, sound card, IC, software program or other product) must meet the General MIDI System Level 1 performance requirements outlined below, instantaneously upon demand, and without additional modification or adjustment/configuration by the user.

Voices: A minimum of either 24 fully dynamically allocated voices are available simultaneously for both melodic and percussive sounds, or 16 dynamically allocated voices are available for melody plus 8 for percussion. All voices respond to velocity. 

Channels: All 16 MIDI Channels are supported. Each Channel can play a variable number 
of voices (polyphony). 
Each Channel can play a different instrument (sound/patch/timbre).
Key-based percussion is always on MIDI Channel 10. 

Instruments: A minimum of 16 simultaneous and different timbres playing various instruments.
A minimum of 128 preset instruments (MIDI program numbers) conforming to the GM 
Instrument Patch Map and 47 percussion sounds which conform to the GM Percussion Key Map. 

Channel Messages: Support for continuous controllers 1, 7, 10, 11, 64, 121 and 123;
RPN #s 0, 1, 2; Channel Pressure, Pitch Bend. 

Other Messages: Respond to the data entry controller and the RPNs for fine and 
course tuning and pitch bend range, as well as all General MIDI System Messages.

---------------
General MIDI Instrument Patch Map 

The names of the instruments indicate what sort of sound will be heard when that 
instrument number (MIDI Program Change or "PC#") is selected on the GM synthesizer. 
These sounds are the same for all MIDI Channels except Channel 10, which has only 
percussion sounds and some sound "effects". (See "GM Percussion Key Map")

GM Instrument Families

The General MIDI instrument sounds are grouped by families. In each family are 8 specific instruments.

PC#     Family                  PC#     Family
1-8     Piano                   65-72   Reed
9-16    Chromatic Percussion    73-80   Pipe
17-24   Organ                   81-88   Synth Lead
25-32   Guitar                  89-96   Synth Pad
33-40   Bass                    97-104  Synth Effects
41-48   Strings                 105-112 Ethnic
49-56   Ensemble                113-120 Percussive
57-64   Brass                   121-128 Sound Effects

GM Instrument Patch Map

Note: While GM does not define the actual characteristics of any sounds, the names
in parentheses after each of the synth leads, pads, and sound effects are, in particular, 
intended only as guides).

PC#         Instrument
1.      Acoustic Grand Piano            65.     Soprano Sax
2.      Bright Acoustic Piano           66.     Alto Sax
3.      Electric Grand Piano            67.     Tenor Sax
4.      Honky-tonk Piano                68.     Baritone Sax
5.      Electric Piano 1                69.     Oboe
6.      Electric Piano 2                70.     English Horn
7.      Harpsichord                     71.     Bassoon
8.      Clavi                           72.     Clarinet
9.      Celesta                         73.     Piccolo
10.     Glockenspiel                    74.     Flute
11.     Music Box                       75.     Recorder
12.     Vibraphone                      76.     Pan Flute
13.     Marimba                         77.     Blown Bottle
14.     Xylophone                       78.     Shakuhachi
15.     Tubular Bells                   79.     Whistle
16.     Dulcimer                        80.     Ocarina
17.     Drawbar Organ                   81.     Lead 1 (square)
18.     Percussive Organ                82.     Lead 2 (sawtooth)
19.     Rock Organ                      83.     Lead 3 (calliope)
20.     Church Organ                    84.     Lead 4 (chiff)
21.     Reed Organ                      85.     Lead 5 (charang)
22.     Accordion                       86.     Lead 6 (voice)
23.     Harmonica                       87.     Lead 7 (fifths)
24.     Tango Accordion                 88.     Lead 8 (bass + lead)
25.     Acoustic Guitar (nylon)         89.     Pad 1 (new age)
26.     Acoustic Guitar (steel)         90.     Pad 2 (warm)
27.     Electric Guitar (jazz)          91.     Pad 3 (polysynth)
28.     Electric Guitar (clean)         92.     Pad 4 (choir)
29.     Electric Guitar (muted)         93.     Pad 5 (bowed)
30.     Overdriven Guitar               94.     Pad 6 (metallic)
31.     Distortion Guitar               95.     Pad 7 (halo)
32.     Guitar harmonics                96.     Pad 8 (sweep)
33.     Acoustic Bass                   97.     FX 1 (rain)
34.     Electric Bass (finger)          98.     FX 2 (soundtrack)
35.     Electric Bass (pick)            99.     FX 3 (crystal)
36.     Fretless Bass                   100.    FX 4 (atmosphere)
37.     Slap Bass 1                     101.    FX 5 (brightness)
38.     Slap Bass 2                     102.    FX 6 (goblins)
39.     Synth Bass 1                    103.    FX 7 (echoes)
40.     Synth Bass 2                    104.    FX 8 (sci-fi)
41.     Violin                          105.    Sitar
42.     Viola                           106.    Banjo
43.     Cello                           107.    Shamisen
44.     Contrabass                      108.    Koto
45.     Tremolo Strings                 109.    Kalimba
46.     Pizzicato Strings               110.    Bag pipe
47.     Orchestral Harp                 111.    Fiddle
48.     Timpani                         112.    Shanai
49.     String Ensemble 1               113.    Tinkle Bell
50.     String Ensemble 2               114.    Agogo
51.     SynthStrings 1                  115.    Steel Drums
52.     SynthStrings 2                  116.    Woodblock
53.     Choir Aahs                      117.    Taiko Drum
54.     Voice Oohs                      118.    Melodic Tom
55.     Synth Voice                     119.    Synth Drum
56.     Orchestra Hit                   120.    Reverse Cymbal
57.     Trumpet                         121.    Guitar Fret Noise
58.     Trombone                        122.    Breath Noise
59.     Tuba                            123.    Seashore
60.     Muted Trumpet                   124.    Bird Tweet
61.     French Horn                     125.    Telephone Ring
62.     Brass Section                   126.    Helicopter
63.     SynthBrass 1                    127.    Applause
64.     SynthBrass 2                    128.    Gunshot
-------------------------
General MIDI Percussion Key Map 
On MIDI Channel 10, each MIDI Note number ("Key#") corresponds to a different drum sound, 
as shown below. GM-compatible instruments must have the sounds on the keys shown here. 
While many current instruments also have additional sounds above or below the range show here, 
and may even have additional "kits" with variations of these sounds, only these sounds are 
supported by General MIDI.

Key#   Drum Sound          Key#   Drum Sound
35 Acoustic Bass Drum	59 Ride Cymbal 2
36 Bass Drum 1		60 Hi Bongo
37 Side Stick		61 Low Bongo
38 Acoustic Snare	62 Mute Hi Conga
39 Hand Clap		63 Open Hi Conga
40 Electric Snare	64 Low Conga
41 Low Floor Tom	65 High Timbale
42 Closed Hi Hat	66 Low Timbale
43 High Floor Tom	67 High Agogo
44 Pedal Hi-Hat		68 Low Agogo
45 Low Tom 		69 Cabasa
46 Open Hi-Hat		70 Maracas
47 Low-Mid Tom 		71 Short Whistle
--------------------------

*/

}
