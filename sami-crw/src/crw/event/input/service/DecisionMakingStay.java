/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package crw.event.input.service;

import crw.proxy.BoatProxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import sami.event.InputEvent;
import sami.proxy.ProxyInt;

/**
 *
 * @author Masoume
 */
public class DecisionMakingStay extends InputEvent{
    // List of fields for which a definition should be provided
    public static final ArrayList<String> fieldNames = new ArrayList<String>();
    // Description for each field
    public static final HashMap<String, String> fieldNameToDescription = new HashMap<String, String>();
    // List of fields for which a variable name should be provided
    public static final ArrayList<String> variableNames = new ArrayList<String>();
    // Description for each variable
    public static final HashMap<String, String> variableNameToDescription = new HashMap<String, String>();

    public DecisionMakingStay() {   
        id = UUID.randomUUID();
    }
    
//    public DecisionMakingStay(ArrayList<BoatProxy> boatProxyList) {
//
//       if (boatProxyList != null) {
//            relevantProxyList = new ArrayList<ProxyInt>();
//            for (BoatProxy boatProxy : boatProxyList) {
//                relevantProxyList.add(boatProxy);
//            }
//        } else {
//            relevantProxyList = null;
//        }
//       
//        id = UUID.randomUUID();
//    }
    
    
    public DecisionMakingStay(UUID relevantOutputEventUuid, UUID missionUuid,ArrayList<BoatProxy> boatProxyList) {
        this.relevantOutputEventId = relevantOutputEventUuid;
        this.missionId = missionUuid;
        id = UUID.randomUUID();  
        
        if (boatProxyList != null) {
            relevantProxyList = new ArrayList<ProxyInt>();
            for (BoatProxy boatProxy : boatProxyList) {
                relevantProxyList.add(boatProxy);
                
                System.out.print("Stayyyyyy");
            }
        } else {
            relevantProxyList = null;
        }

    }
}
