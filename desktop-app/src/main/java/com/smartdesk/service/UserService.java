package com.smartdesk.service;

import com.smartdesk.model.User;
import com.smartdesk.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository repo;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private String currentUsername = null;

    public UserService(UserRepository repo) {
        this.repo = repo;
        // Seed a demo user (username: demo, password: demo)
        if (repo.findByUsername("demo").isEmpty()) {
            User u = new User();
            u.setUsername("demo");
            u.setPasswordHash(encoder.encode("demo"));
            u.setFirstLogin(true);
            repo.save(u);
        }
    }

    public User authenticate(String username, String rawPassword) {
        Optional<User> u = repo.findByUsername(username);
        if (u.isPresent() && encoder.matches(rawPassword, u.get().getPasswordHash())) {
            currentUsername = username;
            return u.get();
        }
        return null;
    }

    public User findByUsername(String username) {
        return repo.findByUsername(username).orElse(null);
    }

    public String getCurrentUsername() { return currentUsername; }

    public void completeProfile(String name, String email, String dept) {
        if (currentUsername == null) return;
        User u = repo.findByUsername(currentUsername).orElseThrow();
        u.setFullName(name);
        u.setEmail(email);
        u.setDepartment(dept);
        u.setFirstLogin(false);
        repo.save(u);
    }
}
