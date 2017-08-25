package sami.uilanguage.fromui;

import java.util.UUID;

/**
 * Super class of all messages to UI
 *
 * @author pscerri
 */
public abstract class FromUiMessage {

    final protected UUID relevantToUiMessageId;
    final protected UUID relevantOutputEventId;
    final protected UUID missionId;

    public FromUiMessage(UUID relevantToUiMessageId, UUID relevantOutputEventId, UUID missionId) {
        this.relevantToUiMessageId = relevantToUiMessageId;
        this.relevantOutputEventId = relevantOutputEventId;
        this.missionId = missionId;
    }

    public UUID getMissionId() {
        return missionId;
    }

    public UUID getRelevantOutputEventId() {
        return relevantOutputEventId;
    }
}
