package player;

import java.util.ArrayList;
import java.util.List;


import utilities.Fraction;


public class Piece {

	
	private String title;

	
	private String composer;

	
	private int trackNumber;

	
	private Fraction defaultNoteLength;

	
	private Fraction meter;

	
	private int tempo;

	
	private String key;

	
	private List<Voice> voices;

    
	public Piece() {
	    this.composer = "Unknown";
	    this.voices = new ArrayList<Voice>();
	}
	
	
	public Fraction getMeter() {
		return meter;
	}

	public void setMeter(Fraction meter) {
		this.meter = meter;
	}

	public int getTempo() {
		return tempo;
	}

	public void setTempo(int tempo) {
		this.tempo = tempo;
	}

	public Fraction getSmallestDivision() {
		/**
		 * The (largest, ideally) smallest division needed such that the length of
		 * each note (and rest) is an integer multiple.
		 */
		Fraction smallestDivision = this.getMeter();
		for( Voice voice  : this.voices ) {
			smallestDivision = Fraction.gcd( smallestDivision, voice.getSmallestDivision() );
		}
		return smallestDivision;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getComposer() {
		return composer;
	}

	public void setComposer(String composer) {
		this.composer = composer;
	}

	public int getTrackNumber() {
		return trackNumber;
	}

	public void setTrackNumber(int trackNumber) {
		this.trackNumber = trackNumber;
	}

	public Fraction getDefaultNoteLength() {
		return defaultNoteLength;
	}

	public void setDefaultNoteLength(Fraction defaultNoteLength) {
		this.defaultNoteLength = defaultNoteLength;
	}

	public List<Voice> getVoices() {
		return voices;
	}

	public void setVoices(List<Voice> voices) {
		this.voices = voices;
	}
	
	public void addVoice(Voice voice) {
	    this.voices.add(voice);
	}

	public Voice getVoice(String name) {
		for( Voice voice : this.voices ) {
			if( voice.name.equals( name ) ) {
				return voice;
			}
		}
		return null;
	}

}