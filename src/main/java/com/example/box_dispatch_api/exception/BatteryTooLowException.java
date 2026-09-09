package com.example.box_dispatch_api.exception;

public class BatteryTooLowException extends RuntimeException{
    public BatteryTooLowException (String message){
        super(message);
    }
}
