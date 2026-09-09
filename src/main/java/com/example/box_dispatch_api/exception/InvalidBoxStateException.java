package com.example.box_dispatch_api.exception;

public class InvalidBoxStateException extends RuntimeException {
    public InvalidBoxStateException(String message) {
        super(message);
    }
}
