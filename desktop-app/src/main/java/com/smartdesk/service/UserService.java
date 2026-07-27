package com.smartdesk.service;

import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.smartdesk.model.User;
import com.smartdesk.model.UserRole;
import com.smartdesk.repository.UserRepository;
import com.smartdesk.service.face.FaceAuthService;

@Service
public class UserService {

    private final UserRepository repo;
    private final FaceAuthService faceAuthService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private User currentUser;

    public UserService(UserRepository repo, FaceAuthService faceAuthService) {
        this.repo = repo;
        this.faceAuthService = faceAuthService;
    }

    public User authenticate(String username, String rawPassword) {
        Optional<User> result = repo.findByUsername(username);
        if (result.isEmpty()) {
            return null;
        }

        User user = result.get();
        if (!user.isActive()) {
            return null;
        }

        String storedHash = user.getPasswordHash();
        boolean matched = storedHash != null && encoder.matches(rawPassword, storedHash);

        if (!matched && storedHash != null && storedHash.equals(rawPassword)) {
            user.setPasswordHash(encoder.encode(rawPassword));
            repo.save(user);
            matched = true;
        }

        if (!matched) {
            return null;
        }

        if (user.getRole() == null) {
            user.setRole(UserRole.EMPLOYEE);
            repo.save(user);
        }

        syncFaceEnrollmentState(user);
        currentUser = user;
        return user;
    }

    public User findByUsername(String username) {
        return repo.findByUsername(username).orElse(null);
    }

    public User getCurrentUser() {
        if (currentUser == null) {
            return null;
        }
        currentUser = repo.findByUsername(currentUser.getUsername()).orElse(currentUser);
        syncFaceEnrollmentState(currentUser);
        return currentUser;
    }

    public String getCurrentUsername() {
        User user = getCurrentUser();
        return user != null ? user.getUsername() : null;
    }

    public boolean needsProfileCompletion(User user) {
        if (user == null) {
            return false;
        }
        boolean employeeFaceMissing = isEmployee(user) && !syncFaceEnrollmentState(user);
        return user.isFirstLogin()
                || isBlank(user.getFullName())
                || isBlank(user.getEmail())
                || isBlank(user.getDepartment())
                || employeeFaceMissing;
    }

    public void saveProfileDraft(String name, String email, String dept) {
        User current = getCurrentUser();
        if (current == null) {
            return;
        }
        current.setFullName(name);
        current.setEmail(email);
        current.setDepartment(dept);
        repo.save(current);
        currentUser = current;
    }

    public void completeProfile(String name, String email, String dept) {
        User current = getCurrentUser();
        if (current == null) {
            return;
        }
        saveProfileDraft(name, email, dept);
        current.setFirstLogin(false);
        repo.save(current);
        currentUser = current;
    }

    public void completeEmployeeOnboarding(String name, String email, String dept) {
        User current = getCurrentUser();
        if (current == null) {
            return;
        }
        saveProfileDraft(name, email, dept);
        current.setFaceEnrolled(true);
        current.setFirstLogin(false);
        repo.save(current);
        currentUser = current;
    }

    public void markFaceEnrolled() {
        User current = getCurrentUser();
        if (current == null) {
            return;
        }
        current.setFaceEnrolled(true);
        repo.save(current);
        currentUser = current;
    }

    public boolean isEmployee(User user) {
        return user != null && user.getRole() == UserRole.EMPLOYEE;
    }

    public boolean requiresFaceVerification(User user) {
        return isEmployee(user) && !needsProfileCompletion(user);
    }

    public void logout() {
        currentUser = null;
    }

    private boolean syncFaceEnrollmentState(User user) {
        if (!isEmployee(user)) {
            return user != null && user.isFaceEnrolled();
        }

        boolean enrolled = faceAuthService.hasCompleteEnrollment(user.getUsername());
        if (user.isFaceEnrolled() != enrolled) {
            user.setFaceEnrolled(enrolled);
            repo.save(user);
        }
        return enrolled;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
