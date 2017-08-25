/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sami.service.pathplanning.algorithm;

import sami.environment.Continuous3DEnvWithDiscreteObstacles;
import sami.environment.EnvironmentModel;
import sami.environment.RectangularObstacle;
import sami.path.DestinationObjective;
import sami.path.Path;
import sami.path.VehicleModel;
import sami.path.Waypoint2D;
import sami.path.Waypoints2D;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import sami.service.pathplanning.PlanningServiceListenerInt;
import sami.service.pathplanning.PlanningServiceResponse;

/**
 *
 * @author pscerri
 */
public class PotentialField {

    private Continuous3DEnvWithDiscreteObstacles lenv = null;
    private final VehicleModel vehicle;
    private final DestinationObjective dest;
    private final PlanningServiceListenerInt l;

    /**
     *
     * @param env
     * @param vehicle
     * @param dest
     * @param l
     * @param noOptions
     * @param noNodes
     * @param roadLength
     */
    public PotentialField(final EnvironmentModel env,
            final VehicleModel vehicle,
            final DestinationObjective dest,
            final PlanningServiceListenerInt l,
            final int noOptions) {

        this.vehicle = vehicle;
        this.dest = dest;
        this.l = l;

        // @todo This is now copy and paste from RRT (and probably should be in the others), might add to top level
        // First do the sampling to get a graph
        if (env instanceof Continuous3DEnvWithDiscreteObstacles) {

            this.lenv = (Continuous3DEnvWithDiscreteObstacles) env;

            (new Thread() {
                public void run() {
                    Waypoints2D plan = execute();

                    // @todo Decide whether to smooth PotentialField plan
                    Smoother sm = new Smoother(env, vehicle, dest, plan, 20);
                    Path path = sm.getSmoothedPlan();
                    // path.setCost(cost);
                    PlanningServiceResponse response = new PlanningServiceResponse(path);
                    // @todo Update cost for smoothed PotentialField 
                    if (noOptions > 1) {
                        ArrayList<Path> paths = new ArrayList<Path>();
                        for (int i = 1; i < noOptions; i++) {
                            lenv = (Continuous3DEnvWithDiscreteObstacles) env.penalizePath(plan);
                            plan = execute();
                            // plan.setCost(cost);
                            paths.add(plan);
                        }
                        response.setAlternatives(paths);
                    }
                    l.responseRecieved(response);
                }
            }).start();
        } else {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Potential field cannot handle environment of type " + env.getClass(), this);
        }
    }

    private Waypoints2D execute() {

        Waypoints2D path = new Waypoints2D();
        path.wps = new ArrayList<Waypoint2D>();

        Vec3D currLoc = new Vec3D(dest.getStartX(), dest.getStartY(), dest.getStartZ(), 0.0, 0.0, 0.0);

        System.out.println("Start loc: " + currLoc);

        boolean atGoal = false;
        int steps = 0;

        while (steps < 5000 && !atGoal) {

            // Vec3D newLoc = new Vec3D(currLoc);
            Vec3D newLoc = new Vec3D(0.0, 0.0, 0.0);
            
            // Influenced to target
            Vec3D toGoal = new Vec3D(dest.getEndX() - currLoc.x, dest.getEndY() - currLoc.y, dest.getEndZ() - currLoc.z);

            double toGoalD = toGoal.length();

            // @todo Parameterize
            if (toGoalD < 1.0) {
                atGoal = true;
                continue;
            } else {
                System.out.println("Dist = " + toGoalD);
            }

            toGoal.resizeTo(Math.min(toGoalD, 10.0));

            // Weighted add
            newLoc.weightedAdd(toGoal, 100.0);

            for (RectangularObstacle r : lenv.getObstacles()) {
                // Vector from center to current location
                Vec3D to = new Vec3D(currLoc.x - r.getCenterX(), currLoc.y - r.getCenterY(), currLoc.z - r.getCenterZ());

                // @todo 3D 

                // Work out the closest point on the rectangle to the currLoc
                double theta = Math.atan2(to.y, to.x);
                // To sides
                double hS = (r.getX2() - r.getCenterX()) / Math.cos(theta);
                // To top and bottom
                double hT = (r.getY2() - r.getCenterY()) / Math.sin(theta);
                // Distance is shorter of the above
                double minH = Math.min(Math.abs(hT), Math.abs(hS));

                double fieldDecayDist = to.length() - minH;



                double influence = r.getCost() * decayValue(fieldDecayDist);

                if (influence > 1.0) {
                    System.out.print("fieldDecayDist = " + fieldDecayDist + " from " + to.length() + " " + minH + " cost = " + r.getCost());
                    System.out.print(" From " + newLoc + " to ");
                    System.out.println(newLoc + " due to " + to + " " + influence);
                }
                // Weighted add
                newLoc.weightedAdd(to, influence);
            }

            // Normalize to get a short movement
            newLoc.normalize();
            
            newLoc.weightedAdd(currLoc, 1.0);
            
            path.wps.add(new Waypoint2D((int) newLoc.x, (int) newLoc.y));

            System.out.println("New loc: " + newLoc);
            currLoc = newLoc;

            steps++;
        }

        return path;
    }

    // @todo Sensible decay factor
    private double decayValue(double dist) {
        dist = Math.max(1.0, dist);
        return 0.1 * 1.0 / Math.max(dist * dist, 1.0);
    }

    private class Vec3D {

        double x, y, z, dx, dy, dz;

        public Vec3D(double x, double y, double z, double dx, double dy, double dz) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.dx = dx;
            this.dy = dy;
            this.dz = dz;
        }

        public Vec3D(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
            dx = 0.0;
            dy = 0.0;
            dz = 0.0;
        }

        public Vec3D(Vec3D v) {
            this.x = v.x;
            this.y = v.y;
            this.z = v.z;
            this.dx = v.dx;
            this.dy = v.dy;
            this.dz = v.dz;
        }

        /**
         * Influence this vector along the line v, by amount c
         */
        public void weightedAdd(Vec3D v, double c) {
            x += c * v.x;
            y += c * v.y;
            z += c * v.z;
        }

        public double length() {
            return Math.sqrt(x * x + y * y + z * z);
        }

        public void normalize() {
            double l = length();
            x /= l;
            y /= l;
            z /= l;
        }

        public void resizeTo(double v) {
            normalize();
            multiply(v);
        }

        public void multiply(double l) {
            x *= l;
            y *= l;
            z *= l;
        }

        public String toString() {
            return "[" + x + ", " + y + "," + z + "]";
        }
    }
}
