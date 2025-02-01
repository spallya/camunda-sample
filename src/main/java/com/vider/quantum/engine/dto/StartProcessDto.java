package com.vider.quantum.engine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StartProcessDto {

    private String processKey;
    private String owner;
    private List<String> taskOwners;
    private Map<String, Object> metaData;

}
