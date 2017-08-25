/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sami.path;

import java.util.ArrayList;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 *
 * @author pscerri
 */
public abstract class VehicleModel {

    private String vehicleCUCSId;
    private String vehicleCallSign;
    private String ownerCUCSId;
    private String ownerCallSign;
    private Location location;
    private Vector3D velocityAir;	// km/h
    private Vector3D velocityGround;	// km/h
    private Orientation heading;
    private double fuelRemaining;	// L
    private double fuelConsumptionRate;
    private double endurance;	// s
    private double powerSetting;
    private String model;
    private double speedMax = 100;	// km/h
    private double speedThreshold = 350;	// km/h
    private double altitudeMax = 2000;	// m
    private double climbRateMax = 20;
    private double descentRateMax = 45;
    //	private double maxAngleOfAscent = 60;
    private FuelProfile fuelProfile = null;
    private double fuelCapacity = 20;	// L
    private double turningRadius = 5;	// m

    public VehicleModel() {
    }

    public VehicleModel(String model, double speedMax, double speedThreshold, double altitudeMax, double climbRateMax, double descentRateMax,
            FuelProfile fuelProfile, double fuelCapacity, double turningRadius) {
        this.model = model;
        this.speedMax = speedMax;
        this.speedThreshold = speedThreshold;
        this.altitudeMax = altitudeMax;
        this.climbRateMax = climbRateMax;
        this.descentRateMax = descentRateMax;
        this.fuelProfile = fuelProfile;
        this.fuelCapacity = fuelCapacity;
        this.turningRadius = turningRadius;
    }

    public VehicleModel(String vehicleCUCSId, String vehicleCallSign, String ownerCUCSId, String ownerCallSign, Location location, Vector3D velocityAir,
            Vector3D velocityGround, Orientation heading, double fuelRemaining, double fuelConsumptionRate, double endurance, double powerSetting) {
        this.vehicleCUCSId = vehicleCUCSId;
        this.vehicleCallSign = vehicleCallSign;
        this.ownerCUCSId = ownerCUCSId;
        this.ownerCallSign = ownerCallSign;
        this.location = location;
        this.velocityAir = velocityAir;
        this.velocityGround = velocityGround;
        this.heading = heading;
        this.fuelRemaining = fuelRemaining;
        this.fuelConsumptionRate = fuelConsumptionRate;
        this.endurance = endurance;
        this.powerSetting = powerSetting;
    }

    public VehicleModel(String vehicleCUCSId, String vehicleCallSign, String ownerCUCSId, String ownerCallSign, Location location, Vector3D velocityAir,
            Vector3D velocityGround, Orientation heading, double fuelRemaining, double fuelConsumptionRate, double endurance, double powerSetting,
            String model, double speedMax, double speedThreshold, double altitudeMax, double climbRateMax, double descentRateMax, FuelProfile fuelProfile,
            double fuelCapacity, double turningRadius) {
        this.vehicleCUCSId = vehicleCUCSId;
        this.vehicleCallSign = vehicleCallSign;
        this.ownerCUCSId = ownerCUCSId;
        this.ownerCallSign = ownerCallSign;
        this.location = location;
        this.velocityAir = velocityAir;
        this.velocityGround = velocityGround;
        this.heading = heading;
        this.fuelRemaining = fuelRemaining;
        this.fuelConsumptionRate = fuelConsumptionRate;
        this.endurance = endurance;
        this.powerSetting = powerSetting;
        this.model = model;
        this.speedMax = speedMax;
        this.speedThreshold = speedThreshold;
        this.altitudeMax = altitudeMax;
        this.climbRateMax = climbRateMax;
        this.descentRateMax = descentRateMax;
        this.fuelProfile = fuelProfile;
        this.fuelCapacity = fuelCapacity;
        this.turningRadius = turningRadius;
    }

    public abstract ArrayList<MotionPrimitive> getPrimitives(VehicleState s);    
    
