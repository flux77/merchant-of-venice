package org.mov.parser;

import java.util.*;

/**
 * A stack of tokens which is used during parsing.
 */
public class TokenStack extends Vector {

    /**
     * Create a new token stack.
     */
    public TokenStack() {
	// nothing to do
    }

    // Get the token on the top of the stack
    private Token get() {
	if(size() > 0)
	    return (Token)firstElement();
	else
	    return null;
    }

    /**
     * Remove and return the token on the top of the stack.
     *
     * @return	the token on the top of the stack
     */
    public Token pop() {
	if(size() > 0)
	    return (Token)remove(0);
	else
	    return null;
    }

    /**
     * Remove the token on the top of the stack and compare it with the
     * given type.
     *
     * @param	tokenType	the expected token type on the stack
     * @return	<code>1</code> if the token is of the same type; 
     *		<code>0</code> otherwise
     */
    public boolean pop(int tokenType) {
	Token token = pop();
	
	if(token != null && tokenType == token.getType())
	    return true;
	else
	    return false;
    }

    /**
     * Compare the token on the top of the stack with the given type.
     * The token will not be removed from the stack.
     *
     * @param	tokenType	token type to compare with
     * @return	<code>1</code> if the token is of the same type; 
     *		<code>0</code> otherwise
     */
    public boolean match(int tokenType) {
	Token token = get();

	if(token != null && token.getType() == tokenType)
	    return true;
	else
	    return false;
    }


}
