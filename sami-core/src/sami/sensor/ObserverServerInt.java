package sami.sensor;

import sami.proxy.ProxyInt;
import java.util.ArrayList;

/**
 * Server managing all observers/sensors
 * 
 * @author nbb
 */
public interface ObserverServerInt {

    public void addListener(ObserverServerListenerInt l);

    public void removeListener(ObserverServerListenerInt l);

    public void createObserver(ProxyInt source, int channel);

    public ObserverInt getObserver(ProxyInt source, int channel);

    public ArrayList<ObserverInt> getObserverListClone();

    public void remove(ObserverInt observer);

    public void shutdown();
}
