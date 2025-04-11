package io.frictionlessdata.tableschema.field;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ValidationMessage;
import io.frictionlessdata.tableschema.exception.*;
import io.frictionlessdata.tableschema.schema.FormalSchemaValidator;
import io.frictionlessdata.tableschema.schema.TypeInferrer;
import io.frictionlessdata.tableschema.util.JsonUtil;

import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.Set;

public class GeojsonField extends Field<JsonNode> {
    private static FormalSchemaValidator geoFormalSchemaValidator;
    private static FormalSchemaValidator topoFormalSchemaValidator;

    static {
        // FIXME: Maybe this inferring against geojson and topojson scheme is too much.
        // Grabbed geojson schema from here: https://github.com/fge/sample-json-schemas/tree/master/geojson
        InputStream geoJsonSchemaInputStream = TypeInferrer.class.getResourceAsStream("/schemas/geojson-schema/geojson.json");
        geoFormalSchemaValidator = FormalSchemaValidator.fromJson(geoJsonSchemaInputStream);
        // Grabbed topojson schema from here: https://github.com/nhuebel/TopoJSON_schema
        InputStream topoJsonSchemaInputStream = TypeInferrer.class.getResourceAsStream("/schemas/topojson-schema/topojson.json");
        topoFormalSchemaValidator = FormalSchemaValidator.fromJson(topoJsonSchemaInputStream);
    }

    GeojsonField(){
        super();
    }

    public GeojsonField(String name) {
        super(name, FIELD_TYPE_GEOJSON);
    }

    public GeojsonField(String name, String format, String title, String description,
                        URI rdfType, Map<String, Object> constraints, Map<String, Object> options, String example){
        super(name, FIELD_TYPE_GEOJSON, format, title, description, rdfType, constraints, options, example);
    }

    @Override
    public JsonNode parseValue(String value, String format, Map<String, Object> options)
            throws TypeInferringException {
        try{
            if(format.equalsIgnoreCase(FIELD_FORMAT_DEFAULT)){
                validateGeoJsonSchema(value);

            }else if(format.equalsIgnoreCase(FIELD_FORMAT_TOPOJSON)){
                validateTopoJsonSchema(value);

            }else{
                throw new TypeInferringException("Unknown format type");
            }

        } catch (ValidationException ve){
            // Not a valid GeoJSON or TopoJSON or Not a valid JSON.
            throw new TypeInferringException(ve);
        }
        return JsonUtil.getInstance().readValue(value);
    }

    @Override
    public String formatValueAsString(JsonNode value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {
        if (null == value)
            return null;
        return JsonUtil.getInstance().serialize(value);
    }


    @Override
    String formatObjectValueAsString(Object value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {
        return value.toString();
    }

    /**
     * We only want to go through this initialization if we have to because it's a
     * performance issue the first time it is executed.
     * Because of this, so we don't include this logic in the constructor and only
     * call it when it is actually required after trying all other type inferral.
     * @param json String-encoded JSON object
     * @throws ValidationException if validation fails
     */
    private void validateGeoJsonSchema(String json) throws ValidationException {
        try {
            Set<ValidationMessage> errors = geoFormalSchemaValidator.validate(json);
            if (!errors.isEmpty()) {
                throw new ValidationException("Geojson field validation failed", errors);
            }
        } catch (JsonParsingException ex) {
            throw new ValidationException(ex);
        }
    }


    /**
     * We only want to go through this initialization if we have to because it's a
     * performance issue the first time it is executed.
     * Because of this, so we don't include this logic in the constructor and only
     * call it when it is actually required after trying all other type inferral.
     * @param json String-encoded JSON object
     */
    private void validateTopoJsonSchema(String json) throws ValidationException {
        try {
            Set<ValidationMessage> errors = topoFormalSchemaValidator.validate(json);
            if (!errors.isEmpty()) {
                throw new ValidationException("Topojson field validation failed", errors);
            }
        } catch (JsonParsingException ex) {
            throw new ValidationException(ex);
        }
    }

    /*
     try to parse both formats, suppress exceptions
     */
    @Override
    public String parseFormat(String json, Map<String, Object> options) {
        try {
            validateGeoJsonSchema(json);
            return FIELD_TYPE_GEOJSON;
        } catch (ValidationException ex) {
            try {
                validateTopoJsonSchema(json);
                return FIELD_FORMAT_TOPOJSON;
            } catch (ValidationException ex1) {
                return "default";
            }
        }
    }

    @Override
    JsonNode checkMinimumConstraintViolated(JsonNode value) {
        return null;
    }

}
