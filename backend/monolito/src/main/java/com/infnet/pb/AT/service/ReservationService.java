package com.infnet.pb.AT.service;


import com.infnet.pb.AT.model.Reservation;
import com.infnet.pb.AT.model.Resource;
import com.infnet.pb.AT.model.User;
import com.infnet.pb.AT.repository.ReservationRepository;
import com.infnet.pb.AT.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ResourceRepository resourceRepository;

    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    public Optional<Reservation> findById(UUID id) {
        return reservationRepository.findById(id);
    }

    @Transactional
    public Reservation createReservation(UUID userId, UUID resourceId, LocalDateTime start, LocalDateTime end) {
        List<Reservation> conflicts = reservationRepository.findByResourceIdAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(resourceId, end, start);
        if (!conflicts.isEmpty()) {
            throw new RuntimeException("Resource is already booked for this time slot.");
        }

        Reservation reservation = Reservation.builder()
                .user(User.builder().id(userId).build())
                .resource(Resource.builder().id(resourceId).build())
                .startTime(start)
                .endTime(end)
                .status("ACTIVE")
                .build();

        return reservationRepository.save(reservation);
    }

    public List<Reservation> getUserReservations(UUID userId) {
        return reservationRepository.findByUserId(userId);
    }

    public void cancelReservation(UUID reservationId) {
        reservationRepository.deleteById(reservationId);
    }
}
