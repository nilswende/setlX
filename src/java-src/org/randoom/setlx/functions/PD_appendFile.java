package org.randoom.setlx.functions;

import org.randoom.setlx.exceptions.FileNotWritableException;
import org.randoom.setlx.exceptions.IncompatibleTypeException;
import org.randoom.setlx.exceptions.SetlException;
import org.randoom.setlx.types.Value;
import org.randoom.setlx.utilities.State;

import java.util.List;

/**
 * appendFile(fileName, content) : Appends a list of strings to a file, each
 *                                 string representing a single line.
 */
public class PD_appendFile extends PD_writeFile {
    /** Definition of the PreDefinedProcedure `appendFile'. */
    public final static PreDefinedProcedure DEFINITION = new PD_appendFile();

    private PD_appendFile() {
        super();
    }

    @Override
    public Value execute(final State state, final List<Value> args, final List<Value> writeBackVars) throws SetlException {
        return exec(state, args, true);
    }
}

