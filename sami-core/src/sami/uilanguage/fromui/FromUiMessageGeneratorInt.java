package sami.uilanguage.fromui;

import java.lang.reflect.Field;
import java.util.Hashtable;
import sami.event.ReflectedEventSpecification;
import sami.uilanguage.MarkupComponent;
import sami.uilanguage.toui.CreationMessage;
import sami.uilanguage.toui.SelectionMessage;

/**
 *
 * @author nbb
 */
public interface FromUiMessageGeneratorInt {

    public FromUiMessage getFromUiMessage(CreationMessage creationMessage, Hashtable<ReflectedEventSpecification, Hashtable<Field, MarkupComponent>> eventSpecToComponentTable);

    public FromUiMessage getFromUiMessage(SelectionMessage selectionMessage, Object option);

    public FromUiMessage getFromUiMessage(SelectionMessage selectionMessage, int optionIndex);
}
