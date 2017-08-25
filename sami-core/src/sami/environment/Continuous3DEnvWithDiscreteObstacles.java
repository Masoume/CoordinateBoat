/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sami.environment;

import java.util.ArrayList;

/**
 *
 * @author pscerri
 */
public class Continuous3DEnvWithDiscreteObstacles extends Continuous3DEnvironment {

    private ArrayList<RectangularObstacle> rects = new ArrayList<RectangularObstacle>();

    public Continuous3DEnvWithDiscreteObstacles(double width, double length, double height) {
        super(width, length, height);
    }

    public void addRect(RectangularObstacle r) {
        rects.add(r);
    }

    public ArrayList<RectangularObstacle> getObstacles() {
        return rects;
    }

    
    @Override
    // @todo Cost should depend on how much the path interacts with the obstacle?
    public double getCost(double tx1, double ty1, double tz1, double tx2, double ty2, double tz2) {

        double c = 0.0;

        for (RectangularObstacle r : rects) {

            if (in(r, tx1, ty1, tz1, tx2, ty2, tz2)) {
                // System.out.println("Cost!");
                c += r.cost;
            }
        }

        return c;
    }

    @Override
    public double getPointCost(double x1, double y1, double z1) {
        return getCost(x1, y1, z1, x1, y1, z1);
    }

    private boolean in(RectangularObstacle r, double tx1, double ty1, double tz1, double tx2, double ty2, double tz2) {
        // If either of the ends of the line are in, it is in
        //boolean x1In = tx1 > r.x1 && tx1 < r.x2;
        //boolean x2In = tx2 > r.x1 && tx2 < r.x2;

        // Negative iff line +x to obs
        //double x1s = tx1 - r.x1;
        //double x2s = tx2 - r.x1;

        // Positive if the signs match, i.e., same side
        //boolean oppSide = x1s * x2s < 0.0;

        // boolean xIn = x1In || x2In || oppSide;

        // Simplified of above
        boolean xIn = (tx1 >= r.x1 && tx1 <= r.x2) || (tx2 >= r.x1 && tx2 <= r.x2) || (((tx1 - r.x1) * (tx2 - r.x1)) < 0.0);
        boolean yIn = (ty1 >= r.y1 && ty1 <= r.y2) || (ty2 >= r.y1 && ty2 <= r.y2) || (((ty1 - r.y1) * (ty2 - r.y1)) < 0.0);
        boolean zIn = (tz1 >= r.z1 && tz1 <= r.z2) || (tz2 >= r.z1 && tz2 <= r.z2) || (((tz1 - r.z1) * (tz2 - r.z1)) < 0.0);

        return xIn && yIn && zIn;
    }
}
