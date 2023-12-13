package com.arya.exceptions;


public class RetryOnFailedException extends RuntimeException{

    String message;

    public RetryOnFailedException(String message) {
        super(message);
        this.message = message;
    }
}
