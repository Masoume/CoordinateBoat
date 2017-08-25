/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package crw.event.input.service;

import static crw.event.input.operator.OperatorSelectsDangerousArea.variableNameToDescription;
import static crw.event.input.operator.OperatorSelectsDangerousArea.variableNames;
import crw.proxy.BoatProxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import sami.area.Area2D;
import sami.event.InputEvent;
import sami.proxy.ProxyInt;


/**
 *
 * @author Masoume
 */
public class BoatOutsideArea extends InputEvent{
    
    // List of fields for which a definition should be provided
    public static final ArrayList<String> fieldNames = new ArrayList<String>();
    // Description for each field
    public static final HashMap<String, String> fieldNameToDescription = new HashMap<String, String>();
    // List of fields for which a variable name should be provided
    public static final ArrayList<String> variableNames = new ArrayList<String>();
    // Description for each variable
    public static final HashMap<String, String> variableNameToDescription = new HashMap<String, String>();

    public Area2D area;
  
    static {
        variableNames.add("area");

        variableNameToDescription.put("area", "Dangerous Area Selected");
    }
    
    public BoatOutsideArea() {   
        id = UUID.randomUUID();
    }

 //   public BoatOutsideArea(UUID relevantOutputEventUuid, UUID missionUuid, ArrayList<BoatProxy> boatProxyList) {
   public BoatOutsideArea(ArrayList<BoatProxy> boatProxyList, Area2D area) {

 //   this.relevantOutputEventId = relevantOutputEventUuid;
  //  this.missionId = missionUuid;
       this.area = area;
       
       if (boatProxyList != null) {
            relevantProxyList = new ArrayList<ProxyInt>();
            for (BoatProxy boatProxy : boatProxyList) {
                relevantProxyList.add(boatProxy);
            }
        } else {
            relevantProxyList = null;
        }
       
        id = UUID.randomUUID();
    }
    
}
