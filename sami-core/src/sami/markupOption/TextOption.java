package sami.markupOption;

import java.util.ArrayList;
import java.util.HashMap;
import sami.markup.MarkupOption;

/**
 *
 * @author nbb
 */
public class TextOption extends MarkupOption {

    // List of enum fields for which an enum option should be selected
    public static final ArrayList<String> fieldNames = new ArrayList<String>();
    // Description for each enum field
    public static final HashMap<String, String> fieldNameToDescription = new HashMap<String, String>();
    // Fields
    public String text;

    static {
        fieldNames.add("text");

        fieldNameToDescription.put("text", "Text");
    }

    public TextOption() {
    }
}
