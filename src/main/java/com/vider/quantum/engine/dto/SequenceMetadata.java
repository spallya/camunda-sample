package com.vider.quantum.engine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SequenceMetadata implements Serializable {

    private int sequence;
    private MetadataField metadata;
    private String dynamicSection;
    private String tabularSection;
    private boolean isRepeatOnField;
    private boolean isTabularSectionField;
    private boolean isDynamicSectionField;
    private boolean isMutuallyExclusiveSectionSelectorField;
}
