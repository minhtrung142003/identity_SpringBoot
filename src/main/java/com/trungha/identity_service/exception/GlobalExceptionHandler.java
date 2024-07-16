package com.trungha.identity_service.exception;

import com.trungha.identity_service.dto.request.ApiResponse;
import jakarta.validation.ConstraintViolation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;
import java.util.Objects;

@Slf4j
@ControllerAdvice // khai báo class này để spring biết rằng khi 1 lỗi xảy ra thì sẽ chạy vào class này.
public class GlobalExceptionHandler {

    private static final String MIN_ATTRIBUTE = "min"; // tao MIN_ATTRIBUTE gan = min de goi lại
    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiResponse> handlingRuntimeException(RuntimeException exception) {
        ApiResponse apiResponse = new ApiResponse<>();
        apiResponse.setCode(ErrorCode.USER_UNCATEGORIZED.getCode());
        apiResponse.setMessage(ErrorCode.USER_UNCATEGORIZED.getMessage());
        return ResponseEntity.badRequest().body(apiResponse);
    }

    // ERROR LOG
    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse> handlingAppException(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        ApiResponse apiResponse = new ApiResponse<>();
        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());
        return ResponseEntity
                .status(errorCode.getHttpStatusCode()) // log error la gi? 404, 400, 401,500
                .body(apiResponse);
    }

    // ERROR 403 USER KO CO QUYEN TRUY CAP
    @ExceptionHandler(value = AccessDeniedException.class)
    ResponseEntity<ApiResponse> handlingAccessDeniedException(AccessDeniedException exception) {
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
        return ResponseEntity.status(errorCode.getHttpStatusCode()).body(
                ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build());
    }

    // exception constraint bị vi phạm
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse> handlingValidation(MethodArgumentNotValidException exception){
        String enumKey = exception.getFieldError().getDefaultMessage();
        ErrorCode errorCode = ErrorCode.INVALID_KEY;
        Map<String, Object> attribute = null; // dung de map min
        try{
            errorCode = ErrorCode.valueOf(enumKey);
            var constraintViolation = exception.getBindingResult() // la error ma cai method MethodArgumentNotValidException ráp lại
                    .getAllErrors().getFirst().unwrap(ConstraintViolation.class);
            // getConstraintDescriptor: noi dung cua annotation ,
            // getAttributes: la 1 cai map mà từ đó có thể lấy dc thong tin chi tiet ma param truyen vao
            attribute = constraintViolation.getConstraintDescriptor().getAttributes();
            log.info(attribute.toString());
        }catch (IllegalArgumentException e) {

        }
        ApiResponse apiResponse = new ApiResponse<>();
        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(Objects.nonNull(attribute) ?
                mapAttributes(errorCode.getMessage(), attribute) :
                        errorCode.getMessage()
                );
        return ResponseEntity.badRequest().body(apiResponse);
    }

    // dung de chang cai min từ gán cứng thành gán động trong ERROR CODE
    private String mapAttributes(String message, Map<String, Object> attribute) {
        String minValue = (String.valueOf( attribute.get(MIN_ATTRIBUTE)));
        return message.replace("{" + MIN_ATTRIBUTE + "}", minValue);
    }
}
