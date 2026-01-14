package com.studentassistant.service;

import com.studentassistant.model.User;
import com.studentassistant.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository repo;

    public User saveUser(User user) {
        return repo.save(user);
    }

    public User getByEmail(String email) {
        return repo.findByEmail(email);
    }

    public boolean existsByEmail(String email) {
        return repo.existsById(email);
    }

    public Optional<User> findById(String email) {
        return repo.findById(email);
    }
}
