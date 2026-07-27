package com.smartdesk.controllers;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Component;

import com.smartdesk.service.AdminDashboardService;
import com.smartdesk.service.UserService;
import com.smartdesk.util.ViewNavigator;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

@Component
public class CompanyDashboardController {

    private static final String CARD_STYLE = "-fx-background-color: white; -fx-background-radius: 14; -fx-border-radius: 14;"
            + "-fx-border-color: #dbe2ea; -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.08), 12, 0, 0, 5);";

    @FXML
    private StackPane contentArea;

    private final AdminDashboardService dashboardService;
    private final UserService userService;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    public CompanyDashboardController(AdminDashboardService dashboardService, UserService userService) {
        this.dashboardService = dashboardService;
        this.userService = userService;
    }

    @FXML
    public void initialize() {
        showOverview();
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        userService.logout();
        try {
            ViewNavigator.showLogin(currentStage(event));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void refreshPage(ActionEvent event) {
        try {
            ViewNavigator.showDashboard(currentStage(event), userService.getCurrentUser());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showOverview() {
        try {
            AdminDashboardService.CompanyAdminOverview overview = dashboardService.getCompanyAdminOverview(userService.getCurrentUser());
            AdminDashboardService.SubscriptionSummary subscription = dashboardService.getCompanySubscription(userService.getCurrentUser());
            List<AdminDashboardService.FeedbackSummary> feedback = dashboardService.getCompanyFeedback(userService.getCurrentUser());

            VBox root = pageRoot();
            root.getChildren().add(sectionTitle("Company Snapshot"));

            FlowPane metrics = new FlowPane(16, 16);
            metrics.getChildren().addAll(
                    metricCard("Employees", String.valueOf(overview.employeeCount()), "Active team members in this company"),
                    metricCard("Projects", String.valueOf(overview.projectCount()), "Projects visible to company admin"),
                    metricCard("Clients", String.valueOf(overview.clientCount()), "Tracked client accounts"),
                    metricCard("ATS Threshold", overview.atsThreshold() + "%", "Company-specific hiring threshold"));
            root.getChildren().add(metrics);

            HBox lower = new HBox(16,
                    detailCard("Company Configuration",
                            "Company: " + overview.companyName(),
                            "Code: " + overview.companyCode(),
                            "Plan: " + overview.planLabel()),
                    detailCard("Billing Snapshot",
                            "Projected amount: " + money(overview.payableAmount()),
                            "Renewal: " + safeDate(subscription.renewalDate()),
                            "Contact: " + subscription.contactEmail()));
            HBox.setHgrow(lower.getChildren().get(0), Priority.ALWAYS);
            HBox.setHgrow(lower.getChildren().get(1), Priority.ALWAYS);
            root.getChildren().add(lower);

            root.getChildren().add(sectionTitle("Recent Feedback"));
            if (feedback.isEmpty()) {
                root.getChildren().add(emptyState("No feedback has been recorded for this company yet."));
            } else {
                FlowPane feedbackPane = new FlowPane(16, 16);
                feedback.stream().limit(3).forEach(item -> feedbackPane.getChildren().add(
                        detailCard(item.reviewerName(),
                                "Rating: " + item.rating() + "/5",
                                item.comment(),
                                "Received: " + safeDate(item.createdAt()))));
                root.getChildren().add(feedbackPane);
            }

            setContent(root);
        } catch (Exception ex) {
            setContent(errorState(ex.getMessage()));
        }
    }

    @FXML
    private void showEmployees() {
        try {
            List<AdminDashboardService.EmployeeSummary> employees = dashboardService.getCompanyEmployees(userService.getCurrentUser());
            VBox root = pageRoot();
            root.getChildren().add(sectionTitle("Employees And Admins"));

            if (employees.isEmpty()) {
                root.getChildren().add(emptyState("No users are assigned to this company yet."));
            } else {
                FlowPane cards = new FlowPane(16, 16);
                employees.forEach(employee -> cards.getChildren().add(detailCard(employee.fullName(),
                        "Username: " + employee.username(),
                        "Role: " + employee.role(),
                        "Department: " + employee.department(),
                        "Email: " + employee.email(),
                        "Face login: " + yesNo(employee.faceEnrolled()) + " | First login pending: " + yesNo(employee.firstLogin()))));
                root.getChildren().add(cards);
            }

            setContent(root);
        } catch (Exception ex) {
            setContent(errorState(ex.getMessage()));
        }
    }

    @FXML
    private void showProjects() {
        try {
            List<AdminDashboardService.ProjectSummary> projects = dashboardService.getCompanyProjects(userService.getCurrentUser());
            VBox root = pageRoot();
            root.getChildren().add(sectionTitle("Projects"));

            if (projects.isEmpty()) {
                root.getChildren().add(emptyState("No projects are assigned to this company yet."));
            } else {
                FlowPane cards = new FlowPane(16, 16);
                projects.forEach(project -> cards.getChildren().add(detailCard(project.projectName(),
                        "Client: " + project.clientName(),
                        "Status: " + project.status(),
                        "Team size: " + project.teamSize())));
                root.getChildren().add(cards);
            }

            setContent(root);
        } catch (Exception ex) {
            setContent(errorState(ex.getMessage()));
        }
    }

    @FXML
    private void showAccessRights() {
        try {
            List<AdminDashboardService.AccessSummary> accessRows = dashboardService.getCompanyAccessSummaries(userService.getCurrentUser());
            VBox root = pageRoot();
            root.getChildren().add(sectionTitle("Access Rights"));

            FlowPane cards = new FlowPane(16, 16);
            accessRows.forEach(row -> cards.getChildren().add(detailCard(row.username(),
                    "Role: " + row.role(),
                    "Scope: " + row.scope(),
                    "Department: " + row.department(),
                    "Face enrolled: " + yesNo(row.faceEnrolled()),
                    "Needs onboarding: " + yesNo(row.firstLogin()))));
            root.getChildren().add(cards);

            setContent(root);
        } catch (Exception ex) {
            setContent(errorState(ex.getMessage()));
        }
    }

    @FXML
    private void showSubscription() {
        try {
            AdminDashboardService.SubscriptionSummary subscription = dashboardService.getCompanySubscription(userService.getCurrentUser());
            VBox root = pageRoot();
            root.getChildren().add(sectionTitle("Subscription And Enrollment"));
            root.getChildren().add(detailCard(subscription.companyName(),
                    "Plan: " + subscription.planLabel(),
                    "Projected amount: " + money(subscription.payableAmount()),
                    "ATS threshold: " + subscription.atsThreshold() + "%",
                    "Renewal date: " + safeDate(subscription.renewalDate()),
                    "Primary contact: " + subscription.contactEmail()));
            setContent(root);
        } catch (Exception ex) {
            setContent(errorState(ex.getMessage()));
        }
    }

    @FXML
    private void showReviews() {
        try {
            List<AdminDashboardService.FeedbackSummary> feedback = dashboardService.getCompanyFeedback(userService.getCurrentUser());
            VBox root = pageRoot();
            root.getChildren().add(sectionTitle("Review Feedback"));
            if (feedback.isEmpty()) {
                root.getChildren().add(emptyState("No review feedback is stored yet."));
            } else {
                FlowPane cards = new FlowPane(16, 16);
                feedback.forEach(item -> cards.getChildren().add(detailCard(item.reviewerName(),
                        "Rating: " + item.rating() + "/5",
                        item.comment(),
                        "Date: " + safeDate(item.createdAt()))));
                root.getChildren().add(cards);
            }
            setContent(root);
        } catch (Exception ex) {
            setContent(errorState(ex.getMessage()));
        }
    }

    private VBox pageRoot() {
        VBox root = new VBox(18);
        root.setPadding(new Insets(22));
        return root;
    }

    private Label sectionTitle(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #102a43;");
        return label;
    }

    private VBox metricCard(String title, String value, String description) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #52606d;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        Label descriptionLabel = new Label(description);
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle("-fx-text-fill: #52606d;");

        VBox card = new VBox(10, titleLabel, valueLabel, descriptionLabel);
        card.setPadding(new Insets(18));
        card.setPrefWidth(240);
        card.setStyle(CARD_STYLE);
        return card;
    }

    private VBox detailCard(String title, String... lines) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #102a43;");

        VBox card = new VBox(8, titleLabel);
        for (String line : lines) {
            Label lineLabel = new Label(line);
            lineLabel.setWrapText(true);
            lineLabel.setStyle("-fx-text-fill: #334e68;");
            card.getChildren().add(lineLabel);
        }
        card.setPadding(new Insets(18));
        card.setPrefWidth(320);
        card.setStyle(CARD_STYLE);
        return card;
    }

    private VBox emptyState(String message) {
        return detailCard("Nothing here yet", message);
    }

    private VBox errorState(String message) {
        return detailCard("Access issue", message != null ? message : "Unable to load this section.");
    }

    private void setContent(Node node) {
        ScrollPane scrollPane = new ScrollPane(node);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        contentArea.getChildren().setAll(scrollPane);
    }

    private Stage currentStage(ActionEvent event) {
        return (Stage) ((Node) event.getSource()).getScene().getWindow();
    }

    private String money(BigDecimal amount) {
        return currencyFormat.format(amount != null ? amount : BigDecimal.ZERO);
    }

    private String safeDate(Object value) {
        return value != null ? value.toString() : "Not scheduled";
    }

    private String yesNo(boolean value) {
        return value ? "Yes" : "No";
    }
}
