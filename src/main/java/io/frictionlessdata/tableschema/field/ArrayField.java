package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.exception.ConstraintsException;
import io.frictionlessdata.tableschema.exception.InvalidCastException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.Map;

public class ArrayField extends Field<Object[]> {

    ArrayField() {
        super();
    }

    public ArrayField(String name) {
        super(name, FIELD_TYPE_ARRAY);
    }

    public ArrayField(String name, String format, String title, String description,
                      URI rdfType, Map constraints, Map options){
        super(name, FIELD_TYPE_ARRAY, format, title, description, rdfType, constraints, options);
    }

    @Override
    public Object[] parseValue(String value, String format, Map<String, Object> options)
            throws InvalidCastException, ConstraintsException {
        Object[] retVal;
        try {
            JSONArray arr = new JSONArray(value);
            retVal = arr.toList().toArray();
        } catch (JSONException ex) {
            throw new InvalidCastException(ex);
        }
        return retVal;
    }

    @Override
    public String formatValueAsString(Object[] value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {
        return value.toString();
    }


    @Override
    public String parseFormat(String value, Map<String, Object> options) {
        return "default";
    }

}
