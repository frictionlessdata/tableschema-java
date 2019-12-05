package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.exception.ConstraintsException;
import io.frictionlessdata.tableschema.exception.InvalidCastException;
import org.json.JSONObject;

import java.util.Map;

public class AnyField extends Field<Object> {

    AnyField() {
        super();
    }

    public AnyField(String name) {
        super(name, FIELD_TYPE_ANY);
    }

    public AnyField(String name, String format, String title, String description, Map constraints, Map options){
        super(name, FIELD_TYPE_ANY, format, title, description, constraints, options);
    }

    @Override
    public Object parseValue(String value, String format, Map<String, Object> options)
            throws InvalidCastException, ConstraintsException {
        return value;
    }

    @Override
    public String parseFormat(String value, Map<String, Object> options) {
        return "default";
    }
}
