package player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import utilities.Fraction;
import utilities.Pair;


public class Measure implements Iterable<Measure> {

	
	private Fraction duration;

	public Fraction getDuration() {
        return duration;
    }

    
	private List<Pair<Note, Fraction>> notes;

	

	
	private Measure next;

	
	private Measure alternateNext = null;

	
	public Measure(Measure next, Measure alternateNext,
			List<Pair<Note, Fraction>> notes)
			throws NoteOutOfBoundsException{
		this.next = next;
		this.alternateNext = alternateNext;
		this.notes = new ArrayList<Pair<Note, Fraction>>();
		this.duration = new Fraction(0);
		
		for (Pair<Note, Fraction> note : notes)
			this.addNote(note.first, note.second);
	}

	
	public Measure(Measure next, Measure alternateNext) {
		this.next = next;
		this.alternateNext = alternateNext;
		this.duration = new Fraction(0);
		this.notes = new ArrayList<Pair<Note, Fraction>>();
	}

	
	public Measure(Measure next) {
		this(next, null);
	}

	
	public Measure() {
		this(null);
	}

	
	public Iterator<Measure> iterator() {
		return new MeasureIterator(this);
	}

	
	public Measure getNext() {
		return next;
	}

	
	public void setNext(Measure next) {
		this.next = next;
	}

	
	public Measure getAlternateNext() {
		return alternateNext;
	}

	
	public void setAlternateNext(Measure alternateNext) {
		this.alternateNext = alternateNext;
	}

	
	public List<Pair<Note, Fraction>> getNotes() {
		return new ArrayList<Pair<Note, Fraction>>(notes);
	}

	
	public void addNote(Note note, Fraction startTime)
			throws NoteOutOfBoundsException {
		
		if( note == null )
			throw new NoteOutOfBoundsException("Cannot add note = null...");
		
		if (startTime == null)
			throw new NoteOutOfBoundsException("You must provide a positive (i.e. non-null) start time.");
		if ( note.duration == null )
			throw new NoteOutOfBoundsException("You must provide a positive (i.e. non-null) duration.");

		
		if (startTime.numerator < 0)
			throw new NoteOutOfBoundsException(
					"Tried to add a note that started at negative time" + startTime + ".");
		if (note.duration.numerator <= 0)
			throw new NoteOutOfBoundsException(
					"Tried to add a non-positive duration note.");
		Fraction endTime = startTime.plus(note.duration);
		if (endTime.minus(this.duration).isPositive())
			this.duration = endTime;
		
		
		if (note.pitch == null)
			return;

		this.notes.add(new Pair<Note, Fraction>(note, startTime));
	}
	
    public String toString(){
        return notes.toString();
    }

	public Fraction getSmallestDivision() {
		Fraction smallestDivision = this.duration;
		for( Pair<Note, Fraction> pair  : this.notes ) {
			smallestDivision = Fraction.gcd( smallestDivision, pair.first.duration );
			smallestDivision = Fraction.gcd( smallestDivision, pair.second );
		}
		return smallestDivision;
	}

}