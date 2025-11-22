package SOUND;
import javax.sound.midi.*;
import java.io.*;

public class MyMidi {

    final int PROGRAM = 192;
    final int NOTEON = 144;
    final int NOTEOFF = 128;
    final int SUSTAIN = 64;
    final int REVERB = 91;
    final int CONTROL = 0xb0;
    final int BALANCE = 0x8; // 64=center (0-127)
    final int PAN = 10; // 64=center (0-127)
    final int ON = 0, OFF = 1;

    ChannelData channels[];
    ChannelData cc;    // current channel
    // -Midi -
    Sequencer sequencer;
    Sequence sequence;
    MidiChannel[] midiChannels;
    Instrument[] instruments;
    Soundbank sb = null;
    Synthesizer synthesizer;
    Track track;
    long startTime;

    public boolean open(Defaults def) {
		try {
			if (synthesizer == null) {
				if ((synthesizer = MidiSystem.getSynthesizer()) == null) {
					System.out.println("getSynthesizer() failed!");
					return false;
				}
			} 
			synthesizer.open();
			sequencer = MidiSystem.getSequencer();
			sequence = new Sequence(Sequence.PPQ, 10);
		} catch (Exception ex) { 
			ex.printStackTrace();
			return false; 
		}
		this.sb = synthesizer.getDefaultSoundbank();	
		//sb = null; // test
		System.out.println("------------------- > Soundbank sb="+sb);
		if (sb != null) {
			instruments = synthesizer.getDefaultSoundbank().getInstruments();
			synthesizer.loadInstrument(instruments[0]); // 0
		}
		
		midiChannels = synthesizer.getChannels();
		channels = new ChannelData[midiChannels.length]; // 16
		for (int i = 0; i < channels.length; i++) {
			channels[i] = new ChannelData(midiChannels[i], i);
			int q = 0;
			if (i < def.voiceMax)
				q =def.getInstrumentForChannel(i);
			channels[i].channel.programChange(q);
			
			//System.out.println("name="+instruments[q].getName());
		}
		cc = channels[1];
		//System.out.println("cc.channel="+cc.channel);
		programChange(12);
		return true;
    }
    
    
    
    public void close() {
        if (synthesizer != null) {
            synthesizer.close();
        }
        if (sequencer != null) {
            sequencer.close();
        }
        sequencer = null;
        synthesizer = null;
        instruments = null;
        channels = null;
    }

    public void startRecord(int instr) {
		track = sequence.createTrack();
		startTime = System.currentTimeMillis();
		// add a program change right at the beginning of 
		// the track for the current instrument
		/*for (int q = 0; q < 16; q++) {
		    if (ti[q] != null) {
			if (ti[q] == o)  {
			    stopChannel(q);
			    return;
			}
		    }
		}
		createShortEvent(PROGRAM, instr);//Instrument change
		*/
    }
    public Sequence readMifiFile(String file) {
	Sequence seq = null;
	File f = new File(file);
	if (!f.exists()) return seq;
	try {
	    seq = MidiSystem.getSequence(new FileInputStream(f));
	} catch(InvalidMidiDataException ime) {
	    ime.printStackTrace();
	    return seq;	    
	}
	catch (IOException ioe) {
	    ioe.printStackTrace();
	    return seq;	  
	}
	if (seq == null) return seq;
	return seq;
    }

    public void saveMidiFile(File file) {
	try {
	    int[] fileTypes = MidiSystem.getMidiFileTypes(sequence);
	    if (fileTypes.length == 0) {
		System.out.println("Can't save sequence");
	    } else {
		if (MidiSystem.write(sequence, fileTypes[0], file) == -1) {
		    throw new IOException("Problems writing to file");
		} 
	    }
	} catch (SecurityException ex) { 
	    //JavaSound.showInfoDialog();
	} catch (Exception ex) { 
	    ex.printStackTrace(); 
	}
    }

/*    public void actionPerformed(ActionEvent ev) {  
	//midiChannels[5].noteOn(60, 600);
	cc.channel.noteOn(90, 127);
    }
*/
    private void programChange(int program) {
		if (instruments != null) {
		    System.out.println("programChange() instruments[program]="+instruments[program]);
		    synthesizer.loadInstrument(instruments[program]);
		}
		System.out.println("ProgramChange() instruments="+instruments+" program="+program);
		cc.channel.programChange(program);
	/*if (record) {
	  createShortEvent(PROGRAM, program);
	  }
	*/
    }

     /**
     * given 120 bpm:
     *   (120 bpm) / (60 seconds per minute) = 2 beats per second
     *   2 / 1000 beats per millisecond
     *   (2 * resolution) ticks per second
     *   (2 * resolution)/1000 ticks per millisecond, or 
     *      (resolution / 500) ticks per millisecond
     *   ticks = milliseconds * resolution / 500
     * @param type = midi command nr.
     * @param data the data for this command
     * @param cd the ChannelData Object
     */
    public void createShortEvent(int type, int data, ChannelData cd ) {
        ShortMessage message = new ShortMessage();
        try {
            long millis = System.currentTimeMillis() - startTime;
            long tick = millis * sequence.getResolution() / 500;
           // System.out.println("ShortEvent:"+cd.num+" vel="+cd.velocity+" rev="+cd.reverb+" bend="+cd.bend);
            //System.out.println("ShortEvent: type="+type+" num="+cd.num+" data="+data+" velocity ="+cd.velocity);
            message.setMessage(type+cd.num, data, cd.velocity); 
            MidiEvent event = new MidiEvent(message, tick);
            track.add(event);
        } catch (Exception ex) { ex.printStackTrace(); }
    }
    /**
     * @param data1 the first data for this command
     * @param data2 the 2nd data for this command
     * @param cd the ChannelData Object
     */
    public void createControlEvent(int data1, int data2, ChannelData cd ) {
        ShortMessage message = new ShortMessage();
        try {
            long millis = System.currentTimeMillis() - startTime;
            long tick = millis * sequence.getResolution() / 500;
            //message.setMessage(type+cd.num, data, cd.velocity); 
	    message.setMessage(ShortMessage.CONTROL_CHANGE,
			       cd.num,
			       data1,
			       data2);
            MidiEvent event = new MidiEvent(message, tick);
            track.add(event);
        } catch (Exception ex) { ex.printStackTrace(); }
    }
    

    /**
     * Stores MidiChannel information.
     */
    class ChannelData {
        MidiChannel channel;
        boolean solo, mono, mute, sustain;
        int velocity; // (0-127) volume 
        int pressure, bend, reverb;
        int row, col;
        int num; // the midi channel (0-15)
 
        public ChannelData(MidiChannel channel, int num) {
            this.channel = channel;
            this.num = num;
            velocity = pressure = bend = reverb = 64;
        }
    } // End class ChannelData

} // end of class







