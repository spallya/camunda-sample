package com.vider.quantum.engine.delegate.variable.template;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vider.quantum.engine.dto.MetadataField;
import com.vider.quantum.engine.dto.TemplateDocMetadataDto;
import org.apache.commons.collections4.CollectionUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

@Component
public class VariableDocTemplateViewEditData implements JavaDelegate {

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        Object templateDbResult = delegateExecution.getVariable("templateDbResult");
        TemplateDocMetadataDto templateDocMetadataDto = generateDocMetadataFromDbResult(templateDbResult);
        if (templateDocMetadataDto != null) {
            List<MetadataField> metadataFields = templateDocMetadataDto.getSections().get(0).getMetadataFields();
            int i = 1;
            for(MetadataField metadataField : metadataFields) {
                delegateExecution.setVariable("question"+i, metadataField.getQuestion());
                delegateExecution.setVariable("fieldNameQuestion" + i, metadataField.getFieldName());
                delegateExecution.setVariable("defaultValueQuestion" + i, metadataField.getFieldValue());
                delegateExecution.setVariable("typeQuestion" + i, metadataField.getType());
                i++;
            }
            delegateExecution.setVariable("googleDocId", templateDocMetadataDto.getDocumentId());
            if (templateDbResult instanceof List) {
                List list = (List) templateDbResult;
                if (CollectionUtils.isNotEmpty(list)) {
                    Object o = list.get(0);
                    if (o != null && o instanceof Map) {
                        Map map = (Map) o;
                        delegateExecution.setVariable("description", String.valueOf(map.getOrDefault("description", "")));
                        delegateExecution.setVariable("templateName", String.valueOf(map.getOrDefault("template_name", "")));
                        delegateExecution.setVariable("displayName", String.valueOf(map.getOrDefault("name", "")));
                        delegateExecution.setVariable("googleDriveLink", String.valueOf(map.getOrDefault("google_drivelink", "")));
                        delegateExecution.setVariable("templateCategory", String.valueOf(map.getOrDefault("template_category_id", "")));
                        delegateExecution.setVariable("labels", getFormattedLabels(map.getOrDefault("labels", "")));
                        delegateExecution.setVariable("excerpt", String.valueOf(map.getOrDefault("excerpt", "")));
                    }
                }
            }
        }

    }

    private String getFormattedLabels(Object labels) {
        StringJoiner joiner = new StringJoiner(",");
        try {
            List list = new ObjectMapper().readValue(String.valueOf(labels), List.class);
            if (CollectionUtils.isNotEmpty(list)) {
                list.forEach(e -> {
                    Map e1 = (Map) e;
                    joiner.add(String.valueOf(e1.getOrDefault("name", "")));
                });
            }

        } catch (Exception e) {

        }
        return joiner.toString();
    }

    private TemplateDocMetadataDto generateDocMetadataFromDbResult(Object metaDataJson) throws JsonProcessingException {
        if (metaDataJson instanceof List) {
            List list = (List) metaDataJson;
            if (CollectionUtils.isNotEmpty(list)) {
                Object o = list.get(0);
                if (o != null && o instanceof Map) {
                    Map map = (Map) o;
                    return new ObjectMapper()
                            .readValue(map.getOrDefault("metadata", "").toString(),
                                    TemplateDocMetadataDto.class);
                }
            }
        }
        return null;
    }
}
