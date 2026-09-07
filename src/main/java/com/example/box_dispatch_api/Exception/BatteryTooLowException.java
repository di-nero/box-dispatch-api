package com.example.box_dispatch_api.Exception;

public class BatteryTooLowException extends RuntimeException{
    public BatteryTooLowException (String message){
        super(message);
    }
}
