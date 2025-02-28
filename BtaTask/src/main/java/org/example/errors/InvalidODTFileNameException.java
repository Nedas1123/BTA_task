package org.example.errors;

public class InvalidODTFileNameException extends Exception{
    public InvalidODTFileNameException(String message) {
        super(message);
    }
}
