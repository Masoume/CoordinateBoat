package sami.markupOption;

import java.util.ArrayList;
import java.util.HashMap;
import sami.markup.MarkupOption;

/**
 *
 * @author nbb
 */
public class BlinkOption extends MarkupOption {

    // List of enum fields for which an enum option should be selected
    public static final ArrayList<String> fieldNames = new ArrayList<String>();
    // Description for each enum field
    public static final HashMap<String, String> fieldNameToDescription = new HashMap<String, String>();
    // Fields
    public int cycleLength = 2000; // ms

    static {
        fieldNames.add("cycleLength");

        fieldNameToDescription.put("cycleLength", "Length of cycle (ms)");
    }

    public BlinkOption() {
    }
    
    public String toString() {
        return "cycleLength: " + cycleLength;
    }
}
