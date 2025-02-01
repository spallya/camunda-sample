package com.vider.quantum.engine.dto;

import lombok.Data;

@Data
class RestCallResponse {

    private String httpStatusCode;
    private String httpResponse;

}