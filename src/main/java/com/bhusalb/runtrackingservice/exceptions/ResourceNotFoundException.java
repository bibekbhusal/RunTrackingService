package com.bhusalb.runtrackingservice.exceptions;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException (final String message) {
        super(message);
    }

    public ResourceNotFoundException (final Class<?> clazz, long id) {
        super(String.format("Entity %s with id %d not found", clazz.getSimpleName(), id));
    }

    public ResourceNotFoundException (final Class<?> clazz, String id) {
        super(String.format("Entity %s with id %s not found", clazz.getSimpleName(), id));
    }
}
