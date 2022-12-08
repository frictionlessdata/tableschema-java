package io.frictionlessdata.tableschema.schema;

import java.util.List;

public interface SchemaInterface {

    boolean isValid();

    void validate();

    List<Exception>  getErrors();
}
