package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.TypeInferrer;
import io.frictionlessdata.tableschema.exceptions.ConstraintsException;
import io.frictionlessdata.tableschema.exceptions.InvalidCastException;
import io.frictionlessdata.tableschema.exceptions.TypeInferringException;
import org.json.JSONObject;

import java.time.Duration;
import java.util.Map;

public class DurationField extends Field<Duration> {

    public DurationField(String name) {
        super(name, FIELD_TYPE_DURATION);
    }

    public DurationField(String name, String format, String title, String description, Map constraints) {
        super(name, FIELD_TYPE_DURATION, format, title, description, constraints);
    }

    public DurationField(JSONObject field) {
        super(field);
        type = FIELD_TYPE_DURATION;
    }

    @Override
    Duration parseValue(String value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {
        try{
            return Duration.parse(value);
        }catch(Exception e){
            throw new TypeInferringException(e);
        }
    }
}
