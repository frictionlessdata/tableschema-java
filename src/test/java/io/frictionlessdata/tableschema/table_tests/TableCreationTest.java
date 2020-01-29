package io.frictionlessdata.tableschema.table_tests;

import io.frictionlessdata.tableschema.datasourceformat.DataSourceFormat;
import io.frictionlessdata.tableschema.exception.TableValidationException;
import io.frictionlessdata.tableschema.field.Field;
import io.frictionlessdata.tableschema.schema.Schema;
import io.frictionlessdata.tableschema.Table;
import org.apache.commons.csv.CSVFormat;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Year;
import java.util.*;

import static io.frictionlessdata.tableschema.TestHelper.getTestDataDirectory;

/**
 *
 *
 */
public class TableCreationTest {
    private static String populationTestJson =  "[{\"city\":\"london\",\"year\":2017,\"population\":8780000},"
        + "{\"city\":\"paris\",\"year\":2017,\"population\":2240000},"
        + "{\"city\":\"rome\",\"year\":2017,\"population\":28600}]";

    private static Object[][] populationTestData = new Object[][] {
        new Object[]{"london", Year.of(2017), new BigInteger("8780000")},
        new Object[]{"paris",Year.of(2017), new BigInteger("2240000")},
        new Object[]{"rome",Year.of(2017), new BigInteger("2860000")}
    };

    private static Object[][] populationStringTestData = new Object[][] {
            new Object[]{"london","2017", "8780000"},
            new Object[]{"paris","2017", "2240000"},
            new Object[]{"rome","2017", "2860000"}
    };

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Rule
    public final ExpectedException exception = ExpectedException.none();


