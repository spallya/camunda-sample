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
public class MutuallyExclusiveSection implements Serializable {

    private MetadataField selector;
    private List<MutuallyExclusiveSectionElement> elements;
}
