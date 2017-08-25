/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sami.uilanguage;

import sami.uilanguage.fromui.FromUiMessage;

/**
 *
 * @author pscerri
 */
public interface UiServerInt {
    
    public void UIMessage(FromUiMessage msg);
 
    public void addServerListener(UiServerListenerInt l);
    
    public void removeServerListener(UiServerListenerInt l);
}
