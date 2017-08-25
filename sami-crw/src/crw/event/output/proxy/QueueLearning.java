/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package crw.event.output.proxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import sami.event.OutputEvent;

/**
 *
 * @author Masoume
 */
public class QueueLearning extends OutputEvent{
    
      // List of fields for which a definition should be provided
    public static final ArrayList<String> fieldNames = new ArrayList<String>();
    // Description for each field
    public static final HashMap<String, String> fieldNameToDescription = new HashMap<String, String>();
//    // Fields
    
    public QueueLearning() {
        id = UUID.randomUUID();
    }

    public QueueLearning(UUID uuid, UUID missionUuid) {
        this.id = uuid;
        this.missionId = missionUuid;
    }

    public String toString() {
        return "QueueLearning";
    }
}
