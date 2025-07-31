package io.frictionlessdata.tableschema.table_tests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.frictionlessdata.tableschema.Table;
import io.frictionlessdata.tableschema.TestHelper;
import io.frictionlessdata.tableschema.exception.TableSchemaException;
import io.frictionlessdata.tableschema.exception.TableValidationException;
import io.frictionlessdata.tableschema.field.*;
import io.frictionlessdata.tableschema.schema.Schema;
import io.frictionlessdata.tableschema.tabledatasource.TableDataSource;
import io.frictionlessdata.tableschema.util.JsonUtil;
import org.apache.commons.csv.CSVFormat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

import static io.frictionlessdata.tableschema.TestHelper.getTestDataDirectory;


public class TableOtherTest {


    private static final String populationTestJson =  "[" +
        "{" +
        "\"city\": \"london\"," +
        "\"year\": 2017," +
        "\"population\": 8780000" +
        "}," +
        "{" +
        "\"city\": \"paris\"," +
        "\"year\": 2017," +
        "\"population\": 2240000" +
        "}," +
        "{" +
        "\"city\": \"rome\"," +
        "\"year\": 2017," +
        "\"population\": 2860000" +
        "}" +
        "]";

    private static final String populationSchema = "{\n" +
        "  \"fields\": [\n" +
        "    {\n" +
        "      \"name\": \"city\",\n" +
        "      \"format\": \"default\",\n" +
        "      \"description\": \"The city.\",\n" +
        "      \"type\": \"string\",\n" +
        "      \"title\": \"city\"\n" +
        "    },\n" +
        "    {\n" +
        "      \"name\": \"year\",\n" +
        "      \"description\": \"The year.\",\n" +
        "      \"type\": \"year\",\n" +
        "      \"title\": \"year\"\n" +
        "    },\n" +
        "    {\n" +
        "      \"name\": \"population\",\n" +
        "      \"description\": \"The population.\",\n" +
        "      \"type\": \"integer\",\n" +
        "      \"title\": \"population\"\n" +
        "    }\n" +
        "  ]\n" +
        "}\n";

    private static Object[][] populationTestData = new Object[][]
        {
            new Object[]{"london",2017,8780000},
            new Object[]{"paris",2017,2240000},
            new Object[]{"rome",2017,2860000}
        };


    @Test
    @DisplayName("Read JSON object array data from string with Schema and compare to Table read from CSV")
    public void testReadFromValidJSONArrayWithSchema() throws Exception{
        File schemaFile = new File(getTestDataDirectory(), "schema/population_schema.json");
        Schema schema = Schema.fromJson (schemaFile, true);
        Table table = Table.fromSource(populationTestJson, schema, TableDataSource.getDefaultCsvFormat());

        Assertions.assertEquals(3, table.read().size());
        Schema expectedSchema = Schema.fromJson(populationSchema, true);
        Table expectedTable = Table.fromSource(new File("data/population.csv")
            , getTestDataDirectory(), expectedSchema, TableDataSource.getDefaultCsvFormat());
        Assertions.assertEquals(expectedTable, table);
    }

    @Test
    @DisplayName("Read JSON object array data from string with Schema, return data as CSV and compare to CSV")
    public void testReadFromValidJSONArrayWithSchemaReturnCSV() throws Exception{
        File schemaFile = new File(getTestDataDirectory(), "schema/population_schema.json");
        Schema schema = Schema.fromJson (schemaFile, true);
        Table table = Table.fromSource(populationTestJson, schema, TableDataSource.getDefaultCsvFormat());

        String csv = table.asCsv();
        String refString = TestHelper.getResourceFileContent("/fixtures/data/population.csv");
        Assertions.assertEquals(
                refString.replaceAll("[\r\n]+", "\n"),
                csv.replaceAll("[\r\n]+", "\n"));
    }

    @Test
    @DisplayName("Read CSV data from string with Schema, return data as JSON object array and compare to file")
    public void testReadFromCSVWithSchemaReturnValidJSONArray() throws Exception{
        File schemaFile = new File(getTestDataDirectory(), "schema/population_schema.json");
        Schema schema = Schema.fromJson (schemaFile, true);
        File inFile = new File("data/population.csv");
        File baseDir = getTestDataDirectory();
        Table table = Table.fromSource(inFile, baseDir, schema, TableDataSource.getDefaultCsvFormat());

        String json = table.asJson();
        JsonNode testNode = JsonUtil.getInstance().readValue(json);
        String refString = TestHelper.getResourceFileContent("/fixtures/data/population.json");
        JsonNode refNode = JsonUtil.getInstance().readValue(refString);
        Assertions.assertEquals(refNode, testNode);
    }


