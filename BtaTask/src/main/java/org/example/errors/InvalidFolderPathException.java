package org.example.errors;

public class InvalidFolderPathException extends Exception{
    public InvalidFolderPathException(String message) {
        super(message);
    }
}
