package io.frictionlessdata.tableschema.fk;


import io.frictionlessdata.tableschema.Table;
import io.frictionlessdata.tableschema.TestHelper;
import io.frictionlessdata.tableschema.exception.ForeignKeyException;
import io.frictionlessdata.tableschema.schema.Schema;
import io.frictionlessdata.tableschema.util.JsonUtil;
import org.apache.commons.csv.CSVFormat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ForeignKeyTest {

    @Test
    @DisplayName("Check ForeignKey against matching data, good case")
    public void testValidFkReference() throws Exception {
        File testDataDir = TestHelper.getTestDataDirectory();
        File source = TestHelper.getResourceFile("/fixtures/schema/population_schema_for_fk_check.json");

        Schema schema = Schema.fromJson(source, true);
        // get path of test CSV file
        File file = new File("data/population_for_fk_check.csv");
        Table table = Table.fromSource(
                file,
                testDataDir,
                schema,
                CSVFormat.DEFAULT.builder().setHeader().build());
        schema.getForeignKeys().get(0).validate(table);
    }

    @Test
    @DisplayName("Check ForeignKey against not matching data -> must throw")
    public void testInvalidFkReference() throws Exception {
        File testDataDir = TestHelper.getTestDataDirectory();
        File source = TestHelper.getResourceFile("/fixtures/schema/population_schema_for_fk_check.json");

        Schema schema = Schema.fromJson(source, true);
        // get path of test CSV file
        File file = new File("data/population_for_fk_check_invalid.csv");
        Table table = Table.fromSource(
                file,
                testDataDir,
                schema,
                CSVFormat.DEFAULT.builder().setHeader().build());
        ForeignKeyException fke = assertThrows(ForeignKeyException.class, ()
                -> schema.getForeignKeys().get(0).validate(table));
        Assertions.assertEquals("Foreign key [check_year-> year] violation : expected: 2018 found: 2017",
                fke.getMessage());
    }

    @Test
    public void testValidStringFields() throws ForeignKeyException {
        Reference ref = new Reference("aResource", "refField", true);
        ForeignKey fk = new ForeignKey("fkField", ref, true);

        // Validation set to strict=true and no exception has been thrown.
        // Test passes.
        Assertions.assertNotNull(fk);
    }

    @Test
    public void testValidArrayFields() throws ForeignKeyException {
        String refFields = "[\"refField1\", \"refField2\"]";
        // TODO: Reference validator checks for JSONArray instance
        // we must enhance this test after jackson refactoring
        // so we can validate with other types, for now, let's check only with String
        Reference ref = new Reference("aResource", refFields, true);
        String fkFields = "[\"fkField1\", \"fkField2\"]";
        ForeignKey fk = new ForeignKey(fkFields, ref, true);

        // Validation set to strict=true and no exception has been thrown.
        // Test passes.
        Assertions.assertNotNull(fk);
    }

    @Test
    public void testNullFields() throws ForeignKeyException {
        Reference ref = new Reference("aResource", "aField", true);


        ForeignKeyException ex = assertThrows(ForeignKeyException.class, () -> {
            new ForeignKey(null, ref, true);
        });
        Assertions.assertEquals("A foreign key must have the fields and reference properties.", ex.getMessage());
    }

    @Test
    public void testNullReference() throws ForeignKeyException{
        ForeignKey fk = new ForeignKey(true);
        fk.setFields("aField");
        ForeignKeyException ex = assertThrows(ForeignKeyException.class, fk::validate);
        Assertions.assertEquals("A foreign key must have the fields and reference properties.", ex.getMessage());
    }

    @Test
    public void testNullFieldsAndReference() throws ForeignKeyException{
        ForeignKey fk = new ForeignKey(true);
        ForeignKeyException ex = assertThrows(ForeignKeyException.class, fk::validate);
        Assertions.assertEquals("A foreign key must have the fields and reference properties.", ex.getMessage());
    }

    @Test
    public void testFieldsNotStringOrArray() throws ForeignKeyException{
        Reference ref = new Reference("aResource", "aField", true);

        ForeignKeyException ex = assertThrows(ForeignKeyException.class, () -> new ForeignKey(25, ref, true));
        Assertions.assertEquals("The foreign key's fields property must be a string or an array.", ex.getMessage());
    }

    @Test
    public void testFkFieldsIsStringAndRefFieldsIsArray() throws ForeignKeyException{
        List<String> refFields = new ArrayList<>();
        refFields.add("field1");
        refFields.add("field2");
        refFields.add("field3");

        Reference ref = new Reference("aResource", JsonUtil.getInstance().createArrayNode(refFields), true);

        ForeignKeyException ex = assertThrows(ForeignKeyException.class, () -> new ForeignKey("aStringField", ref, true));
        Assertions.assertEquals("The reference's fields property must be a string if the outer fields is a string.", ex.getMessage());
    }

    @Test
    public void testFkFieldsIsArrayAndRefFieldsIsString() throws ForeignKeyException{
        Reference ref = new Reference("aResource", "aStringField", true);

        List<String> fkFields = new ArrayList<>();
        fkFields.add("field1");
        fkFields.add("field2");
        fkFields.add("field3");

        ForeignKeyException ex = assertThrows(ForeignKeyException.class,
                () -> new ForeignKey(JsonUtil.getInstance().createArrayNode(fkFields), ref, true));
        Assertions.assertEquals("The reference's fields property must be an array " +
                "if the outer fields is an array.", ex.getMessage());
    }

    @Test
    public void testFkAndRefFieldsDifferentSizeArray() throws ForeignKeyException{
    	List<String> refFields = new ArrayList<>();
        refFields.add("refField1");
        refFields.add("refField2");
        refFields.add("refField3");

        Reference ref = new Reference("aResource", JsonUtil.getInstance().createArrayNode(refFields), true);

        List<String> fkFields = new ArrayList<>();
        fkFields.add("field1");
        fkFields.add("field2");

        ForeignKeyException ex = assertThrows(ForeignKeyException.class,
                () -> new ForeignKey(JsonUtil.getInstance().createArrayNode(fkFields), ref, true));
        Assertions.assertEquals("The reference's fields property must be an array" +
                " of the same length as that of the outer fields' array.", ex.getMessage());
    }
}
