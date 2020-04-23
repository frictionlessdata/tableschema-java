package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.exception.ConstraintsException;
import io.frictionlessdata.tableschema.exception.InvalidCastException;
import io.frictionlessdata.tableschema.exception.TableSchemaException;
import io.frictionlessdata.tableschema.util.JsonUtil;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Definition of a field in a data table. Doesn't hold values
 *
 * Spec: http://frictionlessdata.io/specs/table-schema/index.html#field-descriptors
 *
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_EMPTY)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, defaultImpl = AnyField.class, 
	include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = StringField.class, name = Field.FIELD_TYPE_STRING),
    @JsonSubTypes.Type(value = IntegerField.class, name = Field.FIELD_TYPE_INTEGER),
    @JsonSubTypes.Type(value = NumberField.class, name = Field.FIELD_TYPE_NUMBER),
    @JsonSubTypes.Type(value = BooleanField.class, name = Field.FIELD_TYPE_BOOLEAN),
    @JsonSubTypes.Type(value = ObjectField.class, name = Field.FIELD_TYPE_OBJECT),
    @JsonSubTypes.Type(value = ArrayField.class, name = Field.FIELD_TYPE_ARRAY),
    @JsonSubTypes.Type(value = DateField.class, name = Field.FIELD_TYPE_DATE),
    @JsonSubTypes.Type(value = TimeField.class, name = Field.FIELD_TYPE_TIME),
    @JsonSubTypes.Type(value = DatetimeField.class, name = Field.FIELD_TYPE_DATETIME),
    @JsonSubTypes.Type(value = YearField.class, name = Field.FIELD_TYPE_YEAR),
    @JsonSubTypes.Type(value = YearmonthField.class, name = Field.FIELD_TYPE_YEARMONTH),
    @JsonSubTypes.Type(value = DurationField.class, name = Field.FIELD_TYPE_DURATION),
    @JsonSubTypes.Type(value = GeopointField.class, name = Field.FIELD_TYPE_GEOPOINT),
    @JsonSubTypes.Type(value = GeojsonField.class, name = Field.FIELD_TYPE_GEOJSON),
    @JsonSubTypes.Type(value = AnyField.class, name = Field.FIELD_TYPE_ANY)
    }
)
public abstract class Field<T> {
    public static final String FIELD_TYPE_STRING = "string";
    public static final String FIELD_TYPE_INTEGER = "integer";
    public static final String FIELD_TYPE_NUMBER = "number";
    public static final String FIELD_TYPE_BOOLEAN = "boolean";
    public static final String FIELD_TYPE_OBJECT = "object";
    public static final String FIELD_TYPE_ARRAY = "array";
    public static final String FIELD_TYPE_DATE = "date";
    public static final String FIELD_TYPE_TIME = "time";
    public static final String FIELD_TYPE_DATETIME = "datetime";
    public static final String FIELD_TYPE_YEAR = "year";
    public static final String FIELD_TYPE_YEARMONTH = "yearmonth";
    public static final String FIELD_TYPE_DURATION = "duration";
    public static final String FIELD_TYPE_GEOPOINT = "geopoint";
    public static final String FIELD_TYPE_GEOJSON = "geojson";
    public static final String FIELD_TYPE_ANY = "any";
    
    private List<String> wellKnownFieldTypes = Arrays.asList(
    	FIELD_TYPE_STRING,
    	FIELD_TYPE_INTEGER,
    	FIELD_TYPE_NUMBER,
    	FIELD_TYPE_BOOLEAN,
    	FIELD_TYPE_OBJECT,
    	FIELD_TYPE_ARRAY,
    	FIELD_TYPE_DATE,
    	FIELD_TYPE_TIME,
    	FIELD_TYPE_DATETIME,
    	FIELD_TYPE_YEAR,
    	FIELD_TYPE_YEARMONTH,
    	FIELD_TYPE_DURATION,
    	FIELD_TYPE_GEOPOINT,
    	FIELD_TYPE_GEOJSON,
    	FIELD_TYPE_ANY
    );
    
