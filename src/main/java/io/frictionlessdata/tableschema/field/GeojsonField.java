package io.frictionlessdata.tableschema.field;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import io.frictionlessdata.tableschema.exception.*;
import io.frictionlessdata.tableschema.schema.JsonSchema;
import io.frictionlessdata.tableschema.schema.TypeInferrer;
import io.frictionlessdata.tableschema.util.JsonUtil;

import java.io.InputStream;
import java.net.URI;
import java.util.Map;

public class GeojsonField extends Field<JsonNode> {
    private JsonSchema geoJsonSchema = null;
    private JsonSchema topoJsonSchema = null;

    GeojsonField(){
        super();
    }

    public GeojsonField(String name) {
        super(name, FIELD_TYPE_GEOJSON);
    }

    public GeojsonField(String name, String format, String title, String description,
                        URI rdfType, Map<String, Object> constraints, Map<String, Object> options){
        super(name, FIELD_TYPE_GEOJSON, format, title, description, rdfType, constraints, options);
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

        } catch(ValidationException | JsonParsingException ve){
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
            if(this.geoJsonSchema == null){
                // FIXME: Maybe this inferring against geojson scheme is too much.
                // Grabbed geojson schema from here: https://github.com/fge/sample-json-schemas/tree/master/geojson
                InputStream geoJsonSchemaInputStream = TypeInferrer.class.getResourceAsStream("/schemas/geojson/geojson.json");
                geoJsonSchema = JsonSchema.fromJson(geoJsonSchemaInputStream, true);
            }
            geoJsonSchema.validate(json);
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
            if (topoJsonSchema == null) {
                // FIXME: Maybe this infering against topojson scheme is too much.
                // Grabbed topojson schema from here: https://github.com/nhuebel/TopoJSON_schema
                InputStream topoJsonSchemaInputStream = TypeInferrer.class.getResourceAsStream("/schemas/geojson/topojson.json");
                topoJsonSchema = JsonSchema.fromJson(topoJsonSchemaInputStream, true);
            }
            topoJsonSchema.validate(json);
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
    JsonNode checkMinimumContraintViolated(JsonNode value) {
        return null;
    }

}
