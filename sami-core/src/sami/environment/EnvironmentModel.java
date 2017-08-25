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
public abstract class EnvironmentModel {

    public abstract double getCrowsFly(int loc, int loc0);

    public abstract ArrayList<Integer> getDests(int loc);
    
    public abstract double getCost(int l0, int l1);
    
    public abstract Waypoint2D locToWP2D(int loc);
    
    public abstract EnvironmentModel penalizePath(Path p);
}
