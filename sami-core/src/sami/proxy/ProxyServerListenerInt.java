package sami.proxy;

/**
 *
 * @author pscerri
 */
public interface ProxyServerListenerInt {

    public void proxyAdded(ProxyInt p);

    public void proxyRemoved(ProxyInt p);
}