    public static final String FIELD_FORMAT_DEFAULT = "default";
    public static final String FIELD_FORMAT_ARRAY = "array";
    public static final String FIELD_FORMAT_OBJECT = "object";
    public static final String FIELD_FORMAT_TOPOJSON = "topojson";
    public static final String FIELD_FORMAT_URI = "uri";
    public static final String FIELD_FORMAT_EMAIL = "email";
    public static final String FIELD_FORMAT_BINARY = "binary";
    public static final String FIELD_FORMAT_UUID = "uuid";

    public static final String CONSTRAINT_KEY_REQUIRED = "required";
    public static final String CONSTRAINT_KEY_UNIQUE = "unique";
    public static final String CONSTRAINT_KEY_MIN_LENGTH = "minLength";
    public static final String CONSTRAINT_KEY_MAX_LENGTH = "maxLength";
    public static final String CONSTRAINT_KEY_MINIMUM = "minimum";
    public static final String CONSTRAINT_KEY_MAXIMUM = "maximum";
    public static final String CONSTRAINT_KEY_PATTERN = "pattern";
    public static final String CONSTRAINT_KEY_ENUM = "enum";
    
    public static final String JSON_KEY_NAME = "name";
    public static final String JSON_KEY_TYPE = "type";
    public static final String JSON_KEY_FORMAT = "format";
    public static final String JSON_KEY_TITLE = "title";
    public static final String JSON_KEY_RDFTYPE = "rdfType";
    public static final String JSON_KEY_DESCRIPTION = "description";
    public static final String JSON_KEY_CONSTRAINTS = "constraints";
    /**
     * The field descriptor MUST contain a `name` property.
     * `name` SHOULD NOT be considered case sensitive in determining uniqueness.
     * However, since it should correspond to the name of the field in the data file it
     * may be important to preserve case.
     * http://frictionlessdata.io/specs/table-schema/index.html#field-descriptors
     */
    private String name = "";

    /**
     * A field's `type` property is a string indicating the type of this field.
     * http://frictionlessdata.io/specs/table-schema/index.html#field-descriptors
     */
    String type = "";


    /**
     * A field's `format` property is a string, indicating a format for the field type.
     * http://frictionlessdata.io/specs/table-schema/index.html#field-descriptors
     */
    String format = FIELD_FORMAT_DEFAULT;

    /**
     * A human readable label or title for the field
     */
    private String title = null;

    /**
     * A description for this field e.g. "The recipient of the funds"
     */
    private String description = null;

    /**
     * A richer, "semantic", description of the "type" of data in a given column MAY be
     * provided using a rdfType property on a field descriptor.
     *
     * The value of of the rdfType property MUST be the URI of a RDF Class, that is an
     * instance or subclass of RDF Schema Class object
     * http://frictionlessdata.io/specs/table-schema/index.html#rich-types
     */
    private URI rdfType = null;

    Map<String, Object> constraints = null;

    Map<String, Object> options = null;

    /**
     * Constructor for our reflection-based instantiation only
     */
    Field(){    }

    Field(String name, String type){
        this.name = name;
        this.type = type;
    }

    public Field(
            String name,
            String type,
            String format,
            String title,
            String description,
            URI rdfType,
            Map constraints,
            Map options){
        this.name = name;
        this.type = type;
        this.format = format;
        this.title = title;
        this.rdfType = rdfType;
        this.description = description;
        this.constraints = constraints;
        this.options = options;
    }

    public static Field fromJson (String json) {
        return JsonUtil.getInstance().deserialize(json, Field.class);
    }


    public abstract T parseValue(String value, String format, Map<String, Object> options)
            throws InvalidCastException, ConstraintsException;

    public abstract String formatValueAsString(T value, String format, Map<String, Object> options)
            throws InvalidCastException, ConstraintsException;

    public Object formatValueForJson(T value) throws InvalidCastException, ConstraintsException {
        return value;
    }

    /**
     * Given a value, try to parse the format. Some Field types don't have variant
     * formats and will always return `default`
     *
     * @param value sample value encoded as string
     * @param options format options
     * @return inferred format encoded as a string
     */
    public abstract String parseFormat(String value, Map<String, Object> options);

    /**
     * Use the Field definition to cast a value into the Field type.
     * Enforces constraints by default.
     * @param value the value string to cast
     * @return result of the cast operation
     * @throws InvalidCastException if the content of `value` cannot be cast to the destination type
     * @throws ConstraintsException thrown if `enforceConstraints` was set to `true`and constraints were violated
     */
    public T castValue(String value) throws InvalidCastException, ConstraintsException{
        return castValue(value, true, null);
    }
    
