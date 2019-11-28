package io.frictionlessdata.tableschema;

import io.frictionlessdata.tableschema.exceptions.ForeignKeyException;
import io.frictionlessdata.tableschema.exceptions.InvalidCastException;
import io.frictionlessdata.tableschema.exceptions.PrimaryKeyException;
import io.frictionlessdata.tableschema.fk.ForeignKey;
import io.frictionlessdata.tableschema.fk.Reference;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.everit.json.schema.ValidationException;
import org.joda.time.DateTime;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.instanceOf;


/**
 *
 * 
 */
public class SchemaTest {
    
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void testCreateSchemaFromValidSchemaJson() throws Exception {
        JSONObject schemaJsonObj = new JSONObject();
       
        schemaJsonObj.put("fields", new JSONArray());
        Field nameField = new Field("id", Field.FIELD_TYPE_INTEGER);
        schemaJsonObj.getJSONArray("fields").put(nameField.getJson());
        
        Schema validSchema = new Schema(schemaJsonObj.toString(), true);
        Assert.assertTrue(validSchema.isValid());
    }
    
    @Test
    public void testCreateSchemaFromInvalidSchemaJson() throws Exception {
        JSONObject schemaJsonObj = new JSONObject();
       
        schemaJsonObj.put("fields", new JSONArray());
        Field nameField = new Field("id", Field.FIELD_TYPE_INTEGER);
        Field invalidField = new Field("coordinates", "invalid");
        schemaJsonObj.getJSONArray("fields").put(nameField.getJson());
        schemaJsonObj.getJSONArray("fields").put(invalidField.getJson());
        
        exception.expect(ValidationException.class);
        Schema invalidSchema = new Schema(schemaJsonObj.toString(), true);
        
    }
    
    @Test
    public void testCreateSchemaFromInvalidSchemaJsonWithoutStrictValidation() throws Exception{  
        JSONObject schemaJsonObj = new JSONObject();
       
        schemaJsonObj.put("fields", new JSONArray());
        Field nameField = new Field("id", Field.FIELD_TYPE_INTEGER);
        Field invalidField = new Field("coordinates", "invalid");
        schemaJsonObj.getJSONArray("fields").put(nameField.getJson());
        schemaJsonObj.getJSONArray("fields").put(invalidField.getJson());
        
        Schema invalidSchema = new Schema(schemaJsonObj.toString(), false); // strict=false
        
        Assert.assertEquals(Field.FIELD_TYPE_INTEGER, invalidSchema.getField("id").getType()); 
        Assert.assertEquals("invalid", invalidSchema.getField("coordinates").getType());
        
    }
    
    public void testIsValid(){  
        Schema schema = new Schema();
        
        Field idField = new Field("id", Field.FIELD_TYPE_INTEGER);
        schema.addField(idField);
        
        Assert.assertTrue(schema.isValid());
    }
    
    
    @Test
    public void testCreateSchemaFromValidSchemaUrl() throws Exception{
        URL url = new URL("https://raw.githubusercontent.com/frictionlessdata/tableschema-java/master/src/test/resources/fixtures/simple_schema.json");
        
        Schema validSchema = new Schema(url, true);
        Assert.assertTrue(validSchema.isValid());
    }
    
    @Test
    public void testCreateSchemaFromBadUrl() throws Exception{ 
        URL url = new URL("https://raw.githubusercontent.com/frictionlessdata/tableschema-java/BAD/URL/simple_schema.json");
        
        exception.expect(Exception.class);
        Schema schema = new Schema(url, true);
    }
    
    @Test
    public void testCreateSchemaFromFileWithValidPrimaryKey() throws Exception{
        File source = getResourceFile("/fixtures/primarykey/simple_schema_with_valid_pk.json");
        Schema schemaWithValidPK = new Schema(source, true);
        
        Assert.assertEquals("id", schemaWithValidPK.getPrimaryKey());
    }
    
    @Test
    public void testCreateSchemaFromFileWithInvalidPrimaryKey() throws Exception{
        File source = getResourceFile("/fixtures/primarykey/simple_schema_with_invalid_pk.json");
        
        exception.expect(PrimaryKeyException.class);
        new Schema(source, true);
    }
    
    @Test
    public void testCreateSchemaFromFileWithValidCompositeKey() throws Exception{
        File source = getResourceFile("/fixtures/primarykey/simple_schema_with_valid_ck.json");
        Schema schemaWithValidCK = new Schema(source, false); //FIXME: Why does this primary key fail validation when strict=true
        
        String[] compositePrimaryKey = schemaWithValidCK.getPrimaryKey();
        Assert.assertEquals("name", compositePrimaryKey[0]);
        Assert.assertEquals("surname", compositePrimaryKey[1]);
        
    }
    
