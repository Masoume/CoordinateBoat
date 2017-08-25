package sami.service.information;

import sami.event.GeneratedInputEventSubscription;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pscerri
 */
public class InformationServer {

    private static final Logger LOGGER = Logger.getLogger(InformationServer.class.getName());
    private static ArrayList<InformationServiceProviderInt> serviceProviders = new ArrayList<InformationServiceProviderInt>();

    public static void addServiceProvider(InformationServiceProviderInt p) {
        if (serviceProviders.contains(p)) {
            Logger.getLogger(InformationServer.class.getName()).log(Level.FINE, "Service provider has already been added to service server: " + p);
        } else {
            Logger.getLogger(InformationServer.class.getName()).log(Level.FINE, "Service provider added to service server: " + p);
            serviceProviders.add(p);
        }
    }

    // StateManager stateManager = null;
    public InformationServer() {
        Logger.getLogger(this.getClass().getName()).log(Level.FINE, "Information server started");
    }

    public void subscribe(GeneratedInputEventSubscription sub) {
        boolean accepted = false;
        for (InformationServiceProviderInt serviceProvider : serviceProviders) {
            boolean s = serviceProvider.offer(sub);
            accepted = accepted || s;
        }

        if (accepted) {
            Logger.getLogger(this.getClass().getName()).log(Level.FINE, "Subscription taken: " + sub);
        } else {
            Logger.getLogger(this.getClass().getName()).log(Level.FINE, "No service for: " + sub);
        }
    }

    public void unsubscribe(GeneratedInputEventSubscription sub) {
        boolean canceled = false;
        for (InformationServiceProviderInt serviceProvider : serviceProviders) {
            boolean c = serviceProvider.cancel(sub);
            canceled = canceled || c;
        }
        if (canceled) {
            LOGGER.log(Level.FINE, "Canceling: " + sub);
        } else {
            LOGGER.log(Level.FINE, "No service for: " + sub);
        }
    }
}
