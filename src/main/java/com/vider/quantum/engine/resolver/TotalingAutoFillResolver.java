package com.vider.quantum.engine.resolver;

import com.vider.quantum.engine.dto.AutoFillValue;
import com.vider.quantum.engine.dto.MetadataField;
import com.vider.quantum.engine.dto.TemplateDocMetadataDto;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import static com.vider.quantum.engine.constants.QuantumConstants.COMMA;

@Component
public class TotalingAutoFillResolver implements AutoFillResolver {
    @Override
    public AutoFillResolverType resolverType() {
        return AutoFillResolverType.TOTALING;
    }

    @Override
    public void resolveAutoFillValues(TemplateDocMetadataDto templateDocMetadataDto, Map<String, String> data) {
        resolveTotalingAutoFillValues(templateDocMetadataDto, data); // First for children
        resolveTotalingAutoFillValues(templateDocMetadataDto, data);
    }

    private void resolveTotalingAutoFillValues(TemplateDocMetadataDto templateDocMetadataDto, Map<String, String> data) {
        templateDocMetadataDto.getSections().forEach(section -> {
            List<MetadataField> metadataFields = section.getMetadataFields();
            if (CollectionUtils.isNotEmpty(metadataFields)) {
                metadataFields.forEach(field -> {
                    AutoFillValue autoFillValue = field.getAutoFillValue();
                    if (autoFillValue != null) {
                        DecimalFormat df = new DecimalFormat("0.00");
                        df.setRoundingMode(RoundingMode.FLOOR);
                        resolveFieldIfTotalingIsRequired(data, field, autoFillValue, df);
                    }
                });
            }
        });
    }

    private void resolveFieldIfTotalingIsRequired(Map<String, String> docVariableMap,
                                                         MetadataField field,
                                                         AutoFillValue autoFillValue,
                                                         DecimalFormat df) {
        if (autoFillValue.isNeedToFetchTotalOfFields()) {
            double total = 0;
            if (StringUtils.isNotEmpty(autoFillValue.getFieldNames())) {
                String[] split = autoFillValue.getFieldNames().split(COMMA);
                for (String value : split) {
                    if (validNumericValueExistsInVariableMap(docVariableMap, value)) {
                        double valueToAdd = Double.parseDouble(docVariableMap.get(value));
                        total = total + valueToAdd;
                    }
                }
            }
            if (StringUtils.isNotEmpty(autoFillValue.getIndexedFieldNames())) {
                String[] split = autoFillValue.getIndexedFieldNames().split(COMMA);
                for (String value : split) {
                    int i = 1;
                    String newKey = value + i;
                    while (docVariableMap.containsKey(newKey)) {
                        if (validNumericValueExistsInVariableMap(docVariableMap, newKey)) {
                            double valueToAdd = Double.parseDouble(docVariableMap.get(newKey));
                            total = total + valueToAdd;
                        }
                        i++;
                        newKey = value + i;
                    }
                }
            }
            docVariableMap.put(field.getFieldName(), df.format(total));
        }
    }
}
