package org.randoom.setlx.operators;

import org.randoom.setlx.exceptions.SetlException;
import org.randoom.setlx.operatorUtilities.CollectionBuilder;
import org.randoom.setlx.operatorUtilities.Stack;
import org.randoom.setlx.types.CollectionValue;
import org.randoom.setlx.types.SetlList;
import org.randoom.setlx.types.SetlSet;
import org.randoom.setlx.types.Term;
import org.randoom.setlx.types.Value;
import org.randoom.setlx.utilities.CodeFragment;
import org.randoom.setlx.utilities.State;

import java.util.List;

/**
 * A operator that puts a Set or List on the stack.
 */
public class SetListOperator extends AZeroOperator {
    /**
     * Type of collection to construct.
     */
    public enum CollectionType {
        /**
         * Construct a SetList.
         */
        LIST,
        /**
         * Construct a SetlSet.
         */
        SET
    }

    private final CollectionType    type;
    private final CollectionBuilder builder;

    /**
     * Create a new SetListOperator expression.
     *
     * @param type        Type of collection to construct.
     * @param constructor Collection contents generation object.
     */
    public SetListOperator(final CollectionType type, final CollectionBuilder constructor) {
        this.type    = type;
        this.builder = unify(constructor);
    }

    @Override
    public boolean collectVariablesAndOptimize(State state, List<String> boundVariables, List<String> unboundVariables, List<String> usedVariables) {
        if (builder != null) {
            return builder.collectVariablesAndOptimize(state, boundVariables, unboundVariables, usedVariables);
        }
        return true;
    }

    @Override
    public Value evaluate(State state, Stack<Value> values) throws SetlException {
        if (type == CollectionType.SET) {
            final SetlSet set = new SetlSet();
            if (builder != null) {
                builder.fillCollection(state, set);
            }
            return set;
        } else /* if (mType == LIST) */ {
            final SetlList list = new SetlList();
            if (builder != null) {
                builder.fillCollection(state, list);
            }
            list.compress();
            return list;
        }
    }

    @Override
    public void appendOperatorSign(State state, StringBuilder sb) {
        if (type == CollectionType.SET) {
            sb.append("{");
        } else /* if (mType == LIST) */ {
            sb.append("[");
        }
        if (builder != null) {
            builder.appendString(state, sb);
        }
        if (type == CollectionType.SET) {
            sb.append("}");
        } else /* if (mType == LIST) */ {
            sb.append("]");
        }
    }

    @Override
    public Value modifyTerm(State state, Term term) throws SetlException {
        final CollectionValue result;
        if (type == CollectionType.SET) {
            result = new SetlSet();
        } else /* if (type == CollectionType.LIST) */ {
            result = new SetlList();
        }
        if (builder != null) {
            builder.addToTerm(state, result);
        }
        return result;
    }

    private final static long COMPARE_TO_ORDER_CONSTANT = generateCompareToOrderConstant(SetListOperator.class);

    @Override
    public int compareTo(CodeFragment other) {
        if (this == other) {
            return 0;
        } else if (other.getClass() == SetListOperator.class) {
            SetListOperator otr = (SetListOperator) other;
            int cmp = type.compareTo(otr.type);
            if (cmp != 0) {
                return cmp;
            }

            if (builder != null) {
                if (otr.builder != null) {
                    return builder.compareTo(otr.builder);
                } else {
                    return 1;
                }
            } else if (otr.builder != null) {
                return -1;
            }
            return 0;
        } else {
            return (this.compareToOrdering() < other.compareToOrdering())? -1 : 1;
        }
    }

    @Override
    public long compareToOrdering() {
        return COMPARE_TO_ORDER_CONSTANT;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj.getClass() == SetListOperator.class) {
            SetListOperator other = (SetListOperator) obj;
            if (type == other.type) {
                if (builder != null && other.builder != null) {
                    return builder.equals(other.builder);
                } else if (builder == null && other.builder == null) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int computeHashCode() {
        int hash = ((int) COMPARE_TO_ORDER_CONSTANT) + type.hashCode();
        if (builder != null) {
            hash = hash * 31 + builder.hashCode();
        }
        return hash;
    }
}