    /**
     * Use the Field definition to cast (=parse) a value into the Field type. Constraints enforcing
     * can be switched on or off.
     * @param value the value string to cast
     * @param enforceConstraints whether to enforce Field constraints
     * @param options casting options
     * @return result of the cast operation
     * @throws InvalidCastException if the content of `value` cannot be cast to the destination type
     * @throws ConstraintsException thrown if `enforceConstraints` was set to `true`and constraints were violated
     */
    public T castValue(String value, boolean enforceConstraints, Map<String, Object> options) throws InvalidCastException, ConstraintsException{
        if(this.type.isEmpty()){
            throw new InvalidCastException("Property 'type' must not be empty");
        } else if (StringUtils.isEmpty(value)) {
            return null;
        } else {
            try{
                T castValue = parseValue(value, format, options);
            
                // Check for constraint violations
                if(enforceConstraints && this.constraints != null){
                    Map<String, Object> violatedConstraints = checkConstraintViolations(castValue);
                    if(!violatedConstraints.isEmpty()){
                        throw new ConstraintsException("Violated "+ violatedConstraints.size()+" contstraints");
                    }
                }
                
                return castValue;
                
            }catch(ConstraintsException ce){
                throw ce;
                
            }catch(Exception e){
                throw new InvalidCastException(e);
            }
        } 
    }
    
