package io.frictionlessdata.tableschema.table_tests;

import io.frictionlessdata.tableschema.Table;
import io.frictionlessdata.tableschema.tabledatasource.TableDataSource;
import io.frictionlessdata.tableschema.exception.TableValidationException;
import io.frictionlessdata.tableschema.field.Field;
import io.frictionlessdata.tableschema.schema.Schema;
import org.apache.commons.csv.CSVFormat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Year;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static io.frictionlessdata.tableschema.TestHelper.getTestDataDirectory;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 *
 */
public class TableCreationTest {
    private static final String populationTestJson =  "[{\"city\":\"london\",\"year\":2017,\"population\":8780000},"
        + "{\"city\":\"paris\",\"year\":2017,\"population\":2240000},"
        + "{\"city\":\"rome\",\"year\":2017,\"population\":2860000}]";

    private static final Object[][] populationTestData = new Object[][] {
        new Object[]{"london", Year.of(2017), new BigInteger("8780000")},
        new Object[]{"paris",Year.of(2017), new BigInteger("2240000")},
        new Object[]{"rome",Year.of(2017), new BigInteger("2860000")}
    };

    private static final Object[][] populationStringTestData = new Object[][] {
            new Object[]{"london","2017", "8780000"},
            new Object[]{"paris","2017", "2240000"},
            new Object[]{"rome","2017", "2860000"}
    };


    @Test
    @DisplayName("Create a Table from String array without a Schema or CSVFormat")
    public void testCreate1() {
        List<String[]> data = new ArrayList<>();
        for (Object[] row: populationStringTestData) {
            String[] strArr = new String[row.length];
            for (int j = 0; j < row.length; j++) {
                strArr[j] = row[j].toString();
            }
            data.add(strArr);
        }
        String[] headers = new String[]{"city", "year", "population"};
        Table testTable = new Table(data, headers, null);
        Table expectedTable = Table.fromSource(populationTestJson);
        Assertions.assertEquals(expectedTable, testTable);
    }


    @Test
    @DisplayName("Create a Table from String array with Schema but no CSVFormat")
    public void testCreate2() throws Exception {
        File f = new File(getTestDataDirectory(), "schema/population_schema.json");
        Schema schema = Schema.fromJson (f, true);

        List<String[]> data = new ArrayList<>();
        for (Object[] row: populationStringTestData) {
            String[] strArr = new String[row.length];
            for (int j = 0; j < row.length; j++) {
                strArr[j] = row[j].toString();
            }
            data.add(strArr);
        }
        String[] headers = new String[]{"city", "year", "population"};
        Table testTable = new Table(data, headers, schema);
        Table expectedTable = Table.fromSource(populationTestJson, schema, null);
        Assertions.assertEquals(expectedTable, testTable);
    }

    @Test
    @DisplayName("Create a Table from File without a Schema or CSVFormat")
    public void testReadFromValidFilePath() throws Exception{
        File testDataDir = getTestDataDirectory();
        // get path of test CSV file
        File file = new File("data/simple_data.csv");
        Table table = Table.fromSource(file, testDataDir);

        Assertions.assertEquals(3, table.read().size());
        // must not throw an exception
        table.validate();
    }

    @Test
    @DisplayName("Create a Table from CSV String data without a Schema and with default CSVFormat")
    public void testReadFromValidCSVContentString() throws Exception{
        // get path of test CSV file
        URL sourceFileUrl = TableCreationTest.class.getResource("/fixtures/data/simple_data.csv");
        Path path = Paths.get(sourceFileUrl.toURI());
        String csvContent = new String(Files.readAllBytes(path));

        Table table = Table.fromSource(csvContent, null, TableDataSource.getDefaultCsvFormat());

        Assertions.assertEquals(3, table.read().size());
        // must not throw an exception
        table.validate();
    }

    @Test
    @DisplayName("Create a Table from JSON String data without a Schema and with default CSVFormat")
    public void testReadFromValidJSONArray() throws Exception{
        Table table = Table.fromSource(populationTestJson);
        Assertions.assertEquals(3, table.read().size());
        Schema schema = table.inferSchema();
        File f = new File(getTestDataDirectory(), "schema/population_schema.json");
        Schema expectedSchema = null;
        try (FileInputStream fis = new FileInputStream(f)) {
            expectedSchema = Schema.fromJson (fis, false);
        }

        if (!expectedSchema.equals(schema)) {
            for (int i = 0; i < expectedSchema.getFields().size(); i++) {
                Field<?> expectedField = expectedSchema.getFields().get(i);
                Field<?> actualField = schema.getFields().get(i);
                Assertions.assertEquals(expectedField,actualField);
            }
        }
        Assertions.assertEquals(expectedSchema, schema);

    }


