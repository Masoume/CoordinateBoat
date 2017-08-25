package sami.markup;

import java.util.ArrayList;
import java.util.HashMap;
import sami.markupOption.AreaOption;
import sami.markupOption.PointOption;

/**
 *
 * @author nbb
 */
public class RelevantArea extends Markup {

    // List of enum fields for which an enum option should be selected
    public static final ArrayList<String> enumFieldNames = new ArrayList<String>();
    // Description for each enum field
    public static final HashMap<String, String> enumNameToDescription = new HashMap<String, String>();
    // Mapping from enum value to the MarkupOption field it requires
    public static final HashMap<Enum, String> enumValueToFieldName = new HashMap<Enum, String>();
    // Fields
    public AreaSelection areaSelection;
    public MapType mapType;
    public AreaOption areaOption;
    public PointOption pointOption;

    public enum AreaSelection {

        AREA, CENTER_ON_ALL_PROXIES, CENTER_ON_RELEVANT_PROXIES, CENTER_ON_POINT
    };

    public enum MapType {

        SATELLITE, POLITICAL
    };

    static {
        enumFieldNames.add("areaSelection");
        enumFieldNames.add("mapType");

        enumNameToDescription.put("areaSelection", "What area to show?");
        enumNameToDescription.put("mapType", "What map type to use?");

        enumValueToFieldName.put(AreaSelection.AREA, "areaOption");
        enumValueToFieldName.put(AreaSelection.CENTER_ON_ALL_PROXIES, null);
        enumValueToFieldName.put(AreaSelection.CENTER_ON_POINT, "pointOption");
        enumValueToFieldName.put(AreaSelection.CENTER_ON_RELEVANT_PROXIES, null);
        enumValueToFieldName.put(MapType.POLITICAL, null);
        enumValueToFieldName.put(MapType.SATELLITE, null);
    }

    public RelevantArea() {
    }

    @Override
    public RelevantArea copy() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
