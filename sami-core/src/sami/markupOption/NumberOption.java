package sami.markupOption;

import java.util.ArrayList;
import java.util.HashMap;
import sami.markup.MarkupOption;

/**
 *
 * @author nbb
 */
public class NumberOption extends MarkupOption {

    // List of enum fields for which an enum option should be selected
    public static final ArrayList<String> fieldNames = new ArrayList<String>();
    // Description for each enum field
    public static final HashMap<String, String> fieldNameToDescription = new HashMap<String, String>();
    // Fields
    public int number;

    static {
        fieldNames.add("number");

        fieldNameToDescription.put("number", "Number");
    }

    public NumberOption() {
    }
}
