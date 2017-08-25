/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sami.path;

/**
 * 
 * Unimplemented
 * 
Waypoint Table	Platform Behavior > Next Waypoint ID	Char 16	Name	Pointer to platform behavior set
Waypoint Table	Payload Behavior > Next Waypoint ID	Char 16	Name	Pointer to payload behavior set
Waypoint Table	Loiter Behavior @ Waypoint ID	Char 16	Name	Pointer to loiter behavior set
Waypoint Table	Data Link Behavior > Next Waypoint ID	Char 16	Name	Pointer to datalink configuration set
Waypoint Table	Waypoint Checkpoint List ID	Char 16	Name	Pointer to checkpoint test list 
Platform Behavior Table	Platform Behavior > WP ID	Char 16	Unique Behavior Set Identifier	Reference for invocation of behavior set from WP record
 * 
 * @author pscerri
 */
public class STANAGWaypointLite {
   
    public enum TurnType { Short, Long, Overfly };
    public enum AltitudeType { CrossAt, CrossAtBelow, CrossAbove };
    public enum DepartureType { OnReaching, AtTime, OperatorRelease };
   
    STANAGWaypointLite next = null;
    
    UTMCoordinate coord = null;
    double altitude = Double.NEGATIVE_INFINITY;
    double altitudeTolerance = 0.0;
    double radius = 0.0;
    TurnType turnType = TurnType.Short;
    AltitudeType altitudeType = AltitudeType.CrossAt;
    DepartureType departureType = DepartureType.AtTime;

    public UTMCoordinate getCoord() {
		return coord;
	}
	public void setCoord(UTMCoordinate coord) {
		this.coord = coord;
	}
	public double getAltitude() {
		return altitude;
	}
	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}
    
}
