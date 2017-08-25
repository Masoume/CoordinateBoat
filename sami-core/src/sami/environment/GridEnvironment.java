/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sami.environment;

import sami.path.Path;
import sami.path.Waypoint2D;
import sami.path.Path2D;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 *
 * @author pscerri
 */
public class GridEnvironment extends EnvironmentModel {

    private final double[][] grid;

    public GridEnvironment(double[][] grid) {
        this.grid = grid;
    }

    public GraphEnvironment toGraph() {

        int locations = grid.length * grid[0].length;

        double[][] connections = new double[locations][locations];
        double[][] distances = new double[locations][locations];
        Waypoint2D[] waypoints = new Waypoint2D[locations];

        for (int i = 0; i < locations; i++) {
            for (int j = 0; j < locations; j++) {

                int x1 = locIDtoX(i);
                int y1 = locIDtoY(i);

                int x2 = locIDtoX(j);
                int y2 = locIDtoY(j);

                if ((Math.abs(x1 - x2) == 1 && y1 == y2) || (Math.abs(y1 - y2) == 1 && x1 == x2)) {
                    connections[i][j] = 1;
                    // Might need to think about whether this is the right cost (assumes cost is dest)
                    distances[i][j] = grid[x2][y2];
                } else {
                    connections[i][j] = 0;
                    distances[i][j] = 0;
                }
            }
            waypoints[i] = locToWP2D(i);
        }

        GraphEnvironment graph = new GraphEnvironment(connections, distances, waypoints);

        return graph;
    }

    public double getCrowsFly(int loc1, int loc2) {
        int x1 = locIDtoX(loc1);
        int y1 = locIDtoY(loc1);

        int x2 = locIDtoX(loc2);
        int y2 = locIDtoY(loc2);

        return Math.sqrt(((x1 - x2) * (x1 - x2)) + ((y1 - y2) * (y1 - y2)));
    }

    public double getCost(int loc1, int loc2) {
        int x1 = locIDtoX(loc2);
        int y1 = locIDtoY(loc2);
        
        return grid[x1][y1];
    }

    public java.util.ArrayList<Integer> getDests(int loc) {
        java.util.ArrayList<Integer> exp = new ArrayList<Integer>();
        int x = locIDtoX(loc);
        int y = locIDtoY(loc);

        for (int i = -1; i < 2; i += 2) {

            int nx = x + i;
            if (nx >= 0 && nx < grid.length) {
                exp.add((y * grid.length) + nx);
            }
            int ny = y + 1;
            if (ny >= 0 && ny < grid[0].length) {
                exp.add((ny * grid.length) + x);
            }

        }

        /* Debugging output
        System.out.print("Expansions from [" + x + ", " + y + "]");
        for (Integer integer : exp) {
            int dx = locIDtoX(integer);
            int dy = locIDtoY(integer);
            
            System.out.print("[" + dx + ", " + dy +"]");
        }
        System.out.println("");
        */
        
        return exp;
    }

    private int locIDtoX(int locID) {
        return locID % grid.length;
    }

    private int locIDtoY(int locID) {
        return (int) Math.floor(locID / grid.length);
    }

    public Waypoint2D locToWP2D(int loc) {
        return new Waypoint2D(locIDtoX(loc), locIDtoY(loc));
    }

    @Override
    public EnvironmentModel penalizePath(Path p) {

        double[][] gCopy = new double[grid.length][grid[0].length];

        for (int i = 0; i < gCopy.length; i++) {
            System.arraycopy(grid[i], 0, gCopy[i], 0, grid[i].length);
        }

        // Assume Waypoints2D path
        if (p instanceof Path2D) {
            Path2D wps = (Path2D) p;
            for (Waypoint2D waypoint2D : wps.getWps()) {
                gCopy[waypoint2D.x][waypoint2D.y] += 0.5;
            }

        } else {
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Unhandled environment type", this);
        }

        return new GridEnvironment(gCopy);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < grid.length; i++) {
            sb.append("[");
            for (int j = 0; j < grid[0].length; j++) {
                sb.append(grid[i][j] + " ");
            }
            sb.append("]\n");
        }

        return sb.toString();
    }
}
