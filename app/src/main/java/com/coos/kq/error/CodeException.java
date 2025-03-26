package com.coos.kq.error;

public class CodeException extends Exception {

    public final String code;
    public final String msg;

    public CodeException(String msg) {
        this("1", msg);
    }

    public CodeException(String code, String msg) {
        this(code, msg, null);
    }

    public CodeException(String code, String msg, Throwable throwable) {
        super("code:" + code + ",msg:" + msg, throwable);
        this.code = code;
        this.msg = msg;
    }

}