    @Test
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
        Assert.assertEquals(expectedTable, testTable);
    }


    @Test
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
        Assert.assertEquals(expectedTable, testTable);
    }

    @Test
    public void testReadFromValidFilePath() throws Exception{
        File testDataDir = getTestDataDirectory();
        // get path of test CSV file
        File file = new File("data/simple_data.csv");
        Table table = Table.fromSource(file, testDataDir);

        Assert.assertEquals(3, table.read().size());
        // must not throw an exception
        table.validate();
    }

    @Test
    public void testReadFromValidCSVContentString() throws Exception{
        // get path of test CSV file
        URL sourceFileUrl = TableCreationTest.class.getResource("/fixtures/data/simple_data.csv");
        Path path = Paths.get(sourceFileUrl.toURI());
        String csvContent = new String(Files.readAllBytes(path));

        Table table = Table.fromSource(csvContent, null, DataSourceFormat.getDefaultCsvFormat());

        Assert.assertEquals(3, table.read().size());
        // must not throw an exception
        table.validate();
    }

    @Test
    public void testReadFromValidJSONArray() throws Exception{
        Table table = Table.fromSource(populationTestJson);
        Assert.assertEquals(3, table.read().size());
        Schema schema = table.inferSchema();
        File f = new File(getTestDataDirectory(), "schema/population_schema.json");
        Schema expectedSchema = null;
        try (FileInputStream fis = new FileInputStream(f)) {
            expectedSchema = Schema.fromJson (fis, false);
        }

        if (!expectedSchema.equals(schema)) {
            for (int i = 0; i < expectedSchema.getFields().size(); i++) {
                Field expectedField = expectedSchema.getFields().get(i);
                Field actualField = schema.getFields().get(i);
                Assert.assertEquals(expectedField,actualField);
            }
        }
        Assert.assertEquals(expectedSchema, schema);

    }


    @Test
    public void testReadFromValidJSONArrayWithSchema() throws Exception{
        File f = new File(getTestDataDirectory(), "schema/population_schema.json");

        Schema schema = Schema.fromJson (f, true);
        Table table = Table.fromSource(populationTestJson, schema, DataSourceFormat.getDefaultCsvFormat());
        Assert.assertEquals(3, table.read().size());
        Schema expectedSchema = null;
        try (FileInputStream fis = new FileInputStream(f)) {
            expectedSchema = Schema.fromJson (fis, false);
        }

        if (!expectedSchema.equals(schema)) {
            for (int i = 0; i < expectedSchema.getFields().size(); i++) {
                Field expectedField = expectedSchema.getFields().get(i);
                Field actualField = schema.getFields().get(i);
                Assert.assertEquals(expectedField,actualField);
            }
        }
        Assert.assertEquals(expectedSchema, schema);
        // must not throw an exception
        table.validate();
    }

    @Test
    public void testReadFromValidUrl() throws Exception{
        // get path of test CSV file
        URL url = new URL("https://raw.githubusercontent.com/frictionlessdata/tableschema-java" +
                "/master/src/test/resources/fixtures/data/simple_data.csv");
        Table table = Table.fromSource(url);

        Assert.assertEquals(3, table.read().size());
        // must not throw an exception
        table.validate();
    }


    @Test
    public void testReadFromValidUrlAndValidSchemaURL() throws Exception{
        // get path of test CSV file
        URL tableUrl = new URL("https://raw.githubusercontent.com/frictionlessdata" +
                "/tableschema-java/master/src/test/resources/fixtures/data/population.csv");
        URL schemaUrl = new URL("https://raw.githubusercontent.com/frictionlessdata" +
                "/tableschema-java/master/src/test/resources/fixtures/schema/population_schema.json");
        Table table = Table.fromSource(tableUrl, schemaUrl, null);

        File schemaFile = new File(getTestDataDirectory(), "schema/population_schema.json");
        Schema testSchema = Schema.fromJson (schemaFile, true);
        Table testTable = Table.fromSource(populationTestJson, testSchema, DataSourceFormat.getDefaultCsvFormat());

        Assert.assertEquals(testTable, table);
    }

    /*
        With schema means data gets casted to objects
     */
    @Test
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

        Table table = Table.fromSource(csvContent, schema, DataSourceFormat.getDefaultCsvFormat());

        Assert.assertEquals(3, table.read().size());
        List<Object[]> actualData = table.read();
        for (int i = 0; i < actualData.size(); i++) {
            Object[] actualRow = actualData.get(i);
            Object[] testRow = populationTestData[i];
            Assert.assertArrayEquals(testRow, actualRow);
        }
        // must not throw an exception
        table.validate();
    }

    /*
        With schema means data does not get casted to objects, we will
        receive String-Arrays
     */
    @Test
    public void testReadFromValidFileWithoutValidSchema() throws Exception{
        // get path of test CSV file
        URL sourceFileUrl = TableCreationTest.class.getResource("/fixtures/data/population.csv");
        Path path = Paths.get(sourceFileUrl.toURI());
        String csvContent = new String(Files.readAllBytes(path));

        Table table = Table.fromSource(csvContent);

        Assert.assertEquals(3, table.read().size());
        List<Object[]> actualData = table.read();
        for (int i = 0; i < actualData.size(); i++) {
            Object[] actualRow = actualData.get(i);
            Object[] testRow = populationStringTestData[i];
            Assert.assertArrayEquals(testRow, actualRow);
        }
        // must not throw an exception
        table.validate();
    }

    @Test
    public void testReadFromValidCSVFileWithValidSchemaViaStream() throws Exception{
        // get path of test CSV file
        URL sourceFileUrl = TableCreationTest.class.getResource("/fixtures/data/population.csv");
        Path path = Paths.get(sourceFileUrl.toURI());
        String csvContent = new String(Files.readAllBytes(path));

        File f = new File(getTestDataDirectory(), "schema/population_schema.json");

        ByteArrayInputStream bis = new ByteArrayInputStream(csvContent.getBytes());
        FileInputStream fis = new FileInputStream(f);
        Table table = Table.fromSource(bis, fis, DataSourceFormat.getDefaultCsvFormat());

        File schemaFile = new File(getTestDataDirectory(), "schema/population_schema.json");
        Schema testSchema = Schema.fromJson (schemaFile, true);
        Table testTable = Table.fromSource(populationTestJson, testSchema, DataSourceFormat.getDefaultCsvFormat());
        Assert.assertEquals(testTable, table);
        try {
            bis.close();
        } finally {
            fis.close();
        }
        // must not throw an exception
        table.validate();
    }


    @Test
    public void testReadFromValidJSONFileWithValidSchemaViaStream() throws Exception{
        // get path of test CSV file
        URL sourceFileUrl = TableCreationTest.class.getResource("/fixtures/data/population.json");
        Path path = Paths.get(sourceFileUrl.toURI());
        String jsonContent = new String(Files.readAllBytes(path));

        File f = new File(getTestDataDirectory(), "schema/population_schema.json");

        ByteArrayInputStream bis = new ByteArrayInputStream(jsonContent.getBytes());
        FileInputStream fis = new FileInputStream(f);
        Table table = Table.fromSource(bis, fis, DataSourceFormat.getDefaultCsvFormat());

        File schemaFile = new File(getTestDataDirectory(), "schema/population_schema.json");
        Schema testSchema = Schema.fromJson (schemaFile, true);
        Table testTable = Table.fromSource(populationTestJson, testSchema, DataSourceFormat.getDefaultCsvFormat());
        Assert.assertEquals(testTable, table);
        try {
            bis.close();
        } finally {
            fis.close();
        }
        // must not throw an exception
        table.validate();
    }


    @Test
    public void testReadFromValidFileWithMismatchingValidSchemaViaStream() throws Exception{
        // get path of test CSV file
        URL sourceFileUrl = TableCreationTest.class.getResource("/fixtures/data/population.csv");
        Path path = Paths.get(sourceFileUrl.toURI());
        String csvContent = new String(Files.readAllBytes(path));

        File f = new File(getTestDataDirectory(), "schema/employee_schema.json");

        ByteArrayInputStream bis = new ByteArrayInputStream(csvContent.getBytes());
        FileInputStream fis = new FileInputStream(f);
        Table table = Table.fromSource(bis, fis, DataSourceFormat.getDefaultCsvFormat());

        // must throw an exception
        exception.expect(TableValidationException.class);
        table.validate();
    }


    @Test
    public void testReadFileWithBOM() throws Exception{
        File testDataDir = getTestDataDirectory();
        // get path of test CSV file
        File file = new File("data/simple_data_bom2.tsv");
        Table table = Table.fromSource(file, testDataDir);
        table.setCsvFormat(CSVFormat.TDF.withRecordSeparator("\n").withHeader(new String[0]));
        Assert.assertEquals(3, table.read().size());
        // must not throw an exception
        table.validate();
    }

    // Ensure that a JSON array file with different ordering of properties between the
    // object records can be read into a consistent String array with the help of
    // a Schema.
    @Test
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

        Table table = Table.fromSource(csvContent, schema, DataSourceFormat.getDefaultCsvFormat());

        List<Object[]> actualData = table.read(false);
        for (int i = 0; i < actualData.size(); i++) {
            Object[] actualRow = actualData.get(i);
            Object[] testRow = populationTestData[i];
            Assert.assertArrayEquals(testRow, actualRow);
        }
        // must not throw an exception
        table.validate();
    }

}
