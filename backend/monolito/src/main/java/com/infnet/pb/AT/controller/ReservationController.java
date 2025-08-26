package com.infnet.pb.AT.controller;

import com.infnet.pb.AT.model.Reservation;
import com.infnet.pb.AT.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;


    @PostMapping
    public Reservation createReservation(@RequestParam UUID userId, @RequestParam UUID resourceId, @RequestParam String start, @RequestParam String end) {
        LocalDateTime startTime = LocalDateTime.parse(start);
        LocalDateTime endTime = LocalDateTime.parse(end);
        return reservationService.createReservation(userId, resourceId, startTime, endTime);
    }


    @GetMapping
    public List<Reservation> getUserReservations(@RequestParam UUID userId) {
        return reservationService.getUserReservations(userId);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelReservation(@PathVariable UUID id) {
        reservationService.cancelReservation(id);
        return ResponseEntity.noContent().build();
    }
}
