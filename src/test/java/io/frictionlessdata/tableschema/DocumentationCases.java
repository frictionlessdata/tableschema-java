package io.frictionlessdata.tableschema;

import io.frictionlessdata.tableschema.datasourceformat.DataSourceFormat;
import io.frictionlessdata.tableschema.field.*;
import io.frictionlessdata.tableschema.schema.Schema;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigInteger;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases that mirror the code examples in the documentation to ensure
 * the examples are still valid
 */
class DocumentationCases {

    /**
     * Example
     * https://github.com/frictionlessdata/tableschema-java#parsing-a-csv-using-a-schema
     */

    @Test
    @DisplayName("Parsing a CSV using a Schema")
    void csvParsingWithSchema() throws Exception{
        // Let's start by defining and building the schema of a table that contains data about employees:
        Schema schema = new Schema();

        schema.addField(new IntegerField("id"));
        schema.addField(new StringField("title"));
        // Load the data from URL with the schema.
        Table table = Table.fromSource(
            new URL("https://raw.githubusercontent.com/frictionlessdata/tableschema-java/master" +
                "/src/test/resources/fixtures/data/simple_data.csv"),
            schema, DataSourceFormat.getDefaultCsvFormat());

        List<Object[]> allData = table.read();

        // [1, foo]
        // [2, bar]
        // [3, baz]

        assertEquals(allData.get(0)[0], BigInteger.valueOf(1));
        assertEquals(allData.get(0)[1], "foo");
        assertEquals(allData.get(1)[0], BigInteger.valueOf(2));
        assertEquals(allData.get(1)[1], "bar");
        assertEquals(allData.get(2)[0], BigInteger.valueOf(3));
        assertEquals(allData.get(2)[1], "baz");
    }

    /**
     * Example
     * https://github.com/frictionlessdata/tableschema-java#parse-a-csv-without-a-schema
     */

    @Test
    @DisplayName("Parse a CSV without a Schema")
    void csvParsingWithoutSchema() throws Exception{
        URL url = new URL("https://raw.githubusercontent.com/frictionlessdata/tableschema-java/master" +
            "/src/test/resources/fixtures/data/simple_data.csv");
        Table table = Table.fromSource(url);

        // Iterate through rows
        Iterator<Object[]> iter = table.iterator();
        while(iter.hasNext()){
            Object[] row = iter.next();
            System.out.println(Arrays.toString(row));
        }

        // [1, foo]
        // [2, bar]
        // [3, baz]

        // Read the entire CSV and output it as a List:
        List<Object[]> allData = table.read();

        assertEquals(allData.get(0)[0], "1");
        assertEquals(allData.get(0)[1], "foo");
        assertEquals(allData.get(1)[0], "2");
        assertEquals(allData.get(1)[1], "bar");
        assertEquals(allData.get(2)[0], "3");
        assertEquals(allData.get(2)[1], "baz");
    }

    /**
     * Example
     * https://github.com/frictionlessdata/tableschema-java#build-a-schema
     */

    @Test
    @DisplayName("Build a Schema")
    void buildASchema() throws Exception{
        Schema schema = new Schema();

        Field nameField = new StringField("name");
        schema.addField(nameField);

        Field coordinatesField = new GeopointField("coordinates");
        schema.addField(coordinatesField);

        System.out.println(schema.getJson());

        /*
        {"fields":[
            {"name":"name","format":"default","description":"","type":"string","title":""},
            {"name":"coordinates","format":"default","description":"","type":"geopoint","title":""}
        ]}
         */

        ObjectMapper objectMapper = new ObjectMapper();
        String json = schema.getJson().replaceAll("[\\n\\t\\r ]", "");
        Object jsonObject = objectMapper.readValue(json, Object.class);
        String expectedString = objectMapper.writeValueAsString(jsonObject);
        assertEquals(json, expectedString);
    }

    @Test
    @DisplayName("Build a Schema from JSON")
    void buildASchema2() throws Exception{
        // By default strict=true validation
        Schema schema = new Schema(false);

        String nameFieldJson = "{\"name\":\"name\",\"type\":\""
            + Field.FIELD_TYPE_STRING + "\"}";
        schema.addField(nameFieldJson);

        // Because strict=false, an invalid Field definition will be included.
        // The error will be logged/tracked in the error list schema.getErrors().
        String invalidFieldJson = "{\"name\":\"id\",\"type\":\""
            + Field.FIELD_TYPE_INTEGER + "\",\"format\": \"invalid\"}";
        schema.addField(invalidFieldJson);

        String coordinatesFieldJson = "{\"name\":\"coordinates\",\"type\":\""
            + Field.FIELD_TYPE_GEOPOINT + "\",\"format\":\""
            + Field.FIELD_FORMAT_ARRAY + "\"}";
        schema.addField(coordinatesFieldJson);

        System.out.println(schema.getJson());
        /*
        {"fields":[
            {"name":"name","format":"default","type":"string"},
            {"name":"id","format":"invalid","type":"integer"},
            {"name":"coordinates","format":"array","type":"geopoint"}
        ]}
        */

        ObjectMapper objectMapper = new ObjectMapper();
        String json = schema.getJson().replaceAll("[\\n\\t\\r ]", "");
        Object jsonObject = objectMapper.readValue(json, Object.class);
        String expectedString = objectMapper.writeValueAsString(jsonObject);
        assertEquals(json, expectedString);
    }

