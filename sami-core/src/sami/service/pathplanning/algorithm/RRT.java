/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sami.service.pathplanning.algorithm;

import sami.environment.Continuous3DEnvironment;
import sami.environment.EnvironmentModel;
import sami.path.DestinationObjective;
import sami.path.Path;
import sami.path.VehicleModel;
import sami.path.VehicleModel.MotionPrimitive;
import sami.path.VehicleState;
import sami.path.Waypoint2D;
import sami.path.Waypoints2D;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import sami.service.pathplanning.PlanningServiceListenerInt;
import sami.service.pathplanning.PlanningServiceResponse;
import sami.service.pathplanning.vehicle.SimpleVehicle;

/**
 * @todo Compute multiple options
 *
 * @author pscerri
 */
public class RRT {

    private EnvironmentModel env;
    private final VehicleModel vehicle;
    private final DestinationObjective dest;
    private final PlanningServiceListenerInt l;
    double cost = 0.0;
    Random rand = new Random();

    public RRT(EnvironmentModel contEnv,
            final VehicleModel vehicle,
            final DestinationObjective dest,
            final PlanningServiceListenerInt l,
            final int noOptions) {

        this.env = contEnv;
        this.vehicle = vehicle;
        this.dest = dest;
        this.l = l;

        if (env instanceof Continuous3DEnvironment) {

            (new Thread() {
                public void run() {
                    Waypoints2D plan = execute();

                    // @todo Decide whether to smooth RRT
                    Smoother sm = new Smoother(env, vehicle, dest, plan, 20);                    
                    Path path = sm.getSmoothedPlan();
                    path.setCost(cost);
                    PlanningServiceResponse response = new PlanningServiceResponse(path);
                    // @todo Update cost for smoothed RRT 
                    if (noOptions > 1) {
                        ArrayList<Path> paths = new ArrayList<Path>();
                        for (int i = 1; i < noOptions; i++) {
                            env = env.penalizePath(plan);
                            plan = execute();
                            plan.setCost(cost);
                            paths.add(plan);
                        }
                        response.setAlternatives(paths);
                    }
                    l.responseRecieved(response);
                }
            }).start();
        } else {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "RRT requires a Continuous3DEnvironment", this);
        }
    }

    /**
     * Actually runs the RRT
     */
    private Waypoints2D execute() {

        Continuous3DEnvironment e = (Continuous3DEnvironment) env;

        double goalRate = 0.1;

        Node fNode = null;

        // Create the start and end states
        VehicleState state = new VehicleState(dest.getStartX(), dest.getStartY(), dest.getStartZ());
        VehicleState destState = new VehicleState(dest.getEndX(), dest.getEndY(), dest.getEndZ());

        boolean done = false;

        Hashtable<VehicleState, Node> states = new Hashtable<VehicleState, Node>();
        states.put(state, new Node(state, 0.0, null));

        int expansions = 0;

        // @todo Finish update to TRRT: http://www.iri.upc.edu/people/ljaillet/Papers/Iros08_TransitRRT_final.pdf
        while (!done || ++expansions < 1000) {

            if (expansions > 0 && expansions % 1000 == 0) System.out.println("Expansions: " + expansions);
            
            VehicleState randS = null;

            // Pick an expansion dest
            if (rand.nextDouble() < goalRate) {
                randS = new VehicleState(dest.getEndX(), dest.getEndY(), dest.getEndZ());
            } else {
                randS = new VehicleState(rand.nextDouble() * e.getWidth(), rand.nextDouble() * e.getLength(), state.getZ());
            }
            double bestDist = Double.MAX_VALUE;
            VehicleState rawS = null;

            // Find the closest existing node
            for (VehicleState vehicleState : states.keySet()) {
                double d = vehicleState.distTo(randS);
                // This expanded this is a cheap hack by Paul to stop the expansions happening to the same nodes all the time
                // @todo replace this with something that properly deals with non-frontier nodes
                if (d < bestDist && states.get(vehicleState).expanded < 3) {
                    bestDist = d;
                    rawS = vehicleState;
                }
            }

            // Work out the appropriate primitive for the move
            double primDist = Double.MAX_VALUE;
            VehicleState realS = null;
            ArrayList<MotionPrimitive> primitives = vehicle.getPrimitives(state);
            for (MotionPrimitive motionPrimitive : primitives) {
                VehicleState s = rawS.propagate(motionPrimitive);
                double d = s.distTo(randS);
                if (d < primDist) {
                    primDist = d;
                    realS = s;
                }
            }

            // @todo Depends on the type of cost model
            //if (transitionTest(e.getPointCost(rawS.getX(), rawS.getY(), rawS.getZ()), e.getPointCost(realS.getX(), realS.getY(), realS.getZ()))) {
            if (transitionTest(0.0, e.getPointCost(realS.getX(), realS.getY(), realS.getZ()))) {
                //System.out.print(".");
                
                /*
                if (e.getPointCost(realS.getX(), realS.getY(), realS.getZ()) > 0.0) {
                    System.out.println("Expanding to " + e.getPointCost(realS.getX(), realS.getY(), realS.getZ()));
                }
                */
                
                Node prevNode = states.get(rawS);

                /*
                if (e.getCost(prevNode.state.getX(), prevNode.state.getY(), prevNode.state.getZ(), realS.getX(), realS.getY(), realS.getZ()) > 0.0)  {
                    System.out.println("Expanding to " + e.getPointCost(realS.getX(), realS.getY(), realS.getZ()) + " " +
                            e.getCost(prevNode.state.getX(), prevNode.state.getY(), prevNode.state.getZ(), realS.getX(), realS.getY(), realS.getZ()));
                }
                */
                
                prevNode.expanded++;
                // Create the new node
                Node newNode = new Node(realS,
                        prevNode.cost + e.getCost(prevNode.state.getX(), prevNode.state.getY(), prevNode.state.getZ(), realS.getX(), realS.getY(), realS.getZ()),
                        prevNode);
                states.put(realS, newNode);

                // System.out.println("Expanded to " + realS + " from " + prevNode.state + "  dist now " + realS.distTo(destState));

                // Check for completion (this might be 0.0, but 0.01 is just an epsilon)
                // @todo Done distance needs to be set more intelligently, based on motion primitives
                if (realS.distTo(destState) <= 1.0) {
                    done = true;

                    //System.out.println("Optionally changing to " + newNode.cost + " from " + (fNode == null ? 0.0 : fNode.cost));

                    if (fNode == null || fNode.cost > newNode.cost) {

                        fNode = newNode;
                    }
                    // System.out.println("RRT solution!");
                }
            } else {
                // System.out.println("x = " + e.getPointCost(realS.getX(), realS.getY(), realS.getZ()));
            }

        }

        Waypoints2D wps = new Waypoints2D();
        ArrayList<Waypoint2D> ps = new ArrayList<Waypoint2D>();

        cost = fNode.cost;

        while (fNode != null) {
            ps.add(0, e.locToWP2D(fNode.state.getX(), fNode.state.getY()));
            fNode = fNode.prev;
        }

        wps.setWps(ps);

        return wps;
    }
    
    // Stuff for TRRT (http://www.iri.upc.edu/people/ljaillet/Papers/Iros08_TransitRRT_final.pdf)
    double T = 10e-6;
    double alphaUpT = 2.0;
    double alphaDownT = 1.001;
    int noFail = 0;
    int noFailMax = 5;
    double K = 1000.0;

    private boolean transitionTest(double c1, double c2) {
        // @todo Some max cost condition?

        if (c2 < c1) {
            return true;
        }
        double p = Math.exp((c1 - c2) / (K * T));

        // if (p < 1.0) System.out.println("p = " + p + " " + (c1-c2) + " " + ((c1 - c2) / (K * T)));
        
        if (rand.nextDouble() < p) {
            noFail = 0;
            T /= alphaDownT;
            
            // if (p < 1.0) System.out.println("p = " + p + " " + (c1-c2) + " " + ((c1 - c2) / (K * T)) + " " + T);
            return true;
        } else {
            if (noFail > noFailMax) {
                T *= alphaUpT;
                T = Math.max(10e-6, T);
                // System.out.println("T = " + T);
                noFail = 0;
            } else {
                noFail++;
            }
            return false;
        }
    }

    private class Node {

        VehicleState state;
        double cost;
        private final Node prev;
        int expanded = 0;

        public Node(VehicleState state, double cost, Node prev) {
            this.state = state;
            this.cost = cost;
            this.prev = prev;
        }
    }

    public static void main(String argv[]) {
        new RRT(new Continuous3DEnvironment(100.0, 100.0, 100.0), new SimpleVehicle(1.0), new DestinationObjective(10, 10, 50, 50), new PlanningServiceListenerInt() {
            @Override
            public void responseRecieved(PlanningServiceResponse response) {
                System.out.println("Got response: " + response.getPath());
            }
        }, 1);
    }
}
