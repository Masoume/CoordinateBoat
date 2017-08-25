package sami.service.pathplanning;

import java.util.ArrayList;
import sami.path.Path;

/**
 *
 * @author pscerri
 */
public class PlanningServiceResponse {

    private final Path path;
    private ArrayList<? extends Path> alternatives = null;

    public PlanningServiceResponse(Path path) {
        this.path = path;
        alternatives = new ArrayList<Path>();
    }

    public PlanningServiceResponse(Path path, ArrayList<? extends Path> alternatives) {
        this.path = path;
        this.alternatives = alternatives;
    }

    public Path getPath() {
        return path;
    }

    public ArrayList<? extends Path> getAlternatives() {
        return alternatives;
    }

    public void setAlternatives(ArrayList<Path> alternatives) {
        this.alternatives = alternatives;
    }
}
