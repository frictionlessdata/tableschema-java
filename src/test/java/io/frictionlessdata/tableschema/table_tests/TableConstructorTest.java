package io.frictionlessdata.tableschema.table_tests;

import io.frictionlessdata.tableschema.Table;
import io.frictionlessdata.tableschema.datasourceformats.JsonArrayDataSourceFormat;
import io.frictionlessdata.tableschema.schema.Schema;
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
        Assertions.assertNull(table.getDataSourceFormat());
    }

    @Test
    @DisplayName("Create a Table And Set a Schema")
    void createTable2() throws Exception{
        Table table = new Table();
        File testDataDir = getTestDataDirectory();

        Schema schema = Schema.fromJson(new File(testDataDir, "schema/employee_schema.json"), true);
        table.setSchema(schema);
        Assertions.assertNotNull(table.getSchema());
        Assertions.assertNull(table.getDataSourceFormat());
    }


    @Test
    @DisplayName("Create a Table And Set a DataSourceFormat")
    void createTable3() throws Exception{
        Table table = new Table();
        File testDataDir = getTestDataDirectory();

        try (InputStream inStream = new FileInputStream(new File(testDataDir, "data/population.json"))) {
            JsonArrayDataSourceFormat fmt = new JsonArrayDataSourceFormat (inStream);
            table.setDataSourceFormat(fmt);
        }

        Assertions.assertNull(table.getSchema());
        Assertions.assertNotNull(table.getDataSourceFormat());
    }

    @Test
    @DisplayName("Create a Table And set a Schema and a DataSourceFormat")
    void createTable4() throws Exception{
        Table table = new Table();
        File testDataDir = getTestDataDirectory();

        Schema schema = Schema.fromJson(new File(testDataDir, "schema/population_schema.json"), true);
        try (InputStream inStream = new FileInputStream(new File(testDataDir, "data/population.json"))) {
            JsonArrayDataSourceFormat fmt = new JsonArrayDataSourceFormat (inStream);
            table.setDataSourceFormat(fmt);
        }

        table.setSchema(schema);
        Assertions.assertNotNull(table.getSchema());
        Assertions.assertNotNull(table.getDataSourceFormat());
    }

    @Test
    @DisplayName("Create a Table And set a Schema and a DataSourceFormat - 2")
    void createTable5() throws Exception{
        Table table = new Table();
        File testDataDir = getTestDataDirectory();

        Schema schema = Schema.fromJson(new File(testDataDir, "schema/population_schema.json"), true);
        table.setSchema(schema);
        try (InputStream inStream = new FileInputStream(new File(testDataDir, "data/population.json"))) {
            JsonArrayDataSourceFormat fmt = new JsonArrayDataSourceFormat (inStream);
            table.setDataSourceFormat(fmt);
        }

        Assertions.assertNotNull(table.getSchema());
        Assertions.assertNotNull(table.getDataSourceFormat());
    }
}
