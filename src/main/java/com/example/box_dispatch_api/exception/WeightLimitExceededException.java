package com.example.box_dispatch_api.exception;

public class WeightLimitExceededException extends RuntimeException {
    public WeightLimitExceededException(String message) {
        super(message);
    }
}
