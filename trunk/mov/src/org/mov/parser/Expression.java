package org.mov.parser;

import javax.swing.tree.*;

import org.mov.util.*;

public abstract class Expression extends DefaultMutableTreeNode {

    // Types
    public static final int BOOLEAN_TYPE = 0;
    public static final int VALUE_TYPE = 1;
    public static final int VOLUME_TYPE = 2;
    public static final int PRICE_TYPE = 3;
    public static final int QUOTE_TYPE = 4;
    
    public boolean equivelantTypes(int type1, int type2) {

	// Types are equivelant iff:
	// A They are the same
	// B The left type is VALUE_TYPE, the right either volume or price
	// C The right type is VALUE_TYPE, the left either volume or price

	if((type1 == type2) || // A
	   (type1 == VALUE_TYPE && (type2 == VOLUME_TYPE ||
				    type2 == PRICE_TYPE)) || // B
	   (type2 == VALUE_TYPE && (type1 == VOLUME_TYPE ||
				    type1 == PRICE_TYPE))) // C
	    return true;
	else
	    return false;
    }

    abstract public float evaluate(QuoteCache cache, String symbol, int day)
	throws EvaluationException;

    abstract public String toString();
    abstract public int checkType() throws TypeMismatchException;

    // move this to gene
    abstract public int getNeededChildren();
}


