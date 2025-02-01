package com.vider.quantum.engine.entities.vider;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "organization_preferences")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationPreferences {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "organization_id")
    private Integer organizationId;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "receipt", columnDefinition = "json")
    private String receipt;

    @Column(name = "invoice_preferences", columnDefinition = "json")
    private String invoicePreferences;

    @Column(name = "holiday_preferences", columnDefinition = "json")
    private String holidayPreferences;

    @Column(name = "qtm_preferences", columnDefinition = "json")
    private String qtmPreferences;

    @Column(name = "notification_config", columnDefinition = "json")
    private String notificationConfig;

    @Column(name = "client_preferences", columnDefinition = "json")
    private String clientPreferences;

    @Column(name = "quantum_config", columnDefinition = "json")
    private String quantumConfig;

    @Column(name = "atom_client_prefix", columnDefinition = "json")
    private String atomClientPrefix;

    @Column(name = "task_preferences", columnDefinition = "json")
    private String taskPreferences;

    @Column(name = "automation_config", columnDefinition = "json")
    private String automationConfig;

    @Column(name = "meta_config", columnDefinition = "json")
    private String metaConfig;
}

