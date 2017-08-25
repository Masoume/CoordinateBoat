/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sami.environment;

/**
 *
 * @author pscerri
 */
public class RectangularObstacle {

    final double x1;
    final double x2;
    final double y1;
    final double y2;
    final double z1;
    final double z2;
    final double cost;

    public RectangularObstacle(double x1, double y1, double z1, double x2, double y2, double z2, double cost) {

        if (x1 <= x2) {
            this.x1 = x1;
            this.x2 = x2;
        } else {
            this.x1 = x2;
            this.x2 = x1;
        }
        if (y1 <= y2) {
            this.y1 = y1;
            this.y2 = y2;
        } else {
            this.y1 = y2;
            this.y2 = y1;
        }
        if (z1 <= z2) {
            this.z1 = z1;
            this.z2 = z2;
        } else {
            this.z1 = z2;
            this.z2 = z1;
        }
        this.cost = cost;
    }

    public double getX1() {
        return x1;
    }

    public double getX2() {
        return x2;
    }

    public double getY1() {
        return y1;
    }

    public double getY2() {
        return y2;
    }

    public double getZ1() {
        return z1;
    }

    public double getZ2() {
        return z2;
    }

    public double getCost() {
        return cost;
    }
    
    public double getCenterX() {
        return (x2 + x1)/2.0;
    }
    
    public double getCenterY() {
        return (y2 + y1)/2.0;
    }
    
    public double getCenterZ() {
        return (z2 + z1)/2.0;
    }
}
