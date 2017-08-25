package sami.markup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 *
 * @author nbb
 */
public class ProxyStatus extends Markup {

    // List of enum fields for which an enum option should be selected
    public static final ArrayList<String> enumFieldNames = new ArrayList<String>();
    // Description for each enum field
    public static final HashMap<String, String> enumNameToDescription = new HashMap<String, String>();
    // Mapping from enum value to the MarkupOption field it requires
    public static final HashMap<Enum, String> enumValueToFieldName = new HashMap<Enum, String>();
    // Fields
    public Status proxyStatus;

    public enum Status {

        NOMINAL, WARNING, SEVERE
    };
    public static final Map<Status, Integer> statusToInt;
    public static final Map<Integer, Status> intToStatus;

    static {
        enumFieldNames.add("proxyStatus");

        enumNameToDescription.put("proxyStatus", "Proxy status?");

        enumValueToFieldName.put(Status.NOMINAL, null);
        enumValueToFieldName.put(Status.WARNING, null);
        enumValueToFieldName.put(Status.SEVERE, null);

        statusToInt = new Hashtable<Status, Integer>();
        statusToInt.put(Status.NOMINAL, new Integer(1));
        statusToInt.put(Status.WARNING, new Integer(2));
        statusToInt.put(Status.SEVERE, new Integer(3));
        intToStatus = new Hashtable<Integer, Status>();
        intToStatus.put(statusToInt.get(Status.NOMINAL), Status.NOMINAL);
        intToStatus.put(statusToInt.get(Status.WARNING), Status.WARNING);
        intToStatus.put(statusToInt.get(Status.SEVERE), Status.SEVERE);
    }

    public ProxyStatus() {
    }

    public ProxyStatus(Status status) {
        this.proxyStatus = status;
    }

    public static int getProxyStatus(ProxyStatus priority) {
        return statusToInt.get(priority).intValue();
    }

    public static Status getProxyStatus(int priority) {
        return intToStatus.get(new Integer(priority));
    }
}
