package sami.path;

import java.io.Serializable;

public class Orientation implements Serializable {

    protected double roll;
    protected double pitch;
    protected double yaw;

    public Orientation() {
    }

    public Orientation(double roll, double pitch, double yaw) {
        this.roll = roll;
        this.pitch = pitch;
        this.yaw = yaw;
    }

    private Orientation(Orientation d) {
        this.roll = d.getRoll();
        this.pitch = d.getPitch();
        this.yaw = d.getYaw();
    }

    public double getRoll() {
        return roll;
    }

    public void setRoll(double roll) {
        this.roll = roll;
    }

    public double getPitch() {
        return pitch;
    }

    public void setPitch(double pitch) {
        this.pitch = pitch;
    }

    public double getYaw() {
        return yaw;
    }

    public void setYaw(double yaw) {
        this.yaw = yaw;
    }

    public Orientation clone() {
        return new Orientation(this);
    }

    public String toString() {
        return "Orientation: [" + roll + ", " + pitch + ", " + yaw + "]";
    }
}
