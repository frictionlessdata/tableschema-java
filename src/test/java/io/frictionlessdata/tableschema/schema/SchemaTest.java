package io.frictionlessdata.tableschema.schema;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.frictionlessdata.tableschema.Table;
import io.frictionlessdata.tableschema.TestHelper;
import io.frictionlessdata.tableschema.beans.EmployeeBean;
import io.frictionlessdata.tableschema.beans.ExplicitNamingBean;
import io.frictionlessdata.tableschema.exception.ForeignKeyException;
import io.frictionlessdata.tableschema.exception.PrimaryKeyException;
import io.frictionlessdata.tableschema.exception.ValidationException;
import io.frictionlessdata.tableschema.field.*;
import io.frictionlessdata.tableschema.fk.ForeignKey;
import io.frictionlessdata.tableschema.fk.Reference;
import io.frictionlessdata.tableschema.util.JsonUtil;
import io.frictionlessdata.tableschema.util.ReflectionUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static io.frictionlessdata.tableschema.TestHelper.getResourceFile;
import static io.frictionlessdata.tableschema.TestHelper.getTestDataDirectory;


public class SchemaTest {

    public File folder = Files.createTempDirectory("tableschema-").toFile();

    public SchemaTest() throws IOException {
    }

    @Test
    @DisplayName("Validate creating a Schema from valid JSON representation")
    public void testCreateSchemaFromValidSchemaJson() throws Exception {
        Field<?> nameField = new IntegerField("id");
        String schemaJson = "{\"fields\": [" + nameField.getJson() + "]}";
        Schema validSchema = Schema.fromJson(schemaJson, true);
        Assertions.assertTrue(validSchema.isValid());
    }

    @Test
    @DisplayName("Validate creating a Schema from an invalid file with strict validation throws")
    public void testReadFromInValidSchemaFileWithStrictValidation() throws Exception {
        File f = new File(TestHelper.getTestDataDirectory(), "schema/invalid_population_schema.json");
        Assertions.assertThrows(ValidationException.class, () -> Schema.fromJson(f, true));
    }

    @Test
    @DisplayName("Validate creating a Schema from an invalid file with lenient validation does not throw")
    public void testReadFromInValidSchemaFileWithLenientValidation() throws Exception {
        File f = new File(TestHelper.getTestDataDirectory(), "schema/invalid_population_schema.json");
        Schema schema = Schema.fromJson(f, false);

        Assertions.assertFalse(schema.getErrors().isEmpty());
    }

    @Test
    @DisplayName("Validate simple Schema with one IntegerField")
    public void testIsValid() {
        Schema schema = new Schema();

        Field<?> idField = new IntegerField("id");
        schema.addField(idField);

        Assertions.assertTrue(schema.isValid());
    }


    @Test
    @DisplayName("Validate creation of Schema from valid URL")
    public void testCreateSchemaFromValidSchemaUrl() throws Exception {
        URL url = new URL("https://raw.githubusercontent.com/frictionlessdata/tableschema-java/" +
                "master/src/test/resources/fixtures/schema/simple_schema.json");

        Schema validSchema = Schema.fromJson(url, true);
        Assertions.assertTrue(validSchema.isValid());
    }

    @Test
    @DisplayName("Validate creating a Schema from an invalid URL throws")
    public void testCreateSchemaFromBadUrl() throws Exception {
        URL url = new URL("https://raw.githubusercontent.com/frictionlessdata/" +
                "tableschema-java/BAD/URL/simple_schema.json");

        Assertions.assertThrows(Exception.class, () -> Schema.fromJson(url, true));
    }


    @Test
    @DisplayName("Validate field names of Schema read from JSON representation")
    public void testReadSchemaFieldNames() throws Exception {
        File source = getResourceFile("/fixtures/schema/population_schema.json");
        Schema schema = Schema.fromJson(source, true);

        List<String> fieldNames = schema.getFieldNames();
        Object[] namesArr = fieldNames.toArray();
        String[] testArr = new String[]{"city", "year", "population"};
        Assertions.assertArrayEquals(testArr, namesArr);
    }

