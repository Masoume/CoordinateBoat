package sami.service;

//import com.perc.mitpas.adi.mission.planning.resourceallocation.AllocationSolverServer;
//import com.perc.mitpas.adi.mission.planning.resourceallocation.ResourceAllocationRequest;
//import com.perc.mitpas.adi.mission.planning.resourceallocation.ResponseListener;
// Commented out the above for now, because I don't have this code.  -- Tim
import sami.event.GeneratedInputEventSubscription;
import com.perc.mitpas.adi.mission.planning.resourceallocation.AllocationSolverServer;
import com.perc.mitpas.adi.mission.planning.resourceallocation.ResourceAllocationRequest;
import com.perc.mitpas.adi.mission.planning.resourceallocation.ResponseListener;
import sami.service.information.InformationServer;
import java.util.logging.Level;
import java.util.logging.Logger;
import sami.service.pathplanning.PlanningService;
import sami.service.pathplanning.PlanningServiceListenerInt;
import sami.service.pathplanning.PlanningServiceRequest;

/**
 *
 * @author pscerri
 */
public class ServiceServer {

    InformationServer infoServer = null;

    public ServiceServer() {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Starting services");
        infoServer = new InformationServer();
    }

    public void subscribe(GeneratedInputEventSubscription sub) {
        infoServer.subscribe(sub);
    }

    public void unsubscribe(GeneratedInputEventSubscription sub) {
        infoServer.unsubscribe(sub);
    }

    public void submitPlanningRequest(PlanningServiceRequest request, PlanningServiceListenerInt l) {
        PlanningService.submitRequest(request, l);
    }

    public void submitResourceAllocationRequest(ResourceAllocationRequest request, ResponseListener listener) {
        AllocationSolverServer.submitRequest(request, listener);
    }
    // Commented out the above for now, because I don't have this code.  -- Tim
    // If you don't have it, get it from Praveen, it is basically the DART code
}
