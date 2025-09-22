package com.smartdesk.controllers;

import com.smartdesk.model.User;
//import com.smartdesk.service.FaceService;
import com.smartdesk.service.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.springframework.stereotype.Component;

@Component
public class ProfileController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField deptField;
    @FXML private Label statusLabel;

    private final UserService userService;
//    private final FaceService faceService;


    public ProfileController(UserService userService) {
        this.userService = userService;
    }
//
//    @FXML
//    public void onRegisterFace() {
//        boolean ok = faceService.registerFace(userService.getCurrentUsername());
//        statusLabel.setText(ok ? "Face registered successfully." : "Face registration failed.");
//    }

//    @FXML
//    public void onSave() {
//        userService.completeProfile(nameField.getText(), emailField.getText(), deptField.getText());
//        statusLabel.setText("Profile saved. You can now use face login.");
//    }
}
