package sami.uilanguage;

import java.util.UUID;
import sami.uilanguage.toui.ToUiMessage;

/**
 *
 * @author pscerri
 */
public interface UiClientListenerInt {

    public void toUiMessageReceived(ToUiMessage m);
    
    public void toUiMessageHandled(UUID toUiMessageId);

    public UiClientInt getUiClient();

    public void setUiClient(UiClientInt uiClient);

    public UiServerInt getUiServer();

    public void setUiServer(UiServerInt uiServer);
}
