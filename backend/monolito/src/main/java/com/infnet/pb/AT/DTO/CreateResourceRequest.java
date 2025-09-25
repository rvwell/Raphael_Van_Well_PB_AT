package com.infnet.pb.AT.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateResourceRequest {
    private String name;
    private String description;
    private String location;
    private Integer capacity;
}
