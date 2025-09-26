package com.infnet.pb.AT.controller;

import com.infnet.pb.AT.model.RoleType;
import com.infnet.pb.AT.model.User;
import com.infnet.pb.AT.security.AuthService;
import com.infnet.pb.AT.security.JwtService;
import com.infnet.pb.AT.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.infnet.pb.AT.DTO.LoginRequest;
import com.infnet.pb.AT.DTO.RegisterRequest;
import com.infnet.pb.AT.DTO.TokenResponse;

import java.util.Set;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthService authService, JwtService jwtService, UserService userService, PasswordEncoder passwordEncoder) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
        Authentication auth = authService.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        String token = jwtService.generateToken(auth);
        return ResponseEntity.ok(new TokenResponse(token, "Bearer"));
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody RegisterRequest request) {
        if (userService.findOptionalByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        
        // Converte a string do role para RoleType
        RoleType roleType;
        try {
            roleType = RoleType.fromString(request.getRole());
        } catch (IllegalArgumentException e) {
            // Se o role não for válido, retorna erro 400
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        
        User user = User.builder()
                .email(request.getEmail())
                .name(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(roleType)) // Converte para Set<RoleType>
                .build();
        User saved = userService.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

}
