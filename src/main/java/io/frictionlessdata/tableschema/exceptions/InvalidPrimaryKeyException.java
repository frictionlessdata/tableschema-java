package io.frictionlessdata.tableschema.exceptions;

public class InvalidPrimaryKeyException extends Exception {

    public InvalidPrimaryKeyException() {
    }

    public InvalidPrimaryKeyException(String msg) {
        super(msg);
    }
}