    @Test
    public void testCreateSchemaFromFileWithInvalidCompositeKey() throws Exception{
        File source = getResourceFile("/fixtures/primarykey/simple_schema_with_invalid_ck.json");
        
        exception.expect(PrimaryKeyException.class);
        new Schema(source, true);
    }
     
    @Test
    public void testAddValidField(){
        Field nameField = new Field("id", Field.FIELD_TYPE_INTEGER);
        Schema validSchema = new Schema();
        validSchema.addField(nameField);
        
        Assert.assertEquals(1, validSchema.getFields().size()); 
    }
    
    @Test
    public void testAddInvalidField(){
        Field idField = new Field("id", Field.FIELD_TYPE_INTEGER);
        Field invalidField = new Field("title", "invalid");
        Field geopointField = new Field("coordinates", Field.FIELD_TYPE_GEOPOINT); 
        
        Schema schema = new Schema(); // strict=false by default
        
        // Add a valid field.
        schema.addField(idField);
        
        // Add an invalid field.
        // Won't be ignored because strict=false by default but error will be saved in error list.
        schema.addField(invalidField);
        
        // Add an invalid field.
        // Won't be ignored because strict=false by default but error will be saved in error list.
        schema.addField(geopointField);
        
        Assert.assertEquals(3, schema.getFields().size());
        Assert.assertEquals(2, schema.getErrors().size());
        Assert.assertNotNull(schema.getField("title"));
        Assert.assertNotNull(schema.getField("id"));
        Assert.assertNotNull(schema.getField("coordinates"));
    }
    
    @Test
    public void hasField(){
        Schema schema = new Schema();
        Assert.assertFalse(schema.hasFields());
        
        Field idField = new Field("id", Field.FIELD_TYPE_INTEGER);
        schema.addField(idField);
        Assert.assertTrue(schema.hasFields());
    }
    
    
    @Test
    public void testCastRow() throws Exception{
        Schema schema = new Schema();
        
        // String
        Field fieldString = new Field("fieldString", Field.FIELD_TYPE_STRING);
        schema.addField(fieldString);
        
        // Integer
        Field fieldInteger = new Field("fieldInteger", Field.FIELD_TYPE_INTEGER);
        schema.addField(fieldInteger);
        
        // Boolean
        Field fieldBoolean = new Field("fieldBoolean", Field.FIELD_TYPE_BOOLEAN);
        schema.addField(fieldBoolean);

        // Object
        Field fieldObject = new Field("fieldObject", Field.FIELD_TYPE_OBJECT);
        schema.addField(fieldObject);
        
        // Array
        Field fieldArray = new Field("fieldArray", Field.FIELD_TYPE_ARRAY);
        schema.addField(fieldArray);
        
        // Date
        Field fieldDate = new Field("fieldDate", Field.FIELD_TYPE_DATE);
        schema.addField(fieldDate);
        
        // Time
        Field fieldTime = new Field("fieldTime", Field.FIELD_TYPE_TIME);
        schema.addField(fieldTime);
        
        // Datetime
        Field fieldDatetime = new Field("fieldDatetime", Field.FIELD_TYPE_DATETIME);
        schema.addField(fieldDatetime);
        
        // Year
        Field fieldYear = new Field("fieldYear", Field.FIELD_TYPE_YEAR);
        schema.addField(fieldYear);
        
        // Yearmonth
        Field fieldYearmonth = new Field("fieldYearmonth", Field.FIELD_TYPE_YEARMONTH);
        schema.addField(fieldYearmonth);
        
        // Duration
        Field fieldDuration = new Field("fieldDuration", Field.FIELD_TYPE_DURATION);
        schema.addField(fieldDuration);
        
        // Number
        // TODO: Implement
        
        // Geopoint
        // TODO: Implement
        
        // Geojson
        // TODO: Implement
        
        
        String[] row = new String[]{
            "John Doe", // String
            "25", // Integer
            "T", // Boolean
            "{\"one\": 1, \"two\": 2, \"three\": 3}", // Object
            "[1,2,3,4]", // Array
            "2008-08-30", // Date
            "14:22:33", // Time
            "2008-08-30T01:45:36.123Z", // Datetime
            "2008", // Year
            "2008-08", // Yearmonth
            "P2DT3H4M"  // Duration
            // Number
            // Geopoint
            // Geojson
        };
        
        Object[] castRow = schema.castRow(row);
       
        assertThat(castRow[0], instanceOf(String.class));
        assertThat(castRow[1], instanceOf(Integer.class));
        assertThat(castRow[2], instanceOf(Boolean.class));
        assertThat(castRow[3], instanceOf(JSONObject.class));
        assertThat(castRow[4], instanceOf(JSONArray.class));
        assertThat(castRow[5], instanceOf(DateTime.class));
        assertThat(castRow[6], instanceOf(DateTime.class));
        assertThat(castRow[7], instanceOf(DateTime.class));
        assertThat(castRow[8], instanceOf(Integer.class));
        assertThat(castRow[9], instanceOf(DateTime.class));
        assertThat(castRow[10], instanceOf(Duration.class));
    }
    
