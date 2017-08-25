package sami.engine;

import sami.event.GeneratedEventListenerInt;
import sami.event.GeneratedInputEventSubscription;
import sami.event.InputEvent;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import sami.service.ServiceServer;

/**
 *
 * @author pscerri
 */
public class InputEventMapper {

    private static class InputEventMapperHolder {

        public static final InputEventMapper INSTANCE = new InputEventMapper();
    }

    private InputEventMapper() {
    }

    public static InputEventMapper getInstance() {
        return InputEventMapper.InputEventMapperHolder.INSTANCE;
    }
    private ServiceServer services = null;
    Hashtable<InputEvent, GeneratedInputEventSubscription> eventToSub = new Hashtable<InputEvent, GeneratedInputEventSubscription>();

    /**
     * Set the services object the mapper uses to find services with information
     *
     * @Override
     */
    public void setServiceServer(ServiceServer s) {
        this.services = s;
    }

    /**
     * Called by the plan manager to let the system know it cares about some
     * event
     *
     * @param paramEvent The (input) event from PlanManager with variable names
     * filled out
     * @param listener The process to notify when the subscription generates the
     * actual event
     * @Override
     */
    public void registerEvent(InputEvent paramEvent, GeneratedEventListenerInt listener) {
        Logger.getLogger(this.getClass().getName()).log(Level.FINE, "Registering event " + paramEvent, this);

        GeneratedInputEventSubscription sub = new GeneratedInputEventSubscription(paramEvent, listener);
        eventToSub.put(paramEvent, sub);
        services.subscribe(sub);
    }

    public void unregisterEvent(InputEvent paramEvent) {
        GeneratedInputEventSubscription sub = eventToSub.remove(paramEvent);
        if (sub != null) {
            services.unsubscribe(sub);
        }
    }
}
