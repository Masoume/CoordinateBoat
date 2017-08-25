package sami.path;

import java.util.ArrayList;

/**
 *
 * @author pscerri
 */
public class Path2D extends Path {
 
    protected ArrayList<Waypoint2D> wps = new ArrayList<Waypoint2D>();

    public Path2D() {
    }

    public Path2D(ArrayList<Waypoint2D> wps) {
        this.wps = wps;
    }

    public ArrayList<Waypoint2D> getWps() {
        return wps;
    }

    public void setWps(ArrayList<Waypoint2D> wps) {
        this.wps = wps;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("[");
        for (Waypoint2D waypoint2D : wps) {
            sb.append(waypoint2D);
            sb.append(",");
        }
        sb.deleteCharAt(sb.length()-1);
        sb.append("]");
        
        return sb.toString();
    }

    @Override
    public double getCost() {
        return cost;
    }

    @Override
    public void setCost(double cost) {
        this.cost = cost;
    }
}
