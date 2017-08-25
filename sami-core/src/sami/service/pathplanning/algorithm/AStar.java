/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sami.service.pathplanning.algorithm;

import sami.environment.EnvironmentModel;
import sami.service.pathplanning.PlanningServiceListenerInt;
import sami.service.pathplanning.PlanningServiceResponse;
import sami.path.DestinationObjective;
import sami.path.Path;
import sami.path.VehicleModel;
import sami.path.Waypoints2D;
import sami.path.Waypoint2D;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @todo Technically the graph provided here could be 3D, so the waypoint2D might be changed
 *
 * @author pscerri
 */
public class AStar {

    private EnvironmentModel env;
    private final VehicleModel vehicle;
    private final DestinationObjective dest;
    private final PlanningServiceListenerInt l;

    public AStar(EnvironmentModel graph, VehicleModel vehicle, DestinationObjective dest, final PlanningServiceListenerInt l, final int noOptions) {
        this.env = graph;
        this.vehicle = vehicle;
        this.dest = dest;
        this.l = l;

        (new Thread() {
            public void run() {
                Waypoints2D plan = execute();
                PlanningServiceResponse response = new PlanningServiceResponse(plan);
                if(noOptions > 1) {
                    ArrayList<Path> paths = new ArrayList<Path>();
                    for (int i = 1; i < noOptions; i++) {
                        env = env.penalizePath(plan);
                        plan = execute();
                        paths.add(plan);
                    }
                    response.setAlternatives(paths);
                }
                l.responseRecieved(response);
            }
        }).start();
    }

    /**
     * Straightforward A* implementation
     */
    private Waypoints2D execute() {

        java.util.ArrayList<Node> closed = new java.util.ArrayList<Node>();
        java.util.PriorityQueue<Node> open = new java.util.PriorityQueue<Node>();

        Node s = new Node(dest.getStartLoc(), 0.0, getH(0));
        open.add(s);

        while (!open.isEmpty()) {
            Node o = open.poll();

            // See if we are done
            if (o.loc == dest.getEndLoc()) {
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Done " + o.loc + " " + dest.getEndLoc(), this);
                // Create plan
                Waypoints2D plan = new Waypoints2D();
                ArrayList<Waypoint2D> wps = new ArrayList<Waypoint2D>();

                while (o != null) {
                    wps.add(env.locToWP2D(o.loc));
                    o = o.getPrev();
                }

                plan.setWps(wps);

                return plan;
            }

            closed.add(o);

            java.util.ArrayList<Node> expansions = getExpansions(o);
            for (Node node : expansions) {
                // System.out.println("Expanding ... " + node.loc);
                if (!closed.contains(node)) {
                    if (!open.contains(node)) {
                        open.offer(node);
                        // Logger.getLogger(this.getClass().getName()).log(Level.OFF, "Expanded", this);
                    } else if (open.contains(node)) {
                        Node[] openA = open.toArray(new Node[open.size()]);
                        for (Node a : openA) {
                            if (a.equals(node)) {
                                // Need to check whether new node is better
                                if (a.getF() > node.getF()) {
                                    // Replace
                                    open.remove(a);
                                    open.offer(node);
                                }

                            }
                        }
                    }
                }
            }
        }

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "AStar failed", this);
        return null;
    }

    private double getH(int loc) {
        if (env == null) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "AStar has no environment it can work with", this);
            return 100.0;
        }
        return env.getCrowsFly(loc, dest.getEndLoc());
    }

    private java.util.ArrayList<Node> getExpansions(Node n) {
        java.util.ArrayList<Integer> dests = env.getDests(n.loc);
        java.util.ArrayList<Node> nodes = new java.util.ArrayList<Node>();

        for (Integer i : dests) {
            Node node = new Node(i, n.g + env.getCost(n.loc, i), env.getCrowsFly(i, dest.getEndLoc()), n);
            // System.out.println("New node " + n.getF());
            nodes.add(node);
        }

        return nodes;
    }

    private class Node implements Comparable<Node> {

        private int loc;
        private double g, h;
        private Node prev = null;

        public Node(int loc, double g, double h) {
            this.loc = loc;
            this.g = g;
            this.h = h;
        }

        public Node(int loc, double g, double h, Node prev) {
            this.loc = loc;
            this.g = g;
            this.h = h;
            this.prev = prev;
        }

        public double getG() {
            return g;
        }

        public double getH() {
            return h;
        }

        public Node getPrev() {
            return prev;
        }

        public double getF() {
            return g + h;
        }

        @Override
        public int compareTo(Node t) {
            if (t.getF() > getF()) {
                return -1;
            } else if (t.getF() < getF()) {
                return 1;
            } else {
                return 0;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Node && ((Node) o).loc == loc) {
                return true;
            } else {
                return false;
            }
        }
    }
}
