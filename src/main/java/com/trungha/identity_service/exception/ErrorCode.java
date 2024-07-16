package com.trungha.identity_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    USER_UNCATEGORIZED(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR), // tức là 1 lỗi nào đó ko xac dinh dc
    INVALID_KEY(1001, "INVALID MESSAGE KEY",HttpStatus.BAD_REQUEST), // ví dụ ghi sai code thì show lỗi
    USER_EXISTED(1002, "User existed",HttpStatus.BAD_REQUEST), // user đã tồn tại
    USERNAME_INVALID(1003, "Username must be at least {min} character.",HttpStatus.BAD_REQUEST), // LOI 400
    PASSWORD_ERROR(1004, "Password must be at least {min} character." ,HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User not existed", HttpStatus.NOT_FOUND), // ERROR NOT FOUND 404
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED), // ERROR KO THE LOGIN DC 401
    UNAUTHORIZED(1007, "You don't have permission", HttpStatus.FORBIDDEN), // ERROR 403 KHI USER KO CO QUYEN TRUY CAP
    INVALID_DOB(1008, "Your age must be at least {min}",HttpStatus.BAD_REQUEST), // STATUS ERROR 400
    ;

    private int code;
    private String message;
    private HttpStatusCode httpStatusCode; // them vao de biet loi do la 400 or 401 or 404 or 500

    ErrorCode(int code, String message, HttpStatusCode httpStatusCode) {
        this.code = code;
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }

}
