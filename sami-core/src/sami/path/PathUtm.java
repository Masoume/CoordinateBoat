package sami.path;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author nbb
 */
public class PathUtm extends Path implements Serializable {

    protected List<Location> points = new ArrayList<Location>();

    public PathUtm() {
        points = new ArrayList<Location>();
    }

    public PathUtm(List<Location> points) {
        this.points = points;
    }

    public List<Location> getPoints() {
        return points;
    }

    public String toString() {
        String s = "[";
        if (points != null) {
            for (int i = 0; i < points.size(); i++) {
                s += points.get(i).toString();
            }
        }
        s += "]";
        return s;
    }
}
