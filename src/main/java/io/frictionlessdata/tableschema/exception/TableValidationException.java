package io.frictionlessdata.tableschema.exception;

/**
 *
 *
 */
public class TableValidationException extends TableSchemaException {


    /**
     * Constructs an instance of <code>v</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public TableValidationException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of <code>TableSchemaException</code> by wrapping a Throwable
     *
     * @param t the wrapped exception.
     */
    public TableValidationException(Throwable t) {
        super(t);
    }
}