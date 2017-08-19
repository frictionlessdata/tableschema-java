package io.frictionlessdata.tableschema;

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
    private JSONObject constraints = new JSONObject();
    
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
    
    public JSONObject getJson(){
        JSONObject json = new JSONObject();
        json.put("name", this.name);
        json.put("type", this.type);
        json.put("format", this.format);
        json.put("title", this.title);
        json.put("description", this.description);
        json.put("constraints", this.constraints);
        
        return json;
    }
    
    
       
}
