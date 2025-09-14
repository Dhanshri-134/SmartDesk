package com.smartdesk.controllers;

import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import org.springframework.stereotype.Component;

@Component
public class SuperAdminDashboardController {
    @FXML
    private StackPane contentArea;

    public void initialize() {
        // You can load different views into contentArea when nav buttons are clicked
    }
}

