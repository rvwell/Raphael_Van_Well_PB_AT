package com.infnet.pb.AT.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    private String email;
    private String name;
    private String password;
    private String role; // Agora aceita role como string (ex: "ADMIN", "USER")
}
