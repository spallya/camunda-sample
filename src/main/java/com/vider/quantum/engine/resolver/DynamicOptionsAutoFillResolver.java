package com.vider.quantum.engine.resolver;

import com.vider.quantum.engine.dto.AutoFillValue;
import com.vider.quantum.engine.dto.MetadataField;
import com.vider.quantum.engine.dto.TemplateDocMetadataDto;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.vider.quantum.engine.constants.QuantumConstants.COMMA;

@Component
public class DynamicOptionsAutoFillResolver implements AutoFillResolver {
    @Override
    public AutoFillResolverType resolverType() {
        return AutoFillResolverType.DYNAMIC_OPTIONS;
    }

    @Override
    public void resolveAutoFillValues(TemplateDocMetadataDto templateDocMetadataDto, Map<String, String> data) {
        templateDocMetadataDto.getSections().forEach(section -> {
            List<MetadataField> metadataFields = section.getMetadataFields();
            if (CollectionUtils.isNotEmpty(metadataFields)) {
                metadataFields.forEach(field -> {
                    AutoFillValue autoFillValue = field.getAutoFillValue();
                    if (isDynamicOptionRequired(autoFillValue)) {
                        List<String> dynamicOptions = new ArrayList<>();
                        String indexedFieldNames = autoFillValue.getIndexedFieldNames();
                        for (String indexedField : indexedFieldNames.split(COMMA)) {
                            int i = 1;
                            String newKey = indexedField + i;
                            while (data.containsKey(newKey)) {
                                dynamicOptions.add(data.get(newKey));
                                i++;
                                newKey = indexedField + i;
                            }
                        }
                        field.setOptions(dynamicOptions);
                    }
                });
            }
        });
    }

    private static boolean isDynamicOptionRequired(AutoFillValue autoFillValue) {
        return autoFillValue != null && autoFillValue.isPopulateOptionsUsingIndexedFields();
    }
}
