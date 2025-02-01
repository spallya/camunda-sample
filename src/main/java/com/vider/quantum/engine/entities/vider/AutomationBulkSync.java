package com.vider.quantum.engine.entities.vider;

import com.vider.quantum.engine.listener.AutomationBulkSyncListener;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "automation_bulk_sync")
@EntityListeners(AutomationBulkSyncListener.class)
public class AutomationBulkSync {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "client_ids", length = 255)
    private String clientIds;

    @Column(columnDefinition = "json")
    private String modules;

    @Column(columnDefinition = "json")
    private String schedule;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "org_id")
    private Integer orgId;

    @Column(name = "cron_expression", length = 255)
    private String cronExpression;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "gstr_credentials_id")
    private String gstrCredentialsId;

    @Column(name = "tan_credentials_id")
    private String tanCredentialsId;

    @Column(name = "enabled")
    private String enabled;

    @Enumerated(EnumType.STRING)
    private SyncType type;

    public enum SyncType {
        INCOMETAX, GSTR, TAN
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
