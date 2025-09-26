package com.infnet.pb.AT.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infnet.pb.AT.DTO.LoginRequest;
import com.infnet.pb.AT.DTO.RegisterRequest;
import com.infnet.pb.AT.DTO.TokenResponse;
import com.infnet.pb.AT.model.RoleType;
import com.infnet.pb.AT.model.User;
import com.infnet.pb.AT.security.AuthService;
import com.infnet.pb.AT.security.JwtService;
import com.infnet.pb.AT.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void login_WithValidCredentials_ShouldReturnTokenResponse() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("user@example.com", "password123");
        String expectedToken = "jwt-token-123";
        
        when(authService.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken(authentication)).thenReturn(expectedToken);

        // Act
        ResponseEntity<TokenResponse> response = authController.login(loginRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedToken, response.getBody().getAccessToken());
        assertEquals("Bearer", response.getBody().getTokenType());

        verify(authService).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(authentication);
    }

    @Test
    void login_WithInvalidCredentials_ShouldThrowBadCredentialsException() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("user@example.com", "wrongpassword");
        
        when(authService.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> {
            authController.login(loginRequest);
        });

        verify(authService).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void register_WithNewUser_ShouldReturnCreatedUser() {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest(
                "newuser@example.com", 
                "New User", 
                "password123", 
                Set.of(RoleType.USER)
        );
        
        User savedUser = User.builder()
                .id(UUID.randomUUID())
                .email("newuser@example.com")
                .name("New User")
                .password("encoded-password")
                .roles(Set.of(RoleType.USER))
                .build();

        when(userService.findOptionalByEmail("newuser@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userService.save(any(User.class))).thenReturn(savedUser);

        // Act
        ResponseEntity<User> response = authController.register(registerRequest);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("newuser@example.com", response.getBody().getEmail());
        assertEquals("New User", response.getBody().getName());
        assertEquals("encoded-password", response.getBody().getPassword());

        verify(userService).findOptionalByEmail("newuser@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userService).save(any(User.class));
    }

    @Test
    void register_WithExistingEmail_ShouldReturnConflict() {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest(
                "existing@example.com", 
                "Existing User", 
                "password123", 
                Set.of()
        );
        
        User existingUser = User.builder()
                .id(UUID.randomUUID())
                .email("existing@example.com")
                .name("Existing User")
                .password("encoded-password")
                .build();

        when(userService.findOptionalByEmail("existing@example.com"))
                .thenReturn(Optional.of(existingUser));

        // Act
        ResponseEntity<User> response = authController.register(registerRequest);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNull(response.getBody());

        verify(userService).findOptionalByEmail("existing@example.com");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userService, never()).save(any(User.class));
    }

    @Test
    void login_WithMockMvc_ShouldReturnTokenResponse() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("user@example.com", "password123");
        String expectedToken = "jwt-token-123";
        
        when(authService.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken(authentication)).thenReturn(expectedToken);

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").value(expectedToken))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void register_WithMockMvc_ShouldReturnCreatedUser() throws Exception {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest(
                "newuser@example.com", 
                "New User", 
                "password123", 
                Set.of(RoleType.USER)
        );
        
        User savedUser = User.builder()
                .id(UUID.randomUUID())
                .email("newuser@example.com")
                .name("New User")
                .password("encoded-password")
                .roles(Set.of(RoleType.USER))
                .build();

        when(userService.findOptionalByEmail("newuser@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userService.save(any(User.class))).thenReturn(savedUser);

        // Act & Assert
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.name").value("New User"));
    }

    @Test
    void register_WithExistingEmailMockMvc_ShouldReturnConflict() throws Exception {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest(
                "existing@example.com", 
                "Existing User", 
                "password123", 
                Set.of()
        );
        
        User existingUser = User.builder()
                .id(UUID.randomUUID())
                .email("existing@example.com")
                .name("Existing User")
                .password("encoded-password")
                .build();

        when(userService.findOptionalByEmail("existing@example.com"))
                .thenReturn(Optional.of(existingUser));

        // Act & Assert
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict());
    }
}