package io.frictionlessdata.tableschema;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;


/**
 *
 * 
 */
public class TypeInferer {
    
    private Schema geoJsonSchema = null;
    private Schema topoJsonSchema = null;
    
    public enum Type {
        DURATION, GEOJSON, GEOPOINT, OBJECT,
        ARRAY, DATETIME, TIME, DATE,
        INTEGER, NUMBER, BOOLEAN, STRING, ANY 
    }
    
    private static final String REGEX_DURATION = "(-?(?:[1-9][0-9]*)?[0-9]{4})-(1[0-2]|0[1-9])-(3[01]|0[1-9]|[12][0-9])(T(2[0-3]|[01][0-9]):([0-5][0-9]):([0-5][0-9])(\\.[0-9]+)?(Z|[+-](?:2[0-3]|[01][0-9]):[0-5][0-9])?)?";
      
    public TypeInferer(){
        // Grabbed geojson schema from here: https://github.com/fge/sample-json-schemas/tree/master/geojson
        InputStream geoJsonSchemaInputStream = TypeInferer.class.getResourceAsStream("/schemas/geojson/geojson.json");
        JSONObject rawGeoJsonSchema = new JSONObject(new JSONTokener(geoJsonSchemaInputStream));
        this.geoJsonSchema = SchemaLoader.load(rawGeoJsonSchema);
        
        // Grabbed topojson schema from here: https://github.com/nhuebel/TopoJSON_schema
        InputStream topoJsonSchemaInputStream = TypeInferer.class.getResourceAsStream("/schemas/geojson/topojson.json");
        JSONObject rawTopoJsonSchema = new JSONObject(new JSONTokener(topoJsonSchemaInputStream));
        this.topoJsonSchema = SchemaLoader.load(rawTopoJsonSchema);
    }
    
    public JSONObject infer(String[] headers, List<String[]> data, int rowLimit){
        Map<String, Map<String, Integer>> typeInferralMap = new HashMap();
        
        for(int i = 0; i < headers.length; i++){
            
        }
        
        for(int j = 0; j < rowLimit; j++){
            String[] row = data.get(i);
        }
        return null;
    }
    
    public String findType(){
        
    }
    
    /**
     * Using regex only tests the pattern.
     * Unfortunately, this approach does not test the validity of the date value itself.
     * @param value
     * @return 
     */
    public boolean castDuration(String value){
        Pattern pattern = Pattern.compile(REGEX_DURATION);
        Matcher matcher = pattern.matcher(value);
        
        return matcher.matches();
    }
    
    /**
     * /**
     * Validate against GeoJSON or TopoJSON schema.
     * @param format can be either default or topojson. Default is geojson.
     * @param value
     * @return
     * @throws TypeInferringException 
     */
    public JSONObject castGeoJson(String format, String value) throws TypeInferringException{
        JSONObject jsonObj = null;
        
        try {
            jsonObj = new JSONObject(value);
        
            try{
                if(format.equalsIgnoreCase("default")){
                    this.geoJsonSchema.validate(jsonObj);

                }else if(format.equalsIgnoreCase("topojson")){
                    this.topoJsonSchema.validate(jsonObj);

                }else{
                    throw new TypeInferringException();
                }

            }catch(ValidationException ve){
                // Not a valid GeoJSON or TopoJSON.
                throw new TypeInferringException();
            }
        }catch(JSONException je){
            // Not a valid JSON.
            throw new TypeInferringException();
        }
        
        return jsonObj;
    }
    
    /**
     * Only validates against pattern.
     * Does not validate against min/max brackets -180:180 for lon and -90:90 for lat.
     * @param format can be either default, array, or object.
     * @param value
     * @return 
     * @throws TypeInferringException
     */
    public Integer[] castGeoPoint(String format, String value) throws TypeInferringException{
        try{
            if(format.equalsIgnoreCase("default")){
                String[] geopoint = value.split(",");

                if(geopoint.length == 2){
                    int lon = Integer.parseInt(geopoint[0]);
                    int lat = Integer.parseInt(geopoint[1]);
                    
                    // No exceptions? It's a valid geopoint object.
                    return new Integer[]{lon, lat};
                    
                }else{
                    throw new TypeInferringException();
                }

            }else if(format.equalsIgnoreCase("array")){

                // This will throw an exception if the value is not an array.
                JSONArray jsonArray = new JSONArray(value);
                
                if (jsonArray.length() == 2){
                    int lon = jsonArray.getInt(0);
                    int lat = jsonArray.getInt(1);

                    // No exceptions? It's a valid geopoint object.
                    return new Integer[]{lon, lat};

                }else{
                    throw new TypeInferringException();
                }     

            }else if(format.equalsIgnoreCase("object")){

                // This will throw an exception if the value is not an object.
                JSONObject jsonObj = new JSONObject(value);
                
                if (jsonObj.length() == 2 && jsonObj.has("lon") && jsonObj.has("lat")){
                    int lon = jsonObj.getInt("lon");
                    int lat = jsonObj.getInt("lat");

                    // No exceptions? It's a valid geopoint object.
                    return new Integer[]{lon, lat};

                }else{
                    throw new TypeInferringException();
                }

            }else{
                throw new TypeInferringException();
            }

        }catch(Exception e){
            throw new TypeInferringException();
        }
    }
    
    
    public JSONObject castObject(String value) throws TypeInferringException{
        try {
            return new JSONObject(value);
        }catch(JSONException je){
            throw new TypeInferringException();
        }       
    }
    
    public JSONArray castArray(String value) throws TypeInferringException{
        try {
            return new JSONArray(value);
        }catch(JSONException je){
            throw new TypeInferringException();
        } 
    }
    
    public boolean castDatetime(String value){
        return false;
    }
    
    public boolean castTime(String value){
        return false;
    }
    
    public boolean castDate(String value){
        return false;
    }
    
    public int castInteger(String value) throws TypeInferringException{
        try{
            return Integer.parseInt(value);
        }catch(NumberFormatException nfe){
            throw new TypeInferringException();
        }
    }
    
    public boolean castNumber(String value){
        return false;
    }
    
    public boolean castBoolean(String value) throws TypeInferringException{
        if(Arrays.asList(new String[]{"yes", "y", "true", "t", "1"}).contains(value.toLowerCase())){
            return true;
            
        }else if(Arrays.asList(new String[]{"no", "n", "false", "f", "0"}).contains(value.toLowerCase())){
            return false;
            
        }else{
            throw new TypeInferringException();
        }
    }
    
    /**
     * 
     * @param format can be either default, e-mail, uri, binary, or uuid.
     * @param value
     * @return
     * @throws TypeInferringException 
     */
    public String castString(String format, String value) throws TypeInferringException{
        return value;
    }
    
    public String isAny(String value) throws TypeInferringException{
        return value;
    }


}
