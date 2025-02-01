package com.vider.quantum.engine.util;

import com.vider.quantum.engine.dto.TemplateDocMetadataDto;
import org.apache.commons.collections4.CollectionUtils;

import java.util.concurrent.atomic.AtomicReference;

public class TemplateDocMetadataUtil {

    private TemplateDocMetadataUtil() {

    }

    public static String getSectionMetadataFieldValue(TemplateDocMetadataDto templateDocMetadataDto, String fieldName) {
        AtomicReference<String> fieldValue = new AtomicReference<>("");
        if (CollectionUtils.isNotEmpty(templateDocMetadataDto.getSections())) {
            templateDocMetadataDto.getSections().forEach(section -> {
                if (CollectionUtils.isNotEmpty(section.getMetadataFields())) {
                    section.getMetadataFields().stream()
                            .filter(metadataField -> fieldName.equalsIgnoreCase(metadataField.getFieldName()))
                            .findFirst()
                            .ifPresent(metadataField -> fieldValue.set(metadataField.getFieldValue()));
                }
            });
        }
        return fieldValue.get();
    }
}
