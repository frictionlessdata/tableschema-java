package io.frictionlessdata.tableschema.iterator;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.frictionlessdata.tableschema.Table;
import io.frictionlessdata.tableschema.TestHelper;
import io.frictionlessdata.tableschema.tabledatasource.TableDataSource;
import io.frictionlessdata.tableschema.exception.InvalidCastException;
import io.frictionlessdata.tableschema.schema.Schema;
import org.apache.commons.csv.CSVFormat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static io.frictionlessdata.tableschema.TestHelper.getResourceFile;
import static io.frictionlessdata.tableschema.TestHelper.getTestDataDirectory;

class TableIteratorTest {
    private static Table validPopulationTable = null;
    private static Table nullValuesPopulationTable = null;
    private static Table invalidPopulationTable = null;

    private static final String jsonData = "[" +
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


    @BeforeEach
    void setUp() throws Exception {
        File f = new File(getTestDataDirectory(), "schema/population_schema.json");
        Schema validPopulationSchema = null;
        try (FileInputStream fis = new FileInputStream(f)) {
            validPopulationSchema = Schema.fromJson (fis, false);
        }
        File testDataDir = getTestDataDirectory();
        File file = new File("data/population.csv");
        validPopulationTable
                = Table.fromSource(file, testDataDir, validPopulationSchema, TableDataSource.getDefaultCsvFormat());
        file = new File("data/population-null-values.csv");
        nullValuesPopulationTable
                = Table.fromSource(file, testDataDir, validPopulationSchema, TableDataSource.getDefaultCsvFormat());
        file = new File("data/population-invalid.csv");
        invalidPopulationTable
                = Table.fromSource(file, testDataDir, validPopulationSchema, TableDataSource.getDefaultCsvFormat());

    }

    @Test
    @DisplayName("Test Iterator")
    void hasNext() throws Exception {
        Assertions.assertTrue(validPopulationTable.iterator().hasNext());
        Assertions.assertTrue(validPopulationTable.iterator(true, true, true, false).hasNext());
        Assertions.assertFalse(Table.fromSource("").iterator(true, true, true, false).hasNext());
        Assertions.assertFalse(Table.fromSource("").iterator().hasNext());
    }

    @Test
    @DisplayName("Test non-casting Iterator")
    void nonCasting() {
        validPopulationTable.iterator(false, false, false, false).forEachRemaining((r) -> {
            Assertions.assertTrue(r instanceof Object[]);
            Arrays.stream(((Object[])r)).forEach((c)->{
                Assertions.assertTrue(c instanceof String);
            });
        });

    }


    @Test
    @DisplayName("Test no Schema, but casting Iterator")
    void noSchema() throws Exception {
        File testDataDir = getTestDataDirectory();
        File file = new File("data/population.csv");
        Table populationTable
                = Table.fromSource(file, testDataDir, null, TableDataSource.getDefaultCsvFormat());
        populationTable.iterator(false, false, true, false).forEachRemaining((r) -> {
            Assertions.assertTrue(r instanceof Object[]);
            Arrays.stream(((Object[])r)).forEach((c)->{
                Assertions.assertTrue(c instanceof String);
            });
        });

    }

