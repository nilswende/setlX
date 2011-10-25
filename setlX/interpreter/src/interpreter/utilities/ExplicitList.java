package interpreter.utilities;

import interpreter.exceptions.IncompatibleTypeException;
import interpreter.exceptions.SetlException;
import interpreter.expressions.Expr;
import interpreter.types.CollectionValue;
import interpreter.types.SetlInt;
import interpreter.types.SetlList;
import interpreter.types.Value;

import java.util.List;

public class ExplicitList extends Constructor {
    private List<Expr> mList;

    public ExplicitList(List<Expr> exprList) {
        mList   = exprList;
    }

    public void fillCollection(CollectionValue collection) throws SetlException {
        for (Expr e: mList) {
            collection.addMember(e.eval());
        }
    }

    // sets the variables used to form this list to the variables from the list given as a parameter
    public void setIds(SetlList list) throws SetlException {
        if (list.size() != mList.size()) {
            throw new IncompatibleTypeException("Members of `" + list + "´ are unusable for list assignment.");
        }
        for (int i = 0; i < mList.size(); ++i) {
            Expr  e = mList.get(i);
            Value v = null;
            try {
                v = list.getMember(new SetlInt(i + 1));
            } catch (SetlException se) { /* this can not fail at this point */};

            e.assign(v);
        }
    }

    public int size() {
        return mList.size();
    }

    public String toString(int tabs) {
        String r = "";
        for (Expr e: mList) {
            if (!r.equals("")) {
                r += ", ";
            }
            r += e.toString(tabs);
        }
        return r;
    }
}

