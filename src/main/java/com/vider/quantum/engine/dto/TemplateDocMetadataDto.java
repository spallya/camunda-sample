
package com.vider.quantum.engine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateDocMetadataDto implements Serializable {

    private static final long serialVersionUID = -6429785692096714703L;

    private String documentId;
    private String originalDocumentId;
    private List<Section> sections;
    private List<DynamicSection> dynamicSections;
    private List<TabularSection> tabularSections;
    private MutuallyExclusiveSection mutuallyExclusiveSections;
    private String header;
    private String footer;
    private List<ElementSequenceMetadata> initialSequence;
    private Map<String, SequenceMetadata> sequencing;
}
