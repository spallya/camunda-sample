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
public class ElementSequenceMetadata  implements Serializable {

    private String fieldName;
    private int sequence;
    private boolean mutuallyExclusiveSelectorField;
    private boolean dynamicSectionFields;
    private String dynamicSectionId;
    private boolean tabularSectionFields;
    private String tabularSectionId;
    private String repeatOnFieldName;
    private List<String> fields;
    private List<SequencingFieldMetadata> fieldsWithMetadata;
}
