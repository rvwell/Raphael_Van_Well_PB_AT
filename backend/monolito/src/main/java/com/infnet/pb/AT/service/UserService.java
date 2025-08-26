package com.infnet.pb.AT.service;

import com.infnet.pb.AT.model.User;
import com.infnet.pb.AT.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;


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
}
