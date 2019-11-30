package io.frictionlessdata.tableschema;

import io.frictionlessdata.tableschema.datasources.CsvDataSource;
import io.frictionlessdata.tableschema.datasources.DataSource;
import io.frictionlessdata.tableschema.datasources.JsonArrayDataSource;
import org.apache.commons.csv.CSVFormat;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class DataSourceTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();
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

    private String populationCsv = "city,year,population\n" +
            "london,2017,8780000\n" +
            "paris,2017,2240000\n" +
            "rome,2017,2860000";
    private final String[] populationHeaders = new String[]{
            "city", "year", "population"
    };


    @Test
    public void testCreateJsonArrayDataSource() throws Exception{
        DataSource ds = DataSource.createDataSource(populationJson);
        Assert.assertTrue(ds instanceof JsonArrayDataSource);
    }

    @Test
    public void testJsonArrayDataSourceHeaders() throws Exception{
        DataSource ds = DataSource.createDataSource(populationJson);
        String[] headers = ds.getHeaders();
        Assert.assertArrayEquals(populationHeaders, headers);
    }

    @Test
    public void createCsvDataSource() throws Exception{
        String dates = this.getDatesCsvData();
        DataSource ds = DataSource.createDataSource(dates);
        Assert.assertTrue(ds instanceof CsvDataSource);
    }

    @Test
    public void writeCsvToCsv() throws Exception{
        String dates = this.getDatesCsvData();
        String content = null;

        DataSource ds = DataSource.createDataSource(dates);
        File tempFile = Files.createTempFile("tableschema-", ".csv").toFile();
        ds.writeCsv(tempFile, CSVFormat.RFC4180);
        try (FileReader fr = new FileReader(tempFile)) {
            try (BufferedReader rdr = new BufferedReader(fr)) {
                content = rdr.lines().collect(Collectors.joining("\n"));
            }
        }
        // evade the CRLF mess by nuking all CR chars
        Assert.assertEquals(dates.replaceAll("\\r", ""), content.replaceAll("\\r", ""));
    }

    @Test
    public void testUnsafePath1() throws Exception {
       URL u = DataSourceTest.class.getResource("/fixtures/dates_data.csv");
       Path path = Paths.get(u.toURI());
       Path testPath = path.getParent();
       String maliciousPathName = "/etc/passwd";
       if (runningOnWindowsOperatingSystem()){
           maliciousPathName = "C:/Windows/system.ini";
       }
       Path maliciousPath = new File(maliciousPathName).toPath();
       exception.expect(IllegalArgumentException.class);
       DataSource.toSecure(maliciousPath, testPath);
    }

    @Test
    public void testUnsafePath2() throws Exception {
        URL u = DataSourceTest.class.getResource("/fixtures/dates_data.csv");
        Path path = Paths.get(u.toURI());
        Path testPath = path.getParent();
        String maliciousPathName = "/etc/";
        if (runningOnWindowsOperatingSystem()){
            maliciousPathName = "C:/Windows/";
        }
        Path maliciousPath = new File(maliciousPathName).toPath();
        exception.expect(IllegalArgumentException.class);
        DataSource.toSecure(maliciousPath, testPath);
    }

    @Test
    public void testSafePath() throws Exception {
        URL u = DataSourceTest.class.getResource("/fixtures/dates_data.csv");
        Path path = Paths.get(u.toURI());
        Path testPath = path.getParent().getParent();
        String maliciousPathName = "fixtures/dates_data.csv";
        if (runningOnWindowsOperatingSystem()){
            maliciousPathName = "fixtures/dates_data.csv";
        }
        Path maliciousPath = new File(maliciousPathName).toPath();
        DataSource.toSecure(maliciousPath, testPath);
    }

    @Test
    public void testSafePathCreationCsv() throws Exception {
        DataSource ds = DataSource.createDataSource(new File ("data/population.csv"), TestHelper.getTestDataDirectory());
        Assert.assertNotNull(ds);
    }

    @Test
    public void testSafePathCreationJson() throws Exception {
        DataSource ds = DataSource.createDataSource(new File ("simple_geojson.json"), TestHelper.getTestDataDirectory());
        Assert.assertNotNull(ds);
    }

    @Test
    public void testSafePathInputStreamCreationCsv() throws Exception {
        DataSource ds;
        File inFile = new File(TestHelper.getTestDataDirectory(), "data/population.csv");
        try (FileInputStream is = new FileInputStream(inFile)) {
            ds = new CsvDataSource(is);
            List<String[]> data = ds.data();
            Assert.assertNotNull(data);
            byte[] bytes = Files.readAllBytes(new File(TestHelper.getTestDataDirectory(), "data/population.csv").toPath());
            String[] content = new String(bytes).split("\n");
            for (int i = 1; i < content.length; i++) {
                String[] testArr = content[i].split(",");
                Assert.assertArrayEquals(testArr, data.get(i-1));
            }
        }
        Assert.assertNotNull(ds);
    }


    @Test
    public void testSafePathInputStreamCreationJson() throws Exception {
        DataSource ds;
        File inFile = new File(TestHelper.getTestDataDirectory(), "data/population.json");
        try (FileInputStream is = new FileInputStream(inFile)) {
            ds = new JsonArrayDataSource(is);
            Assert.assertArrayEquals(populationHeaders, ds.getHeaders());
        }
        Assert.assertNotNull(ds);
    }

    @Test
    public void testWrongInputStreamCreationJson() throws Exception {
        DataSource ds;
        File inFile = new File(TestHelper.getTestDataDirectory(), "data/population.csv");
        exception.expect(JSONException.class);
        try (FileInputStream is = new FileInputStream(inFile)) {
            ds = new JsonArrayDataSource(is);
            Assert.assertArrayEquals(populationHeaders, ds.getHeaders());
        }
        Assert.assertNotNull(ds);
    }

    @Test
    public void testWrongInputStreamCreationCsv() throws Exception {
        DataSource ds;
        File inFile = new File(TestHelper.getTestDataDirectory(), "data/population.json");
        exception.expect(IllegalArgumentException.class);
        try (FileInputStream is = new FileInputStream(inFile)) {
            ds = new CsvDataSource(is);
        }
        Assert.assertNotNull(ds);
    }

    @Test
    public void testSafeStreamCreationJson() throws Exception {
        DataSource ds = DataSource.createDataSource(new File ("data/population.json"), TestHelper.getTestDataDirectory());
        Assert.assertNotNull(ds);
    }

    @Test
    public void writeCsvToFile() throws Exception{
        String content = null;

        DataSource ds;
        File tempFile = Files.createTempFile("tableschema-", ".json").toFile();

        File inFile = new File(TestHelper.getTestDataDirectory(), "data/population.csv");
        try (FileInputStream is = new FileInputStream(inFile)) {
            String popCsv = new String(Files.readAllBytes(inFile.toPath()));
            ds = DataSource.createDataSource(popCsv);
            Assert.assertArrayEquals(populationHeaders, ds.getHeaders());
        }
        System.out.println(tempFile.getAbsolutePath());
        ds.write(tempFile);
        try (FileReader fr = new FileReader(tempFile)) {
            try (BufferedReader rdr = new BufferedReader(fr)) {
                content = rdr.lines().collect(Collectors.joining("\n"));
            }
        }
        // evade the CRLF mess by nuking all CR chars
        Assert.assertEquals(populationCsv.replaceAll("\\r", ""), content.replaceAll("\\r", ""));
    }


    @Test
    public void writeJsonToFile() throws Exception{
        String content = null;
        String popCsv;

        File inFile = new File(TestHelper.getTestDataDirectory(), "data/population.json");
        popCsv = new String(Files.readAllBytes(inFile.toPath()));
        DataSource ds = DataSource.createDataSource(popCsv);

        File tempFile = Files.createTempFile("tableschema-", ".json").toFile();
        ds.write(tempFile);
        try (FileReader fr = new FileReader(tempFile)) {
            try (BufferedReader rdr = new BufferedReader(fr)) {
                content = rdr.lines().collect(Collectors.joining("\n"));
            }
        }
        // evade the CRLF mess by nuking all CR chars
        Assert.assertEquals(content.replaceAll("[\\r\\n ]", ""), popCsv.replaceAll("[\\r\\n ]", ""));
    }

    private static boolean runningOnWindowsOperatingSystem() {
        String os = System.getProperty("os.name");
        return (os.toLowerCase().contains("windows"));
    }

    private String getDatesCsvData() {
        return getFileContents("/fixtures/dates_data.csv");
    }

    private static String getFileContents(String fileName) {
        try {
            // Create file-URL of source file:
            URL sourceFileUrl = DataSourceTest.class.getResource(fileName);
            // Get path of URL
            Path path = Paths.get(sourceFileUrl.toURI());
            return new String(Files.readAllBytes(path));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}

