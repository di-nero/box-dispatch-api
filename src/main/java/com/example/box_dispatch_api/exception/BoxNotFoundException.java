package com.example.box_dispatch_api.exception;

public class BoxNotFoundException extends RuntimeException {
    public BoxNotFoundException(String message) {
        super(message);
    }
}
