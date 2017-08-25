package sami.markup;

import java.util.HashMap;

/**
 *
 * @author nbb
 */
public class Markup {

    protected HashMap<String, String> fieldNameToVariableName = null;

    public Markup() {
    }

    public Markup copy() {
        Markup copy = new Markup();
        if (fieldNameToVariableName != null) {
            copy.fieldNameToVariableName = (HashMap<String, String>)fieldNameToVariableName.clone();
        }
        return copy;
    }

    public void addVariable(String fieldName, String variableName) {
        if (fieldNameToVariableName == null) {
            fieldNameToVariableName = new HashMap<String, String>();
        }
        fieldNameToVariableName.put(fieldName, variableName);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Markup) {
            boolean ret = o.getClass().getName().equalsIgnoreCase(this.getClass().getName());

            return ret;
        }
        return super.equals(o);
    }

    @Override
    public String toString() {
        return this.getClass().getName();
    }
}
