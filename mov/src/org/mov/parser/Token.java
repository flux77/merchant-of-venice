package org.mov.parser;

public class Token {

    // Token types

    //
    // Add percent(number, number) function
    //

    public static final int LEFT_PARENTHESIS_TOKEN = 0;   // (
    public static final int RIGHT_PARENTHESIS_TOKEN = 1;  // )
    public static final int LESS_THAN_EQUAL_TOKEN = 2;    // <=
    public static final int GREATER_THAN_EQUAL_TOKEN = 3; // >=
    public static final int LESS_THAN_TOKEN = 4;          // <
    public static final int GREATER_THAN_TOKEN = 5;       // >
    public static final int EQUAL_TOKEN = 6;              // ==
    public static final int ADD_TOKEN = 7;                // +
    public static final int SUBTRACT_TOKEN = 8;           // -
    public static final int MULTIPLY_TOKEN = 9;           // *
    public static final int DIVIDE_TOKEN = 10;            // /
    public static final int OR_TOKEN = 11;                // or
    public static final int AND_TOKEN = 12;               // and
    public static final int NOT_TOKEN = 13;               // not(boolean)    
    public static final int COMMA_TOKEN = 14;             // ,
    public static final int LAG_TOKEN = 15;               // lag(type, lag)
    public static final int MIN_TOKEN = 16;         // min(type, days, lag)
    public static final int MAX_TOKEN = 17;         // max(type, days, lag)
    public static final int AVG_TOKEN = 18;         // avg(type, days, lag)
    public static final int HELD_TOKEN = 19;              // held
    public static final int DAY_OPEN_TOKEN = 20;          // day_open
    public static final int DAY_CLOSE_TOKEN = 21;         // day_close
    public static final int DAY_LOW_TOKEN = 22;           // day_low
    public static final int DAY_HIGH_TOKEN = 23;          // day_high
    public static final int DAY_VOLUME_TOKEN = 24;        // day_volume
    public static final int IF_TOKEN = 25;                // if
    public static final int LEFT_BRACE_TOKEN = 26;        // {
    public static final int RIGHT_BRACE_TOKEN = 27;       // }
    public static final int FULLSTOP_TOKEN = 28;          // .
    public static final int ELSE_TOKEN = 29;              // else
    public static final int AGE_TOKEN = 30;               // age
    public static final int PERCENT_TOKEN = 31;      // percent(number, %)
    public static final int NOT_EQUAL_TOKEN = 32;         // !=
    public static final int RSI_TOKEN = 33;	     // rsi(type, days, lag)
    public static final int FIXED_LENGTH_TOKENS = 34;

    public static final int NUMBER_TOKEN = 100;           // [0-9]+
    
    private int type;
    private int intValue; // for number tokens
    
    public static String stringToToken(Token token, String string) 
	throws ParserException {

	String[] tokenStrings = new String[FIXED_LENGTH_TOKENS];

	// Map of token ID's to strings - match order
	tokenStrings[LEFT_PARENTHESIS_TOKEN]   = "(";
	tokenStrings[RIGHT_PARENTHESIS_TOKEN]  = ")";
	tokenStrings[LESS_THAN_EQUAL_TOKEN]    = "<=";
	tokenStrings[GREATER_THAN_EQUAL_TOKEN] = ">=";
	tokenStrings[LESS_THAN_TOKEN]          = "<";
	tokenStrings[GREATER_THAN_TOKEN]       = ">";
	tokenStrings[EQUAL_TOKEN]              = "==";
	tokenStrings[ADD_TOKEN]                = "+";
	tokenStrings[SUBTRACT_TOKEN]           = "-";
	tokenStrings[MULTIPLY_TOKEN]           = "*";
	tokenStrings[DIVIDE_TOKEN]             = "/";
	tokenStrings[OR_TOKEN]                 = "or";
	tokenStrings[AND_TOKEN]                = "and";
	tokenStrings[NOT_TOKEN]                = "not";
	tokenStrings[COMMA_TOKEN]              = ",";
	tokenStrings[HELD_TOKEN]               = "held";
	tokenStrings[LAG_TOKEN]                = "lag";
	tokenStrings[MIN_TOKEN]                = "min";
	tokenStrings[MAX_TOKEN]                = "max";
	tokenStrings[AVG_TOKEN]                = "avg";
	tokenStrings[DAY_OPEN_TOKEN]           = "day_open";
	tokenStrings[DAY_CLOSE_TOKEN]          = "day_close";
	tokenStrings[DAY_LOW_TOKEN]            = "day_low";
	tokenStrings[DAY_HIGH_TOKEN]           = "day_high";
	tokenStrings[DAY_VOLUME_TOKEN]         = "day_volume";
	tokenStrings[IF_TOKEN]                 = "if";
	tokenStrings[LEFT_BRACE_TOKEN]         = "{";
	tokenStrings[RIGHT_BRACE_TOKEN]        = "}";
	tokenStrings[FULLSTOP_TOKEN]           = ".";
	tokenStrings[ELSE_TOKEN]               = "else";
	tokenStrings[AGE_TOKEN]                = "age";
	tokenStrings[PERCENT_TOKEN]            = "percent";
	tokenStrings[NOT_EQUAL_TOKEN]          = "!=";
	tokenStrings[RSI_TOKEN]		       = "rsi";

	boolean matched = false;

	// First check to see if its a number 
	if(Character.isDigit(string.charAt(0))) {
	    int value = 0;

	    do {
		// All this to convert a character to an integer!
		value = value*10 + 
		    Integer.parseInt(string.substring(0, 1));
		string = string.substring(1);
	    } while(string.length() > 0 && 
		    Character.isDigit(string.charAt(0)));

	    token.setType(Token.NUMBER_TOKEN);
	    token.setIntValue(value);

	    matched = true;
	}
	else 
	    for(int i = 0; !matched && i < tokenStrings.length; i++)
		if(tokenStrings[i] != null && 
		   string.startsWith(tokenStrings[i])) {
		    
		    token.setType(i);
		    
		    // move string along
		    string = string.substring(tokenStrings[i].length());
		    matched = true;
		}
	
	if(!matched)
	    throw new ParserException("unknown symbol");

	return string;
    }

    public Token() {
	type = 0;
	intValue = 0;
    }

    public int getType() {
	return type;
    }
    
    public void setType(int type) {
	this.type = type;
    }

    public int getIntValue() {
	return intValue;
    }

    public void negate() {
	intValue = -intValue;
    }

    public void setIntValue(int value) {
	intValue = value;
    }
}
