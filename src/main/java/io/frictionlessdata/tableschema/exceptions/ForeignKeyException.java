package io.frictionlessdata.tableschema.exceptions;

/**
 *
 */
public class ForeignKeyException extends TableSchemaException {

    /**
     * Constructs an instance of <code>ForeignKeyException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public ForeignKeyException(String msg) {
        super(msg);
    }
}
