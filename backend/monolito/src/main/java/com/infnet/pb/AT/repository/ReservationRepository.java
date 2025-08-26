package com.infnet.pb.AT.repository;

import com.infnet.pb.AT.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
    List<Reservation> findByResourceIdAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(UUID resourceId, LocalDateTime end, LocalDateTime start);
    List<Reservation> findByUserId(UUID userId);
}