    @Test
    @DisplayName("Infer a Schema")
    void inferASchema() throws Exception{
        URL url = new URL("https://raw.githubusercontent.com/frictionlessdata/tableschema-java/master" +
            "/src/test/resources/fixtures/data/simple_data.csv");
        Table table = Table.fromSource(url);

        Schema schema = table.inferSchema();
        System.out.println(schema.getJson());

        /*
         {"fields":[
            {"name":"id","format":"","description":"","title":"","type":"integer","constraints":{}},
            {"name":"title","format":"","description":"","title":"","type":"string","constraints":{}}
        ]}
         */

        ObjectMapper objectMapper = new ObjectMapper();
        String json = schema.getJson().replaceAll("[\\n\\t\\r ]", "");
        Object jsonObject = objectMapper.readValue(json, Object.class);
        String expectedString = objectMapper.writeValueAsString(jsonObject);
        assertEquals(json, expectedString);
    }

    @Test
    @DisplayName("Write a Schema into a File")
    void writeASchemaToFile() throws Exception{
        Schema schema = new Schema();

        Field nameField = new StringField("name");
        schema.addField(nameField);

        Field coordinatesField = new GeopointField("coordinates");
        schema.addField(coordinatesField);

        schema.writeJson(new File("schema.json"));
        // TODO: Add assert statement here
    }

    @Test
    @DisplayName("Parse a CSV with a Schema")
    void parseCsvWithSchema() throws Exception{
        // Let's start by defining and building the schema of a table that contains data on employees:
        Schema schema = new Schema();

        Field idField = new IntegerField("id");
        schema.addField(idField);

        Field nameField = new StringField("name");
        schema.addField(nameField);

        Field dobField = new DateField("dateOfBirth");
        schema.addField(dobField);

        Field isAdminField = new BooleanField("isAdmin");
        schema.addField(isAdminField);

        Field addressCoordinatesField = new GeopointField("addressCoordinates");
        addressCoordinatesField.setFormat(Field.FIELD_FORMAT_OBJECT);
        schema.addField(addressCoordinatesField);

        Field contractLengthField = new DurationField("contractLength");
        schema.addField(contractLengthField);

        Field infoField = new ObjectField("info");
        schema.addField(infoField);

        // Load the data from URL with the schema.
        URL url = new URL("https://raw.githubusercontent.com/frictionlessdata/tableschema-java/master" +
            "/src/test/resources/fixtures/data/employee_data.csv");
        Table table = Table.fromSource(url, schema, DataSourceFormat.getDefaultCsvFormat());

        Iterator<Object[]> iter = table.iterator(false, false, true, false);
        while(iter.hasNext()){

            // The fetched array will contain row values that have been cast into their
            // appropriate types as per field definitions in the schema.
            Object[] row = iter.next();

            BigInteger id = (BigInteger)row[0];
            String name = (String)row[1];
            LocalDate dob = (LocalDate)row[2];
            boolean isAdmin = (boolean)row[3];
            double[] addressCoordinates = (double[])row[4];
            Duration contractLength = (Duration)row[5];
            Map info = (Map)row[6];
        }

        // TODO: Add assert statement here
    }

    @Test
    @DisplayName("Validate a Schema")
    void validateSchema() throws Exception {
        String schemaJson = "{\"fields\":[{\"name\":\"id\",\"type\":\""
            + Field.FIELD_TYPE_INTEGER + "\"}]}";
        Schema schema = Schema.fromJson(schemaJson, true);

        System.out.println(schema.isValid());
        // true

        assertTrue(schema.isValid());
    }

    @Test
    @DisplayName("Setting a Primary Key - Single Key")
    void settingPrimaryKey() throws Exception{
        Schema schema = new Schema();

        Field idField = new IntegerField("id");
        schema.addField(idField);

        Field nameField = new StringField("name");
        schema.addField(nameField);

        schema.setPrimaryKey("id");
        String primaryKey = schema.getPrimaryKey();

        assertEquals("id", primaryKey);
    }

    @Test
    @DisplayName("Setting a Primary Key - Composite Key")
    void settingPrimaryKey2() throws Exception{
        Schema schema = new Schema();

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
}
