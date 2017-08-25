package sami.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 *
 * @author nbb
 */
public class SendSoftRestartMission extends OutputEvent {

    // List of fields for which a definition should be provided
    public static final ArrayList<String> fieldNames = new ArrayList<String>();
    // Description for each field
    public static final HashMap<String, String> fieldNameToDescription = new HashMap<String, String>();

    public SendSoftRestartMission() {
        id = UUID.randomUUID();
    }

    public SendSoftRestartMission(UUID missionId) {
        this.missionId = missionId;
        id = UUID.randomUUID();
    }

    public String toString() {
        return "SendSoftRestartMission [" + missionId + "]";
    }
}
