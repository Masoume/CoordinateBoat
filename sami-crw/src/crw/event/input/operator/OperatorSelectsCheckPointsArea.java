/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package crw.event.input.operator;

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
public class OperatorSelectsCheckPointsArea extends InputEvent{
    
    // List of fields for which a definition should be provided
    public static final ArrayList<String> fieldNames = new ArrayList<String>();
    // Description for each field
    public static final HashMap<String, String> fieldNameToDescription = new HashMap<String, String>();
    // List of fields for which a variable name should be provided
    public static final ArrayList<String> variableNames = new ArrayList<String>();
    // Description for each variable
    public static final HashMap<String, String> variableNameToDescription = new HashMap<String, String>();

    public int noOfCheckPoints;
    
    static {
        variableNames.add("noOfCheckPoints");

        variableNameToDescription.put("noOfCheckPoints", "Number of check points");
    }
       
    public OperatorSelectsCheckPointsArea(){    
       id = UUID.randomUUID();
    }
    
    public OperatorSelectsCheckPointsArea(UUID relevantOutputEventUuid, UUID missionUuid,ArrayList<BoatProxy> boatProxyList,int noOfCheckPoints) {
        this.relevantOutputEventId = relevantOutputEventUuid;
        this.missionId = missionUuid;
        id = UUID.randomUUID();  
        this.noOfCheckPoints = noOfCheckPoints;
        
        if (boatProxyList != null) {
            relevantProxyList = new ArrayList<ProxyInt>();
            for (BoatProxy boatProxy : boatProxyList) {
                relevantProxyList.add(boatProxy);
            }
        } else {
            relevantProxyList = null;
        }
    }
    
}
