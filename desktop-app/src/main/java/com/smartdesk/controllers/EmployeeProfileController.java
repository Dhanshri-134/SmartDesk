package com.smartdesk.controllers;

import java.io.File;

import org.springframework.stereotype.Component;

import com.smartdesk.model.User;
import com.smartdesk.service.UserService;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

@Component
public class EmployeeProfileController {

    @FXML private ImageView profileImage;
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField deptField;
    @FXML private TextField roleField;

    private final UserService userService;

    public EmployeeProfileController(UserService userService) {
        this.userService = userService;
    }

    @FXML
    public void initialize() {
        Image defaultImage = new Image(getClass().getResourceAsStream("/static/images/human.png"));
        profileImage.setImage(defaultImage);

        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        nameField.setText(currentUser.getFullName() != null ? currentUser.getFullName() : "");
        emailField.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");
        deptField.setText(currentUser.getDepartment() != null ? currentUser.getDepartment() : "");
        roleField.setText(currentUser.getRole() != null ? currentUser.getRole().getLabel() : "Employee");
    }

    @FXML
    private void handleChangePhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

        File selectedFile = fileChooser.showOpenDialog(profileImage.getScene().getWindow());
        if (selectedFile != null) {
            Image image = new Image(selectedFile.toURI().toString());
            profileImage.setImage(image);
        }
    }
}
