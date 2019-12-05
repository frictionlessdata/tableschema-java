package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.exception.ConstraintsException;
import io.frictionlessdata.tableschema.exception.InvalidCastException;
import org.json.JSONObject;

import java.net.URI;
import java.util.Map;

public class ObjectField extends Field<JSONObject> {

    ObjectField() {
        super();
    }

    public ObjectField(String name) {
        super(name, FIELD_TYPE_OBJECT);
    }

    public ObjectField(String name, String format, String title, String description,
                       URI rdfType, Map constraints, Map options){
        super(name, FIELD_TYPE_OBJECT, format, title, description, rdfType, constraints, options);
    }

    @Override
    public JSONObject parseValue(String value, String format, Map<String, Object> options)
            throws InvalidCastException, ConstraintsException {
        return new JSONObject(value);
    }

    @Override
    public String parseFormat(String value, Map<String, Object> options) {
        return "default";
    }
}
