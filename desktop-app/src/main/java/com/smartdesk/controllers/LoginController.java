package com.smartdesk.controllers;

import com.smartdesk.model.User;
//import com.smartdesk.service.FaceService;
import com.smartdesk.service.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;

    private final UserService userService;
//    private final FaceService faceService;

    public LoginController(UserService userService){//   , FaceService faceService) {
        this.userService = userService;
//        this.faceService = faceService;
    }

    @FXML
    public void onLogin() {
        String u = usernameField.getText();
        String p = passwordField.getText();
        User user = userService.authenticate(u, p);

        if (user == null) {
            statusLabel.setText("Invalid credentials.");
            return;
        }

        if (user.isFirstLogin()) {
            goToProfile(user);
        } else {
            goToDashboard();
        }
    }

    @FXML
    public void onFaceLogin() {
        String u = usernameField.getText();
        if (u == null || u.isBlank()) {
            statusLabel.setText("Enter username first for face login.");
            return;
        }
//        boolean ok = faceService.verifyFace(u);
//        if (!ok) {
//            statusLabel.setText("Face verification failed.");
//            return;
//        }
        User user = userService.findByUsername(u);
        if (user == null) {
            statusLabel.setText("User not found.");
            return;
        }
        if (user.isFirstLogin()) {
            goToProfile(user);
        } else {
            goToDashboard();
        }
    }

    private void goToProfile(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Profile.fxml"));
            loader.setControllerFactory(((Stage) statusLabel.getScene().getWindow()).getProperties()::get);
            Parent root = loader.load();
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            stage.setTitle("Complete Profile");
            stage.setScene(new Scene(root, 600, 520));
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Failed to load profile.");
        }
    }

    private void goToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Profile.fxml"));
            Parent root = loader.load(); // Placeholder: reuse Profile as next screen stub
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            stage.setTitle("SmartDesk - Dashboard (stub)");
            stage.setScene(new Scene(root, 800, 600));
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Failed to load dashboard.");
        }
    }
}

