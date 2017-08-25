/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package crw.uilanguage.message.fromui;

import crw.Conversion;
import gov.nasa.worldwind.geom.Position;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;
import sami.area.Area2D;
import sami.path.Location;
import sami.uilanguage.fromui.FromUiMessage;

/**
 *
 * @author Masoume
 */
public class AreaSelectedMessage extends FromUiMessage{

    Area2D area;
    ArrayList<Location> positions;
    
  /*  public AreaSelectedMessage(UUID relevantToUiMessageId, UUID relevantOutputEventId, UUID missionId, Area2D area) {
        super(relevantToUiMessageId, relevantOutputEventId, missionId);
        
        this.area = area;              
    }*/
    public AreaSelectedMessage(UUID relevantToUiMessageId, UUID relevantOutputEventId, UUID missionId, ArrayList<Location> positions) {
        super(relevantToUiMessageId, relevantOutputEventId, missionId);
        
        this.positions = positions;
       /* for (Location location : area.getPoints()){
           positions.add(Conversion.locationToPosition(location));
        }*/
        
    }
    
    public Area2D getArea(){
        
        return area;
    }
    
    public ArrayList<Location> getPositions()
    {
        return positions;
    }
    
    @Override
    public UUID getRelevantOutputEventId() {
        return relevantOutputEventId;
    }   
}
