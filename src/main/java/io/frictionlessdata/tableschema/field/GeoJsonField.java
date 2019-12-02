package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.TypeInferrer;
import io.frictionlessdata.tableschema.exceptions.ConstraintsException;
import io.frictionlessdata.tableschema.exceptions.InvalidCastException;
import org.json.JSONObject;

import java.util.Map;

public class GeoJsonField extends Field<JSONObject> {

    public GeoJsonField(String name) {
        super(name, FIELD_TYPE_GEOJSON);
    }

    public GeoJsonField(String name, String format, String title, String description, Map constraints) {
        super(name, FIELD_TYPE_GEOJSON, format, title, description, constraints);
    }

    public GeoJsonField(JSONObject field) {
        super(field);
        type = FIELD_TYPE_GEOJSON;
    }

    @Override
    JSONObject getCastValue(String value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {
        return TypeInferrer.getInstance().castGeojson(format, value, options);
    }
}
