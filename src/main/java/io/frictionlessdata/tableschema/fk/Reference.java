package io.frictionlessdata.tableschema.fk;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.frictionlessdata.tableschema.exception.ForeignKeyException;
import io.frictionlessdata.tableschema.util.JsonUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This implements a reference from one Table to another. Weirdly, this is in tableschema as per
 * the spec: https://specs.frictionlessdata.io/table-schema/#foreign-keys while it is used from
 * the datapackage standard (Tables have no notion of resources).
 * 
 */
public class Reference {
    private static final String JSON_KEY_RESOURCE = "resource";
    private static final String JSON_KEY_FIELDS = "fields";

    private String resource = "";
    private List<String> fields = null;
    
    public Reference(){
    }

    public Reference(String resource, Collection<String> fields, boolean strict) throws ForeignKeyException{
        this.resource = resource;
        this.setFields(new ArrayList<>(fields));

        if(strict){
            this.validate();
        }
    }
    
    public Reference(String resource, String[] fields, boolean strict) throws ForeignKeyException{
        this.resource = resource;
        List<String> flocFields = Arrays.stream(fields).collect(Collectors.toList());
        this.setFields(flocFields);
        
        if(strict){
            this.validate();
        }
    }

    public Reference(String resource, String field, boolean strict) throws ForeignKeyException{
        this.resource = resource;
        this.setFields(field);

        if(strict){
            this.validate();
        }
    }
    
    public static Reference fromJson(String json, boolean strict) throws ForeignKeyException{
        Reference ref = new Reference();
        JsonNode refJsonObject = JsonUtil.getInstance().createNode(json);
        
        if(refJsonObject.has(JSON_KEY_RESOURCE)){
            ref.resource = refJsonObject.get(JSON_KEY_RESOURCE).asText();
        }
        
        if (refJsonObject.has(JSON_KEY_FIELDS)){
            ref.fields = resolveFields (refJsonObject.get(JSON_KEY_FIELDS));
        }
        
        if(strict){
            ref.validate();
        }
        return ref;
    }

    public String getResource(){
        return this.resource;
    }
    
    public void setResource(String resource){
        this.resource = resource;
    }
    
    public <Any> Any getFields(){
        if ((null == fields) || (fields.size() == 0)) {
            return null;
        } else if (fields.size() == 1) {
            return (Any)fields.get(0);
        }
        return (Any)this.fields;
    }
    
    public void setFields(Collection<String> fields){
        this.fields = new ArrayList<>(fields);
    }

    public void setFields(String field){
        this.fields = new ArrayList<>();
        this.fields.add(field);
    }

    public void addField(String field) {
        if (null == fields) {
            fields = new ArrayList<>();
        }
        fields.add(field);
    }
    
    public final void validate() throws ForeignKeyException{
        if ((null == this.resource) || (this.fields == null)) {
            throw new ForeignKeyException("A foreign key's reference must have fields and resource properties.");
            
        } /*else if (!(this.fields instanceof String) && !(this.fields instanceof String[])){
            throw new ForeignKeyException("The foreign key's reference fields property must be a string or an array.");
        }*/
    }
    
    @JsonIgnore
    public String getJson(){
        return JsonUtil.getInstance().serialize(this);
    }

    private static List<String> resolveFields (JsonNode refJsonObject) {
        List<String> entries = new ArrayList<>();
        if(refJsonObject.isArray()) {
            ArrayNode node = (ArrayNode) refJsonObject;
            node.elements().forEachRemaining((e) -> entries.add(e.asText()));
        } else {
            entries.add(refJsonObject.asText());
        }
        return entries;
    }
}
