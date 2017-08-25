package sami.uilanguage;

import sami.uilanguage.fromui.FromUiMessage;

/**
 *
 * @author pscerri
 */
public interface UiServerListenerInt {

    public void FromUiMessage(FromUiMessage m);

    public UiClientInt getUiClient();

    public void setUiClient(UiClientInt uiClient);

    public UiServerInt getUiServer();

    public void setUiServer(UiServerInt uiServer);
}
