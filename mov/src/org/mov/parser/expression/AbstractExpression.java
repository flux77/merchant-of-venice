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

package org.mov.parser.expression;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mov.parser.Expression;

/**
 * The abstract base class for all expressions in the <i>Gondola</i> language. This
 * class implements the {@link Expression} interface and provides functions for
 * managing an expression's child nodes. E.g. the expression <code>4+5</code> would
 * consist of three nodes. The plus being the root node, which would have two
 * child nodes of <code>4</code> and <code>5</code>.
 *
 * @see Expression
 */
public abstract class AbstractExpression implements Expression {

    // Pointer to parent node (if any)
    private Expression parent = null;

    // Array of children.
    private Expression children[];

    /**
     * Create a new expression.
     */
    public AbstractExpression() {
        children = new Expression[getChildCount()];
    }

    /**
     * Get the parent of this node.
     */
    public Expression getParent() {
        return parent;
    }

    /**
     * Set the parent of this node.
     *
     * @param the new parent.
     */
    public void setParent(Expression parent) {
        assert parent != this;

        this.parent = parent;
    }

    /**
     * Return the child of this node at the given index.
     *
     * @return child at given index.
     */
    public Expression getChild(int index) {
        assert index <= getChildCount();

        return children[index];
    }

    /**
     * Return whether this node is the root node. The root node has no
     * parent.
     * 
     * @return <code>TRUE</code> iff this node is the root node.
     */
    public boolean isRoot() {
        return getParent() == null;
    }

    /**
     * Set this expression's child to the given child. The new child
     * will be removed from its parent.
     *
     * @param child the new child.
     * @param index the index of the new child.
     */
    public void setChild(Expression child, int index) {
        assert index <= getChildCount();
        assert child != this;

        // Remove reference to new child from its old parent
        if (child != null && child.getParent() != null) {
            Expression oldParent = child.getParent();
            oldParent.setChild(null, oldParent.getIndex(child));
        }

        // Remove parent reference to this class from old child
        if (getChild(index) != null)
            getChild(index).setParent(null);

        // Set parent reference to this class in new child
        if (child != null)
            child.setParent(this);

        // Set reference to new child in this class
        children[index] = child;
    }
    
    /**
     * Perform simplifications and optimisations on the expression tree.
     * For example, if the expression tree was <code>a and true</code> then the
     * expression tree would be simplified to <code>a</code>.
     */
    public Expression simplify() {
        // Simplify child arguments
        if(getChildCount() > 0) {
            for(int i = 0; i < getChildCount(); i++) 
                setChild(getChild(i).simplify(), i);
        }

        return this;
    }

    /**
     * Return the index of the given argument in the expression. This
     * method uses reference equality rather than using the equals
     * method.
     *
     * @param child the child expression to locate
     * @return index of the child expression or <code>-1</code> if it could
     *              not be found
     */
    public int getIndex(Expression child) {
        for(int index = 0; index < getChildCount(); index++) {
            if(getChild(index) == child)
                return index;
        }
        
        // Not found
        return -1;
    }

    /**
     * Returns whether this expression tree and the given expression tree
     * are equivalent.
     *
     * @param object the other expression
     */
    public boolean equals(Object object) {

        // Top level nodes the sames?
        if(!(object instanceof Expression))
            return false;
        Expression expression = (Expression)object;
        
        if(getClass() != expression.getClass())
            return false;
        
        // Check all children are the same - only check the children
        // we currently have - we might not have them all yet!
        for(int i = 0; i < getChildCount(); i++) {
            if(!getChild(i).equals(expression.getChild(i)))
                return false;
        }

        return true;
    }

    /**
     * If you override the {@link #equals} method then you should override
     * this method. It provides a very basic hash code function.
     *
     * @return a poor hash code of the tree
     */
    public int hashCode() {
        // If you implement equals you should implement hashCode().
        // Since I don't need it I haven't bothered to implement a very
        // good hash.
        return getClass().hashCode();
    }

    /** 
     * Count the number of nodes in the tree.
     *
     * @return number of nodes or 1 if this is a terminal node
     */
    public int size() {
        int count = 1;

        for(int i = 0; i < getChildCount(); i++) {
            Expression child = getChild(i);

            count += child.size();
        }

        return count;
    }

    /**
     * Count the number of nodes in the tree with the given type.
     *
     * @return number of nodes in the tree with the given type.
     */
    public int size(int type) {
        int count = 0;

        assert(type == BOOLEAN_TYPE || type == FLOAT_TYPE || type == INTEGER_TYPE ||
               type == FLOAT_QUOTE_TYPE || type == INTEGER_QUOTE_TYPE);

        if(getType() == type)
            count = 1;

        for(int i = 0; i < getChildCount(); i++) {
            Expression child = getChild(i);

            count += child.size(type);
        }

        return count;
    }

    /**
     * Return an iterator over the node's children.
     *
     * @return iterator.
     */
    public Iterator iterator() {
        List list = new ArrayList();
        buildIterationList(list, this);
        return list.iterator();
    }

    private void buildIterationList(List list, Expression expression) {
        list.add(expression);
        
        for(int i = 0; i < expression.getChildCount(); i++)
            buildIterationList(list, expression.getChild(i));
    }

    abstract public Object clone();
}
