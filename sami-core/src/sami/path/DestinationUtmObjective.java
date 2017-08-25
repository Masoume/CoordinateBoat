package sami.path;

/**
 * @todo Allow final and initial pose to be specified
 *
 * @author pscerri
 */
public class DestinationUtmObjective extends ObjectiveFunction {

    private Location startLocation, endLocation;

    public DestinationUtmObjective(Location startLocation, Location endLocation) {
        this.startLocation = startLocation;
        this.endLocation = endLocation;
    }

    public Location getStartLocation() {
        return startLocation;
    }

    public Location setStartLocation() {
        return startLocation;
    }

    public Location getEndLocation() {
        return endLocation;
    }

    public Location setEndLocation() {
        return endLocation;
    }
    
    public String toString() {
        return "DestinationUtmObjective: startLocation = " + startLocation + "; endLocation = " + endLocation;
    }
}
