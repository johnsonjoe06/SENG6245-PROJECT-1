package Lexer;

import static org.junit.Assert.assertEquals;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.Before;
import org.junit.Test;


public class LexerTest {
    public final TokenType FIELD_NUM = new TokenType("FIELD_NUM",
            Pattern.compile("X:\\s*\\d+\\n"));
    public final  TokenType FIELD_TITLE = new TokenType("FIELD_TITLE",
            Pattern.compile("T:.*\\n"));
    public final  TokenType FIELD_COMP = new TokenType("FIELD_COMP",
            Pattern.compile("C:.*\\n"));
    public final  TokenType FIELD_DEFAULT_LEN = new TokenType("FIELD_DEFAULT_LEN",
            Pattern.compile("L:"));
    public final  TokenType FIELD_METER = new TokenType("FIELD_METER",
            Pattern.compile("M:"));
    public final  TokenType FIELD_TEMPO = new TokenType("FIELD_TEMPO",
            Pattern.compile("Q:"));
    public final TokenType FIELD_VOICE = new TokenType("FIELD_VOICE",
            Pattern.compile("V:.*\\n"));
    public final  TokenType FIELD_KEY = new TokenType("FIELD_KEY",
            Pattern.compile("K:"));
    public final  TokenType BASENOTE = new TokenType("BASENOTE",
            Pattern.compile("[a-gA-G]{1}"));
    public final  TokenType KEY_ACCIDENTAL = new TokenType("KEY_ACCIDENTAL",
            Pattern.compile("[#b]"));
    public final  TokenType ACCIDENTAL = new TokenType("ACCIDENTAL",
            Pattern.compile("(\\^{1,2})|(_{1,2})|(=)"));
    public final  TokenType MODE_MINOR = new TokenType("MODE_MINOR",
            Pattern.compile("m"));
    public final  TokenType METER = new TokenType("METER",
            Pattern.compile("(C)|(C\\|)"));
    public final  TokenType OCTAVE = new TokenType("OCTAVE",
            Pattern.compile("('+)|(,+)"));
    public final  TokenType DUPLET = new TokenType("DUPLET",
            Pattern.compile("\\(2"));
    public final  TokenType TUPLET = new TokenType("TUPLET",
            Pattern.compile("\\(3"));
    public final  TokenType QUADRUPLET = new TokenType("QUADRUPLET",
            Pattern.compile("\\(4"));
    public final  TokenType OPEN_REPEAT = new TokenType("OPEN_REPEAT",
            Pattern.compile("\\|:"));
    public final  TokenType CLOSE_REPEAT = new TokenType("CLOSE_REPEAT",
            Pattern.compile(":\\|"));
    public final  TokenType DOUBLE_BARLINE = new TokenType("DOUBLE_BARLINE",
            Pattern.compile("(\\|\\|)|(\\[\\|)|(\\|\\])"));
    public final  TokenType BARLINE = new TokenType("BARLINE",
            Pattern.compile("\\|"));
    public final  TokenType ONE_REPEAT = new TokenType("ONE_REPEAT",
            Pattern.compile("\\[1"));
    public final  TokenType TWO_REPEAT = new TokenType("TWO_REPEAT",
            Pattern.compile("\\[2"));
    public final TokenType FRACTION = new TokenType("FRACTION",
            Pattern.compile("\\d+/\\d+"));
    public final TokenType FRACTION_NOT_STRICT = new TokenType("FRACTION_NOT_STRICT",
            Pattern.compile("\\d*/\\d*"));
    public final  TokenType DIGITS = new TokenType("DIGITS",
            Pattern.compile("\\d+"));
    public final  TokenType REST = new TokenType("REST",
            Pattern.compile("z"));
    public final  TokenType OPEN_CHORD = new TokenType("OPEN_CHORD",
            Pattern.compile("\\["));
    public final  TokenType CLOSE_CHORD = new TokenType("CLOSE_CHORD",
            Pattern.compile("\\]"));
    public final  TokenType COMMENT = new TokenType("COMMENT",
            Pattern.compile("%.*\\n"));
    public final  TokenType NEWLINE = new TokenType("NEWLINE",
            Pattern.compile("\\n"));
    public final  TokenType SPACE = new TokenType("SPACE",
            Pattern.compile("[\\s]"));
    
    List<TokenType> types = new ArrayList<TokenType>();

