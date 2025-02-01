package com.vider.quantum.engine.entities.vider;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "tan_client_credentials")
@Data
public class TanClientCredentials {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "tan_number", length = 45)
    private String tanNumber;

    @Column(name = "password", length = 45)
    private String password;

    @Column(name = "client_id")
    private Integer clientId;

    @Column(name = "organization_id")
    private Integer organizationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "password_id")
    private Integer passwordId;

    public enum Status {
        ENABLE, DISABLE
    }
}
