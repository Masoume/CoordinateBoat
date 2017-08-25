package sami.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 *
 * @author nbb
 */
public class StartTimer extends OutputEvent {

    // List of fields for which a definition should be provided
    public static final ArrayList<String> fieldNames = new ArrayList<String>();
    // Description for each field
    public static final HashMap<String, String> fieldNameToDescription = new HashMap<String, String>();
    // Fields
    public int timerDuration;
    
    static {
        fieldNames.add("timerDuration");

        fieldNameToDescription.put("timerDuration", "Timer duration? (s)");
    }
    
    public StartTimer() {
        id = UUID.randomUUID();
    }

    public StartTimer(UUID missionId) {
        this.missionId = missionId;
        id = UUID.randomUUID();
    }

    public String toString() {
        return "StartTimer [" + timerDuration + "]";
    }
}
