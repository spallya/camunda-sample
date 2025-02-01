package com.vider.quantum.engine.util;

import com.vider.quantum.engine.configuration.ApplicationContextProvider;
import com.vider.quantum.engine.resolver.AutoFillResolver;
import com.vider.quantum.engine.resolver.AutoFillResolverType;

import java.util.Map;
import java.util.Optional;

public class DiscoveryUtil {

    private DiscoveryUtil() {

    }

    public static Optional<AutoFillResolver> discoverAutoFillResolver(AutoFillResolverType resolverType) {
        ApplicationContextProvider.getApplicationContext().getBean("autoFillResolverLookup");
        Map<AutoFillResolverType, AutoFillResolver> autoFillResolverLookup =
                (Map<AutoFillResolverType, AutoFillResolver>) ApplicationContextProvider.getApplicationContext().getBean("autoFillResolverLookup");
        return Optional.ofNullable(autoFillResolverLookup.getOrDefault(resolverType, null));
    }
}
