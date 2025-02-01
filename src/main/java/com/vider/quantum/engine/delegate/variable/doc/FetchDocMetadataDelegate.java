package com.vider.quantum.engine.delegate.variable.doc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vider.quantum.engine.dto.DynamicSection;
import com.vider.quantum.engine.dto.TemplateDocMetadataDto;
import com.vider.quantum.engine.manager.FileManager;
import com.vider.quantum.engine.util.VariableDocUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class FetchDocMetadataDelegate implements JavaDelegate {

    private final ResourceLoader resourceLoader;
    private final FileManager fileManager;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        TemplateDocMetadataDto templateDocMetadataDto = null;
        Object metaDataJson = delegateExecution.getVariable("metaDataJson");
        if (metaDataJson == null) {
            templateDocMetadataDto = generateDocMetadataFromStaticResource(delegateExecution);
        } else {
            templateDocMetadataDto = generateDocMetadataFromDbResult(metaDataJson);
        }
        templateDocMetadataDto.setOriginalDocumentId(templateDocMetadataDto.getDocumentId());
        List<DynamicSection> dynamicSections = templateDocMetadataDto.getDynamicSections();
        if (templateDocMetadataDto.getMutuallyExclusiveSections() != null) {
            VariableDocUtil.updateDocWithMutuallyExclusiveSections(templateDocMetadataDto);
        }
        if (CollectionUtils.isNotEmpty(dynamicSections)) {
            VariableDocUtil.updateDocWithDynamicSections(templateDocMetadataDto);
        }
        if (CollectionUtils.isNotEmpty(templateDocMetadataDto.getTabularSections())) {
            TemplateDocMetadataDto finalTemplateDocMetadataDto = templateDocMetadataDto;
            templateDocMetadataDto.getTabularSections()
                    .forEach(tabularSection ->
                            VariableDocUtil.updateDocWithTabularSections(finalTemplateDocMetadataDto, tabularSection, fileManager));
            templateDocMetadataDto.setTabularSections(finalTemplateDocMetadataDto.getTabularSections());
        }
        VariableDocUtil.updateElementsSequencing(templateDocMetadataDto);
        delegateExecution.setVariable("templateDocMetadata", templateDocMetadataDto);
    }

    private TemplateDocMetadataDto generateDocMetadataFromStaticResource(DelegateExecution delegateExecution) throws IOException {
        Object docTemplateName = delegateExecution.getVariable("docTemplateName");
        Resource resource = resourceLoader.getResource("classpath:document/template/metadata/" +
                docTemplateName + ".json");
        InputStream inputStream = resource.getInputStream();
        return new ObjectMapper().readValue(inputStream,
                TemplateDocMetadataDto.class);
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
