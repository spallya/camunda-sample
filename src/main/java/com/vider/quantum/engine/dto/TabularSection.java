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
public class TabularSection implements Serializable {

    private String sectionId;
    private List<MetadataField> metadataFields;
    private MetadataField repeatOn;
    private List<TableColumnMetadata> columns;
    private String repeatUsingLinkedDynamicSection;

}
