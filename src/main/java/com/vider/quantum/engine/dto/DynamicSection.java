package com.vider.quantum.engine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DynamicSection implements Serializable {

    private String sectionId;
    private List<MetadataField> metadataFields;
    private String content;
    private String contentJoiner;
    private String repeatUsingLinkedDynamicSection;
    private String startIndexAfterLinkedDynamicSSection;
    private String totalElementsUsingLinkedDynamicSection;
    private MetadataField repeatOn;
    private boolean noNeedToUpdateGoogleDoc;
    private int spacingBetweenElements;
    private Map<String, ContentMetadata> contentMapper;

    public String getEffectiveLinkedDynamicSectionIds(){
        Set<String> linkedDynamicSections = new HashSet<>();
        String[] temp = null;
        StringJoiner joiner = new StringJoiner(",");
        if (StringUtils.isNotEmpty(getStartIndexAfterLinkedDynamicSSection())) {
            temp = getStartIndexAfterLinkedDynamicSSection().split(",");
            linkedDynamicSections.addAll(Arrays.asList(temp));
        }
        if (StringUtils.isNotEmpty(getTotalElementsUsingLinkedDynamicSection())) {
            temp = getTotalElementsUsingLinkedDynamicSection().split(",");
            linkedDynamicSections.addAll(Arrays.asList(temp));
        }
        if (StringUtils.isNotEmpty(getRepeatUsingLinkedDynamicSection())) {
            temp = getRepeatUsingLinkedDynamicSection().split(",");
            linkedDynamicSections.addAll(Arrays.asList(temp));
        }
        linkedDynamicSections.forEach(joiner::add);
        return joiner.toString();
    }

}