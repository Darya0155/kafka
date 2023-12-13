package com.arya.exceptions;


public class DontRetryOnFailedException extends RuntimeException{

    String message;

    public DontRetryOnFailedException(String message) {
        super(message);
        this.message = message;
    }
}
