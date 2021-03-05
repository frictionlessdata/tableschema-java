package io.frictionlessdata.tableschema.fk;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.frictionlessdata.tableschema.exception.ForeignKeyException;
import io.frictionlessdata.tableschema.util.JsonUtil;

import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * 
 */
public class Reference {
    private static final String JSON_KEY_DATAPACKAGE = "datapackage";
    private static final String JSON_KEY_RESOURCE = "resource";
    private static final String JSON_KEY_FIELDS = "fields";
    
    private URL datapackage = null;
    private String resource = null;
    private Object fields = null;
    
    public Reference(){
    }
    
    public Reference(String resource, Object fields) throws ForeignKeyException{
        this(resource, fields, false);
    }
    
    public Reference(String resource, Object fields, boolean strict) throws ForeignKeyException{
        this.resource = resource;
        this.fields = fields;
        
        if(strict){
            this.validate();
        }
    }
    
    public Reference(URL datapackage, String resource, Object fields) throws ForeignKeyException{
        this(datapackage, resource, fields, false);
    }
    
    public Reference(URL datapackage, String resource, Object fields, boolean strict) throws ForeignKeyException{
        this.resource = resource;
        this.fields = fields;
        this.datapackage = datapackage;
        
        if(strict){
            this.validate();
        }
    }
    
    public Reference(String json, boolean strict) throws ForeignKeyException{
        JsonNode refJsonObject = JsonUtil.getInstance().createNode(json);
        if(refJsonObject.has(JSON_KEY_DATAPACKAGE)){
            try{
                this.datapackage = new URL(refJsonObject.get(JSON_KEY_DATAPACKAGE).asText());
                
            }catch(MalformedURLException mue){
                // leave datapackage set to null;
                this.datapackage = null;
            }  
        }
        
        if(refJsonObject.has(JSON_KEY_RESOURCE)){
            this.resource = refJsonObject.get(JSON_KEY_RESOURCE).asText();
        }
        
        if(refJsonObject.has(JSON_KEY_FIELDS)){
        	if(refJsonObject.get(JSON_KEY_FIELDS).isArray()) {
        		this.fields = refJsonObject.get(JSON_KEY_FIELDS);
        	} else {
        		this.fields = refJsonObject.get(JSON_KEY_FIELDS).asText();
        	}
        }
        
        if(strict){
            this.validate();
        }
    }
    
    public URL getDatapackage(){
        return this.datapackage;
    }
    
    public String getResource(){
        return this.resource;
    }
    
    public void setResource(String resource){
        this.resource = resource;
    }
    
    public <Any> Any getFields(){
        return (Any)this.fields;
    }
    
    public void setFields(Object fields){
        this.fields = fields;
    }
    
    public final void validate() throws ForeignKeyException{
        if(this.resource == null || this.fields == null){
            throw new ForeignKeyException("A foreign key's reference must have the fields and resource properties.");
            
        }else if(!(this.fields instanceof String) && !(ArrayNode.class.isAssignableFrom(this.fields.getClass()))){
            throw new ForeignKeyException("The foreign key's reference fields property must be a string or an array.");
        }
    }
    
    @JsonIgnore
    public String getJson(){
        return JsonUtil.getInstance().serialize(this);
    }
}
