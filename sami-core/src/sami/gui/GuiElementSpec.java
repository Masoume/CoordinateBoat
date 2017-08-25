package sami.gui;

/**
 *
 * @author pscerri
 */
public class GuiElementSpec implements java.io.Serializable {

    String elementName = null;
    int relativeSize = 100;

    public GuiElementSpec() {
    }

    public GuiElementSpec(String elementName) {
        this.elementName = elementName;
    }

    public String getElementName() {
        return elementName;
    }

    public boolean matches(String type) {
        return type.equalsIgnoreCase(elementName);
    }

    public int getRelativeSize() {
        return relativeSize;
    }

    public void setRelativeSize(int relativeSize) {
        this.relativeSize = relativeSize;
    }
}
