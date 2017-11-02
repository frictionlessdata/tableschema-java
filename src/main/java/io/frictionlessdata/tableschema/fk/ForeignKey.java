package io.frictionlessdata.tableschema.fk;

import io.frictionlessdata.tableschema.exceptions.ForeignKeyException;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 
 */
public class ForeignKey {
    private static final String JSON_KEY_FIELDS = "fields";
    private static final String JSON_KEY_REFERENCE = "reference";
    
    private Object fields = null;
    private Reference reference = null;
    
    public ForeignKey(){   
    }
    
    public ForeignKey(Object fields, Reference reference){
        this.fields = fields;
        this.reference = reference;
    }
    
    public ForeignKey(JSONObject fkJsonObject) throws ForeignKeyException{
        this(fkJsonObject, false);
    }
    
    public ForeignKey(JSONObject fkJsonObject, boolean strict) throws ForeignKeyException{
        
        if(fkJsonObject.has(JSON_KEY_FIELDS)){
            this.fields = fkJsonObject.get(JSON_KEY_FIELDS);
        }
        
        if(fkJsonObject.has(JSON_KEY_REFERENCE)){
            JSONObject refJsonObject = fkJsonObject.getJSONObject(JSON_KEY_REFERENCE);
            this.reference = new Reference(refJsonObject, strict);
        }
        
        if(strict){
            this.validate();
        } 
    }
    
    public void setFields(Object fields){
        this.fields = fields;
    }
    
    public void setReference(Reference reference){
        this.reference = reference;
    }
    
    private void validate() throws ForeignKeyException{
        if(this.fields == null || this.reference == null){
            throw new ForeignKeyException();
            
        }else if(!(this.fields instanceof String) || !(this.fields instanceof JSONArray)){
            throw new ForeignKeyException();
        }
        
        //TODO: Validate against resource object
    }
    
}
