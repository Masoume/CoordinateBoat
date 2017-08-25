package sami.sensor;

/**
 * Listens to Observer server to hear about new observers/sensors
 * 
 * @author nbb
 */
public interface ObserverServerListenerInt {

    public void observerAdded(ObserverInt p);

    public void observerRemoved(ObserverInt p);
}
