package com.vider.quantum.engine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskCompletionDto implements Serializable {

    private String taskId;
    private List<String> taskIds;
    private String comments;
    private String userId;
    private String status;
    private String lastUpdatedOn;

}
