package io.frictionlessdata.tableschema.field;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.frictionlessdata.tableschema.exception.ConstraintsException;
import io.frictionlessdata.tableschema.exception.InvalidCastException;
import io.frictionlessdata.tableschema.exception.TypeInferringException;
import io.frictionlessdata.tableschema.util.JsonUtil;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

public class GeopointField extends Field<double[]> {

    GeopointField(){
        super();
    }

    public GeopointField(String name) {
        super(name, FIELD_TYPE_GEOPOINT);
    }

    public GeopointField(String name, String format, String title, String description,
                         URI rdfType, Map<String, Object> constraints, Map<String, Object> options){
        super(name, FIELD_TYPE_GEOPOINT, format, title, description, rdfType, constraints, options);
    }

    @Override
    public double[] parseValue(String value, String format, Map<String, Object> options)
            throws TypeInferringException {
        try{
            if(format.equalsIgnoreCase(Field.FIELD_FORMAT_DEFAULT)){
                String[] geopoint = value.split(", *");

                if(geopoint.length == 2){
                    double lon = Double.parseDouble(geopoint[0]);
                    double lat = Double.parseDouble(geopoint[1]);

                    // No exception? It's a valid geopoint object.
                    return new double[]{lon, lat};

                }else{
                    throw new TypeInferringException("Geo points must have two coordinates");
                }

            }else if(format.equalsIgnoreCase(Field.FIELD_FORMAT_ARRAY)){

                // This will throw an exception if the value is not an array.
                ArrayNode jsonArray = JsonUtil.getInstance().createArrayNode(value);

                if (jsonArray.size() == 2){
                    double lon = jsonArray.get(0).asDouble();
                    double lat = jsonArray.get(1).asDouble();

                    // No exception? It's a valid geopoint object.
                    return new double[]{lon, lat};

                }else{
                    throw new TypeInferringException("Geo points must have two coordinates");
                }

            }else if(format.equalsIgnoreCase(Field.FIELD_FORMAT_OBJECT)){

                // This will throw an exception if the value is not an object.
                JsonNode jsonObj = JsonUtil.getInstance().createNode(value);

                if (jsonObj.size() == 2 && jsonObj.has("lon") && jsonObj.has("lat")){
                    double lon = jsonObj.get("lon").asDouble();
                    double lat = jsonObj.get("lat").asDouble();

                    // No exception? It's a valid geopoint object.
                    return new double[]{lon, lat};

                }else{
                    throw new TypeInferringException();
                }

            }else{
                throw new TypeInferringException();
            }

        }catch(Exception e){
            if (e instanceof TypeInferringException) {
                throw e;
            }
            throw new TypeInferringException(e);
        }
    }

    @Override
    public String formatValueAsString(double[] value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {
        if ((null == format) || (format.equalsIgnoreCase(Field.FIELD_FORMAT_DEFAULT))){
            return value[0]+","+value[1];
        }else if(format.equalsIgnoreCase(Field.FIELD_FORMAT_ARRAY)){
            return "["+value[0]+","+value[1]+"]";
        } else if(format.equalsIgnoreCase(Field.FIELD_FORMAT_OBJECT)){
            return "{\"lon\": "+value[0]+", \"lat\":"+value[1]+"}";
        }
        return null;
    }

    @Override
    public Object formatValueForJson(double[] value) throws InvalidCastException, ConstraintsException {
        if (null == value)
            return null;
        if ((null == format) || (format.equalsIgnoreCase(Field.FIELD_FORMAT_DEFAULT))){
            return value[0]+","+value[1];
        }else if(format.equalsIgnoreCase(Field.FIELD_FORMAT_ARRAY)){
            return value;
        } else if(format.equalsIgnoreCase(Field.FIELD_FORMAT_OBJECT)){
            Map<String, Double> map = new LinkedHashMap<>();
            map.put("lon", value[0]);
            map.put("lat", value[1]);
            return map;
        }
        return null;
    }

    @Override
    public String parseFormat(String value, Map<String, Object> options) {
        try {
            JsonNode node = JsonUtil.getInstance().createNode(value);
            if(node.isArray()) {
            	return FIELD_FORMAT_ARRAY;
            } else {
            	return FIELD_FORMAT_OBJECT;
            }
        } catch (Exception ex) {
        	return FIELD_FORMAT_DEFAULT;
        }
    }

    @Override
    double[] checkMinimumContraintViolated(double[] value) {
        return null;
    }

}
