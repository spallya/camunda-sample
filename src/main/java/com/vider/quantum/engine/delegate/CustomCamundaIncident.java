package com.vider.quantum.engine.delegate;

import org.camunda.bpm.engine.impl.incident.IncidentContext;
import org.camunda.bpm.engine.impl.incident.IncidentHandler;
import org.camunda.bpm.engine.runtime.Incident;
import org.springframework.stereotype.Component;

@Component
public class CustomCamundaIncident implements IncidentHandler {
    @Override
    public String getIncidentHandlerType() {
        return "metadataFeeder";
    }

    @Override
    public Incident handleIncident(IncidentContext incidentContext, String s) {
        return null;
    }

    @Override
    public void resolveIncident(IncidentContext incidentContext) {
        System.out.println();

    }

    @Override
    public void deleteIncident(IncidentContext incidentContext) {
        System.out.println();

    }
}
