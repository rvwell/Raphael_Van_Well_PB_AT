package com.infnet.pb.AT.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infnet.pb.AT.DTO.CreateReservationRequest;
import com.infnet.pb.AT.model.Reservation;
import com.infnet.pb.AT.model.Resource;
import com.infnet.pb.AT.model.User;
import com.infnet.pb.AT.service.ReservationService;
import com.infnet.pb.AT.service.UserService;
import com.infnet.pb.AT.config.TestSecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservationController.class)
@Import(TestSecurityConfig.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID userId;
    private UUID resourceId;
    private UUID reservationId;
    private User mockUser;
    private Resource mockResource;
    private Reservation mockReservation;
    private CreateReservationRequest createRequest;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        resourceId = UUID.randomUUID();
        reservationId = UUID.randomUUID();

        mockUser = User.builder()
                .id(userId)
                .email("user@test.com")
                .name("Test User")
                .build();

        mockResource = Resource.builder()
                .id(resourceId)
                .name("Test Resource")
                .description("Test Description")
                .build();

        mockReservation = Reservation.builder()
                .id(reservationId)
                .user(mockUser)
                .resource(mockResource)
                .startTime(LocalDateTime.of(2025, 1, 15, 10, 0))
                .endTime(LocalDateTime.of(2025, 1, 15, 12, 0))
                .status("ACTIVE")
                .build();

        createRequest = new CreateReservationRequest(
                resourceId,
                "2025-01-15T10:00:00",
                "2025-01-15T12:00:00"
        );
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = {"USER"})
    void createReservation_Success() throws Exception {
        when(userService.findOptionalByEmail("user@test.com")).thenReturn(Optional.of(mockUser));
        when(reservationService.createReservation(eq(userId), eq(resourceId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(mockReservation);

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(reservationId.toString()))
                .andExpect(jsonPath("$.user.email").value("user@test.com"))
                .andExpect(jsonPath("$.resource.name").value("Test Resource"));

        verify(reservationService).createReservation(eq(userId), eq(resourceId), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = {"USER"})
    void createReservation_MissingFields() throws Exception {
        CreateReservationRequest invalidRequest = new CreateReservationRequest(null, null, null);

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Missing required fields"));

        verify(reservationService, never()).createReservation(any(), any(), any(), any());
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = {"USER"})
    void createReservation_UserNotFound() throws Exception {
        when(userService.findOptionalByEmail("user@test.com")).thenReturn(Optional.empty());

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isNotFound());

        verify(reservationService, never()).createReservation(any(), any(), any(), any());
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = {"USER"})
    void createReservation_InvalidDateFormat() throws Exception {
        CreateReservationRequest invalidDateRequest = new CreateReservationRequest(
                resourceId, "invalid-date", "2025-01-15T12:00:00"
        );

        when(userService.findOptionalByEmail("user@test.com")).thenReturn(Optional.of(mockUser));

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid date format. Use ISO-8601, e.g. 2025-09-25T10:00:00"));

        verify(reservationService, never()).createReservation(any(), any(), any(), any());
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = {"USER"})
    void createReservation_EndBeforeStart() throws Exception {
        CreateReservationRequest invalidTimeRequest = new CreateReservationRequest(
                resourceId, "2025-01-15T12:00:00", "2025-01-15T10:00:00"
        );

        when(userService.findOptionalByEmail("user@test.com")).thenReturn(Optional.of(mockUser));

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTimeRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("end must be after start"));

        verify(reservationService, never()).createReservation(any(), any(), any(), any());
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = {"USER"})
    void createReservation_ResourceConflict() throws Exception {
        when(userService.findOptionalByEmail("user@test.com")).thenReturn(Optional.of(mockUser));
        when(reservationService.createReservation(eq(userId), eq(resourceId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenThrow(new RuntimeException("Resource is already booked for this time slot."));

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isConflict())
                .andExpect(content().string("Resource is already booked for this time slot."));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listAll_AdminAccess() throws Exception {
        List<Reservation> reservations = List.of(mockReservation);
        when(reservationService.findAll()).thenReturn(reservations);

        mockMvc.perform(get("/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(reservationId.toString()));

        verify(reservationService).findAll();
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = {"USER"})
    void getById_OwnerAccess() throws Exception {
        when(reservationService.findById(reservationId)).thenReturn(Optional.of(mockReservation));
        when(userService.findOptionalByEmail("user@test.com")).thenReturn(Optional.of(mockUser));

        mockMvc.perform(get("/reservations/{id}", reservationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reservationId.toString()))
                .andExpect(jsonPath("$.user.email").value("user@test.com"));

        verify(reservationService).findById(reservationId);
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
    void getById_AdminAccess() throws Exception {
        User adminUser = User.builder()
                .id(UUID.randomUUID())
                .email("admin@test.com")
                .name("Admin User")
                .build();

        when(reservationService.findById(reservationId)).thenReturn(Optional.of(mockReservation));
        when(userService.findOptionalByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));

        mockMvc.perform(get("/reservations/{id}", reservationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reservationId.toString()));

        verify(reservationService).findById(reservationId);
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = {"USER"})
    void getById_NotFound() throws Exception {
        when(reservationService.findById(reservationId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/reservations/{id}", reservationId))
                .andExpect(status().isNotFound());

        verify(reservationService).findById(reservationId);
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = {"USER"})
    void getById_Forbidden() throws Exception {
        User otherUser = User.builder()
                .id(UUID.randomUUID())
                .email("other@test.com")
                .name("Other User")
                .build();

        Reservation otherReservation = Reservation.builder()
                .id(reservationId)
                .user(otherUser)
                .resource(mockResource)
                .startTime(LocalDateTime.of(2025, 1, 15, 10, 0))
                .endTime(LocalDateTime.of(2025, 1, 15, 12, 0))
                .status("ACTIVE")
                .build();

        when(reservationService.findById(reservationId)).thenReturn(Optional.of(otherReservation));
        when(userService.findOptionalByEmail("user@test.com")).thenReturn(Optional.of(mockUser));

        mockMvc.perform(get("/reservations/{id}", reservationId))
                .andExpect(status().isForbidden());

        verify(reservationService).findById(reservationId);
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = {"USER"})
    void myReservations_Success() throws Exception {
        List<Reservation> userReservations = List.of(mockReservation);
        when(userService.findOptionalByEmail("user@test.com")).thenReturn(Optional.of(mockUser));
        when(reservationService.getUserReservations(userId)).thenReturn(userReservations);

        mockMvc.perform(get("/reservations/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(reservationId.toString()));

        verify(reservationService).getUserReservations(userId);
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = {"USER"})
    void myReservations_UserNotFound() throws Exception {
        when(userService.findOptionalByEmail("user@test.com")).thenReturn(Optional.empty());

        mockMvc.perform(get("/reservations/me"))
                .andExpect(status().isNotFound());

        verify(reservationService, never()).getUserReservations(any());
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = {"USER"})
    void cancelReservation_OwnerAccess() throws Exception {
        when(reservationService.findById(reservationId)).thenReturn(Optional.of(mockReservation));
        when(userService.findOptionalByEmail("user@test.com")).thenReturn(Optional.of(mockUser));

        mockMvc.perform(delete("/reservations/{id}", reservationId))
                .andExpect(status().isNoContent());

        verify(reservationService).cancelReservation(reservationId);
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
    void cancelReservation_AdminAccess() throws Exception {
        User adminUser = User.builder()
                .id(UUID.randomUUID())
                .email("admin@test.com")
                .name("Admin User")
                .build();

        when(reservationService.findById(reservationId)).thenReturn(Optional.of(mockReservation));
        when(userService.findOptionalByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));

        mockMvc.perform(delete("/reservations/{id}", reservationId))
                .andExpect(status().isNoContent());

        verify(reservationService).cancelReservation(reservationId);
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = {"USER"})
    void cancelReservation_NotFound() throws Exception {
        when(reservationService.findById(reservationId)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/reservations/{id}", reservationId))
                .andExpect(status().isNotFound());

        verify(reservationService, never()).cancelReservation(any());
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = {"USER"})
    void cancelReservation_Forbidden() throws Exception {
        User otherUser = User.builder()
                .id(UUID.randomUUID())
                .email("other@test.com")
                .name("Other User")
                .build();

        Reservation otherReservation = Reservation.builder()
                .id(reservationId)
                .user(otherUser)
                .resource(mockResource)
                .startTime(LocalDateTime.of(2025, 1, 15, 10, 0))
                .endTime(LocalDateTime.of(2025, 1, 15, 12, 0))
                .status("ACTIVE")
                .build();

        when(reservationService.findById(reservationId)).thenReturn(Optional.of(otherReservation));
        when(userService.findOptionalByEmail("user@test.com")).thenReturn(Optional.of(mockUser));

        mockMvc.perform(delete("/reservations/{id}", reservationId))
                .andExpect(status().isForbidden());

        verify(reservationService, never()).cancelReservation(any());
    }
}