package io.frictionlessdata.tableschema;

import io.frictionlessdata.tableschema.beans.EmployeeBean;
import io.frictionlessdata.tableschema.beans.GrossDomesticProductBean;
import io.frictionlessdata.tableschema.exception.InvalidCastException;
import io.frictionlessdata.tableschema.field.DateField;
import io.frictionlessdata.tableschema.field.GeopointField;
import io.frictionlessdata.tableschema.iterator.BeanIterator;
import io.frictionlessdata.tableschema.schema.BeanSchema;
import io.frictionlessdata.tableschema.schema.Schema;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static io.frictionlessdata.tableschema.TestHelper.getTestDataDirectory;

class TableIteratorTest {
    private static Table validPopulationTable = null;
    private static Table nullValuesPopulationTable = null;
    private static Table invalidPopulationTable = null;

    @BeforeEach
    void setUp() throws Exception {
        File f = new File(getTestDataDirectory(), "schema/population_schema.json");
        Schema validPopulationSchema = null;
        try (FileInputStream fis = new FileInputStream(f)) {
            validPopulationSchema = Schema.fromJson (fis, false);
        }
        File testDataDir = getTestDataDirectory();
        File file = new File("data/population.csv");
        validPopulationTable = new Table(file, testDataDir, validPopulationSchema);
        file = new File("data/population-null-values.csv");
        nullValuesPopulationTable = new Table(file, testDataDir, validPopulationSchema);
        file = new File("data/population-invalid.csv");
        invalidPopulationTable = new Table(file, testDataDir, validPopulationSchema);

    }

    @Test
    void hasNext() throws Exception {
        Assertions.assertTrue(validPopulationTable.iterator().hasNext());
        Assertions.assertTrue(validPopulationTable.iterator(true, true, true, false).hasNext());
        Assertions.assertFalse(new Table("").iterator(true, true, true, false).hasNext());
        Assertions.assertFalse(new Table("").iterator().hasNext());
    }

    @Test
    void remove() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            nullValuesPopulationTable.iterator(true, false, true, false).remove();
        });
    }

    @Test
    void testNextCast() throws Exception {
        // assertThrows(InvalidCastException.class, () -> {
        Iterator iter = nullValuesPopulationTable.iterator(true, false, true, false);
        Map<String, Object> obj =  (Map<String, Object>)iter.next();
        Assertions.assertNull(obj.get("year"));
        obj =  (Map<String, Object>)iter.next();
        Assertions.assertNull(obj.get("year"));

        //});

    }

    @Test
    void testNextInvalidCast() throws Exception {
        Assertions.assertThrows(InvalidCastException.class, () -> {
            invalidPopulationTable.iterator(true, false, true, false).next();
        });

    }
}