    @Test
    public void testCsvDataSourceFormatToJson() throws Exception{
        File schemaFile = new File(getTestDataDirectory(), "schema/employee_schema.json");
        Schema schema = Schema.fromJson (schemaFile, true);
        File inFile = new File("data/employee_data.csv");

        Table table = Table.fromSource(inFile, getTestDataDirectory(), schema, null);
        String s = table.asJson();

        File referenceFile = new File(getTestDataDirectory(), "data/employee_data.json");
        String referenceContent = String.join("", Files.readAllLines(referenceFile.toPath()));

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode reference = objectMapper.readTree(referenceContent);
        JsonNode actual = objectMapper.readTree(s);
        Assertions.assertEquals(reference.textValue(), actual.textValue());
    }

    @Test
    public void testJsonDataSourceFormatToJson() throws Exception{
        File schemaFile = new File(getTestDataDirectory(), "schema/employee_schema.json");
        Schema schema = Schema.fromJson (schemaFile, true);
        File inFile = new File("data/employee_data.json");

        Table table = Table.fromSource(inFile, getTestDataDirectory(), schema, null);
        String s = table.asJson();

        File referenceFile = new File(getTestDataDirectory(), "data/employee_data.json");
        String referenceContent = String.join("", Files.readAllLines(referenceFile.toPath()));
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode reference = objectMapper.readTree(referenceContent);
        JsonNode actual = objectMapper.readTree(s);
        Assertions.assertEquals(reference.textValue(), actual.textValue());
    }


    /*
    The schema contains an additional column not present in the data. Since JSON objects
    will simply drop entries with null values, this can happen, but the Schema should still
    be considered valid.
     */
    @Test
    public void testReadFromValidJSONArrayWithExtendedSchema() throws Exception{
        File schemaFile = new File(getTestDataDirectory(), "schema/population_schema_additional_field.json");
        Schema schema = Schema.fromJson (schemaFile, true);
        Table table = Table.fromSource(new File("data/population.json")
            , getTestDataDirectory(), schema, TableDataSource.getDefaultCsvFormat());
        List<Object[]> data = table.read();
        Assertions.assertEquals(3, data.size());
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode populationTest = objectMapper.readTree(populationTestJson);

        for (int i = 0; i < data.size(); i++) {
            Object[] row = data.get(i);
            JsonNode expectedObj = populationTest.get(i);
            Assertions.assertEquals(4, row.length);
            for (int j = 0; j < row.length; j++) {
                if (j == 0) {
                    Assertions.assertEquals(expectedObj.get("city").asText(), row[j]);
                } else if (j == 1) {
                    Assertions.assertNull(row[j]);
                } else if (j == 2) {
                    Assertions.assertEquals(expectedObj.get("year").toString(), row[j].toString());
                } else if (j == 3) {
                    Assertions.assertEquals(expectedObj.get("population").bigIntegerValue(), row[j]);
                }
            }
        }
    }

    @Test
    public void testInferTypesIntAndDates() throws Exception{
        Table table = Table.fromSource(new File ("dates_data.csv"), getTestDataDirectory());
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode schema = objectMapper.readTree(table.inferSchema().asJson());
        JsonNode schemaFiles = schema.get("fields");

        // The field names are the same as the name of the type we are expecting to be inferred.
        for(int i=0; i< schemaFiles.size(); i++) {
            JsonNode node = schemaFiles.get(i);
            Assertions.assertEquals(node.get("name").asText(), node.get("type").asText());
        }
    }


    @Test
    public void testInferTypesIntBoolAndGeopoints() throws Exception{
        Table table = Table.fromSource(new File ("int_bool_geopoint_data.csv"), getTestDataDirectory());

        // Infer
        Schema schema = table.inferSchema();

        Iterator<Field<?>> iter = schema.getFields().iterator();

        // The field names are the same as the name of the type we are expecting to be inferred.
        // So if type is set then it means that inferral worked.
        while(iter.hasNext()){
            Field<?> field = iter.next();
            String name = field.getName();
            String type = field.getType();
            Assertions.assertEquals(name, type);
        }
    }

