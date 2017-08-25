/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package crw.event.output.proxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import sami.area.Area2D;
import sami.event.OutputEvent;

/**
 *
 * @author Masoume
 */
public class ProxySelectDangerousArea extends OutputEvent{
    
// List of fields for which a definition should be provided
    public static final ArrayList<String> fieldNames = new ArrayList<String>();
    // Description for each field
    public static final HashMap<String, String> fieldNameToDescription = new HashMap<String, String>();
    // Fields
    public Area2D area;
   // LinkedList<Location> positions;
    //public ArrayList<Position> positions;
    
    static {
        fieldNames.add("area");
        fieldNameToDescription.put("area", "Select Dangerous Area?");
    }
          
    /*for (Location location : area.getPoints()) {
        positions.add(Conversion.locationToPosition(location));
    }
    */
    
    public ProxySelectDangerousArea()
    {
        id= UUID.randomUUID();
    }
    
    public ProxySelectDangerousArea(UUID uuid, UUID missionUuid,Area2D area) {
        this.id = uuid;
        this.missionId = missionUuid;
        this.area = area;
        /*for (Location location : area.getPoints()) {
            this.positions.add(location);
        }*/
    }

    public Area2D getArea() {
        return area;
    }

    public void setArea(Area2D area) {
        this.area = area;
    }

    public String toString() {
        return "OperatorSelectDangerousArea [" + area + "]";
    }
    
}
