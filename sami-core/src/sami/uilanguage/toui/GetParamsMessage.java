package sami.uilanguage.toui;

import sami.event.ReflectedEventSpecification;
import java.lang.reflect.Field;
import java.util.Hashtable;
import java.util.UUID;

/**
 *
 * @author pscerri
 */
public class GetParamsMessage extends CreationMessage {

    public GetParamsMessage(UUID relevantOutputEventId, UUID missionId, int priority, Hashtable<ReflectedEventSpecification, Hashtable<Field, String>> eventSpecToFieldDescriptions) {
        super(relevantOutputEventId, missionId, priority, eventSpecToFieldDescriptions);
    }
    
    public String toString() {
        return "GetParamsMessage [" + eventSpecToFieldDescriptions + "]";
    }
}
