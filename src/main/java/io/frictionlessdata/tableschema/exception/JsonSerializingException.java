package io.frictionlessdata.tableschema.exception;

public class JsonSerializingException extends TableSchemaException {
    /**
     * Constructs an instance of <code>JsonSerializingException</code> by wrapping a Throwable
     *
     * @param t the wrapped exception.
     */
    public JsonSerializingException(Throwable t) {
        super(t);
    }
}
