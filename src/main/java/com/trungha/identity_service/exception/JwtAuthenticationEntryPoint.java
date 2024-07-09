package com.trungha.identity_service.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trungha.identity_service.dto.request.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import java.io.IOException;

// DUNG DE EXCEPTION ERROR 401
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {
        ErrorCode errorCode = ErrorCode.UNAUTHENTICATED;

        response.setStatus(errorCode.getHttpStatusCode().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE); // important, tra cai body ve

        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();

        ObjectMapper objectMapper = new ObjectMapper(); // convert obj sang string vì write yeu cau string
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse)); // convert obj mapper sang json
        response.flushBuffer(); // gui request ve client
    }
}
