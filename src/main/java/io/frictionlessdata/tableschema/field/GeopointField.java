package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.exception.ConstraintsException;
import io.frictionlessdata.tableschema.exception.InvalidCastException;
import io.frictionlessdata.tableschema.exception.TypeInferringException;
import org.everit.json.schema.ValidationException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class GeopointField extends Field<int[]> {

    GeopointField(){
        super();
    }

    public GeopointField(String name) {
        super(name, FIELD_TYPE_GEOPOINT);
    }

    public GeopointField(String name, String format, String title, String description, Map constraints, Map options){
        super(name, FIELD_TYPE_GEOPOINT, format, title, description, constraints, options);
    }

    @Override
    public int[] parseValue(String value, String format, Map<String, Object> options)
            throws InvalidCastException, ConstraintsException {
        try{
            if(format.equalsIgnoreCase(io.frictionlessdata.tableschema.field.Field.FIELD_FORMAT_DEFAULT)){
                String[] geopoint = value.split(",");

                if(geopoint.length == 2){
                    int lon = Integer.parseInt(geopoint[0]);
                    int lat = Integer.parseInt(geopoint[1]);

                    // No exception? It's a valid geopoint object.
                    return new int[]{lon, lat};

                }else{
                    throw new TypeInferringException("Geo points must have two coordinates");
                }

            }else if(format.equalsIgnoreCase(io.frictionlessdata.tableschema.field.Field.FIELD_FORMAT_ARRAY)){

                // This will throw an exception if the value is not an array.
                JSONArray jsonArray = new JSONArray(value);

                if (jsonArray.length() == 2){
                    int lon = jsonArray.getInt(0);
                    int lat = jsonArray.getInt(1);

                    // No exception? It's a valid geopoint object.
                    return new int[]{lon, lat};

                }else{
                    throw new TypeInferringException("Geo points must have two coordinates");
                }

            }else if(format.equalsIgnoreCase(Field.FIELD_FORMAT_OBJECT)){

                // This will throw an exception if the value is not an object.
                JSONObject jsonObj = new JSONObject(value);

                if (jsonObj.length() == 2 && jsonObj.has("lon") && jsonObj.has("lat")){
                    int lon = jsonObj.getInt("lon");
                    int lat = jsonObj.getInt("lat");

                    // No exception? It's a valid geopoint object.
                    return new int[]{lon, lat};

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
