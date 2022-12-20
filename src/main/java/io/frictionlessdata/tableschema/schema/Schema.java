package io.frictionlessdata.tableschema.schema;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.networknt.schema.ValidationMessage;
import io.frictionlessdata.tableschema.exception.*;
import io.frictionlessdata.tableschema.field.Field;
import io.frictionlessdata.tableschema.fk.ForeignKey;
import io.frictionlessdata.tableschema.io.FileReference;
import io.frictionlessdata.tableschema.io.LocalFileReference;
import io.frictionlessdata.tableschema.io.URLFileReference;
import io.frictionlessdata.tableschema.util.JsonUtil;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class implements the Table Schema specification from https://specs.frictionlessdata.io//table-schema/index.html
 *
 * Table Schema is a simple language- and implementation-agnostic way to declare a schema for tabular data.
 * Table Schema is well suited for use cases around handling and validating tabular data in text formats such as CSV,
 * but its utility extends well beyond this core usage, towards a range of applications where data benefits
 * from a portable schema format
 *
 * This class is the Java implementation of a Table Schema. It makes a semantic difference between:
 * <ul>
 *      <li>constructing a Schema from scratch (e.g. empty or from a Collection of Fields)</li>
 *      <li>the various overloaded `fromSource()` methods that read a Schema from a URL or File or InputStream.</li>
 *      <li>guessing a Schema from a sample of data ("inferring" a Schema), the `infer()` methods</li>
 * </ul>
 *
 * Schemas can be serialized to File or OutputStream via the `writeJson()` Methods. The seriolized form
 * can be used to re-create a schema via the `fromSource()` method.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_EMPTY)
public class Schema {
    public static final String JSON_KEY_FIELDS = "fields";
    public static final String JSON_KEY_PRIMARY_KEY = "primaryKey";
    public static final String JSON_KEY_FOREIGN_KEYS = "foreignKeys";

    private FormalSchemaValidator tableFormalSchemaValidator = null;
    List<Field<?>> fields = new ArrayList<>();

    private FormalSchemaValidator tableJsonSchema = null;
    private Object primaryKey = null;
    private final List<ForeignKey> foreignKeys = new ArrayList<>();

    boolean strictValidation = true;

    @JsonIgnore
    private final List<ValidationException> errors = new ArrayList<>();

    @JsonIgnore
    FileReference<?> reference;

    /**
     * Create an empty table schema without strict validation
     */
    public Schema() {
        this.initValidator();
    }

