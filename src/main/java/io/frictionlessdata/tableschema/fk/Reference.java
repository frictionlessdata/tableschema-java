package io.frictionlessdata.tableschema.fk;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.frictionlessdata.tableschema.exception.ForeignKeyException;
import io.frictionlessdata.tableschema.exception.ValidationException;
import io.frictionlessdata.tableschema.util.JsonUtil;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * This implements a reference from one Table to another. Weirdly, this is in tableschema as per
 * the spec: https://specs.frictionlessdata.io/table-schema/#foreign-keys while it is used from
 * the datapackage standard (Tables have no notion of resources).
 * 
 */

public class Reference {
    private static final String JSON_KEY_RESOURCE = "resource";
    private static final String JSON_KEY_FIELDS = "fields";

    @JsonProperty(JSON_KEY_RESOURCE)
    private String resourceName = null;

    @JsonProperty(JSON_KEY_FIELDS)
    private Object fields = null;

    @JsonIgnore
    private boolean strictValidation = true;

    @JsonIgnore
    private final ArrayList<ValidationException> errors = new ArrayList<>();

    public Reference(){}

    public Reference(String resource, Object fields, boolean strict) throws ForeignKeyException{
        this.resourceName = resource;
        this.fields = fields;
        this.strictValidation = strict;
        this.validate();
    }


    @JsonIgnore
    public String getResource(){
        return this.resourceName;
    }

    @JsonIgnore
    public void setResource(String resource){
        this.resourceName = resource;
    }

    @JsonIgnore
    public <Any> Any getFields(){
        return (Any)this.fields;
    }

    @JsonIgnore
    public void setFields(Object fields){
        this.fields = fields;
    }

    @JsonIgnore
    public boolean isStrictValidation() {
        return strictValidation;
    }

    @JsonIgnore
    public void setStrictValidation(boolean strictValidation) {
        this.strictValidation = strictValidation;
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
    
    public final void validate() throws ForeignKeyException{
        errors.clear();
        ForeignKeyException fke = null;
        if(this.resourceName == null || this.fields == null){
            fke = new ForeignKeyException("A foreign key's reference must have the fields and resource properties.");
            
        }else if(!(this.fields instanceof String) && !(this.fields instanceof Collection)){
            fke = new ForeignKeyException("The foreign key's reference fields property must be a string or an array.");
        }
        if(fke != null){
            if(this.strictValidation){
                throw fke;
            }else{
                errors.add(fke);
            }
        }
    }

    /**
     * Get the JSON representation of the Reference.
     * @return String-serialized JSON Object containing the properties of this foreign key reference
     */
    @JsonIgnore
    @Deprecated
    public String getJson(){
        return asJson();
    }

    /**
     * Get the JSON representation of the Reference.
     * @return String-serialized JSON Object containing the properties of this  foreign key reference
     */
    @JsonIgnore
    public String asJson(){
        return JsonUtil.getInstance().serialize(this);
    }

    @JsonIgnore
    public ArrayList<ValidationException> getErrors(){
        return errors;
    }
}
