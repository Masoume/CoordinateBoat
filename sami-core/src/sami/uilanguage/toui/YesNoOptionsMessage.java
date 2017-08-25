package sami.uilanguage.toui;

import java.util.ArrayList;
import java.util.UUID;

/**
 *
 * @author nbb
 */
public class YesNoOptionsMessage extends SelectionMessage {

    public YesNoOptionsMessage(UUID relevantOutputEventId, UUID missionId, int priority) {
        super(relevantOutputEventId, missionId, priority, false, false, true);
        ArrayList<String> temp = new ArrayList<String>();
        temp.add("Yes");
        temp.add("No");
        optionsList = temp;
    }
    
    public String toString() {
        return "YesNoOptionsMessage";
    }
}
