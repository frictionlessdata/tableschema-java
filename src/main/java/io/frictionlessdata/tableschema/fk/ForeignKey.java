package io.frictionlessdata.tableschema.fk;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.frictionlessdata.tableschema.Table;
import io.frictionlessdata.tableschema.exception.ForeignKeyException;
import io.frictionlessdata.tableschema.util.JsonUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 
 */
public class ForeignKey {
    private static final String JSON_KEY_FIELDS = "fields";
    private static final String JSON_KEY_REFERENCE = "reference";
    
    private List<String> fields = null;
    private Reference reference = null;
    
    private boolean strictValidation = false;
    private final ArrayList<Exception> errors = new ArrayList<>();
    
    public ForeignKey(){   
    }
    
    public ForeignKey(boolean strict){  
        this();
        this.strictValidation = strict;
    }

    public ForeignKey(String field, Reference reference, boolean strict) throws ForeignKeyException{
        this(strict);
        this.setFields(field);
        this.reference = reference;
        this.validate();
    }

    public ForeignKey(String[] fields, Reference reference, boolean strict) throws ForeignKeyException{
        this(strict);
        List<String> flocFields = (null == fields) ? null : Arrays.stream(fields).collect(Collectors.toList());
        this.setFields(flocFields);
        this.reference = reference;
        this.validate();
    }

    public ForeignKey(Collection<String> fields, Reference reference, boolean strict) throws ForeignKeyException{
        this(strict);
        this.setFields(new ArrayList<>(fields));
        this.reference = reference;
        this.validate();
    }

    public static ForeignKey fromJson(String json, boolean strict) throws ForeignKeyException{
        ForeignKey key = new ForeignKey(strict);
        JsonNode fkJsonObject = JsonUtil.getInstance().readValue(json);

        if (fkJsonObject.has(JSON_KEY_FIELDS)){
            key.fields = resolveFields (fkJsonObject.get(JSON_KEY_FIELDS));
        }
        
        if(fkJsonObject.has(JSON_KEY_REFERENCE)){
            JsonNode refJsonObject = fkJsonObject.get(JSON_KEY_REFERENCE);
            key.reference = Reference.fromJson(refJsonObject.toString(), strict);
        }

        key.validate();
        return key;
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
        this.fields = new ArrayList<>();
        if (null != fields) {
            this.fields.addAll(fields);
        }
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

    public void setReference(Reference reference){
        this.reference = reference;
    }
    
    public Reference getReference(){
        return this.reference;
    }
    
    public final void validate() throws ForeignKeyException{
        ForeignKeyException fke = null;
        
        if (this.getFields() == null || this.reference == null){
            fke = new ForeignKeyException("A foreign key must have fields and reference properties.");
            
        } else if (!(this.getFields() instanceof String) && !(this.getFields() instanceof List)){
            fke = new ForeignKeyException("The foreign key's fields property must be a string or an array.");
            
        } else if (this.getFields() instanceof List && !(this.reference.getFields() instanceof List)){
            if (this.fields.size() > 1) {
                fke = new ForeignKeyException("The reference's fields property must be an array if the outer fields is an array.");
            }
        } else if (this.getFields() instanceof String && !(this.reference.getFields() instanceof String)){
            fke = new ForeignKeyException("The reference's fields property must be a string if the outer fields is a string.");
            
        } else if (this.getFields() instanceof List && this.reference.getFields() instanceof List){
            List<String> refFields = reference.getFields();
            
            if (fields.size() != refFields.size()){
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
            if (getFields() instanceof String) {
                fieldNames.add(fields.get(0));
                foreignFieldNames.add(reference.getFields());
            } else  if (getFields() instanceof List) {
                for (int i = 0; i < fields.size(); i++) {
                    fieldNames.add(fields.get(i));
                    foreignFieldNames.add(((List<String>) reference.getFields()).get(i));
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
