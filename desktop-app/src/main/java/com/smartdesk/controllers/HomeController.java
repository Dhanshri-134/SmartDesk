package com.smartdesk.controllers;


import java.io.IOException;
import java.util.Objects;

import jakarta.annotation.PostConstruct;
import org.opencv.core.Core;
import org.springframework.stereotype.Component;

import com.smartdesk.config.SpringContext;
import com.smartdesk.facerecognition.FaceLoginController;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

@Component
public class HomeController {

    @FXML
    private void refreshPage(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/Home.fxml")
            );

            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene().getWindow();

            Scene scene = new Scene(root, 960, 720);
            scene.getStylesheets().add(
                    getClass().getResource("/static/css/styles.css")
                            .toExternalForm()
            );

            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @PostConstruct
    public void init() {
        try {
            // Just test if OpenCV is available
            System.out.println("OpenCV Version: " + org.opencv.core.Core.VERSION);
            System.out.println("OpenCV Loaded Successfully ✅");
        } catch (Exception e) {
            System.out.println("OpenCV Failed To Load ❌");
            e.printStackTrace();
        }
    }

    @FXML
    private StackPane contentArea;

    @FXML
    private Label welcomeText;

    @FXML
    public void initialize() {
    // Perform face verification immediately on startup
    new Thread(() -> {
//        boolean verified = performFaceVerification();
        boolean verified = true;

        if (!verified) {
            System.out.println("❌ Access Denied at startup.");
            System.exit(0); // exit if face not verified
        }
    }).start();
}

private boolean performFaceVerification() {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/face_login.fxml"));
        Stage stage = new Stage();
        Scene scene = new Scene(loader.load());
        FaceLoginController controller = loader.getController();

        final boolean[] verified = {false};
        controller.setOnVerified(() -> {
            verified[0] = true;
            stage.close();
        });

        stage.setScene(scene);
        stage.showAndWait(); // Wait until face verification completes

        // ✅ If face verification fails, show stylish backup password dialog
        if (!verified[0]) {
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("Backup Login");
            dialog.setHeaderText("Face verification failed ⚠️");
            dialog.getDialogPane().setStyle(
                "-fx-background-color: linear-gradient(to right, #0f2027, #203a43, #2c5364);" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-font-family: 'Segoe UI';"
            );

            Label label = new Label("Enter your password:");
            label.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
            PasswordField passwordField = new PasswordField();
            passwordField.setPromptText("Password");
            passwordField.setStyle("-fx-background-radius: 8; -fx-padding: 6;");

            VBox vbox = new VBox(10, label, passwordField);
            vbox.setPadding(new Insets(10));
            dialog.getDialogPane().setContent(vbox);

            ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == loginButtonType) {
                    return passwordField.getText();
                }
                return null;
            });

            dialog.showAndWait().ifPresent(password -> {
                // 🔹 Replace with your DB password logic
                if (password.equals("admin123")) {
                    verified[0] = true;
                    showAlert("Access Granted ✅", "Welcome back!");
                } else {
                    showAlert("Access Denied ❌", "Incorrect password!");
                }
            });
        }

        return verified[0];
    } catch (Exception e) {
        e.printStackTrace();
        showAlert("Error", "Face verification failed!");
        return false;
    }
}




    @FXML
    private void openCompanyDashboard(ActionEvent event) {
//        if (!performFaceVerification()) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CompanyDashboard.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, 960, 720);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/static/css/styles.css")).toExternalForm());

            stage.setScene(scene);
            stage.setTitle("SmartDesk - Company Dashboard");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openEmployeeDashboard(ActionEvent event) {

//        if (!performFaceVerification()) return;
        // 🔹 Check face before allowing access

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EmployeeDashboard.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, 960, 720);
            scene.getStylesheets().add(getClass().getResource("/static/css/styles.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("SmartDesk - Employee Dashboard");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openSuperadminDashboard(ActionEvent event) {
    
//        if (!performFaceVerification()) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SuperadminDashboard.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, 960, 720);
            scene.getStylesheets().add(getClass().getResource("/static/css/styles.css").toExternalForm());

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
            loader.setControllerFactory(SpringContext.getContext()::getBean);

            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, 960, 720);
            scene.getStylesheets().add(getClass().getResource("/static/css/styles.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("SmartDesk - Profile");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
