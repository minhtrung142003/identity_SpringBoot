package com.trungha.identity_service.exception;

public class AppException extends RuntimeException{
    private ErrorCode errorCode;

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }
}
