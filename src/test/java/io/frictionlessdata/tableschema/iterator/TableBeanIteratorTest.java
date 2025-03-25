package io.frictionlessdata.tableschema.iterator;

import com.google.common.util.concurrent.AtomicDouble;
import io.frictionlessdata.tableschema.Table;
import io.frictionlessdata.tableschema.beans.EmployeeBean;
import io.frictionlessdata.tableschema.beans.EmployeeBeanWithAnnotation;
import io.frictionlessdata.tableschema.beans.GrossDomesticProductBean;
import io.frictionlessdata.tableschema.beans.NumbersBean;
import io.frictionlessdata.tableschema.tabledatasource.TableDataSource;
import io.frictionlessdata.tableschema.field.DateField;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static io.frictionlessdata.tableschema.TestHelper.getTestDataDirectory;

class TableBeanIteratorTest {
    private static Schema gdpSchema = null;
    private static Table employeeTable = null;
    private static Table employeeTableAlternateDateFormat = null;
    private static Table gdpTable = null;
    private static File testDataDir;

    @BeforeEach
    void setUp() throws Exception {
        testDataDir = getTestDataDirectory();

    }


    @Test
    @DisplayName("Test deserialization of EmployeeBean")
    void testBeanDeserialization() throws Exception {

        File file = new File("data/employee_data.csv");
        employeeTable = Table.fromSource(file, testDataDir, null, TableDataSource.getDefaultCsvFormat());

        List<EmployeeBean> employees = new ArrayList<>();
        BeanIterator<EmployeeBean> bit = new BeanIterator<>(employeeTable, EmployeeBean.class, false);
        while (bit.hasNext()) {
            EmployeeBean employee = bit.next();
            employees.add(employee);
        }
        Assertions.assertEquals(3, employees.size());
        EmployeeBean frank = employees.get(1);
        Assertions.assertEquals("Frank McKrank", frank.getName());
        Assertions.assertEquals("1992-02-14", new DateField("date").formatValueAsString(frank.getDateOfBirth(), null, null));
        Assertions.assertFalse(frank.getAdmin());
        Assertions.assertEquals("(90.0, 45.223, NaN)", frank.getAddressCoordinates().toString());
        Assertions.assertEquals("PT15M", frank.getContractLength().toString());
        Map info = frank.getInfo();
        Assertions.assertEquals(45, info.get("pin"));
        Assertions.assertEquals(83.23, info.get("rate"));
        Assertions.assertEquals(90, info.get("ssn"));
    }

    @Test
    @DisplayName("Test deserialization of EmployeeBean with Annotation")
    void testBeanDeserialization2() throws Exception {
        File inFile = new File("data/employee_full.csv");
        Table employeeTableAlternateDateFormat = Table.fromSource(
                inFile,
                testDataDir,
                null,
                TableDataSource.getDefaultCsvFormat());
        List<EmployeeBeanWithAnnotation> employees = new ArrayList<>();
        BeanIterator<EmployeeBeanWithAnnotation> bit = new BeanIterator<>(employeeTableAlternateDateFormat, EmployeeBeanWithAnnotation.class, false);
        while (bit.hasNext()) {
            EmployeeBeanWithAnnotation employee = bit.next();
            employees.add(employee);
        }
        Assertions.assertEquals(3, employees.size());
        EmployeeBeanWithAnnotation frank = employees.get(1);
        Assertions.assertEquals("McKrank", frank.getLastName());
        Assertions.assertEquals("1992-02-14", new DateField("date").formatValueAsString(frank.getDateOfBirth(), null, null));
        Assertions.assertFalse(frank.getManager());
        Assertions.assertEquals("(-91.254898, 35.6087, NaN)", frank.getAddressCoordinates().toString());

        List<String> interests = frank.getInterests();
        Assertions.assertEquals(2, interests.size());
        Assertions.assertEquals("sports", interests.get(0));
        Assertions.assertEquals("reading", interests.get(1));
    }

    @Test
    @DisplayName("Test deserialization of big floats (GrossDomesticProductBean)")
    void testBeanDeserialization3() throws Exception {
        File file = new File("data/gdp.csv");
        gdpTable = Table.fromSource(file, testDataDir, null, TableDataSource.getDefaultCsvFormat());
        List<GrossDomesticProductBean> records = new ArrayList<>();
        BeanIterator<GrossDomesticProductBean> bit
                = new BeanIterator<>(gdpTable, GrossDomesticProductBean.class, false);

        while (bit.hasNext()) {
            GrossDomesticProductBean record = bit.next();
            records.add(record);
        }
        Assertions.assertEquals(11507, records.size());
    }

    @Test
    @DisplayName("Test deserialization of various numbers")
    void testBeanDeserialization4() throws Exception {
        NumbersBean bn = new NumbersBean();
        bn.setAtomicIntegerVal(new AtomicInteger(2345123));
        bn.setBigDecimalVal(new BigDecimal("3542352304245234542345345423453.02345234"));
        bn.setBigIntVal(new BigInteger("23459734123456676123981234"));
        bn.setByteVal(Byte.parseByte("126"));
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
                = Table.fromSource(dataFile, getTestDataDirectory(), schema, TableDataSource.getDefaultCsvFormat());
        BeanIterator<NumbersBean> bit = (BeanIterator<NumbersBean>) numbersTable.iterator(NumbersBean.class, false);

        NumbersBean record = bit.next();
        Assertions.assertEquals(bn, record);
    }

}