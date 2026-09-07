package com.example.box_dispatch_api.Exception;

public class InvalidBoxStateException extends RuntimeException {

    public InvalidBoxStateException(String message) {
        super(message);
    }
}
