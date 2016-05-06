package player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Pattern;

import sound.Pitch;
import utilities.Fraction;
import utilities.Pair;

import Lexer.*;


public class Parser {

	// Define TokenTypes
	public final static TokenType FIELD_NUM = new TokenType("FIELD_NUM",
			Pattern.compile("X:\\s*\\d+\\n"));
	public final static TokenType FIELD_TITLE = new TokenType("FIELD_TITLE",
			Pattern.compile("T:.*\\n"));
	public final static TokenType FIELD_COMP = new TokenType("FIELD_COMP",
			Pattern.compile("C:.*\\n"));
	public final static TokenType FIELD_DEFAULT_LEN = new TokenType(
			"FIELD_DEFAULT_LEN", Pattern.compile("L:"));
	public final static TokenType FIELD_METER = new TokenType("FIELD_METER",
			Pattern.compile("M:"));
	public final static TokenType FIELD_TEMPO = new TokenType("FIELD_TEMPO",
			Pattern.compile("Q:"));
	public final static TokenType FIELD_VOICE = new TokenType("FIELD_VOICE",
			Pattern.compile("V:.*\\n"));
	public final static TokenType FIELD_KEY = new TokenType("FIELD_KEY",
			Pattern.compile("K:"));
	public final static TokenType BASENOTE = new TokenType("BASENOTE",
			Pattern.compile("[a-gA-G]{1}"));
	public final static TokenType KEY_ACCIDENTAL = new TokenType(
			"KEY_ACCIDENTAL", Pattern.compile("[#b]"));
	public final static TokenType ACCIDENTAL = new TokenType("ACCIDENTAL",
			Pattern.compile("(\\^{1,2})|(_{1,2})|(=)"));
	public final static TokenType MODE_MINOR = new TokenType("MODE_MINOR",
			Pattern.compile("m"));
	public final static TokenType METER = new TokenType("METER",
			Pattern.compile("(C)|(C\\|)"));
	public final static TokenType OCTAVE = new TokenType("OCTAVE",
			Pattern.compile("('+)|(,+)"));
	public final static TokenType DUPLET = new TokenType("DUPLET",
			Pattern.compile("\\(2"));
	public final static TokenType TUPLET = new TokenType("TUPLET",
			Pattern.compile("\\(3"));
	public final static TokenType QUADRUPLET = new TokenType("QUADRUPLET",
			Pattern.compile("\\(4"));
	public final static TokenType OPEN_REPEAT = new TokenType("OPEN_REPEAT",
			Pattern.compile("\\|:"));
	public final static TokenType CLOSE_REPEAT = new TokenType("CLOSE_REPEAT",
			Pattern.compile(":\\|"));
	public final static TokenType DOUBLE_BARLINE = new TokenType(
			"DOUBLE_BARLINE", Pattern.compile("(\\|\\|)|(\\[\\|)|(\\|\\])"));
	public final static TokenType BARLINE = new TokenType("BARLINE",
			Pattern.compile("\\|"));
	public final static TokenType ONE_REPEAT = new TokenType("ONE_REPEAT",
			Pattern.compile("\\[1"));
	public final static TokenType TWO_REPEAT = new TokenType("TWO_REPEAT",
			Pattern.compile("\\[2"));
	public final static TokenType FRACTION = new TokenType("FRACTION",
			Pattern.compile("\\d+/\\d+"));
	public final static TokenType FRACTION_NOT_STRICT = new TokenType(
			"FRACTION_NOT_STRICT", Pattern.compile("\\d*/\\d*"));
	public final static TokenType DIGITS = new TokenType("DIGITS",
			Pattern.compile("\\d+"));
	public final static TokenType REST = new TokenType("REST",
			Pattern.compile("z"));
	public final static TokenType OPEN_CHORD = new TokenType("OPEN_CHORD",
			Pattern.compile("\\["));
	public final static TokenType CLOSE_CHORD = new TokenType("CLOSE_CHORD",
			Pattern.compile("\\]"));
	public final static TokenType COMMENT = new TokenType("COMMENT",
			Pattern.compile("%.*\\n"));
	public final static TokenType NEWLINE = new TokenType("NEWLINE",
			Pattern.compile("\\n"));
	public final static TokenType SPACE = new TokenType("SPACE",
			Pattern.compile("[\\s]"));

	
	public static TokenType[] typeArray = { FIELD_NUM, FIELD_TITLE, FIELD_COMP,
			FIELD_DEFAULT_LEN, FIELD_METER, FIELD_TEMPO, FIELD_VOICE,
			FIELD_KEY, BASENOTE, KEY_ACCIDENTAL, ACCIDENTAL, MODE_MINOR, METER,
			OCTAVE, DUPLET, TUPLET, QUADRUPLET, OPEN_REPEAT, CLOSE_REPEAT,
			DOUBLE_BARLINE, BARLINE, ONE_REPEAT, TWO_REPEAT, FRACTION,
			FRACTION_NOT_STRICT, DIGITS, REST, OPEN_CHORD, CLOSE_CHORD,
			COMMENT, NEWLINE, SPACE };

