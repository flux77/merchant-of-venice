package org.mov.parser;

import java.util.*;

public class Parser {

    // Grammar in EBNF 
    // EXPR = BOOLEAN_EXPR [ LOGIC BOOLEAN_EXPR ]
    // LOGIC = "and" | "or"
    // BOOLEAN_EXPR = ADD_EXPR [ RELATION ADD_EXPR ]
    // RELATION = "==" | "<=" | ">=" | "<" | ">" | "!="
    // ADD_EXPR = MULTIPLY_EXPR [ ADD_OPERATOR MULTIPLY_EXPR ]
    // ADD_OPERATION = "-" | "+"
    // MULTIPLY_EXPR = FACTOR [ MULTIPLY_OPERATOR FACTOR ]
    // MULTIPLY_OPERATOR = "*" | "/"
    // FACTOR = VARIABLE | NUMBER | FUNCTION | "(" EXPR ")"
    // NUMBER = ["-"]{0-9}+ ["." {0-9}+]
    // VARIABLE = "held" | "age" 
    // QUOTE      "day_open" | "day_close" | "day_low" | "day_high" | 
    //            "day_volume"
    // FUNCTION = "lag" "(" QUOTE "," EXPR ")" | 
    //            "min" "(" QUOTE "," EXPR "," EXPR ")" | 
    //            "max" "(" QUOTE "," EXPR "," EXPR ")" |
    //            "avg" "(" QUOTE "," EXPR "," EXPR ")" |
    //		  "rsi" "(" EXPR "," EXPR ")" |
    //            "not" "(" EXPR ")" |
    //		  "percent" "(" EXPR "," EXPR ")" |
    //            "if"  "(" EXPR ")" "{" EXPR "}" "else" "{" EXPR "}"

    public Expression parse(String string) throws ExpressionException
    {
	// Perform lexical analysis on string - i.e. reduce it to stack of
	// tokens
	TokenStack tokens = lexicalAnalysis(string);

	// Translate stack of tokens to expression
	Expression expression = parseExpression(tokens);

	// Check for type mismatch
	expression.checkType();

	return expression;
    }

    private TokenStack lexicalAnalysis(String string) 
	throws ParserException {

	TokenStack tokens = new TokenStack();
	Token token;

	while(string.length() > 0) {

	    // skip leading spaces
	    while(Character.isWhitespace(string.charAt(0)))
		string = string.substring(1);

	    // Extract next token
	    token = new Token();
	    string = Token.stringToToken(token, string);
	    tokens.add(token);
	}

	return tokens;
    }

    private Expression parseExpression(TokenStack tokens) 
	throws ParserException {

	Expression left = parseBooleanExpression(tokens);

	if(tokens.match(Token.AND_TOKEN) ||
	   tokens.match(Token.OR_TOKEN)) {

	    Token operation = tokens.pop();
	    Expression right = parseBooleanExpression(tokens);

	    return(ExpressionFactory.newExpression(operation, left, right));
	}
	return left;
    }

    private Expression parseBooleanExpression(TokenStack tokens) 
	throws ParserException {

	Expression left = parseAddExpression(tokens);

	if(tokens.match(Token.EQUAL_TOKEN) ||
	   tokens.match(Token.NOT_EQUAL_TOKEN) ||
	   tokens.match(Token.LESS_THAN_EQUAL_TOKEN) ||
	   tokens.match(Token.LESS_THAN_TOKEN) ||
	   tokens.match(Token.GREATER_THAN_TOKEN) ||
	   tokens.match(Token.GREATER_THAN_EQUAL_TOKEN)) {
	    
	    Token operation = tokens.pop();
	    Expression right = parseAddExpression(tokens);

	    return(ExpressionFactory.newExpression(operation, left, right));
	}
	return left;
    }
	
    private Expression parseAddExpression(TokenStack tokens) 
	throws ParserException {

	Expression left = parseMultiplyExpression(tokens);

	if(tokens.match(Token.ADD_TOKEN) ||
	   tokens.match(Token.SUBTRACT_TOKEN)) {

	    Token operation = tokens.pop();	    
	    Expression right = parseMultiplyExpression(tokens);

	    return(ExpressionFactory.newExpression(operation, left, right));
	}
	return left;
    }
    
    private Expression parseMultiplyExpression(TokenStack tokens) 
	throws ParserException {
	
	Expression left = parseFactor(tokens);

	if(tokens.match(Token.MULTIPLY_TOKEN) ||
	   tokens.match(Token.DIVIDE_TOKEN)) {
	    
	    Token operation = tokens.pop();
	    Expression right = parseFactor(tokens);

	    return(ExpressionFactory.newExpression(operation, left, right));
	}
	
	return left;
    }	

    private Expression parseFactor(TokenStack tokens) 
	throws ParserException {

	Expression expression;

	// VARIABLE
	if(tokens.match(Token.HELD_TOKEN) ||
	   tokens.match(Token.AGE_TOKEN)) {
	    expression = parseVariable(tokens);
	}

	// NUMBER
	else if(tokens.match(Token.NUMBER_TOKEN) || 
		tokens.match(Token.SUBTRACT_TOKEN)) {
	    expression = parseNumber(tokens);
	}
	
	// FUNCTION
	else if(tokens.match(Token.LAG_TOKEN) ||
		tokens.match(Token.MIN_TOKEN) ||
		tokens.match(Token.MAX_TOKEN) ||
		tokens.match(Token.AVG_TOKEN) ||
		tokens.match(Token.RSI_TOKEN) ||
		tokens.match(Token.NOT_TOKEN) ||
		tokens.match(Token.IF_TOKEN) ||
		tokens.match(Token.PERCENT_TOKEN)) {
	    expression = parseFunction(tokens);
	}

	// EXPRESSION
	else if(tokens.match(Token.LEFT_PARENTHESIS_TOKEN)) {
	    tokens.pop();
	    expression = parseExpression(tokens);
	    parseRightParenthesis(tokens);
	}
	else
	    throw new ParserException("unexpected symbol");

	return expression;
    }

