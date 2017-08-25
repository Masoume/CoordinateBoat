/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package crw.event.input.operator;

import crw.Conversion;
import crw.proxy.BoatProxy;
import gov.nasa.worldwind.geom.Position;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.UUID;
import sami.area.Area2D;
import sami.event.InputEvent;
import sami.path.Location;
import sami.proxy.ProxyInt;

/**
 *
 * @author Masoume
 */
public class OperatorSelectsDangerousArea extends InputEvent{
    
    
    // List of fields for which a definition should be provided
    public static final ArrayList<String> fieldNames = new ArrayList<String>();
    // Description for each field
    public static final HashMap<String, String> fieldNameToDescription = new HashMap<String, String>();
    // List of fields for which a variable name should be provided
    public static final ArrayList<String> variableNames = new ArrayList<String>();
    // Description for each variable
    public static final HashMap<String, String> variableNameToDescription = new HashMap<String, String>();

    // Variables
    //public Hashtable<ProxyInt, Location> proxyPoints = null;
      
      
     public Area2D area;
  //  public ArrayList<Location> positions;
    
    //ArrayList<BoatProxy> boatProxyList = new ArrayList<BoatProxy>();
    
    static {
        variableNames.add("area");

        variableNameToDescription.put("area", "Dangerous Area Selected");
    }
       
    public OperatorSelectsDangerousArea(){    
       id = UUID.randomUUID();
    }
    /*public OperatorSelectsDangerousArea(UUID relevantOutputEventUuid, UUID missionUuid,ArrayList<Location> positions) {
        this.relevantOutputEventId = relevantOutputEventUuid;
        this.missionId = missionUuid;
       // this.area = area;
      /*  for (Location location : area.getPoints()){
          // positions.add(Conversion.locationToPosition(location));
            positions.add(location);
        }
        this.positions = positions;
        id = UUID.randomUUID();
    }
*/
    
    public OperatorSelectsDangerousArea(UUID relevantOutputEventUuid, UUID missionUuid,ArrayList<BoatProxy> boatProxyList,Area2D area) {
        this.relevantOutputEventId = relevantOutputEventUuid;
        this.missionId = missionUuid;
        id = UUID.randomUUID();  
        this.area = area;
        
        if (boatProxyList != null) {
            relevantProxyList = new ArrayList<ProxyInt>();
            for (BoatProxy boatProxy : boatProxyList) {
                relevantProxyList.add(boatProxy);
            }
        } else {
            relevantProxyList = null;
        }
    }
    
    
 //   public Area2D getArea() {
//        return area;
 //   }
    
 //   public ArrayList<Location> getPositions() {
 //       return positions;
 //   }
    

}
