package io.frictionlessdata.tableschema.schema;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.frictionlessdata.tableschema.exception.*;
import io.frictionlessdata.tableschema.field.Field;
import io.frictionlessdata.tableschema.fk.ForeignKey;
import io.frictionlessdata.tableschema.io.FileReference;
import io.frictionlessdata.tableschema.io.LocalFileReference;
import io.frictionlessdata.tableschema.io.URLFileReference;
import io.frictionlessdata.tableschema.util.JsonUtil;

/**
 *
 * 
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_EMPTY)
public class Schema {
    private static final int JSON_INDENT_FACTOR = 4;
    public static final String JSON_KEY_FIELDS = "fields";
    public static final String JSON_KEY_PRIMARY_KEY = "primaryKey";
    public static final String JSON_KEY_FOREIGN_KEYS = "foreignKeys";
    
    private JsonSchema tableJsonSchema = null;
    private List<Field> fields = new ArrayList();
    private Object primaryKey = null;
    private List<ForeignKey> foreignKeys = new ArrayList();
    
    private boolean strictValidation = true;
    private List<Exception> errors = new ArrayList();

    @JsonIgnore
    FileReference reference;

    /**
     * Create an empty table schema without strict validation
     */
    public Schema(){
        this.initValidator();
    }

    /**
     * Create an empty table schema
     * @param strict whether to enforce strict validation
     */
    public Schema(boolean strict){
        this.strictValidation = strict;
        this.initValidator();
    }

    /**
     * Create and validate a new table schema using a collection of fields.
     *
     * @param fields the fields to use for the Table
     * @param strict whether to enforce strict validation
     * @throws ValidationException thrown if parsing throws an exception
     */
    public Schema(Collection<Field> fields, boolean strict) throws ValidationException{
        this.strictValidation = strict;
        this.fields = new ArrayList<>(fields);

        initValidator();
        validate();
    }

    /**
     * Read, create, and validate a table schema from an {@link java.io.InputStream}.
     *
     * @param inStream the InputStream to read the schema JSON data from
     * @param strict whether to enforce strict validation
     * @throws Exception thrown if reading from the stream or parsing throws an exception
     */
    public static Schema fromJson (InputStream inStream, boolean strict) throws IOException {
        Schema schema = new Schema(strict);
        schema.initSchemaFromStream(inStream);
        schema.validate();
        return schema;
    }

    /**
     * Read, create, and validate a table schema from a remote location.
     *
     * @param schemaUrl the URL to read the schema JSON data from
     * @param strict whether to enforce strict validation
     * @throws Exception thrown if reading from the stream or parsing throws an exception
     */
    public static Schema fromJson(URL schemaUrl, boolean strict) throws Exception{
        FileReference reference = new URLFileReference(schemaUrl);
        Schema schema = fromJson (reference.getInputStream(), strict);
        schema.reference = reference;
        reference.close();
        return schema;
    }

    /**
     * Read, create, and validate a table schema from a FileReference.
     *
     * @param reference the File or URL to read schema JSON data from
     * @param strict whether to enforce strict validation
     * @throws Exception thrown if reading from the stream or parsing throws an exception
     */
    public static Schema fromJson (FileReference reference, boolean strict) throws Exception {
        Schema schema = fromJson (reference.getInputStream(), strict);
        schema.reference = reference;
        reference.close();
        return schema;
    }

    /**
     * Read, create, and validate a table schema from a local {@link java.io.File}.
     *
     * @param schemaFile the File to read schema JSON data from
     * @param strict whether to enforce strict validation
     * @throws Exception thrown if reading from the stream or parsing throws an exception
     */
    public static Schema fromJson (File schemaFile, boolean strict) throws Exception {
        FileReference reference = new LocalFileReference(schemaFile);
        Schema schema = fromJson (reference.getInputStream(), strict);
        schema.reference = reference;
        reference.close();
        return schema;
    }

    /**
     * Read, create, and validate a table schema from a JSON string.
     *
     * @param schemaJson the File to read schema JSON data from
     * @param strict whether to enforce strict validation
     * @throws Exception thrown if reading from the stream or parsing throws an exception
     */
    public  static Schema fromJson (String schemaJson, boolean strict) throws IOException {
        return fromJson (new ByteArrayInputStream(schemaJson.getBytes()), strict);
    }
    
    /**
     * Infer the data types and return the generated schema.
     * @param data
     * @param headers
     * @return Schema generated from the inferred input
     * @throws TypeInferringException 
     */
    public static Schema infer(List<Object[]> data, String[] headers) throws TypeInferringException, IOException {
        return fromJson(TypeInferrer.getInstance().infer(data, headers), true);
    }

    /**
     * Infer the data types and return the generated schema.
     * @param data
     * @param headers
     * @param rowLimit
     * @return Schema generated from the inferred input
     * @throws TypeInferringException
     */
    public static Schema infer(List<Object[]> data, String[] headers, int rowLimit) throws TypeInferringException, IOException {
        return fromJson(TypeInferrer.getInstance().infer(data, headers, rowLimit), true);
    }
    
    /**
     * Initializes the schema from given stream.
     * Used for Schema class instantiation with remote or local schema file.
     * @param inStream the `InputStream` to read and parse the Schema from
     * @throws Exception when reading fails
     */
    private void initSchemaFromStream(InputStream inStream) throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(inStream, StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(inputStreamReader);
        String schemaString = br.lines().collect(Collectors.joining("\n"));
        inputStreamReader.close();
        br.close();
        
        this.initFromSchemaJson(schemaString);
    }
    
    private void initFromSchemaJson(String json) throws PrimaryKeyException, ForeignKeyException{
        JsonNode schemaObj = JsonUtil.getInstance().readValue(json);
        // Set Fields
        if(schemaObj.has(JSON_KEY_FIELDS)){
        	TypeReference<List<Field>> fieldsTypeRef = new TypeReference<List<Field>>() {};
        	String fieldsJson = schemaObj.withArray(JSON_KEY_FIELDS).toString();
            this.fields.addAll(JsonUtil.getInstance().deserialize(fieldsJson, fieldsTypeRef));
        }
        
        // Set Primary Key
        if(schemaObj.has(JSON_KEY_PRIMARY_KEY)){
        	if(schemaObj.get(JSON_KEY_PRIMARY_KEY).isArray()) {
        		this.setPrimaryKey(schemaObj.withArray(JSON_KEY_PRIMARY_KEY));
        	} else {
        		this.setPrimaryKey(schemaObj.get(JSON_KEY_PRIMARY_KEY).asText());
        	}
        }
        
        // Set Foreign Keys
        if(schemaObj.has(JSON_KEY_FOREIGN_KEYS)){
            JsonNode fkJsonArray = schemaObj.withArray(JSON_KEY_FOREIGN_KEYS);
            fkJsonArray.forEach((f)->{
                ForeignKey fk = new ForeignKey(f.toString(), this.strictValidation);
                this.addForeignKey(fk);
                
                if(!this.strictValidation){
                    this.getErrors().addAll(fk.getErrors());
                } 
            });
        }
    }
    
    private void initValidator(){
        // Init for validation
        InputStream tableSchemaInputStream = TypeInferrer.class.getResourceAsStream("/schemas/table-schema.json");
        this.tableJsonSchema = JsonSchema.fromJson(tableSchemaInputStream, strictValidation);
    }
    
    /**
     * Check if schema is valid or not.
     * @return
     */
    @JsonIgnore
    public boolean isValid(){
        try{
            validate();
            return ((null == errors) || (errors.isEmpty()));
        }catch(ValidationException ve){
            return false;
        }
    }

    @SuppressWarnings("rawtypes")
	private void validate(String foundFieldName) throws ValidationException{
        Field foundField = fields
                .stream()
                .filter((f) -> f.getName().equals(foundFieldName))
                .findFirst()
                .orElse(null);
        if (null == foundField) {
            throw new ValidationException (String.format("%s: Primary key field %s not found", tableJsonSchema, foundFieldName));
        }
    }

    /**
     * Validate the loaded Schema. First do a formal validation via JSON schema,
     * then check foreign keys match to existing fields.
     *
     * Validation is strict or unstrict depending on how the package was
     * instantiated with the strict flag.
     * @throws ValidationException 
     */
    @JsonIgnore
    private void validate() throws ValidationException{
        try{
        	String json = this.getJson();
             this.tableJsonSchema.validate(json);
             if (null != foreignKeys) {
                 for (ForeignKey fk : foreignKeys) {
                     Object fields = fk.getFields();
                     if (fields instanceof ArrayNode) {
                         List<Object> subFields = new ArrayList<>();
                         ((ArrayNode) fields).elements().forEachRemaining(f->subFields.add(f.asText()));
                         for (Object subField : subFields) {
                             validate((String) subField);
                         }
                     } else if (fields instanceof String) {
                         validate((String) fields);
                     }
                 }
             }

        }catch(ValidationException ve){
            if(this.strictValidation){
                throw ve;
            }else{
                this.getErrors().add(ve);
            }
        }
    }
    
    public List<Exception> getErrors(){
        return this.errors;
    }
    
    @JsonIgnore
    public String getJson(){
    	return JsonUtil.getInstance().serialize(this);
    }

    public Object[] castRow(String[] row) throws InvalidCastException{
        
        if(row.length != this.fields.size()){
            throw new InvalidCastException("Row length is not equal to the number of defined fields.");
        }
        
        try{
            Object[] castRow = new Object[this.fields.size()];
        
            for(int i=0; i<row.length; i++){
                Field field = this.fields.get(i);
                castRow[i] = field.parseValue(row[i], field.getFormat(), null);
            }

            return castRow;
            
        }catch(Exception e){
            throw new InvalidCastException(e);
        }
        
    }
    
    public void writeJson (File outputFile) throws IOException{
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            writeJson(fos);
        }
    }

    public void writeJson (OutputStream output) throws IOException{
        try (BufferedWriter file = new BufferedWriter(new OutputStreamWriter(output))) {
            file.write(this.getJson());
        }
    }
    
    public void addField(Field field){
        this.fields.add(field);
        this.validate();
    }

    /**
     * Add a field from a JSON string representation.
     * @param json serialized JSON oject
     */
    public void addField(String json){
        Field field = Field.fromJson(json);
        this.addField(field);
    }
    
    public List<Field> getFields(){
        return this.fields;
    }
    
    public Field getField(String name){
        Iterator<Field> iter = this.fields.iterator();
        while(iter.hasNext()){
            Field field = iter.next();
            if(field.getName().equalsIgnoreCase(name)){
                return field;
            }
        }
        return null;
    }
    
    public List<String> getFieldNames(){
        return fields
                .stream()
                .map(Field::getName)
                .collect(Collectors.toList());
    }
    
    public boolean hasField(String name){
        Field field = fields
            .stream()
            .filter((f) -> f.getName().equals(name))
            .findFirst()
            .orElse(null);
        return (null != field);
    }
    
    public boolean hasFields(){
        return !this.getFields().isEmpty();
    }


    public FileReference getReference() {
        return reference;
    }
    /**
     * Set single primary key with the option of validation.
     * @param key
     * @throws PrimaryKeyException 
     */
    public void setPrimaryKey(String key) throws PrimaryKeyException{
        checkKey(key);
        this.primaryKey = key; 
    }

    private void checkKey(String key) {
        if(!this.hasField(key)){
            PrimaryKeyException pke = new PrimaryKeyException("No such field as: " + key + ".");
            if(this.strictValidation){
                throw pke;
            }else{
                this.getErrors().add(pke);
            }
        }
    }

    public void setPrimaryKey(String[] keys) throws PrimaryKeyException{
        setPrimaryKey(JsonUtil.getInstance().createArrayNode(keys));
    }
    
    /**
     * Set composite primary key with the option of validation.
     * @param compositeKey
     * @throws PrimaryKeyException 
     */
    public void setPrimaryKey(ArrayNode compositeKey) throws PrimaryKeyException{
        compositeKey.forEach(k->{
        	checkKey(k.asText());
        });
        this.primaryKey = compositeKey;
    }

    @SuppressWarnings("unchecked")
	public <Any> Any getPrimaryKey(){
    	if(Objects.isNull(primaryKey)) {
    		return null;
    	}
        if (primaryKey instanceof String)
            return (Any)primaryKey;
        if (primaryKey instanceof JsonNode) {
        	JsonNode jsonNode = (JsonNode)primaryKey;
        	if(jsonNode.isArray()) {
        		final List<String> retVal = new ArrayList<>();
        		jsonNode.forEach(k->retVal.add(k.asText()));
                return (Any)retVal.toArray(new String[retVal.size()]);
        	} else {
        		return (Any)jsonNode.asText();
        	}
        };
        throw new TableSchemaException("Unknown PrimaryKey type: "+primaryKey.getClass());
    }

    @JsonIgnore
    public List<String> getPrimaryKeyParts() {
        if (primaryKey instanceof String)
            return Arrays.asList((String) primaryKey);
        if (primaryKey instanceof ArrayNode) {
            final List<String> retVal = new ArrayList<>();
            ((ArrayNode)primaryKey).forEach(k->retVal.add(k.asText()));
            return retVal;
        }
        throw new TableSchemaException("Unknown PrimaryKey type: "+primaryKey.getClass());
    }

    public List<ForeignKey> getForeignKeys(){
        return this.foreignKeys;
    }
    
    public void addForeignKey(ForeignKey foreignKey){
        this.foreignKeys.add(foreignKey);
    }

    /**
     * Similar to {@link #equals(Object)}, but disregards the `format` property
     * to allow for Schemas that are similar except that Fields have no
     * defined format. Also treats null and empty string the same for `name` and
     * `type`.
     *
     * @param other the Field to compare against
     * @return true if the other Field is equals ignoring the format, false otherwise
     */
    public boolean similar(Schema other) {
        if (this == other) return true;
        boolean same = true;
        for (Field f : fields) {
            Field otherField = other.getField(f.getName());
            same = same & f.similar(otherField);
        }
        if (!same)
            return false;
        return Objects.equals(primaryKey, other.primaryKey) &&
                Objects.equals(foreignKeys, other.foreignKeys);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Schema schema = (Schema) o;
        return Objects.equals(fields, schema.fields) &&
                Objects.equals(primaryKey, schema.primaryKey) &&
                Objects.equals(foreignKeys, schema.foreignKeys);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fields, primaryKey, foreignKeys);
    }
}