package org.randoom.setlx.statements;

import org.randoom.setlx.exceptions.TermConversionException;
import org.randoom.setlx.expressions.Variable;
import org.randoom.setlx.types.SetlList;
import org.randoom.setlx.types.Term;
import org.randoom.setlx.types.Value;
import org.randoom.setlx.utilities.Environment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/*
grammar rule:
statement
    : 'var' listOfVariables ';'
    | [...]
    ;

listOfVariables
    : variable (',' variable)*
    ;
*/

public class GlobalDefinition extends Statement {
    // functional character used in terms (MUST be class name starting with lower case letter!)
    private final static String FUNCTIONAL_CHARACTER = "^globalDefinition";

    private final List<Variable> mVars;

    public GlobalDefinition(final List<Variable> vars) {
        mVars = vars;
    }

    protected Value exec() {
        for (final Variable var : mVars) {
            var.makeGlobal();
        }
        return null;
    }

    /* string operations */

    public void appendString(final StringBuilder sb, final int tabs) {
        Environment.getLineStart(sb, tabs);
        sb.append("var ");

        final Iterator<Variable> iter = mVars.iterator();
        while (iter.hasNext()) {
            iter.next().appendString(sb, 0);
            if (iter.hasNext()) {
                sb.append(", ");
            }
        }

        sb.append(";");
    }

    /* term operations */

    public Term toTerm() {
        Term result = new Term(FUNCTIONAL_CHARACTER, 1);

        final SetlList varList = new SetlList(mVars.size());
        for (final Variable var : mVars) {
            varList.addMember(var.toTerm());
        }
        result.addMember(varList);

        return result;
    }

    public static GlobalDefinition termToStatement(final Term term) throws TermConversionException {
        if (term.size() != 1 || ! (term.firstMember() instanceof SetlList)) {
            throw new TermConversionException("malformed " + FUNCTIONAL_CHARACTER);
        } else {
            final SetlList          vars    = (SetlList) term.firstMember();
            final List<Variable>    varList = new ArrayList<Variable>(vars.size());
            for (final Value v : vars) {
                if ( ! (v instanceof Term)) {
                    throw new TermConversionException("malformed " + FUNCTIONAL_CHARACTER);
                }
                varList.add(Variable.termToExpr((Term) v));
            }
            return new GlobalDefinition(varList);
        }
    }
}

