package com.vider.quantum.engine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MutuallyExclusiveSectionElement implements Serializable {

    private String sectionId;
    private List<MetadataField> metadataFields;
    private String content;
    private String activeOnSelectorValue;

}
