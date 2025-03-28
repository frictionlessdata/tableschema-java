package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.exception.ConstraintsException;
import io.frictionlessdata.tableschema.exception.InvalidCastException;
import io.frictionlessdata.tableschema.exception.TypeInferringException;

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
                    URI rdfType, Map<String, Object> constraints, Map<String, Object> options, String example){
        super(name, FIELD_TYPE_ANY, format, title, description, rdfType, constraints, options, example);
    }

    @Override
    public boolean isCompatibleValue(String value, String format) {
        return true;
    }

    @Override
    public Object parseValue(String value, String format, Map<String, Object> options)
            throws TypeInferringException {
        return value;
    }

    @Override
    public String formatValueAsString(Object value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {
        return value.toString();
    }

    @Override
    String formatObjectValueAsString(Object value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {
        return value.toString();
    }


    @Override
    public String parseFormat(String value, Map<String, Object> options) {
        return "default";
    }

    @Override
    Object checkMinimumContraintViolated(Object value) {
        return null;
    }

}
