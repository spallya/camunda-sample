package com.vider.quantum.engine.entities.vider;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "automation_machines")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutomationMachine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "machine_name")
    private String machineName;

    @Column(name = "auto_credentials_id")
    private Integer autoCredentialsId;

    @Column(name = "tan_credentials_id")
    private Integer tanCredentialsId;

    @Column(name = "gstr_credentials_id")
    private Integer gstrCredentialsId;

    @Column(columnDefinition = "json", name = "modules")
    private String modules;

    private String status;

    @Column(name = "user_id")
    private Integer userId;

    private String remarks;

    @Column(name = "created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime updatedAt;

    @Column(columnDefinition = "json", name = "complete_modules")
    private String completeModules;

    @Enumerated(EnumType.STRING)
    private SyncType type;

    public enum SyncType {
        INCOMETAX, GSTR, TAN
    }

}
