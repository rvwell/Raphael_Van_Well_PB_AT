package com.infnet.pb.AT.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "reservations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {
    @Id
    @GeneratedValue
    private UUID id;


    @ManyToOne(optional = false)
    @JoinColumn(name = "resource_id")
    private Resource resource;


    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;


    @Column(nullable = false)
    private LocalDateTime startTime;


    @Column(nullable = false)
    private LocalDateTime endTime;


    @Column(nullable = false)
    @Builder.Default
    private String status = "ACTIVE";
}
