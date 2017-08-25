package sami.markupOption;

import java.util.ArrayList;
import java.util.HashMap;
import sami.markup.MarkupOption;

/**
 *
 * @author nbb
 */
public class TimeoutOption extends MarkupOption {

    // List of enum fields for which an enum option should be selected
    public static final ArrayList<String> fieldNames = new ArrayList<String>();
    // Description for each enum field
    public static final HashMap<String, String> fieldNameToDescription = new HashMap<String, String>();
    // Fields
    public int timeout = 30; // s

    static {
        fieldNames.add("timeout");

        fieldNameToDescription.put("timeout", "Timeout duration (s)");
    }

    public TimeoutOption() {
    }
}
