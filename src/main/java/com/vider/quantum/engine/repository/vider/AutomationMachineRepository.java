package com.vider.quantum.engine.repository.vider;

import com.vider.quantum.engine.entities.vider.AutomationMachine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface AutomationMachineRepository extends JpaRepository<AutomationMachine, Integer> {
    List<AutomationMachine> findAllByStatus(String status);

    List<AutomationMachine> findAllByStatusInAndAutoCredentialsId(Collection<String> statuses, Integer autoCredentialsId);

    List<AutomationMachine> findAllByAutoCredentialsId(Integer autoCredentialsId);

    List<AutomationMachine> findAllByStatusInAndGstrCredentialsId(Collection<String> statuses, Integer gstrCredentialsId);

    List<AutomationMachine> findAllByStatusInAndTanCredentialsId(List<String> statuses, Integer tanCredentialsId);
}
