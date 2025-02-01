package com.vider.quantum.engine.resolver;

import com.vider.quantum.engine.dto.TemplateDocMetadataDto;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Map;

import static com.vider.quantum.engine.service.VariableDocumentService.MASKED_VALUE;

public interface AutoFillResolver {

    AutoFillResolverType resolverType();

    void resolveAutoFillValues(TemplateDocMetadataDto templateDocMetadataDto, Map<String, String> data);

    default boolean validNumericValueExistsInVariableMap(Map<String, String> docVariableMap,
                                                                String key) {
        return StringUtils.isNotEmpty(key)
                && docVariableMap.containsKey(key)
                && StringUtils.isNotEmpty(docVariableMap.get(key))
                && !MASKED_VALUE.equalsIgnoreCase(docVariableMap.get(key))
                && NumberUtils.isCreatable(docVariableMap.get(key));
    }

}
