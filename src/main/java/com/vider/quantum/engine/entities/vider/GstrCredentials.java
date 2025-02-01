package com.vider.quantum.engine.entities.vider;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "gstr_credentials")
public class GstrCredentials {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_name", length = 45)
    private String userName;

    @Column(name = "password", length = 45)
    private String password;

    @Column(name = "client_id")
    private Integer clientId;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "organization_id")
    private Integer organizationId;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "datetime(6) default current_timestamp(6)")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, columnDefinition = "datetime(6) default current_timestamp(6) on update current_timestamp(6)")
    private LocalDateTime updatedAt;

    @Column(name = "auth_token", length = 45)
    private String authToken;
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;
    public enum Status {
        ENABLE, DISABLE
    }
}
