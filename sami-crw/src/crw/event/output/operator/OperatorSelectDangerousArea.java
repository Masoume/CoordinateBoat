/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package crw.event.output.operator;

import crw.Conversion;
import gov.nasa.worldwind.geom.Position;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;
import sami.area.Area2D;
import sami.event.OutputEvent;
import sami.path.Location;

/**
 *
 * @author Masoume
 */
public class OperatorSelectDangerousArea extends OutputEvent {
    
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
    
    public OperatorSelectDangerousArea()
    {
        id = UUID.randomUUID();
    }
    
    public OperatorSelectDangerousArea(UUID uuid, UUID missionUuid,Area2D area) {
        this.id = uuid;
        this.missionId = missionUuid;
        this.area = area;
        /*for (Location location : area.getPoints()) {
            this.positions.add(location);
        }*/
    }

    /*public OperatorSelectDangerousArea(UUID uuid, UUID missionUuid,ArrayList<Position> positions) {
        this.id = uuid;
        this.missionId = missionUuid;
        this.area = area;
        this.positions = positions;
    }
    */
    /*public ArrayList<Position> getPos(){
        return positions;
    }
      */  
   /* public LinkedList<Location> getPos(){
        return positions;
    }
    public void setPos(LinkedList<Location> positions) {
        this.positions = positions;
    }*/
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
