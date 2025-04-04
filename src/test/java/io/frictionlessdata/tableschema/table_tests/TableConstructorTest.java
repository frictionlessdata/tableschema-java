package io.frictionlessdata.tableschema.table_tests;

import io.frictionlessdata.tableschema.Table;
import io.frictionlessdata.tableschema.schema.Schema;
import io.frictionlessdata.tableschema.tabledatasource.JsonArrayTableDataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import static io.frictionlessdata.tableschema.TestHelper.getTestDataDirectory;

class TableConstructorTest {


    @Test
    @DisplayName("Create a Table")
    void createTable1() throws Exception{
        Table table = new Table();
        Assertions.assertNull(table.getSchema());
        Assertions.assertNull(table.getTableDataSource());
    }

    @Test
    @DisplayName("Create a Table And Set a Schema")
    void createTable2() throws Exception{
        Table table = new Table();
        File testDataDir = getTestDataDirectory();

        Schema schema = Schema.fromJson(new File(testDataDir, "schema/employee_schema.json"), true);
        table.setSchema(schema);
        Assertions.assertNotNull(table.getSchema());
        Assertions.assertNull(table.getTableDataSource());
    }


    @Test
    @DisplayName("Create a Table And Set a TableDataSource")
    void createTable3() throws Exception{
        Table table = new Table();
        File testDataDir = getTestDataDirectory();

        try (InputStream inStream = new FileInputStream(new File(testDataDir, "data/population.json"))) {
            JsonArrayTableDataSource fmt = new JsonArrayTableDataSource(inStream);
            table.setTableDataSource(fmt);
        }

        Assertions.assertNull(table.getSchema());
        Assertions.assertNotNull(table.getTableDataSource());
    }

    @Test
    @DisplayName("Create a Table And set a Schema and a TableDataSource")
    void createTable4() throws Exception{
        Table table = new Table();
        File testDataDir = getTestDataDirectory();

        Schema schema = Schema.fromJson(new File(testDataDir, "schema/population_schema.json"), true);
        try (InputStream inStream = new FileInputStream(new File(testDataDir, "data/population.json"))) {
            JsonArrayTableDataSource fmt = new JsonArrayTableDataSource(inStream);
            table.setTableDataSource(fmt);
        }

        table.setSchema(schema);
        Assertions.assertNotNull(table.getSchema());
        Assertions.assertNotNull(table.getTableDataSource());
    }

    @Test
    @DisplayName("Create a Table And set a Schema and a TableDataSource - 2")
    void createTable5() throws Exception{
        Table table = new Table();
        File testDataDir = getTestDataDirectory();

        Schema schema = Schema.fromJson(new File(testDataDir, "schema/population_schema.json"), true);
        table.setSchema(schema);
        try (InputStream inStream = new FileInputStream(new File(testDataDir, "data/population.json"))) {
            JsonArrayTableDataSource fmt = new JsonArrayTableDataSource(inStream);
            table.setTableDataSource(fmt);
        }

        Assertions.assertNotNull(table.getSchema());
        Assertions.assertNotNull(table.getTableDataSource());
    }
}
