package com.dfire.common.exception;



/**
 * @author scx
 */
public class UnsupportedTypeException extends RuntimeException {

    public UnsupportedTypeException(){
        super();
    }

    public UnsupportedTypeException(String message){
        super(message);
    }

    public UnsupportedTypeException(Throwable e){
        super(e);
    }

    public UnsupportedTypeException(String msg, Throwable e){
        super(msg, e);
    }
}
