package sami.uilanguage.fromui;

import java.util.UUID;

/**
 *
 * @author nbb
 */
public class YesNoSelectedMessage extends FromUiMessage {

    private boolean yes;

    public YesNoSelectedMessage(UUID relevantToUiMessageId, UUID relevantOutputEventId, UUID missionId, boolean yes) {
        super(relevantToUiMessageId, relevantOutputEventId, missionId);
        this.yes = yes;
    }

    public boolean getYes() {
        return yes;
    }

    public void setYes(boolean yes) {
        this.yes = yes;
    }
    
    public String toString() {
        return "YesNoSelectedMessage [" + yes + "]";
    }
}
