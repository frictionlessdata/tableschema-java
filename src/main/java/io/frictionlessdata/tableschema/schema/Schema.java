package io.frictionlessdata.tableschema.schema;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.networknt.schema.ValidationMessage;
import io.frictionlessdata.tableschema.Table;
import io.frictionlessdata.tableschema.exception.PrimaryKeyException;
import io.frictionlessdata.tableschema.exception.TableSchemaException;
import io.frictionlessdata.tableschema.exception.TypeInferringException;
import io.frictionlessdata.tableschema.exception.ValidationException;
import io.frictionlessdata.tableschema.field.Field;
import io.frictionlessdata.tableschema.fk.ForeignKey;
import io.frictionlessdata.tableschema.io.FileReference;
import io.frictionlessdata.tableschema.io.LocalFileReference;
import io.frictionlessdata.tableschema.io.URLFileReference;
import io.frictionlessdata.tableschema.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
@JsonPropertyOrder({"fields", "missingValues", "primaryKey", "foreignKeys"})
public class Schema implements SchemaInterface{
    private static final Logger log = LoggerFactory.getLogger(Schema.class);

    public static final String JSON_KEY_FIELDS = "fields";
    public static final String JSON_KEY_MISSING_VALUES = "missingValues";
    public static final String JSON_KEY_PRIMARY_KEY = "primaryKey";
    public static final String JSON_KEY_FOREIGN_KEYS = "foreignKeys";

    /**
     * The charset or encoding of the schema
     */
    @JsonIgnore
    private Charset charset = StandardCharsets.UTF_8;

    // the schema validator
    @JsonIgnore
    private FormalSchemaValidator tableFormalSchemaValidator = null;

    /**
     * List of {@link Field}s of this schema
     */
    @JsonProperty(JSON_KEY_FIELDS)
    List<Field<?>> fields = new ArrayList<>();

    /**
     * List of mssing value definitions of this schema
     */
    @JsonProperty(JSON_KEY_MISSING_VALUES)
    List<String> missingValues = new ArrayList<>();

    /**
     * The primary key of this schema, if any
     */
    @JsonProperty(JSON_KEY_PRIMARY_KEY)
    private Object primaryKey = null;

    /**
     * List of {@link ForeignKey}s of this schema
     */
    @JsonProperty(JSON_KEY_FOREIGN_KEYS)
    private final List<ForeignKey> foreignKeys = new ArrayList<>();

    /**
     * Whether validation errors should be thrown as exceptions or only reported
     */
    @JsonIgnore
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
        InputStreamReader inputStreamReader = new InputStreamReader(inStream, StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(inputStreamReader);
        String schemaString = br.lines().collect(Collectors.joining("\n"));
        inputStreamReader.close();
        br.close();

        return fromJson(schemaString, strict);
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
        return fromJson(reference, strict);
    }

    /**
     * Read, create, and validate a table schema from a FileReference.
     *
     * @param reference the File or URL to read schema JSON data from
     * @param strict    whether to enforce strict validation
     * @throws IOException thrown if reading from the stream or parsing throws an exception
     */
    public static Schema fromJson(FileReference<?> reference, boolean strict) throws IOException {
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
        return fromJson(reference, strict);
    }