    @Test
    @DisplayName("Infer Schema from huge Table")
    public void testInferSchemaFromHugeTable() throws Exception{
        File f = new File("data/gdp.csv");
        Table table = Table.fromSource(f, getTestDataDirectory());
        Assertions.assertEquals(11507, table.read().size());
        Schema schema = table.inferSchema(10);
        File schemaFile = new File(getTestDataDirectory(), "schema/gdp_schema.json");
        Schema expectedSchema = null;
        try (FileInputStream fis = new FileInputStream(schemaFile)) {
            expectedSchema = Schema.fromJson(fis, false);
        }

        if (!expectedSchema.equals(schema)) {
            for (int i = 0; i < expectedSchema.getFields().size(); i++) {
                Field expectedField = expectedSchema.getFields().get(i);
                Field actualField = schema.getFields().get(i);
                Assertions.assertEquals(expectedField,actualField);
            }
        }
        Assertions.assertEquals(expectedSchema, schema);
    }

    @Test
    public void testIterateCastKeyedData() throws Exception{
        File testDataDir = getTestDataDirectory();
        // Let's start by defining and building the schema:
        Schema employeeTableSchema = getEmployeeTableSchema();

        // Fetch the data and apply the schema
        File file = new File("data/employee_data.csv");
        Table employeeTable = Table.fromSource(file, testDataDir, employeeTableSchema, TableDataSource.getDefaultCsvFormat());

        Iterator<Map<String, Object>> iter = employeeTable.mappingIterator(false, true, false);

        while(iter.hasNext()){
            Map row = iter.next();

            Assertions.assertEquals(BigInteger.class, row.get("id").getClass());
            Assertions.assertEquals(String.class, row.get("name").getClass());
            Assertions.assertEquals(LocalDate.class, row.get("dateOfBirth").getClass());
            Assertions.assertEquals(Boolean.class, row.get("isAdmin").getClass());
            Assertions.assertEquals(double[].class, row.get("addressCoordinates").getClass());
            Assertions.assertEquals(Duration.class, row.get("contractLength").getClass());
            Assertions.assertTrue(HashMap.class.isAssignableFrom(row.get("info").getClass()));
        }
    }

    @Test
    public void testFetchHeaders() throws Exception{
        File testDataDir = getTestDataDirectory();
        // get path of test CSV file
        File file = new File("data/simple_data.csv");
        Table table = Table.fromSource(file, testDataDir);

        Assertions.assertEquals("[id, title]", Arrays.toString(table.getHeaders()));
    }


    // schema doesn't fit data -> expect exception
    @Test
    public void loadTableWithMismatchingSchema() throws Exception {
        File testDataDir = getTestDataDirectory();
        File file = new File("data/population.json");
        Schema schema = Schema.fromJson(new File(testDataDir, "schema/employee_schema.json"), true);

        Table table = Table.fromSource(file, testDataDir, schema, TableDataSource.getDefaultCsvFormat());

        Assertions.assertThrows(TableValidationException.class, table::validate);
    }


    @Test
    public void testReadUncastData() throws Exception{
        File testDataDir = getTestDataDirectory();
        File file = new File("data/simple_data.csv");
        Table table = Table.fromSource(file, testDataDir);

        Assertions.assertEquals(3, table.read().size());
        Assertions.assertEquals("1", table.read().get(0)[0]);
        Assertions.assertEquals("foo", table.read().get(0)[1]);
    }

    @Test
    public void testReadCastData() throws Exception{
        File testDataDir = getTestDataDirectory();

        // Let's start by defining and building the schema:
        Schema employeeTableSchema = getEmployeeTableSchema();

        // Fetch the data and apply the schema
        File file = new File("data/employee_data.csv");
        Table employeeTable = Table.fromSource(file, testDataDir, employeeTableSchema, TableDataSource.getDefaultCsvFormat());

        // We will iterate the rows and these are the values classes we expect:
        Class<?>[] expectedTypes = new Class<?>[]{
            BigInteger.class,
            String.class,
            LocalDate.class,
            Boolean.class,
            double[].class,
            Duration.class,
            HashMap.class
        };

        List<Object[]> data = employeeTable.read(true);

        for (Object[] row : data) {
            for (int i = 0; i < row.length; i++) {
                Assertions.assertTrue(expectedTypes[i].isAssignableFrom(row[i].getClass()));
            }
        }
    }