    @Test
    @DisplayName("Create a Table from JSON String data with a Schema and with default CSVFormat")
    public void testReadFromValidJSONArrayWithSchema() throws Exception{
        File f = new File(getTestDataDirectory(), "schema/population_schema.json");

        Schema schema = Schema.fromJson (f, true);
        Table table = Table.fromSource(populationTestJson, schema, TableDataSource.getDefaultCsvFormat());
        Assertions.assertEquals(3, table.read().size());
        Schema expectedSchema = null;
        try (FileInputStream fis = new FileInputStream(f)) {
            expectedSchema = Schema.fromJson (fis, false);
        }

        if (!expectedSchema.equals(schema)) {
            for (int i = 0; i < expectedSchema.getFields().size(); i++) {
                Field<?> expectedField = expectedSchema.getFields().get(i);
                Field<?> actualField = schema.getFields().get(i);
                Assertions.assertEquals(expectedField,actualField);
            }
        }
        Assertions.assertEquals(expectedSchema, schema);
        // must not throw an exception
        table.validate();
    }

    @Test
    @DisplayName("Create a Table from URL without a Schema or CSVFormat")
    public void testReadFromValidUrl() throws Exception{
        // get path of test CSV file
        URL url = new URL("https://raw.githubusercontent.com/frictionlessdata/tableschema-java" +
                "/master/src/test/resources/fixtures/data/simple_data.csv");
        Table table = Table.fromSource(url);

        Assertions.assertEquals(3, table.read().size());
        // must not throw an exception
        table.validate();
    }


    @Test
    @DisplayName("Create a Table from URL with Schema and with default CSVFormat and UTF-8 encoding")
    public void testReadFromValidUrlAndValidSchemaURL() throws Exception{
        // get path of test CSV file
        URL tableUrl = new URL("https://raw.githubusercontent.com/frictionlessdata" +
                "/tableschema-java/master/src/test/resources/fixtures/data/population.csv");
        URL schemaUrl = new URL("https://raw.githubusercontent.com/frictionlessdata" +
                "/tableschema-java/master/src/test/resources/fixtures/schema/population_schema.json");
        Table table = Table.fromSource(tableUrl, schemaUrl, null, null);

        File schemaFile = new File(getTestDataDirectory(), "schema/population_schema.json");
        Schema testSchema = Schema.fromJson (schemaFile, true);
        Table testTable = Table.fromSource(populationTestJson, testSchema, TableDataSource.getDefaultCsvFormat());

        Assertions.assertEquals(testTable, table);
    }

    /*
        With schema means data gets cast to objects
     */
    @Test
    @DisplayName("Create a Table from a CSV String with Schema and with default CSVFormat")
    public void testReadFromValidFileWithValidSchema() throws Exception{
        // get path of test CSV file
        URL sourceFileUrl = TableCreationTest.class.getResource("/fixtures/data/population.csv");
        Path path = Paths.get(sourceFileUrl.toURI());
        String csvContent = new String(Files.readAllBytes(path));

        File f = new File(getTestDataDirectory(), "schema/population_schema.json");
        Schema schema = null;
        try (FileInputStream fis = new FileInputStream(f)) {
            schema = Schema.fromJson (fis, false);
        }

        Table table = Table.fromSource(csvContent, schema, TableDataSource.getDefaultCsvFormat());

        Assertions.assertEquals(3, table.read().size());
        List<Object[]> actualData = table.read();
        for (int i = 0; i < actualData.size(); i++) {
            Object[] actualRow = actualData.get(i);
            Object[] testRow = populationTestData[i];
            Assertions.assertArrayEquals(testRow, actualRow);
        }
        // must not throw an exception
        table.validate();
    }


