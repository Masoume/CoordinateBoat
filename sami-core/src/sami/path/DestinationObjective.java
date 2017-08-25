/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sami.path;

/**
 * x,y and loc are alternatives, but the system doesn't really understand that.
 * 
 * @todo Allow final and initial pose to be specified
 * 
 * @author pscerri
 */
public class DestinationObjective extends ObjectiveFunction {
    private int endX = 0;
    private int endY = 0;
    private int endZ = 0;

    private int startX = 0;
    private int startY = 0;
    private int startZ = 0;
    
    private int endLoc = 0;    
    private int startLoc = 0;
    
    /**
     * Works with either a graph or grid
     * 
     * @param xDest
     * @param yDest 
     */
    public DestinationObjective(int xOrig, int yOrig, int xDest, int yDest) {
        this.endX = xDest;
        this.endY = yDest;
        this.startX = xOrig;
        this.startY = yOrig;
    }
    
    public DestinationObjective(int startLoc, int destLoc) {
        this.endLoc = destLoc;        
        this.startLoc = startLoc;        
    }

    /**
     * Try to avoid using, just here for instantiation.
     */
    public DestinationObjective() {
    }        

    public int getEndX() {
        return endX;
    }

    public int getEndY() {
        return endY;
    }

    public int getStartX() {
        return startX;
    }

    public int getStartY() {
        return startY;
    }

    public int getEndLoc() {
        return endLoc;
    }

    public int getStartLoc() {
        return startLoc;
    }

    public int getEndZ() {
        return endZ;
    }

    public void setEndZ(int endZ) {
        this.endZ = endZ;
    }

    public int getStartZ() {
        return startZ;
    }

    public void setStartZ(int startZ) {
        this.startZ = startZ;
    }
    
    
}
