package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.TypeInferrer;
import io.frictionlessdata.tableschema.exceptions.ConstraintsException;
import io.frictionlessdata.tableschema.exceptions.InvalidCastException;
import org.json.JSONObject;

import java.util.Map;

public class StringField extends Field<String> {

    StringField() {
        super();
    }

    public StringField(String name) {
        super(name, FIELD_TYPE_STRING);
    }

    public StringField(String name, String format, String title, String description, Map constraints) {
        super(name, FIELD_TYPE_STRING, format, title, description, constraints);
    }

    public StringField(JSONObject field) {
        super(field);
        type = FIELD_TYPE_STRING;
    }

    @Override
    public String parseValue(String value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {
        return value;
    }
}
