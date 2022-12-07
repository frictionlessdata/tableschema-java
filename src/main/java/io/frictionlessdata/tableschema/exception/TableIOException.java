package io.frictionlessdata.tableschema.exception;

public class TableIOException extends TableSchemaException{

    public TableIOException() {}

    public TableIOException(String msg) {
        super(msg);
    }

    public TableIOException(Throwable t) {
        super(t);
    }
}
