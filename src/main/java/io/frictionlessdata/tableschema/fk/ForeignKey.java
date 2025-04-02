package io.frictionlessdata.tableschema.fk;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.frictionlessdata.tableschema.Table;
import io.frictionlessdata.tableschema.exception.ForeignKeyException;
import io.frictionlessdata.tableschema.exception.ValidationException;
import io.frictionlessdata.tableschema.util.JsonUtil;

import java.util.*;

/**
 * 
 */
public class ForeignKey {
    private static final String JSON_KEY_FIELDS = "fields";
    private static final String JSON_KEY_REFERENCE = "reference";

    @JsonProperty(JSON_KEY_FIELDS)
    private Object fields = null;

    @JsonProperty(JSON_KEY_REFERENCE)
    private Reference reference = null;
    
    private boolean strictValidation = true;
    private final ArrayList<ValidationException> errors = new ArrayList<>();
    
    public ForeignKey(){}

    public ForeignKey(Object fields, Reference reference, boolean strict) throws ForeignKeyException{
        this.fields = fields;
        this.reference = reference;
        this.strictValidation = strict;
        this.validate();
    }

    public void setFields(Object fields){
        this.fields = fields;
    }

    public <Any> Any getFields(){
        return (Any)this.fields;
    }

    @JsonIgnore
    public List<String> getFieldNames(){
        if (this.fields instanceof String){
            return Collections.singletonList((String)this.fields);
        } else if (this.fields instanceof Collection){
            return new ArrayList<>( (Collection<String>)this.fields);
        } else {
            throw new IllegalArgumentException("Invalid fields type in reference: "+this.fields.getClass().getName());
        }
    }
    
    public void setReference(Reference reference){
        this.reference = reference;
    }
    
    public Reference getReference(){
        return this.reference;
    }
    
    public final void validate() throws ForeignKeyException{
        ForeignKeyException fke = null;
        this.errors.clear();

        if(this.fields == null || this.reference == null){
            fke = new ForeignKeyException("A foreign key must have the fields and reference properties.");
            
        }else if(!(this.fields instanceof String) && !(this.fields instanceof Collection)){
            fke = new ForeignKeyException("The foreign key's fields property must be a string or an array.");
            
        }else if(this.fields instanceof Collection && !(this.reference.getFields() instanceof Collection)){
            fke = new ForeignKeyException("The reference's fields property must be an array if the outer fields is an array.");
            
        }else if(this.fields instanceof String && !(this.reference.getFields() instanceof String)){
            fke = new ForeignKeyException("The reference's fields property must be a string if the outer fields is a string.");
            
        }else if(this.fields instanceof Collection && this.reference.getFields() instanceof Collection){
            Collection<?> fkFields = (Collection<?>)fields;
            Collection<?> refFields = reference.getFields();
            
            if(fkFields.size() != refFields.size()){
                fke = new ForeignKeyException("The reference's fields property must be an array of the same length as that of the outer fields' array.");
            }
        }

        if (null != reference) {
            reference.validate();
        }

        if(fke != null){
            if(this.strictValidation){
                throw fke;  
            }else{
                errors.add(fke);
                if (null != reference) {
                    errors.addAll(reference.getErrors());
                }
            }           
        }

    }

    /**
     * validate the foreign key against the table. We only can validate self-referencing FKs, as we
     * do not have access to the tables in different resources of a  datapackages in the tableschema library.
     * @param table the table to validate against
     * @throws ForeignKeyException if the foreign key is violated
     */
    public final void validate(Table table) throws ForeignKeyException{
        validate();

        // self-referencing FK
        if (reference.getResource().equals("")) {
            List<String> fieldNames = new ArrayList<>();
            List<String> foreignFieldNames = new ArrayList<>();
            List<String> lFields = getFieldNames();
            for (int i = 0; i < lFields.size(); i++) {
                fieldNames.add(lFields.get(i));
                foreignFieldNames.add(reference.getFieldNames().get(i));
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
         } else {
            throw new UnsupportedOperationException("Foreign key references across package resources are not supported");
        }

    }
    
    @JsonIgnore
    public String getJson(){
        return JsonUtil.getInstance().serialize(this);
    }

    @JsonIgnore
    public ArrayList<ValidationException> getErrors(){
        return errors;
    }
    
}
