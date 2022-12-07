package io.frictionlessdata.tableschema.tabledatasource;

import io.frictionlessdata.tableschema.Table;
import io.frictionlessdata.tableschema.TestHelper;
import io.frictionlessdata.tableschema.schema.Schema;
import org.apache.commons.csv.CSVFormat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.util.stream.Collectors;

import static io.frictionlessdata.tableschema.TestHelper.getTestDataDirectory;

class JsonArrayTableDataSourceTest {
    private String populationJson = "[" +
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

    private String populationJsonMissingEntry = "[" +
            "{" +
            "\"city\": \"london\"," +
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

    private final String[] populationHeaders = new String[]{
            "city", "year", "population"
    };

    @Test
    @DisplayName("Test TableDataSource.createDataSourceFormat creates JsonArrayTableDataSource from " +
            "JSON array data")
    void testCreateJsonArrayDataSource() throws Exception{
        TableDataSource ds = TableDataSource.fromSource(populationJson);
        Assertions.assertTrue(ds instanceof JsonArrayTableDataSource);
    }

    @Test
    @DisplayName("Validate Header extraction from a JsonArrayTableDataSource")
    void testJsonArrayDataSourceHeaders() throws Exception{
        TableDataSource ds = TableDataSource.fromSource(populationJson);
        String[] headers = ds.getHeaders();
        Assertions.assertArrayEquals(populationHeaders, headers);
    }

    @Test
    @DisplayName("Validate creating a JsonArrayTableDataSource from JSON file")
    void testSafePathCreationJson() throws Exception {
        TableDataSource ds = TableDataSource.fromSource(new File("simple_geojson.json"),
                TestHelper.getTestDataDirectory());
        Assertions.assertNotNull(ds);
    }
/*
    @Test
    @DisplayName("Validate creating and writing a JsonArrayTableDataSource from JSON with null entries")
    void testCreateAndWriteJsonArrayDataSourceWithMissingEntries() throws Exception {
        TableDataSource ds = TableDataSource.createDataSourceFormat(populationJsonMissingEntry);

        File tempFile = Files.createTempFile("tableschema-", ".json").toFile();
        try (FileWriter wr = new FileWriter(tempFile);
                BufferedWriter bwr = new BufferedWriter(wr)) {
            ds.writeCsv(bwr, null, populationHeaders);
        }
    }

    @Test
    @DisplayName("Validate creating and writing a JsonArrayTableDataSource from JSON without headers " +
            "raises an exception")
    void testCreateAndWriteJsonArrayDataSourceWithoutHeaders() throws Exception {
        TableDataSource ds = TableDataSource.createDataSourceFormat(populationJsonMissingEntry);

        File tempFile = Files.createTempFile("tableschema-", ".json").toFile();
        Assertions.assertThrows(Exception.class, () -> {
        try (FileWriter wr = new FileWriter(tempFile);
             BufferedWriter bwr = new BufferedWriter(wr)) {
            ds.writeCsv(bwr, null, null);
        }
        });
    }
*/
    @Test
    @DisplayName("Validate creating a JsonArrayTableDataSource from InputStream containing JSON")
    void testSafePathInputStreamCreationJson() throws Exception {
        TableDataSource ds;
        File inFile = new File(TestHelper.getTestDataDirectory(), "data/population.json");
        try (FileInputStream is = new FileInputStream(inFile)) {
            ds = new JsonArrayTableDataSource(is);
            Assertions.assertArrayEquals(populationHeaders, ds.getHeaders());
        }
        Assertions.assertNotNull(ds);
    }

    @Test
    @DisplayName("Validate creating a JsonArrayTableDataSource from wrong input data (CSV) raises" +
            "an exception")
    void testWrongInputStreamCreationJson() throws Exception {
        File inFile = new File(TestHelper.getTestDataDirectory(), "data/population.csv");
        Assertions.assertThrows(Exception.class, () -> {
            try (FileInputStream is = new FileInputStream(inFile)) {
                TableDataSource ds = new JsonArrayTableDataSource(is);
                Assertions.assertArrayEquals(populationHeaders, ds.getHeaders());
            }
        });
    }

    @Test
    @DisplayName("Validate reading from a JSON file and writing out yields the same result")
    public void testJsonDataSourceFormatToJson() throws Exception{
        File schemaFile = new File(getTestDataDirectory(), "schema/employee_full_schema.json");
        Schema schema = Schema.fromJson (schemaFile, true);

        File inFile = new File("data/employee_full.json");
        Table table = Table.fromSource(inFile, getTestDataDirectory(), schema, null);
        FileWriter fileWriter = new FileWriter("test.json");
        table.write(fileWriter, TableDataSource.Format.FORMAT_JSON);
        fileWriter.close();

        String s = table
                .asJson()
                .replaceAll("\\s+", " ")
                .replaceAll(" }", "}")
                .replaceAll("\" : ", "\":")
                .replaceAll(" ]", "]");

        File referenceFile = new File(getTestDataDirectory(), "data/employee_full.json");
        String referenceContent = String.join("", Files.readAllLines(referenceFile.toPath()));
        referenceContent = referenceContent
                .replaceAll("\\s+", " ")
                .replaceAll(" }", "}")
                .replaceAll(" ]", "]")
                .replaceAll("\" : ", "\":");

        Assertions.assertEquals(referenceContent, s);

        File testFile = new File( "test.json");
        BufferedReader rdr = new BufferedReader(new FileReader(testFile));
        String testContent = rdr.lines().collect(Collectors.joining("\n"));
        testContent = testContent
                .replaceAll("\\s+", " ")
                .replaceAll(" }", "}")
                .replaceAll(" ]", "]")
                .replaceAll("\" : ", "\":");
        Assertions.assertEquals(referenceContent, testContent);

        File testFileCsv = new File("test.csv");
        table.writeCsv(testFileCsv, CSVFormat.DEFAULT);

        File referenceFileCsv = new File(getTestDataDirectory(), "data/employee_full.csv");
        String referenceContentCsv = String.join("\n", Files.readAllLines(referenceFileCsv.toPath()));
        referenceContentCsv = referenceContentCsv
                .replaceAll(" +", " ")
                .replaceAll("TRUE", "true")
                .replaceAll("FALSE", "false");

        rdr = new BufferedReader(new FileReader(testFileCsv));
        String testContentCsv = rdr.lines().collect(Collectors.joining("\n"));
        testContentCsv = testContentCsv
                .replaceAll(" +", " ")
                .replaceAll("\\[ ", "[")
                .replaceAll(" ]", "]");
        Assertions.assertEquals(referenceContentCsv, testContentCsv);
    }
}
