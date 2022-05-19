package com.kustacks.kuring.worker.error;

import io.sentry.Sentry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.SQLException;

@ControllerAdvice
public class ErrorController {

    private final Logger log = LoggerFactory.getLogger(ErrorController.class);

    @ExceptionHandler(SQLException.class)
    public @ResponseBody ErrorResponse handleSQLException(SQLException e) {
        log.error("[SQLException] {}", e.getMessage());
        Sentry.captureException(e);
        return new ErrorResponse(ErrorCode.DB_SQLEXCEPTION);
    }

    @ExceptionHandler(InternalLogicException.class)
    public void handleInternalLogicException(InternalLogicException e) {
        log.error("[InternalLogicException] {}", e.getErrorCode().getMessage(), e);
        Sentry.captureException(e);
    }
}
