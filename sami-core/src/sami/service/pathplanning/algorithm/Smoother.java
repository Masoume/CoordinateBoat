/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sami.service.pathplanning.algorithm;

import sami.environment.Continuous3DEnvironment;
import sami.environment.EnvironmentModel;
import sami.path.DestinationObjective;
import sami.path.VehicleModel;
import sami.path.Waypoint2D;
import sami.path.Waypoints2D;
import java.util.ArrayList;

/**
 *
 * @author pscerri
 */
public class Smoother {

    private final EnvironmentModel env;
    private final VehicleModel vehicle;
    private final DestinationObjective dest;
    private final Waypoints2D unsmoothedPlan;
    Continuous3DEnvironment ce = null;
    private Waypoints2D smoothedPlan;
    double beta = 0.33;

    public Smoother(EnvironmentModel env,
            VehicleModel vehicle,
            DestinationObjective dest,
            Waypoints2D plan, 
            int noIterations) {

        this.env = env;
        this.vehicle = vehicle;
        this.dest = dest;
        this.unsmoothedPlan = plan;


        if (env instanceof Continuous3DEnvironment) {

            ce = (Continuous3DEnvironment) env;

            simpleSmoothing2(noIterations);

        } else {
            System.out.println("Smoother can't deal with environment of type " + env.getClass());
            smoothedPlan = unsmoothedPlan;
        }
    }

    // @todo Parameterize simpleSmoothing
    // @todo 3D
    private void simpleSmoothing(int noIterations) {
        Waypoint2D[] wps = new Waypoint2D[unsmoothedPlan.wps.size()];
        int i = 0;
        for (Waypoint2D waypoint2D : unsmoothedPlan.wps) {
            wps[i++] = waypoint2D;
        }

        // Iteratively move the points, ignoring any change in cost
        for (int k = 0; k < noIterations; k++) {
            for (int j = 1; j < wps.length - 1; j++) {

                double dx = ((wps[j - 1].x + wps[j + 1].x) / 2.0) - wps[j].x;
                double dy = ((wps[j - 1].y + wps[j + 1].y) / 2.0) - wps[j].y;

                int nx = (int) (wps[j].x + beta * dx);
                int ny = (int) (wps[j].y + beta * dy);

                wps[j] = new Waypoint2D(nx, ny);
            }
        }

        // Create the smoothed plan
        smoothedPlan = new Waypoints2D();
        smoothedPlan.wps = new ArrayList<Waypoint2D>();
        for (Waypoint2D waypoint2D : wps) {
            smoothedPlan.wps.add(waypoint2D);
        }
    }

    // @todo Parameterize simpleSmoothing
    // @todo 3D
    private void simpleSmoothing2(int noIterations) {
        Waypoint2D[] wps = new Waypoint2D[unsmoothedPlan.wps.size()];
        int i = 0;
        for (Waypoint2D waypoint2D : unsmoothedPlan.wps) {
            wps[i++] = waypoint2D;
        }

        // Iteratively move the points, if it is worth it
        for (int k = 0; k < noIterations; k++) {
            for (int j = 1; j < wps.length - 1; j++) {
                double dx = ((wps[j - 1].x + wps[j + 1].x) / 2.0) - wps[j].x;
                double dy = ((wps[j - 1].y + wps[j + 1].y) / 2.0) - wps[j].y;

                wps[j] = minCost(dx, dy, wps[j], wps[j - 1], wps[j + 1]);
            }
        }

        // Create the smoothed plan
        smoothedPlan = new Waypoints2D();
        smoothedPlan.wps = new ArrayList<Waypoint2D>();
        for (Waypoint2D waypoint2D : wps) {
            smoothedPlan.wps.add(waypoint2D);
        }
    }

    /**
     *
     * @param dx
     * @param dy
     * @param c Current waypoint (the one we are dealing with)
     * @param s Start waypoint (the one before current)
     * @param e End waypoint (the one after current)
     * @return
     */
    Waypoint2D minCost(double dx, double dy, Waypoint2D c, Waypoint2D s, Waypoint2D e) {

        Waypoint2D ret = c;
        double bestCost = ce.getCost(s.x, s.y, 0, c.x, c.y, 0)
                + ce.getCost(c.x, c.y, 0, e.x, e.y, 0);

        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {

                int nx = (int) (c.x + i * (beta * dx));
                int ny = (int) (c.y + j * (beta * dy));

                double newCost = ce.getCost(s.x, s.y, 0, nx, ny, 0)
                        + ce.getCost(nx, ny, 0, e.x, e.y, 0);

                if (bestCost > newCost) {
                    ret = new Waypoint2D(nx, ny);
                    // System.out.println("Best is " + i + " " + j + " for cost " + newCost);
                    bestCost = newCost;
                }
            }
        }

        return ret;

    }

    public Waypoints2D getSmoothedPlan() {
        return smoothedPlan;
    }
}
