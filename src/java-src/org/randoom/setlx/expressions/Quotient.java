package org.randoom.setlx.expressions;

import org.randoom.setlx.exceptions.SetlException;
import org.randoom.setlx.exceptions.TermConversionException;
import org.randoom.setlx.types.Term;
import org.randoom.setlx.types.Value;
import org.randoom.setlx.utilities.State;
import org.randoom.setlx.utilities.TermConverter;

import java.util.List;

/**
 * Implementation of the / operator.
 *
 * grammar rule:
 * product
 *     : reduce ([...] | '/' reduce)*
 *     ;
 *
 * implemented here as:
 *       ======              ======
 *        lhs                 rhs
 */
public class Quotient extends Expr {
    // functional character used in terms
    private final static String FUNCTIONAL_CHARACTER = generateFunctionalCharacter(Quotient.class);
    // precedence level in SetlX-grammar
    private final static int    PRECEDENCE           = 1700;

    private final Expr lhs;
    private final Expr rhs;

    /**
     * Constructor.
     *
     * @param lhs Left hand side of the operator.
     * @param rhs Right hand side of the operator.
     */
    public Quotient(final Expr lhs, final Expr rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    protected Value evaluate(final State state) throws SetlException {
        return lhs.eval(state).quotient(state, rhs.eval(state));
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
        sb.append(" / ");
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
     * Convert a term representing a Quotient expression into such an expression.
     *
     * @param state                    Current state of the running setlX program.
     * @param term                     Term to convert.
     * @return                         Resulting expression of this conversion.
     * @throws TermConversionException If term is malformed.
     */
    public static Quotient termToExpr(final State state, final Term term) throws TermConversionException {
        if (term.size() != 2) {
            throw new TermConversionException("malformed " + FUNCTIONAL_CHARACTER);
        } else {
            final Expr lhs = TermConverter.valueToExpr(state, term.firstMember());
            final Expr rhs = TermConverter.valueToExpr(state, term.lastMember());
            return new Quotient(lhs, rhs);
        }
    }

    @Override
    public int precedence() {
        return PRECEDENCE;
    }

    /**
     * Get the functional character used in terms.
     *
     * @return functional character used in terms.
     */
    public static String functionalCharacter() {
        return FUNCTIONAL_CHARACTER;
    }
}