    /**
     * Create an empty table schema
     *
     * @param strict whether to enforce strict validation
     */
    public Schema(boolean strict) {
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
    public Schema(Collection<Field<?>> fields, boolean strict) throws ValidationException {
        this.strictValidation = strict;
        this.fields = new ArrayList<>(fields);

        initValidator();
        validate();
    }

    /**
     * Read, create, and validate a table schema from an {@link java.io.InputStream}.
     *
     * @param inStream the InputStream to read the schema JSON data from
     * @param strict   whether to enforce strict validation
     * @throws IOException thrown if reading from the stream or parsing throws an exception
     */
    public static Schema fromJson(InputStream inStream, boolean strict) throws IOException {
        Schema schema = new Schema(strict);
        schema.initSchemaFromStream(inStream);
        schema.validate();
        return schema;
    }

    /**
     * Read, create, and validate a table schema from a remote location.
     *
     * @param schemaUrl the URL to read the schema JSON data from
     * @param strict    whether to enforce strict validation
     * @throws IOException thrown if reading from the stream or parsing throws an exception
     */
    public static Schema fromJson(URL schemaUrl, boolean strict) throws IOException {
        FileReference<URL> reference = new URLFileReference(schemaUrl);
        Schema schema = fromJson(reference.getInputStream(), strict);
        schema.reference = reference;
        reference.close();
        return schema;
    }

    /**
     * Read, create, and validate a table schema from a FileReference.
     *
     * @param reference the File or URL to read schema JSON data from
     * @param strict    whether to enforce strict validation
     * @throws IOException thrown if reading from the stream or parsing throws an exception
     */
    public static Schema fromJson(FileReference<?> reference, boolean strict) throws Exception {
        Schema schema = fromJson(reference.getInputStream(), strict);
        schema.reference = reference;
        reference.close();
        return schema;
    }

    /**
     * Read, create, and validate a table schema from a local {@link java.io.File}.
     *
     * @param schemaFile the File to read schema JSON data from
     * @param strict     whether to enforce strict validation
     * @throws IOException thrown if reading from the stream or parsing throws an exception
     */
    public static Schema fromJson(File schemaFile, boolean strict) throws IOException {
        FileReference<File> reference = new LocalFileReference(schemaFile);
        Schema schema = fromJson(reference.getInputStream(), strict);
        schema.reference = reference;
        reference.close();
        return schema;
    }

    /**
     * Read, create, and validate a table schema from a JSON string.
     *
     * @param schemaJson the File to read schema JSON data from
     * @param strict     whether to enforce strict validation
     * @throws IOException thrown if reading from the stream or parsing throws an exception
     */
    public static Schema fromJson(String schemaJson, boolean strict) throws IOException {
        return fromJson(new ByteArrayInputStream(schemaJson.getBytes()), strict);
    }

    /**
     * Infer the data types and return the generated schema.
     *
     * @param data    a List of table rows
     * @param headers the table headers
     * @return Schema generated from the inferred input
     * @throws TypeInferringException if inferring of the Schema fails
     */
    public static Schema infer(List<Object[]> data, String[] headers) throws TypeInferringException, IOException {
        return fromJson(TypeInferrer.getInstance().infer(data, headers), true);
    }

    /**
     * Infer the data types and return the generated schema.
     *
     * @param data     a List of table rows
     * @param headers  the table headers
     * @param rowLimit maximal number of rows to use for Schema inferral
     * @return Schema generated from the inferred input
     * @throws TypeInferringException if inferring of the Schema fails
     * @throws IOException            if an underlying IOException is thrown
     */
    public static Schema infer(List<Object[]> data, String[] headers, int rowLimit) throws TypeInferringException, IOException {
        return fromJson(TypeInferrer.getInstance().infer(data, headers, rowLimit), true);
    }

    /**
     * Check if schema is valid or not.
     *
     * @return true if schema is valid
     */
    @JsonIgnore
    public boolean isValid() {
        try {
            validate();
            return errors.isEmpty();
        } catch (ValidationException ve) {
            return false;
        }
    }

    public void writeJson(File outputFile) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            writeJson(fos);
        }
    }

    public void writeJson(OutputStream output) throws IOException {
        try (BufferedWriter file = new BufferedWriter(new OutputStreamWriter(output))) {
            file.write(this.asJson());
        }
    }

    public void addField(Field<?> field) {
        this.fields.add(field);
        this.validate();
    }

    /**
     * Add a field from a JSON string representation.
     *
     * @param json serialized JSON oject
     */
    public void addField(String json) {
        Field<?> field = Field.fromJson(json);
        this.addField(field);
    }

    public List<Field<?>> getFields() {
        return this.fields;
    }

    /**
     * Retrieve a field by its `name` property.
     *
     * @return the matching Field or null if no name matches
     */
    public Field getField(String name) {
        for (Field<?> field : this.fields) {
            if (field.getName().equalsIgnoreCase(name)) {
                return field;
            }
        }
        return null;
    }

    @JsonIgnore
    public List<String> getFieldNames() {
        return fields
                .stream()
                .map(Field::getName)
                .collect(Collectors.toList());
    }

    @JsonIgnore
    public String[] getHeaders() {
         return getFieldNames().toArray(new String[0]);
    }

    /**
     * Test whether this Schema contains a field with the given `name` property.
     *
     * @return true if a field with the given names exists or false if no name matches
     */
    public boolean hasField(String name) {
        Field<?> field = fields
                .stream()
                .filter((f) -> f.getName().equals(name))
                .findFirst()
                .orElse(null);
        return (null != field);
    }

    public boolean hasFields() {
        return !this.getFields().isEmpty();
    }


    @SuppressWarnings("unchecked")
    public <Any> Any getPrimaryKey() {
        if (Objects.isNull(primaryKey)) {
            return null;
        }
        if (primaryKey instanceof String)
            return (Any) primaryKey;
        if (primaryKey instanceof JsonNode) {
            JsonNode jsonNode = (JsonNode) primaryKey;
            if (jsonNode.isArray()) {
                final List<String> retVal = new ArrayList<>();
                jsonNode.forEach(k -> retVal.add(k.asText()));
                return (Any) retVal.toArray(new String[0]);
            } else {
                return (Any) jsonNode.asText();
            }
        }
        throw new TableSchemaException("Unknown PrimaryKey type: " + primaryKey.getClass());
    }

    @JsonIgnore
    public List<String> getPrimaryKeyParts() {
        if (primaryKey instanceof String)
            return Collections.singletonList((String) primaryKey);
        if (primaryKey instanceof ArrayNode) {
            final List<String> retVal = new ArrayList<>();
            ((ArrayNode) primaryKey).forEach(k -> retVal.add(k.asText()));
            return retVal;
        }
        throw new TableSchemaException("Unknown PrimaryKey type: " + primaryKey.getClass());
    }

    public List<ForeignKey> getForeignKeys() {
        return this.foreignKeys;
    }


    public List<ValidationException> getErrors(){
        return this.errors;
    }

    /**
     * For consistency, use {@link #asJson()} instead
     * @return JSON-Representation
     */
    @Deprecated()
    @JsonIgnore
    public String getJson() {
        return asJson();
    }

    @JsonIgnore
    public String asJson() {
        return JsonUtil.getInstance().serialize(this);
    }

    public FileReference<?> getReference() {
        return reference;
    }

    /**
     * Set single primary key with the option of validation.
     *
     * @param key the name of the primary key column
     * @throws PrimaryKeyException if this Schema does not contain a field with the given `name` property and
     *                             validation is set to strict
     */
    public void setPrimaryKey(String key) throws PrimaryKeyException {
        checkKey(key);
        this.primaryKey = key;
    }

    public void setPrimaryKey(String[] keys) throws PrimaryKeyException {
        setPrimaryKey(JsonUtil.getInstance().createArrayNode(keys));
    }

    /**
     * Set composite primary key with the option of validation.
     *
     * @param compositeKey the composite primary to set
     * @throws PrimaryKeyException if this Schema does not contain fields listed in the `compositeKey` and
     *                             validation is set to strict
     */
    public void setPrimaryKey(ArrayNode compositeKey) throws PrimaryKeyException {
        compositeKey.forEach(k -> checkKey(k.asText()));
        this.primaryKey = compositeKey;
    }

    public void addForeignKey(ForeignKey foreignKey) {
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
        for (Field<?> f : fields) {
            Field<?> otherField = other.getField(f.getName());
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
        if (null == o) return false;
        if (!(o instanceof Schema)) return false;
        Schema schema = (Schema) o;
        return Objects.equals(fields, schema.fields) &&
                Objects.equals(primaryKey, schema.primaryKey) &&
                Objects.equals(foreignKeys, schema.foreignKeys);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fields, primaryKey, foreignKeys);
    }

    /**
     * Validate the loaded Schema. First do a formal validation via JSON schema,
     * then check foreign keys match to existing fields.
     * <p>
     * Validation is strict or lenient depending on how the package was
     * instantiated with the strict flag.
     *
     * @throws ValidationException If validation fails and validation is strict
     */
    @JsonIgnore
    public void validate() throws ValidationException{
        String json = this.asJson();
        Set<ValidationMessage> messages = tableFormalSchemaValidator.validate(json);
        if (!messages.isEmpty()) {
            errors.add(new ValidationException(tableFormalSchemaValidator, messages));
        }
        for (ForeignKey fk : foreignKeys) {
            Object fields = fk.getFields();
            if (fields instanceof ArrayNode) {
                List<Object> subFields = new ArrayList<>();
                ((ArrayNode) fields).elements().forEachRemaining(f->subFields.add(f.asText()));
                for (Object subField : subFields) {
                    try{
                        validate((String) subField);
                    } catch (ValidationException ve) {
                        errors.add(ve);
                    }
                }
            } else if (fields instanceof String) {
                try{
                    validate((String) fields);
                } catch(ValidationException ve){
                    errors.add(ve);
                }
            }
        }
        if (strictValidation && !errors.isEmpty()) {
            throw new ValidationException(tableFormalSchemaValidator.getName(), errors);
        }
    }

    /**
     * Initializes the schema from given stream.
     * Used for Schema class instantiation with remote or local schema file.
     *
     * @param inStream the `InputStream` to read and parse the Schema from
     * @throws IOException when reading fails
     */
    private void initSchemaFromStream(InputStream inStream) throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(inStream, StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(inputStreamReader);
        String schemaString = br.lines().collect(Collectors.joining("\n"));
        inputStreamReader.close();
        br.close();

        this.initFromSchemaJson(schemaString);
    }

    private void initFromSchemaJson(String json) throws PrimaryKeyException, ForeignKeyException {
        JsonNode schemaObj = JsonUtil.getInstance().readValue(json);
        // Set Fields
        if (schemaObj.has(JSON_KEY_FIELDS)) {
            TypeReference<List<Field<?>>> fieldsTypeRef = new TypeReference<List<Field<?>>>() {
            };
            String fieldsJson = schemaObj.withArray(JSON_KEY_FIELDS).toString();
            this.fields.addAll(JsonUtil.getInstance().deserialize(fieldsJson, fieldsTypeRef));
        }

        // Set Primary Key
        if (schemaObj.has(JSON_KEY_PRIMARY_KEY)) {
            if (schemaObj.get(JSON_KEY_PRIMARY_KEY).isArray()) {
                this.setPrimaryKey(schemaObj.withArray(JSON_KEY_PRIMARY_KEY));
            } else {
                this.setPrimaryKey(schemaObj.get(JSON_KEY_PRIMARY_KEY).asText());
            }
        }

        // Set Foreign Keys
        if (schemaObj.has(JSON_KEY_FOREIGN_KEYS)) {
            JsonNode fkJsonArray = schemaObj.withArray(JSON_KEY_FOREIGN_KEYS);
            fkJsonArray.forEach((f) -> {
                ForeignKey fk = new ForeignKey(f.toString(), strictValidation);
                this.addForeignKey(fk);

                if (!strictValidation) {
                    fk.getErrors().forEach((e) -> {
                        errors.add(new ValidationException(e));
                    });
                }
            });
        }
    }

    private void initValidator() {
        // Init for validation
        InputStream tableSchemaInputStream = TypeInferrer.class.getResourceAsStream("/schemas/table-schema.json");
        this.tableFormalSchemaValidator = FormalSchemaValidator.fromJson(tableSchemaInputStream, strictValidation);
    }


    @SuppressWarnings("rawtypes")
    private void validate(String foundFieldName) throws ValidationException {
        Field foundField = fields
                .stream()
                .filter((f) -> f.getName().equals(foundFieldName))
                .findFirst()
                .orElse(null);
        if (null == foundField) {
            throw new ValidationException(String.format("Primary key field %s not found", foundFieldName));
        }
    }

    private void checkKey(String key) {
        if (!this.hasField(key)) {
            PrimaryKeyException pke = new PrimaryKeyException("No such field: " + key + ".");
            if (this.strictValidation) {
                throw pke;
            } else {
                errors.add(new ValidationException(pke));
            }
        }
    }


}