package com.github.jaryur.sequence.exception;

/**
 * Created by Jaryur
 *
 * Comment:
 */
public class SequenceConfigException extends RuntimeException{


    public SequenceConfigException(String message) {
        super(message);
    }

    public SequenceConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
