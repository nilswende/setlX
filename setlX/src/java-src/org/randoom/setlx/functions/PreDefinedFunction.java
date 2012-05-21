package org.randoom.setlx.functions;

import org.randoom.setlx.exceptions.IncorrectNumberOfParametersException;
import org.randoom.setlx.exceptions.SetlException;
import org.randoom.setlx.expressions.Expr;
import org.randoom.setlx.statements.Block;
import org.randoom.setlx.types.ProcedureDefinition;
import org.randoom.setlx.types.SetlString;
import org.randoom.setlx.types.Term;
import org.randoom.setlx.types.Value;
import org.randoom.setlx.utilities.Environment;
import org.randoom.setlx.utilities.ParameterDef;
import org.randoom.setlx.utilities.VariableScope;
import org.randoom.setlx.utilities.WriteBackAgent;

import java.util.ArrayList;
import java.util.List;

public abstract class PreDefinedFunction extends ProcedureDefinition {
    // functional characters used in terms
    private final static String  FUNCTIONAL_CHARACTER = "^preDefinedProcedure";
    // continue execution of this function in debug mode until it returns. MAY ONLY BE SET BY ENVIRONMENT CLASS!
    public        static boolean sStepThroughFunction = false;

    private String  mName;
    private boolean mUnlimitedParameters;
    private boolean mAllowFewerParameters;
    private boolean mDoNotChangeScope;

    protected PreDefinedFunction(String name) {
        super(new ArrayList<ParameterDef>(), new Block());
        mName                   = name;
        mUnlimitedParameters    = false;
        mAllowFewerParameters   = false;
        mDoNotChangeScope       = false;
    }

    public final String getName() {
        return mName;
    }

    // add parameters to own definition
    protected void addParameter(String param) {
        mParameters.add(new ParameterDef(param, ParameterDef.READ_ONLY));
    }
    protected void addParameter(String param, int type) {
        mParameters.add(new ParameterDef(param, type));
    }

    // allow an unlimited number of parameters
    protected void enableUnlimitedParameters() {
        mUnlimitedParameters    = true;
    }

    // allow an calling with fewer number of parameters then specified
    protected void allowFewerParameters() {
        mAllowFewerParameters   = true;
    }

    // do not create a new scope during execution
    protected void doNotChangeScope() {
        mDoNotChangeScope       = true;
    }

    // this function is to be implemented by all predefined functions
    public abstract Value execute(List<Value> args, List<Value> writeBackVars) throws SetlException;

    // this function is called from within SetlX
    public Value call(List<Expr> exprs, List<Value> args) throws SetlException {
        if (mParameters.size() < args.size()) {
            if (mUnlimitedParameters) {
                // unlimited means: at least the number of defined parameters or more
                // no error
            } else {
                String error = "Procedure is defined with a fewer number of parameters ";
                error +=       "(" + mParameters.size();
                if (mAllowFewerParameters) {
                    error +=   " or less";
                }
                error +=       ").";
                throw new IncorrectNumberOfParametersException(error);
            }
        } else if (mParameters.size() > args.size()) {
            if (mAllowFewerParameters) {
                // fewer parameters are allowed
                // no error
            } else {
                String error = "Procedure is defined with a larger number of parameters ";
                error +=       "(" + mParameters.size();
                if (mUnlimitedParameters) {
                    error +=   " or more";
                }
                error +=       ").";
                throw new IncorrectNumberOfParametersException(error);
            }
        }

        // save old scope
        VariableScope oldScope = VariableScope.getScope();
        // create new scope used for the function call
        VariableScope.setScope(oldScope.cloneFunctions());

        if (mDoNotChangeScope) {
            //we just changed our mind
            VariableScope.setScope(oldScope);
        }

        // List of writeBack-values, which should be stored into the outer scope
        ArrayList<Value>    writeBackVars   = new ArrayList<Value>(mParameters.size());

        // results of call to predefined function
        Value               result          = null;
        WriteBackAgent      wba             = null;

        if (sStepThroughFunction) {
            Environment.setDebugStepThroughFunction(false);
        }

        try {
            // call predefined function (which may add writeBack-values to List)
            result  = this.execute(args, writeBackVars);

            // extract 'rw' arguments from writeBackVars list and store them into WriteBackAgent
            if (( ! mDoNotChangeScope) && writeBackVars.size() > 0) {
                wba = new WriteBackAgent(writeBackVars.size());
                for (int i = 0; i < mParameters.size(); ++i) {
                    ParameterDef param = mParameters.get(i);
                    if (param.getType() == ParameterDef.READ_WRITE && writeBackVars.size() > 0) {
                        // value of parameter after execution
                        Value postValue = writeBackVars.remove(0);
                        // expression used to fill parameter before execution
                        Expr  preExpr   = exprs.get(i);
                        /* if possible the WriteBackAgent will set the variable used in
                           this expression to its postExecution state in the outer scope */
                        wba.add(preExpr, postValue);
                    }
                }
            }

        } finally {  // make sure scope is always reset
            if ( ! mDoNotChangeScope) {
                // restore old scope
                VariableScope.setScope(oldScope);
                // write values in WriteBackAgent into restored scope
                if (wba != null) {
                    wba.writeBack();
                }
            }
        }

        return result;
    }

    /* string and char operations */

    public final String toString(int tabs) {
        String endl = Environment.getEndl();
        String result = "procedure (";
        for (int i = 0; i < mParameters.size(); ++i) {
            if (i > 0) {
                result += ", ";
            }
            result += mParameters.get(i);
        }
        if (mUnlimitedParameters) {
            if (mParameters.size() > 0) {
                result += ", ";
            }
            result += "...";
        }
        result += ") {" + endl;
        result += Environment.getLineStart(tabs + 1) + "/* predefined procedure `" + mName + "' */" + endl;
        result += Environment.getLineStart(tabs) + "}";
        return result;
    }

    /* term operations */

    public Value toTerm() {
        Term result = new Term(FUNCTIONAL_CHARACTER);

        result.addMember(new SetlString(mName));

        return result;
    }
}
