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

import nz.org.venice.parser.Expression;

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

    private Expression parse(String string) {
        try {
            Variables variables = new Variables();
            variables.add("x", Expression.INTEGER_TYPE, false);
            variables.add("y", Expression.INTEGER_TYPE, false);
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
        expression = expression.simplify();
        return expression.toString();
    }
}
