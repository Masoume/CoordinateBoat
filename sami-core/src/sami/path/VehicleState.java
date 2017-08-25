/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sami.path;

import sami.path.VehicleModel.MotionPrimitive;

/**
 *
 * @author pscerri
 */
public class VehicleState {

    public VehicleState(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public double getVx() {
        return vx;
    }

    public double getVy() {
        return vy;
    }

    public double getVz() {
        return vz;
    }
    
    
    double x;
    double y;
    double z;
    double vx = 0.0;
    double vy = 0.0;
    double vz = 0.0;
    
    
    public double distTo(VehicleState v) {
        return Math.sqrt(
                ((v.x - x) * (v.x - x)) + 
                ((v.y - y) * (v.y - y)) + 
                ((v.z - z) * (v.z - z)) 
                );
    }
    
    public VehicleState propagate(MotionPrimitive p) {
        return new VehicleState(x + p.dx, y + p.dy, z + p.dz);
    }
    
    public String toString() {
        return "V:[" + x + ", " + y + ", " + z + "]";
    }
}
