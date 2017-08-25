/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sami.uilanguage;

import java.util.UUID;
import sami.uilanguage.toui.ToUiMessage;

/**
 *
 * @author pscerri
 */
public interface UiClientInt {
     
    public void addClientListener(UiClientListenerInt l);
    
    public void removeClientListener(UiClientListenerInt l);
    
    public void toUiMessageReceived(ToUiMessage m);
    
    public void toUiMessageHandled(UUID toUiMessageId);
    
}
