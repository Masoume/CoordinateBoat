package sami.markup;

import java.util.ArrayList;
import java.util.HashMap;
import sami.markupOption.TextOption;

/**
 *
 * @author nbb
 */
public class Keyword extends Markup {

    // List of enum fields for which an enum option should be selected
    public static final ArrayList<String> enumFieldNames = new ArrayList<String>();
    // Description for each enum field
    public static final HashMap<String, String> enumNameToDescription = new HashMap<String, String>();
    // Mapping from enum value to the MarkupOption field it requires
    public static final HashMap<Enum, String> enumValueToFieldName = new HashMap<Enum, String>();
    // Fields
    public Text text;
    public TextOption textOption;

    public enum Text {

        SPECIFY
    };

    static {
        enumFieldNames.add("text");

        enumNameToDescription.put("text", "What is the keyword?");

        enumValueToFieldName.put(Text.SPECIFY, "textOption");
    }

    public Keyword() {
    }
}
