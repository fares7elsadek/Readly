package com.fares_elsadek.Readly.exceptions;

public class NotFoundException extends RuntimeException{
    public NotFoundException(String resource,String id){
        super(String.format("The %s with id: %s not found",resource,id));
    }
}
