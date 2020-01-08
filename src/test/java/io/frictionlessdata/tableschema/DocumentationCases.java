package io.frictionlessdata.tableschema;

import io.frictionlessdata.tableschema.datasourceformats.DataSourceFormat;
import io.frictionlessdata.tableschema.field.*;
import io.frictionlessdata.tableschema.iterator.TableIterator;
import io.frictionlessdata.tableschema.schema.Schema;
import org.apache.commons.csv.CSVFormat;
import org.joda.time.DateTime;
import org.json.JSONObject;
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
        // Let's start by defining and building the schema of a table that contains data on employees:
        Schema schema = new Schema();

        schema.addField(new IntegerField("id"));
        schema.addField(new StringField("title"));
        // Load the data from URL with the schema.
        Table table = Table.fromJson(
                new URL("https://raw.githubusercontent.com/frictionlessdata/tableschema-java/master" +
                        "/src/test/resources/fixtures/data/simple_data.csv"),
                schema, DataSourceFormat.getDefaultCsvFormat());

        List<Object[]> allData = table.read();

        // [1, foo]
        // [2, bar]
        // [3, baz]
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
        Table table = Table.fromJson(url);

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

    }

    @Test
    @DisplayName("Build a Schema from JSON")
    void buildASchema2() throws Exception{
        Schema schema = new Schema(); // By default strict=false validation

        JSONObject nameFieldJsonObject = new JSONObject();
        nameFieldJsonObject.put("name", "name");
        nameFieldJsonObject.put("type", Field.FIELD_TYPE_STRING);
        schema.addField(nameFieldJsonObject.toString());

        // Because strict=false, an invalid Field definition will be included.
        // The error will be logged/tracked in the error list schema.getErrors().
        JSONObject invalidFieldJsonObject = new JSONObject();
        invalidFieldJsonObject.put("name", "id");
        invalidFieldJsonObject.put("type", Field.FIELD_TYPE_INTEGER);
        invalidFieldJsonObject.put("format", "invalid");
        schema.addField(invalidFieldJsonObject.toString());

        JSONObject coordinatesFieldJsonObject = new JSONObject();
        coordinatesFieldJsonObject.put("name", "coordinates");
        coordinatesFieldJsonObject.put("type", Field.FIELD_TYPE_GEOPOINT);
        coordinatesFieldJsonObject.put("format", Field.FIELD_FORMAT_ARRAY);
        schema.addField(coordinatesFieldJsonObject.toString());

        System.out.println(schema.getJson());
        /*
        {"fields":[
            {"name":"name","format":"default","type":"string"},
            {"name":"id","format":"invalid","type":"integer"},
            {"name":"coordinates","format":"array","type":"geopoint"}
        ]}
        */
    }

    @Test
    @DisplayName("Infer a Schema")
    void inferASchema() throws Exception{
        URL url = new URL("https://raw.githubusercontent.com/frictionlessdata/tableschema-java/master" +
                                "/src/test/resources/fixtures/data/simple_data.csv");
        Table table = Table.fromJson(url);

        Schema schema = table.inferSchema();
        System.out.println(schema.getJson());

        /*
         {"fields":[
            {"name":"id","format":"","description":"","title":"","type":"integer","constraints":{}},
            {"name":"title","format":"","description":"","title":"","type":"string","constraints":{}}
        ]}
         */
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
        Table table = Table.fromJson(url, schema, DataSourceFormat.getDefaultCsvFormat());

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
    }
}

