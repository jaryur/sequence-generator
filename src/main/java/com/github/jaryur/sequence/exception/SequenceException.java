package com.github.jaryur.sequence.exception;

/**
 * Created by Jaryur
 *
 * Comment:
 */
public class SequenceException extends RuntimeException{

    public SequenceException(String message) {
        super(message);
    }

    public SequenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
