package com.example.auth_service.service.impl;

import com.example.auth_service.model.User;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;

    @Override
    public void deactivateUser(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        user.ifPresent(u -> {
            u.setActive(false);
            userRepository.save(u);
        });
    }
}
