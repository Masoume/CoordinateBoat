package sami.sensor;

import sami.event.InputEvent;

/**
 * Listens to a particular observation/sensor source
 * 
 * @author nbb
 */
public interface ObservationListenerInt {

    public void eventOccurred(InputEvent ie);

    public void newObservation(Observation o);
}
