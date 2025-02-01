package com.vider.quantum.engine.repository.vider;

import com.vider.quantum.engine.entities.vider.AutomationServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AutomationServerRepository extends JpaRepository<AutomationServer, Integer> {
    List<AutomationServer> findAllByServerStatus(String serverStatus);
}
