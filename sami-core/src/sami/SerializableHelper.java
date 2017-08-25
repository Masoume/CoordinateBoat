package sami;

import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 *
 * @author nbb
 */
public class SerializableHelper {

    private static final Logger LOGGER = Logger.getLogger(SerializableHelper.class.getName());
    private static OutputStream testSink;
    private static ObjectOutputStream testStream;

    public static HashMap<String, Object> serialToTransient(String className, HashMap<String, Object> serialLookup) {
        HashMap<String, Object> transientLookup = new HashMap<String, Object>();
        for (String fieldName : serialLookup.keySet()) {
            transientLookup.put(fieldName, serialLookup.get(fieldName));
        }
        return transientLookup;
    }

    public static HashMap<String, Object> transientToSerial(HashMap<String, Object> transientLookup) {
        //@todo need to decide if we should have to handle non-serializable classes or not - 
        //  this will handle some cases of non-serializable classes, but for the most part I 
        //  am just making the datamodel classes serializable
        HashMap<String, Object> serialLookup = new HashMap<String, Object>();
        for (String fieldName : transientLookup.keySet()) {
            Object object = transientLookup.get(fieldName);
            if (isSerializable(object)) {
                // If it is serializable, just copy the entry
                serialLookup.put(fieldName, object);
            } else {
                LOGGER.severe("Field named \"" + fieldName + "\" is not serializable!");
//                // If it is not serializable, make a hashmap containing each of the field's fields
//                HashMap<String, Object> subTransientLookup = new HashMap<String, Object>();
//                for (Field subField : object.getClass().getDeclaredFields()) {
//                    if (!subField.isAccessible()) {
//                        subField.setAccessible(true);
//                    }
//                    String subFieldName = subField.getName();
//                    try {
//                        subTransientLookup.put(subFieldName, subField.get(object));
//                    } catch (IllegalAccessException iae) {
//                        iae.printStackTrace();
//                    }
//                }
//                HashMap<String, Object> subSerialLookup = transientToSerial(subTransientLookup);
//                serialLookup.put(fieldName, subSerialLookup);
            }
        }
        return serialLookup;
    }

    public static boolean isSerializable(final Object o) {
        boolean ret = false;
        try {
            if (o == null) {
                ret = true;
            } else if (o.getClass().isPrimitive()) {
                ret = true;
            } else if (o instanceof Serializable || o instanceof Externalizable) {
                if (testSink == null) {
                    testSink = new ByteArrayOutputStream();
                }
                testStream = new ObjectOutputStream(testSink);
                testStream.writeObject(o);
                // If we are here, there were no exceptions and this is serializable
                ret = true;
            }
        } catch (IOException io) {
            //io.printStackTrace();
        }
        // Shut down stream
        if (testStream != null) {
            try {
                testStream.close();
            } catch (IOException ex) {
            }
        }
        return ret;
    }
}
