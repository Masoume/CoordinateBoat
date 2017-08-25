package sami.path;

/**
 *
 * @author pscerri
 */
public class Waypoint2D extends Waypoint{

    public int x;
    public int y;

    public Waypoint2D() {
    }

    public Waypoint2D(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public String toString() {
        return "[" + x + ", " + y + "]";
    }
}
