/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sami.path;

import java.util.ArrayList;
import sami.path.Path;

/**
 * Mission Plan Table	Platform Identifier	Char 16	Platform Callsign	Assigned
 * Unique Identifier for Platform Mission Plan Table	Waypoint List ID	Char 16
 * Name of Mission Waypoint List	Used to reference defined set of mission route
 * fixes, behaviors, and other supporting fixes Waypoint Table	Waypoint List ID
 * Char 16	Name of Mission Waypoint List	PK for set of mission route fixes,
 * behaviors, and other supporting fixes for a mission. Waypoint Table	Active
 * Waypoint	XOR Flag Yes/No	Indicates which WP on the mission waypoint list is
 * currently the active destination. Waypoint Table	Waypoint Identifier	Char 16
 * Name Waypoint Table	Waypoint Type	Enumeration	"Navigation WP Loiter WP
 * Coordination WP Initial Point Release Point Target WP
 *
 * @author pscerri
 */
public class STANAGPlanLite extends Path {

    String identifier = null;
    long startTime = 0L;
    ArrayList<STANAGWaypointLite> waypoints = null;
    STANAGWaypointLite LoiterWP = null;
    STANAGWaypointLite CoordinationWP = null;
    STANAGWaypointLite InitialWP = null;
    STANAGWaypointLite ReleaseWP = null;
    STANAGWaypointLite TargetWP = null;

    public STANAGWaypointLite getCoordinationWP() {
        return CoordinationWP;
    }

    public STANAGWaypointLite getInitialWP() {
        return InitialWP;
    }

    public STANAGWaypointLite getLoiterWP() {
        return LoiterWP;
    }

    public STANAGWaypointLite getReleaseWP() {
        return ReleaseWP;
    }

    public STANAGWaypointLite getTargetWP() {
        return TargetWP;
    }

    public String getIdentifier() {
        return identifier;
    }

    public long getStartTime() {
        return startTime;
    }

    public ArrayList<STANAGWaypointLite> getWaypoints() {
        return waypoints;
    }

    public void setCoordinationWP(STANAGWaypointLite CoordinationWP) {
        this.CoordinationWP = CoordinationWP;
    }

    public void setInitialWP(STANAGWaypointLite InitialWP) {
        this.InitialWP = InitialWP;
    }

    public void setLoiterWP(STANAGWaypointLite LoiterWP) {
        this.LoiterWP = LoiterWP;
    }

    public void setReleaseWP(STANAGWaypointLite ReleaseWP) {
        this.ReleaseWP = ReleaseWP;
    }

    public void setTargetWP(STANAGWaypointLite TargetWP) {
        this.TargetWP = TargetWP;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setWaypoints(ArrayList<STANAGWaypointLite> waypoints) {
        this.waypoints = waypoints;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        try {
            sb.append("STANAGPlanLite:[");
            for (STANAGWaypointLite sTANAGWaypointLite : waypoints) {
                sb.append(sTANAGWaypointLite.toString());
                sb.append(",");
            }
            sb.append("]");
        } catch (NullPointerException e) {
            sb.append("No Waypoints]");
        }
        return sb.toString();
    }

    @Override
    public double getCost() {
        return cost;
    }

    @Override
    public void setCost(double cost) {
        this.cost = cost;
    }
}