    /**
     * Returns a Map with all the constraints that have been violated.
     * @param value either a JSONArray/JSONObject or a string containing JSON
     * @return Map containing all the contraints violations
     */
    public Map<String, Object> checkConstraintViolations(Object value){
       
        Map<String, Object> violatedConstraints = new HashMap<>();
        
        // Indicates whether this field is allowed to be null. If required is true, then null is disallowed. 
        if(this.constraints.containsKey(CONSTRAINT_KEY_REQUIRED)){
            if((boolean) this.constraints.get(CONSTRAINT_KEY_REQUIRED) && value == null){
                violatedConstraints.put(CONSTRAINT_KEY_REQUIRED, true);
            }
        }
        
        // All values for that field MUST be unique within the data file in which it is found.
        // Can't check UNIQUE constraint when operating with only one value.
        // TODO: Implement a method that takes List<Object> value as argument.
        /*
        if(this.constraints.containsKey(CONSTRAINT_KEY_UNIQUE)){
    
        }*/
        
        // An integer that specifies the minimum length of a value.
        if(this.constraints.containsKey(CONSTRAINT_KEY_MIN_LENGTH)){
            int minLength = (int)this.constraints.get(CONSTRAINT_KEY_MIN_LENGTH);
 
            if(value instanceof String){
                if(((String)value).length() < minLength){
                    violatedConstraints.put(CONSTRAINT_KEY_MIN_LENGTH, minLength);
                }
                
            }else if (value instanceof JsonNode && !((JsonNode)value).isArray()){
                if(((JsonNode)value).size() < minLength){
                    violatedConstraints.put(CONSTRAINT_KEY_MIN_LENGTH, minLength);
                }
                 
            }else if (value instanceof ArrayNode){
                if(((ArrayNode)value).size() < minLength){
                    violatedConstraints.put(CONSTRAINT_KEY_MIN_LENGTH, minLength);
                }                
            }
        }
        
        // An integer that specifies the maximum length of a value.
        if(this.constraints.containsKey(CONSTRAINT_KEY_MAX_LENGTH)){
            int maxLength = (int)this.constraints.get(CONSTRAINT_KEY_MAX_LENGTH);
            
            if(value instanceof String){
                if(((String)value).length() > maxLength){
                    violatedConstraints.put(CONSTRAINT_KEY_MAX_LENGTH, maxLength);
                }
                
            }else if (value instanceof JsonNode){
                if(((JsonNode)value).size() > maxLength){
                    violatedConstraints.put(CONSTRAINT_KEY_MAX_LENGTH, maxLength);
                }
                 
            }else if (value instanceof ArrayNode){
                if(((ArrayNode)value).size() > maxLength){
                    violatedConstraints.put(CONSTRAINT_KEY_MAX_LENGTH, maxLength);
                }                
            }  
        }
        
        /*
         * Specifies a minimum value for a field.
         * This is different to minLength which checks the number of items in the value.
         * A minimum value constraint checks whether a field value is greater than or equal to the specified value.
         * The range checking depends on the type of the field.
         * E.g. an integer field may have a minimum value of 100; a date field might have a minimum date.
         * If a minimum value constraint is specified then the field descriptor MUST contain a type key.
         **/
        if(this.constraints.containsKey(CONSTRAINT_KEY_MINIMUM)){
            
            if(value instanceof Integer){
                int minInt = (int)this.constraints.get(CONSTRAINT_KEY_MINIMUM);
                if((int)value < minInt){
                    violatedConstraints.put(CONSTRAINT_KEY_MINIMUM, minInt);
                }
                
            }else if(value instanceof DateTime){
                DateTime minDateTime = (DateTime)this.constraints.get(CONSTRAINT_KEY_MINIMUM);
                if(((DateTime)value).isBefore(minDateTime)){
                    violatedConstraints.put(CONSTRAINT_KEY_MINIMUM, minDateTime);
                }
                
            }else if(value instanceof Duration){
                Duration minDuration = (Duration)this.constraints.get(CONSTRAINT_KEY_MINIMUM);
                if(((Duration)value).compareTo(minDuration) < 0){
                    violatedConstraints.put(CONSTRAINT_KEY_MINIMUM, minDuration);
                }
            } 
        }
        
        // As for minimum, but specifies a maximum value for a field.
        if(this.constraints.containsKey(CONSTRAINT_KEY_MAXIMUM)){
            
            if(value instanceof Integer){
                int maxInt = (int)this.constraints.get(CONSTRAINT_KEY_MAXIMUM);
                if((int)value > maxInt){
                    violatedConstraints.put(CONSTRAINT_KEY_MAXIMUM, maxInt);
                }
                
            }else if(value instanceof DateTime){
                DateTime maxDateTime = (DateTime)this.constraints.get(CONSTRAINT_KEY_MAXIMUM);
                
                if(((DateTime)value).isAfter(maxDateTime)){
                    violatedConstraints.put(CONSTRAINT_KEY_MAXIMUM, maxDateTime);
                }
                
            }else if(value instanceof Duration){
                Duration maxDuration = (Duration)this.constraints.get(CONSTRAINT_KEY_MAXIMUM);
                if(((Duration)value).compareTo(maxDuration) > 0){
                    violatedConstraints.put(CONSTRAINT_KEY_MAXIMUM, maxDuration);
                }
            } 
        }
        
        // A regular expression that can be used to test field values. If the regular expression matches then the value is valid.
        if(this.constraints.containsKey(CONSTRAINT_KEY_PATTERN)){
            String regexPatternString = (String)this.constraints.get(CONSTRAINT_KEY_PATTERN);
            
            // Constraint only applies to a String value.
            if(value instanceof String){       
                Pattern pattern = Pattern.compile(regexPatternString);
                Matcher matcher = pattern.matcher((String)value);
                
                if(!matcher.matches()){
                    violatedConstraints.put(CONSTRAINT_KEY_PATTERN, regexPatternString);
                }
            
            }else{
                // If the value is not a String, then just interpret as a constraint violation.
                violatedConstraints.put(CONSTRAINT_KEY_PATTERN, regexPatternString);
            }
        }
        
        // The value of the field must exactly match a value in the enum array.
        if(this.constraints.containsKey(CONSTRAINT_KEY_ENUM)){
            boolean violatesEnumConstraint = true;
            
            if(value instanceof String){
                List<String> stringList = (List<String>)this.constraints.get(CONSTRAINT_KEY_ENUM); 
                
                Iterator<String> iter = stringList.iterator();
                while(iter.hasNext()){
                    if(iter.next().equalsIgnoreCase((String)value)){
                        violatesEnumConstraint = false;
                        break;
                    }
                }
                
            }else if(value instanceof JsonNode){
                List<JsonNode> jsonObjList = (List<JsonNode>)this.constraints.get(CONSTRAINT_KEY_ENUM);
                
                Iterator<JsonNode> iter = jsonObjList.iterator();
                while(iter.hasNext()){
                    if(iter.next().equals((JsonNode)value)){
                        violatesEnumConstraint = false;
                        break;
                    }
                }
                
            }else if(value instanceof Integer){
                List<Integer> intList = (List<Integer>)this.constraints.get(CONSTRAINT_KEY_ENUM);
                
                Iterator<Integer> iter = intList.iterator();
                while(iter.hasNext()){
                    if(iter.next() == (int)value){
                        violatesEnumConstraint = false;
                        break;
                    }
                }
                
            }else if(value instanceof DateTime){
                List<DateTime> dateTimeList = (List<DateTime>)this.constraints.get(CONSTRAINT_KEY_ENUM);
                
                Iterator<DateTime> iter = dateTimeList.iterator();
                while(iter.hasNext()){
                    if(iter.next().compareTo((DateTime)value) == 0){
                        violatesEnumConstraint = false;
                        break;
                    }
                }
                
            }else if(value instanceof Duration){
                List<Duration> durationList = (List<Duration>)this.constraints.get(CONSTRAINT_KEY_ENUM);
                
                Iterator<Duration> iter = durationList.iterator();
                while(iter.hasNext()){
                    if(iter.next().compareTo((Duration)value) == 0){
                        violatesEnumConstraint = false;
                        break;
                    }
                }
                
            }
            
            if(violatesEnumConstraint){
                violatedConstraints.put(CONSTRAINT_KEY_ENUM, this.constraints.get(CONSTRAINT_KEY_ENUM));
            }
        }
        
        return violatedConstraints;
    }

