package com.vider.quantum.engine.helper;

import com.vider.quantum.engine.dto.TemplateDocMetadataDto;
import com.vider.quantum.engine.resolver.AutoFillResolverType;
import com.vider.quantum.engine.util.DiscoveryUtil;

import java.util.List;
import java.util.Map;

public class AutoFillResolverHelper {

    private AutoFillResolverHelper() {

    }

    public static void executeResolvers(List<AutoFillResolverType> resolverTypes,
                                        TemplateDocMetadataDto templateDocMetadataDto,
                                        Map<String, String> data) {
        resolverTypes.forEach(resolverType ->
                DiscoveryUtil.discoverAutoFillResolver(resolverType)
                        .ifPresent(resolver -> resolver.resolveAutoFillValues(templateDocMetadataDto, data)));
    }

}
