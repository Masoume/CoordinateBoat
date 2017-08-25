package sami.uilanguage.toui;

import java.lang.reflect.Field;
import java.util.Hashtable;
import java.util.UUID;
import sami.event.ReflectedEventSpecification;

/**
 * @author nbb
 */
public abstract class CreationMessage extends ToUiMessage {

    protected final Hashtable<ReflectedEventSpecification, Hashtable<Field, String>> eventSpecToFieldDescriptions;

    public CreationMessage(UUID relevantOutputEventId, UUID missionId, int priority, Hashtable<ReflectedEventSpecification, Hashtable<Field, String>> eventSpecToFieldDescriptions) {
        super(relevantOutputEventId, missionId, priority);
        this.eventSpecToFieldDescriptions = eventSpecToFieldDescriptions;
    }

    public Hashtable<ReflectedEventSpecification, Hashtable<Field, String>> getEventSpecToFieldDescriptions() {
        return eventSpecToFieldDescriptions;
    }
    
    public String toString() {
        return "CreationMessage [" + eventSpecToFieldDescriptions + "]";
    }
}