    /**
     * Read, create, and validate a table schema from a JSON string.
     *
     * @param schemaJson the File to read schema JSON data from
     * @param strict     whether to enforce strict validation
     * @throws IOException thrown if reading from the stream or parsing throws an exception
     */
    public static Schema fromJson(String schemaJson, boolean strict) throws IOException {
        Schema schema = JsonUtil.getInstance().deserialize(schemaJson, new TypeReference<>() {});
        schema.strictValidation = strict;
        schema.validate();
        return schema;
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
     * Infers a table schema from various data sources.
     *
     * This method attempts to infer a schema by reading data from one or more sources
     * (direct data, files, or URLs), creating tables from each source, and then inferring
     * schemas from those tables. All inferred schemas must be equal, otherwise an exception
     * is thrown.
     * This method can incur a significant performance penalty for large data sets, in that case use the
     * overloaded method with a row limit.
     *
     * @param data    Direct data source - can be a String containing table data or an ArrayNode
     *                containing JSON representation of table data. May be null if using file or URL sources.
     * @param charset  The character encoding to use when reading from URLs. Used for URL streams only.
     *
     * @return         The inferred Schema that is consistent across all provided data sources
     * @throws IllegalStateException if no valid data source is provided, if the data type is not supported,
     *                              or if schemas inferred from different sources are not equal
     * @throws RuntimeException     if an IOException occurs while reading from files or URLs
     */
    public static Schema infer(Object data, Charset charset) {
        return infer(data, charset, -1);
    }

    /**
     * Infers a table schema from various data sources.
     *
     * This method attempts to infer a schema by reading data from one or more sources
     * (direct data, files, or URLs), creating tables from each source, and then inferring
     * schemas from those tables. All inferred schemas must be equal, otherwise an exception
     * is thrown.
     *
     * @param data    Direct data source - can be a String containing table data or an ArrayNode
     *                containing JSON representation of table data. May be null if using file or URL sources.
     * @param charset  The character encoding to use when reading from URLs. Used for URL streams only.
     * @param rowLimit The max numer of rows to scan. Huge input files can take a considerable time    to infer.
     * @return         The inferred Schema that is consistent across all provided data sources
     * @throws IllegalStateException if no valid data source is provided, if the data type is not supported,
     *                              or if schemas inferred from different sources are not equal
     * @throws RuntimeException     if an IOException occurs while reading from files or URLs
     */
    public static Schema infer(
            Object data,
            Charset charset,
            int rowLimit) {
        List<File> paths = new ArrayList<>();
        List<URL> urls = new ArrayList<>();
        List<String> s = new ArrayList<>();
        if (data != null) {
            if (data instanceof String) {
                s.add((String)data);
            } else if (data instanceof ArrayNode) {
                s.add(JsonUtil.getInstance().serialize(data));
            } else if (data instanceof List) {
                // check to see wehther the list contains URLs or file paths for later processing
                for (Object row : (List<?>) data) {
                    if (row instanceof String) {
                        try {
                           URL url = new URL((String) row); // Check if it's a valid URL
                            urls.add(url);
                        } catch (Exception e) {
                            // Not a valid URL, treat as local file path
                            try {
                                Files.readString(new File((String)row).toPath()); // Check if it's a valid file path
                                paths.add(new File((String)row));
                            } catch (Exception e2) {
                                // Not a valid file path, treat as string data
                                s.add((String)row);
                            }
                        }

                    } else if (row instanceof File) {
                        paths.add((File) row);
                    } else if (row instanceof URL) {
                        urls.add((URL) row);
                    } else if (row != null) {
                        throw new TypeInferringException("Unsupported data type for inferring schema: " + row.getClass().getSimpleName());
                    }
                }
            } else if (data instanceof String[]) {
                // check to see wehther the list contains URLs or file paths for later processing
                for (String row : (String[]) data) {
                    if (row != null) {
                        try {
                            URL url = new URL(row); // Check if it's a valid URL
                            urls.add(url);
                        } catch (Exception e) {
                            // Not a valid URL, treat as local file path
                            try {
                                Files.readString(new File(row).toPath()); // Check if it's a valid file path
                                paths.add(new File(row));
                            } catch (Exception e2) {
                                // Not a valid file path, treat as string data
                                s.add(row);
                            }
                        }

                    }
                }
            } else if (data instanceof File[]) {
                for (File row : (File[]) data) {
                    if (row != null) {
                        paths.add(row);
                    }
                }
            } else if (data instanceof URL[]) {
                for (URL row : (URL[]) data) {
                    if (row != null) {
                        urls.add(row);
                    }
                }
            } else {
                throw new IllegalStateException("Cannot infer schema from provided data type");
            }
        }
        if (paths != null && !paths.isEmpty()) {
            paths.forEach((f) -> {
                try {
                    s.add(Files.readString(f.toPath()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        if (urls != null && !urls.isEmpty()) {
            urls.forEach((url) -> {
                try {
                    InputStream str = url.openStream();
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(str, charset))) {
                        s.add(reader.lines().collect(Collectors.joining("\n")));
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            });
        }
        if (s.isEmpty()){
            throw new IllegalStateException("No valid data source provided for schema inference");
        }
        List<Schema> schemas = new ArrayList<>();
        for (String str : s) {
            Table table = Table.fromSource(str);
            String[] headers = table.getHeaders();
            Schema schema = table.inferSchema(headers, rowLimit);
            schemas.add(schema);
        }
        Schema lastSchema = null;
        for (Schema schema: schemas) {
            if (null == lastSchema) {
                lastSchema = schema;
            } else {
                if (!lastSchema.equals(schema)) {
                    throw new IllegalStateException("Inferred schemas are not equal: " + lastSchema + " != " + schema);
                }
            }
        }
        return lastSchema;
    }

    /**
     * Check if schema is valid or not.
     *
     * @return true if schema is valid
     */
    @Override
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

    private void writeJson(OutputStream output) throws IOException {
        try (BufferedWriter file = new BufferedWriter(new OutputStreamWriter(output, charset))) {
            file.write(this.asJson());
        }
    }

    @Override
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

    @Override
    public List<Field<?>> getFields() {
        return this.fields;
    }

    /**
     * Retrieve a field by its `name` property.
     *
     * @return the matching Field or null if no name matches
     */
    @Override
    public Field<?> getField(String name) {
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


    public List<String> getMissingValues() {
        return missingValues;
    }

    public void setMissingValues(List<String> missingValues) {
        this.missingValues = missingValues;
    }

    @Override
    @JsonIgnore
    public String[] getHeaders() {
         return getFieldNames().toArray(new String[0]);
    }

    public FileReference<?> getReference() {
        return reference;
    }

    /**
     * Test whether this Schema contains a field with the given `name` property.
     *
     * @return true if a field with the given names exists or false if no name matches
     */
    boolean hasField(String name) {
        Field<?> field = fields
                .stream()
                .filter((f) -> f.getName().equals(name))
                .findFirst()
                .orElse(null);
        return (null != field);
    }

    @Deprecated
    boolean hasFields() {
        return !this.getFields().isEmpty();
    }

    @Override
    @JsonIgnore
    public boolean isEmpty() {
        return this.getFields().isEmpty();
    }

    @SuppressWarnings("unchecked")
    public <Any> Any getPrimaryKey() {
        if (Objects.isNull(primaryKey)) {
            return null;
        }
        if (primaryKey instanceof String) {
            return (Any) primaryKey;
        } else if (primaryKey instanceof Collection<?>) {
            Collection<?> collection = (Collection<?>) primaryKey;
            final List<String> retVal = new ArrayList<>();
            collection.forEach(k -> retVal.add(k.toString()));
            return (Any) retVal.toArray(new String[0]);
        }
        throw new TableSchemaException("Unknown PrimaryKey type: " + primaryKey.getClass());
    }

    /**
     * Set primary key, either string or collection of strings
     *
     * @param key the names of the compound primary key column
     */
    @JsonProperty("primaryKey")
    public void setPrimaryKey(Object key) throws PrimaryKeyException {
        if (key instanceof String) {
            setPrimaryKey((String) key);
        } else if (key instanceof Collection) {
            ((Collection)key).forEach(k -> checkKey(k.toString()));
        } else {
            throw new PrimaryKeyException("Invalid primary key type: " + key.getClass());
        }
        this.primaryKey = key;
    }

    /**
     * Set single primary key
     *
     * @param key the name of the primary key column
     */
    public void setPrimaryKey(String key) {
        this.primaryKey = key;
    }

    /**
     * Set compound primary key
     *
     * @param keys the names of the compound primary key column
     */
    public void setPrimaryKey(String[] keys){
        List<String> pkList = new ArrayList<>(Arrays.asList(keys));
        setPrimaryKey(pkList);
    }

    @JsonIgnore
    List<String> getPrimaryKeyParts() {
        if (null == primaryKey) {
            return Collections.emptyList();
        }
        if (primaryKey instanceof String)
            return Collections.singletonList((String) primaryKey);
        if (primaryKey instanceof Collection) {
            final List<String> retVal = new ArrayList<>();
            ((Collection) primaryKey).forEach(k -> retVal.add(k.toString()));
            return retVal;
        }
        throw new TableSchemaException("Unknown PrimaryKey type: " + primaryKey.getClass());
    }

    @Override
    public List<ForeignKey> getForeignKeys() {
        return this.foreignKeys;
    }

    @Override
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


    void addForeignKey(ForeignKey foreignKey) {
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
     * Validate the loaded Schema against the Table. Check that the primary and foreign keys can be found
     * <p>
     * Validation is strict or lenient depending on how the Schema was
     * instantiated with the `strictValidation` flag. With strict validation, all validation
     * errors will lead to a ValidationException thrown.
     *
     * @throws ValidationException If validation fails and validation is strict
     */
    @JsonIgnore
    public void validate(Table t) throws ValidationException{
        errors.clear();
        for (ForeignKey fk : foreignKeys) {
            try{
                fk.validate(t);
            } catch (ValidationException ve) {
                errors.add(ve);
            }
        }
        for (String fieldName : getPrimaryKeyParts()) {
            try{
                validatePrimaryKeyComponent(fieldName);
            } catch (ValidationException ve) {
                errors.add(ve);
            }
        }
        if (!errors.isEmpty()) {
            if (strictValidation) {
                throw new ValidationException(errors);
            } else {
                log.warn("Schema validation failed: {}", errors);
            }
        }
    }

    /**
     * Validate the loaded Schema. First do a formal validation via JSON schema,
     * then check foreign keys match to existing fields.
     * <p>
     * Validation is strict or lenient depending on how the Schema was
     * instantiated with the `strictValidation` flag. With strict validation, all validation
     * errors will lead to a ValidationException thrown.
     *
     * @throws ValidationException If validation fails and validation is strict
     */
    @Override
    @JsonIgnore
    public void validate() throws ValidationException{
        String json = this.asJson();
        Set<ValidationMessage> messages = tableFormalSchemaValidator.validate(json);
        if (!messages.isEmpty()) {
            errors.add(new ValidationException(tableFormalSchemaValidator.getName(), messages));
        }
        for (ForeignKey fk : foreignKeys) {
            fk.validate();
            errors.addAll(fk.getErrors());
            for (String fieldName : fk.getFieldNames()) {
                try{
                    validatePrimaryKeyComponent(fieldName);
                } catch (ValidationException ve) {
                    errors.add(ve);
                }
            }
        }
        if (primaryKey instanceof String) {
            PrimaryKeyException pke = checkKey((String) primaryKey);
            if (null != pke) {
                errors.add(pke);
            }
        } else if (primaryKey instanceof Collection) {
            ((Collection) primaryKey).forEach(k -> {
                PrimaryKeyException pke = checkKey((String) k);
                if (null != pke) {
                    errors.add(pke);
                }
            });
        }
        if (!errors.isEmpty()) {
            if (strictValidation) {
                throw new ValidationException(errors);
            } else {
                log.warn("Schema validation failed: {}", errors);
            }
        }
    }

    private void initValidator() {
        // Init for validation
        InputStream tableSchemaInputStream = TypeInferrer.class.getResourceAsStream("/schemas/table-schema.json");
        this.tableFormalSchemaValidator = FormalSchemaValidator.fromJson(tableSchemaInputStream);
    }

    private void validatePrimaryKeyComponent(String foundFieldName) throws ValidationException {
        Field<?> foundField = fields
                .stream()
                .filter((f) -> f.getName().equals(foundFieldName))
                .findFirst()
                .orElse(null);
        if (null == foundField) {
            throw new ValidationException(String.format("Primary key field %s not found", foundFieldName));
        }
    }

    private PrimaryKeyException checkKey(String key) {
        if (!this.hasField(key)) {
            return new PrimaryKeyException("No such field: '" + key + "'.");
        }
        return null;
    }


}