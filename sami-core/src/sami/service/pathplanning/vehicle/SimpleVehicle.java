package sami.service.pathplanning.vehicle;

import sami.path.VehicleModel;
import sami.path.VehicleState;
import java.util.ArrayList;

/**
 *
 * @author pscerri
 */
public class SimpleVehicle extends VehicleModel {

    private final double speed;

    public SimpleVehicle(double speed) {
        this.speed = speed;
    }

    @Override
    public ArrayList<MotionPrimitive> getPrimitives(VehicleState s) {
        ArrayList<MotionPrimitive> ps = new ArrayList<MotionPrimitive>();
        MotionPrimitive p = new MotionPrimitive(1.0, 0.0, 0.0);
        ps.add(p);
        p = new MotionPrimitive(0.0, 1.0, 0.0);
        ps.add(p);
        p = new MotionPrimitive(0.0, -1.0, 0.0);
        ps.add(p);
        p = new MotionPrimitive(-1.0, 0.0, 0.0);
        ps.add(p);
        return ps;
    }
}
