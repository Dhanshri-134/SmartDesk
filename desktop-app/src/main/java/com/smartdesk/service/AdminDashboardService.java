package com.smartdesk.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

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

@Service
public class AdminDashboardService {

    public record SuperAdminOverview(long companyCount, long employeeCount, long activeProjectCount,
                                     BigDecimal projectedRevenue) {
    }

    public record CompanySummary(String companyName, String companyCode, int atsThreshold, long employeeCount,
                                 long projectCount, int clientCount, String companyAdmin, String planLabel,
                                 BigDecimal payableAmount, boolean active) {
    }

    public record CredentialSummary(String username, String fullName, String role, String companyName, boolean active,
                                    boolean firstLogin, boolean faceEnrolled) {
    }

    public record PaymentSummary(String companyName, String planLabel, BigDecimal amount, LocalDate renewalDate) {
    }

    public record PricingSummary(String planLabel, String description, String pricingRule) {
    }

    public record FeedbackSummary(String companyName, String reviewerName, int rating, String comment,
                                  LocalDate createdAt) {
    }

    public record CompanyAdminOverview(String companyName, String companyCode, int atsThreshold, long employeeCount,
                                       long projectCount, int clientCount, BigDecimal payableAmount, String planLabel) {
    }

    public record EmployeeSummary(String fullName, String username, String role, String department, String email,
                                  boolean faceEnrolled, boolean firstLogin) {
    }

    public record ProjectSummary(String projectName, String clientName, String status, int teamSize) {
    }

    public record AccessSummary(String username, String role, String department, String scope,
                                boolean faceEnrolled, boolean firstLogin) {
    }

    public record SubscriptionSummary(String companyName, String planLabel, BigDecimal payableAmount, int atsThreshold,
                                      LocalDate renewalDate, String contactEmail) {
    }

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ReviewFeedbackRepository reviewFeedbackRepository;

    public AdminDashboardService(CompanyRepository companyRepository, UserRepository userRepository,
                                 ProjectRepository projectRepository, ReviewFeedbackRepository reviewFeedbackRepository) {
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.reviewFeedbackRepository = reviewFeedbackRepository;
    }

    public SuperAdminOverview getSuperAdminOverview() {
        long companyCount = companyRepository.count();
        long employeeCount = userRepository.countByRole(UserRole.EMPLOYEE);
        long activeProjectCount = projectRepository.countByStatusIn(
                List.of(ProjectStatus.PLANNED, ProjectStatus.IN_PROGRESS, ProjectStatus.MAINTENANCE));
        BigDecimal projectedRevenue = companyRepository.findAll().stream()
                .map(this::calculateCompanyBilling)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new SuperAdminOverview(companyCount, employeeCount, activeProjectCount, projectedRevenue);
    }

    public List<CompanySummary> getCompanySummaries() {
        return companyRepository.findAll().stream()
                .sorted(Comparator.comparing(Company::getName))
                .map(company -> new CompanySummary(
                        company.getName(),
                        company.getCompanyCode(),
                        safeInt(company.getAtsThreshold()),
                        userRepository.countByCompanyAndRole(company, UserRole.EMPLOYEE),
                        projectRepository.countByCompany(company),
                        safeInt(company.getClientCount()),
                        userRepository.findFirstByCompanyAndRole(company, UserRole.COMPANY_ADMIN)
                                .map(User::getUsername)
                                .orElse("Not assigned"),
                        company.getSubscriptionPlan() != null ? company.getSubscriptionPlan().getLabel() : "Not set",
                        calculateCompanyBilling(company),
                        company.isActive()))
                .toList();
    }

    public List<CredentialSummary> getCredentialSummaries() {
        return userRepository.findAll().stream()
                .sorted(Comparator
                        .comparing((User user) -> companyName(user.getCompany()))
                        .thenComparing(user -> user.getRole() != null ? user.getRole().name() : "")
                        .thenComparing(User::getUsername))
                .map(user -> new CredentialSummary(
                        user.getUsername(),
                        blankFallback(user.getFullName(), "Profile pending"),
                        user.getRole() != null ? user.getRole().getLabel() : "Unknown",
                        companyName(user.getCompany()),
                        user.isActive(),
                        user.isFirstLogin(),
                        user.isFaceEnrolled()))
                .toList();
    }

    public List<PaymentSummary> getPaymentSummaries() {
        return companyRepository.findAll().stream()
                .sorted(Comparator.comparing(Company::getName))
                .map(company -> new PaymentSummary(
                        company.getName(),
                        company.getSubscriptionPlan() != null ? company.getSubscriptionPlan().getLabel() : "Not set",
                        calculateCompanyBilling(company),
                        company.getRenewalDate()))
                .toList();
    }