    @Test
    public void testCastRowWithInvalidLength() throws Exception{
        Schema schema = new Schema();
        
        Field fieldString = new Field("name", Field.FIELD_TYPE_STRING);
        schema.addField(fieldString);
        
        Field fieldInteger = new Field("id", Field.FIELD_TYPE_INTEGER);
        schema.addField(fieldInteger);
        
        String[] row = new String[]{"John Doe", "25", "T"}; // length is 3 instead of 2.
        
        exception.expect(InvalidCastException.class);
        schema.castRow(row);
    }
    
    @Test
    public void testCastRowWithInvalidValue() throws Exception{
        Schema schema = new Schema();
        
        Field fieldString = new Field("name", Field.FIELD_TYPE_STRING);
        schema.addField(fieldString);
        
        Field fieldInteger = new Field("id", Field.FIELD_TYPE_INTEGER);
        schema.addField(fieldInteger);
        
        String[] row = new String[]{"John Doe", "25 String"};
        
        exception.expect(InvalidCastException.class);
        schema.castRow(row);
    }
    
    @Test
    public void testSaveWithField() throws Exception{
        File createdFile = folder.newFile("test_schema.json");
        
        Schema createdSchema = new Schema(); 
        
        // Fields with Constraints
        Map<String, Object> intFieldConstraints = new HashMap();
        intFieldConstraints.put(Field.CONSTRAINT_KEY_REQUIRED, true);
                
        Field intField = new Field("id", Field.FIELD_TYPE_INTEGER, Field.FIELD_FORMAT_DEFAULT, null, null, intFieldConstraints);
        createdSchema.addField(intField);
        
        Map<String, Object> stringFieldConstraints = new HashMap();
        stringFieldConstraints.put(Field.CONSTRAINT_KEY_MIN_LENGTH, 36);
        stringFieldConstraints.put(Field.CONSTRAINT_KEY_MAX_LENGTH, 45);
        
        Field stringField = new Field("name", Field.FIELD_TYPE_STRING, Field.FIELD_FORMAT_DEFAULT, "the title", "the description", stringFieldConstraints);
        createdSchema.addField(stringField);

        // Save schema
        createdSchema.writeJson(createdFile);
        
        Schema readSchema = new Schema(createdFile, true);
        
        // Assert id field
        Assert.assertEquals(Field.FIELD_TYPE_INTEGER, readSchema.getField("id").getType());
        Assert.assertEquals(Field.FIELD_FORMAT_DEFAULT, readSchema.getField("id").getFormat());
        Assert.assertNull(readSchema.getField("id").getTitle());
        Assert.assertNull(readSchema.getField("id").getDescription());
        Assert.assertTrue((boolean)readSchema.getField("id").getConstraints().get(Field.CONSTRAINT_KEY_REQUIRED));
        
        // Assert name field
        Assert.assertEquals(Field.FIELD_TYPE_STRING, readSchema.getField("name").getType());
        Assert.assertEquals(Field.FIELD_FORMAT_DEFAULT, readSchema.getField("name").getFormat());
        Assert.assertEquals("the title", readSchema.getField("name").getTitle());
        Assert.assertEquals("the description", readSchema.getField("name").getDescription());
        Assert.assertEquals(36, readSchema.getField("name").getConstraints().get(Field.CONSTRAINT_KEY_MIN_LENGTH));
        Assert.assertEquals(45, readSchema.getField("name").getConstraints().get(Field.CONSTRAINT_KEY_MAX_LENGTH));
    }
    
