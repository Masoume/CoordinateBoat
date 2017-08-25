package sami.uilanguage.toui;

import java.util.ArrayList;
import java.util.UUID;
import sami.markup.Markup;

/**
 * Super class of all messages to UI
 *
 * @author pscerri
 */
public abstract class ToUiMessage {

    final protected int priority;
    final protected long creationTime;    // epoch time
    final protected ArrayList<Markup> markups = new ArrayList<Markup>();
    final protected UUID messageId;
    final protected UUID relevantOutputEventId;
    final protected UUID missionId;

    public ToUiMessage(UUID relevantOutputEventId, UUID missionId, int priority) {
        this.relevantOutputEventId = relevantOutputEventId;
        this.missionId = missionId;
        this.priority = priority;
        creationTime = System.currentTimeMillis();
        messageId = UUID.randomUUID();
    }

//    public ToUiMessage(UUID messageId, UUID relevantOutputEventId, UUID missionId, long creationTime, int priority) {
//        this.messageId = messageId;
//        this.relevantOutputEventId = relevantOutputEventId;
//        this.missionId = missionId;
//        this.creationTime = creationTime;
//        this.priority = priority;
//    }

    public UUID getMessageId() {
        return messageId;
    }

    public UUID getRelevantOutputEventId() {
        return relevantOutputEventId;
    }
    
    public UUID getMissionId() {
        return missionId;
    }

    public void addMarkup(Markup markup) {
        markups.add(markup);
    }

    public ArrayList<Markup> getMarkups() {
        return markups;
    }

    public int getPriority() {
        return priority;
    }

    public long getCreationTime() {
        return creationTime;
    }
}
