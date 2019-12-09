package io.frictionlessdata.tableschema;

import io.frictionlessdata.tableschema.field.Field;
import io.frictionlessdata.tableschema.field.GeopointField;
import io.frictionlessdata.tableschema.field.IntegerField;
import io.frictionlessdata.tableschema.field.StringField;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

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
        Table table = new Table(
                new URL("https://raw.githubusercontent.com/frictionlessdata/tableschema-java/master/src/test/resources/fixtures/data/simple_data.csv"),
                schema);

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
        Table table = new Table(url);

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
        Table table = new Table(url);

        Schema schema = table.inferSchema();
        System.out.println(schema.getJson());

        /*
         {"fields":[
            {"name":"id","format":"","description":"","title":"","type":"integer","constraints":{}},
            {"name":"title","format":"","description":"","title":"","type":"string","constraints":{}}
        ]}
         */
    }
}

