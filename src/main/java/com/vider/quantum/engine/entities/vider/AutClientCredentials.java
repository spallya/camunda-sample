package com.vider.quantum.engine.entities.vider;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "aut_client_credentials")
public class AutClientCredentials {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "client_id")
    private Integer clientId;

    @Column(name = "organization_id")
    private Integer organizationId;

    @Column(name = "pan_number")
    private String panNumber;

    @Column(name = "password")
    private String password;

    @Column(name = "sync_status")
    private String syncStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum SyncStatus {
        SYNC, NOT_SYNC
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;
    public enum Status {
        ENABLE, DISABLE
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
