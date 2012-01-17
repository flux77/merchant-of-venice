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

import nz.org.venice.parser.expression.NumberExpression;
import nz.org.venice.parser.expression.AddExpression;

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
	
	//These expressions shouldn't be generated anymore from GP
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

	    /*
	    assertEquals(typeTest(left5, right5, 
				  Expression.INTEGER_TYPE,
				  Expression.FLOAT2_TYPE), true);	    
	    */

	} catch (TypeMismatchException e) {
	    fail("Type Mistmatch UnExpected" + e);
	}
    }
        
    public void testEvaluate() {
	String absExpString = "abs(-0.0001)";
	String cosExpString = "cos(0.0)";
	String cos2ExpString = "cos(3.141592653/2.0)";
	String sinExpString = "sin(3.141592653)";
	String randExpString = "random(23042394)";
	String randExpString2 = "random(23042394)";
	String expSymString = "close(\"THISSYMBOLNOTFOUND\")";

	Expression absExp = parse(absExpString);
	Expression cosExp = parse(cosExpString);
	Expression sinExp = parse(sinExpString);
	Expression randExp = parse(randExpString);
	Expression randExp2 = parse(randExpString);
	Expression expSymExp = parse(expSymString);

	try {
	    Variables emptyVars = new Variables();
	    double absVal = absExp.evaluate(emptyVars, null, null, 0);	    
	    double cosVal = cosExp.evaluate(emptyVars, null, null, 0);
	    double sinVal = sinExp.evaluate(emptyVars, null, null, 0);
	    double randVal = randExp.evaluate(emptyVars, null, null, 0);
	    //Want to check that the rand function will return the same value
	    //given the same seed.
	    double randVal2 = randExp2.evaluate(emptyVars, null, null, 0); 
	    
	
	    assertTrue(absVal > 0.0);
	    assertTrue(withinEpsilon(cosVal, 1.0));
	    assertTrue(withinEpsilon(sinVal, 0.0));
	    assertTrue(withinEpsilon(randVal, 0.653709));
	    assertTrue(withinEpsilon(randVal, 0.653709));
	    
	} catch (EvaluationException e) {
	    System.out.println("Evaluation Exception: " + e);
	}
		
	try {
	    Variables emptyVars = new Variables();
	    double closeVal = expSymExp.evaluate(emptyVars, null, null, 0);
	    fail("Was expecting evaluationException, got: " + closeVal);
	} catch (EvaluationException e) {
	    assertTrue(true);
	}
	
    }
    
    public void testCommentParse() {
	String str1 = "(close > 5)";
	String str2 = "/* rule */ (close > 5)";
	String str3 = "(close > 5)/* rule */";
	String str4 = "/* rule */ /* next comment */(close > 5)/* rule */";
	String str5 = "/* rule /* nested comment */*/(close > 5)";
	String str6 = "/* no end comment marker (close > 5)";
	String str7 = "/* no nested /* end comment marker */ (close > 5)";

	Expression exp1 = parse(str1);	
	Expression exp2 = parse(str2);
	Expression exp3 = parse(str3);
	Expression exp4 = parse(str4);
	Expression exp5 = parse(str5);
	

	assertTrue(exp2 != null && exp2.equals(exp1));
	assertTrue(exp3 != null && exp3.equals(exp1));
	assertTrue(exp4 != null && exp4.equals(exp1));
	assertTrue(exp5 != null && exp5.equals(exp1));

	assertTrue(failParse(str6));
	assertTrue(failParse(str7));
    }

    public void testParameterNum() {
	//failParse sets up int variables, x,y
	String test1 = "int function parmtest(int p1, int p2, int p3) { p1+p2} int\ntest = parmtest(x,y)\ntrue";

	String test2 = "int function parmtest(int p1, int p2) { p1+p2} int test = parmtest(x,y,x)\ntrue";

	String test3 = "int function parmtest(int p1, int p2) { p1+p2} int test = parmtest(x,y)\ntrue";

	String test4 = "int n = 0\nint function outer() { n }\nint function parmtest() { n = outer() } int test = parmtest()\ntrue";
	

	assertTrue(failParse(test1));
	assertTrue(failParse(test2));
	Expression exp3 = parse(test3);
	Expression exp4 = parse(test4);
	assertTrue(exp3 != null);		
	assertTrue(exp4 != null);		
    }

    public void testParameterType() {
	String test1 = "int n = 0\n int function noop(double val) { val = 3.14 }\n n = noop(n)";
	
	String test2 = "int n = 0\nint function real2int(double val) { val = 2.0}\nn = real2int(3.14)";

	String test3 = "int n = 0\nint function real2int(double val) { val = 3}\nn = real2int(3.14)";

	String test4 = "int function retInt() { 3 }\nfloat foo = retInt()";
	
	String test5 = "int foo = 0\nint function localVar() {int foo = 5}";
	String test6 = "int bar = 0\nint function localVar(int foo) {int bar = 5}";
	
	assertTrue(failParse(test1));
	assertTrue(failParse(test2));
	Expression exp3 = parse(test3);
	assertTrue(test3 != null);
	assertTrue(failParse(test4));
	assertTrue(failParse(test5));
	assertTrue(failParse(test6));
	
    }
    
    public void testFunctionReturnType() {
	String func1 = "float function retFl() { 3.14 }\n int function retInt() { float foo = retFl()\n -50}\n lag(close, retInt())";
	
	Expression funcExp1 = parse(func1);
	assertTrue(funcExp1 == null);
	
    }

    public void testFunctionEval() {
	String caller1 = "int function incme(int n) { n = n + 1}\nint function decme(int n) { n = n - 1}\nfor (int i = 0; i < 5; i = i + 1) { incme(decme(i)) } i";
	
	String caller2 = "int function noop(int n) { n }\nint function incme(int n) { n = n + 1}\nfloat j = 0.0\nfor (int i = 0; i < 5; i = incme(noop(i))) { float k = 0.0\nk = k + 1.0\nj = j + 0.1} j";

	//Simple recursive functions
	String rec1 = "int function fact(int n) { if (n >= 2) { n*fact(n-1) } else { 1 }}\nfact(5)";
	
	String rec2 = "int function fib(int n) { if (n >= 2) { fib(n-1) + fib(n-2) } else { 1 }}\nfib(5)";

	String rec3 = "int function fib(int n) { if (n >= 2) { fib(n-2) + fib(n-1) } else { 1 }}\nfib(5)";

	Expression callExp1 = parse(caller1);
	Expression callExp2 = parse(caller2);

	Expression exprec1 = parse(rec1);
	Expression exprec2 = parse(rec2);
	Expression exprec3 = parse(rec3);
	
	assertTrue(callExp1 != null);
	assertTrue(callExp2 != null);
	assertTrue(exprec1 != null);
	assertTrue(exprec2 != null);
	assertTrue(exprec3 != null);

	try {
	    Variables variables = new Variables();
	    double callEval1 = callExp1.evaluate(variables, null, null, 0);
	    double callEval2 = callExp2.evaluate(variables, null, null, 0);
	    double recEval1 = exprec1.evaluate(variables, null, null, 0);
	    double recEval2 = exprec2.evaluate(variables, null, null, 0);
	    double recEval3 = exprec3.evaluate(variables, null, null, 0);
	    
	    assertTrue(callEval1 == 5.0);
	    assertTrue(callEval2 == 0.5);
	    assertTrue(recEval1 == 120.0);
	    assertTrue(recEval2 == 8.0);
	    assertTrue(recEval3 == 8.0);
	} catch (EvaluationException e) {
	    System.out.println(e);
	    assertTrue(false);
	}
	
    }

    public void testLimits() {
	String forTest = "for (int i = 0; i > 0; i = i + 1) { int n = 0\n n = n + 1 }";
	
	String whileTest = "for (int i = 0; i > 0; i = i + 1) { int n = 0\n n = n + 1 }";

	AnalyserGuard.getInstance().setRuntimeLimit(10);
	Expression forExp = parse(forTest);
	Expression whileExp = parse(whileTest);

	assertTrue(forExp != null);
	assertTrue(whileExp != null);
	
	try {
	    Variables variables = new Variables();
	    double forEval = forExp.evaluate(variables, null, null, 0);	    
	    
	    assertTrue(false);
	} catch (EvaluationException e) {
	    if (e == EvaluationException.EVAL_TIME_TOO_LONG_EXCEPTION) {
		assertTrue(true);
	    } else {
		assertTrue(false);
	    }
	}

	try {
	    Variables variables = new Variables();
	    double whileEval = whileExp.evaluate(variables, null, null, 0);	    	    
	    assertTrue(false);
	} catch (EvaluationException e) {
	    if (e == EvaluationException.EVAL_TIME_TOO_LONG_EXCEPTION) {
		assertTrue(true);
	    } else {
		assertTrue(false);
	    }
	}	
    }
    
    
    public void testClauseReturn() {
	String test1 = "int n = 0\nfor (int i = 0; i < 10; i = i + 1) { n = n + 1} n";
	String test2 = "int foo = 0\nint n = 0\nfor (int i = 0; i < 10; i = i + 1) { foo = foo - 3\nn = n + 1} n";

	Expression exp1 = parse(test1, Expression.INTEGER_TYPE);
	Expression exp2 = parse(test2, Expression.INTEGER_TYPE);
	assertTrue(exp1 != null);
	assertTrue(exp2 != null);

	try {
	    Variables variables = new Variables();
	    double clauseEval1 = exp1.evaluate(variables, null, null, 0);
	    double clauseEval2 = exp2.evaluate(variables, null, null, 0);	    
	    assertTrue(clauseEval1 == 10.0);
	    assertTrue(clauseEval2 == 10.0);
	} catch (EvaluationException e) {
	    assertTrue(false);
	}
    }
    
    public void testEqualsHashcodeContract() {
	String test1 = "int n = 0\nfor (int i = 0; i < 10; i = i + 1) { n = n + 1} n";
	String test2 = "int n = 0\nfor (int i = 0; i < 10; i = i + 1) { n = n + 1} n";

	String test3 = "int n = 0\nfor (int i = 0; i < 10; i = i + 1) { n = n + 1} n";
	
	String test4 = "int k = 0\nfor (int j = 0; j < 10; j = j + 1) { k = k + 1} k";

	String test5 = "int j = 10\nwhile (j > 0) { j = j - 1} j";

	String test6 = "int k = 0\nfor (int l = 0; l < 10; l = l + 1) { k = k + 1} k";
	
	Expression exp1 = parse(test1, Expression.INTEGER_TYPE);
	Expression exp2 = parse(test2, Expression.INTEGER_TYPE);
	Expression exp3 = parse(test3, Expression.INTEGER_TYPE);
	Expression exp4 = parse(test4, Expression.INTEGER_TYPE);
	Expression exp5 = parse(test5, Expression.INTEGER_TYPE);
	Expression exp6 = parse(test5, Expression.INTEGER_TYPE);
	assertTrue(exp1 != null);
	assertTrue(exp2 != null);
	assertTrue(exp3 != null);
	assertTrue(exp4 != null);
	assertTrue(exp5 != null);
	assertTrue(exp6 != null);

	//reflexifity test
	assertTrue(exp1.equals(exp1));
	
	//symmetry
	assertTrue(exp1.equals(exp2));
	assertTrue(exp2.equals(exp1));

	//transitive
	assertTrue(exp1.equals(exp2));
	assertTrue(exp2.equals(exp3));
	assertTrue(exp1.equals(exp3));

	//consistency
	assertTrue(exp1.equals(exp2));
	assertTrue(exp1.equals(exp2));
	assertTrue(exp1.equals(exp2));

	assertTrue(exp3.equals(exp4) == false);
	assertTrue(exp3.equals(exp4) == false);
	assertTrue(exp3.equals(exp4) == false);
	
	//null test
	assertTrue(exp1.equals(null) == false);

	//general equality
	assertTrue(exp4.equals(exp5) == false);
	assertTrue(exp3.equals(exp4) == false);
	//Difference is in free variable name - in future may
	//want this to be true rather than false. But we've got lots to get
	//right first in Gondola before we get there.
	assertTrue(exp4.equals(exp6) == false);	

	//hashcode tests
	assertTrue(exp1.hashCode() == exp2.hashCode());
	assertTrue(exp1.hashCode() == exp3.hashCode());
	assertTrue(exp2.hashCode() == exp3.hashCode());
	assertTrue(exp1.hashCode() == exp1.hashCode());
	assertTrue(exp4.hashCode() != exp5.hashCode());       
    }

    public void testHashCode() {
	Expression num1 = new NumberExpression(3.1415926);
	Expression num2 = new NumberExpression(3);

	assertTrue(num1.hashCode() != num2.hashCode());
	
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
    
    private boolean failParse(String string) {	
	try {
            Variables variables = new Variables();
            variables.add("x", Expression.INTEGER_TYPE, false);
            variables.add("y", Expression.INTEGER_TYPE, false);
            variables.add("a", Expression.BOOLEAN_TYPE, false);
            variables.add("b", Expression.BOOLEAN_TYPE, false);
            variables.add("c", Expression.BOOLEAN_TYPE, false);
	    
            Parser.parse(variables, string);
	    return false;
        }
        catch(ExpressionException e) {
            return true;
        }
    }

    private String simplify(String string) {       
	Expression expression = parse(string);

	if (expression != null) {
	    Expression retExp = expression.simplify();	
	    return retExp.toString();
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
