package com.vider.quantum.engine.repository.camunda;

import com.vider.quantum.engine.entities.camunda.AutomationServerDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AutomationServerDetailsRepository extends JpaRepository<AutomationServerDetails, Long> {

    List<AutomationServerDetails> findAllByActive(String active);

    @Query(
            value = "SELECT * " +
                    "FROM atmn_server_details " +
                    "WHERE current_load < max_parallel_capacity " +
                    "AND active = 'Y'",
            nativeQuery = true)
    List<AutomationServerDetails> findAllActiveUsableServers();

    Optional<AutomationServerDetails> findServerByServerUrl(String serverUrl);
}