	public static List<TokenType> types = new ArrayList<TokenType>(
			Arrays.asList(typeArray));
	
	public static HashMap<Pitch, Pitch> accidentalChanges;//map to hold accidental changes in a measure, should be reinitialized
	
	public static Piece parse(String abcContents) throws NoteOutOfBoundsException {
		Lexer l = new Lexer(types);
		List<Token> tokens = l.lex(abcContents);

		Piece piece = parseTokens(tokens);
		return piece;
	}

	
	public static Piece parseTokens( List<Token> tokens ) throws NoteOutOfBoundsException {
		
		if (tokens.size() < 2) {
			throw new IllegalArgumentException(
					"Field has invalid number of fields");
		}
		if (tokens.get(0).type != FIELD_NUM) {
			throw new IllegalArgumentException(
					"Header must start with Track Number");
		}
		if (tokens.get(1).type != FIELD_TITLE) {
			throw new IllegalArgumentException(
					"2nd field in header must be Title");
		}

		ListIterator<Token> iter = tokens.listIterator();
		Piece piece = new Piece();
		
		
		@SuppressWarnings("unused")
        Token next = parseHeaderInfo(piece, iter);
		return piece;
	}

	
	public static Token parseHeaderInfo(Piece piece, ListIterator<Token> iter) throws NoteOutOfBoundsException {

		// set defaults
		Fraction defaultLen = new Fraction(1, 8);
		piece.setMeter(new Fraction(4, 4));
		boolean setDefaultLenFlag = false;
		boolean seenMusic = false; //flag if file contains abc lines
		boolean seenKey = false;

		Voice currentVoice = null;
		
		Map< Voice, Stack<Measure> > openRepeatStackMap = new HashMap< Voice, Stack<Measure> >();
		Map< Voice, List<Measure> > previousMeasuresMap = new HashMap< Voice, List<Measure> >();
		
		
		while (iter.hasNext()) {
			Token next = iter.next();
			// If we're in the header...
			if (!seenKey) {
				if (next.type == FIELD_NUM) {
					int track = Integer.parseInt(next.contents.substring(2)
							.trim());
					piece.setTrackNumber(track);
				} else if (next.type == FIELD_TITLE) {
					piece.setTitle(next.contents.substring(2).trim());
				} else if (next.type == FIELD_COMP) {
					piece.setComposer(next.contents.substring(2).trim());
				} else if (next.type == FIELD_DEFAULT_LEN) {
					next = eatSpaces(iter);
					if (next != null && next.type == FRACTION) {
						defaultLen = parseFraction(next.contents);
						piece.setDefaultNoteLength(defaultLen);
						setDefaultLenFlag = true;
						while(iter.hasNext()){//get rid of spaces
						    next = iter.next();
						    if(next.type!=SPACE){
						        iter.previous();
						        break;
						    }
						}
						if (!eatNewLine(iter))// next token should be end of
												// line character to end the
												// header field
							throw new IllegalArgumentException(
									"Field L: must be ended by an end of line character");
					} else
						throw new IllegalArgumentException(
								"Field L: must be followed by a fraction note length "
										+ next.type.name + " " + next.contents);
				} else if (next.type == FIELD_METER) {
					next = eatSpaces(iter);
					if (next != null
							&& (next.type == METER || next.type == FRACTION || next.type == BASENOTE)) {
						if (!next.contents.equals("C") && !next.contents.equals("C|"))
							piece.setMeter(parseFraction(next.contents));
						else if(next.contents.equals("C")){
						    piece.setMeter(new Fraction(4,4));
						    if(iter.hasNext())
						        next = iter.next();
						    if(!next.contents.equals("|"))
						        iter.previous();
						}
	                      while(iter.hasNext()){//get rid of spaces
	                            next = iter.next();
	                            if(next.type!=SPACE){
	                                iter.previous();
	                                break;
	                            }
	                        }
						if (!eatNewLine(iter))// next token should be end of
												// line character to end the
												// header field
							throw new IllegalArgumentException(
									"Field M: must be ended by an end of line character");
					} else
						throw new IllegalArgumentException(
								"Field M: must be followed by a meter definition");
				} else if (next.type == FIELD_TEMPO) {
					next = eatSpaces(iter);
					if (next != null && next.type == DIGITS) {
						piece.setTempo(Integer.parseInt(next.contents));
	                      while(iter.hasNext()){//get rid of spaces
	                            next = iter.next();
	                            if(next.type!=SPACE){
	                                iter.previous();
	                                break;
	                            }
	                        }
						if (!eatNewLine(iter))// next token should be end of
												// line character to end the
												// header field
							throw new IllegalArgumentException(
									"Field L: must be ended by an end of line character");
					} else
						throw new IllegalArgumentException(
								"Field Q: must be followed by an integer tempo defintion");
				} else if (next.type == FIELD_VOICE) {
					// Add to our Piece's list of declared Voices
					String voiceName = next.contents.substring(2).trim();
					Voice voice = new Voice(voiceName);
					piece.addVoice(voice);
					openRepeatStackMap.put(voice, new Stack<Measure>());
					previousMeasuresMap.put(voice, new ArrayList<Measure>());
				} else if (next.type == FIELD_KEY) {
					next = eatSpaces(iter);
					if (next.type != null && next.type == BASENOTE) {
						String key = next.contents;
						key += parseHeaderKey(iter);// find other key info and
													// take out the end of line
													// character
						piece.setKey(key);
						if (setDefaultLenFlag == false) {
							piece.setDefaultNoteLength(defaultLen);
						}

						seenKey = true;

						if (piece.getVoices().size() == 0) {
							Voice voice = new Voice("default");
							piece.addVoice( voice );
							currentVoice = voice;
							openRepeatStackMap.put(voice, new Stack<Measure>());
							previousMeasuresMap.put(voice, new ArrayList<Measure>());
						}
					} else
						throw new IllegalArgumentException(
								"Field K: must be followed by a keynote");
				}
			}
			
			else {
				if (next.type == FIELD_VOICE) {
					String voiceName = next.contents.substring(2).trim();
					currentVoice = piece.getVoice(voiceName);
				} else if(next.type==NEWLINE){
				    //skip
				}
				else {
					if( currentVoice == null ) {
						throw new RuntimeException("Undeclared voice!");//throws an exception when an unknown voice is
						
					}
					if(currentVoice.getStart()==null) {
						
						Measure startMeasure = new Measure();
						currentVoice.setStart( startMeasure );
						
						openRepeatStackMap.get(currentVoice).push( startMeasure );
					}
					Measure tail = currentVoice.tail();
					parseMeasureStructure(piece, tail, iter, openRepeatStackMap.get(currentVoice), previousMeasuresMap.get(currentVoice) );
					seenMusic=true;
				}
			}
		}
	    //Throw an exception if there is no musical content
        if(!seenMusic)
            throw new IllegalArgumentException("abc file must have at least one line of abc music");
		return null;
	}

