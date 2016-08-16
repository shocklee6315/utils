package com.shock.utils.exception;

/**
 * Created by shocklee on 16/8/16.
 */
public class ReflectionException extends RuntimeException {

    private static final long serialVersionUID = -5439915454935047912L;

    public ReflectionException() {
        super();
    }

    public ReflectionException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public ReflectionException(String detailMessage) {
        super(detailMessage);
    }

    public ReflectionException(Throwable throwable) {
        super(throwable);
    }
}
