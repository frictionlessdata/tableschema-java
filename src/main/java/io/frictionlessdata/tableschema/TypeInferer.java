package io.frictionlessdata.tableschema;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.io.InputStream;
import java.time.Duration;
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
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeMap;


/**
 *
 * 
 */
public class TypeInferer {
    
    private Schema geoJsonSchema = null;
    private Schema topoJsonSchema = null;
    private JSONObject tableSchema = null;
    
    private Map<String, Map<String, Integer>> typeInferralMap = new HashMap();
    
    // The order in which the types will be attempted to be inferred.
    // Once a type is successfully inferred, we do not bother with the remaining types.
    private static final List<String[]> TYPE_INFERRAL_ORDER_LIST = new ArrayList<>(Arrays.asList(
        new String[]{"duration", "default"}, // No different formats, just use default.
        new String[]{"geojson", "default"},
        new String[]{"geojson", "topojson"},
        new String[]{"geopoint", "default"},
        new String[]{"geopoint", "array"},
        new String[]{"geopoint", "object"},
        new String[]{"object", "default"},
        new String[]{"array", "default"},
        new String[]{"datetime", "default"}, // No different formats, just use default.
        new String[]{"time", "default"}, // No different formats, just use default.
        new String[]{"date", "default"}, // No different formats, just use default.
        new String[]{"integer", "default"}, // No different formats, just use default.
        new String[]{"number", "default"}, // No different formats, just use default.
        new String[]{"boolean", "default"}, // No different formats, just use default.
        new String[]{"string", "default"}, // No different formats, just use default.
        new String[]{"any", "default"})); // No different formats, just use default.
    

    private static final String REGEX_DURATION = "(-?(?:[1-9][0-9]*)?[0-9]{4})-(1[0-2]|0[1-9])-(3[01]|0[1-9]|[12][0-9])(T(2[0-3]|[01][0-9]):([0-5][0-9]):([0-5][0-9])(\\.[0-9]+)?(Z|[+-](?:2[0-3]|[01][0-9]):[0-5][0-9])?)?";
      
    public TypeInferer(){
        
        // Grabbed geojson schema from here: https://github.com/fge/sample-json-schemas/tree/master/geojson
        InputStream geoJsonSchemaInputStream = TypeInferer.class.getResourceAsStream("/schemas/geojson/geojson.json");
        JSONObject rawGeoJsonSchema = new JSONObject(new JSONTokener(geoJsonSchemaInputStream));
        //this.geoJsonSchema = SchemaLoader.load(rawGeoJsonSchema);
        
        // Grabbed topojson schema from here: https://github.com/nhuebel/TopoJSON_schema
        InputStream topoJsonSchemaInputStream = TypeInferer.class.getResourceAsStream("/schemas/geojson/topojson.json");
        JSONObject rawTopoJsonSchema = new JSONObject(new JSONTokener(topoJsonSchemaInputStream));
        //this.topoJsonSchema = SchemaLoader.load(rawTopoJsonSchema);
    }
    
    /**
     * Infer the data types and return the generated schema.
     * @param data
     * @param headers
     * @return
     * @throws TypeInferringException 
     */
    public JSONObject infer(List<String[]> data, String[] headers) throws TypeInferringException{
        return this.infer(data, headers, data.size()-1);
    }
    
    /**
     * Infer the data types and return the generated schema.
     * @param data
     * @param headers
     * @param rowLimit
     * @return
     * @throws TypeInferringException 
     */
    public JSONObject infer(List<String[]> data, String[] headers, int rowLimit) throws TypeInferringException{
        
        // If the given row limit is bigger than the length of the data
        // then just use the length of the data.
        if(rowLimit > data.size()-1){
            rowLimit = data.size()-1;
        }

        // The JSON Array that will define the fields in the schema JSON Object.
        JSONArray tableFieldJsonArray = new JSONArray();
        
        // Init the type inferral map and init the schema objects
        for (String header : headers) {
            // Init the type inferral map to track our inferences for each row.
            this.typeInferralMap.put(header, new HashMap());
            
            // Init the schema objects
            JSONObject fieldObj = new JSONObject();
            fieldObj.put("name", header);
            fieldObj.put("title", ""); // This will stay blank.
            fieldObj.put("type", ""); // We wil set this after type inferring is complete.
            
            // Wrap it all in an array.
            tableFieldJsonArray.put(fieldObj);
        }

        // Find the type for each column data for each row.
        // This uses method invokation via reflection in a foor loop that iterates
        // for each possible type/format combo. Insprect the findType method for implementation.
        for(int i = 1; i <= rowLimit; i++){
            String[] row = data.get(i);
            
            for(int j = 0; j < row.length; j++){
                this.findType(headers[j], row[j]);
            }
        }
        
        // We are done inferring types.
        // Now for each field we figure out which type was the most inferred and settle for that type
        // as the final type for the field.
        for(int j=0; j < tableFieldJsonArray.length(); j++){
            String fieldName = tableFieldJsonArray.getJSONObject(j).getString("name");
            HashMap<String, Integer> typeInferralCountMap = (HashMap<String, Integer>)this.typeInferralMap.get(fieldName);
            TreeMap<String, Integer> typeInferralCountMapSortedByCount = sortMapByValue(typeInferralCountMap); 
           
            if(!typeInferralCountMapSortedByCount.isEmpty()){
                String inferredType = typeInferralCountMapSortedByCount.firstEntry().getKey();
                tableFieldJsonArray.getJSONObject(j).put("type", inferredType);
            }
            
        }
        
        // Now that the types have been inferred and set, we build and return the schema object.
        JSONObject schemaJsonObject = new JSONObject();
        schemaJsonObject.put("fields", tableFieldJsonArray);
        
        return schemaJsonObject;
    }
    
