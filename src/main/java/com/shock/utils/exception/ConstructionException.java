package com.shock.utils.exception;

/**
 * Created by shocklee on 16/7/8.
 */
public class ConstructionException extends RuntimeException{

    private static final long serialVersionUID = -5439915454935047912L;

    private Class<?> beanClass;

    public ConstructionException(Class<?> beanClass, String msg) {
        this(beanClass, msg, null);
    }

    public ConstructionException(Class<?> beanClass, String msg, Throwable cause) {
        super("Failed to instantiate [" + beanClass.getName() + "]: " + msg, cause);
        this.beanClass = beanClass;
    }
    /**
     * Return the offending bean class.
     */
    public Class<?> getBeanClass() {
        return this.beanClass;
    }
}
