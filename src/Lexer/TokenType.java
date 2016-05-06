package Lexer;

import java.util.regex.Pattern;


public class TokenType {
	public final String name;
	public final Pattern pattern;

	public TokenType(String name, Pattern pattern) {
		this.name = name;
		this.pattern = pattern;
	}
}