    @Test
    @DisplayName("Create a Table from CSV String data without a Schema and without CSVFormat")
    public void testReadFromValidFileWithoutValidSchema() throws Exception{
        // get path of test CSV file
        URL sourceFileUrl = TableCreationTest.class.getResource("/fixtures/data/population.csv");
        Path path = Paths.get(sourceFileUrl.toURI());
        String csvContent = new String(Files.readAllBytes(path));

        Table table = Table.fromSource(csvContent);

        Assertions.assertEquals(3, table.read().size());
        List<Object[]> actualData = table.read();
        for (int i = 0; i < actualData.size(); i++) {
            Object[] actualRow = actualData.get(i);
            Object[] testRow = populationStringTestData[i];
            Assertions.assertArrayEquals(testRow, actualRow);
        }
        // must not throw an exception
        table.validate();
    }

    @Test
    @DisplayName("Create a Table from CSV String data with Schema and with default CSVFormat")
    public void testReadFromValidCSVFileWithValidSchemaViaStream() throws Exception{
        // get path of test CSV file
        URL sourceFileUrl = TableCreationTest.class.getResource("/fixtures/data/population.csv");
        Path path = Paths.get(sourceFileUrl.toURI());
        String csvContent = new String(Files.readAllBytes(path));

        File f = new File(getTestDataDirectory(), "schema/population_schema.json");

        ByteArrayInputStream bis = new ByteArrayInputStream(csvContent.getBytes());
        FileInputStream fis = new FileInputStream(f);
        Table table = Table.fromSource(bis, fis, TableDataSource.getDefaultCsvFormat());

        File schemaFile = new File(getTestDataDirectory(), "schema/population_schema.json");
        Schema testSchema = Schema.fromJson (schemaFile, true);
        Table testTable = Table.fromSource(populationTestJson, testSchema, TableDataSource.getDefaultCsvFormat());
        Assertions.assertEquals(testTable, table);
        try {
            bis.close();
        } finally {
            fis.close();
        }
        // must not throw an exception
        table.validate();
    }


    @Test
    @DisplayName("Create a Table from JSON String data with Schema and with default CSVFormat")
    public void testReadFromValidJSONFileWithValidSchema() throws Exception{
        // get path of test CSV file
        URL sourceFileUrl = TableCreationTest.class.getResource("/fixtures/data/population.json");
        Path path = Paths.get(sourceFileUrl.toURI());
        String jsonContent = new String(Files.readAllBytes(path));

        File f = new File(getTestDataDirectory(), "schema/population_schema.json");

        ByteArrayInputStream bis = new ByteArrayInputStream(jsonContent.getBytes());
        FileInputStream fis = new FileInputStream(f);
        Table table = Table.fromSource(bis, fis, TableDataSource.getDefaultCsvFormat());

        File schemaFile = new File(getTestDataDirectory(), "schema/population_schema.json");
        Schema testSchema = Schema.fromJson (schemaFile, true);
        Table testTable = Table.fromSource(populationTestJson, testSchema, TableDataSource.getDefaultCsvFormat());
        Assertions.assertEquals(testTable, table);
        try {
            bis.close();
        } finally {
            fis.close();
        }
        // must not throw an exception
        table.validate();
    }


    @Test
    @DisplayName("Create a Table from CSV String data with Schema from Stream and with default CSVFormat")
    public void testReadFromValidFileWithMismatchingValidSchemaViaStream() throws Exception{
        // get path of test CSV file
        URL sourceFileUrl = TableCreationTest.class.getResource("/fixtures/data/population.csv");
        Path path = Paths.get(sourceFileUrl.toURI());
        String csvContent = new String(Files.readAllBytes(path));

        File f = new File(getTestDataDirectory(), "schema/employee_schema.json");

        ByteArrayInputStream bis = new ByteArrayInputStream(csvContent.getBytes());
        FileInputStream fis = new FileInputStream(f);
        Table table = Table.fromSource(bis, fis, TableDataSource.getDefaultCsvFormat());

        // must throw an exception
        Assertions.assertThrows(TableValidationException.class, table::validate);
    }

    @Test
    @DisplayName("Create a Table from CSV String data with BOM with Schema from Stream and with default CSVFormat")
    public void testReadFileWithBOMAndSchema() throws Exception{
        File testDataDir = getTestDataDirectory();
        // get path of test CSV file
        File file = new File("data/simple_data_bom2.tsv");
        Table table = Table.fromSource(file, testDataDir);
        table.setCsvFormat(CSVFormat.TDF.builder().setRecordSeparator("\n").setHeader().build());
        File f = new File(getTestDataDirectory(), "schema/simple_data_schema.json");
        Schema schema = null;
        try (FileInputStream fis = new FileInputStream(f)) {
            schema = Schema.fromJson (fis, false);
        }
        // must not throw an exception
        table.setSchema(schema);
    }

