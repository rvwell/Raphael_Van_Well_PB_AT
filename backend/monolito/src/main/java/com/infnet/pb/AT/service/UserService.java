package com.infnet.pb.AT.service;

import com.infnet.pb.AT.model.User;
import com.infnet.pb.AT.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User registerUser(String username, String password) {
        User user = new User();
        user.setName(username);
        user.setPassword(password);
        return userRepository.save(user);
    }
}
