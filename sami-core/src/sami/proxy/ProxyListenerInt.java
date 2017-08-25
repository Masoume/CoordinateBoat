package sami.proxy;

import sami.event.InputEvent;

/**
 *
 * @author pscerri
 */
public interface ProxyListenerInt {

    public void eventOccurred(InputEvent ie);

    public void poseUpdated();

    public void waypointsUpdated();

    public void waypointsComplete();
}
