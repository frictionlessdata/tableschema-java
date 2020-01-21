package io.frictionlessdata.tableschema.schema;

import io.frictionlessdata.tableschema.Table;
import io.frictionlessdata.tableschema.TestHelper;
import io.frictionlessdata.tableschema.beans.EmployeeBean;
import io.frictionlessdata.tableschema.exception.ForeignKeyException;
import io.frictionlessdata.tableschema.exception.InvalidCastException;
import io.frictionlessdata.tableschema.exception.PrimaryKeyException;
import io.frictionlessdata.tableschema.field.*;
import io.frictionlessdata.tableschema.fk.ForeignKey;
import io.frictionlessdata.tableschema.fk.Reference;
import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.everit.json.schema.ValidationException;
import org.joda.time.DateTime;

import static io.frictionlessdata.tableschema.TestHelper.getTestDataDirectory;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;


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
        Field nameField = new IntegerField("id");
        String schemaJson = "{\"fields\": [" + nameField.getJson() + "]}";
        Schema validSchema = Schema.fromJson(schemaJson, true);
        Assert.assertTrue(validSchema.isValid());
    }
    /*
    @Test
    public void testCreateSchemaFromInvalidSchemaJson() throws Exception {
        JSONObject schemaJsonObj = new JSONObject();

        schemaJsonObj.put("fields", new JSONArray());
        Field nameField = new IntegerField("id");
        Field invalidField = new Field("coordinates", "invalid");
        schemaJsonObj.getJSONArray("fields").put(nameField.getJson());
        schemaJsonObj.getJSONArray("fields").put(invalidField.getJson());

        exception.expect(ValidationException.class);
        new Schema(schemaJsonObj.toString(), true);
    }
*/

    @Test
    public void testReadFromInValidSchemaFileWithStrictValidation() throws Exception{
        File f = new File(TestHelper.getTestDataDirectory(), "schema/invalid_population_schema.json");
        exception.expect(ValidationException.class);
        Schema.fromJson(f, true);
    }

    /*
    @Test
    public void testCreateSchemaFromInvalidSchemaJsonWithoutStrictValidation() throws Exception{
        JSONObject schemaJsonObj = new JSONObject();

        schemaJsonObj.put("fields", new JSONArray());
        Field nameField = new IntegerField("id");
        Field invalidField = new Field("coordinates", "invalid");
        schemaJsonObj.getJSONArray("fields").put(nameField.getJson());
        schemaJsonObj.getJSONArray("fields").put(invalidField.getJson());

        Schema invalidSchema = new Schema(schemaJsonObj.toString(), false); // strict=false

        Assert.assertEquals(Field.FIELD_TYPE_INTEGER, invalidSchema.getField("id").getType());
        Assert.assertEquals("invalid", invalidSchema.getField("coordinates").getType());

    }*/

    @Test
    public void testIsValid(){
        Schema schema = new Schema();

        Field idField = new IntegerField("id");
        schema.addField(idField);

        Assert.assertTrue(schema.isValid());
    }


    @Test
    public void testCreateSchemaFromValidSchemaUrl() throws Exception{
        URL url = new URL("https://raw.githubusercontent.com/frictionlessdata/tableschema-java/" +
                "master/src/test/resources/fixtures/schema/simple_schema.json");

        Schema validSchema = Schema.fromJson(url, true);
        Assert.assertTrue(validSchema.isValid());
    }

    @Test
    public void testCreateSchemaFromBadUrl() throws Exception{
        URL url = new URL("https://raw.githubusercontent.com/frictionlessdata/" +
                "tableschema-java/BAD/URL/simple_schema.json");

        exception.expect(Exception.class);
        Schema.fromJson(url, true);
    }


    @Test
    public void testReadSchemaFieldNames() throws Exception{
        File source = getResourceFile("/fixtures/schema/population_schema.json");
        Schema schema = Schema.fromJson(source, true);

        List<String> fieldNames = schema.getFieldNames();
        Object[] namesArr = fieldNames.toArray();
        String[] testArr = new String[]{"city", "year", "population"};
        Assert.assertArrayEquals(testArr, namesArr);
    }


    @Test
    public void testCreateFromListOfFields() throws Exception {
        List<Field> fields = new ArrayList<>();

        Field fieldString = new StringField("fieldString");
        fields.add(fieldString);
        Field fieldInteger = new IntegerField("fieldInteger");
        fields.add(fieldInteger);
        Field fieldBoolean = new BooleanField("fieldBoolean");
        fields.add(fieldBoolean);

        Schema schema = new Schema(fields, true);
        assertEquals(3, schema.getFields().size());
        assertEquals(fields, schema.getFields());
    }

    @Test
    public void testCreateSchemaFromFileWithValidPrimaryKey() throws Exception{
        File source = getResourceFile("/fixtures/primarykey/simple_schema_with_valid_pk.json");
        Schema schemaWithValidPK = Schema.fromJson(source, true);

        assertEquals("id", schemaWithValidPK.getPrimaryKey());
    }

    @Test
    public void testCreateSchemaFromFileWithInvalidPrimaryKey() throws Exception{
        File source = getResourceFile("/fixtures/primarykey/simple_schema_with_invalid_pk.json");

        exception.expect(PrimaryKeyException.class);
        Schema.fromJson(source, true);
    }

    @Test
    public void testCreateSchemaFromFileWithValidCompositeKey() throws Exception{
        File source = getResourceFile("/fixtures/primarykey/simple_schema_with_valid_ck.json");
        Schema schemaWithValidCK = Schema.fromJson(source, true);

        String[] compositePrimaryKey = schemaWithValidCK.getPrimaryKey();
        assertEquals("name", compositePrimaryKey[0]);
        assertEquals("surname", compositePrimaryKey[1]);

    }

    @Test
    public void testCreateSchemaFromFileWithInvalidCompositeKey() throws Exception{
        File source = getResourceFile("/fixtures/primarykey/simple_schema_with_invalid_ck.json");

        exception.expect(PrimaryKeyException.class);
        Schema.fromJson (source, true);
    }

    @Test
    public void testAddValidField(){
        Field nameField = new IntegerField("id");
        Schema validSchema = new Schema();
        validSchema.addField(nameField);

        assertEquals(1, validSchema.getFields().size());
    }

    @Test
    public void testRetrieveNonexistingField(){
        Field nameField = new IntegerField("id");
        Schema validSchema = new Schema();
        validSchema.addField(nameField);

        Assert.assertNull(validSchema.getField("lksajdf"));
    }

    @Test
    public void testAddValidFieldAsJson(){
        Field nameField = new IntegerField("id");
        Schema validSchema = new Schema();
        validSchema.addField(nameField.getJson());
        Field foundNameField = validSchema.getField("id");

        assertEquals(nameField, foundNameField);
    }


    @Test
    public void hasField(){
        Schema schema = new Schema();
        Assert.assertFalse(schema.hasFields());

        Field idField = new IntegerField("id");
        schema.addField(idField);
        Assert.assertTrue(schema.hasFields());
    }

    @Test
    public void hasSetField(){
        Schema schema = new Schema();
        Assert.assertFalse(schema.hasFields());

        Field idField = new IntegerField("id");
        schema.addField(idField);
        Assert.assertTrue(schema.hasField("id"));
        Assert.assertFalse(schema.hasField(null));
        Assert.assertFalse(schema.hasField("sdfsd"));
    }


    @Test
    public void testCastRow() throws Exception{
        Schema schema = new Schema();

        // String
        Field fieldString = new StringField("fieldString");
        schema.addField(fieldString);

        // Integer
        Field fieldInteger = new IntegerField("fieldInteger");
        schema.addField(fieldInteger);

        // Boolean
        Field fieldBoolean = new BooleanField("fieldBoolean");
        schema.addField(fieldBoolean);

        // Object
        Field fieldObject = new ObjectField("fieldObject");
        schema.addField(fieldObject);

        // Array
        Field fieldArray = new ArrayField("fieldArray");
        schema.addField(fieldArray);

        // Date
        Field fieldDate = new DateField("fieldDate");
        schema.addField(fieldDate);

        // Time
        Field fieldTime = new TimeField("fieldTime");
        schema.addField(fieldTime);

        // Datetime
        Field fieldDatetime = new DatetimeField("fieldDatetime");
        schema.addField(fieldDatetime);

        // Year
        Field fieldYear = new YearField("fieldYear");
        schema.addField(fieldYear);

        // Yearmonth
        Field fieldYearmonth = new YearmonthField("fieldYearmonth");
        schema.addField(fieldYearmonth);

        // Duration
        Field fieldDuration = new DurationField("fieldDuration");
        schema.addField(fieldDuration);

        // Number
        Field fieldNumber = new NumberField("fieldNumber");
        schema.addField(fieldNumber);

        // Number
        Field fieldGeopoint = new GeopointField("fieldGeopoint");
        schema.addField(fieldGeopoint);

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
            "P2DT3H4M",  // Duration
            "123.32",
            "1.123, 4565.34"
            // Geojson
        };

        Object[] castRow = schema.castRow(row);

        assertThat(castRow[0], instanceOf(String.class));
        assertThat(castRow[1], instanceOf(BigInteger.class));
        assertThat(castRow[2], instanceOf(Boolean.class));
        assertThat(castRow[3], instanceOf(Map.class));
        assertTrue(Map.class.isAssignableFrom(castRow[3].getClass()));
        Map<String, Object> obj = (Map<String, Object>)castRow[3];
        assertTrue(obj.keySet().contains("one"));
        assertEquals(1, obj.get("one"));
        assertThat(castRow[4], instanceOf(Object[].class));
        assertThat(castRow[5], instanceOf(LocalDate.class));
        assertThat(castRow[6], instanceOf(DateTime.class));
        assertThat(castRow[7], instanceOf(ZonedDateTime.class));
        assertThat(castRow[8], instanceOf(Year.class));
        assertThat(castRow[9], instanceOf(YearMonth.class));
        assertThat(castRow[10], instanceOf(Duration.class));
        assertThat(castRow[11], instanceOf(BigDecimal.class));
        assertThat(castRow[12], instanceOf(double[].class));
    }

    @Test
    public void testCastRowWithInvalidLength() throws Exception{
        Schema schema = new Schema();

        Field fieldString = new StringField("name");
        schema.addField(fieldString);

        Field fieldInteger = new IntegerField("id");
        schema.addField(fieldInteger);

        String[] row = new String[]{"John Doe", "25", "T"}; // length is 3 instead of 2.

        exception.expect(InvalidCastException.class);
        schema.castRow(row);
    }

    @Test
    public void testCastRowWithInvalidValue() throws Exception{
        Schema schema = new Schema();

        Field fieldString = new StringField("name");
        schema.addField(fieldString);

        Field fieldInteger = new IntegerField("id");
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

        Field intField = new IntegerField(
                "id",
                Field.FIELD_FORMAT_DEFAULT,
                null,
                null,
                null,
                intFieldConstraints,
                null);
        createdSchema.addField(intField);

        Map<String, Object> stringFieldConstraints = new HashMap();
        stringFieldConstraints.put(Field.CONSTRAINT_KEY_MIN_LENGTH, 36);
        stringFieldConstraints.put(Field.CONSTRAINT_KEY_MAX_LENGTH, 45);

        Field stringField = new StringField(
                "name",
                Field.FIELD_FORMAT_DEFAULT,
                "the title",
                "the description",
                null,
                stringFieldConstraints,
                null);
        createdSchema.addField(stringField);

        // Save schema
        createdSchema.writeJson(createdFile);

        Schema readSchema = Schema.fromJson (createdFile, true);

        // Assert id field
        assertEquals(Field.FIELD_TYPE_INTEGER, readSchema.getField("id").getType());
        assertEquals(Field.FIELD_FORMAT_DEFAULT, readSchema.getField("id").getFormat());
        Assert.assertNull(readSchema.getField("id").getTitle());
        Assert.assertNull(readSchema.getField("id").getDescription());
        Assert.assertTrue((boolean)readSchema.getField("id").getConstraints().get(Field.CONSTRAINT_KEY_REQUIRED));

        // Assert name field
        assertEquals(Field.FIELD_TYPE_STRING, readSchema.getField("name").getType());
        assertEquals(Field.FIELD_FORMAT_DEFAULT, readSchema.getField("name").getFormat());
        assertEquals("the title", readSchema.getField("name").getTitle());
        assertEquals("the description", readSchema.getField("name").getDescription());
        assertEquals(36, readSchema.getField("name").getConstraints().get(Field.CONSTRAINT_KEY_MIN_LENGTH));
        assertEquals(45, readSchema.getField("name").getConstraints().get(Field.CONSTRAINT_KEY_MAX_LENGTH));
    }

    @Test
    public void testSaveWithPrimaryKey() throws Exception{
        File createdFile = folder.newFile("test_schema.json");

        Schema createdSchema = new Schema(true);

        Field intField = new IntegerField("id", Field.FIELD_FORMAT_DEFAULT, null, null, null, null, null);
        createdSchema.addField(intField);

        Field stringField = new StringField("name", Field.FIELD_FORMAT_DEFAULT, null, null, null, null, null);
        createdSchema.addField(stringField);

        // Primary Key
        createdSchema.setPrimaryKey("id");

        // Save schema
        createdSchema.writeJson(createdFile);

        Schema readSchema = Schema.fromJson (createdFile, true);

        // Assert Primary Key
        assertEquals("id", readSchema.getPrimaryKey());
    }

    @Test
    public void testSaveWithForeignKey() throws Exception{
        File createdFile = folder.newFile("test_schema.json");

        Schema createdSchema = new Schema();

        Field intField = new IntegerField("id", Field.FIELD_FORMAT_DEFAULT, null, null, null, null, null);
        createdSchema.addField(intField);

        Field stringField = new StringField("name", Field.FIELD_FORMAT_DEFAULT, null, null, null, null, null);
        createdSchema.addField(stringField);

        // Foreign Keys
        Reference ref = new Reference(new URL("http://data.okfn.org/data/mydatapackage/"), "resource", "name");
        ForeignKey fk = new ForeignKey("name", ref, true);
        createdSchema.addForeignKey(fk);

        // Save schema
        createdSchema.writeJson(createdFile);

        Schema readSchema = Schema.fromJson (createdFile, true);

        // Assert Foreign Keys
        assertEquals("name", readSchema.getForeignKeys().get(0).getFields());
        assertEquals("http://data.okfn.org/data/mydatapackage/", readSchema.getForeignKeys().get(0).getReference().getDatapackage().toString());
        assertEquals("resource", readSchema.getForeignKeys().get(0).getReference().getResource());
    }

    @Test
    public void testSinglePrimaryKey() throws PrimaryKeyException{
        Schema schema = new Schema(true);

        Field idField = new IntegerField("id");
        schema.addField(idField);

        schema.setPrimaryKey("id");
        String key = schema.getPrimaryKey();

        assertEquals("id", key);
    }

    @Test
    public void testInvalidSinglePrimaryKey() throws PrimaryKeyException{
        Schema schema = new Schema(true);

        Field idField = new IntegerField("id");
        schema.addField(idField);

        exception.expect(PrimaryKeyException.class);
        schema.setPrimaryKey("invalid");
    }

    @Test
    public void testCompositePrimaryKey() throws PrimaryKeyException{
        Schema schema = new Schema(true);

        Field idField = new IntegerField("id");
        schema.addField(idField);

        Field nameField = new StringField("name");
        schema.addField(nameField);

        Field surnameField = new StringField("surname");
        schema.addField(surnameField);

        schema.setPrimaryKey(new String[]{"name", "surname"});
        String[] compositeKey = schema.getPrimaryKey();

        assertEquals("name", compositeKey[0]);
        assertEquals("surname", compositeKey[1]);
    }

    @Test
    public void testInvalidCompositePrimaryKey() throws PrimaryKeyException{
        Schema schema = new Schema(true);

        Field idField = new IntegerField("id");
        schema.addField(idField);

        Field nameField = new StringField("name");
        schema.addField(nameField);

        Field surnameField = new StringField("surname");
        schema.addField(surnameField);

        exception.expect(PrimaryKeyException.class);
        schema.setPrimaryKey(new String[]{"name", "invalid"});
    }

    @Test
    public void testInvalidCompositePrimaryKeyWithoutStrictValidation() throws PrimaryKeyException{
        Schema schema = new Schema(false);

        Field idField = new IntegerField("id");
        schema.addField(idField);

        Field nameField = new StringField("name");
        schema.addField(nameField);

        Field surnameField = new StringField("surname");
        schema.addField(surnameField);

        String[] compositeKey = new String[]{"name", "invalid"};
        schema.setPrimaryKey(compositeKey); // strict=false

        List<String> fetchedCompositeKey = schema.getPrimaryKeyParts();
        assertEquals("name", fetchedCompositeKey.get(0));
        assertEquals("invalid", fetchedCompositeKey.get(1));
    }

    @Test
    public void testInvalidForeignKeyArray() throws PrimaryKeyException, ForeignKeyException, Exception{
        File source = getResourceFile("/fixtures/foreignkeys/schema_invalid_fk_array.json");

        exception.expectMessage("The reference's fields property must be an array if the outer fields is an array.");
        Schema.fromJson (source, true);
    }

    @Test
    public void testInvalidForeignKeyArrayString() throws PrimaryKeyException, ForeignKeyException, Exception{
        File source = getResourceFile("/fixtures/foreignkeys/schema_invalid_fk_array_string.json");

        exception.expectMessage("The reference's fields property must be a string if the outer fields is a string.");
        Schema.fromJson (source, true);
    }

    @Test
    public void testInvalidForeignKeyArrayStringRef() throws PrimaryKeyException, ForeignKeyException, Exception{
        File source = getResourceFile("/fixtures/foreignkeys/schema_invalid_fk_array_string_ref.json");

        exception.expectMessage("The reference's fields property must be an array if the outer fields is an array.");
        Schema.fromJson (source, true);
    }

    @Test
    public void testInvalidForeignKeyArrayWrongNumber() throws PrimaryKeyException, ForeignKeyException, Exception{
        File source = getResourceFile("/fixtures/foreignkeys/schema_invalid_fk_array_wrong_number.json");

        exception.expectMessage("The reference's fields property must be an array of the same length as that of the outer fields' array.");
        Schema.fromJson (source, true);
    }

    @Test
    public void testInvalidForeignKeyNoReference() throws PrimaryKeyException, ForeignKeyException, Exception{
        File source = getResourceFile("/fixtures/foreignkeys/schema_invalid_fk_no_reference.json");

        exception.expectMessage("A foreign key must have the fields and reference properties.");
        Schema schema = Schema.fromJson (source, true);
    }

    @Test
    public void testInvalidForeignKeyString() throws PrimaryKeyException, ForeignKeyException, Exception{
        File source = getResourceFile("/fixtures/foreignkeys/schema_invalid_fk_string.json");

        exception.expect(ValidationException.class);
        Schema.fromJson (source, true);
    }

    @Test
    public void testInvalidForeignKeyStringArrayRef() throws PrimaryKeyException, ForeignKeyException, Exception{
        File source = getResourceFile("/fixtures/foreignkeys/schema_invalid_fk_string_array_ref.json");

        exception.expectMessage("The reference's fields property must be a string if the outer fields is a string.");
        Schema.fromJson (source, true);
    }

    @Test
    public void testValidForeignKeyArray() throws PrimaryKeyException, ForeignKeyException, Exception{
        File source = getResourceFile("/fixtures/foreignkeys/schema_valid_fk_array.json");
        Schema schema = Schema.fromJson (source, true);

        // TODO: change this test, after removal of org.json stuff from ForeignKey and Schema classes
        JSONArray parsedFields = schema.getForeignKeys().get(0).getFields();
        assertEquals("id", parsedFields.getString(0));
        assertEquals("title", parsedFields.getString(1));

        JSONArray refFields = schema.getForeignKeys().get(0).getReference().getFields();
        assertEquals("fk_id", refFields.getString(0));
        assertEquals("title_id", refFields.getString(1));
    }

    @Test
    public void testValidForeignKeyString() throws PrimaryKeyException, ForeignKeyException, Exception{
        File source = getResourceFile("/fixtures/foreignkeys/schema_valid_fk_string.json");
        Schema schema = Schema.fromJson (source, true);

        assertEquals("position_title", schema.getForeignKeys().get(0).getFields());
        assertEquals("positions", schema.getForeignKeys().get(0).getReference().getResource());
        assertEquals("name", schema.getForeignKeys().get(0).getReference().getFields());
    }

    @Test
    public void testInferTypesComplexSchema() throws Exception{
        Table table = Table.fromSource(new File ("data/employee_data.csv"), getTestDataDirectory());

        String schemaObjStr = table.inferSchema().getJson();
        Schema schema = Schema.fromJson (schemaObjStr, true);

        File f = new File(getTestDataDirectory(), "schema/employee_schema.json");
        Schema expectedSchema;
        try (FileInputStream fis = new FileInputStream(f)) {
            expectedSchema = Schema.fromJson (fis, false);
        }

        if (!expectedSchema.equals(schema)) {
            for (int i = 0; i < expectedSchema.getFields().size(); i++) {
                Field expectedField = expectedSchema.getFields().get(i);
                Field testField = schema.getFields().get(i);
                Assert.assertEquals(expectedField, testField);

            }
        }
        assertEquals(expectedSchema, schema);
    }

    @Test
    public void testIssue20() throws Exception {
        Schema expectedschema = Schema.fromJson (new File(getTestDataDirectory()
                , "schema/employee_schema.json"), true);
        Assert.assertNotNull(expectedschema);
    }

    @Test
    public void test2Issue20() throws Exception {
        URL url = new URL("https://raw.githubusercontent.com/frictionlessdata/tableschema-java/" +
                "master/src/test/resources/fixtures/data/simple_data.csv");
        Table table = Table.fromSource(url);

        Schema schema = table.inferSchema();
        String json = schema.getJson();
        Schema newSchema = Schema.fromJson(json, true);
        Assert.assertTrue(newSchema.isValid());
    }


    @Test
    public void testIssue14() throws Exception {
        Schema schema = Schema.fromJson (new File(getTestDataDirectory()
                , "schema/employee_full_schema.json"), true);
        Assert.assertNotNull(schema);

        File f = new File("data/employee_full.csv");
        Table table = Table.fromSource(f, getTestDataDirectory(), schema, null);
        List<Object[]> data = table.read();
        Assert.assertEquals(3, data.size());
    }

    // Create schema from a provided Bean class and compare with
    // human-defined schema. Allow for slight differences eg. in the
    // field format.
    @Test
    public void testSchemaFromBeanClass() throws Exception{
        Schema schema = BeanSchema.infer(EmployeeBean.class);
        File f = new File(getTestDataDirectory(), "schema/employee_schema.json");
        Schema expectedSchema;
        try (FileInputStream fis = new FileInputStream(f)) {
            expectedSchema = Schema.fromJson (fis, false);
        }
        Assert.assertTrue(expectedSchema.similar(schema));
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
