package player;

import sound.Pitch;
import utilities.Fraction;


public class Note {
	
	
	public final Fraction duration;
	
	
	public final Pitch pitch;

	
    public Note(Fraction duration, Pitch pitch) {
        this.duration = duration;
        this.pitch = pitch;
    }
    
	public Fraction getDuration() {
		return duration;
	}
	
	public boolean equals(Object other) {
	    if (other instanceof Note) {
            Note o = (Note) other;
            return ((this.duration.equals(o.duration)) && (this.pitch.equals(o.pitch)));
        }
        return false;
	}
	
	public String toString(){
	    return this.duration.toString()+" "+this.pitch.toString();
	}

}