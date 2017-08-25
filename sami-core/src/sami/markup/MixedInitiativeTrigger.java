package sami.markup;

import java.util.ArrayList;
import java.util.HashMap;
import sami.markupOption.TimeoutOption;

/**
 *
 * @author nbb
 */
public class MixedInitiativeTrigger extends Markup {

    // List of enum fields for which an enum option should be selected
    public static final ArrayList<String> enumFieldNames = new ArrayList<String>();
    // Description for each enum field
    public static final HashMap<String, String> enumNameToDescription = new HashMap<String, String>();
    // Mapping from enum value to the MarkupOption field it requires
    public static final HashMap<Enum, String> enumValueToFieldName = new HashMap<Enum, String>();
    // Fields
    public Trigger trigger;
    public TimeoutOption timeout;

    public enum Trigger {

        NEVER, TIMEOUT, IMMEDIATELY
    };

    static {
        enumFieldNames.add("trigger");

        enumNameToDescription.put("trigger", "When to delegate decision to autonomy?");

        enumValueToFieldName.put(Trigger.IMMEDIATELY, null);
        enumValueToFieldName.put(Trigger.NEVER, null);
        enumValueToFieldName.put(Trigger.TIMEOUT, "timeout");
    }

    public MixedInitiativeTrigger() {
    }
}
