package com.vider.quantum.engine.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Builder
@Data
public class ErrorDto {

    private String correlationId;
    private String statusCode;
    private String errorMessage;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    private Date timestamp;

}
