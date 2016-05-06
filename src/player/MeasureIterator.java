package player;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MeasureIterator implements Iterator<Measure> {
	
	Measure start;

	
	Measure current;

	
	Map<Measure, Integer> timesSeen;

	
	public MeasureIterator(Measure start) {
		this.start = start;
		this.current = null;
		this.timesSeen = new HashMap<Measure, Integer>();
	}

	public boolean hasNext() {

		
		if (this.current == null)
			return true;

		
		if (this.current.getNext() == null)
			return false;

		
		return true;
	}

	public Measure next() {

		.
		if (this.current == null) {
			this.current = this.start;
			return this.current;
		}

		
		if (!this.hasNext())
			return null;

		
		int timesSeenBefore = 1;
		if (this.timesSeen.containsKey(this.current))
			timesSeenBefore += this.timesSeen.get(this.current);
		this.timesSeen.put(this.current, timesSeenBefore);

		
		if (this.current.getAlternateNext() == null)
			this.current = this.current.getNext();
		
		else if (timesSeenBefore % 2 == 1)
			this.current = this.current.getNext();
		
		else
			this.current = this.current.getAlternateNext();

		return this.current;
	}

	
	public void remove() throws RuntimeException {
		throw new RuntimeException("Removing Measures is not supported.");
	}

}