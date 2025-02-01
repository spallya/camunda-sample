package com.vider.quantum.engine.configuration;

import com.vider.quantum.engine.resolver.AutoFillResolver;
import com.vider.quantum.engine.resolver.AutoFillResolverType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class DiscoveryConfig {

    private final List<AutoFillResolver> autoFillResolvers;

    @Bean(name = "autoFillResolverLookup")
    public Map<AutoFillResolverType, AutoFillResolver> autoFillResolverLookup() {
        Map<AutoFillResolverType, AutoFillResolver> autoFillResolverLookup = new HashMap<>();
        autoFillResolvers.forEach(autoFillResolver -> {
            if (!autoFillResolverLookup.containsKey(autoFillResolver.resolverType())) {
                autoFillResolverLookup.put(autoFillResolver.resolverType(), autoFillResolver);
            } else {
                throw new IllegalStateException("One Auto Fill Resolver should be mapped to one resolver type."
                        + autoFillResolver.resolverType());
            }
        });
        return autoFillResolverLookup;
    }
}
