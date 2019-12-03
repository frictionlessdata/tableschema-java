package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.TypeInferrer;
import io.frictionlessdata.tableschema.exceptions.ConstraintsException;
import io.frictionlessdata.tableschema.exceptions.InvalidCastException;
import io.frictionlessdata.tableschema.exceptions.TypeInferringException;
import org.everit.json.schema.ValidationException;
import org.json.JSONException;
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
        JSONObject jsonObj = null;

        try {
            jsonObj = new JSONObject(value);
            try{
                if(format.equalsIgnoreCase(io.frictionlessdata.tableschema.field.Field.FIELD_FORMAT_DEFAULT)){
                    validateGeoJsonSchema(jsonObj);

                }else if(format.equalsIgnoreCase(io.frictionlessdata.tableschema.field.Field.FIELD_FORMAT_TOPOJSON)){
                    validateTopoJsonSchema(jsonObj);

                }else{
                    throw new TypeInferringException("Unknown format type");
                }

            }catch(ValidationException ve){
                // Not a valid GeoJSON or TopoJSON.
                throw new TypeInferringException(ve);
            }
        }catch(JSONException je){
            // Not a valid JSON.
            throw new TypeInferringException(je);
        }

        return jsonObj;
    }
}
