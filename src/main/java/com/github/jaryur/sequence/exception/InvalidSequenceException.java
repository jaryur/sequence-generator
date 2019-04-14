package com.github.jaryur.sequence.exception;

/**
 * Created by Jaryur
 *
 * Comment:
 */
public class InvalidSequenceException extends RuntimeException{

    public InvalidSequenceException(String message) {
        super(message);
    }

    public InvalidSequenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
