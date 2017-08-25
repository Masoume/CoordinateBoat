package sami.markup;

import java.util.ArrayList;
import java.util.HashMap;
import sami.markupOption.BlinkOption;

/**
 *
 * @author nbb
 */
public class Attention extends Markup {

    // List of enum fields for which an enum option should be selected
    public static final ArrayList<String> enumFieldNames = new ArrayList<String>();
    // Description for each enum field
    public static final HashMap<String, String> enumNameToDescription = new HashMap<String, String>();
    // Mapping from enum value to the MarkupOption field it requires
    public static final HashMap<Enum, String> enumValueToFieldName = new HashMap<Enum, String>();
    // Fields
    public AttentionType attentionType;
    public AttentionEnd attentionEnd;
    public AttentionTarget attentionTarget;
    public BlinkOption blink;

    public enum AttentionType {

        HIGHLIGHT, BLINK
    };

    public enum AttentionEnd {

        ON_CLICK
    };

    public enum AttentionTarget {

        PANEL, FRAME, ALL_PROXIES, RELEVANT_PROXIES
    };

    static {
        enumFieldNames.add("attentionType");
        enumFieldNames.add("attentionEnd");
        enumFieldNames.add("attentionTarget");

        enumNameToDescription.put("attentionType", "How to draw attention?");
        enumNameToDescription.put("attentionEnd", "When to stop drawing attention?");
        enumNameToDescription.put("attentionTarget", "What to draw attention to?");

        enumValueToFieldName.put(AttentionType.BLINK, "blink");
        enumValueToFieldName.put(AttentionType.HIGHLIGHT, null);
        enumValueToFieldName.put(AttentionEnd.ON_CLICK, null);
        enumValueToFieldName.put(AttentionTarget.ALL_PROXIES, null);
        enumValueToFieldName.put(AttentionTarget.FRAME, null);
        enumValueToFieldName.put(AttentionTarget.PANEL, null);
        enumValueToFieldName.put(AttentionTarget.RELEVANT_PROXIES, null);
    }

    public Attention() {
    }

    public String toString() {
        return "attentionType: " + attentionType
                + "attentionEnd " + attentionEnd
                + "attentionTarget " + attentionTarget
                + "blinkOption " + blink;

    }
}
