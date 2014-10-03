package org.randoom.setlx.expressionUtilities;

import org.randoom.setlx.exceptions.SetlException;
import org.randoom.setlx.exceptions.TermConversionException;
import org.randoom.setlx.expressions.Expr;
import org.randoom.setlx.types.CollectionValue;
import org.randoom.setlx.types.Rational;
import org.randoom.setlx.types.SetlString;
import org.randoom.setlx.types.Term;
import org.randoom.setlx.types.Value;
import org.randoom.setlx.utilities.State;
import org.randoom.setlx.utilities.TermConverter;

import java.util.List;

/**
 * A range between two values, with optional step size.
 *
 * grammar rule:
 * range
 *     : expr (',' expr)? '..' expr
 *     ;
 *
 * implemented here as:
 *       ====      ====        ====
 *       start    second       stop
 */
public class Range extends CollectionBuilder {
    private final static String FUNCTIONAL_CHARACTER = generateFunctionalCharacter(Range.class);

    private final Expr start;
    private final Expr second;
    private final Expr stop;

    /**
     * Create new Range.
     *
     * @param start  Expression to evaluate to the start value.
     * @param second Expression to evaluate to the step value.
     * @param stop   Expression to evaluate to the stop value.
     */
    public Range(final Expr start, final Expr second, final Expr stop) {
        this.start  = start;
        this.second = second;
        this.stop   = stop;
    }

    @Override
    public void fillCollection(final State state, final CollectionValue collection) throws SetlException {
        final Value start = this.start.eval(state);
              Value step;
        // compute step
        if (second != null) {
            step = second.eval(state).difference(state, start);
        } else {
            step = Rational.ONE;
        }
        start.fillCollectionWithinRange(state, step, stop.eval(state), collection);
    }

    @Override
    public void collectVariablesAndOptimize (
        final State        state,
        final List<String> boundVariables,
        final List<String> unboundVariables,
        final List<String> usedVariables
    ) {
        start.collectVariablesAndOptimize(state, boundVariables, unboundVariables, usedVariables);
        if (second != null) {
            second.collectVariablesAndOptimize(state, boundVariables, unboundVariables, usedVariables);
        }
        stop.collectVariablesAndOptimize(state, boundVariables, unboundVariables, usedVariables);
    }

    /* string operations */

    @Override
    public void appendString(final State state, final StringBuilder sb) {
        start.appendString(state, sb, 0);
        if (second != null) {
            sb.append(", ");
            second.appendString(state, sb, 0);
        }
        sb.append(" .. ");
        stop.appendString(state, sb, 0);
    }

    /* term operations */

    @Override
    public void addToTerm(final State state, final CollectionValue collection) throws SetlException {
        final Term result = new Term(FUNCTIONAL_CHARACTER, 3);
        result.addMember(state, start.toTerm(state));
        if (second != null) {
            result.addMember(state, second.toTerm(state));
        } else {
            result.addMember(state, SetlString.NIL);
        }
        result.addMember(state, stop.toTerm(state));

        collection.addMember(state, result);
    }

    /**
     * Regenerate Range from a term representing this expression.
     *
     * @param state                    Current state of the running setlX program.
     * @param term                     Term representation.
     * @return                         Regenerated Range.
     * @throws TermConversionException Thrown in case the term is malformed.
     */
    /*package*/ static Range termToRange(final State state, final Term term) throws TermConversionException {
        if (term.size() != 3) {
            throw new TermConversionException("malformed " + FUNCTIONAL_CHARACTER);
        } else {
            try {
                final Expr start  = TermConverter.valueToExpr(state, term.firstMember());

                      Expr second = null;
                if (! term.getMember(2).equals(SetlString.NIL)) {
                    second  = TermConverter.valueToExpr(state, term.getMember(2));
                }

                final Expr stop   = TermConverter.valueToExpr(state, term.lastMember());
                return new Range(start, second, stop);
            } catch (final SetlException se) {
                throw new TermConversionException("malformed " + FUNCTIONAL_CHARACTER);
            }
        }
    }

    /**
     * Get the functional character used in terms.
     *
     * @return functional character used in terms.
     */
    /*package*/ static String getFunctionalCharacter() {
        return FUNCTIONAL_CHARACTER;
    }

    /* comparisons */

    @Override
    public int compareTo(final CollectionBuilder other) {
        if (this == other) {
            return 0;
        } else if (other.getClass() == Range.class) {
            Range range = (Range) other;
            int cmp = start.compareTo(range.start);
            if (cmp != 0) {
                return cmp;
            }
            cmp = stop.compareTo(range.stop);
            if (cmp != 0) {
                return cmp;
            }
            if (second != null) {
                if (range.second != null) {
                    return second.compareTo(range.second);
                } else {
                    return -1;
                }
            } if (range.second != null) {
                return 1;
            }
            return 0;
        } else {
            return (this.compareToOrdering() < other.compareToOrdering())? -1 : 1;
        }
    }

    private final static long COMPARE_TO_ORDER_CONSTANT = generateCompareToOrderConstant(Range.class);

    @Override
    public long compareToOrdering() {
        return COMPARE_TO_ORDER_CONSTANT;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        } else if (obj.getClass() == Range.class) {
            Range range = (Range) obj;
            if (start.equals(range.start) && stop.equals(range.stop)) {
                if (second != null && range.second != null) {
                    return second.equals(range.second);
                } else if (second == null && range.second == null) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = ((int) COMPARE_TO_ORDER_CONSTANT) + start.hashCode();
        hash = hash * 31 + stop.hashCode();
        if (second != null) {
            hash = hash * 31 + second.hashCode();
        }
        return hash;
    }
}

