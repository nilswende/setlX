package org.randoom.setlx.operatorUtilities;

import org.randoom.setlx.assignments.AAssignableExpression;
import org.randoom.setlx.exceptions.SetlException;
import org.randoom.setlx.exceptions.TermConversionException;
import org.randoom.setlx.exceptions.UndefinedOperationException;
import org.randoom.setlx.types.CollectionValue;
import org.randoom.setlx.types.Om;
import org.randoom.setlx.types.Term;
import org.randoom.setlx.utilities.FragmentList;
import org.randoom.setlx.utilities.ImmutableCodeFragment;
import org.randoom.setlx.utilities.State;

import java.util.List;

/**
 * Base class for various expressions that can 'fill' a collection.
 */
public abstract class CollectionBuilder extends ImmutableCodeFragment {
    /**
     * Create a list of assignable expressions from this collection builder.
     *
     * @return                             AssignableExpressions.
     * @throws UndefinedOperationException if operator can not be converted.
     */
    public FragmentList<AAssignableExpression> convertToAssignableExpressions() throws UndefinedOperationException {
        throw new UndefinedOperationException("Expression cannot be converted");
    }

    /**
     * Expand given collection with values generated by executing this builder.
     *
     * @param state          Current state of the running setlX program.
     * @param collection     Collection to extend.
     * @throws SetlException Thrown in case of some (user-) error.
     */
    public abstract void fillCollection(
        final State           state,
        final CollectionValue collection
    ) throws SetlException;

    @Override // Explicit override to increase visibility to public.
    public abstract boolean collectVariablesAndOptimize (
        final State        state,
        final List<String> boundVariables,
        final List<String> unboundVariables,
        final List<String> usedVariables
    );

    /* String operations */

    @Override
    public void appendString(final State state, final StringBuilder sb, final int tabs) {
        appendString(state, sb);
    }

    /**
     * Appends a string representation of this code fragment to the given
     * StringBuilder object.
     *
     * @see org.randoom.setlx.utilities.CodeFragment#appendString(State, StringBuilder, int)
     *
     * @param state Current state of the running setlX program.
     * @param sb    StringBuilder to append to.
     */
    public abstract void appendString(final State state, final StringBuilder sb);

    /* term operations */

    @Override
    public Om toTerm(final State state) {
        return Om.OM;
    }

    /**
     * Expand given CollectionValue with the term representation of this builder.
     *
     * @param state          Current state of the running setlX program.
     * @param collection     CollectionValue to expand.
     * @throws SetlException Thrown in case the term is malformed.
     */
    public abstract void addToTerm(final State state, final CollectionValue collection) throws SetlException;

    /**
     * Regenerate CollectionBuilder from a CollectionValue containing a term
     * representation.
     *
     * @param state                    Current state of the running setlX program.
     * @param value                    CollectionValue containing the term representation.
     * @return                         Regenerated CollectionBuilder object.
     * @throws TermConversionException Thrown in case the term is malformed.
     */
    public static CollectionBuilder collectionValueToBuilder(final State state, final CollectionValue value) throws TermConversionException {
        if (value.size() == 1 && value.firstMember().getClass() == Term.class) {
            final Term    term = (Term) value.firstMember();
            final String  fc   = term.getFunctionalCharacter();
            if (fc.equals(SetlIteration.getFunctionalCharacter())) {
                return SetlIteration.termToIteration(state, term);
            } else if (fc.equals(Range.getFunctionalCharacter())) {
                return Range.termToRange(state, term);
            } else if (fc.equals(ExplicitListWithRest.getFunctionalCharacter())) {
                return ExplicitListWithRest.termToExplicitListWithRest(state, term);
            } else {
                // assume explicit list of a single term
                return ExplicitList.collectionValueToExplicitList(state, value);
            }
        } else {
            // assume explicit list;
            return ExplicitList.collectionValueToExplicitList(state, value);
        }
    }
}

