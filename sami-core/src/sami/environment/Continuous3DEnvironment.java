/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sami.environment;

import sami.path.Path;
import sami.path.Waypoint2D;
import java.util.ArrayList;

/**
 * Dummy, needs to be implemented
 * 
 * @author pscerri
 */
public class Continuous3DEnvironment extends EnvironmentModel {

    double width, length, height;

    public Continuous3DEnvironment(double width, double length, double height) {
        this.width = width;
        this.length = length;
        this.height = height;
    }
            
    @Override
    public double getCrowsFly(int loc, int loc0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ArrayList<Integer> getDests(int loc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double getCost(int l0, int l1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Waypoint2D locToWP2D(int loc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public EnvironmentModel penalizePath(Path p) {
        // @todo Implement penalize path
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public double getCost(double x1, double y1, double z1, double x2, double y2, double z2) {
        // Dummy implementation
        return x2/15.0 + Math.sqrt(((x1-x2)*(x1-x2)) + ((y1-y2)*(y1-y2)) + 5.0*((z1-z2)*(z1-z2)));
    }
    
    public double getPointCost(double x1, double y1, double z1) {
        return 0.0;
    }

    public double getWidth() {
        return width;
    }

    public double getLength() {
        return length;
    }

    public double getHeight() {
        return height;
    }
    
    public Waypoint2D locToWP2D(double x, double y) {
        // @todo This is not correct
        return new Waypoint2D((int)x, (int)y);
    }
}

