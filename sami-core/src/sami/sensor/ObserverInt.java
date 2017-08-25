package sami.sensor;

import sami.event.OutputEvent;

/**
 * A source of a type of observation/sensor data
 * 
 * @author nbb
 */
public interface ObserverInt {

    public void addListener(ObservationListenerInt l);

    public void removeListener(ObservationListenerInt l);

    public void handleEvent(OutputEvent oe);
}
