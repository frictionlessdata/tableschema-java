package io.frictionlessdata.tableschema.datasourceformats;

import io.frictionlessdata.tableschema.TestHelper;
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

public class DataSourceFormatsTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();


    private String populationCsv = "city,year,population\n" +
            "london,2017,8780000\n" +
            "paris,2017,2240000\n" +
            "rome,2017,2860000";
    private final String[] populationHeaders = new String[]{
            "city", "year", "population"
    };

    @Test
    public void createCsvDataSource() throws Exception{
        String dates = this.getDatesCsvData();
        DataSourceFormat ds = DataSourceFormat.createDataSourceFormat(dates);
        Assert.assertTrue(ds instanceof CsvDataSourceFormat);
    }

    @Test
    public void writeCsvToCsv() throws Exception{
        String dates = this.getDatesCsvData();
        String content = null;

        DataSourceFormat ds = DataSourceFormat.createDataSourceFormat(dates);
        File tempFile = Files.createTempFile("tableschema-", ".csv").toFile();
        ds.writeCsv(tempFile, CSVFormat.RFC4180, ds.getHeaders());
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
       URL u = DataSourceFormatsTest.class.getResource("/fixtures/dates_data.csv");
       Path path = Paths.get(u.toURI());
       Path testPath = path.getParent();
       String maliciousPathName = "/etc/passwd";
       if (runningOnWindowsOperatingSystem()){
           maliciousPathName = "C:/Windows/system.ini";
       }
       Path maliciousPath = new File(maliciousPathName).toPath();
       exception.expect(IllegalArgumentException.class);
       DataSourceFormat.toSecure(maliciousPath, testPath);
    }

    @Test
    public void testUnsafePath2() throws Exception {
        URL u = DataSourceFormatsTest.class.getResource("/fixtures/dates_data.csv");
        Path path = Paths.get(u.toURI());
        Path testPath = path.getParent();
        String maliciousPathName = "/etc/";
        if (runningOnWindowsOperatingSystem()){
            maliciousPathName = "C:/Windows/";
        }
        Path maliciousPath = new File(maliciousPathName).toPath();
        exception.expect(IllegalArgumentException.class);
        DataSourceFormat.toSecure(maliciousPath, testPath);
    }

    @Test
    public void testSafePath() throws Exception {
        URL u = DataSourceFormatsTest.class.getResource("/fixtures/dates_data.csv");
        Path path = Paths.get(u.toURI());
        Path testPath = path.getParent().getParent();
        String maliciousPathName = "fixtures/dates_data.csv";
        if (runningOnWindowsOperatingSystem()){
            maliciousPathName = "fixtures/dates_data.csv";
        }
        Path maliciousPath = new File(maliciousPathName).toPath();
        DataSourceFormat.toSecure(maliciousPath, testPath);
    }

    @Test
    public void testSafePathCreationCsv() throws Exception {
        DataSourceFormat ds = DataSourceFormat.createDataSourceFormat(new File ("data/population.csv"), TestHelper.getTestDataDirectory());
        Assert.assertNotNull(ds);
    }


    @Test
    public void testUrlCreationCsv() throws Exception {
        DataSourceFormat ds = new CsvDataSourceFormat(new URL ("https://raw.githubusercontent.com/frictionlessdata" +
                "/tableschema-java/master/src/test/resources/fixtures/data/population.csv"));
        Assert.assertNotNull(ds);
        List<String[]> data = ds.data();
        Assert.assertEquals(3, data.size());
    }


    @Test
    public void testSafePathInputStreamCreationCsv() throws Exception {
        DataSourceFormat ds;
        File inFile = new File(TestHelper.getTestDataDirectory(), "data/population.csv");
        try (FileInputStream is = new FileInputStream(inFile)) {
            ds = new CsvDataSourceFormat(is);
            List<String[]> data = ds.data();
            Assert.assertNotNull(data);
            byte[] bytes = Files.readAllBytes(new File(TestHelper.getTestDataDirectory(), "data/population.csv").toPath());
            String[] content = new String(bytes).split("[\n\r]+");
            for (int i = 1; i < content.length; i++) {
                String[] testArr = content[i].split(",");
                Assert.assertArrayEquals(testArr, data.get(i-1));
            }
        }
        Assert.assertNotNull(ds);
    }

    @Test
    public void testZipInputFileCreationCsv() throws Exception {
        DataSourceFormat ds;
        File basePath = new File(TestHelper.getTestDataDirectory(),"data/population.zip");
        File inFile = new File("population.csv");
        ds = new CsvDataSourceFormat(inFile,basePath);
        List<String[]> data = ds.data();
        Assert.assertNotNull(data);
        byte[] bytes = Files.readAllBytes(new File(TestHelper.getTestDataDirectory(), "data/population.csv").toPath());
        String[] content = new String(bytes).split("[\n\r]+");
        for (int i = 1; i < content.length; i++) {
            String[] testArr = content[i].split(",");
            Assert.assertArrayEquals(testArr, data.get(i-1));
        }
        Assert.assertNotNull(ds);
    }


    @Test
    public void testWrongInputStreamCreationCsv() throws Exception {
        DataSourceFormat ds;
        File inFile = new File(TestHelper.getTestDataDirectory(), "data/population.json");
        exception.expect(IllegalArgumentException.class);
        try (FileInputStream is = new FileInputStream(inFile)) {
            ds = new CsvDataSourceFormat(is);
        }
        Assert.assertNotNull(ds);
    }

    @Test
    public void writeCsvToFile() throws Exception{
        String content = null;

        DataSourceFormat ds;
        File tempFile = Files.createTempFile("tableschema-", ".csv").toFile();

        File inFile = new File(TestHelper.getTestDataDirectory(), "data/population.csv");
        try (FileInputStream is = new FileInputStream(inFile)) {
            String popCsv = new String(Files.readAllBytes(inFile.toPath()));
            ds = DataSourceFormat.createDataSourceFormat(popCsv);
            Assert.assertArrayEquals(populationHeaders, ds.getHeaders());
        }
        ds.write(tempFile);
        try (FileReader fr = new FileReader(tempFile)) {
            try (BufferedReader rdr = new BufferedReader(fr)) {
                content = rdr.lines().collect(Collectors.joining("\n"));
            }
        }
        // evade the CRLF mess by nuking all CR chars
        Assert.assertEquals(populationCsv.replaceAll("\\r", ""), content.replaceAll("\\r", ""));
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
            URL sourceFileUrl = DataSourceFormatsTest.class.getResource(fileName);
            // Get path of URL
            Path path = Paths.get(sourceFileUrl.toURI());
            return new String(Files.readAllBytes(path));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}

