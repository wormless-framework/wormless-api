package com.wormless.exception;

public class BusinessException extends RuntimeException {
    public BusinessException(String message) { super(message); }
}