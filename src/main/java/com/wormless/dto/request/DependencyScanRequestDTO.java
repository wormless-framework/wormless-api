package com.wormless.dto.request;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public record DependencyScanRequestDTO(
        @NotBlank String repository,
        @NotBlank String ref,
        @NotBlank @Pattern(regexp = "[a-fA-F0-9]{40}|[a-fA-F0-9]{64}") String commitSha,
        @NotNull JsonNode scanReport,
        @Size(max = 100) List<@NotBlank String> scannerWarnings) {
}