    @Before
    public void setUp() {
        types.add(FIELD_NUM);
        types.add(FIELD_TITLE);
        types.add(FIELD_COMP);
        types.add(FIELD_DEFAULT_LEN);
        types.add(FIELD_METER);
        types.add(FIELD_TEMPO);
        types.add(FIELD_VOICE);
        types.add(FIELD_KEY);
        types.add(BASENOTE);
        types.add(KEY_ACCIDENTAL);
        types.add(ACCIDENTAL);
        types.add(MODE_MINOR);
        types.add(METER);
        types.add(OCTAVE);
        types.add(DUPLET);
        types.add(TUPLET);
        types.add(QUADRUPLET);
        types.add(OPEN_REPEAT);
        types.add(CLOSE_REPEAT);
        types.add(DOUBLE_BARLINE);
        types.add(BARLINE);
        types.add(ONE_REPEAT);
        types.add(TWO_REPEAT);
        types.add(FRACTION);
        types.add(FRACTION_NOT_STRICT);
        types.add(DIGITS);
        types.add(REST);
        types.add(OPEN_CHORD);
        types.add(CLOSE_CHORD);
        types.add(COMMENT);
        types.add(NEWLINE);
        types.add(SPACE);
    }
    

    /**
     * Ensures that lexer correctly lexes Fractions.
     */
    @Test
    public void testFractionLexing(){
        Lexer l = new Lexer(types);
        List<Token> tokens = l.lex("3/ /3 3/12 /12 / 2/ 12/ 12 12/12");

        List<Token> expected = new ArrayList<Token>();
        expected.add(new Token("3/", FRACTION_NOT_STRICT));
        expected.add(new Token(" ", SPACE));
        expected.add(new Token("/3", FRACTION_NOT_STRICT));
        expected.add(new Token(" ", SPACE));
        expected.add(new Token("3/12", FRACTION));
        expected.add(new Token(" ", SPACE));
        expected.add(new Token("/12", FRACTION_NOT_STRICT));
        expected.add(new Token(" ", SPACE));
        expected.add(new Token("/", FRACTION_NOT_STRICT));
        expected.add(new Token(" ", SPACE));
        expected.add(new Token("2/", FRACTION_NOT_STRICT));
        expected.add(new Token(" ", SPACE));
        expected.add(new Token("12/", FRACTION_NOT_STRICT));
        expected.add(new Token(" ", SPACE));
        expected.add(new Token("12", DIGITS));
        expected.add(new Token(" ", SPACE));
        expected.add(new Token("12/12", FRACTION));
        for (int i = 0; i < expected.size(); i++)
        {
            assertEquals(tokens.get(i), expected.get(i));
        }
    }
    
    /**
     * Ensures that lexer correctly lexes accidentals with basenotes.
     */
    @Test
    public void testAccidentalLexing(){
        Lexer l = new Lexer(types);
        List<Token> tokens = l.lex("=C ^^C __C _c ^b ^^^");
        
        List<Token> expected = new ArrayList<Token>();
        expected.add(new Token("=", ACCIDENTAL));
        expected.add(new Token("C", BASENOTE));
        expected.add(new Token(" ", SPACE));
        expected.add(new Token("^^", ACCIDENTAL));
        expected.add(new Token("C", BASENOTE));
        expected.add(new Token(" ", SPACE));
        expected.add(new Token("__", ACCIDENTAL));
        expected.add(new Token("C", BASENOTE));
        expected.add(new Token(" ", SPACE));
        expected.add(new Token("_", ACCIDENTAL));
        expected.add(new Token("c", BASENOTE));
        expected.add(new Token(" ", SPACE));
        expected.add(new Token("^", ACCIDENTAL));
        expected.add(new Token("b", BASENOTE));
        expected.add(new Token(" ", SPACE));
        expected.add(new Token("^^", ACCIDENTAL));
        expected.add(new Token("^", ACCIDENTAL));
        for (int i = 0; i < expected.size(); i++)
        {
            assertEquals(tokens.get(i), expected.get(i));
        }
        
    }

    /**
     * Ensures that null input correctly throws an error.  
     */
    @Test (expected=RuntimeException.class)
    public void testNullLex(){
        Lexer l = new Lexer(types);
        l.lex(null);
    }
    
    /**
     * Ensures that non-recognized patterns correctly throw an error.  
     */
    @Test (expected=RuntimeException.class)
    public void testBadDecimals(){
        Lexer l = new Lexer(types);
        l.lex("3/ /.3 3/12 /12 / 2/ 12/ 12 12/12");   
    }
    
    @Test (expected=RuntimeException.class)
    public void testInvalidHead(){
        Lexer l = new Lexer(types);
        l.lex("X: 1  U:" + "\n");
    }

    
    /**
     * Ensures that Space tokens are being lexed correctly and 
     * play a part in determining the parsing of future tokens.  
     */
    @Test 
    public void testParseSpaceOrder(){
        Lexer l = new Lexer(types);
        List<Token> tokens1 = l.lex("|:||");
        List<Token> expected1 = new ArrayList<Token>();
        expected1.add(new Token("|:", OPEN_REPEAT));
        expected1.add(new Token("||", DOUBLE_BARLINE));
        assertEquals(expected1, tokens1);
        
        List<Token> tokens2 = l.lex("| :||" + "\n");
        List<Token> expected2 = new ArrayList<Token>();
        expected2.add(new Token("|", BARLINE));
        expected2.add(new Token(" ", SPACE));
        expected2.add(new Token(":|", CLOSE_REPEAT));
        expected2.add(new Token("|", BARLINE));
        expected2.add(new Token("\n", NEWLINE));
        assertEquals(expected2, tokens2);
    }
    
