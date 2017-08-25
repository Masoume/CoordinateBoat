package sami.service.pathplanning;

import java.util.ArrayList;
import sami.path.PathUtm;

/**
 *
 * @author pscerri
 */
public class PlanningUtmServiceResponse {

    private final PathUtm path;
    private ArrayList<PathUtm> alternatives = null;

    public PlanningUtmServiceResponse(PathUtm path) {
        this.path = path;
        alternatives = new ArrayList<PathUtm>();
    }

    public PlanningUtmServiceResponse(PathUtm path, ArrayList<PathUtm> alternatives) {
        this.path = path;
        this.alternatives = alternatives;
    }

    public PathUtm getPath() {
        return path;
    }

    public ArrayList<PathUtm> getAlternatives() {
        return alternatives;
    }

    public void setAlternatives(ArrayList<PathUtm> alternatives) {
        this.alternatives = alternatives;
    }
}
