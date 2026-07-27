package com.smartdesk.controllers;

import java.io.IOException;

import org.springframework.stereotype.Controller;

import com.smartdesk.model.User;
import com.smartdesk.service.UserService;
import com.smartdesk.util.ViewNavigator;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

@Controller
public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label statusLabel;

    private final UserService userService;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @FXML
    private void onLogin() {
        String username = usernameField.getText() != null ? usernameField.getText().trim() : "";
        String password = passwordField.getText() != null ? passwordField.getText() : "";

        if (username.isBlank() || password.isBlank()) {
            statusLabel.setText("Enter both username and password.");
            return;
        }

        User user = userService.authenticate(username, password);
        if (user == null) {
            statusLabel.setText("Invalid username, password, or inactive user.");
            return;
        }

        try {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            if (userService.needsProfileCompletion(user)) {
                ViewNavigator.showProfile(stage);
                return;
            }

            if (userService.requiresFaceVerification(user)
                    && !ViewNavigator.showFaceVerificationDialog(stage, userService.getCurrentUser())) {
                userService.logout();
                statusLabel.setText("Face authentication failed. Dashboard access denied.");
                return;
            }

            ViewNavigator.showDashboard(stage, userService.getCurrentUser());
        } catch (IOException e) {
            statusLabel.setText("Error opening dashboard.");
        }
    }
}
