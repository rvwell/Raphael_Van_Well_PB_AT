package com.infnet.pb.AT.DTO;

import com.infnet.pb.AT.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    private String email;
    private String name;
    private String password;
    private Set<Role> roles;
}
