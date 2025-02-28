package org.example.errors;

public class XMLFileReadingException extends Exception{
    public XMLFileReadingException(String message){
        super(message);
    }

    public XMLFileReadingException(String message,Throwable cause){
        super(message);
    }
}
