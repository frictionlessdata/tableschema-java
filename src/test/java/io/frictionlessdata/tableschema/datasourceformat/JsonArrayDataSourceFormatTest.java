package io.frictionlessdata.tableschema.datasourceformat;

import io.frictionlessdata.tableschema.TestHelper;
import org.apache.commons.csv.CSVFormat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;

import io.frictionlessdata.tableschema.schema.Schema;
import io.frictionlessdata.tableschema.Table;

import static io.frictionlessdata.tableschema.TestHelper.getTestDataDirectory;

class JsonArrayDataSourceFormatTest {
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
    @DisplayName("Test DataSourceFormat.createDataSourceFormat creates JsonArrayDataSourceFormat from " +
            "JSON array data")
    void testCreateJsonArrayDataSource() throws Exception{
        DataSourceFormat ds = DataSourceFormat.createDataSourceFormat(populationJson);
        Assertions.assertTrue(ds instanceof JsonArrayDataSourceFormat);
    }

    @Test
    @DisplayName("Validate Header extraction from a JsonArrayDataSourceFormat")
    void testJsonArrayDataSourceHeaders() throws Exception{
        DataSourceFormat ds = DataSourceFormat.createDataSourceFormat(populationJson);
        String[] headers = ds.getHeaders();
        Assertions.assertArrayEquals(populationHeaders, headers);
    }

    @Test
    @DisplayName("Validate creating a JsonArrayDataSourceFormat from JSON file")
    void testSafePathCreationJson() throws Exception {
        DataSourceFormat ds = DataSourceFormat.createDataSourceFormat(new File("simple_geojson.json"),
                TestHelper.getTestDataDirectory());
        Assertions.assertNotNull(ds);
    }
/*
    @Test
    @DisplayName("Validate creating and writing a JsonArrayDataSourceFormat from JSON with null entries")
    void testCreateAndWriteJsonArrayDataSourceWithMissingEntries() throws Exception {
        DataSourceFormat ds = DataSourceFormat.createDataSourceFormat(populationJsonMissingEntry);

        File tempFile = Files.createTempFile("tableschema-", ".json").toFile();
        try (FileWriter wr = new FileWriter(tempFile);
                BufferedWriter bwr = new BufferedWriter(wr)) {
            ds.writeCsv(bwr, null, populationHeaders);
        }
    }

    @Test
    @DisplayName("Validate creating and writing a JsonArrayDataSourceFormat from JSON without headers " +
            "raises an exception")
    void testCreateAndWriteJsonArrayDataSourceWithoutHeaders() throws Exception {
        DataSourceFormat ds = DataSourceFormat.createDataSourceFormat(populationJsonMissingEntry);

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
    @DisplayName("Validate creating a JsonArrayDataSourceFormat from InputStream containing JSON")
    void testSafePathInputStreamCreationJson() throws Exception {
        DataSourceFormat ds;
        File inFile = new File(TestHelper.getTestDataDirectory(), "data/population.json");
        try (FileInputStream is = new FileInputStream(inFile)) {
            ds = new JsonArrayDataSourceFormat(is);
            Assertions.assertArrayEquals(populationHeaders, ds.getHeaders());
        }
        Assertions.assertNotNull(ds);
    }

    @Test
    @DisplayName("Validate creating a JsonArrayDataSourceFormat from wrong input data (CSV) raises" +
            "an exception")
    void testWrongInputStreamCreationJson() throws Exception {
        File inFile = new File(TestHelper.getTestDataDirectory(), "data/population.csv");
        Assertions.assertThrows(Exception.class, () -> {
            try (FileInputStream is = new FileInputStream(inFile)) {
                DataSourceFormat ds = new JsonArrayDataSourceFormat(is);
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
        table.write(fileWriter, DataSourceFormat.Format.FORMAT_JSON);
        fileWriter.close();
        String s = table.asJson().replaceAll("\\s+", " ").replaceAll(" }", "}");
        FileWriter jsonWriter = new FileWriter("test.json");
        table.write(jsonWriter, DataSourceFormat.Format.FORMAT_JSON);
        jsonWriter.close();

        File referenceFile = new File(getTestDataDirectory(), "data/employee_full.json");
        String referenceContent = String.join("", Files.readAllLines(referenceFile.toPath()));
        referenceContent = referenceContent.replaceAll("\\s+", " ").replaceAll(" }", "}");

        Assertions.assertEquals(referenceContent, s);

        File testFile = new File( "test.json");
        String testContent = String.join("", Files.readAllLines(testFile.toPath()));
        testContent = testContent.replaceAll("\\s+", " ").replaceAll(" }", "}");
        Assertions.assertEquals(referenceContent, testContent);

        File testFileCsv = new File("test.csv");
        table.writeCsv(testFileCsv, CSVFormat.DEFAULT);
        File referenceFileCsv = new File(getTestDataDirectory(), "data/employee_full.csv");
        String referenceContentCsv = String.join("", Files.readAllLines(referenceFileCsv.toPath()));
        referenceContentCsv = referenceContentCsv
                .replaceAll("\\s+", " ")
                .replaceAll("TRUE", "true")
                .replaceAll("FALSE", "false");
        String testContentCsv = String.join("", Files.readAllLines(testFileCsv.toPath()));
        testContentCsv = testContentCsv
                .replaceAll("\\s+", " ")
                .replaceAll("\\[ ", "[")
                .replaceAll(" ]", "]");
                /*.replaceAll(" }", "}");*/
        testContentCsv = testContentCsv.replaceAll("\\s+", " ").replaceAll(" }", "}");
        Assertions.assertEquals(referenceContentCsv, testContentCsv);
    }
}
