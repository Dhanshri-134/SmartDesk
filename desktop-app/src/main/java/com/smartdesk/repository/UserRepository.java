package com.smartdesk.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartdesk.model.Company;
import com.smartdesk.model.User;
import com.smartdesk.model.UserRole;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findFirstByCompanyAndRole(Company company, UserRole role);
    List<User> findAllByCompanyOrderByRoleAscUsernameAsc(Company company);
    long countByRole(UserRole role);
    long countByCompanyAndRole(Company company, UserRole role);
}
