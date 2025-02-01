package com.vider.quantum.engine.entities.camunda;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "atmn_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutomationRequest implements Serializable {
    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "process_instance_id")
    private String processInstanceId;

    @Column(name = "status")
    private String status;

    @Column(name = "requests")
    private String requests;

    @Column(name = "message")
    private String message;

    @Column(name = "client_id")
    private String clientId;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    // Getter and setters

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        updatedAt = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }
}
