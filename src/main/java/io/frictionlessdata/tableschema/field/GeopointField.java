package io.frictionlessdata.tableschema.field;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.frictionlessdata.tableschema.exception.ConstraintsException;
import io.frictionlessdata.tableschema.exception.InvalidCastException;
import io.frictionlessdata.tableschema.exception.TypeInferringException;
import io.frictionlessdata.tableschema.util.JsonUtil;
import org.locationtech.jts.geom.Coordinate;

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
                         URI rdfType, Map<String, Object> constraints, Map<String, Object> options, String example){
        super(name, FIELD_TYPE_GEOPOINT, format, title, description, rdfType, constraints, options, example);
    }

    @Override
    public double[] parseValue(String value, String format, Map<String, Object> options)
            throws TypeInferringException {
        try{
            if (format.equalsIgnoreCase(Field.FIELD_FORMAT_DEFAULT)){
                return parseDefaultString(value);
            } else if (format.equalsIgnoreCase(Field.FIELD_FORMAT_ARRAY)){
                return parseArrayString(value);
            } else if(format.equalsIgnoreCase(Field.FIELD_FORMAT_OBJECT)) {
                return parseObjectString(value);
            }
        } catch(Exception e){
            if (e instanceof TypeInferringException) {
                throw e;
            }
            throw new TypeInferringException(e);
        }
        throw new TypeInferringException("Invalid format for geopoint field: " + format);
    }

    @Override
    public boolean isCompatibleValue(String value, String format) {
        try {
            parseDefaultString(value);
            return true;
        } catch (Exception ex) {
            try {
                parseArrayString(value);
                return true;
            } catch (Exception ex2) {
                try {
                    parseObjectString(value);
                    return true;
                } catch (Exception ex3) {
                    return false;
                }
            }
        }
    }

    private static double[] parseDefaultString(String value) throws TypeInferringException {
        String[] geopoint = value.split(", *");

        if(geopoint.length == 2){
            double lon = Double.parseDouble(geopoint[0]);
            double lat = Double.parseDouble(geopoint[1]);

            // No exception? It's a valid geopoint object.
            return new double[]{lon, lat};

        }else{
            throw new TypeInferringException("Geo points must have two coordinates");
        }
    }

    private static double[] parseArrayString(String value) throws TypeInferringException {
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
    }

    private static double[] parseObjectString(String value) throws TypeInferringException {
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
    String formatObjectValueAsString(Object value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {
        if (value instanceof Coordinate) {
            Coordinate coor = (Coordinate)value;
            double[] vals = new double[]{coor.x, coor.y, coor.z};
            return formatValueAsString(vals, format,options);
        }
        return value.toString();
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
    double[] checkMinimumConstraintViolated(double[] value) {
        return null;
    }

}
