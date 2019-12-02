package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.TypeInferrer;
import io.frictionlessdata.tableschema.exceptions.ConstraintsException;
import io.frictionlessdata.tableschema.exceptions.InvalidCastException;
import org.json.JSONObject;

import java.util.Map;

public class GeoPointField extends Field<int[]> {

    public GeoPointField(String name) {
        super(name, FIELD_TYPE_GEOPOINT);
    }

    public GeoPointField(String name, String format, String title, String description, Map constraints) {
        super(name, FIELD_TYPE_GEOPOINT, format, title, description, constraints);
    }

    public GeoPointField(JSONObject field) {
        super(field);
        type = FIELD_TYPE_GEOPOINT;
    }

    @Override
    int[] getCastValue(String value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {
        return TypeInferrer.getInstance().castGeopoint(format, value, options);
    }
}
