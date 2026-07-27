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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

@Component
public class SuperAdminDashboardController {

    private static final String CARD_STYLE = "-fx-background-color: white; -fx-background-radius: 14; -fx-border-radius: 14;"
            + "-fx-border-color: #d7dee7; -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.08), 12, 0, 0, 5);";

    @FXML
    private StackPane contentArea;

    private final AdminDashboardService dashboardService;
    private final UserService userService;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    public SuperAdminDashboardController(AdminDashboardService dashboardService, UserService userService) {
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
        AdminDashboardService.SuperAdminOverview overview = dashboardService.getSuperAdminOverview();
        List<AdminDashboardService.PaymentSummary> payments = dashboardService.getPaymentSummaries();
        List<AdminDashboardService.FeedbackSummary> feedback = dashboardService.getFeedbackSummaries();

        VBox root = pageRoot();
        root.getChildren().add(sectionTitle("Platform Overview"));

        FlowPane metrics = new FlowPane(16, 16);
        metrics.getChildren().addAll(
                metricCard("Companies", String.valueOf(overview.companyCount()), "Enrolled organizations on the platform"),
                metricCard("Employees", String.valueOf(overview.employeeCount()), "Total active employees across companies"),
                metricCard("Active Projects", String.valueOf(overview.activeProjectCount()), "Planned, in-progress, or maintenance projects"),
                metricCard("Projected Revenue", money(overview.projectedRevenue()), "Based on plan pricing and employee counts"));
        root.getChildren().add(metrics);

        root.getChildren().add(sectionTitle("Upcoming Payments"));
        FlowPane paymentCards = new FlowPane(16, 16);
        payments.stream().limit(4).forEach(payment -> paymentCards.getChildren().add(detailCard(payment.companyName(),
                "Plan: " + payment.planLabel(),
                "Amount: " + money(payment.amount()),
                "Renewal: " + safeDate(payment.renewalDate()))));
        root.getChildren().add(paymentCards);

        root.getChildren().add(sectionTitle("Latest Feedback"));
        FlowPane feedbackCards = new FlowPane(16, 16);
        feedback.stream().limit(3).forEach(item -> feedbackCards.getChildren().add(detailCard(item.companyName(),
                "Reviewer: " + item.reviewerName(),
                "Rating: " + item.rating() + "/5",
                item.comment(),
                "Date: " + safeDate(item.createdAt()))));
        root.getChildren().add(feedbackCards);

        setContent(root);
    }

    @FXML
    private void showCompanies() {
        List<AdminDashboardService.CompanySummary> companies = dashboardService.getCompanySummaries();
        VBox root = pageRoot();
        root.getChildren().add(sectionTitle("Enrolled Companies"));
        FlowPane cards = new FlowPane(16, 16);
        companies.forEach(company -> cards.getChildren().add(detailCard(company.companyName(),
                "Code: " + company.companyCode(),
                "Company admin: " + company.companyAdmin(),
                "Employees: " + company.employeeCount(),
                "Projects: " + company.projectCount(),
                "Clients: " + company.clientCount(),
                "ATS threshold: " + company.atsThreshold() + "%",
                "Plan: " + company.planLabel(),
                "Projected amount: " + money(company.payableAmount()),
                "Active: " + yesNo(company.active()))));
        root.getChildren().add(cards);
        setContent(root);
    }

    @FXML
    private void showCredentials() {
        List<AdminDashboardService.CredentialSummary> credentials = dashboardService.getCredentialSummaries();
        VBox root = pageRoot();
        root.getChildren().add(sectionTitle("Credential Management"));
        FlowPane cards = new FlowPane(16, 16);
        credentials.forEach(item -> cards.getChildren().add(detailCard(item.username(),
                "Name: " + item.fullName(),
                "Role: " + item.role(),
                "Company: " + item.companyName(),
                "Account active: " + yesNo(item.active()),
                "First login pending: " + yesNo(item.firstLogin()),
                "Face enrolled: " + yesNo(item.faceEnrolled()))));
        root.getChildren().add(cards);
        setContent(root);
    }

    @FXML
    private void showPayments() {
        List<AdminDashboardService.PaymentSummary> payments = dashboardService.getPaymentSummaries();
        VBox root = pageRoot();
        root.getChildren().add(sectionTitle("Payments"));
        FlowPane cards = new FlowPane(16, 16);
        payments.forEach(payment -> cards.getChildren().add(detailCard(payment.companyName(),
                "Plan: " + payment.planLabel(),
                "Amount: " + money(payment.amount()),
                "Renewal date: " + safeDate(payment.renewalDate()))));
        root.getChildren().add(cards);
        setContent(root);
    }

    @FXML
    private void showPricing() {
        List<AdminDashboardService.PricingSummary> plans = dashboardService.getPricingCatalog();
        VBox root = pageRoot();
        root.getChildren().add(sectionTitle("Pricing Modules"));
        FlowPane cards = new FlowPane(16, 16);
        plans.forEach(plan -> cards.getChildren().add(detailCard(plan.planLabel(),
                plan.description(),
                plan.pricingRule())));
        root.getChildren().add(cards);
        setContent(root);
    }

    @FXML
    private void showFeedback() {
        List<AdminDashboardService.FeedbackSummary> feedback = dashboardService.getFeedbackSummaries();
        VBox root = pageRoot();
        root.getChildren().add(sectionTitle("Review Feedback Management"));
        FlowPane cards = new FlowPane(16, 16);
        feedback.forEach(item -> cards.getChildren().add(detailCard(item.companyName(),
                "Reviewer: " + item.reviewerName(),
                "Rating: " + item.rating() + "/5",
                item.comment(),
                "Date: " + safeDate(item.createdAt()))));
        root.getChildren().add(cards);
        setContent(root);
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
        card.setPrefWidth(250);
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
