package sami.event;

/**
 *
 * @author nbb
 */
public class GeneratedInputEventSubscription {

    private final Class inputEventClass;
    private final GeneratedEventListenerInt listener;
    private final InputEvent paramEvent;

    public GeneratedInputEventSubscription(InputEvent paramEvent, GeneratedEventListenerInt listener) {
        this.paramEvent = paramEvent;
        this.listener = listener;
        inputEventClass = paramEvent.getClass();
    }

    public void eventGenerated(InputEvent ie) {
        listener.eventGenerated(ie);
    }

    public InputEvent getParamEvent() {
        return paramEvent;
    }

    public Class getSubscriptionClass() {
        return inputEventClass;
    }

    public GeneratedEventListenerInt getListener() {
        return listener;
    }

    public String toString() {
        return "GeneratedInputEventSubscription for " + inputEventClass.getName() + " with listener " + listener;
    }
}
