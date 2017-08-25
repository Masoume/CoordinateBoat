/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sami.service.pathplanning;

import sami.environment.EnvironmentModel;
import sami.path.ObjectiveFunction;
import sami.path.VehicleModel;

/**
 *
 * @author pscerri
 */
public class PlanningServiceRequest {
    private final EnvironmentModel em;
    private final ObjectiveFunction of;
    private final VehicleModel veh;

    private int noOptions = 1;
    
    public PlanningServiceRequest(EnvironmentModel em, ObjectiveFunction of, VehicleModel veh) {
        this.em = em;
        this.of = of;
        this.veh = veh;
    }
    
    public PlanningServiceRequest(EnvironmentModel em, ObjectiveFunction of, VehicleModel veh, int noOptions) {
        this.em = em;
        this.of = of;
        this.veh = veh;
        this.noOptions = noOptions;
    }

    public EnvironmentModel getEm() {
        return em;
    }

    public ObjectiveFunction getOf() {
        return of;
    }

    public VehicleModel getVeh() {
        return veh;
    }

    public int getNoOptions() {
        return noOptions;
    }

    public void setNoOptions(int noOptions) {
        this.noOptions = noOptions;
    }
        
}
