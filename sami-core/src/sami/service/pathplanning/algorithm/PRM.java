/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sami.service.pathplanning.algorithm;

import sami.environment.Continuous3DEnvironment;
import sami.environment.EnvironmentModel;
import sami.environment.GraphEnvironment;
import sami.path.DestinationObjective;
import sami.path.VehicleModel;
import sami.path.Waypoint2D;
import sami.path.Waypoints2D;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import sami.service.pathplanning.PlanningServiceListenerInt;
import sami.service.pathplanning.PlanningServiceResponse;

/**
 * @todo Take into account vehicle pose
 * @todo Assumes bi-directional links which is wrong for vehicle model
 *
 *
 * @author pscerri
 */
public class PRM {

    public PRM(final EnvironmentModel env,
            final VehicleModel vehicle,
            final DestinationObjective dest,
            final PlanningServiceListenerInt l,
            final int noOptions,
            final int noNodes,
            final double roadLength) {

        // First do the sampling to get a graph
        if (env instanceof Continuous3DEnvironment) {

            final Continuous3DEnvironment ce = (Continuous3DEnvironment) env;

            Waypoint2D[] waypoints = new Waypoint2D[noNodes];
            final double[][] connections = new double[noNodes][noNodes];
            double[][] crowsFlight = new double[noNodes][noNodes];

            // Make sure start and end locations are included            
            waypoints[0] = new Waypoint2D(dest.getStartX(), dest.getStartY());
            waypoints[1] = new Waypoint2D(dest.getEndX(), dest.getEndY());

            crowsFlight[0][1] = dist(waypoints[0], waypoints[1]);
            crowsFlight[1][0] = dist(waypoints[0], waypoints[1]);

            // A* uses graph indicies, so translate the objective
            DestinationObjective translatedDest = new DestinationObjective(0, 1);

            // Add other points
            Random rand = new Random();

            // Find nearby points
            double maxDist = roadLength * Math.max(ce.getWidth(), ce.getLength());

            int count = 0;
            final Semaphore semaphore = new Semaphore(0);

            for (int i = 2; i < crowsFlight.length; i++) {
                double x = rand.nextDouble() * ce.getWidth();
                double y = rand.nextDouble() * ce.getLength();

                Waypoint2D w = new Waypoint2D((int) x, (int) y);

                waypoints[i] = w;


                for (int j = 0; j < i; j++) {
                    double d = dist(waypoints[j], waypoints[i]);

                    /*
                     if (j == 0 || j == 1) {
                     System.out.println("Dist is " + d + " for " + j + " versus " + maxDist);
                     }
                     */

                    if (d < maxDist) {
                        DestinationObjective dobj = new DestinationObjective(waypoints[j].x, waypoints[j].y, waypoints[i].x, waypoints[i].y);
                        final int li = i;
                        final int lj = j;

                        count++;

                        // @todo PRM needs to choose how to parameterize RRT to get lots of quick solutions
                        RRT rrt = new RRT(ce, vehicle, dobj, new PlanningServiceListenerInt() {
                            @Override
                            public void responseRecieved(PlanningServiceResponse response) {
                                // @todo Remove this eplison addition to cost designed to help A* (0.0 is not a link)
                                // @todo Save the actual path (or recreate), assumes a straight line now.
                                connections[li][lj] = response.getPath().getCost() + 0.01;
                                connections[lj][li] = response.getPath().getCost() + 0.01;

                                semaphore.release();
                            }
                        }, 1);


                        if (j == 0 || j == 1) {
                            System.out.println("Added link " + i + " to " + j + " for " + d + " " + maxDist);
                        }
                    }

                    crowsFlight[j][i] = d;
                    crowsFlight[i][j] = d;
                }

            }

            try {
                System.out.print("-");
                System.out.println("Available " + semaphore.availablePermits() + " versus " + count);
                // @todo count * 0.99 is probably better, let a couple of long running RRTs fail.
                semaphore.acquire(count);
                System.out.println("+");
            } catch (InterruptedException ex) {
                Logger.getLogger(PRM.class.getName()).log(Level.SEVERE, "Semaphore acquire failed: " + ex, ex);
            }

            GraphEnvironment ge = new GraphEnvironment(connections, crowsFlight, waypoints);

            // Hand over to A*
            AStar a = new AStar(ge, vehicle, translatedDest, new PlanningServiceListenerInt() {
                @Override
                public void responseRecieved(PlanningServiceResponse response) {
                    // @todo Decide whether to do smoothing
                    if (response.getPath() != null) {

                        Waypoints2D origPath = (Waypoints2D) response.getPath();
                        final Waypoints2D rrtPath = new Waypoints2D();
                        rrtPath.wps = new ArrayList<Waypoint2D>();
                        
                        // Replan each segment, mostly for space reasons, but theoretically for continuity
                        Waypoint2D s = origPath.getWps().get(0);
                        int i = 1;
                        while (i < origPath.getWps().size()) {
                            Waypoint2D e = origPath.getWps().get(i);

                            DestinationObjective dobj = new DestinationObjective(s.x, s.y, e.x, e.y);

                            // @todo PRM needs to choose how to parameterize RRT to get lots of quick solutions
                            RRT rrt = new RRT(ce, vehicle, dobj, new PlanningServiceListenerInt() {
                                @Override
                                public void responseRecieved(PlanningServiceResponse response) {

                                    rrtPath.getWps().addAll(((Waypoints2D) response.getPath()).getWps());

                                    semaphore.release();
                                }
                            }, 1);
                            try {
                                System.out.print("o");
                                semaphore.acquire();
                                System.out.println("x");
                            } catch (InterruptedException ex) {
                                Logger.getLogger(PRM.class.getName()).log(Level.SEVERE, "Semaphore acquire failed: " + ex, ex);
                            }
                            s = e;
                            i++;
                        }

                        // @todo Decide whether to smooth and how much
                        Smoother sm = new Smoother(ce, vehicle, dest, rrtPath, 20);
                        
                        // @todo Handle options for PRM returned

                        PlanningServiceResponse sResponse = new PlanningServiceResponse(sm.getSmoothedPlan());
                        l.responseRecieved(sResponse);
                    } else {
                        // @todo Handle A* failing
                        l.responseRecieved(response);
                    }
                }
            }, noOptions);

        } else {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "RRT requires a Continuous3DEnvironment", this);
        }

    }

    private double dist(Waypoint2D a, Waypoint2D b) {
        return Math.sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y));
    }
}
