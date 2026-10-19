package com.wormless.security;

import com.wormless.dto.request.DependencyScanRequestDTO;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class GitHubActionsOidcService {

    private static final String ISSUER = "https://token.actions.githubusercontent.com";

    private final JwtDecoder jwtDecoder;
    private final String audience;
    private final Set<String> allowedRepositories;

    public GitHubActionsOidcService(
            @Value("${wormless.security.github-oidc.audience:api://wormless}") String audience,
            @Value("${wormless.security.github-oidc.allowed-repositories:}") String allowedRepositories) {
        this.audience = audience;
        this.allowedRepositories = Arrays.stream(allowedRepositories.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(String::toLowerCase)
                .collect(Collectors.toUnmodifiableSet());

        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withJwkSetUri(ISSUER + "/.well-known/jwks")
                .build();
        OAuth2TokenValidator<Jwt> audienceValidator = jwt -> jwt.getAudience().contains(audience)
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(
                        new OAuth2Error("invalid_token", "Audience OIDC inválida.", null));
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(ISSUER), audienceValidator));
        this.jwtDecoder = decoder;
    }

    public void validate(String bearerToken, DependencyScanRequestDTO request) {
        if (allowedRepositories.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Nenhum repositório foi autorizado para usar a Action Wormless.");
        }
        Jwt jwt;
        try {
            jwt = jwtDecoder.decode(bearerToken);
        } catch (JwtException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Token OIDC do GitHub Actions inválido ou expirado.");
        }

        String repository = jwt.getClaimAsString("repository");
        String ref = jwt.getClaimAsString("ref");
        String sha = jwt.getClaimAsString("sha");
        String eventName = jwt.getClaimAsString("event_name");
        if (repository == null || !allowedRepositories.contains(repository.toLowerCase())
                || !repository.equalsIgnoreCase(request.repository())
                || ref == null || !ref.equals(request.ref())
                || sha == null || !sha.equalsIgnoreCase(request.commitSha())
                || eventName == null
                || !Set.of("push", "pull_request", "workflow_dispatch").contains(eventName)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "O token OIDC não está autorizado para este repositório, referência ou evento.");
        }
    }
}
