package com.aritra.ecs.demo.exceptions;

public class ResourceAlreadyExists extends RuntimeException {
    public ResourceAlreadyExists(String message) { super(message); }
}
