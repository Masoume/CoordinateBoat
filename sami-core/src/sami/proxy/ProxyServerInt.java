package sami.proxy;

import com.perc.mitpas.adi.common.datamodels.AbstractAsset;
import java.awt.Color;
import java.net.InetSocketAddress;
import java.util.ArrayList;

/**
 *
 * @author nbb
 */
public interface ProxyServerInt {

    // Listener stuff
    public boolean addListener(ProxyServerListenerInt l);

    public boolean removeListener(ProxyServerListenerInt l);

    // Proxy stuff
    public ProxyInt createProxy(String name, Color color, InetSocketAddress addr);

    public ProxyInt getProxy(AbstractAsset asset);

    public AbstractAsset getAsset(ProxyInt proxy);

    public ArrayList<ProxyInt> getProxyListClone();

    public ArrayList<AbstractAsset> getAssetListClone();

    public boolean remove(ProxyInt proxy);

    public boolean remove(AbstractAsset asset);

    public boolean shutdown();
}