    /**
     * General test using the given sample piece1.abc file.  
     * Requires Lexer to lex basic Tokens, including whitespace,
     * base notes, Fractions, barlines and tuplets.  
     */
    @Test
    public void sampleTest1() {
        BufferedReader f = null;
        String line = "";
        try {
          f = new BufferedReader(new FileReader("sample_abc/piece1.abc"));
          String nextLine = f.readLine();
          while (nextLine != null) {
            line += nextLine + "\n";
            nextLine = f.readLine();
          }
          f.close();
        } catch (IOException e) {
          System.err.println("File couldn't be read");
        }
        //System.out.println(line);
        Lexer l = new Lexer(types);
        List<Token> tokens = l.lex(line);
        
        List<String> expected = new ArrayList<String>();
        expected.add("FIELD_NUM X: 1" + "\n");
        expected.add("FIELD_TITLE T: Piece No.1" + "\n");
        expected.add("FIELD_METER M:");
        expected.add("SPACE" + " " + " ");
        //Extra spacing needed
        expected.add("FRACTION 4/4");
        expected.add("NEWLINE" + " " + "\n");
        //Extra spacing needed
        expected.add("FIELD_DEFAULT_LEN L:");
        expected.add("SPACE" + "  ");
        expected.add("FRACTION 1/4");
        expected.add("NEWLINE" + " " + "\n");
        expected.add("FIELD_TEMPO Q:");
        expected.add("SPACE" + "  ");
        expected.add("DIGITS 140");
        expected.add("NEWLINE" + " " + "\n");
        expected.add("FIELD_KEY K:");
        expected.add("SPACE" + "  ");
        expected.add("BASENOTE C");
        expected.add("NEWLINE" + " " + "\n");
        expected.add("BASENOTE C");
        expected.add("SPACE" + "  ");
        expected.add("BASENOTE C");
        expected.add("SPACE" + "  ");
        expected.add("BASENOTE C");
        expected.add("FRACTION 3/4");
        expected.add("SPACE" + "  ");
        expected.add("BASENOTE D");
        expected.add("FRACTION_NOT_STRICT /4");
        expected.add("SPACE" + "  ");
        expected.add("BASENOTE E");
        expected.add("SPACE" + "  ");
        expected.add("BARLINE |");
        expected.add("NEWLINE" + " " + "\n");
        
        expected.add("BASENOTE E");
        expected.add("FRACTION 3/4");
        expected.add("SPACE" + "  ");
        expected.add("BASENOTE D");
        expected.add("FRACTION_NOT_STRICT /4");
        expected.add("SPACE" + "  ");
        expected.add("BASENOTE E");
        expected.add("FRACTION 3/4");
        expected.add("SPACE" + "  ");
        expected.add("BASENOTE F");
        expected.add("FRACTION_NOT_STRICT /4");
        expected.add("SPACE" + "  ");
        expected.add("BASENOTE G");
        expected.add("DIGITS 2");
        expected.add("SPACE" + "  ");
        expected.add("BARLINE |");
        expected.add("NEWLINE" + " " + "\n");
        
        expected.add("TUPLET (3");
        expected.add("BASENOTE c");
        expected.add("FRACTION 1/2");
        expected.add("BASENOTE c");
        expected.add("FRACTION 1/2");
        expected.add("BASENOTE c");
        expected.add("FRACTION 1/2");
        expected.add("SPACE" + "  ");
        expected.add("TUPLET (3");
        expected.add("BASENOTE G");
        expected.add("FRACTION 1/2");
        expected.add("BASENOTE G");
        expected.add("FRACTION 1/2");
        expected.add("BASENOTE G");
        expected.add("FRACTION 1/2");
        expected.add("SPACE" + "  ");
        expected.add("TUPLET (3");
        expected.add("BASENOTE E");
        expected.add("FRACTION 1/2");
        expected.add("BASENOTE E");
        expected.add("FRACTION 1/2");
        expected.add("BASENOTE E");
        expected.add("FRACTION 1/2");
        expected.add("SPACE" + "  ");
        expected.add("TUPLET (3");
        expected.add("BASENOTE C");
        expected.add("FRACTION 1/2");
        expected.add("BASENOTE C");
        expected.add("FRACTION 1/2");
        expected.add("BASENOTE C");
        expected.add("FRACTION 1/2");
        expected.add("SPACE" + "  ");
        expected.add("BARLINE |");
        expected.add("NEWLINE" + " " + "\n");
        
        expected.add("BASENOTE G");
        expected.add("FRACTION 3/4");
        expected.add("SPACE" + "  ");
        expected.add("BASENOTE F");
        expected.add("FRACTION_NOT_STRICT /4");
        expected.add("SPACE" + "  ");
        expected.add("BASENOTE E");
        expected.add("FRACTION 3/4");
        expected.add("SPACE" + "  ");
        expected.add("BASENOTE D");
        expected.add("FRACTION_NOT_STRICT /4");
        expected.add("SPACE" + "  ");
        expected.add("BASENOTE C");
        expected.add("DIGITS 2");
        expected.add("SPACE" + "  ");
        expected.add("DOUBLE_BARLINE |]");
        expected.add("NEWLINE" + " " + "\n");
        
        for (int i = 0; i < tokens.size(); i++)
        {
            assertEquals(tokens.get(i).toString(), expected.get(i));
        }
    }
    
