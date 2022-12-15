package io.frictionlessdata.tableschema.tabledatasource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.frictionlessdata.tableschema.Table;
import io.frictionlessdata.tableschema.beans.EmployeeBeanWithAnnotation;
import io.frictionlessdata.tableschema.iterator.BeanIterator;
import io.frictionlessdata.tableschema.schema.BeanSchema;
import io.frictionlessdata.tableschema.schema.Schema;
import io.frictionlessdata.tableschema.util.JsonUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static io.frictionlessdata.tableschema.TestHelper.getTestDataDirectory;

public class BeanTableDataSourceTest {
    @Test
    @DisplayName("Test deserialization of EmployeeBean with Annotation")
    void testBeanDeserialization2() throws Exception {
        Collection<EmployeeBeanWithAnnotation> employees = getEmployees();

        BeanTableDataSource<EmployeeBeanWithAnnotation> source
                = new BeanTableDataSource<>(employees, EmployeeBeanWithAnnotation.class);
        Table t = Table.fromSource(source);
        List<EmployeeBeanWithAnnotation> employees2 = new ArrayList<>();
        BeanIterator<EmployeeBeanWithAnnotation> bit2 = new BeanIterator<>(t, EmployeeBeanWithAnnotation.class, false);
        while (bit2.hasNext()) {
            EmployeeBeanWithAnnotation employee = bit2.next();
            employees2.add(employee);
        }
        ArrayNode arrayNode = JsonUtil.getInstance().createArrayNode(employees);
        ArrayNode arrayNode2 = JsonUtil.getInstance().createArrayNode(employees2);
        Assertions.assertEquals(arrayNode, arrayNode2);

        t.iterator(false, false, false, false).forEachRemaining((row)-> {System.out.println();});
    }

    @Test
    @DisplayName("Test Table creation from EmployeeBean instances with Annotation")
    void testBeanTableCreation() throws Exception {
        Collection<EmployeeBeanWithAnnotation> employees = getEmployees();

        Table t = new Table(employees, EmployeeBeanWithAnnotation.class);
        List<EmployeeBeanWithAnnotation> employees2 = new ArrayList<>();
        BeanIterator<EmployeeBeanWithAnnotation> bit2 = new BeanIterator<>(t, EmployeeBeanWithAnnotation.class, false);
        while (bit2.hasNext()) {
            EmployeeBeanWithAnnotation employee = bit2.next();
            employees2.add(employee);
        }
        ArrayNode arrayNode = JsonUtil.getInstance().createArrayNode(employees);
        ArrayNode arrayNode2 = JsonUtil.getInstance().createArrayNode(employees2);
        Assertions.assertEquals(arrayNode, arrayNode2);
    }

    private static Collection<EmployeeBeanWithAnnotation> getEmployees() throws Exception {
        File testDataDir = getTestDataDirectory();
        File inFile = new File("data/employee_full.csv");
        Schema schema = BeanSchema.infer(EmployeeBeanWithAnnotation.class);
        Table inTable = Table.fromSource(
                inFile,
                testDataDir,
                schema,
                TableDataSource.getDefaultCsvFormat(),
                TableDataSource.getDefaultEncoding());

        List<EmployeeBeanWithAnnotation> employees = new ArrayList<>();
        BeanIterator<EmployeeBeanWithAnnotation> bit = new BeanIterator<>(inTable, EmployeeBeanWithAnnotation.class, false);
        while (bit.hasNext()) {
            EmployeeBeanWithAnnotation employee = bit.next();
            employees.add(employee);
        }
        return employees;
    }
}
