package com.boraver.teamgenerator.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public Object badRequest(
            IllegalArgumentException ex,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        if (isSseRequest(request)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("error", Objects.toString(ex.getMessage(), "Requisição inválida"));

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(SecurityException.class)
    public Object unauthorized(
            SecurityException ex,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        if (isSseRequest(request)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("error", Objects.toString(ex.getMessage(), "Não autorizado"));

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(Exception.class)
    public Object generic(
            Exception ex,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        if (isSseRequest(request)) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return null;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("error", Objects.toString(ex.getMessage(), "Erro interno do servidor"));

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private boolean isSseRequest(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        String uri = request.getRequestURI();

        return (accept != null && accept.contains(MediaType.TEXT_EVENT_STREAM_VALUE))
                || (uri != null && uri.matches("^/championships/[^/]+/stream$"));
    }
}