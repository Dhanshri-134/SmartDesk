package com.smartdesk.config;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.smartdesk.model.Company;
import com.smartdesk.model.Project;
import com.smartdesk.model.ProjectStatus;
import com.smartdesk.model.ReviewFeedback;
import com.smartdesk.model.SubscriptionPlan;
import com.smartdesk.model.User;
import com.smartdesk.model.UserRole;
import com.smartdesk.repository.CompanyRepository;
import com.smartdesk.repository.ProjectRepository;
import com.smartdesk.repository.ReviewFeedbackRepository;
import com.smartdesk.repository.UserRepository;

@Component
public class DataInitializer {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final ProjectRepository projectRepository;
    private final ReviewFeedbackRepository reviewFeedbackRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public DataInitializer(UserRepository userRepository, CompanyRepository companyRepository,
                           ProjectRepository projectRepository, ReviewFeedbackRepository reviewFeedbackRepository) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.projectRepository = projectRepository;
        this.reviewFeedbackRepository = reviewFeedbackRepository;
    }

    @PostConstruct
    public void init() {
        Company nexora = upsertCompany("Nexora Systems", "NEXORA", "admin@nexora.com", 60, 9,
                SubscriptionPlan.YEARLY, BigDecimal.valueOf(240000), BigDecimal.ZERO, 1,
                LocalDate.now().plusMonths(11), true);
        Company vertex = upsertCompany("Vertex Digital", "VERTEX", "ops@vertex.com", 72, 6,
                SubscriptionPlan.PER_EMPLOYEE, BigDecimal.ZERO, BigDecimal.valueOf(2800), 1,
                LocalDate.now().plusMonths(6), true);

        upsertUser("superadmin", "super123", "Platform Superadmin", "platform@smartdesk.com", "Platform Ops",
                "Platform Owner", UserRole.SUPERADMIN, null, true, true, true);
        upsertUser("admin", "admin123", "Legacy Superadmin", "legacy@smartdesk.com", "Platform Ops",
                "Platform Owner", UserRole.SUPERADMIN, null, false, true, true);

        upsertUser("nexora.admin", "Nexora@123", "Aditi Kulkarni", "aditi@nexora.com", "Administration",
                "Company Admin", UserRole.COMPANY_ADMIN, nexora, false, true, true);
        upsertUser("vertex.admin", "Vertex@123", "Rahul Mehta", "rahul@vertex.com", "Operations",
                "Company Admin", UserRole.COMPANY_ADMIN, vertex, false, true, true);

        upsertUser("demo", "demo", "Tanuja Lambote", "tanuja@nexora.com", "Software Engineering",
                "Developer", UserRole.EMPLOYEE, nexora, false, true, true);
        upsertUser("dev.aarav", "Aarav@123", "Aarav Sharma", "aarav@nexora.com", "Engineering",
                "Backend Developer", UserRole.EMPLOYEE, nexora, false, true, true);
        upsertUser("dev.priya", "Priya@123", "Priya Singh", "priya@nexora.com", "QA",
                "QA Engineer", UserRole.EMPLOYEE, nexora, false, false, true);
        upsertUser("dev.milan", "Milan@123", "Milan Desai", "milan@vertex.com", "Product",
                "Frontend Developer", UserRole.EMPLOYEE, vertex, false, true, true);
        upsertUser("dev.sara", "Sara@123", "Sara Khan", "sara@vertex.com", "Engineering",
                "ML Engineer", UserRole.EMPLOYEE, vertex, true, false, true);

        seedProjects(nexora, vertex);
        seedFeedback(nexora, vertex);
    }

    private Company upsertCompany(String name, String code, String email, int atsThreshold, int clientCount,
                                  SubscriptionPlan plan, BigDecimal planPrice, BigDecimal perEmployeePrice,
                                  int contractYears, LocalDate renewalDate, boolean active) {
        Company company = companyRepository.findByCompanyCode(code).orElseGet(Company::new);
        company.setName(name);
        company.setCompanyCode(code);
        company.setContactEmail(email);
        company.setAtsThreshold(atsThreshold);
        company.setClientCount(clientCount);
        company.setSubscriptionPlan(plan);
        company.setPlanPrice(planPrice);
        company.setPricePerEmployee(perEmployeePrice);
        company.setContractYears(contractYears);
        company.setRenewalDate(renewalDate);
        company.setActive(active);
        return companyRepository.save(company);
    }

    private void upsertUser(String username, String rawPassword, String fullName, String email, String department,
                            String designation, UserRole role, Company company, boolean firstLogin,
                            boolean faceEnrolled, boolean active) {
        User user = userRepository.findByUsername(username).orElseGet(User::new);
        user.setUsername(username);

        String storedHash = user.getPasswordHash();
        if (storedHash == null || !storedHash.startsWith("$2a$") && !storedHash.startsWith("$2b$")) {
            user.setPasswordHash(encoder.encode(rawPassword));
        }

        user.setFullName(fullName);
        user.setEmail(email);
        user.setDepartment(department);
        user.setDesignation(designation);
        user.setRole(role);
        user.setCompany(company);
        user.setFirstLogin(firstLogin);
        user.setFaceEnrolled(faceEnrolled);
        user.setActive(active);
        userRepository.save(user);
    }

    private void seedProjects(Company nexora, Company vertex) {
        if (projectRepository.count() > 0) {
            return;
        }

        projectRepository.save(createProject(nexora, "SmartDesk Hiring Suite", "GreenOrbit Labs",
                ProjectStatus.IN_PROGRESS, 4));
        projectRepository.save(createProject(nexora, "Attendance Insight Portal", "MetroFin",
                ProjectStatus.MAINTENANCE, 3));
        projectRepository.save(createProject(nexora, "Dev Productivity Tracker", "Internal",
                ProjectStatus.PLANNED, 2));

        projectRepository.save(createProject(vertex, "Retail Forecast Engine", "BlueCart",
                ProjectStatus.IN_PROGRESS, 5));
        projectRepository.save(createProject(vertex, "Voice Meeting Assistant", "DocuBridge",
                ProjectStatus.BLOCKED, 3));
        projectRepository.save(createProject(vertex, "Client Success Dashboard", "Vertex Internal",
                ProjectStatus.COMPLETED, 2));
    }

    private Project createProject(Company company, String name, String client, ProjectStatus status, int teamSize) {
        Project project = new Project();
        project.setCompany(company);
        project.setName(name);
        project.setClientName(client);
        project.setStatus(status);
        project.setTeamSize(teamSize);
        return project;
    }

    private void seedFeedback(Company nexora, Company vertex) {
        if (reviewFeedbackRepository.count() > 0) {
            return;
        }

        reviewFeedbackRepository.save(createFeedback(nexora, "Nexora HR Team", 5,
                "ATS threshold and admin reporting saved our screening time.", LocalDate.now().minusDays(18)));
        reviewFeedbackRepository.save(createFeedback(nexora, "Finance Lead", 4,
                "Yearly billing visibility is clear, but we still need automated invoice export.", LocalDate.now().minusDays(7)));
        reviewFeedbackRepository.save(createFeedback(vertex, "Vertex Delivery Team", 4,
                "Company admin controls are helping with employee onboarding and project visibility.", LocalDate.now().minusDays(11)));
    }

    private ReviewFeedback createFeedback(Company company, String reviewer, int rating, String comment, LocalDate date) {
        ReviewFeedback feedback = new ReviewFeedback();
        feedback.setCompany(company);
        feedback.setReviewerName(reviewer);
        feedback.setRating(rating);
        feedback.setComment(comment);
        feedback.setCreatedAt(date);
        return feedback;
    }
}
