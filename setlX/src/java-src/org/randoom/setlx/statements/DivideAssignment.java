package org.randoom.setlx.statements;

import org.randoom.setlx.exceptions.SetlException;
import org.randoom.setlx.exceptions.TermConversionException;
import org.randoom.setlx.expressions.Expr;
import org.randoom.setlx.types.Term;
import org.randoom.setlx.types.Value;
import org.randoom.setlx.utilities.Environment;
import org.randoom.setlx.utilities.TermConverter;

/*
grammar rule:
assignmentOther
    : assignable ('/=' | [...] ) anyExpr
    ;

implemented here as:
      ==========                 =======
         mLhs                     mRhs
*/

public class DivideAssignment extends Statement {
    // functional character used in terms
    public  final static String     FUNCTIONAL_CHARACTER    = "^divideAssignment";
    // Trace all assignments. MAY ONLY BE SET BY ENVIRONMENT CLASS!
    public        static boolean    sTraceAssignments       = false;

    // precedence level in SetlX-grammar
    private final static int        PRECEDENCE              = 1000;

    private final Expr  mLhs;
    private final Expr  mRhs;

    public DivideAssignment(final Expr lhs, final Expr rhs) {
        mLhs  = lhs;
        mRhs  = rhs;
    }

    protected Value exec() throws SetlException {
        final Value assigned = mLhs.eval().divideAssign(mRhs.eval().clone());
        mLhs.assignUncloned(assigned);

        if (sTraceAssignments) {
            Environment.outWriteLn("~< Trace: " + mLhs + " := " + assigned + " >~");
        }

        return null;
    }

    /* string operations */

    public void appendString(final StringBuilder sb, final int tabs) {
        mLhs.appendString(sb, tabs);
        sb.append(" /= ");
        mRhs.appendString(sb, tabs);
    }

    /* term operations */

    public Term toTerm() {
        final Term result = new Term(FUNCTIONAL_CHARACTER, 2);
        result.addMember(mLhs.toTerm());
        result.addMember(mRhs.toTerm());
        return result;
    }

    public static DivideAssignment termToStatement(final Term term) throws TermConversionException {
        if (term.size() != 2) {
            throw new TermConversionException("malformed " + FUNCTIONAL_CHARACTER);
        } else {
            final Expr lhs = TermConverter.valueToExpr(term.firstMember());
            final Expr rhs = TermConverter.valueToExpr(PRECEDENCE, false, term.lastMember());
            return new DivideAssignment(lhs, rhs);
        }
    }

    // precedence level in SetlX-grammar
    public int precedence() {
        return PRECEDENCE;
    }
}

