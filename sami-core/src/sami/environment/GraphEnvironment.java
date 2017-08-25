/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sami.environment;

import sami.path.Path;
import sami.path.Waypoint2D;
import java.util.ArrayList;

/**
 *
 * @author pscerri
 */
public class GraphEnvironment extends EnvironmentModel {
    
    private final Waypoint2D [] waypoints;
    private final double[][] connections;
    private final double[][] crowsFlightDistances;

    /**
     * Values in the matrix represent costs between locations
     * 
     * @param connections Costs between locations, 0.0 represents no connection
     */
    public GraphEnvironment(double [][] connections, double [][] crowsFlightDistances, Waypoint2D [] waypoints) {
        this.connections = connections;
        this.crowsFlightDistances = crowsFlightDistances;
        this.waypoints = waypoints;
    }

    public double getCrowsFly(int loc, int loc0) {
        return crowsFlightDistances[loc][loc0];
    }

    public ArrayList<Integer> getDests(int loc) {
        ArrayList<Integer> dests = new ArrayList<Integer>();
        
        for (int i = 0; i < connections.length; i++) {
            if (connections[i][loc] > 0)
                dests.add(i);            
        }
        
        return dests;
    }
    
    public double getCost(int l0, int l1) {
        return connections[l0][l1];
    }

    @Override
    public Waypoint2D locToWP2D(int loc) {
        return waypoints[loc];
    }

    @Override
    public EnvironmentModel penalizePath(Path p) {
        // @todo penalizePath implementation
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    
}

