/* Merchant of Venice - technical analysis software for the stock market.
   Copyright (C) 2002 Andrew Leppard (aleppard@picknowl.com.au)

   This program is free software; you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation; either version 2 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package nz.org.venice.parser;

import junit.framework.TestCase;


public class ExpressionTest extends TestCase {
    
    public void testEquals() {
        // Symmetry
        assertEquals(parse("a or b"), parse("b or a"));
        assertEquals(parse("a and b"), parse("b and a"));
        assertEquals(parse("x+y"), parse("y+x"));
        assertEquals(parse("x*y"), parse("y*x"));
        assertEquals(parse("x==y"), parse("y==x"));
        assertEquals(parse("x!=y"), parse("y!=x"));

        // Variables and numbers are handled specially
        assertEquals(parse("true"), parse("true"));
        assertEquals(parse("false"), parse("false"));
        assertEquals(parse("12.0"), parse("12.0"));
        assertEquals(parse("12"), parse("12"));
        assertEquals(parse("a"), parse("a"));
        assertEquals(parse("x"), parse("x"));	
    }
    

    
    public void testSimplify() {
        // Not
        assertEquals("true", simplify("not(false)"));
        assertEquals("false", simplify("not(not(false))"));
        assertEquals("true", simplify("not(not(not(false)))"));
        assertEquals("x<=y", simplify("not(x>y)"));
        assertEquals("x>=y", simplify("not(x<y)"));
        assertEquals("x<y", simplify("not(x>=y)"));
        assertEquals("x>y", simplify("not(x<=y)"));
        assertEquals("x!=y", simplify("not(x==y)"));
        assertEquals("x==y", simplify("not(x!=y)"));

        // Or
        assertEquals("false", simplify("false or false"));
        assertEquals("true", simplify("false or true"));
        assertEquals("true", simplify("true or false"));
        assertEquals("true", simplify("true or true"));
        assertEquals("a", simplify("a or false"));
        assertEquals("true", simplify("a or true"));
        assertEquals("a", simplify("false or a"));
        assertEquals("true", simplify("true or a"));
        assertEquals("a", simplify("a or a"));
        assertEquals("a or b", simplify("a or b"));

        // And
        assertEquals("false", simplify("false and false"));
        assertEquals("false", simplify("false and true"));
        assertEquals("false", simplify("true and false"));
        assertEquals("true", simplify("true and true"));
        assertEquals("false", simplify("a and false"));
        assertEquals("a", simplify("a and true"));
        assertEquals("false", simplify("false and a"));
        assertEquals("a", simplify("true and a"));
        assertEquals("true", simplify("a and a"));
        assertEquals("a and b", simplify("a and b"));

        // /
        assertEquals("2", simplify("10/5"));
        assertEquals("1", simplify("x/x")); // Pragmatism over idealism
        assertEquals("0", simplify("0/x"));
        assertEquals("x", simplify("x/1"));
        assertEquals("x/y", simplify("x/y"));

        // +
        assertEquals("15", simplify("5+10"));
        assertEquals("x", simplify("0+x"));
        assertEquals("x", simplify("x+0"));
        assertEquals("2*x", simplify("x+x"));
        assertEquals("2*(x+y)", simplify("(x+y)+(x+y)"));

        // -
        assertEquals("-5", simplify("5-10"));
        assertEquals("x", simplify("x-0"));
        assertEquals("0", simplify("x-x"));
        assertEquals("x-y", simplify("x-y"));

        // *
        assertEquals("50", simplify("10*5"));
        assertEquals("x", simplify("x*1"));
	//Bug here
        assertEquals("x", simplify("1*x"));
        assertEquals("0", simplify("x*0"));
        assertEquals("0", simplify("0*x"));
        assertEquals("x*y", simplify("x*y"));

        // <
        assertEquals("true", simplify("5<10"));
        assertEquals("false", simplify("11<10"));
        assertEquals("false", simplify("x<x"));
        assertEquals("x<y", simplify("x<y"));

        // >
       assertEquals("true", simplify("10>5"));
        assertEquals("false", simplify("5>10"));
        assertEquals("false", simplify("x>x"));
        assertEquals("x>y", simplify("x>y"));

        // <=
        assertEquals("true", simplify("5<=10"));
        assertEquals("false", simplify("11<=10"));
        assertEquals("true", simplify("x<=x"));
        assertEquals("x<=y", simplify("x<=y"));

        // >=
        assertEquals("true", simplify("10>=5"));
        assertEquals("false", simplify("5>=10"));
        assertEquals("true", simplify("x>=x"));
        assertEquals("x>=y", simplify("x>=y"));

        // ==
        assertEquals("true", simplify("5==5"));
        assertEquals("false", simplify("5==10"));
        assertEquals("true", simplify("x==x"));
        assertEquals("x==y", simplify("x==y"));

        // !=
        assertEquals("true", simplify("5!=10"));
        assertEquals("false", simplify("5!=5"));
        assertEquals("false", simplify("x!=x"));
        assertEquals("x!=y", simplify("x!=y"));

        // If
        assertEquals("a", simplify("if(true) {a} else {b}"));
        assertEquals("b", simplify("if(false) {a} else {b}"));
        assertEquals("b", simplify("if(a) {b} else {b}"));
        assertEquals("if(a)\n   a\nelse\n   b\n", simplify("if(a) {a} else {b}"));
        assertEquals("if(a)\n   b\nelse\n   a\n", simplify("if(not(a)) {a} else {b}"));

        // Sqrt
        assertEquals("5", simplify("sqrt(25)"));

        // Abs
        assertEquals("10", simplify("abs(10)"));
        assertEquals("10", simplify("abs(-10)"));

    }
    
    
    //Test that simplify maintains type correctness
    //Simplify used to change the type of an expression
    //as in (0.0 + x) == -3.076. When x is integer, 
    //simplified expression, x == -3.076 is no type correct
    public void testSimplifyTypes() {

	String left1 = "x == -3.076121";
	String right1 = "(exp(-23.259159) + x) == -3.076121";
	String left2 = "x + 0 == -3.076121"; 
	String right2 = "x == -3.076121";
	String left3 = "1.0 * x == -3.076121";
	String right3 = "x == -3.076121";
	String left4 = "1.0 * x == -3.076121";
	String right4 = "x == -3.076121";
	
	String left5 = "(obv(abs(-43), -17, 1*(ema(low, x, -4, 0.434678))))>(momentum(volume, 5, -26))";

	String right5 = "(obv(43, -17, ema(low, x, -4, 0.434678)))>(momentum(volume, 5, -26))";


	try {
	    
	    assertEquals(typeTest(left1, right1, 
				  Expression.FLOAT_TYPE,
				  Expression.INTEGER_TYPE), true);
	    
	    
	    assertEquals(typeTest(left2, right2, 
				  Expression.FLOAT_TYPE,
				  Expression.FLOAT_TYPE), true);	    

	    assertEquals(typeTest(left3, right3, 
				  Expression.INTEGER_TYPE,
				  Expression.FLOAT_TYPE), true);	    
	    
	    assertEquals(typeTest(left4, right4, 
				  Expression.FLOAT_TYPE,
				  Expression.FLOAT_TYPE), true);	    

	    assertEquals(typeTest(left5, right5, 
				  Expression.INTEGER_TYPE,
				  Expression.INTEGER_TYPE), true);	    

	} catch (TypeMismatchException e) {
	    fail("Type Mistmatch UnExpected" + e);
	}
    }
          
    
    public void testEvaluate() {
	String absExpString = "abs(-0.0001)";
	String cosExpString = "cos(0.0)";
	String cos2ExpString = "cos(3.141592653/2.0)";
	String sinExpString = "sin(3.141592653)";

	Expression absExp = parse(absExpString);
	Expression cosExp = parse(cosExpString);
	Expression sinExp = parse(sinExpString);

	try {
	    Variables emptyVars = new Variables();
	    double absVal = absExp.evaluate(emptyVars, null, null, 0);	    
	    double cosVal = cosExp.evaluate(emptyVars, null, null, 0);
	    double sinVal = sinExp.evaluate(emptyVars, null, null, 0);
	    
	    assertTrue(absVal > 0.0);
	    assertTrue(withinEpsilon(cosVal, 1.0));
	    assertTrue(withinEpsilon(sinVal, 0.0));
	    
	} catch (EvaluationException e) {
	    System.out.println("Evaluation Exception: " + e);
	}
	
	
    }
    


    private Expression parse(String string) {
	return parse(string, Expression.INTEGER_TYPE);	
    }

    private Expression parse(String string, int type) {
        try {
            Variables variables = new Variables();
            variables.add("x", type, false);
            variables.add("y", type, false);
            variables.add("a", Expression.BOOLEAN_TYPE, false);
            variables.add("b", Expression.BOOLEAN_TYPE, false);
            variables.add("c", Expression.BOOLEAN_TYPE, false);

            return Parser.parse(variables, string);
        }
        catch(ExpressionException e) {
            System.out.println(e);
            assert false;
            return null;
        }
    }

    private String simplify(String string) {
        Expression expression = parse(string);

	if (expression != null) {
	    expression = expression.simplify();
	    return expression.toString();
	}
          return "";
    }

    //Test that the types of the two expressions are compatible after
    //simplification. 
    //
    //e1, e2 are the expressions
    //type1, type2 are the types of the variables in e1, e2 respectively. 
    private boolean typeTest(String e1, String e2, 
			     int type1, int type2) throws TypeMismatchException {
	int t1 = -1;
	int t2 = -1;
	
	Expression exp1 = parse(e1, type1);	
	Expression exp2 = parse(e2, type2);

	if (exp1 != null && exp2 != null) {
	    t1 = exp1.checkType();
	    exp2 = exp2.simplify();
	   
	    if (exp2 != null) {
		t2 = exp2.checkType();		
		return (t1 == t2);
	    }	    	    
	}	
	return false;
    }
    
    private boolean withinEpsilon(double val, double testVal) {
	double epsilon = 0.000005;
	
	if (val - epsilon <= testVal &&
	    val + epsilon >= testVal) {
	    return true;
	}
	return false;
    } 

}
