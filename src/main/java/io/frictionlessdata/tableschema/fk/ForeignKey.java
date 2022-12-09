package io.frictionlessdata.tableschema.fk;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.frictionlessdata.tableschema.Table;
import io.frictionlessdata.tableschema.exception.ForeignKeyException;
import io.frictionlessdata.tableschema.util.JsonUtil;

import java.util.*;

/**
 * 
 */
public class ForeignKey {
    private static final String JSON_KEY_FIELDS = "fields";
    private static final String JSON_KEY_REFERENCE = "reference";
    
    private Object fields = null;
    private Reference reference = null;
    
    private boolean strictValidation = false;
    private final ArrayList<Exception> errors = new ArrayList<>();
    
    public ForeignKey(){   
    }
    
    public ForeignKey(boolean strict){  
        this();
        this.strictValidation = strict;
    }

    public ForeignKey(Object fields, Reference reference, boolean strict) throws ForeignKeyException{
        this.fields = fields;
        this.reference = reference;
        this.strictValidation = strict;
        this.validate();
    }
    
    public ForeignKey(String json, boolean strict) throws ForeignKeyException{
        JsonNode fkJsonObject = JsonUtil.getInstance().readValue(json);
        this.strictValidation = strict;
        
        if(fkJsonObject.has(JSON_KEY_FIELDS)){
        	if(fkJsonObject.get(JSON_KEY_FIELDS).isArray()) {
        		fields = fkJsonObject.get(JSON_KEY_FIELDS);
        	} else {
        		fields = fkJsonObject.get(JSON_KEY_FIELDS).asText();
        	}
        }
        
        if(fkJsonObject.has(JSON_KEY_REFERENCE)){
            JsonNode refJsonObject = fkJsonObject.get(JSON_KEY_REFERENCE);
            reference = new Reference(refJsonObject.toString(), strict);
        }
        
        validate();
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
        ForeignKeyException fke = null;
        
        if(this.fields == null || this.reference == null){
            fke = new ForeignKeyException("A foreign key must have the fields and reference properties.");
            
        }else if(!(this.fields instanceof String) && !(this.fields instanceof ArrayNode)){
            fke = new ForeignKeyException("The foreign key's fields property must be a string or an array.");
            
        }else if(this.fields instanceof ArrayNode && !(this.reference.getFields() instanceof ArrayNode)){
            fke = new ForeignKeyException("The reference's fields property must be an array if the outer fields is an array.");
            
        }else if(this.fields instanceof String && !(this.reference.getFields() instanceof String)){
            fke = new ForeignKeyException("The reference's fields property must be a string if the outer fields is a string.");
            
        }else if(this.fields instanceof ArrayNode && this.reference.getFields() instanceof ArrayNode){
        	ArrayNode fkFields = (ArrayNode)fields;
        	ArrayNode refFields = reference.getFields();
            
            if(fkFields.size() != refFields.size()){
                fke = new ForeignKeyException("The reference's fields property must be an array of the same length as that of the outer fields' array.");
            }
        }

        if(fke != null){
            if(this.strictValidation){
                throw fke;  
            }else{
                errors.add(fke);
            }           
        }

    }

    public final void validate(Table table) throws ForeignKeyException{
        validate();

        // self-referencing FK
        if (reference.getResource().equals("")) {
            List<String> fieldNames = new ArrayList<>();
            List<String> foreignFieldNames = new ArrayList<>();
            if (fields instanceof String) {
                fieldNames.add((String) fields);
                foreignFieldNames.add(reference.getFields());
            } else  if (fields instanceof ArrayNode) {
                for (int i = 0; i < ((ArrayNode) fields).size(); i++) {
                    fieldNames.add(((ArrayNode) fields).get(i).asText());
                    foreignFieldNames.add(((ArrayNode) reference.getFields()).get(i).asText());
                }
            }
            Iterator<Object> iterator = table.iterator(true, false, false, false);
            while (iterator.hasNext()) {
                Map<String, Object> next = (Map<String, Object>)iterator.next();
                for (int i = 0; i < fieldNames.size(); i++){
                    if (!next.get(fieldNames.get(i)).equals(next.get(foreignFieldNames.get(i)))) {
                        throw new ForeignKeyException("Foreign key ["+fieldNames.get(i)+ "-> "
                                +foreignFieldNames.get(i)+"] violation : expected: "
                                +next.get(fieldNames.get(i)) + " found: "
                                +next.get(foreignFieldNames.get(i)));
                    }
                }
            }
         }

    }
    
    @JsonIgnore
    public String getJson(){
        return JsonUtil.getInstance().serialize(this);
    }
    
    public List<Exception> getErrors(){
        return errors;
    }
    
}
