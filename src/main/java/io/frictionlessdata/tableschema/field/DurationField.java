package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.exception.ConstraintsException;
import io.frictionlessdata.tableschema.exception.InvalidCastException;
import io.frictionlessdata.tableschema.exception.TypeInferringException;
import org.json.JSONObject;

import java.time.Duration;
import java.util.Map;

public class DurationField extends Field<Duration> {

    DurationField() {
        super();
    }

    public DurationField(String name) {
        super(name, FIELD_TYPE_DURATION);
    }

    public DurationField(String name, String format, String title, String description, Map constraints, Map options){
        super(name, FIELD_TYPE_DURATION, format, title, description, constraints, options);
    }

    public DurationField(JSONObject field) {
        super(field);
        type = FIELD_TYPE_DURATION;
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
}
