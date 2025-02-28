package org.example.errors;

public class ODTFileReadingException extends Exception {
    public ODTFileReadingException(String message){
        super(message);
    }

    public ODTFileReadingException(String message, Throwable cause){
        super(message);
    }
}
