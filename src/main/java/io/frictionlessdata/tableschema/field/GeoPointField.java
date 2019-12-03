package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.TypeInferrer;
import io.frictionlessdata.tableschema.exceptions.ConstraintsException;
import io.frictionlessdata.tableschema.exceptions.InvalidCastException;
import io.frictionlessdata.tableschema.exceptions.TypeInferringException;
import org.json.JSONArray;
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
    public int[] parseValue(String value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {
        try{
            if(format.equalsIgnoreCase(io.frictionlessdata.tableschema.field.Field.FIELD_FORMAT_DEFAULT)){
                String[] geopoint = value.split(",");

                if(geopoint.length == 2){
                    int lon = Integer.parseInt(geopoint[0]);
                    int lat = Integer.parseInt(geopoint[1]);

                    // No exceptions? It's a valid geopoint object.
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

                    // No exceptions? It's a valid geopoint object.
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

                    // No exceptions? It's a valid geopoint object.
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
}
