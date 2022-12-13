package io.frictionlessdata.tableschema.table_tests;

import io.frictionlessdata.tableschema.Table;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import static io.frictionlessdata.tableschema.TestHelper.getTestDataDirectory;
import static io.frictionlessdata.tableschema.TestHelper.getTestsuiteDataDirectory;

public class TableEncodingTests {

    // Test for https://github.com/frictionlessdata/tableschema-java/issues/77
    // currently disabled
    @Test
    @DisplayName("Create a Table from a ISO-8859-1 encoded file")
    void createTableFromIso8859() throws Exception{
        File testDataDir = getTestDataDirectory();

        Table table
                = Table.fromSource(new File("csv/encodings/iso8859.csv"), testDataDir, null, null, StandardCharsets.ISO_8859_1);

        Iterator<Object[]> iter = table.iterator();
        Object[] row = iter.next();
        Assertions.assertEquals("Réunion", row[0]);
    }
}
