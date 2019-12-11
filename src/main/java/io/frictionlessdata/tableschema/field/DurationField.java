package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.exception.ConstraintsException;
import io.frictionlessdata.tableschema.exception.InvalidCastException;
import io.frictionlessdata.tableschema.exception.TypeInferringException;
import org.json.JSONObject;

import java.net.URI;
import java.time.Duration;
import java.util.Map;

public class DurationField extends Field<Duration> {

    DurationField() {
        super();
    }

    public DurationField(String name) {
        super(name, FIELD_TYPE_DURATION);
    }

    public DurationField(String name, String format, String title, String description,
                         URI rdfType, Map constraints, Map options){
        super(name, FIELD_TYPE_DURATION, format, title, description, rdfType, constraints, options);
    }

    @Override
    public Duration parseValue(String value, String format, Map<String, Object> options)
            throws InvalidCastException, ConstraintsException {
        try{
            return Duration.parse(value);
        }catch(Exception e){
            throw new TypeInferringException(e);
        }
    }

    @Override
    public String formatValue(Duration value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {
        return value.toString();
    }


    @Override
    public String parseFormat(String value, Map<String, Object> options) {
        return "default";
    }
}
