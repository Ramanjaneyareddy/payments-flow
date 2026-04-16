package com.payflow.inquiry.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.net.URI;
import java.time.Instant;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(PaymentNotFoundException.class)
    public ProblemDetail handleNotFound(PaymentNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return createProblemDetail(HttpStatus.NOT_FOUND, ex.getMessage(), "not-found");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneral(Exception ex) {
        log.error("Internal error: ", ex);
        return createProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", "internal-server-error");
    }

    private ProblemDetail createProblemDetail(HttpStatus status, String detail, String errorKey) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create("https://payflow.com/errors/" + errorKey));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}