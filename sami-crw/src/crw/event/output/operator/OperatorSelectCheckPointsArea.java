/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package crw.event.output.operator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import sami.area.Area2D;
import sami.event.OutputEvent;

/**
 *
 * @author Masoume
 */

public class OperatorSelectCheckPointsArea extends OutputEvent{
    // List of fields for which a definition should be provided
    public static final ArrayList<String> fieldNames = new ArrayList<String>();
    // Description for each field
    public static final HashMap<String, String> fieldNameToDescription = new HashMap<String, String>();
    // Fields
    public int noOfCheckPoints;

    static {
        fieldNames.add("noOfCheckPoints");

        fieldNameToDescription.put("noOfCheckPoints", "Number of check points");
    }
    
    public OperatorSelectCheckPointsArea(){
        id = UUID.randomUUID();
    }
    
    public OperatorSelectCheckPointsArea(UUID uuid, UUID missionUuid,int noOfCheckPoints) {
        this.id = uuid;
        this.missionId = missionUuid;
        this.noOfCheckPoints = noOfCheckPoints;
    }
    
    public int getNoOfCheckPoints() {
        return noOfCheckPoints;
    }

    public void setNoOfCheckPoints(int noOfCheckPoints) {
        this.noOfCheckPoints = noOfCheckPoints;
    }

    public String toString() {
        return "OperatorSelectCheckPointsArea [" + noOfCheckPoints + "]";
    }
}
