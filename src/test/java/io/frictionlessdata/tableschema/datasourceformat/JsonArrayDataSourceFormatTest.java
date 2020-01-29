package io.frictionlessdata.tableschema.datasourceformat;

import io.frictionlessdata.tableschema.TestHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.util.stream.Collectors;

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

    @Test
    @DisplayName("Validate creating and writing a JsonArrayDataSourceFormat from JSON with null entries")
    void testCreateAndWriteJsonArrayDataSourceWithMissingEntries() throws Exception {
        DataSourceFormat ds = DataSourceFormat.createDataSourceFormat(populationJsonMissingEntry);

        File tempFile = Files.createTempFile("tableschema-", ".json").toFile();
        try (FileWriter wr = new FileWriter(tempFile);
                BufferedWriter bwr = new BufferedWriter(wr)) {
            ds.writeCsv(bwr, null, populationHeaders);
        }
        System.out.println(tempFile.getAbsolutePath());
    }

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
    void writeJsonToFile() throws Exception{
        String content = null;
        String popCsv;

        File inFile = new File(TestHelper.getTestDataDirectory(), "data/population.json");
        popCsv = new String(Files.readAllBytes(inFile.toPath()));
        DataSourceFormat ds = DataSourceFormat.createDataSourceFormat(popCsv);

        File tempFile = Files.createTempFile("tableschema-", ".json").toFile();
        ds.write(tempFile);
        try (FileReader fr = new FileReader(tempFile)) {
            try (BufferedReader rdr = new BufferedReader(fr)) {
                content = rdr.lines().collect(Collectors.joining("\n"));
            }
        }
        // evade the CRLF mess by nuking all CR chars
        Assertions.assertEquals(content.replaceAll("[\\r\\n ]", ""),
                popCsv.replaceAll("[\\r\\n ]", ""));
    }
}
