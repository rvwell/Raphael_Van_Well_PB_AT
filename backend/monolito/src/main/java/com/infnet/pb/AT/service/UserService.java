package com.infnet.pb.AT.service;

import com.infnet.pb.AT.model.User;
import com.infnet.pb.AT.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Optional<User> findOptionalByEmail(String email) {
        return userRepository.findByEmail(email) != null
                ? Optional.of(userRepository.findByEmail(email))
                : Optional.empty();
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public void deleteUser(String id) {
        userRepository.deleteById(UUID.fromString(id));
    }

    public User updateName(UUID id, String name) {
        User user = userRepository.findById(id).orElseThrow();
        user.setName(name);
        return userRepository.save(user);
    }

    public boolean changePassword(UUID id, String current, String next) {
        User user = userRepository.findById(id).orElseThrow();
        if (!passwordEncoder.matches(current, user.getPassword())) {
            return false;
        }
        user.setPassword(passwordEncoder.encode(next));
        userRepository.save(user);
        return true;
    }
}
