package io.frictionlessdata.tableschema.table_tests;

import io.frictionlessdata.tableschema.Table;
import io.frictionlessdata.tableschema.exception.TableIOException;
import io.frictionlessdata.tableschema.inputstream.ByteOrderMarkStrippingInputStream;
import io.frictionlessdata.tableschema.schema.Schema;
import org.apache.commons.csv.CSVFormat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import static io.frictionlessdata.tableschema.TestHelper.getTestDataDirectory;

public class TableBOMTests {
    private CSVFormat csvFormat = CSVFormat
            .TDF
            .builder()
            .setRecordSeparator("\n")
            .setHeader(new String[0])
            .get();


    @Test
    @DisplayName("Create a Table from CSV File with BOM with Schema from Stream and with default CSVFormat")
    public void testReadFileWithBOMAndSchema() throws Exception{
        File testDataDir = getTestDataDirectory();
        // get path of test CSV file
        File file = new File("data/simple_data_bom2.tsv");
        Table table = Table.fromSource(file, testDataDir);
        table.setCsvFormat(csvFormat);
        File f = new File(getTestDataDirectory(), "schema/simple_data_schema.json");
        Schema schema = null;
        try (FileInputStream fis = new FileInputStream(f)) {
            schema = Schema.fromJson (fis, false);
        }
        // must not throw an exception
        table.setSchema(schema);
    }

    @Test
    @DisplayName("Create a Table from CSV File with BOM with Schema from Stream and with default CSVFormat")
    public void testReadFileWithBOMAndSchemafromInputStream() throws Exception{
        File testDataDir = getTestDataDirectory();
        // get path of test CSV file
        InputStream inputStream = TableBOMTests.class.getClassLoader().getResourceAsStream("fixtures/data/simple_data_bom2.tsv");
        File f = new File(getTestDataDirectory(), "schema/simple_data_schema.json");
        FileInputStream schemaIs = new FileInputStream(f);

        Table table = Table.fromSource(inputStream, schemaIs, csvFormat);
        table.validate();
    }

    @Test
    @DisplayName("Create a Table from CSV File with BOM without a Schema from Stream " +
            "and with custom CSVFormat")
    public void testReadFileWithBOM() throws Exception{
        File testDataDir = getTestDataDirectory();
        // get path of test CSV file
        File file = new File("data/data_bom.tsv");
        Table table = Table.fromSource(file, testDataDir);

        table.setCsvFormat(csvFormat);
        Assertions.assertEquals(3, table.read().size());
        // must not throw an exception
        table.validate();
    }

    @Test
    @DisplayName("Create a Table from CSV String data with BOM with Schema from Stream and with tab-delimited CSVFormat")
    @Disabled
    public void testReadStringWithBOMAndSchema() throws Exception{
        // get path of test CSV file
        File file = new File(getTestDataDirectory(),"data/data_bom.tsv");
        StringBuilder json  = new StringBuilder();
        try (InputStream is = new FileInputStream(file)) {
            int r = is.read();
            while (r != -1) {
                json.append((char)r);
                r = is.read();
            }
        }
        String input = json.toString();
        byte[] bytes = new byte[input.length()];
        input.getBytes(0, input.length(), bytes, 0);
        Charset charset = StandardCharsets.UTF_16;
        String content;

        try (ByteOrderMarkStrippingInputStream bims  = new ByteOrderMarkStrippingInputStream(new ByteArrayInputStream(bytes));
             InputStreamReader isr = new InputStreamReader(bims.skipBOM(), charset == null ? bims.getCharset() : charset);
             BufferedReader rdr = new BufferedReader(isr)) {
            content = rdr.lines().collect(Collectors.joining("\n"));
        } catch (IOException ex) {
            throw new TableIOException(ex);
        }

        Table table = Table.fromSource(json.toString(), null, csvFormat);
        File f = new File(getTestDataDirectory(), "schema/simple_data_schema.json");
        Schema schema = null;
        try (FileInputStream fis = new FileInputStream(f)) {
            schema = Schema.fromJson (fis, false);
        }
        // must not throw an exception
        table.setSchema(schema);
    }

}
