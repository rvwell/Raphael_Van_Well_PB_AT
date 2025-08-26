package com.infnet.pb.AT.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "resources")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resource {
    @Id
    @GeneratedValue
    private UUID id;


    @Column(nullable = false)
    private String name;


    private String description;
    private String location;
    private Integer capacity;
}
