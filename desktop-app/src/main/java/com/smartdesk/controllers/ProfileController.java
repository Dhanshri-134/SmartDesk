package com.smartdesk.controllers;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.smartdesk.model.User;
import com.smartdesk.service.UserService;
import com.smartdesk.util.ViewNavigator;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

@Component
public class ProfileController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField deptField;
    @FXML private Label statusLabel;

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @FXML
    public void initialize() {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return;
        }
        nameField.setText(currentUser.getFullName() != null ? currentUser.getFullName() : "");
        emailField.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");
        deptField.setText(currentUser.getDepartment() != null ? currentUser.getDepartment() : "");
        statusLabel.setText(currentUser.isFaceEnrolled()
                ? "Face registration is already completed. Save your profile to continue."
                : "Fill the profile, then complete mandatory face registration before dashboard access.");
    }

    @FXML
    public void onRegisterFace() {
        if (!profileFieldsAreValid()) {
            statusLabel.setText("Complete all profile fields before starting face registration.");
            return;
        }

        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            statusLabel.setText("No signed-in user is available.");
            return;
        }

        userService.saveProfileDraft(clean(nameField), clean(emailField), clean(deptField));

        try {
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            boolean enrolled = ViewNavigator.showFaceEnrollmentDialog(stage, currentUser);
            if (enrolled) {
                userService.markFaceEnrolled();
                statusLabel.setText("Face registration completed. Save your profile to continue.");
            } else {
                statusLabel.setText("Face registration is mandatory before entering the employee dashboard.");
            }
        } catch (IOException e) {
            statusLabel.setText("Unable to open face registration right now.");
        }
    }

    @FXML
    public void onSave() {
        if (!profileFieldsAreValid()) {
            statusLabel.setText("Complete all profile fields before continuing.");
            return;
        }

        String name = clean(nameField);
        String email = clean(emailField);
        String department = clean(deptField);
        User currentUser = userService.getCurrentUser();

        if (userService.isEmployee(currentUser) && !currentUser.isFaceEnrolled()) {
            userService.saveProfileDraft(name, email, department);
            try {
                Stage stage = (Stage) statusLabel.getScene().getWindow();
                boolean enrolled = ViewNavigator.showFaceEnrollmentDialog(stage, currentUser);
                if (!enrolled) {
                    statusLabel.setText("Face registration is compulsory before entering the employee dashboard.");
                    return;
                }
            } catch (IOException e) {
                statusLabel.setText("Unable to open face registration right now.");
                return;
            }

            userService.completeEmployeeOnboarding(name, email, department);
        } else {
            userService.completeProfile(name, email, department);
        }

        try {
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            ViewNavigator.showDashboard(stage, userService.getCurrentUser());
        } catch (IOException e) {
            statusLabel.setText("Profile saved, but dashboard loading failed.");
        }
    }

    private boolean profileFieldsAreValid() {
        return !clean(nameField).isBlank() && !clean(emailField).isBlank() && !clean(deptField).isBlank();
    }

    private String clean(TextField field) {
        return field.getText() != null ? field.getText().trim() : "";
    }
}
