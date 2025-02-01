package com.vider.quantum.engine.delegate.variable.template;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.vider.quantum.engine.dto.MetadataField;
import com.vider.quantum.engine.dto.Section;
import com.vider.quantum.engine.dto.TemplateDocMetadataDto;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class FormatDataForVariableTemplate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        String description = getVariable(delegateExecution, "description");
        String templateName = getVariable(delegateExecution, "templateName");
        String displayName = getVariable(delegateExecution, "displayName");
        String googleDriveLink = getVariable(delegateExecution, "googleDriveLink");
        String templateCategory = getVariable(delegateExecution, "templateCategory");
        String labels = getVariable(delegateExecution, "labels");
        String excerpt = getVariable(delegateExecution, "excerpt");

        TemplateDocMetadataDto templateDocMetadataDto = buildTemplateDocMetadata(delegateExecution);
        List<Map<String, String>> labelsObj = prepareFormattedLabels(labels);

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String metadataJson = ow.writeValueAsString(templateDocMetadataDto);
        String labelsJson = ow.writeValueAsString(labelsObj);

        delegateExecution.setVariable("metadataJson", metadataJson);
        delegateExecution.setVariable("name", displayName);
        delegateExecution.setVariable("templateCategoryId", templateCategory);
        delegateExecution.setVariable("labelsJson", labelsJson);
        delegateExecution.setVariable("description", description);
        delegateExecution.setVariable("templateId", templateName);
        delegateExecution.setVariable("googleDriveLink", googleDriveLink);
        delegateExecution.setVariable("excerpt", excerpt);
    }

    private List<Map<String, String>> prepareFormattedLabels(String labels) {
        List<Map<String, String>> labelsObj = new ArrayList<>();
        String[] split = labels.split(",");
        for (String s : split) {
            if(StringUtils.isNotBlank(s)) {
                Map<String, String> map = new HashMap<>();
                map.put("name", s.trim());
                labelsObj.add(map);
            }
        }
        return labelsObj;
    }

    private TemplateDocMetadataDto buildTemplateDocMetadata(DelegateExecution delegateExecution) {
        List<Section> sectionList = new ArrayList<>();
        String googleDocId = getVariable(delegateExecution, "googleDocId");
        Section generalSection = Section.builder().sectionId("generalSection").build();
        List<MetadataField> metadataFields = new ArrayList<>();
        for (int i = 1; i < 21; i++) {
            String question = getVariable(delegateExecution, "question" + i);
            String fieldName = getVariable(delegateExecution, "fieldNameQuestion" + i);
            String defaultValue = getVariable(delegateExecution, "defaultValueQuestion" + i);
            String type = getVariable(delegateExecution, "typeQuestion" + i);
            if (StringUtils.isNotBlank(question) && StringUtils.isNotBlank(fieldName)) {
                metadataFields.add(MetadataField.builder()
                        .fieldName(fieldName)
                        .fieldValue(defaultValue)
                        .question(question)
                        .type(type)
                        .build());
            }
        }
        generalSection.setMetadataFields(metadataFields);
        sectionList.add(generalSection);
        return TemplateDocMetadataDto.builder()
                .sections(sectionList)
                .footer("")
                .header("")
                .documentId(googleDocId)
                .build();
    }

    private String getVariable(DelegateExecution delegateExecution, String key) {
        Object variable = delegateExecution.getVariable(key);
        if (variable != null) {
            return String.valueOf(variable);
        }
        return "";
    }
}
