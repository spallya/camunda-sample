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

@Component
public class ConversionAutoFillResolver implements AutoFillResolver {
    @Override
    public AutoFillResolverType resolverType() {
        return AutoFillResolverType.CONVERSION;
    }

    @Override
    public void resolveAutoFillValues(TemplateDocMetadataDto templateDocMetadataDto, Map<String, String> data) {
        templateDocMetadataDto.getSections().forEach(section -> {
            List<MetadataField> metadataFields = section.getMetadataFields();
            if (CollectionUtils.isNotEmpty(metadataFields)) {
                metadataFields.forEach(field -> {
                    AutoFillValue autoFillValue = field.getAutoFillValue();
                    if (isConversionRequired(autoFillValue)) {
                        String copyValueFrom = autoFillValue.getCopyValueFromIndexed();
                        if (copyValueFromIsPresent(data, copyValueFrom)) {
                            DecimalFormat df = new DecimalFormat("0.00");
                            df.setRoundingMode(RoundingMode.FLOOR);
                            resolveFieldIfConversionIsRequired(data, field, autoFillValue, df);
                            if (data.containsKey(field.getFieldName())) {
                                field.setFieldValue(data.get(field.getFieldName()));
                            }
                        }
                    }
                });
            }
        });
    }

    private static boolean copyValueFromIsPresent(Map<String, String> data, String copyValueFrom) {
        return StringUtils.isNotEmpty(copyValueFrom)
                && data.containsKey(copyValueFrom);
    }

    private static boolean isConversionRequired(AutoFillValue autoFillValue) {
        return autoFillValue != null && autoFillValue.isConversionRequired();
    }

    private void resolveFieldIfConversionIsRequired(Map<String, String> docVariableMap,
                                                    MetadataField field,
                                                    AutoFillValue autoFillValue,
                                                    DecimalFormat df) {
        if (autoFillValue.isConversionRequired()) {
            double convertedValue;
            String copyValueFrom = autoFillValue.getCopyValueFromIndexed();
            String conversionRateFrom = autoFillValue.getDynamicConversionRateFromMetaDataField();
            if (validNumericValueExistsInVariableMap(docVariableMap, copyValueFrom)
            ) {
                convertedValue = Double.parseDouble(docVariableMap.get(copyValueFrom));
                if (validNumericValueExistsInVariableMap(docVariableMap, conversionRateFrom)
                ) {
                    double conversionRate = Double.parseDouble(docVariableMap.get(conversionRateFrom));
                    convertedValue = convertedValue / conversionRate;
                }
                docVariableMap.put(field.getFieldName(), df.format(convertedValue));
            }
        }
    }
}
