/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package crw.uilanguage.message.toui;

import gov.nasa.worldwind.geom.Position;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import sami.path.Location;
import sami.uilanguage.toui.SelectionMessage;

/**
 *
 * @author Masoume
 */
    
public class AreaSelectionMessage extends SelectionMessage {
 
    public AreaSelectionMessage(UUID relevantOutputEventId, UUID missionId, int priority, List<?> optionsList) {
        super(relevantOutputEventId, missionId, priority, false, false,true, optionsList);
    }
        
 }
    