    public List<PricingSummary> getPricingCatalog() {
        return List.of(
                new PricingSummary(SubscriptionPlan.PER_EMPLOYEE.getLabel(),
                        SubscriptionPlan.PER_EMPLOYEE.getDescription(),
                        "Recommended for growing teams. Billing = active employees x configured rate."),
                new PricingSummary(SubscriptionPlan.YEARLY.getLabel(),
                        SubscriptionPlan.YEARLY.getDescription(),
                        "Flat annual contract. Useful when company size is stable for 12 months."),
                new PricingSummary(SubscriptionPlan.TWO_YEAR.getLabel(),
                        SubscriptionPlan.TWO_YEAR.getDescription(),
                        "Discounted long-term contract with locked pricing for 24 months."));
    }

    public List<FeedbackSummary> getFeedbackSummaries() {
        return reviewFeedbackRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toFeedbackSummary)
                .toList();
    }

    public CompanyAdminOverview getCompanyAdminOverview(User user) {
        Company company = requireCompany(user);
        return new CompanyAdminOverview(
                company.getName(),
                company.getCompanyCode(),
                safeInt(company.getAtsThreshold()),
                userRepository.countByCompanyAndRole(company, UserRole.EMPLOYEE),
                projectRepository.countByCompany(company),
                safeInt(company.getClientCount()),
                calculateCompanyBilling(company),
                company.getSubscriptionPlan() != null ? company.getSubscriptionPlan().getLabel() : "Not set");
    }

    public List<EmployeeSummary> getCompanyEmployees(User user) {
        Company company = requireCompany(user);
        return userRepository.findAllByCompanyOrderByRoleAscUsernameAsc(company).stream()
                .map(companyUser -> new EmployeeSummary(
                        blankFallback(companyUser.getFullName(), companyUser.getUsername()),
                        companyUser.getUsername(),
                        companyUser.getRole() != null ? companyUser.getRole().getLabel() : "Unknown",
                        blankFallback(companyUser.getDepartment(), "Not assigned"),
                        blankFallback(companyUser.getEmail(), "Not added"),
                        companyUser.isFaceEnrolled(),
                        companyUser.isFirstLogin()))
                .toList();
    }

    public List<ProjectSummary> getCompanyProjects(User user) {
        Company company = requireCompany(user);
        return projectRepository.findAllByCompanyOrderByNameAsc(company).stream()
                .map(project -> new ProjectSummary(
                        project.getName(),
                        blankFallback(project.getClientName(), "Internal"),
                        project.getStatus() != null ? project.getStatus().getLabel() : "Unknown",
                        safeInt(project.getTeamSize())))
                .toList();
    }

    public List<AccessSummary> getCompanyAccessSummaries(User user) {
        Company company = requireCompany(user);
        return userRepository.findAllByCompanyOrderByRoleAscUsernameAsc(company).stream()
                .map(companyUser -> new AccessSummary(
                        companyUser.getUsername(),
                        companyUser.getRole() != null ? companyUser.getRole().getLabel() : "Unknown",
                        blankFallback(companyUser.getDepartment(), "Not assigned"),
                        companyUser.getRole() == UserRole.COMPANY_ADMIN
                                ? "Full company access"
                                : "Assigned project and collaboration access",
                        companyUser.isFaceEnrolled(),
                        companyUser.isFirstLogin()))
                .toList();
    }

    public SubscriptionSummary getCompanySubscription(User user) {
        Company company = requireCompany(user);
        return new SubscriptionSummary(
                company.getName(),
                company.getSubscriptionPlan() != null ? company.getSubscriptionPlan().getLabel() : "Not set",
                calculateCompanyBilling(company),
                safeInt(company.getAtsThreshold()),
                company.getRenewalDate(),
                blankFallback(company.getContactEmail(), "Not added"));
    }

    public List<FeedbackSummary> getCompanyFeedback(User user) {
        Company company = requireCompany(user);
        return reviewFeedbackRepository.findAllByCompanyOrderByCreatedAtDesc(company).stream()
                .map(this::toFeedbackSummary)
                .toList();
    }

    private FeedbackSummary toFeedbackSummary(ReviewFeedback feedback) {
        return new FeedbackSummary(
                feedback.getCompany() != null ? feedback.getCompany().getName() : "Platform",
                blankFallback(feedback.getReviewerName(), "Anonymous"),
                safeInt(feedback.getRating()),
                blankFallback(feedback.getComment(), "No comment"),
                feedback.getCreatedAt());
    }

    private Company requireCompany(User user) {
        if (user == null || user.getCompany() == null) {
            throw new IllegalStateException("Current user is not assigned to a company.");
        }
        return user.getCompany();
    }

    public BigDecimal calculateCompanyBilling(Company company) {
        long employeeCount = userRepository.countByCompanyAndRole(company, UserRole.EMPLOYEE);
        BigDecimal amount;
        if (company.getSubscriptionPlan() == SubscriptionPlan.PER_EMPLOYEE) {
            amount = safeMoney(company.getPricePerEmployee()).multiply(BigDecimal.valueOf(employeeCount));
        } else {
            amount = safeMoney(company.getPlanPrice());
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal safeMoney(BigDecimal amount) {
        return amount != null ? amount : BigDecimal.ZERO;
    }

    private int safeInt(Integer value) {
        return value != null ? value : 0;
    }

    private String companyName(Company company) {
        return company != null ? company.getName() : "Platform";
    }

    private String blankFallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
