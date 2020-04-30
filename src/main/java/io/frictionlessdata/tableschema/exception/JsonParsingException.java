package io.frictionlessdata.tableschema.exception;

public class JsonParsingException extends TableSchemaException {

    /**
     * Constructs an instance of <code>JsonParsingException</code> by wrapping a Throwable
     *
     * @param t the wrapped exception.
     */
    public JsonParsingException(Throwable t) {
        super(t);
    }
}
