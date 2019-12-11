package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.exception.ConstraintsException;
import io.frictionlessdata.tableschema.exception.InvalidCastException;
import io.frictionlessdata.tableschema.exception.TypeInferringException;
import org.everit.json.schema.ValidationException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.Map;

public class GeopointField extends Field<double[]> {

    GeopointField(){
        super();
    }

    public GeopointField(String name) {
        super(name, FIELD_TYPE_GEOPOINT);
    }

    public GeopointField(String name, String format, String title, String description,
                         URI rdfType, Map constraints, Map options){
        super(name, FIELD_TYPE_GEOPOINT, format, title, description, rdfType, constraints, options);
    }

    @Override
    public double[] parseValue(String value, String format, Map<String, Object> options)
            throws InvalidCastException, ConstraintsException {
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
                JSONArray jsonArray = new JSONArray(value);

                if (jsonArray.length() == 2){
                    double lon = jsonArray.getDouble(0);
                    double lat = jsonArray.getDouble(1);

                    // No exception? It's a valid geopoint object.
                    return new double[]{lon, lat};

                }else{
                    throw new TypeInferringException("Geo points must have two coordinates");
                }

            }else if(format.equalsIgnoreCase(Field.FIELD_FORMAT_OBJECT)){

                // This will throw an exception if the value is not an object.
                JSONObject jsonObj = new JSONObject(value);

                if (jsonObj.length() == 2 && jsonObj.has("lon") && jsonObj.has("lat")){
                    double lon = jsonObj.getDouble("lon");
                    double lat = jsonObj.getDouble("lat");

                    // No exception? It's a valid geopoint object.
                    return new double[]{lon, lat};

                }else{
                    throw new TypeInferringException();
                }

            }else{
                throw new TypeInferringException();
            }

        }catch(Exception e){
            throw new TypeInferringException(e);
        }
    }

    @Override
    public String formatValue(double[] value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {
        if ((null == format) || (format.equalsIgnoreCase(Field.FIELD_FORMAT_DEFAULT))){
            return value[0]+","+value[1];
        }else if(format.equalsIgnoreCase(Field.FIELD_FORMAT_ARRAY)){
            return "["+value[0]+","+value[1]+"]";
        } else if(format.equalsIgnoreCase(Field.FIELD_FORMAT_OBJECT)){
            return "{\"lat\": "+value[0]+", \"lon\":"+value[1]+"}";
        }
        return null;
    }

    @Override
    public String parseFormat(String value, Map<String, Object> options) {
        try {
            new JSONObject(value);
            return FIELD_FORMAT_OBJECT;
        } catch (JSONException ex) {
            try {
                new JSONArray(value);
                return FIELD_FORMAT_ARRAY;
            } catch (JSONException ex2) {
                return FIELD_FORMAT_DEFAULT;
            }
        }
    }

}
