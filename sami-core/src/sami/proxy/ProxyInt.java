package sami.proxy;

import sami.event.OutputEvent;
import java.util.ArrayList;
import java.util.UUID;

/**
 *
 * @author pscerri
 */
public interface ProxyInt {

    public int getProxyId();

    public String getProxyName();

    public void addListener(ProxyListenerInt l);

    public void removeListener(ProxyListenerInt l);

    public void start();

    public void handleEvent(OutputEvent oe);

    public OutputEvent getCurrentEvent();

    public ArrayList<OutputEvent> getEvents();

    public void abortEvent(UUID eventId);

    public void abortMission(UUID missionId);
}
