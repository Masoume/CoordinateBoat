/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sami.event;

/**
 *
 * @author pscerri
 */
public abstract class InformationEventSubscription {

    private final InformationEventHandlerInt handler;
    private final InputEvent paramEvent;

    public InformationEventSubscription(InformationEventHandlerInt handler, InputEvent paramEvent) {
        this.handler = handler;
        this.paramEvent = paramEvent;
    }

    public InputEvent getParamEvent() {
        return paramEvent;
    }

    public void event(InputEvent ie) {
        handler.event(ie);
    }
    
    public String toString() {
        return "InformationEventSubscription for " + paramEvent.getClass().getName();
    }
}
