# Specification compliance

Documents compliance with the [specifications](https://frictionlessdata.io/specs/table-schema/). 

## [Descriptor](https://github.com/frictionlessdata/specs/blob/master/specs/table-schema.md#descriptor)

### A Table Schema is represented by a descriptor. The descriptor MUST be a JSON object (JSON is defined in RFC 4627).

- `It MUST contain a property 'fields'. fields MUST be an array where each entry in the array is a field descriptor (as defined below)`

    - implemented in Schema.java as `private List<Field> fields
    
- `The order of elements in fields array MUST be the order of fields in the CSV file`
    - implemented in Schema.java as 
        this.fields.forEach((field) -> {
            schemaJson.getJSONArray(JSON_KEY_FIELDS).put(new JSONObject(field.getJson()));
        });    
        
    - implemented in `Schema#initFromSchemaJson()` (ordered iteration)   
    - for JSON arrays of JSON objects as a data source, we do not check for order, as it cannot be truly guaranteed (see https://github.com/frictionlessdata/specs/issues/656)
    
    
## [Field Descriptors](https://github.com/frictionlessdata/specs/blob/master/specs/table-schema.md#field-descriptors)

### A field descriptor MUST be a JSON object that describes a single field

- `A field descriptor MUST be a JSON object that describes a single field
    - implemented in the class hierarchy in package `io.frictionlessdata.tableschema.field`, in `Field#getJson()` 
    (conversion to JSON object), and `Field#fromJson(String)` .
    
#### name    
- `The field descriptor MUST contain a name property.`
    - implemented in Field.java as `private String name = "";`
    
- `name SHOULD NOT be considered case sensitive in determining uniqueness. However, since it should correspond to the name of the field in the data file it may be important to preserve case.`     
    - implemented in `Field.equals(Object)` via `name.equalsIgnoreCase(field.name)`
    
#### title
- implemented in Field.java as `private String title = "";`

#### description
- implemented in Field.java as `private String description = "";`

### [Types and Formats](https://github.com/frictionlessdata/specs/blob/master/specs/table-schema.md#types-and-formats)
- Both type and format are optional: in a field descriptor, the absence of a type property indicates that the field 
    is of the type "string", and the absence of a format property indicates that the field's type format is "default".
    
    - implemented in Fild#fromJson(String) as `String type = fieldDef.has(JSON_KEY_TYPE) 
    ? fieldDef.getString(JSON_KEY_TYPE) : "string";` and `field.format = (!StringUtils.isEmpty(format)) 
    ? format.trim() : FIELD_FORMAT_DEFAULT`;
   
#### string - The field contains strings, that is, sequences of characters.   
- implemented in `StringField.java` 
- format:
	- `default: any valid string.`: supported in `parseFormat()` as default fallback
	- `email: A valid email address.`: supported in `parseFormat()` via commons-validator
	- `uri: A valid URI.`: supported in `parseFormat()`
	- `binary: A base64 encoded string representing binary data.` Not supported in `parseFormat()`. No idea how to ensure a discrimination between based64-encoded binary and default
	- `uuid: A string that is a uuid.`: supported in `parseFormat()`

#### number - The field contains numbers of any kind including decimals.   
- implemented in `NumberField.java`   
