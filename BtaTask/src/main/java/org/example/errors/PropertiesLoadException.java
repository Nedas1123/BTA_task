package org.example.errors;

public class PropertiesLoadException extends Exception{
    public PropertiesLoadException(String message) {
        super(message);
    }

    public PropertiesLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
