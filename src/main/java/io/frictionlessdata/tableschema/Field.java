package io.frictionlessdata.tableschema;

import io.frictionlessdata.tableschema.exceptions.ConstraintsException;
import io.frictionlessdata.tableschema.exceptions.InvalidCastException;
import java.lang.reflect.Method;
import org.json.JSONObject;

/**
 *
 * 
 */
public class Field {
  
    private String name = "";
    private String type = "";
    private String format = "default";
    private String title = "";
    private String description = "";
    
    //FIXME: Change this to Map
    private JSONObject constraints = null;
    
    public Field(String name){
        this.name = name;
    }
    
    public Field(String name, String type){
        this.name = name;
        this.type = type;
    }
    
    public Field(String name, String type, String format){
        this.name = name;
        this.type = type;
        this.format = format;
    }
    
    public Field(String name, String type, String format, String title, String description){
        this.name = name;
        this.type = type;
        this.format = format;
        this.title = title;
        this.description = description;
    }
    
    public Field(String name, String type, String format, String title, String description, JSONObject constraints){
        this.name = name;
        this.type = type;
        this.format = format;
        this.title = title;
        this.description = description;
        this.constraints = constraints;
    }
    
    public Field(JSONObject field){
        //TODO: Maybe use Gson serializer for this instead? Is it worth importing library just for this?      
        this.name = field.has("name") ? field.getString("name") : "";
        this.type = field.has("type") ? field.getString("type") : "";
        this.format = field.has("format") ? field.getString("format") : "default";
        this.title = field.has("title") ? field.getString("title") : "";
        this.description = field.has("description") ? field.getString("description") : "";
        
        //FIXME: Handle with Map instead of JSONObject.
        this.constraints = field.has("constraints") ? field.getJSONObject("constraints") : null;
    }
    
    /**
     * Use the Field definition to cast a value into the Field type.
     * @param <Any>
     * @param value
     * @return
     * @throws InvalidCastException
     * @throws ConstraintsException 
     */
    public <Any> Any castValue(String value) throws InvalidCastException, ConstraintsException{
        if(this.type.isEmpty()){
            throw new InvalidCastException();
        }else{
            try{
                // Using reflection to invoke appropriate type casting method from the TypeInferrer class
                String castMethodName = "cast" + (this.type.substring(0, 1).toUpperCase() + this.type.substring(1));
                Method method = TypeInferrer.class.getMethod(castMethodName, String.class, String.class);
                Object castValue = method.invoke(new TypeInferrer(), this.format, value);
            
                return (Any)castValue;
                
            }catch(Exception e){
                throw new InvalidCastException();
            }
        } 
    }
    
    /**
     * Get the JSON representation of the Field.
     * @return 
     */
    public JSONObject getJson(){
        //FIXME: Maybe we should use JSON serializer like Gson?
        JSONObject json = new JSONObject();
        json.put("name", this.name);
        json.put("type", this.type);
        json.put("format", this.format);
        json.put("title", this.title);
        json.put("description", this.description);
        json.put("constraints", this.constraints);
        
        return json;
    }
    
    public String getName(){
        return this.name;
    }
}
