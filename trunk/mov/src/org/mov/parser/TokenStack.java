package org.mov.parser;

import java.util.*;

public class TokenStack extends Vector {

    public Token get() {
	if(size() > 0)
	    return (Token)firstElement();
	else
	    return null;
    }

    public Token pop() {
	if(size() > 0)
	    return (Token)remove(0);
	else
	    return null;
    }

    public boolean pop(int tokenType) {
	Token token = pop();
	
	if(tokenType == token.getType())
	    return true;
	else
	    return false;
    }

    public boolean match(int tokenType) {
	Token token = get();

	if(token != null && token.getType() == tokenType)
	    return true;
	else
	    return false;
    }


}
