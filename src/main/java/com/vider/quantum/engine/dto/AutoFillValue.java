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
public class AutoFillValue implements Serializable {

    private boolean autoFillFromMetadata;
    private String fieldName;
    private boolean needToFetchTotalOfFields;
    private boolean populateOptionsUsingIndexedFields;
    private String fieldNames;
    private String indexedFieldNames;
    private boolean conversionRequired;
    private boolean indexCopyValueFrom;
    private String copyValueFrom;
    private String copyValueFromIndexed;
    private String dynamicConversionRateFromMetaDataField;

}
