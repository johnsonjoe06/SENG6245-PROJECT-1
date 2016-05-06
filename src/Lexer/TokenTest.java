package Lexer;

import static org.junit.Assert.*;

import java.util.regex.Pattern;

import org.junit.Test;


public class TokenTest {
    public final static TokenType FIELD_NUM = new TokenType("FIELD_NUM",
            Pattern.compile("X:.*\\n"));
    
    @Test
    public void testEquals(){
        Token t = new Token("X:\n", FIELD_NUM);
        Token f = new Token("X:sfa\n", FIELD_NUM);
        assertEquals(t, t);
        assertFalse(t.equals(f));
        assertTrue(t.equals(new Token("X:\n", FIELD_NUM)));
    }

}
