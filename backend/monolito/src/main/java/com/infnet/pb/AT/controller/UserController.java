package com.infnet.pb.AT.controller;

import com.infnet.pb.AT.model.User;
import com.infnet.pb.AT.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public User registerUser(@RequestParam String name, @RequestParam String password) {
        return userService.registerUser(name, password);
    }
}