    @Test
    @DisplayName("Validate creating a Schema having NumberFields with options for group and decimal separator")
    public void testCreateSchemaWithNumberOptions() throws Exception {
        File source = getResourceFile("/fixtures/schema/number_types_schema.json");
        Schema schema = Schema.fromJson(source, true);

        Assertions.assertEquals( ",", schema.getField("numberWithComma").getOptions().get("groupChar"));
        Assertions.assertEquals( ".", schema.getField("numberWithComma").getOptions().get("decimalChar"));
    }

    @Test
    @DisplayName("Validate creating a Schema from a List of Fields")
    public void testCreateFromListOfFields() throws Exception {
        List<Field<?>> fields = new ArrayList<>();

        Field<?> fieldString = new StringField("fieldString");
        fields.add(fieldString);
        Field<?> fieldInteger = new IntegerField("fieldInteger");
        fields.add(fieldInteger);
        Field<?> fieldBoolean = new BooleanField("fieldBoolean");
        fields.add(fieldBoolean);

        Schema schema = new Schema(fields, true);
        Assertions.assertEquals(3, schema.getFields().size());
        Assertions.assertEquals(fields, schema.getFields());
    }

    @Test
    public void testCreateSchemaFromFileWithValidPrimaryKey() throws Exception {
        File source = getResourceFile("/fixtures/primarykey/simple_schema_with_valid_pk.json");
        Schema schemaWithValidPK = Schema.fromJson(source, true);

        Assertions.assertEquals("id", schemaWithValidPK.getPrimaryKey());
    }

    @Test
    public void testCreateSchemaFromFileWithInvalidPrimaryKey() throws Exception {
        File source = getResourceFile("/fixtures/primarykey/simple_schema_with_invalid_pk.json");

        Throwable t = Assertions.assertThrows(PrimaryKeyException.class, () -> Schema.fromJson(source, true));
        Assertions.assertEquals("No such field: invalid.", t.getMessage());
    }

    @Test
    public void testCreateSchemaFromFileWithValidCompositeKey() throws Exception {
        File source = getResourceFile("/fixtures/primarykey/simple_schema_with_valid_ck.json");
        Schema schemaWithValidCK = Schema.fromJson(source, true);

        String[] compositePrimaryKey = schemaWithValidCK.getPrimaryKey();
        Assertions.assertEquals("name", compositePrimaryKey[0]);
        Assertions.assertEquals("surname", compositePrimaryKey[1]);

    }

    @Test
    public void testCreateSchemaFromFileWithInvalidCompositeKey() throws Exception {
        File source = getResourceFile("/fixtures/primarykey/simple_schema_with_invalid_ck.json");

        Throwable t = Assertions.assertThrows(PrimaryKeyException.class, () -> Schema.fromJson(source, true));
        Assertions.assertEquals("No such field: invalid.", t.getMessage());
    }

    @Test
    public void testAddValidField() {
        Field<?> nameField = new IntegerField("id");
        Schema validSchema = new Schema();
        validSchema.addField(nameField);

        Assertions.assertEquals(1, validSchema.getFields().size());
    }

    @Test
    public void testRetrieveNonexistingField() {
        Field<?> nameField = new IntegerField("id");
        Schema validSchema = new Schema();
        validSchema.addField(nameField);

        Assertions.assertNull(validSchema.getField("lksajdf"));
    }

    @Test
    public void testAddValidFieldAsJson() {
        Field<?> nameField = new IntegerField("id");
        Schema validSchema = new Schema();
        validSchema.addField(nameField.getJson());
        Field<?> foundNameField = validSchema.getField("id");

        Assertions.assertEquals(nameField, foundNameField);
    }


    @Test
    public void hasField() {
        Schema schema = new Schema();
        Assertions.assertTrue(schema.isEmpty());

        Field<?> idField = new IntegerField("id");
        schema.addField(idField);
        Assertions.assertFalse(schema.isEmpty());
    }

    @Test
    public void hasSetField() {
        Schema schema = new Schema();
        Assertions.assertTrue(schema.isEmpty());

        Field<?> idField = new IntegerField("id");
        schema.addField(idField);
        Assertions.assertTrue(schema.hasField("id"));
        Assertions.assertFalse(schema.hasField(null));
        Assertions.assertFalse(schema.hasField("sdfsd"));
    }