    @Test
    @DisplayName("Test Iterator throws on remove")
    void remove() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            nullValuesPopulationTable.iterator(true, false, true, false).remove();
        });
    }

    @Test
    @DisplayName("Test casting Iterator")
    void testNextCast()  {
        Iterator<Map<String, Object>> iter
                = nullValuesPopulationTable.mappingIterator( false, true, false);
        Map<String, Object> obj = iter.next();
        Assertions.assertNull(obj.get("year"));
        obj = iter.next();
        Assertions.assertNull(obj.get("year"));
    }

    @Test
    @DisplayName("Test defaul keyed Iterator")
    void testNextCast2() throws Exception {
        Iterator<Map<String, Object>> iter = nullValuesPopulationTable.mappingIterator();
        Map<String, Object> obj =  (Map<String, Object>)iter.next();
        Assertions.assertNull(obj.get("year"));
        obj =  (Map<String, Object>)iter.next();
        Assertions.assertNull(obj.get("year"));
    }

    @Test
    void testNextInvalidCast() throws Exception {
        Assertions.assertThrows(InvalidCastException.class, () -> {
            invalidPopulationTable.iterator(true, false, true, false).next();
        });
    }

    @Test
    @DisplayName("Test String Array Iterator on data with trailing null values in rows")
    void testTrailingNullsIterator() throws Exception{
        File f = new File(getTestDataDirectory(), "schema/simple_data_schema.json");
        Schema validSimpleSchema = Schema.fromJson(f, true);
        File file = new File("data/simple_data_utf16le_trailing_nulls.tsv");
        Table table = Table.fromSource(
                file, getTestDataDirectory(),
                validSimpleSchema,
                TableDataSource.getDefaultCsvFormat().builder().setDelimiter('\t').build(),
                StandardCharsets.UTF_16LE);

        // Expected data.
        List<String[]> expectedData  = new ArrayList<>();
        expectedData.add(new String[]{"1", "foo"});
        expectedData.add(new String[]{"2", ""});
        expectedData.add(new String[]{"3", "baz"});

        // Get Iterator.
        Iterator<String[]> iter = table.stringArrayIterator();
        int expectedDataIndex = 0;

        // Assert data.
        while(iter.hasNext()){
            String[] record = iter.next();
            String id = record[0];
            String title = record[1];

            Assertions.assertEquals(expectedData.get(expectedDataIndex)[0], id);
            Assertions.assertEquals(expectedData.get(expectedDataIndex)[1], title);

            expectedDataIndex++;
        }
    }


    @Test
    @DisplayName("Test String Array Iterator")
    void testStringArrayIterateDataFromJSONFormatAlternateSchema() throws Exception{
        //set a schema to guarantee the ordering of properties
        Schema schema = Schema.fromJson(new File(getTestDataDirectory(), "/schema/population_schema_alternate.json"), true);
        Table table = Table.fromSource(jsonData, schema, TableDataSource.getDefaultCsvFormat());

        // Expected data.
        List<String[]> expectedData = this.getExpectedAlternatePopulationData();

        // Get Iterator.
        Iterator<String[]> iter = table.stringArrayIterator();
        int expectedDataIndex = 0;

        // Assert data.
        while(iter.hasNext()){
            String[] record = iter.next();
            String year = record[0];
            String city = record[1];
            String population = record[2];

            Assertions.assertEquals(expectedData.get(expectedDataIndex)[0], year);
            Assertions.assertEquals(expectedData.get(expectedDataIndex)[1], city);
            Assertions.assertEquals(expectedData.get(expectedDataIndex)[2], population);

            expectedDataIndex++;
        }
    }

    @Test
    @DisplayName("Test String Iterator on JSON input data")
    void testStringArrayIterateDataFromJSONFormatAlternateSchemaNoRelations() throws Exception{
        //set a schema to guarantee the ordering of properties
        Schema schema = Schema.fromJson(new File(getTestDataDirectory(), "/schema/population_schema_alternate.json"), true);
        Table table = Table.fromSource(jsonData, schema, TableDataSource.getDefaultCsvFormat());

        // Expected data.
        List<String[]> expectedData = this.getExpectedAlternatePopulationData();

        // Get Iterator.
        Iterator<String[]> iter = table.stringArrayIterator(false);
        int expectedDataIndex = 0;

        // Assert data.
        while(iter.hasNext()){
            String[] record = iter.next();
            String year = record[0];
            String city = record[1];
            String population = record[2];

            Assertions.assertEquals(expectedData.get(expectedDataIndex)[0], year);
            Assertions.assertEquals(expectedData.get(expectedDataIndex)[1], city);
            Assertions.assertEquals(expectedData.get(expectedDataIndex)[2], population);

            expectedDataIndex++;
        }
    }

    @Test
    @DisplayName("Test Object Array Iterator on JSON data")
    void testStringObjectArrayIterateDataFromJSONFormatAlternateSchema() throws Exception{
        //set a schema to guarantee the ordering of properties
        Schema schema = Schema.fromJson(new File(getTestDataDirectory(), "/schema/population_schema_alternate.json"), true);
        Table table = Table.fromSource(jsonData, schema, TableDataSource.getDefaultCsvFormat());

        // Expected data.
        List<String[]> expectedData = this.getExpectedAlternatePopulationData();

        // Get Iterator.
        Iterator<Object> iter = table.iterator(false, false, true, false);
        int expectedDataIndex = 0;

        // Assert data.
        while(iter.hasNext()){
            Object[] record = (Object[])iter.next();
            String year = record[0].toString();
            String city = record[1].toString();
            String population = record[2].toString();

            Assertions.assertEquals(expectedData.get(expectedDataIndex)[0], year);
            Assertions.assertEquals(expectedData.get(expectedDataIndex)[1], city);
            Assertions.assertEquals(expectedData.get(expectedDataIndex)[2], population);

            expectedDataIndex++;
        }
    }


    @Test
    @DisplayName("Test keyed Iterator on JSON data")
    void testStringObjectMapIterateDataFromJSONFormatAlternateSchema() throws Exception{
        //set a schema to guarantee the ordering of properties
        Schema schema = Schema.fromJson(new File(getTestDataDirectory(), "/schema/population_schema_alternate.json"), true);
        Table table = Table.fromSource(jsonData, schema, TableDataSource.getDefaultCsvFormat());

        // Expected data.
        List<String[]> expectedData = this.getExpectedAlternatePopulationData();

        // Get Iterator.
        Iterator<Map<String, Object>> iter = table.mappingIterator(false, true, false);
        int expectedDataIndex = 0;

        // Assert data.
        while(iter.hasNext()){
            Map<String, Object> record = iter.next();
            String year = record.get("year").toString();
            String city = record.get("city").toString();
            String population = record.get("population").toString();

            Assertions.assertEquals(expectedData.get(expectedDataIndex)[0], year);
            Assertions.assertEquals(expectedData.get(expectedDataIndex)[1], city);
            Assertions.assertEquals(expectedData.get(expectedDataIndex)[2], population);

            expectedDataIndex++;
        }
    }

    @Test
    @DisplayName("No headers specified")
    void noHeaders() throws Exception{
        ObjectMapper objectMapper = new ObjectMapper();
        File basePath = getResourceFile("/fixtures/data/");
        String expectedString = TestHelper.getResourceFileContent(
                "/fixtures/schema/employee_schema.json");
        Schema schema = Schema.fromJson(expectedString, true);

        File source = new File("employee_data_no_headers.csv");
        // the CSVFormat.DEFAULT format specifies "no header"
        Table table = Table.fromSource(source, basePath, schema, CSVFormat.DEFAULT);
        Iterator<Object> iterator = table.iterator(false, false, true, false);
        Object[] testRow = (Object[])iterator.next();
        Assertions.assertEquals(BigInteger.class, testRow[0].getClass());
        Assertions.assertEquals(1, ((BigInteger) testRow[0]).intValue());
        Assertions.assertEquals(String.class, testRow[1].getClass());
        Assertions.assertEquals("John Doe", testRow[1]);
        Assertions.assertEquals(LocalDate.class, testRow[2].getClass());
        Assertions.assertEquals("1976-01-13", ((LocalDate) testRow[2]).format(DateTimeFormatter.ISO_LOCAL_DATE));
        Assertions.assertEquals(Boolean.class, testRow[3].getClass());
        Assertions.assertEquals(true, testRow[3]);
        Assertions.assertTrue(testRow[4] instanceof double[]);
        Assertions.assertArrayEquals(new double[]{90.123, 45.0}, ((double[]) testRow[4]));
        Assertions.assertEquals(Duration.class, testRow[5].getClass());
        Assertions.assertEquals("PT51H4M", ((Duration) testRow[5]).toString());
        Assertions.assertEquals(java.util.LinkedHashMap.class, testRow[6].getClass());
        Assertions.assertEquals(90, ((Map) testRow[6]).get("ssn"));
        Assertions.assertEquals(45, ((Map) testRow[6]).get("pin"));
        Assertions.assertEquals(83.23, ((Map) testRow[6]).get("rate"));
    }

    private List<String[]> getExpectedAlternatePopulationData(){
        List<String[]> expectedData  = new ArrayList<>();
        expectedData.add(new String[]{"2017", "london", "8780000"});
        expectedData.add(new String[]{"2017", "paris", "2240000"});
        expectedData.add(new String[]{"2017", "rome", "2860000"});

        return expectedData;
    }
}