    @Test
    public void saveTable() throws Exception{
        final Path tempDirPath = Files.createTempDirectory("tableschema-");
        String createdFileName = "test_data_table.csv";

        File testDataDir = getTestDataDirectory();
        File file = new File("data/simple_data.csv");
        Table loadedTable = Table.fromSource(file, testDataDir);

        loadedTable.writeCsv(new File (tempDirPath.toFile(), createdFileName), CSVFormat.RFC4180);

        Table readTable = Table.fromSource(new File(createdFileName), tempDirPath.toFile());
        Assertions.assertEquals("id", readTable.getHeaders()[0]);
        Assertions.assertEquals("title", readTable.getHeaders()[1]);
        Assertions.assertEquals(3, readTable.read().size());
    }

    @Test
    public void saveTableAlternateSchema() throws Exception{
        final Path tempDirPath = Files.createTempDirectory("tableschema-");
        String createdFileName = "test_data_table.csv";

        File testDataDir = getTestDataDirectory();
        File file = new File("data/population.json");
        Schema schema = Schema.fromJson(new File (testDataDir, "schema/population_schema_alternate.json"), true);
        Table loadedTable = Table.fromSource(file, testDataDir, schema, TableDataSource.getDefaultCsvFormat());

        loadedTable.writeCsv(new File (tempDirPath.toFile(), createdFileName), CSVFormat.RFC4180);

        Table readTable = Table.fromSource(new File(createdFileName), tempDirPath.toFile());
        String[] headers = readTable.getHeaders();

        Assertions.assertEquals("year", headers[0]);
        Assertions.assertEquals("city", headers[1]);
        Assertions.assertEquals("population", headers[2]);
        Assertions.assertEquals(3, readTable.read().size());
    }

    @Test
    public void testInvalidTableCast() throws Exception {
        File file = new File("data/employee_data.csv");
        Table table = Table.fromSource(file, getTestDataDirectory());

        Assertions.assertThrows(TableSchemaException.class, () -> {table.read(true);});
    }

    private Schema getEmployeeTableSchema(){
        Schema schema = new Schema();

        Field<?> idField = new IntegerField("id");
        schema.addField(idField);

        Field<?> nameField = new StringField("name");
        schema.addField(nameField);

        Field<?> dobField = new DateField("dateOfBirth");
        schema.addField(dobField);

        Field<?> isAdminField = new BooleanField("isAdmin");
        schema.addField(isAdminField);

        Field<?> addressCoordinatesField
            = new GeopointField("addressCoordinates", Field.FIELD_FORMAT_OBJECT, null, null, null, null, null, null);
        schema.addField(addressCoordinatesField);

        Field<?> contractLengthField = new DurationField("contractLength");
        schema.addField(contractLengthField);

        Field<?> infoField = new ObjectField("info");
        schema.addField(infoField);

        return schema;
    }


    @Test
    public void testSetCsvFormat() throws Exception {
        File testDataDir = getTestDataDirectory();

        // Let's start by defining and building the schema:
        Schema employeeTableSchema = getEmployeeTableSchema();

        // Fetch the data and apply the schema
        File file = new File("data/employee_data.csv");
        Table employeeTable = Table.fromSource(file, testDataDir, employeeTableSchema, TableDataSource.getDefaultCsvFormat());

        CSVFormat expectedFmt = CSVFormat.INFORMIX_UNLOAD_CSV;
        employeeTable.setCsvFormat(expectedFmt);
        CSVFormat testFmt = employeeTable.getCsvFormat();
        Assertions.assertEquals(expectedFmt, testFmt);
    }

    @Test
    public void testSetSchema() throws Exception {
        File testDataDir = getTestDataDirectory();
        File file = new File("data/employee_data.csv");
        Table table = Table.fromSource(file, testDataDir);

        Schema schema = Schema.fromJson(new File(testDataDir, "schema/employee_schema.json"), true);

        table.setSchema(schema);
        table.read(true);
    }
}