    private Expression parseVariable(TokenStack tokens) 
	throws ParserException {

	Token variable = tokens.pop();
	Expression expression;
	
	switch(variable.getType()) {
	case(Token.HELD_TOKEN):
	case(Token.AGE_TOKEN):
	    expression = ExpressionFactory.newExpression(variable);
	    break;
	default:
	    throw new ParserException("expected variable");
	}

	return expression;
    }

    private Expression parseQuote(TokenStack tokens) 
	throws ParserException {
	
	Token quote = tokens.pop();
	Expression expression;
	
	switch(quote.getType()) {
	case(Token.DAY_OPEN_TOKEN):
	case(Token.DAY_CLOSE_TOKEN):
	case(Token.DAY_LOW_TOKEN):
	case(Token.DAY_HIGH_TOKEN):
	case(Token.DAY_VOLUME_TOKEN):
	    expression = ExpressionFactory.newExpression(quote);
	    break;
	default:
	    throw new ParserException("expected quote type");
	}

	return expression;
    }

    private Expression parseNumber(TokenStack tokens) 
	throws ParserException {

	Token number = tokens.pop();
	boolean negate = false;

	// Is there a "-" infront? Handle negative numbers 
	if(number.getType() == Token.SUBTRACT_TOKEN) {
	    number = tokens.pop();
	    negate = true;
	}

	if(number.getType() == Token.NUMBER_TOKEN) {
	    if(negate)
		number.negate();

	    return ExpressionFactory.newExpression(number);	    
	}
	else
	    throw new ParserException("expected number");
    }

    private Expression parseFunction(TokenStack tokens) 
	throws ParserException {

	Expression expression;
	Expression arg1 = null;
	Expression arg2 = null;
	Expression arg3 = null;
	    
	Token function = tokens.pop();

	// all functions must have a left parenthesis after the function
	// name
	parseLeftParenthesis(tokens);

	switch(function.getType()) {
	case(Token.LAG_TOKEN):

	    arg1 = parseQuote(tokens);
	    parseComma(tokens);
	    arg2 = parseExpression(tokens);
	    break;

	case(Token.MIN_TOKEN):

	    arg1 = parseQuote(tokens);
	    parseComma(tokens);
	    arg2 = parseExpression(tokens);
	    parseComma(tokens);
	    arg3 = parseExpression(tokens);	    
	    break;
	    
	case(Token.MAX_TOKEN):
	    
	    arg1 = parseQuote(tokens);
	    parseComma(tokens);
	    arg2 = parseExpression(tokens);
	    parseComma(tokens);
	    arg3 = parseExpression(tokens);	    
	    break;
	
	case(Token.AVG_TOKEN):
	    
	    arg1 = parseQuote(tokens);
	    parseComma(tokens);
	    arg2 = parseExpression(tokens);
	    parseComma(tokens);
	    arg3 = parseExpression(tokens);	    
	    break;

	case(Token.RSI_TOKEN):
	    
	    arg1 = parseExpression(tokens);
	    parseComma(tokens);
	    arg2 = parseExpression(tokens);	    
	    break;

	case(Token.NOT_TOKEN):

	    arg1 = parseExpression(tokens);
	    break;
					       
	case(Token.IF_TOKEN):
	    
	    arg1 = parseExpression(tokens);
	    parseRightParenthesis(tokens);
	    parseLeftBrace(tokens);
	    arg2 = parseExpression(tokens);
	    parseRightBrace(tokens);
	    parseElse(tokens);
	    parseLeftBrace(tokens);
	    arg3 = parseExpression(tokens);
	    parseRightBrace(tokens);				
	    break;
	    
	case(Token.PERCENT_TOKEN):

	    arg1 = parseExpression(tokens);
	    parseComma(tokens);
	    arg2 = parseExpression(tokens);
	    break;

	default:
	    throw new ParserException("expected function");
	}

	// create epxression
	expression = ExpressionFactory.newExpression(function, arg1, arg2, 
						     arg3);
	
	// all functions must end with a right parenthesis (ignore IF
	// because weve already removed its right parenthesis
	if(function.getType() != Token.IF_TOKEN)
	    parseRightParenthesis(tokens);

	return expression;
    }

    private void parseComma(TokenStack tokens) throws ParserException {
	if(!tokens.pop(Token.COMMA_TOKEN))
	    throw new ParserException("expected comma");
    }

    private void parseLeftParenthesis(TokenStack tokens) 
	throws ParserException {
	if(!tokens.pop(Token.LEFT_PARENTHESIS_TOKEN))
	    throw new ParserException("expected left parenthesis");
    }

    private void parseRightParenthesis(TokenStack tokens) 
	throws ParserException {
	if(!tokens.pop(Token.RIGHT_PARENTHESIS_TOKEN))
	    throw new ParserException("missing right parenthesis");
    }

    private void parseLeftBrace(TokenStack tokens) throws ParserException {
	if(!tokens.pop(Token.LEFT_BRACE_TOKEN))
	    throw new ParserException("expected left brace");
    }

    private void parseRightBrace(TokenStack tokens) 
	throws ParserException {

	if(!tokens.pop(Token.RIGHT_BRACE_TOKEN))
	    throw new ParserException("missing right brace");
    }

    private void parseElse(TokenStack tokens) throws ParserException {
	if(!tokens.pop(Token.ELSE_TOKEN))
	    throw new ParserException("expected else token");
    }
}

