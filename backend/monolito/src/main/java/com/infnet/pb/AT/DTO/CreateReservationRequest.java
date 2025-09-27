package com.infnet.pb.AT.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateReservationRequest {
    private UUID resourceId;
    private String start;
    private String end;
}