    @Test
    @DisplayName("Create a Table from CSV String data with BOM without a Schema from Stream " +
            "and with custom CSVFormat")
    public void testReadFileWithBOM() throws Exception{
        File testDataDir = getTestDataDirectory();
        // get path of test CSV file
        File file = new File("data/simple_data_bom2.tsv");
        Table table = Table.fromSource(file, testDataDir);
        CSVFormat csvFormat = CSVFormat
                .TDF
                .builder()
                .setRecordSeparator("\n")
                .setHeader(new String[0])
                .build();
        table.setCsvFormat(csvFormat);
        Assertions.assertEquals(3, table.read().size());
        // must not throw an exception
        table.validate();
    }

    // Ensure that a JSON array file with different ordering of properties between the
    // object records can be read into a consistent String array with the help of
    // a Schema.
    @Test
    @DisplayName("Create a Table from JSON String data with a differing ordering of properties and " +
            "with a Schema and with default CSVFormat")
    public void testReadFromValidJSONFileWithDifferingOrderingWithValidSchema() throws Exception{
        // get path of test CSV file
        URL sourceFileUrl = TableCreationTest.class.getResource("/fixtures/data/population_alternate.json");
        Path path = Paths.get(sourceFileUrl.toURI());
        String csvContent = new String(Files.readAllBytes(path));

        File f = new File(getTestDataDirectory(), "schema/population_schema.json");
        Schema schema = null;
        try (FileInputStream fis = new FileInputStream(f)) {
            schema = Schema.fromJson (fis, false);
        }

        Table table = Table.fromSource(csvContent, schema, TableDataSource.getDefaultCsvFormat());

        List<Object[]> actualData = table.read(true);
        for (int i = 0; i < actualData.size(); i++) {
            Object[] actualRow = actualData.get(i);
            Object[] testRow = populationTestData[i];
            Assertions.assertArrayEquals(testRow, actualRow);
        }
        // must not throw an exception
        table.validate();
    }

    @Test
    @DisplayName("Create a Table from URL as UTF-8 without Schema or CSVFormat")
    void csvParsingWithSchemaNullAndCsvFormatNullFromURL() throws Exception{

        URL url = new URL("https://raw.githubusercontent.com/frictionlessdata/tableschema-java/master" +
                "/src/test/resources/fixtures/data/simple_data.csv");
        // Load the data from URL without a schema.
        Table table = Table.fromSource(url, (Schema)null, null, null);

        List<Object[]> data = new ArrayList<>();
        Iterator<Object[]> iterator = table.iterator();
        while (iterator.hasNext()) {
            Object[] record = iterator.next();
            data.add(record);
        }
        Assertions.assertNull(table.getSchema());
        Assertions.assertNotNull(table.getCsvFormat());
        Assertions.assertNotNull(table.getTableDataSource());

        assertEquals("1", data.get(0)[0]);
        assertEquals("foo", data.get(0)[1]);
        assertEquals("2", data.get(1)[0]);
        assertEquals("bar", data.get(1)[1]);
        assertEquals("3", data.get(2)[0]);
        assertEquals("baz", data.get(2)[1]);
    }

    @Test
    @DisplayName("Create a Table from URL without Schema or CSVFormat")
    void csvParsingWithSchemaNullAndCsvFormatNullFromFile() throws Exception{
        File testDataDir = getTestDataDirectory();
        File testFile = new File( "data/simple_data.csv");

        Table table = Table.fromSource(testFile, testDataDir, (Schema)null, null);

        List<Object[]> data = new ArrayList<>();
        Iterator<Object[]> iterator = table.iterator();
        while (iterator.hasNext()) {
            Object[] record = iterator.next();
            data.add(record);
        }
        Assertions.assertNull(table.getSchema());
        // The CSVFormat of the Table can't be null, but must be default even if we create the table with a null value
        Assertions.assertNotNull(table.getCsvFormat());
        Assertions.assertEquals(TableDataSource.getDefaultCsvFormat(), table.getCsvFormat());
        Assertions.assertNotNull(table.getTableDataSource());

        assertEquals("1", data.get(0)[0]);
        assertEquals("foo", data.get(0)[1]);
        assertEquals("2", data.get(1)[0]);
        assertEquals("bar", data.get(1)[1]);
        assertEquals("3", data.get(2)[0]);
        assertEquals("baz", data.get(2)[1]);
    }
}
