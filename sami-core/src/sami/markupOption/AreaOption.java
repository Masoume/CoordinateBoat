package sami.markupOption;

import java.util.ArrayList;
import java.util.HashMap;
import sami.area.Area2D;
import sami.markup.MarkupOption;

/**
 *
 * @author nbb
 */
public class AreaOption extends MarkupOption {

    // List of enum fields for which an enum option should be selected
    public static final ArrayList<String> fieldNames = new ArrayList<String>();
    // Description for each enum field
    public static final HashMap<String, String> fieldNameToDescription = new HashMap<String, String>();
    // Fields
    public Area2D area;

    static {
        fieldNames.add("area");

        fieldNameToDescription.put("area", "Area");
    }

    public AreaOption() {
    }
}
