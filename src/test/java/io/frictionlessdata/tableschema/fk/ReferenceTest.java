package io.frictionlessdata.tableschema.fk;

import io.frictionlessdata.tableschema.exception.ForeignKeyException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ReferenceTest {

    @Test
    @DisplayName("Create a Reference with resource and field properties")
    public void testValidStringFieldsReference() throws ForeignKeyException{
        Reference ref = new Reference("resource", "field", true);
        // Validation set to strict=true and no exception has been thrown.
        Assertions.assertNotNull(ref);
    }

    @Test
    @DisplayName("Create a Reference with two fields from JSON")
    public void testValidArrayFieldsReference() throws ForeignKeyException{
        List<String> fields = new ArrayList<>();
        fields.add("field1");
        fields.add("field2");

        Reference ref = new Reference("resource", fields, true);
        // Validation set to strict=true and no exception has been thrown.
        Assertions.assertNotNull(ref);
    }

    @Test
    @DisplayName("Create a Reference without 'null' resource property -> must throw")
    public void testNullFields() throws ForeignKeyException{
        ForeignKeyException fke = Assertions.assertThrows(ForeignKeyException.class, () -> {
            new Reference(null, "resource", true);
        });
        Assertions.assertEquals(
                "A foreign key's reference must have the fields and resource properties.",
                fke.getMessage());
    }

    @Test
    @DisplayName("Create a Reference without resource property -> must throw in validation")
    public void testNullResource() throws ForeignKeyException{
        Reference ref = new Reference();
        ref.setFields("aField");

        ForeignKeyException fke = Assertions.assertThrows(ForeignKeyException.class, ref::validate);
        Assertions.assertEquals(
                "A foreign key's reference must have the fields and resource properties.",
                fke.getMessage());
    }

    @Test
    @DisplayName("Create a Reference without fields -> must throw in validation")
    public void testNullFieldsAndResource() throws ForeignKeyException{
        Reference ref = new Reference();

        ForeignKeyException fke = Assertions.assertThrows(ForeignKeyException.class, ref::validate);
        Assertions.assertEquals(
                "A foreign key's reference must have the fields and resource properties.",
                fke.getMessage());
    }

    @Test
    @DisplayName("Create a Reference with invalid field type -> must throw in validation")
    public void testInvalidFieldsType() throws ForeignKeyException{
        ForeignKeyException fke = Assertions.assertThrows(ForeignKeyException.class,
                ()-> {new Reference("resource", 123, true);});
        Assertions.assertEquals(
                "The foreign key's reference fields property must be a string or an array.",
                fke.getMessage());

    }

    @Test
    @DisplayName("Create a Reference without fields with lenient validation -> must not throw")
    public void testNullFieldsAndResourceLenient() throws ForeignKeyException{
        Reference ref = new Reference();
        ref.setStrictValidation(false);

        ref.validate();
        Assertions.assertEquals(1, ref.getErrors().size());
    }

    @Test
    @DisplayName("Create a Reference with invalid field type with lenient validation -> must not throw")
    public void testInvalidFieldsTypeLenient() throws ForeignKeyException{
        Reference ref = new Reference("resource", 123, false);

        Assertions.assertEquals(1, ref.getErrors().size());

    }
}
