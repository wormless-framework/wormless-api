package com.wormless.security;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

record RegisterRequest(String email, String password, Role role) {
}

record LoginRequest(String email, String password) {
}

record LoginResponse(String token, String role) {
}

@RestController
@RequestMapping("/api/auth")
class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    ResponseEntity<Void> register(@RequestBody RegisterRequest request) {
        if (request.email() == null || request.password() == null || request.role() == null) {
            throw new IllegalArgumentException("email, password e role sao obrigatorios");
        }

        User user = new User(request.email(), passwordEncoder.encode(request.password()), request.role());
        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("email ou senha invalidos"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("email ou senha invalidos");
        }

        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(new LoginResponse(token, user.getRole().name()));
    }
}