    public static Field forType(String type, String name) {
        Field field = null;
        Class<?> clazz = null;
        try {
            String className =
                    "io.frictionlessdata.tableschema.field."
                            + type.substring(0, 1).toUpperCase() + type.substring(1)
                            + "Field";
            clazz = Class.forName(className);
            field = (Field)clazz.newInstance();
            field.name = name;
            field.type = type;
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
            // didn't find a field definition, just return as
            // AnyField
        } catch (ClassNotFoundException ex) {
            field = new AnyField(name);
        }
        return field;
    }

    /**
     * Get the JSON representation of the Field.
     * @return String-serialized JSON Object containing the properties of this field
     */
    @JsonIgnore
    public String getJson(){
        return JsonUtil.getInstance().serialize(this);
    }


    @JsonIgnore
    public String getCastMethodName() {
        return "cast" + (this.type.substring(0, 1).toUpperCase() + this.type.substring(1));
    }

    public String getName(){
        return this.name;
    }
    
    public String getType(){
    	if(Objects.nonNull(this.type) && !isWellKnownType(this.type)) {
    		return FIELD_TYPE_ANY;
    	} else return this.type;
    }

	public String getFormat(){
        return this.format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getTitle(){
        return this.title;
    }
    
    public String getDescription(){
        return this.description;
    }
    
    public Map<String, Object> getConstraints(){
        return this.constraints;
    }

    public URI getRdfType() {
        return rdfType;
    }

    public void setRdfType(URI rdfType) {
        this.rdfType = rdfType;
    }

    public Map<String, Object> getOptions() {
        return options;
    }

    public void setOptions(Map<String, Object> options) {
        this.options = options;
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
    public boolean similar(Field other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        if ((!StringUtils.isEmpty(name)) && (!StringUtils.isEmpty(other.name))){
            if (!name.equals(other.name))
                return false;
        }
        if ((!StringUtils.isEmpty(type)) && (!StringUtils.isEmpty(other.type))){
            if (!type.equals(other.type))
                return false;
        }
        return Objects.equals(constraints, other.constraints);
    }
    

    private boolean isWellKnownType(String typeName) {
		return wellKnownFieldTypes.contains(typeName);
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Field field = (Field) o;
        return name.equalsIgnoreCase(field.name) &&
                type.equals(field.type) &&
                Objects.equals(format, field.format) &&
                Objects.equals(constraints, field.constraints);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, format, constraints);
    }

    @Override
    public String toString() {
        Class clazz = this.getClass();
        return clazz.getName().replace(clazz.getPackage().getName(), "")
                +" {" +
                "name='" + name + '\'' +
                ", format='" + format + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}
