package com.dfire.common.exception;


/**
 * 权限异常
 *
 * @author scx
 */
public class NoPermissionException extends RuntimeException {

    public NoPermissionException() {
        super();
    }

    public NoPermissionException(String message) {
        super(message);
    }

    public NoPermissionException(Throwable e) {
        super(e);
    }

    public NoPermissionException(String msg, Throwable e) {
        super(msg, e);
    }
}
