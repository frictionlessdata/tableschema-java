package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.exception.ConstraintsException;
import io.frictionlessdata.tableschema.exception.InvalidCastException;

import java.net.URI;
import java.util.Map;

public class AnyField extends Field<Object> {

    AnyField() {
        super();
    }

    public AnyField(String name) {
        super(name, FIELD_TYPE_ANY);
    }

    public AnyField(String name, String format, String title, String description,
                    URI rdfType, Map<String, Object> constraints, Map<String, Object> options){
        super(name, FIELD_TYPE_ANY, format, title, description, rdfType, constraints, options);
    }

    @Override
    public Object parseValue(String value, String format, Map<String, Object> options)
            throws InvalidCastException, ConstraintsException {
        return value;
    }

    @Override
    public String formatValueAsString(Object value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {
        return value.toString();
    }


    @Override
    public String parseFormat(String value, Map<String, Object> options) {
        return "default";
    }

}