    @Test
    public void testSaveWithPrimaryKey() throws Exception{
        File createdFile = folder.newFile("test_schema.json");
        
        Schema createdSchema = new Schema(true); 
        
        Field intField = new Field("id", Field.FIELD_TYPE_INTEGER, Field.FIELD_FORMAT_DEFAULT);
        createdSchema.addField(intField);
        
        Field stringField = new Field("name", Field.FIELD_TYPE_STRING, Field.FIELD_FORMAT_DEFAULT);
        createdSchema.addField(stringField);
        
        // Primary Key
        createdSchema.setPrimaryKey("id");
        
        // Save schema
        createdSchema.writeJson(createdFile);
        
        Schema readSchema = new Schema(createdFile, true);
        
        // Assert Primary Key
        Assert.assertEquals("id", readSchema.getPrimaryKey());
    }
    
    @Test
    public void testSaveWithForeignKey() throws Exception{
        File createdFile = folder.newFile("test_schema.json");
        
        Schema createdSchema = new Schema(); 
        
        Field intField = new Field("id", Field.FIELD_TYPE_INTEGER, Field.FIELD_FORMAT_DEFAULT);
        createdSchema.addField(intField);
        
        Field stringField = new Field("name", Field.FIELD_TYPE_STRING, Field.FIELD_FORMAT_DEFAULT);
        createdSchema.addField(stringField);
        
        // Foreign Keys
        Reference ref = new Reference(new URL("http://data.okfn.org/data/mydatapackage/"), "resource", "name");
        ForeignKey fk = new ForeignKey("name", ref, true);
        createdSchema.addForeignKey(fk);
        
        // Save schema
        createdSchema.writeJson(createdFile);
        
        Schema readSchema = new Schema(createdFile, true);
        
        // Assert Foreign Keys
        Assert.assertEquals("name", readSchema.getForeignKeys().get(0).getFields());
        Assert.assertEquals("http://data.okfn.org/data/mydatapackage/", readSchema.getForeignKeys().get(0).getReference().getDatapackage().toString());
        Assert.assertEquals("resource", readSchema.getForeignKeys().get(0).getReference().getResource());
    }
    
    @Test
    public void testSinglePrimaryKey() throws PrimaryKeyException{
        Schema schema = new Schema(true);
        
        Field idField = new Field("id", Field.FIELD_TYPE_INTEGER);
        schema.addField(idField);
        
        schema.setPrimaryKey("id");
        String key = schema.getPrimaryKey();
        
        Assert.assertEquals("id", key);
    }
    
    @Test
    public void testInvalidSinglePrimaryKey() throws PrimaryKeyException{
        Schema schema = new Schema(true);
        
        Field idField = new Field("id", Field.FIELD_TYPE_INTEGER);
        schema.addField(idField);
        
        exception.expect(PrimaryKeyException.class);
        schema.setPrimaryKey("invalid");
    }
    
    @Test
    public void testCompositePrimaryKey() throws PrimaryKeyException{
        Schema schema = new Schema(true);
        
        Field idField = new Field("id", Field.FIELD_TYPE_INTEGER);
        schema.addField(idField);
        
        Field nameField = new Field("name", Field.FIELD_TYPE_STRING);
        schema.addField(nameField);
        
        Field surnameField = new Field("surname", Field.FIELD_TYPE_STRING);
        schema.addField(surnameField);

        schema.setPrimaryKey(new String[]{"name", "surname"});
        String[] compositeKey = schema.getPrimaryKey();
        
        Assert.assertEquals("name", compositeKey[0]);
        Assert.assertEquals("surname", compositeKey[1]);  
    }
    
    @Test
    public void testInvalidCompositePrimaryKey() throws PrimaryKeyException{
        Schema schema = new Schema(true);
        
        Field idField = new Field("id", Field.FIELD_TYPE_INTEGER);
        schema.addField(idField);
        
        Field nameField = new Field("name", Field.FIELD_TYPE_STRING);
        schema.addField(nameField);
        
        Field surnameField = new Field("surname", Field.FIELD_TYPE_STRING);
        schema.addField(surnameField);

        exception.expect(PrimaryKeyException.class);
        schema.setPrimaryKey(new String[]{"name", "invalid"}); 
    }
    
    @Test
    public void testInvalidCompositePrimaryKeyWithoutStrictValidation() throws PrimaryKeyException{
        Schema schema = new Schema();
        
        Field idField = new Field("id", Field.FIELD_TYPE_INTEGER);
        schema.addField(idField);
        
        Field nameField = new Field("name", Field.FIELD_TYPE_STRING);
        schema.addField(nameField);
        
        Field surnameField = new Field("surname", Field.FIELD_TYPE_STRING);
        schema.addField(surnameField);

        String[] compositeKey = new String[]{"name", "invalid"};
        schema.setPrimaryKey(compositeKey); // strict=false
        
        String[] fetchedCompositeKey = schema.getPrimaryKey();
        Assert.assertEquals("name", fetchedCompositeKey[0]);
        Assert.assertEquals("invalid", fetchedCompositeKey[1]);
    }
    
