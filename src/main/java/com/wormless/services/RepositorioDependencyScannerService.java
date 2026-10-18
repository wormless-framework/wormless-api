package com.wormless.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.wormless.dto.request.DependencyScanRequestDTO;
import com.wormless.dto.response.DependencyScanResponseDTO;
import com.wormless.dto.response.DependencyScanResponseDTO.Finding;
import com.wormless.dto.response.DependencyScanResponseDTO.Vulnerability;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

/** Converts an OSV-Scanner CycloneDX report into the Wormless API response. */
@Service
public class RepositorioDependencyScannerService {

    private static final int MAX_COMPONENTS = 10_000;
    private static final int MAX_VULNERABILITIES = 10_000;

    public DependencyScanResponseDTO scan(DependencyScanRequestDTO request) {
        List<String> warnings = request.scannerWarnings() == null
                ? new ArrayList<>() : new ArrayList<>(request.scannerWarnings());
        JsonNode report = request.scanReport();
        JsonNode components = report.path("components");
        JsonNode advisories = report.path("vulnerabilities");

        if (!"CycloneDX".equals(report.path("bomFormat").asText()) || !components.isArray()) {
            warnings.add("O scanner não retornou um SBOM CycloneDX válido.");
            return response(request, 0, 0, false, warnings, List.of());
        }
        if (components.size() > MAX_COMPONENTS || advisories.isArray()
                && advisories.size() > MAX_VULNERABILITIES) {
            warnings.add("O SBOM excede os limites aceitos pela API Wormless.");
            return response(request, components.size(), 0, false, warnings, List.of());
        }

        Map<String, Component> componentsByReference = new LinkedHashMap<>();
        for (JsonNode component : components) {
            String reference = component.path("bom-ref").asText(component.path("purl").asText(""));
            String name = component.path("name").asText("");
            String group = component.path("group").asText("");
            String version = component.path("version").asText("");
            if (reference.isBlank() || name.isBlank() || version.isBlank()) {
                warnings.add("O SBOM contém um componente sem referência, nome ou versão.");
                continue;
            }
            String packageName = group.isBlank() ? name : group + ":" + name;
            componentsByReference.put(reference, new Component(packageName, version));
        }

        Map<Component, Set<Vulnerability>> findingsByComponent = new LinkedHashMap<>();
        boolean vulnerabilitiesFound = advisories.isArray() && !advisories.isEmpty();
        if (vulnerabilitiesFound) {
            for (JsonNode advisory : advisories) {
                String identifier = advisory.path("id").asText("");
                if (identifier.isBlank()) {
                    warnings.add("O SBOM contém um aviso sem identificador.");
                    continue;
                }
                List<String> aliases = readAliases(advisory);
                String summary = advisory.path("description").asText(
                        advisory.path("detail").asText(""));
                Vulnerability vulnerability = new Vulnerability(identifier, aliases, summary);
                JsonNode affects = advisory.path("affects");
                if (!affects.isArray() || affects.isEmpty()) {
                    warnings.add("O aviso " + identifier + " não identifica os pacotes afetados.");
                    continue;
                }
                for (JsonNode affected : affects) {
                    String reference = affected.path("ref").asText("");
                    Component component = componentsByReference.get(reference);
                    if (component == null) {
                        warnings.add("O SBOM não relaciona o aviso " + identifier
                                + " a um componente conhecido.");
                        continue;
                    }
                    findingsByComponent.computeIfAbsent(component, ignored -> new LinkedHashSet<>())
                            .add(vulnerability);
                }
            }
        } else if (!advisories.isMissingNode() && !advisories.isArray()) {
            warnings.add("O SBOM contém uma lista de vulnerabilidades inválida.");
        }

        List<Finding> findings = findingsByComponent.entrySet().stream()
                .map(entry -> new Finding(entry.getKey().packageName(), entry.getKey().version(),
                        "Resolved Maven dependency graph", List.copyOf(entry.getValue())))
                .toList();
        return response(request, components.size(), findingsByComponent.size(),
                vulnerabilitiesFound, warnings, findings);
    }

    private static List<String> readAliases(JsonNode advisory) {
        Set<String> aliases = new LinkedHashSet<>();
        JsonNode references = advisory.path("references");
        if (references.isArray()) {
            references.forEach(reference -> {
                String identifier = reference.path("id").asText("");
                if (!identifier.isBlank()) {
                    aliases.add(identifier);
                }
            });
        }
        return List.copyOf(aliases);
    }

    private static DependencyScanResponseDTO response(
            DependencyScanRequestDTO request,
            int scanned,
            int vulnerable,
            boolean vulnerabilitiesFound,
            List<String> warnings,
            List<Finding> findings) {
        return new DependencyScanResponseDTO(
                request.repository(), request.ref(), scanned, vulnerable,
                vulnerabilitiesFound, warnings.isEmpty(), findings, warnings);
    }

    private record Component(String packageName, String version) {
    }
}
