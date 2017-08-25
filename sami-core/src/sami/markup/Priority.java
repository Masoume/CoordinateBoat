package sami.markup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 *
 * @author nbb
 */
public class Priority extends Markup {

    // List of enum fields for which an enum option should be selected
    public static final ArrayList<String> enumFieldNames = new ArrayList<String>();
    // Description for each enum field
    public static final HashMap<String, String> enumNameToDescription = new HashMap<String, String>();
    // Mapping from enum value to the MarkupOption field it requires
    public static final HashMap<Enum, String> enumValueToFieldName = new HashMap<Enum, String>();
    // Fields
    public Ranking ranking;

    public enum Ranking {

        LOW, MEDIUM, HIGH, CRITICAL
    };
    public static final Map<Ranking, Integer> priorityToInt;
    public static final Map<Integer, Ranking> intToPriority;

    static {
        enumFieldNames.add("ranking");

        enumNameToDescription.put("ranking", "Priority ranking?");

        enumValueToFieldName.put(Ranking.CRITICAL, null);
        enumValueToFieldName.put(Ranking.HIGH, null);
        enumValueToFieldName.put(Ranking.LOW, null);
        enumValueToFieldName.put(Ranking.MEDIUM, null);

        priorityToInt = new Hashtable<Ranking, Integer>();
        priorityToInt.put(Ranking.LOW, new Integer(1));
        priorityToInt.put(Ranking.MEDIUM, new Integer(2));
        priorityToInt.put(Ranking.HIGH, new Integer(3));
        priorityToInt.put(Ranking.CRITICAL, new Integer(4));
        intToPriority = new Hashtable<Integer, Ranking>();
        intToPriority.put(priorityToInt.get(Ranking.LOW), Ranking.LOW);
        intToPriority.put(priorityToInt.get(Ranking.MEDIUM), Ranking.MEDIUM);
        intToPriority.put(priorityToInt.get(Ranking.HIGH), Ranking.HIGH);
        intToPriority.put(priorityToInt.get(Ranking.CRITICAL), Ranking.CRITICAL);
    }

    public Priority() {
    }

    public Priority(Ranking ranking) {
        this.ranking = ranking;
    }

    public static int getPriority(Ranking priority) {
        return priorityToInt.get(priority).intValue();
    }

    public static Ranking getPriority(int priority) {
        return intToPriority.get(new Integer(priority));
    }
}
