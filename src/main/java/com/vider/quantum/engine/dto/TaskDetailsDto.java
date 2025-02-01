package com.vider.quantum.engine.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@Builder
public class TaskDetailsDto implements Serializable {

    protected String id;
    protected String owner;
    protected String ownerName;
    protected String ownerRoles;
    protected String ownerGroups;
    protected String ownerImageUrl;
    protected List<TaskUser> taskOwners;
    protected String assignee;
    protected String assigneeName;
    protected String assigneeRoles;
    protected String assigneeGroups;
    protected boolean completed;
    protected String parentTaskId;
    protected String processInstanceId;
    protected String name;
    protected String description;
    protected String comments;
    protected String status;
    protected int priority;
    protected Date createTime;
    protected Date lastUpdated;
    protected String lastUpdatedOn;
    protected Date dueDate;
    protected Date followUpDate;
    protected String assigneeImageUrl;

}
