package com.wormless.dto.response;

import java.util.List;

public record DependencyScanResponseDTO(
        String repository,
        String ref,
        int dependenciesScanned,
        int vulnerableDependencies,
        boolean vulnerabilitiesFound,
        boolean analysisComplete,
        List<Finding> findings,
        List<String> warnings) {

    public record Finding(
            String packageName,
            String version,
            String manifest,
            List<Vulnerability> vulnerabilities) {
    }

    public record Vulnerability(
            String identifier,
            List<String> aliases,
            String summary) {
    }
}
