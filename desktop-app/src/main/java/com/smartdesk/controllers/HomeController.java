package com.smartdesk.controllers;

import com.smartdesk.config.SpringContext;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class HomeController {

    @FXML
    private StackPane contentArea;

    public void initialize() {
        // You can load different views into contentArea when nav buttons are clicked
    }
    @FXML
    private Label welcomeText;

    @FXML
    private void openCompanyDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CompanyDashboard.fxml"));
            Parent superadminRoot = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            Scene scene = new Scene(superadminRoot, 960, 720);
            scene.getStylesheets().add(getClass().getResource("/static/css/styles.css").toExternalForm()); // re-apply CSS

            stage.setScene(scene);
            stage.setTitle("SmartDesk - Superadmin Dashboard");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openEmployeeDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SuperadminDashboard.fxml"));
            Parent superadminRoot = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            Scene scene = new Scene(superadminRoot, 960, 720);
            scene.getStylesheets().add(getClass().getResource("/static/css/styles.css").toExternalForm()); // re-apply CSS

            stage.setScene(scene);
            stage.setTitle("SmartDesk - Superadmin Dashboard");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openSuperadminDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SuperadminDashboard.fxml"));
            Parent superadminRoot = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            Scene scene = new Scene(superadminRoot, 960, 720);
            scene.getStylesheets().add(getClass().getResource("/static/css/styles.css").toExternalForm()); // re-apply CSS

            stage.setScene(scene);
            stage.setTitle("SmartDesk - Superadmin Dashboard");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openProfile(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Profile.fxml"));
            loader.setControllerFactory(SpringContext.getContext()::getBean); // ✅ let Spring create controllers

            Parent profileRoot = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            Scene scene = new Scene(profileRoot, 960, 720);
            scene.getStylesheets().add(
                    getClass().getResource("/static/css/styles.css").toExternalForm()
            ); // re-apply CSS

            stage.setScene(scene);
            stage.setTitle("SmartDesk - Superadmin Dashboard");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

