package sami.path;

/**
 *
 * @author pscerri
 */
public abstract class Path {

    protected double cost = 0;

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }
}
