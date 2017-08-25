package sami.statemanager;

import sami.environment.EnvironmentModel;
import sami.path.VehicleModel;
import sami.proxy.ProxyInt;

/**
 *
 * @author pscerri
 */
public interface StateManagerInt {
    
    public EnvironmentModel getEnvironmentModel();
    public VehicleModel getVehicleModel(ProxyInt p);
    public VehicleModel getVehicleModel(String proxyId);
    
}
