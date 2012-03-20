package interpreter.statements;

import interpreter.exceptions.SetlException;
import interpreter.exceptions.TermConversionException;
import interpreter.expressions.Expr;
import interpreter.types.SetlList;
import interpreter.types.Term;
import interpreter.types.Value;
import interpreter.utilities.Condition;
import interpreter.utilities.Environment;
import interpreter.utilities.MatchResult;
import interpreter.utilities.TermConverter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/*
grammar rule:
statement
    : [...]
    | 'match' '(' expr ')' '{' ('case' exprList ':' block)* ('default' ':' block)? '}'
    ;

implemented here as:
                                       ========     =====
                                        mTerms   mStatements
*/

public class MatchCaseBranch extends MatchAbstractBranch {
    // functional character used in terms
    /*package*/ final static String FUNCTIONAL_CHARACTER = "'matchCaseBranch";

    private List<Expr>  mExprs;      // expressions which creates terms to match
    private List<Value> mTerms;      // terms to match
    private Block       mStatements; // block to execute after match

    public MatchCaseBranch(List<Expr> exprs, Block statements){
        mExprs      = exprs;
        mTerms      = new ArrayList<Value>(exprs.size());
        for (Expr expr: exprs) {
            mTerms.add(expr.toTerm());
        }
        mStatements = statements;
    }

    private MatchCaseBranch(List<Expr> exprs, List<Value> terms, Block statements){
        mExprs      = exprs;
        mTerms      = terms;
        mStatements = statements;
    }

    public MatchResult matches(Value term) {
        MatchResult last = new MatchResult(false);
        for (Value v : mTerms) {
            last = v.matchesTerm(term);
            if (last.isMatch()) {
                return last;
            }
        }
        return last;
    }

    public void execute() throws SetlException {
        mStatements.execute();
    }

    /* string operations */

    public String toString(int tabs) {
        String result = Environment.getTabs(tabs);
        result += "case ";

        Iterator<Expr> iter = mExprs.iterator();
        while (iter.hasNext()) {
            Expr expr   = iter.next();
            result += expr.toString(tabs);
            if (iter.hasNext()) {
                result += ", ";
            }
        }

        result += ":" + Environment.getEndl();
        result += mStatements.toString(tabs + 1) + Environment.getEndl();
        return result;
    }

    /* term operations */

    public Term toTerm() {
        Term     result   = new Term(FUNCTIONAL_CHARACTER);

        SetlList termList = new SetlList();
        for (Value v: mTerms) {
            termList.addMember(v);
        }
        result.addMember(termList);
        result.addMember(mStatements.toTerm());
        return result;
    }

    public static MatchCaseBranch termToBranch(Term term) throws TermConversionException {
        if (term.size() != 2 || ! (term.firstMember() instanceof SetlList)) {
            throw new TermConversionException("malformed " + FUNCTIONAL_CHARACTER);
        } else {
            SetlList    termList    = (SetlList) term.firstMember();
            List<Expr>  exprs       = new ArrayList<Expr>();
            List<Value> terms       = new ArrayList<Value>();
            for (Value v : termList) {
                exprs.add(TermConverter.valueToExpr(v));
                terms.add(v);
            }
            Block       block       = TermConverter.valueToBlock(term.lastMember());
            return new MatchCaseBranch(exprs, terms, block);
        }
    }
}
