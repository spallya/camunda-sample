package com.vider.quantum.engine.advice;

import com.vider.quantum.engine.dto.ErrorDto;
import com.vider.quantum.engine.util.RequestCorrelation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Arrays;
import java.util.Date;

@ControllerAdvice
@Slf4j
public class QuantumEngineExceptionHandlerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ErrorDto> handleGenericException(Exception ex) {
        log.error(ex.getMessage());
        log.error(Arrays.toString(ex.getStackTrace()));
        return new ResponseEntity<>(ErrorDto.builder()
                .statusCode(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                .errorMessage(ex.getMessage())
                .timestamp(new Date())
                .correlationId(RequestCorrelation.getId())
                .build(),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }


}