    @Test
    public void testSaveWithField() throws Exception {
        File createdFile = new File (folder,"test_schema.json");

        Schema createdSchema = new Schema();

        // Fields with Constraints
        Map<String, Object> intFieldConstraints = new HashMap<>();
        intFieldConstraints.put(Field.CONSTRAINT_KEY_REQUIRED, true);

        Field<?> intField = new IntegerField(
                "id",
                Field.FIELD_FORMAT_DEFAULT,
                null,
                null,
                null,
                intFieldConstraints,
                null);
        createdSchema.addField(intField);

        Map<String, Object> stringFieldConstraints = new HashMap<>();
        stringFieldConstraints.put(Field.CONSTRAINT_KEY_MIN_LENGTH, 36);
        stringFieldConstraints.put(Field.CONSTRAINT_KEY_MAX_LENGTH, 45);

        Field<?> stringField = new StringField(
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

        Schema readSchema = Schema.fromJson(createdFile, true);

        // Assert id field
        Assertions.assertEquals(Field.FIELD_TYPE_INTEGER, readSchema.getField("id").getType());
        Assertions.assertEquals(Field.FIELD_FORMAT_DEFAULT, readSchema.getField("id").getFormat());
        Assertions.assertNull(readSchema.getField("id").getTitle());
        Assertions.assertNull(readSchema.getField("id").getDescription());
        Assertions.assertTrue((boolean) readSchema.getField("id").getConstraints().get(Field.CONSTRAINT_KEY_REQUIRED));

        // Assert name field
        Assertions.assertEquals(Field.FIELD_TYPE_STRING, readSchema.getField("name").getType());
        Assertions.assertEquals(Field.FIELD_FORMAT_DEFAULT, readSchema.getField("name").getFormat());
        Assertions.assertEquals("the title", readSchema.getField("name").getTitle());
        Assertions.assertEquals("the description", readSchema.getField("name").getDescription());
        Assertions.assertEquals(36, readSchema.getField("name").getConstraints().get(Field.CONSTRAINT_KEY_MIN_LENGTH));
        Assertions.assertEquals(45, readSchema.getField("name").getConstraints().get(Field.CONSTRAINT_KEY_MAX_LENGTH));
    }

    @Test
    public void testSaveWithPrimaryKey() throws Exception {
        File createdFile = new File (folder,"test_schema.json");

        Schema createdSchema = new Schema(true);

        Field<?> intField = new IntegerField("id", Field.FIELD_FORMAT_DEFAULT, null, null, null, null, null);
        createdSchema.addField(intField);

        Field<?> stringField = new StringField("name", Field.FIELD_FORMAT_DEFAULT, null, null, null, null, null);
        createdSchema.addField(stringField);

        // Primary Key
        createdSchema.setPrimaryKey("id");

        // Save schema
        createdSchema.writeJson(createdFile);

        Schema readSchema = Schema.fromJson(createdFile, true);

        // Assert Primary Key
        Assertions.assertEquals("id", readSchema.getPrimaryKey());
    }

    @Test
    public void testSaveWithForeignKey() throws Exception {
        File createdFile = new File (folder,"test_schema.json");

        Schema createdSchema = new Schema();

        Field<?> intField = new IntegerField("id", Field.FIELD_FORMAT_DEFAULT, null, null, null, null, null);
        createdSchema.addField(intField);

        Field<?> stringField = new StringField("name", Field.FIELD_FORMAT_DEFAULT, null, null, null, null, null);
        createdSchema.addField(stringField);

        // Foreign Keys
        Reference ref = new Reference("resource", "fields", true);
        ForeignKey fk = new ForeignKey("name", ref, true);
        createdSchema.addForeignKey(fk);

        // Save schema
        createdSchema.writeJson(createdFile);

        Schema readSchema = Schema.fromJson(createdFile, true);

        // Assert Foreign Keys
        Assertions.assertEquals("name", readSchema.getForeignKeys().get(0).getFields());
        Assertions.assertEquals("fields", readSchema.getForeignKeys().get(0).getReference().getFields());
        Assertions.assertEquals("resource", readSchema.getForeignKeys().get(0).getReference().getResource());
    }

    @Test
    public void testSinglePrimaryKey() throws PrimaryKeyException {
        Schema schema = new Schema(true);

        Field<?> idField = new IntegerField("id");
        schema.addField(idField);

        schema.setPrimaryKey("id");
        String key = schema.getPrimaryKey();

        Assertions.assertEquals("id", key);
    }

    @Test
    public void testInvalidSinglePrimaryKey() throws PrimaryKeyException {
        Schema schema = new Schema(true);

        Field<?> idField = new IntegerField("id");
        schema.addField(idField);

        Throwable t = Assertions.assertThrows(PrimaryKeyException.class, () -> schema.setPrimaryKey("invalid"));
        Assertions.assertEquals("No such field: invalid.", t.getMessage());
    }

    @Test
    public void testCompositePrimaryKey() throws PrimaryKeyException {
        Schema schema = new Schema(true);

        Field<?> idField = new IntegerField("id");
        schema.addField(idField);

        Field<?> nameField = new StringField("name");
        schema.addField(nameField);

        Field<?> surnameField = new StringField("surname");
        schema.addField(surnameField);

        schema.setPrimaryKey(new String[]{"name", "surname"});
        String[] compositeKey = schema.getPrimaryKey();

        Assertions.assertEquals("name", compositeKey[0]);
        Assertions.assertEquals("surname", compositeKey[1]);
    }

    @Test
    public void testInvalidCompositePrimaryKey() throws PrimaryKeyException {
        Schema schema = new Schema(true);

        Field<?> idField = new IntegerField("id");
        schema.addField(idField);

        Field<?> nameField = new StringField("name");
        schema.addField(nameField);

        Field<?> surnameField = new StringField("surname");
        schema.addField(surnameField);

        Throwable t = Assertions.assertThrows(PrimaryKeyException.class,
                () -> schema.setPrimaryKey(new String[]{"name", "invalid"}));
        Assertions.assertEquals("No such field: invalid.", t.getMessage());
    }

    @Test
    public void testInvalidCompositePrimaryKeyWithoutStrictValidation() throws PrimaryKeyException {
        Schema schema = new Schema(false);

        Field<?> idField = new IntegerField("id");
        schema.addField(idField);

        Field<?> nameField = new StringField("name");
        schema.addField(nameField);

        Field<?> surnameField = new StringField("surname");
        schema.addField(surnameField);

        String[] compositeKey = new String[]{"name", "invalid"};
        schema.setPrimaryKey(compositeKey); // strict=false

        List<String> fetchedCompositeKey = schema.getPrimaryKeyParts();
        Assertions.assertEquals("name", fetchedCompositeKey.get(0));
        Assertions.assertEquals("invalid", fetchedCompositeKey.get(1));
    }

    @Test
    public void testInvalidForeignKeyArray() throws Exception {
        File source = getResourceFile("/fixtures/foreignkeys/schema_invalid_fk_array.json");

        ForeignKeyException fke = Assertions.assertThrows(ForeignKeyException.class, () -> Schema.fromJson(source, true));
        Assertions.assertEquals("The reference's fields property must be an array if the outer fields is an array.", fke.getMessage());
    }

    @Test
    public void testInvalidForeignKeyArrayString() throws Exception {
        File source = getResourceFile("/fixtures/foreignkeys/schema_invalid_fk_array_string.json");

        ForeignKeyException fke = Assertions.assertThrows(ForeignKeyException.class, () -> Schema.fromJson(source, true));
        Assertions.assertEquals("The reference's fields property must be a string if " +
                "the outer fields is a string.", fke.getMessage());
    }

    @Test
    public void testInvalidForeignKeyArrayStringRef() throws Exception {
        File source = getResourceFile("/fixtures/foreignkeys/schema_invalid_fk_array_string_ref.json");

        ForeignKeyException fke = Assertions.assertThrows(ForeignKeyException.class, () -> Schema.fromJson(source, true));
        Assertions.assertNotNull(fke);
        Assertions.assertEquals("The reference's fields property must be an array if the outer fields " +
                "is an array.", fke.getMessage());
    }

    @Test
    public void testInvalidForeignKeyArrayWrongNumber() throws Exception {
        File source = getResourceFile("/fixtures/foreignkeys/schema_invalid_fk_array_wrong_number.json");

        ForeignKeyException fke =Assertions.assertThrows(ForeignKeyException.class, () -> Schema.fromJson(source, true));
        Assertions.assertNotNull(fke);
        Assertions.assertEquals("The reference's fields property must be an array of the same length as that" +
                        " of the outer fields' array.", fke.getMessage());
    }

    @Test
    public void testInvalidForeignKeyNoReference() throws Exception {
        File source = getResourceFile("/fixtures/foreignkeys/schema_invalid_fk_no_reference.json");

        ForeignKeyException fke = Assertions.assertThrows(ForeignKeyException.class, () -> Schema.fromJson(source, true));
        Assertions.assertEquals("A foreign key must have the fields and reference properties.", fke.getMessage());
    }

    @Test
    public void testInvalidForeignKeyString() throws Exception {
        File source = getResourceFile("/fixtures/foreignkeys/schema_invalid_fk_string.json");

        Throwable t = Assertions.assertThrows(ValidationException.class, () -> Schema.fromJson(source, true));
        List<Object> messages = ((ValidationException) t).getMessages();
        Assertions.assertEquals(1,messages.size());
        Assertions.assertEquals("Primary key field doesnotexist not found", messages.get(0));
    }

    @Test
    public void testInvalidForeignKeyStringArrayRef() throws Exception {
        File source = getResourceFile("/fixtures/foreignkeys/schema_invalid_fk_string_array_ref.json");

        ForeignKeyException fke = Assertions.assertThrows(ForeignKeyException.class, () -> Schema.fromJson(source, true));
        Assertions.assertEquals("The reference's fields property must be a string if " +
                "the outer fields is a string.", fke.getMessage());
    }

    @Test
    public void testValidForeignKeyArray() throws Exception {
        File source = getResourceFile("/fixtures/foreignkeys/schema_valid_fk_array.json");
        Schema schema = Schema.fromJson(source, true);

        List<String> parsedFields = schema.getForeignKeys().get(0).getFieldNames();
        Assertions.assertEquals("id", parsedFields.get(0));
        Assertions.assertEquals("title", parsedFields.get(1));

        List<String> refFields = schema.getForeignKeys().get(0).getReference().getFieldNames();
        Assertions.assertEquals("fk_id", refFields.get(0));
        Assertions.assertEquals("title_id", refFields.get(1));
    }

    @Test
    public void testValidForeignKeyString() throws Exception {
        File source = getResourceFile("/fixtures/foreignkeys/schema_valid_fk_string.json");
        Schema schema = Schema.fromJson(source, true);

        Assertions.assertEquals("position_title", schema.getForeignKeys().get(0).getFields());
        Assertions.assertEquals("positions", schema.getForeignKeys().get(0).getReference().getResource());
        Assertions.assertEquals("name", schema.getForeignKeys().get(0).getReference().getFields().toString());
    }

    @Test
    public void testInferTypesComplexSchema() throws Exception {
        Table table = Table.fromSource(new File("data/employee_data.csv"), getTestDataDirectory());

        String schemaObjStr = table.inferSchema().asJson();
        Schema schema = Schema.fromJson(schemaObjStr, true);

        File f = new File(getTestDataDirectory(), "schema/employee_schema.json");
        Schema expectedSchema;
        try (FileInputStream fis = new FileInputStream(f)) {
            expectedSchema = Schema.fromJson(fis, false);
        }

        if (!expectedSchema.equals(schema)) {
            for (int i = 0; i < expectedSchema.getFields().size(); i++) {
                Field<?> expectedField = expectedSchema.getFields().get(i);
                Field<?> testField = schema.getFields().get(i);
                Assertions.assertEquals(expectedField, testField);

            }
        }
        Assertions.assertEquals(expectedSchema, schema);
    }


    @Test
    public void testSaveDefaultBooleanValues() throws Exception {
        File createdFile = new File(folder, "test_schema.json");

        File f = new File(getTestDataDirectory(), "schema/employee_schema.json");
        Schema createdSchema = Schema.fromJson(f, true);
        createdSchema.writeJson(createdFile);

        Schema readSchema = Schema.fromJson(createdFile, true);
        Assertions.assertEquals(createdSchema, readSchema);

        BooleanField adminField = ((BooleanField)readSchema.getField("isAdmin"));
        Assertions.assertEquals("true", adminField.formatValueAsString(true));
        Assertions.assertEquals("false", adminField.formatValueAsString(false));
        Assertions.assertEquals("true", adminField.formatValueAsString(true));
        Assertions.assertEquals("false", adminField.formatValueAsString(false));
    }


    @Test
    public void testSaveAlternateBooleanValues() throws Exception {
        File createdFile = new File(folder, "test_schema.json");

        File f = new File(getTestDataDirectory(), "schema/employee_schema_boolean_alternative_values.json");
        Schema createdSchema = Schema.fromJson(f, true);
        createdSchema.writeJson(createdFile);

        Schema readSchema = Schema.fromJson(createdFile, true);
        Assertions.assertEquals(createdSchema, readSchema);

        BooleanField adminField = ((BooleanField)readSchema.getField("isAdmin"));
        Assertions.assertEquals("TRUE", adminField.formatValueAsString(true));
        Assertions.assertEquals("FALSE", adminField.formatValueAsString(false));
        Assertions.assertEquals("TRUE", adminField.formatValueAsString(true));
        Assertions.assertEquals("FALSE", adminField.formatValueAsString(false));
    }


    @Test
    public void testIssue20() throws Exception {
        Schema expectedschema = Schema.fromJson(new File(getTestDataDirectory()
                , "schema/employee_schema.json"), true);
        Assertions.assertNotNull(expectedschema);
    }

    @Test
    public void test2Issue20() throws Exception {
        URL url = new URL("https://raw.githubusercontent.com/frictionlessdata/tableschema-java/" +
                "master/src/test/resources/fixtures/data/simple_data.csv");
        Table table = Table.fromSource(url);

        Schema schema = table.inferSchema();
        String json = schema.asJson();
        Schema newSchema = Schema.fromJson(json, true);
        Assertions.assertTrue(newSchema.isValid());
    }

    // Test for https://github.com/frictionlessdata/tableschema-java/issues/14
    // "schema_valid_full.json" from the Python version is named "employee_full_schema.json" here
    @Test
    public void testIssue14() throws Exception {
        Schema schema = Schema.fromJson(new File(getTestDataDirectory()
                , "schema/employee_full_schema.json"), true);
        Assertions.assertNotNull(schema);

        File f = new File("data/employee_full.csv");
        Table table = Table.fromSource(f, getTestDataDirectory(), schema, null);
        List<Object[]> data = table.read();
        Assertions.assertEquals(3, data.size());
    }

    // Create schema from a provided Bean class and compare with
    // human-defined schema. Allow for slight differences eg. in the
    // field format.
    @Test
    public void testSchemaFromBeanClass() throws Exception {
        Schema schema = BeanSchema.infer(EmployeeBean.class);
        File f = new File(getTestDataDirectory(), "schema/employee_schema.json");
        Schema expectedSchema;
        try (FileInputStream fis = new FileInputStream(f)) {
            expectedSchema = Schema.fromJson(fis, false);
        }
        Assertions.assertTrue(expectedSchema.similar(schema));
    }


    @Test
    @DisplayName("Validate creating Field mapping from Bean")
    void testCreateFieldMapping() throws Exception{
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> fieldNameMapping = new TreeMap<>(ReflectionUtil.getFieldNameMapping(ExplicitNamingBean.class));
        String expectedString = TestHelper.getResourceFileContent(
                "/fixtures/beans/explicitnamingbean.json");
        Map<String, String> expectedFieldMap
                = new TreeMap<>(objectMapper.readValue(expectedString, new TypeReference<Map<String, String>>() {}));
        Assertions.assertEquals(expectedFieldMap, fieldNameMapping);
    }

    @Test
    @DisplayName("Validate the networknt lib can validate our schema")
    void testSchemaValidator() throws Exception {
        String schemaStr = TestHelper.getResourceFileContent(
                "/schemas/table-schema.json");
        FormalSchemaValidator.fromJson(schemaStr);
    }

    @Test
    @DisplayName("Roundtrip: Validate creating a Schema from JSON with all properties, serialize to JSON and compare")
    public void testCreateFullSchemaFromSchemaJson() throws Exception {
        String expectedString = TestHelper.getResourceFileContent(
                "/fixtures/schema/full_schema.json");
        Schema schema = Schema.fromJson(expectedString, true);
        String serialized = JsonUtil.getInstance().serialize(schema);
        Assertions.assertEquals(expectedString.replaceAll("\r\n","\n"), serialized.replaceAll("\r\n","\n"));
    }
}
