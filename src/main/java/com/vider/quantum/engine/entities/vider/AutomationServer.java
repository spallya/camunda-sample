package com.vider.quantum.engine.entities.vider;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "automation_servers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutomationServer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;


    @Column(name = "machine_name")
    private String machineName;

    @Column(name = "health_check_url")
    private String healthCheckUrl;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "automation_machines_id")
    private Integer automationMachinesId;

    @Column(name = "server_status")
    private String serverStatus;
}
