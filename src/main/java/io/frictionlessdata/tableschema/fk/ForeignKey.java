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
    
    public ForeignKey(Object fields, Reference reference) throws ForeignKeyException{
        this(fields, reference, false);
    }
    
    public ForeignKey(Object fields, Reference reference, boolean strict) throws ForeignKeyException{
        this.fields = fields;
        this.reference = reference;
        
        if(strict){
            this.validate();
        } 
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
    
    public <Any> Any getFields(){
        return (Any)this.fields;
    }
    
    public void setReference(Reference reference){
        this.reference = reference;
    }
    
    public Reference getReference(){
        return this.reference;
    }
    
    public final void validate() throws ForeignKeyException{
        if(this.fields == null || this.reference == null){
            throw new ForeignKeyException("A foreign key must have the fields and reference properties.");
            
        }else if(!(this.fields instanceof String) && !(this.fields instanceof JSONArray)){
            throw new ForeignKeyException("The foreign key's fields property must be a string or an array.");
            
        }else if(this.fields instanceof JSONArray && !(this.reference.getFields() instanceof JSONArray)){
            throw new ForeignKeyException("The reference's fields property must be an array if the outer fields is an array.");
            
        }else if(this.fields instanceof String && !(this.reference.getFields() instanceof String)){
            throw new ForeignKeyException("The reference's fields property must be a string if the outer fields is a string.");
            
        }else if(this.fields instanceof JSONArray && this.reference.getFields() instanceof JSONArray){
            JSONArray fkFields = (JSONArray)this.fields;
            JSONArray refFields = (JSONArray)this.reference.getFields();
            
            if(fkFields.length() != refFields.length()){
                throw new ForeignKeyException("The reference's fields property must an array of the same length as that of the outer fields' array.");
            }
        }
    }
    
}
