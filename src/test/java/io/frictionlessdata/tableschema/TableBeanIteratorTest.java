package io.frictionlessdata.tableschema;

import com.google.common.util.concurrent.AtomicDouble;
import io.frictionlessdata.tableschema.beans.EmployeeBean;
import io.frictionlessdata.tableschema.beans.GrossDomesticProductBean;
import io.frictionlessdata.tableschema.beans.NumbersBean;
import io.frictionlessdata.tableschema.datasourceformats.DataSourceFormat;
import io.frictionlessdata.tableschema.exception.InvalidCastException;
import io.frictionlessdata.tableschema.field.DateField;
import io.frictionlessdata.tableschema.iterator.BeanIterator;
import io.frictionlessdata.tableschema.schema.BeanSchema;
import io.frictionlessdata.tableschema.schema.Schema;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static io.frictionlessdata.tableschema.TestHelper.getTestDataDirectory;

class TableBeanIteratorTest {
    private static Schema employeeSchema = null;
    private static Schema gdpSchema = null;
    private static Table validPopulationTable = null;
    private static Table nullValuesPopulationTable = null;
    private static Table invalidPopulationTable = null;
    private static Table employeeTable = null;
    private static Table gdpTable = null;

    @BeforeEach
    void setUp() throws Exception {
        File f = new File(getTestDataDirectory(), "schema/population_schema.json");
        Schema validPopulationSchema = null;
        try (FileInputStream fis = new FileInputStream(f)) {
            validPopulationSchema = Schema.fromJson (fis, false);
        }
        File testDataDir = getTestDataDirectory();
        File file = new File("data/population.csv");
        validPopulationTable
                = new Table(file, testDataDir, validPopulationSchema, DataSourceFormat.getDefaultCsvFormat());
        file = new File("data/population-null-values.csv");
        nullValuesPopulationTable
                = new Table(file, testDataDir, validPopulationSchema, DataSourceFormat.getDefaultCsvFormat());
        file = new File("data/population-invalid.csv");
        invalidPopulationTable
                = new Table(file, testDataDir, validPopulationSchema, DataSourceFormat.getDefaultCsvFormat());

        file = new File("data/employee_data.csv");
        employeeSchema = BeanSchema.infer(EmployeeBean.class);
        employeeTable = new Table(file, testDataDir, employeeSchema, DataSourceFormat.getDefaultCsvFormat());

        file = new File("data/gdp.csv");
        gdpSchema = BeanSchema.infer(GrossDomesticProductBean.class);
        gdpTable = new Table(file, testDataDir, gdpSchema, DataSourceFormat.getDefaultCsvFormat());

    }


    @Test
    @DisplayName("Test deserialization of EmployeeBean")
    void testBeanDeserialization() throws Exception {
        List<EmployeeBean> employees = new ArrayList<>();
        BeanIterator<EmployeeBean> bit = new BeanIterator<>(employeeTable, EmployeeBean.class);
        while (bit.hasNext()) {
            EmployeeBean employee = bit.next();
            employees.add(employee);
        }
        Assertions.assertEquals(3, employees.size());
        EmployeeBean frank = employees.get(1);
        Assertions.assertEquals("Frank McKrank", frank.getName());
        Assertions.assertEquals("1992-02-14", new DateField("date").formatValue(frank.getDateOfBirth(), null, null));
        Assertions.assertFalse(frank.getAdmin());
        Assertions.assertEquals("(90.0, 45.0, NaN)", frank.getAddressCoordinates().toString());
        Assertions.assertEquals("PT15M", frank.getContractLength().toString());
        Assertions.assertEquals("{\"pin\":45,\"rate\":83.23,\"ssn\":90}", frank.getInfo().toString());
    }

    @Test
    @DisplayName("Test deserialization of big floats (GrossDomesticProductBean)")
    void testBeanDeserialization2() throws Exception {
        List<GrossDomesticProductBean> records = new ArrayList<>();
        BeanIterator<GrossDomesticProductBean> bit = new BeanIterator<>(gdpTable, GrossDomesticProductBean.class);

        while (bit.hasNext()) {
            GrossDomesticProductBean record = bit.next();
            records.add(record);
        }
        Assertions.assertEquals(11507, records.size());
    }

    @Test
    @DisplayName("Test deserialization of various numbers")
    void testBeanDeserialization3() throws Exception {
        NumbersBean bn = new NumbersBean();
        bn.setAtomicIntegerVal(new AtomicInteger(2345123));
        bn.setBigDecimalVal(new BigDecimal("3542352304245234542345345423453.02345234"));
        bn.setBigIntVal(new BigInteger("23459734123456676123981234"));
        bn.setByteVal(new Byte("126"));
        bn.setId(23143245);
        bn.setLongVal(893479850249L);
        bn.setLongClassVal(908347392304952L);
        bn.setIntVal(234534);
        bn.setShortVal((short)234);
        bn.setFloatVal(3245.1234f);
        bn.setDoubleVal(345234552345.2345);
        bn.setDoubleClassVal(3.4567347437347346E23);
        bn.setFloatClassVal(2.34566246E9f);
        bn.setAtomicLongVal(new AtomicLong(234597341234502345L));
        bn.setAtomicDoubleVal(new AtomicDouble(3453254.34));

        Schema schema = null;
        File f = new File(getTestDataDirectory(), "schema/number_types_schema.json");
        try (FileInputStream fis = new FileInputStream(f)) {
            schema = Schema.fromJson (fis, false);
        }
        File dataFile = new File("data/number_types.csv");
        Table numbersTable
                = new Table(dataFile, getTestDataDirectory(), schema, DataSourceFormat.getDefaultCsvFormat());
        BeanIterator<NumbersBean> bit = new BeanIterator<>(numbersTable, NumbersBean.class);

        NumbersBean record = bit.next();
        Assertions.assertEquals(bn, record);
    }

}