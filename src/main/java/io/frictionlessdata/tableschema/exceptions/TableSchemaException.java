package io.frictionlessdata.tableschema.exceptions;

/**
 *
 *
 */
public class TableSchemaException extends RuntimeException {

    /**
     * Creates a new instance of <code>TableSchemaException</code> without
     * detail message.
     */
    public TableSchemaException() {
    }

    /**
     * Constructs an instance of <code>v</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public TableSchemaException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of <code>TableSchemaException</code> by wrapping a Throwable
     *
     * @param t the wrapped exception.
     */
    public TableSchemaException(Throwable t) {
        super(t);
    }
}