    /**
     * General test for an abc file with a variety of features.  
     * Requires Lexer to lex different Tokens; in addition to whitespace,
     * standard header Tokens, notes, accidentals, octaves, Fractions and rests,
     * handle measure structures such as barlines and repeats (both open/close and n-th)
     */
    @Test
    public void genTest1() {
        BufferedReader f = null;
        String line = "";
        try {
          f = new BufferedReader(new FileReader("sample_abc/LexerTest1.abc"));
          String nextLine = f.readLine();
          while (nextLine != null) {
            line += nextLine + "\n";
            nextLine = f.readLine();
          }
          f.close();
        } catch (IOException e) {
          System.err.println("File couldn't be read");
        }
        Lexer l = new Lexer(types);
        List<Token> tokens = l.lex(line);
        List<String> expected = new ArrayList<String>();
        expected.add("FIELD_NUM X: 1" + "\n");
        expected.add("FIELD_TITLE T: Lexer Test" + "\n");
        expected.add("FIELD_METER M:");
        expected.add("SPACE" + "  ");
        expected.add("FRACTION 4/4");
        expected.add("NEWLINE" + " " + "\n");
        expected.add("FIELD_VOICE V: Voice 2" + "\n");
        expected.add("FIELD_DEFAULT_LEN L:");
        expected.add("SPACE" + "  ");
        expected.add("FRACTION 1/4");
        expected.add("NEWLINE" + " " + "\n");
        expected.add("FIELD_TEMPO Q:");
        expected.add("SPACE" + "  ");
        expected.add("DIGITS 140");
        expected.add("NEWLINE" + " " + "\n");
        expected.add("FIELD_VOICE V: Voice 1" + "\n");
        expected.add("FIELD_COMP C: Tim the Beaver" + "\n");
        expected.add("FIELD_KEY K:");
        expected.add("SPACE" + "  ");
        expected.add("BASENOTE C");
        expected.add("NEWLINE" + " " + "\n");
        expected.add("FIELD_VOICE V: Voice 1" + "\n");
        expected.add("OPEN_CHORD [");
        expected.add("BASENOTE C");
        expected.add("BASENOTE c");
        expected.add("OCTAVE '");
        expected.add("CLOSE_CHORD ]");
        expected.add("SPACE  ");
        expected.add("BASENOTE C");
        expected.add("FRACTION_NOT_STRICT /");
        expected.add("SPACE  ");
        expected.add("BASENOTE C");
        expected.add("FRACTION 3/4");
        expected.add("SPACE  ");
        expected.add("BARLINE |");
        expected.add("SPACE  ");
        expected.add("TUPLET (3");
        expected.add("BASENOTE C");
        expected.add("BASENOTE B");
        expected.add("ACCIDENTAL __");
        expected.add("BASENOTE A");
        expected.add("SPACE  ");
        expected.add("CLOSE_REPEAT :|");
        expected.add("SPACE " + " ");
        expected.add("SPACE  ");
        expected.add("NEWLINE" + " " + "\n");
        expected.add("FIELD_VOICE V: Voice 2         " + "\n");
        expected.add("ONE_REPEAT [1");
        expected.add("ACCIDENTAL ^");
        expected.add("BASENOTE C");
        expected.add("OCTAVE ,");
        expected.add("FRACTION_NOT_STRICT /4");
        expected.add("SPACE" + "  ");
        expected.add("BARLINE |");
        expected.add("NEWLINE" + " " + "\n");
        
        for (int i = 0; i < tokens.size(); i++)
        {
            assertEquals(tokens.get(i).toString(), expected.get(i));
        }
    }
    
}
