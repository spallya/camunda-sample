package com.vider.quantum.engine.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class TaskUser  implements Serializable {

    protected String owner;
    protected String ownerName;
    protected String ownerRoles;
    protected String ownerGroups;
    protected String ownerImageUrl;
}
