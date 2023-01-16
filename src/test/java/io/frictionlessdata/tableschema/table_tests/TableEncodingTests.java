package io.frictionlessdata.tableschema.table_tests;

import io.frictionlessdata.tableschema.Table;
import io.frictionlessdata.tableschema.schema.Schema;
import io.frictionlessdata.tableschema.tabledatasource.CsvTableDataSource;
import io.frictionlessdata.tableschema.tabledatasource.TableDataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import static io.frictionlessdata.tableschema.TestHelper.getTestDataDirectory;

public class TableEncodingTests {

    // Test for https://github.com/frictionlessdata/tableschema-java/issues/77
    @Test
    @DisplayName("Create a Table from an ISO-8859-1 encoded file")
    void createTableFromIso8859() throws Exception{
        File testDataDir = getTestDataDirectory();

        Table table = Table.fromSource(
                new File("csv/encodings/iso8859.csv"),
                testDataDir,
                null,
                null,
                StandardCharsets.ISO_8859_1);

        Iterator<Object[]> iter = table.iterator();
        Object[] row = iter.next();
        Assertions.assertEquals("Réunion", row[0]);
    }

    // Test for https://github.com/frictionlessdata/tableschema-java/issues/77
    @Test
    @DisplayName("Create a Table from an ISO-8859-1 encoded URL")
    void createTableFromIso8859Url() throws Exception{
        Table table = Table.fromSource(
                new URL("https://raw.githubusercontent.com/frictionlessdata" +
                        "/tableschema-java/master/src/test/resources/fixtures/csv/encodings/iso8859.csv"),
                (Schema)null,
                null,
                StandardCharsets.ISO_8859_1);

        Iterator<Object[]> iter = table.iterator();
        Object[] row = iter.next();
        Assertions.assertEquals("Réunion", row[0]);
    }
}
