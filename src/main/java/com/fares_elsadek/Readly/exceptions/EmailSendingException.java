package com.fares_elsadek.Readly.exceptions;

public class EmailSendingException extends RuntimeException{
    public EmailSendingException(String message, Throwable cause) {
        super(message, cause);
    }
}