	public static void parseMeasureStructure(Piece piece, Measure currentMeasure,
			ListIterator<Token> iter, Stack<Measure> openRepeatStack, List<Measure> previousMeasures ) throws NoteOutOfBoundsException {
		HashMap<String, Pitch> scale = CircleOfFifths.getKeySignature(piece.getKey());
		iter.previous();
		while (iter.hasNext()) {
			
			parseMeasureContents(piece, currentMeasure, iter, scale);
			
			if (!iter.hasNext()) {
				break;
			}

			Token next = iter.next();
	        // If we need to, go up a level and switch voices.
	        if (next.type == FIELD_VOICE) {
	        	iter.previous();
	        	return;
	        }
	        
	        // The ONE_REPEAT and TWO_REPEAT tokens also cause special behavior not related to incrementing the measure.
	        if (next.type == ONE_REPEAT) {
				continue;
			} else if (next.type == TWO_REPEAT) {
				previousMeasures.get( previousMeasures.size() - 2 ).setAlternateNext( currentMeasure );
				continue;
			}
	        
	       
	        Measure newMeasure = new Measure();
			currentMeasure.setNext(newMeasure);

			Measure previousMeasure = currentMeasure;
            currentMeasure = newMeasure;
			previousMeasures.add(previousMeasure);
	        
			if (next.type == BARLINE) {
				
			} else if( next.type == DOUBLE_BARLINE ) {
				
				openRepeatStack.clear();
				openRepeatStack.push( newMeasure );
				
			} else if (next.type == OPEN_REPEAT) {
				
				openRepeatStack.push(currentMeasure);
			} else if (next.type == CLOSE_REPEAT) {
				if (openRepeatStack.size() == 0) {
					throw new RuntimeException("No matching open repeat.");
				}
				previousMeasure.setNext(openRepeatStack.pop());
				previousMeasure.setAlternateNext(currentMeasure);
			} else {
				throw new IllegalArgumentException(
						"Bad Tokens found in parsing music.");
			}
		}
	}

	
	public static void parseMeasureContents(Piece piece, Measure measure, ListIterator<Token> iter, HashMap<String, Pitch> scale) throws NoteOutOfBoundsException {
        Fraction measureLen = new Fraction(0);
        accidentalChanges = new HashMap<Pitch, Pitch>();
		while (iter.hasNext()) {
			Token next = iter.next();
			Note nextNote;
			if (next.type == DUPLET) {
                nextNote = parseNoteElement(piece, iter, scale, new Fraction(3,2));
                measure.addNote(nextNote, measureLen);
                measureLen = measureLen.plus(nextNote.duration);
                
                nextNote = parseNoteElement(piece, iter, scale, new Fraction(3,2));
                measure.addNote(nextNote, measureLen);
                measureLen = measureLen.plus(nextNote.duration);
			} else if (next.type == TUPLET) {
                nextNote = parseNoteElement(piece, iter, scale, new Fraction(2,3));
                measure.addNote(nextNote, measureLen);
                measureLen = measureLen.plus(nextNote.duration);
                
                nextNote = parseNoteElement(piece, iter, scale, new Fraction(2,3));
                measure.addNote(nextNote, measureLen);
                measureLen = measureLen.plus(nextNote.duration);
                
                nextNote = parseNoteElement(piece, iter, scale, new Fraction(2,3));
                measure.addNote(nextNote, measureLen);
                measureLen = measureLen.plus(nextNote.duration);
			} else if (next.type == QUADRUPLET) {
                nextNote = parseNoteElement(piece, iter, scale, new Fraction(3,4));
                measure.addNote(nextNote, measureLen);
                measureLen = measureLen.plus(nextNote.duration);
                
                nextNote = parseNoteElement(piece, iter, scale, new Fraction(3,4));
                measure.addNote(nextNote, measureLen);
                measureLen = measureLen.plus(nextNote.duration);
                
                nextNote = parseNoteElement(piece, iter, scale, new Fraction(3,4));
                measure.addNote(nextNote, measureLen);
                measureLen = measureLen.plus(nextNote.duration);
                
                nextNote = parseNoteElement(piece, iter, scale, new Fraction(3,4));
                measure.addNote(nextNote, measureLen);
                measureLen = measureLen.plus(nextNote.duration);
			}
			else if (next.type == OPEN_CHORD) {
			    Fraction longestDuration = new Fraction(0);
				while (iter.hasNext()) {
						next = iter.next();			
    				if(next.type==BASENOTE || next.type==ACCIDENTAL || next.type==REST){
    				    iter.previous();
    					nextNote = parseNoteElement(piece, iter, scale, new Fraction(1));
                        measure.addNote(nextNote, measureLen);
                        if(nextNote.duration.minus(longestDuration).isPositive())//keep longest length
                            longestDuration = nextNote.duration;
    				}
    				else if(next.type==CLOSE_CHORD){
    				    break;
    				}
    				else{
    				    throw new IllegalArgumentException("Invalid elements in chord");
    				}
				}
				if(next.type!=CLOSE_CHORD)
				    throw new IllegalArgumentException("Chord isn't closed");
				measureLen = measureLen.plus(longestDuration);
			}
			else if (next.type == ACCIDENTAL || next.type == BASENOTE || next.type == REST) {
			    iter.previous();
                nextNote = parseNoteElement(piece, iter, scale, new Fraction(1));
                measure.addNote(nextNote, measureLen);
                measureLen = measureLen.plus(nextNote.duration);
			} else if (next.type == SPACE || next.type == NEWLINE || next.type == COMMENT) {
				
			} else if (next.type == BARLINE || next.type == DOUBLE_BARLINE
					|| next.type == OPEN_REPEAT || next.type == CLOSE_REPEAT
					|| next.type == ONE_REPEAT || next.type == TWO_REPEAT || next.type == FIELD_VOICE) {
				
				iter.previous();
				if(measure.getDuration().minus(piece.getMeter()).isPositive()){
				    throw new NoteOutOfBoundsException("Measure duration is too long");
				}
				return;
			} else {
				throw new IllegalArgumentException(
						"Bad Tokens found in parsing music");
			}
		}
	}

	
	@SuppressWarnings("unused")
    public static Note parseNoteElement(Piece piece,
			ListIterator<Token> iter, HashMap<String, Pitch> scale, Fraction modifier) {
		Token next;
		Pitch p = null;
		Fraction noteLength = null;
		Pitch original = null;
		boolean accidental = false;

		
		if (iter.hasNext()) {
			next = iter.next();
			if (next.type == ACCIDENTAL){
										
			    accidental = true;
			    Pair<Pitch, Pitch> pitches = parseAccidental(next, iter, scale);
			    original = pitches.first;
				p = pitches.second;
			}
			else if (next.type == BASENOTE)
				p = parseBasenote(next, 3, scale);
			else if (next.type == REST){
			    if(iter.hasNext()){
	                next = iter.next();
	                if(next.type==DIGITS||next.type==FRACTION||next.type==FRACTION_NOT_STRICT){
	                    noteLength = piece.getDefaultNoteLength().times(parseNoteLength(next));
	                }
	                else{
	                    noteLength = piece.getDefaultNoteLength();
	                    iter.previous();
	                }
	            }
	            else{
	                noteLength = piece.getDefaultNoteLength();
	            }
	            return new Note(noteLength.times(modifier), p);
			}
			else
				
				throw new IllegalArgumentException(
						"Note element must contain a basenote");
		}
		
		if (iter.hasNext()) {
			next = iter.next();
			if (next.type == OCTAVE) {
				p = parseOctave(next, p);
				if(accidental){
				    original = parseOctave(next, original);
				    accidentalChanges.put(original, p);
				}
				else{
				    if(accidentalChanges.containsKey(p)){
				        p = accidentalChanges.get(p);
				    }
				}
				if (iter.hasNext()) {
					next = iter.next();
					if (next.type == DIGITS || next.type == FRACTION
							|| next.type == FRACTION_NOT_STRICT){
						noteLength = piece.getDefaultNoteLength().times(parseNoteLength(next));
						return new Note(noteLength.times(modifier), p);
					}
					else if (next.type == OCTAVE)
						throw new IllegalArgumentException(
								"Note should not have mixed octave modifiers");
					else{
					    iter.previous();
						return new Note(piece.getDefaultNoteLength().times(modifier), p);
					}
				}
			} else if (next.type == DIGITS || next.type == FRACTION
					|| next.type == FRACTION_NOT_STRICT){// is note length token
				noteLength = piece.getDefaultNoteLength().times(parseNoteLength(next));
				if(accidental){//add modified note to hashmap of accidental changes
                    accidentalChanges.put(original, p);
                }
				else{
                    if(accidentalChanges.containsKey(p)){
                        p = accidentalChanges.get(p);
                    }
                }
				return new Note(noteLength.times(modifier), p);
			}
			else{
			    iter.previous();
			    if(accidental){
                    accidentalChanges.put(original, p);
                }
			    else{
                    if(accidentalChanges.containsKey(p)){
                        p = accidentalChanges.get(p);
                    }
                }
				return new Note(piece.getDefaultNoteLength().times(modifier), p);
			}
		}
		if(accidental){
            accidentalChanges.put(original, p);
        }
		else{//modify note by accidental if it is in the changed note hashmap and the note has no accidental this time
            if(accidentalChanges.containsKey(p)){
                p = accidentalChanges.get(p);
            }
        }
		
		if (noteLength == null){
			return new Note(piece.getDefaultNoteLength().times(modifier), p);
		}
		else
			return new Note(noteLength.times(modifier),p);

	}

	
	public static Pair<Pitch,Pitch> parseAccidental(Token next, ListIterator<Token> iter,
			HashMap<String, Pitch> scale) {
		int accidental = 0;
		if (next.contents.equals("^"))
			accidental = 1;
		else if (next.contents.equals("^^"))
			accidental = 2;
		else if (next.contents.equals("_"))
			accidental = -1;
		else if (next.contents.equals("__"))
			accidental = -2;
		else if (next.contents.equals("="))
			accidental = 0;
		else
			throw new IllegalArgumentException("Invalid type of accidental");
		Token basenote;
		if (iter.hasNext()) {
			basenote = iter.next();
			if (basenote.type != BASENOTE)
				throw new IllegalArgumentException(
						"Accidental must be followed by basenote");
			return new Pair<Pitch, Pitch>(parseBasenote(basenote, 3, scale), parseBasenote(basenote, accidental, scale));
		} else
			throw new IllegalArgumentException(
					"Accidental must be followed by basenote");
	}

	
	public static Pitch parseBasenote(Token next, int accidental,
			HashMap<String, Pitch> scale) {
		int octave = 0;
		if (next.contents.equals(next.contents.toLowerCase())) 
			octave = 12;// raise the pitch by 12 halfsteps for an octave
		if (accidental == 3)
			return scale.get(next.contents.toUpperCase()).transpose(octave);
		else
			return new Pitch(next.contents.toUpperCase().toCharArray()[0])
					.transpose(accidental + octave);
	}

	
	public static Pitch parseOctave(Token next, Pitch p) {
		if (next.contents.contains(",")) {
			int octavesDown = next.contents.length();
			return p.transpose(-octavesDown * 12);
		} else {
			int octavesUp = next.contents.length();
			return p.transpose(octavesUp * 12);
		}
	}

	
	public static Fraction parseNoteLength(Token next) {
		if (next.type == DIGITS)
			return new Fraction(Integer.parseInt(next.contents));
		else if (next.type == FRACTION)
			return parseFraction(next.contents);
		else if (next.type == FRACTION_NOT_STRICT)
			return parseFractionNotStrict(next.contents);
		else
			throw new IllegalArgumentException(
					"Token argument to parseNoteLength must be either digit or strict or non-strict fraction");
	}

	
	public static Token eatSpaces(ListIterator<Token> iter) {
		Token next;
		while (iter.hasNext()) {
			next = iter.next();
			if (next.type != SPACE)
				return next;
		}
		return null;
	}

	
	public static boolean eatNewLine(ListIterator<Token> iter) {
		if (iter.hasNext()) {
			
			if (iter.next().type != NEWLINE)
				return false;
		}
		return true;
	}

	
	public static Fraction parseFraction(String frac) {
		int slashPos = frac.indexOf('/');
		int num = Integer.parseInt(frac.substring(0, slashPos));
		int denom = Integer.parseInt(frac.substring(slashPos + 1));
		return new Fraction(num, denom);
	}

	
	public static Fraction parseFractionNotStrict(String frac) {
		if (frac.equals("/"))
			return new Fraction(1, 2);
		else if (frac.endsWith("/"))
			return new Fraction(Integer.parseInt(frac.substring(0,
					frac.length() - 1)), 2);
		else
			
			return new Fraction(1, Integer.parseInt(frac.substring(1)));
	}

	
	public static String parseHeaderKey(ListIterator<Token> iter) {
		Token next;
		String key = "";
		boolean minor = false;

		while (iter.hasNext()) {
		    
			next = iter.next();
			if (next.type == BASENOTE || next.type == KEY_ACCIDENTAL) {
				if (minor == false)
					key += next.contents;
				else
					throw new IllegalArgumentException(
							"In field K: key accidental must be declared before minor mode");
			} else if (next.type == MODE_MINOR) {
				key += next.contents;
				minor = true;
			} else if(next.type==SPACE){
			    //do nothing
			} else if (next.type == NEWLINE) {
				return key;
			} else
				throw new IllegalArgumentException(
						"Field K: must be ended by a newline character");
		}
		return key;
	}

	
	public static List<Token> lex(String string) {
		Lexer l = new Lexer(types);
		return l.lex(string);
	}

}