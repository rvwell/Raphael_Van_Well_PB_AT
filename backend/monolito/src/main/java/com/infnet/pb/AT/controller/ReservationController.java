package com.infnet.pb.AT.controller;

import com.infnet.pb.AT.model.Reservation;
import com.infnet.pb.AT.service.ReservationService;
import com.infnet.pb.AT.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.infnet.pb.AT.DTO.CreateReservationRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private UserService userService;

    // Create reservation for current user
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createReservation(Authentication authentication, @RequestBody CreateReservationRequest req) {
        if (req == null || req.getResourceId() == null || req.getStart() == null || req.getEnd() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing required fields");
        }
        UUID userId = userService.findOptionalByEmail(authentication.getName())
                .map(u -> u.getId())
                .orElse(null);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        LocalDateTime startTime;
        LocalDateTime endTime;
        try {
            startTime = LocalDateTime.parse(req.getStart());
            endTime = LocalDateTime.parse(req.getEnd());
        } catch (DateTimeParseException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid date format. Use ISO-8601, e.g. 2025-09-25T10:00:00");
        }
        if (!endTime.isAfter(startTime)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("end must be after start");
        }
        try {
            Reservation saved = reservationService.createReservation(userId, req.getResourceId(), startTime, endTime);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
        }
    }

    // List all reservations - ADMIN only
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Reservation>> listAll() {
        return ResponseEntity.ok(reservationService.findAll());
    }

    // Get reservation by id - only owner or ADMIN
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Reservation> getById(Authentication authentication, @PathVariable String id) {
        UUID uuid = UUID.fromString(id);
        Optional<Reservation> opt = reservationService.findById(uuid);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        Reservation r = opt.get();
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        UUID currentUserId = userService.findOptionalByEmail(authentication.getName()).map(u -> u.getId()).orElse(null);
        if (isAdmin || (currentUserId != null && r.getUser() != null && r.getUser().getId().equals(currentUserId))) {
            return ResponseEntity.ok(r);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    // List current user's reservations
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Reservation>> myReservations(Authentication authentication) {
        UUID userId = userService.findOptionalByEmail(authentication.getName())
                .map(u -> u.getId())
                .orElse(null);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(reservationService.getUserReservations(userId));
    }

    // Cancel reservation - only owner or ADMIN
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> cancelReservation(Authentication authentication, @PathVariable String id) {
        UUID uuid = UUID.fromString(id);
        Optional<Reservation> opt = reservationService.findById(uuid);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        Reservation r = opt.get();
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        UUID currentUserId = userService.findOptionalByEmail(authentication.getName()).map(u -> u.getId()).orElse(null);
        if (!isAdmin && (currentUserId == null || r.getUser() == null || !r.getUser().getId().equals(currentUserId))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        reservationService.cancelReservation(uuid);
        return ResponseEntity.noContent().build();
    }

}
