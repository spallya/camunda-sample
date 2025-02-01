
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
public class MetadataField implements Serializable {

    private String fieldName;
    private String fieldValue;
    private String question;
    private String type;
    private String placeHolder;
    private String tips;
    private String id;
    private boolean hideInUi;
    private boolean drivesConditionalContent;
    private List<String> options;
    private AutoFillValue autoFillValue;

}
