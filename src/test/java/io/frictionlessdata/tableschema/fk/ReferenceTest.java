package io.frictionlessdata.tableschema.fk;

import io.frictionlessdata.tableschema.exception.ForeignKeyException;
import io.frictionlessdata.tableschema.util.JsonUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 *
 */
public class ReferenceTest {

    @Test
    public void testValidStringFieldsReference() throws ForeignKeyException{
        Reference ref = new Reference("resource", "field");

        // Validation set to strict=true and no exception has been thrown.
        // Test passes.
        Assertions.assertNotNull(ref);
    }

    @Test
    public void testValidArrayFieldsReference() throws ForeignKeyException{
        List<String> fields = new ArrayList<>();
        fields.add("field1");
        fields.add("field2");

        Reference ref = new Reference("resource", JsonUtil.getInstance().createArrayNode(fields));

        // Validation set to strict=true and no exception has been thrown.
        // Test passes.
        Assertions.assertNotNull(ref);
    }

    @Test
    public void testNullFields(){
        ForeignKeyException msg = assertThrows(ForeignKeyException.class, () -> new Reference(null, "resource", true));
        Assertions.assertEquals(
                "A foreign key's reference must have the fields and resource properties.",
                msg.getMessage());
    }

    @Test
    public void testNullResource() {
        Reference ref = new Reference();
        ref.setFields("aField");
        ForeignKeyException msg = assertThrows(ForeignKeyException.class, ref::validate);
        Assertions.assertEquals(
                "A foreign key's reference must have the fields and resource properties.",
                msg.getMessage());
    }

    @Test
    public void testNullFieldsAndResource() {
        Reference ref = new Reference();
        ForeignKeyException msg = assertThrows(ForeignKeyException.class, ref::validate);
        Assertions.assertEquals(
                "A foreign key's reference must have the fields and resource properties.",
                msg.getMessage());
    }

    @Test
    public void testInvalidFieldsType() throws ForeignKeyException{
        ForeignKeyException msg = assertThrows(ForeignKeyException.class, () -> new Reference("resource", 123, true));
        Assertions.assertEquals(
                "The foreign key's reference fields property must be a string or an array.",
                msg.getMessage());

    }

}