    @Test
    public void testInvalidForeignKeyArray() throws PrimaryKeyException, ForeignKeyException, Exception{  
        File source = getResourceFile("/fixtures/foreignkeys/schema_invalid_fk_array.json");
        
        exception.expectMessage("The reference's fields property must be an array if the outer fields is an array.");
        new Schema(source, true);
    }
    
    @Test
    public void testInvalidForeignKeyArrayString() throws PrimaryKeyException, ForeignKeyException, Exception{  
        File source = getResourceFile("/fixtures/foreignkeys/schema_invalid_fk_array_string.json");
 
        exception.expectMessage("The reference's fields property must be a string if the outer fields is a string.");
        new Schema(source, true);
    }
    
    @Test
    public void testInvalidForeignKeyArrayStringRef() throws PrimaryKeyException, ForeignKeyException, Exception{
        File source = getResourceFile("/fixtures/foreignkeys/schema_invalid_fk_array_string_ref.json");
        
        exception.expectMessage("The reference's fields property must be an array if the outer fields is an array.");
        new Schema(source, true);
    }
    
    @Test
    public void testInvalidForeignKeyArrayWrongNumber() throws PrimaryKeyException, ForeignKeyException, Exception{
        File source = getResourceFile("/fixtures/foreignkeys/schema_invalid_fk_array_wrong_number.json");
        
        exception.expectMessage("The reference's fields property must be an array of the same length as that of the outer fields' array.");
        new Schema(source, true);
    }
    
    @Test
    public void testInvalidForeignKeyNoReference() throws PrimaryKeyException, ForeignKeyException, Exception{
        File source = getResourceFile("/fixtures/foreignkeys/schema_invalid_fk_no_reference.json");
        
        exception.expectMessage("A foreign key must have the fields and reference properties.");
        Schema schema = new Schema(source, true);
    }
    
    @Test
    public void testInvalidForeignKeyString() throws PrimaryKeyException, ForeignKeyException, Exception{
        File source = getResourceFile("/fixtures/foreignkeys/schema_invalid_fk_string.json");
        
        exception.expect(ValidationException.class);
        new Schema(source, true);
    }
    
    @Test
    public void testInvalidForeignKeyStringArrayRef() throws PrimaryKeyException, ForeignKeyException, Exception{
        File source = getResourceFile("/fixtures/foreignkeys/schema_invalid_fk_string_array_ref.json");
        
        exception.expectMessage("The reference's fields property must be a string if the outer fields is a string.");
        new Schema(source, true);
    }
    
    @Test
    public void testValidForeignKeyArray() throws PrimaryKeyException, ForeignKeyException, Exception{
        File source = getResourceFile("/fixtures/foreignkeys/schema_valid_fk_array.json");
        Schema schema = new Schema(source, true);

        JSONArray parsedFields = schema.getForeignKeys().get(0).getFields();
        Assert.assertEquals("id", parsedFields.getString(0));
        Assert.assertEquals("title", parsedFields.getString(1));
        
        JSONArray refFields = schema.getForeignKeys().get(0).getReference().getFields();  
        Assert.assertEquals("fk_id", refFields.getString(0));
        Assert.assertEquals("title_id", refFields.getString(1));
    }
    
    @Test
    public void testValidForeignKeyString() throws PrimaryKeyException, ForeignKeyException, Exception{
        File source = getResourceFile("/fixtures/foreignkeys/schema_valid_fk_string.json");
        Schema schema = new Schema(source, true);
        
        Assert.assertEquals("position_title", schema.getForeignKeys().get(0).getFields());
        Assert.assertEquals("positions", schema.getForeignKeys().get(0).getReference().getResource());
        Assert.assertEquals("name", schema.getForeignKeys().get(0).getReference().getFields());
    }

    private static File getResourceFile(String fileName) throws URISyntaxException {
        try {
            // Create file-URL of source file:
            URL sourceFileUrl = SchemaTest.class.getResource(fileName);
            // normal case: resolve against resources path
            Path path = Paths.get(sourceFileUrl.toURI());
            return path.toFile();
        } catch (NullPointerException ex) {
            // special case for invalid path test
            return new File (fileName);
        }
    }
}
