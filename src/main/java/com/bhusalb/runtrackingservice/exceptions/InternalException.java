package com.bhusalb.runtrackingservice.exceptions;

public class InternalException extends RuntimeException {

    public InternalException (final String message) {
        super(message);
    }

    public InternalException (final String message, final Throwable cause) {
        super(message, cause);
    }

    public InternalException (final Throwable cause) {
        super(cause);
    }
}