    public class MotionPrimitive {
        double dx, dy, dz;

        public MotionPrimitive(double dx, double dy, double dz) {
            this.dx = dx;
            this.dy = dy;
            this.dz = dz;            
        }    
    }
    
    public double getAltitudeMax() {
        return this.altitudeMax;
    }

    public double getClimbRateMax() {
        return this.climbRateMax;
    }

    public double getDescentRateMax() {
        return this.descentRateMax;
    }

    public double getEndurance() {
        return this.endurance;
    }

    public double getFuelCapacity() {
        return this.fuelCapacity;
    }

    public double getFuelConsumptionRate() {
        return this.fuelConsumptionRate;
    }

    public FuelProfile getFuelProfile() {
        return this.fuelProfile;
    }

    public double getFuelRemaining() {
        return this.fuelRemaining;
    }

    public Orientation getHeading() {
        return this.heading;
    }

    public Location getLocation() {
        return this.location;
    }

    public String getModel() {
        return this.model;
    }

    public String getOwnerCallSign() {
        return this.ownerCallSign;
    }

    public String getOwnerCUCSId() {
        return this.ownerCUCSId;
    }

    public double getPowerSetting() {
        return this.powerSetting;
    }

    public double getSpeedMax() {
        return this.speedMax;
    }

    public double getSpeedThreshold() {
        return this.speedThreshold;
    }

    public double getTurningRadius() {
        return this.turningRadius;
    }

    public String getVehicleCallSign() {
        return this.vehicleCallSign;
    }

    public String getVehicleCUCSId() {
        return this.vehicleCUCSId;
    }

    public Vector3D getVelocityAir() {
        return this.velocityAir;
    }

    public Vector3D getVelocityGround() {
        return this.velocityGround;
    }

    public void setAltitudeMax(double altitudeMax) {
        this.altitudeMax = altitudeMax;
    }

    public void setClimbRateMax(double climbRateMax) {
        this.climbRateMax = climbRateMax;
    }

    public void setDescentRateMax(double descentRateMax) {
        this.descentRateMax = descentRateMax;
    }

    public void setEndurance(double endurance) {
        this.endurance = endurance;
    }

    public void setFuelCapacity(double fuelCapacity) {
        this.fuelCapacity = fuelCapacity;
    }

    public void setFuelConsumptionRate(double fuelConsumptionRate) {
        this.fuelConsumptionRate = fuelConsumptionRate;
    }

    public void setFuelProfile(FuelProfile fuelProfile) {
        this.fuelProfile = fuelProfile;
    }

    public void setFuelRemaining(double fuelRemaining) {
        this.fuelRemaining = fuelRemaining;
    }

    public void setHeading(Orientation heading) {
        this.heading = heading;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setOwnerCallSign(String ownerCallSign) {
        this.ownerCallSign = ownerCallSign;
    }

    public void setOwnerCUCSId(String ownerCUCSId) {
        this.ownerCUCSId = ownerCUCSId;
    }

    public void setPowerSetting(double powerSetting) {
        this.powerSetting = powerSetting;
    }

    public void setSpeedMax(double speedMax) {
        this.speedMax = speedMax;
    }

    public void setSpeedThreshold(double speedThreshold) {
        this.speedThreshold = speedThreshold;
    }

    public void setTurningRadius(double turningRadius) {
        this.turningRadius = turningRadius;
    }

    public void setVehicleCallSign(String vehicleCallSign) {
        this.vehicleCallSign = vehicleCallSign;
    }

    public void setVehicleCUCSId(String vehicleCUCSId) {
        this.vehicleCUCSId = vehicleCUCSId;
    }

    public void setVelocityAir(Vector3D velocityAir) {
        this.velocityAir = velocityAir;
    }

    public void setVelocityGround(Vector3D velocityGround) {
        this.velocityGround = velocityGround;
    }
}
