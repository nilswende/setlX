package org.randoom.setlx.boolExpressions;

import org.randoom.setlx.exceptions.SetlException;
import org.randoom.setlx.exceptions.TermConversionException;
import org.randoom.setlx.expressions.Expr;
import org.randoom.setlx.types.SetlBoolean;
import org.randoom.setlx.types.Term;
import org.randoom.setlx.types.Value;
import org.randoom.setlx.utilities.State;
import org.randoom.setlx.utilities.TermConverter;

import java.util.List;

/**
 * Implementation of the greater-or-equal operator.
 *
 * grammar rule:
 * comparison
 *     : expr '>=' expr
 *     ;
 *
 * implemented here as:
 *       ====      ====
 *       lhs        rhs
 */
public class GreaterOrEqual extends Expr {
    // functional character used in terms
    private final static String FUNCTIONAL_CHARACTER = generateFunctionalCharacter(GreaterOrEqual.class);
    // precedence level in SetlX-grammar
    private final static int    PRECEDENCE           = 1500;

    private final Expr lhs;
    private final Expr rhs;

    /**
     * Create new GreaterOrEqual.
     *
     * @param lhs Expression to evaluate and compare.
     * @param rhs Expression to evaluate and compare.
     */
    public GreaterOrEqual(final Expr lhs, final Expr rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    /*
     * Note that these comparisons do not always behave as expected when
     * the compared values are no numbers!
     *
     * For example:
     * Set comparisons are based upon the subset relation
     * a < b
     * is true, when a is a subset of b.
     *
     * However the negation of this comparison
     * !(a < b)
     * is not (in all cases) equal to
     * a >= b
     * because it is possible that a is not a subset of b AND
     * b is neither subset nor equal to a (e.g. a := {1}; b := {2}).
     * In this case
     * !(a < b)
     * would be true, but
     * a >= b
     * would be false.
     *
     * When comparing numbers
     * !(a < b)
     * would always be the same as
     * a >= b
     */

    @Override
    protected SetlBoolean evaluate(final State state) throws SetlException {
        try {
            final Value lhs = this.lhs.eval(state);
            final Value rhs = this.rhs.eval(state);
            // note: rhs and lhs swapped!
            return SetlBoolean.valueOf(rhs.isEqualTo(state, lhs) == SetlBoolean.TRUE || rhs.isLessThan(state, lhs) == SetlBoolean.TRUE);
        } catch (final SetlException se) {
            se.addToTrace("Error in substitute comparison \"(" + rhs.toString(state) + " == " + lhs.toString(state) + ") || (" + rhs.toString(state) + " < " + lhs.toString(state) +  ")\":");
            throw se;
        }
    }

    @Override
    protected void collectVariables (
        final State        state,
        final List<String> boundVariables,
        final List<String> unboundVariables,
        final List<String> usedVariables
    ) {
        lhs.collectVariablesAndOptimize(state, boundVariables, unboundVariables, usedVariables);
        rhs.collectVariablesAndOptimize(state, boundVariables, unboundVariables, usedVariables);
    }

    /* string operations */

    @Override
    public void appendString(final State state, final StringBuilder sb, final int tabs) {
        lhs.appendBracketedExpr(state, sb, tabs, PRECEDENCE, false);
        sb.append(" >= ");
        rhs.appendBracketedExpr(state, sb, tabs, PRECEDENCE, true);
    }

    /* term operations */

    @Override
    public Term toTerm(final State state) throws SetlException {
        final Term result = new Term(FUNCTIONAL_CHARACTER, 2);
        result.addMember(state, lhs.toTerm(state));
        result.addMember(state, rhs.toTerm(state));
        return result;
    }

    /**
     * Convert a term representing a GreaterOrEqual into such an expression.
     *
     * @param state                    Current state of the running setlX program.
     * @param term                     Term to convert.
     * @return                         Resulting expression.
     * @throws TermConversionException Thrown in case of a malformed term.
     */
    public static GreaterOrEqual termToExpr(final State state, final Term term) throws TermConversionException {
        if (term.size() != 2) {
            throw new TermConversionException("malformed " + FUNCTIONAL_CHARACTER);
        } else {
            final Expr lhs = TermConverter.valueToExpr(state, term.firstMember());
            final Expr rhs = TermConverter.valueToExpr(state, term.lastMember());
            return new GreaterOrEqual(lhs, rhs);
        }
    }

    @Override
    public int precedence() {
        return PRECEDENCE;
    }
}

