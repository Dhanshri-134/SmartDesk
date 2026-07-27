package com.smartdesk.controllers;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.smartdesk.service.UserService;
import com.smartdesk.util.ViewNavigator;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

@Component
public class EmployeeDashboardController {

    @FXML
    private StackPane contentArea;

    @FXML
    private Button meetingBtn;

    private final UserService userService;

    public EmployeeDashboardController(UserService userService) {
        this.userService = userService;
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
    public void initialize() {
    }

    @FXML
    private void handleMeetingBtn() {
        try {
            new com.smartdesk.meeting.MeetingPopup().show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleProfileBtn() throws IOException {
        Node node = ViewNavigator.load("/fxml/EmployeeProfile.fxml");
        contentArea.getChildren().setAll(node);
    }

    private Stage currentStage(ActionEvent event) {
        return (Stage) ((Node) event.getSource()).getScene().getWindow();
    }
}