    private void findType(String header, String datum){
        
        for(String[] typeInferralDefinition: TYPE_INFERRAL_ORDER_LIST){
            try{
                String dataType = typeInferralDefinition[0];
                String castMethodName = "cast" + (dataType.substring(0, 1).toUpperCase() + dataType.substring(1));
                String format = typeInferralDefinition[1];
                 
                Method method = TypeInferer.class.getMethod(castMethodName, String.class, String.class);
                method.invoke(new TypeInferer(), format, datum);
                
                // If no exception is thrown, in means that a type has been inferred.
                // Let's keep track of it in the inferral map.
                this.updateInferralMap(header, dataType);
                
                // We no longer need to try to infer other types.
                // Let's break out of the loop.
                break;

            }catch (Exception e) {
                // Do nothing.
                // An exception here means that we failed to infer with the current type.
                // Move on to attempt with the next type in the following iteration.
            }
        }
    }
    
    /**
     * The type inferral map is where we keep track of the types inferred for values within the same field.
     * @param header
     * @param typeKey 
     */
    private void updateInferralMap(String header, String typeKey){
        if(this.typeInferralMap.get(header).containsKey(typeKey)){
            int newCount = this.typeInferralMap.get(header).get(typeKey) + 1;
            this.typeInferralMap.get(header).replace(typeKey, newCount);
        }else{
            this.typeInferralMap.get(header).put(typeKey, 1);
        }
    }
    
    /**
     * We use a map to keep track the inferred type counts for each field.
     * Once we are done inferring, we settle for the type with that was inferred the most for the same field.
     * @param map
     * @return 
     */
    private TreeMap<String, Integer> sortMapByValue(HashMap<String, Integer> map){
        Comparator<String> comparator = new MapValueComparator(map);
        TreeMap<String, Integer> result = new TreeMap<String, Integer>(comparator);
        result.putAll(map);

        return result;
    }
    
    /**
     * Using regex only tests the pattern.
     * Unfortunately, this approach does not test the validity of the date value itself.
     * @param value
     * @return 
     * @throws TypeInferringException 
     */
    public Duration castDuration(String format, String value) throws TypeInferringException{
        Pattern pattern = Pattern.compile(REGEX_DURATION);
        Matcher matcher = pattern.matcher(value);
        matcher.matches();
        
        throw new TypeInferringException();
    }
    
    /**
     * /**
     * Validate against GeoJSON or TopoJSON schema.
     * @param format can be either default or topojson. Default is geojson.
     * @param value
     * @return
     * @throws TypeInferringException 
     */
    public JSONObject castGeojson(String format, String value) throws TypeInferringException{
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
    public Integer[] castGeopoint(String format, String value) throws TypeInferringException{
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
    
    
    public JSONObject castObject(String format, String value) throws TypeInferringException{
        try {
            return new JSONObject(value);
        }catch(JSONException je){
            throw new TypeInferringException();
        }       
    }
    
    public JSONArray castArray(String format, String value) throws TypeInferringException{
        try {
            return new JSONArray(value);
        }catch(JSONException je){
            throw new TypeInferringException();
        } 
    }
    
    public boolean castDatetime(String format, String value) throws TypeInferringException{
        throw new TypeInferringException();
    }
    
    public boolean castTime(String format, String value) throws TypeInferringException{
        throw new TypeInferringException();
    }
    
    public boolean castDate(String format, String value) throws TypeInferringException{
        throw new TypeInferringException();
    }
    
    public int castInteger(String format, String value) throws TypeInferringException{
        try{
            return Integer.parseInt(value);
        }catch(NumberFormatException nfe){
            throw new TypeInferringException();
        }
    }
    
    public boolean castNumber(String format, String value) throws TypeInferringException{
        throw new TypeInferringException();
    }
    
    public boolean castBoolean(String format, String value) throws TypeInferringException{
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
        throw new TypeInferringException();
    }
    
    public String isAny(String format, String value) throws TypeInferringException{
        throw new TypeInferringException();
    }


}
