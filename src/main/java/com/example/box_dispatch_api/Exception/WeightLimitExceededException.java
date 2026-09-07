package com.example.box_dispatch_api.Exception;

public class WeightLimitExceededException extends RuntimeException {

    public WeightLimitExceededException(String message) {
        super(message);
    }
}
