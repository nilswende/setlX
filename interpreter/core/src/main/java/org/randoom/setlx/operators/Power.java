package org.randoom.setlx.operators;

import org.randoom.setlx.exceptions.SetlException;
import org.randoom.setlx.exceptions.TermConversionException;
import org.randoom.setlx.operatorUtilities.OperatorExpression;
import org.randoom.setlx.types.Term;
import org.randoom.setlx.types.Value;
import org.randoom.setlx.utilities.FragmentList;
import org.randoom.setlx.utilities.State;

import java.util.ArrayDeque;
import java.util.List;

/**
 * Operator that raises the second value from the stack by the first one.
 */
public class Power extends ABinaryInfixOperator {
    /** Singleton **/
    public static final Power P = new Power();

    private Power() {}

    @Override
    public Value evaluate(State state, ArrayDeque<Value> values, OperatorExpression operatorExpression, int currentStackDepth) throws SetlException {
        Value rhs = values.poll();
        Value lhs = values.poll();
        return lhs.power(state, rhs);
    }

    @Override
    public void appendOperatorSign(State state, StringBuilder sb, List<String> expressions) {
        sb.append(" ** ");
    }

    /**
     * Append the operator represented by a term to the supplied operator stack.
     *
     * @param state                    Current state of the running setlX program.
     * @param term                     Term to convert.
     * @param operatorStack            Operator to append to.
     * @throws TermConversionException If term is malformed.
     */
    public static void appendToOperatorStack(final State state, final Term term, FragmentList<AOperator> operatorStack) throws TermConversionException {
        appendToOperatorStack(state, term, operatorStack, P);
    }

    @Override
    public boolean isLeftAssociative() {
        return false;
    }

    @Override
    public boolean isRightAssociative() {
        return true;
    }

    @Override
    public int precedence() {
        return 2000;
    }

    private final static long COMPARE_TO_ORDER_CONSTANT = generateCompareToOrderConstant(Power.class);

    @Override
    public long compareToOrdering() {
        return COMPARE_TO_ORDER_CONSTANT;
    }

    @Override
    public int computeHashCode() {
        return (int) COMPARE_TO_ORDER_CONSTANT;
    }
}
