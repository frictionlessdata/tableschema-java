package io.frictionlessdata.tableschema.tabledatasource;

import io.frictionlessdata.tableschema.Table;
import io.frictionlessdata.tableschema.TestHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class TableDataSourceFormatsTest {

    private String populationCsv = "city,year,population\n" +
            "london,2017,8780000\n" +
            "paris,2017,2240000\n" +
            "rome,2017,2860000";
    private final String[] populationHeaders = new String[]{
            "city", "year", "population"
    };

    private final static String populationjson = "[\n" +
            "  {\n" +
            "    \"city\": \"london\",\n" +
            "    \"year\": \"2017\",\n" +
            "    \"population\": 8780000\n" +
            "  },\n" +
            "  {\n" +
            "    \"city\": \"paris\",\n" +
            "    \"year\": \"2017\",\n" +
            "    \"population\": 2240000\n" +
            "  },\n" +
            "  {\n" +
            "    \"city\": \"rome\",\n" +
            "    \"year\": \"2017\",\n" +
            "    \"population\": 2860000\n" +
            "  }\n" +
            "]";

    @Test
    @DisplayName("Create a TableDataSource from CSV String data and ensure it is a CsvTableDataSource")
    public void createCsvDataSource() throws Exception{
        String dates = this.getDatesCsvData();
        TableDataSource ds = TableDataSource.fromSource(dates);
        Assertions.assertTrue(ds instanceof CsvTableDataSource);
    }

    @Test
    @DisplayName("Create a TableDataSource from an unsafe path and ensure an exception is thrown")
    public void testUnsafePath1() throws Exception {
       URL u = TableDataSourceFormatsTest.class.getResource("/fixtures/dates_data.csv");
       Path path = Paths.get(u.toURI());
       Path testPath = path.getParent();
       String maliciousPathName = "/etc/passwd";
       if (runningOnWindowsOperatingSystem()){
           maliciousPathName = "C:/Windows/system.ini";
       }
       Path maliciousPath = new File(maliciousPathName).toPath();

       assertThrows(IllegalArgumentException.class, () -> {
           TableDataSource.toSecure(maliciousPath, testPath);});
    }

    @Test
    @DisplayName("Create a TableDataSource from a safe path and ensure no exception is thrown")
    public void testSafePath() throws Exception {
        URL u = TableDataSourceFormatsTest.class.getResource("/fixtures/dates_data.csv");
        Path path = Paths.get(u.toURI());
        Path testPath = path.getParent().getParent();
        String maliciousPathName = "fixtures/dates_data.csv";
        if (runningOnWindowsOperatingSystem()){
            maliciousPathName = "fixtures/dates_data.csv";
        }
        Path maliciousPath = new File(maliciousPathName).toPath();
        TableDataSource.toSecure(maliciousPath, testPath);
    }

    @Test
    @DisplayName("Create a TableDataSource from a safe path and ensure no exception is thrown")
    public void testSafePathCreationCsv() throws Exception {
        TableDataSource ds = TableDataSource.fromSource(new File ("data/population.csv"), TestHelper.getTestDataDirectory(), Charset.defaultCharset());
        Assertions.assertNotNull(ds);
    }


    @Test
    @DisplayName("Create a CsvTableDataSource from an URL containing CSV String data and ensure the data size is 3")
    public void testUrlCreationCsv() throws Exception {
        TableDataSource ds = TableDataSource.fromSource(new URL("https://raw.githubusercontent.com/frictionlessdata" +
                "/tableschema-java/master/src/test/resources/fixtures/data/population.csv"), StandardCharsets.UTF_8);
        Assertions.assertNotNull(ds);
        List<String[]> data = ds.getDataAsStringArray();
        Assertions.assertEquals(3, data.size());
    }


    @Test
    @DisplayName("Create a CsvTableDataSource from InputStream and validate content matches")
    public void testSafePathInputStreamCreationCsv() throws Exception {
        TableDataSource ds;
        File inFile = new File(TestHelper.getTestDataDirectory(), "data/population.csv");
        try (FileInputStream is = new FileInputStream(inFile)) {
            ds = TableDataSource.fromSource(is, StandardCharsets.UTF_8);
            List<String[]> data = ds.getDataAsStringArray();
            Assertions.assertNotNull(data);
            byte[] bytes = Files.readAllBytes(new File(TestHelper.getTestDataDirectory(), "data/population.csv").toPath());
            String[] content = new String(bytes).split("[\n\r]+");
            for (int i = 1; i < content.length; i++) {
                String[] testArr = content[i].split(",");
                Assertions.assertArrayEquals(testArr, data.get(i-1));
            }
        }
        Assertions.assertNotNull(ds);
    }

    @Test
    @DisplayName("Create a CsvTableDataSource from File and validate content matches")
    public void testInputFileCreationCsv() throws Exception {
        TableDataSource ds;
        String refContent = TestHelper.getResourceFileContent( "fixtures/data/population.csv");
        ds = new CsvTableDataSource(refContent);
        List<String[]> data = ds.getDataAsStringArray();
        Assertions.assertNotNull(data);
        byte[] bytes = Files.readAllBytes(new File(TestHelper.getTestDataDirectory(), "data/population.csv").toPath());
        String[] content = new String(bytes).split("[\n\r]+");
        for (int i = 1; i < content.length; i++) {
            String[] testArr = content[i].split(",");
            Assertions.assertArrayEquals(testArr, data.get(i-1));
        }
        Assertions.assertNotNull(ds);
    }

    @Test
    @DisplayName("Create a CsvTableDataSource from zipped File and validate content matches")
    public void testZipInputFileCreationCsv() throws Exception {
        TableDataSource ds;
        File basePath = new File(TestHelper.getTestDataDirectory(),"data/population.zip");
        File inFile = new File("population.csv");
        ds = TableDataSource.fromSource(inFile, basePath, TableDataSource.getDefaultEncoding());
        List<String[]> data = ds.getDataAsStringArray();
        Assertions.assertNotNull(data);
        List<String> lines = Files.readAllLines(new File(TestHelper.getTestDataDirectory(), "data/population.csv").toPath());
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            String[] testArr = line.split(",");
            Assertions.assertArrayEquals(testArr, data.get(i-1));
        }
        Assertions.assertNotNull(ds);
    }


    @Test
    @DisplayName("Create a JsonArrayTableDataSource from JSON data")
    public void testWrongInputStreamCreationCsv() throws Exception {
        final TableDataSource ds;
        File inFile = new File(TestHelper.getTestDataDirectory(), "data/population.json");

        FileInputStream is = new FileInputStream(inFile);
        ds = TableDataSource.fromSource(is, StandardCharsets.UTF_8);
        is.close();

        Assertions.assertTrue(ds instanceof JsonArrayTableDataSource);
    }

    @Test
    @DisplayName("Create a CsvTableDataSource from CSV data write it back to file and ensure content matches")
    public void writeCsvToFile() throws Exception{
        File tempFile = Files.createTempFile("tableschema-", ".csv").toFile();

        File inFile = new File(TestHelper.getTestDataDirectory(), "data/population.csv");
        String popCsv = new String(Files.readAllBytes(inFile.toPath()));
        Table ds = Table.fromSource(popCsv);
        Assertions.assertArrayEquals(populationHeaders, ds.getHeaders());
        try (FileWriter fr = new FileWriter(tempFile)) {
            ds.write(fr, TableDataSource.Format.FORMAT_CSV);
        }
        String content;
        try (FileReader fr = new FileReader(tempFile)) {
            try (BufferedReader rdr = new BufferedReader(fr)) {
                content = rdr.lines().collect(Collectors.joining("\n"));
            }
        }
        // evade the CRLF mess by nuking all CR chars
        Assertions.assertEquals(populationCsv.replaceAll("\\r", ""), content.replaceAll("\\r", ""));
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
            URL sourceFileUrl = TableDataSourceFormatsTest.class.getResource(fileName);
            // Get path of URL
            Path path = Paths.get(sourceFileUrl.toURI());
            return new String(Files.readAllBytes(path));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}

