package com.vider.quantum.engine.entities.camunda;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "atmn_server_details")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutomationServerDetails implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "server_url")
    private String serverUrl;

    @Column(name = "max_parallel_capacity")
    private int maxParallelCapacity;

    @Column(name = "current_load")
    private int currentLoad;

    @Column(name = "active")
    private String active;

}
