package com.smartdesk.util;

import java.io.IOException;

import org.springframework.beans.BeansException;
import org.springframework.context.ConfigurableApplicationContext;

import com.smartdesk.config.SpringContext;
import com.smartdesk.controllers.FaceAuthDialogController;
import com.smartdesk.model.User;
import com.smartdesk.model.UserRole;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public final class ViewNavigator {

    private ViewNavigator() {
    }

    public static FXMLLoader createLoader(String fxmlPath) {
        FXMLLoader loader = new FXMLLoader(ViewNavigator.class.getResource(fxmlPath));
        ConfigurableApplicationContext context = SpringContext.getContext();
        if (context != null) {
            loader.setControllerFactory(type -> {
                try {
                    return context.getBean(type);
                } catch (BeansException ex) {
                    try {
                        return type.getDeclaredConstructor().newInstance();
                    } catch (Exception reflectionEx) {
                        throw new IllegalStateException("Failed to create controller: " + type.getName(), reflectionEx);
                    }
                }
            });
        }
        return loader;
    }

    public static Parent load(String fxmlPath) throws IOException {
        return createLoader(fxmlPath).load();
    }

    public static void showLogin(Stage stage) throws IOException {
        showScene(stage, "/fxml/Login.fxml", "SmartDesk - Login", 520, 420, "/static/css/styles.css");
    }

    public static void showProfile(Stage stage) throws IOException {
        showScene(stage, "/fxml/Profile.fxml", "SmartDesk - Profile Setup", 760, 520, "/static/css/styles.css");
    }

    public static boolean showFaceEnrollmentDialog(Stage owner, User user) throws IOException {
        return showFaceDialog(owner, user, true);
    }

    public static boolean showFaceVerificationDialog(Stage owner, User user) throws IOException {
        return showFaceDialog(owner, user, false);
    }

    public static void showDashboard(Stage stage, User user) throws IOException {
        UserRole role = user != null && user.getRole() != null ? user.getRole() : UserRole.EMPLOYEE;
        switch (role) {
            case SUPERADMIN -> showScene(stage, "/fxml/SuperadminDashboard.fxml", "SmartDesk - Superadmin Dashboard",
                    1280, 800, "/static/css/dashboard.css");
            case COMPANY_ADMIN -> showScene(stage, "/fxml/CompanyDashboard.fxml", "SmartDesk - Company Admin Dashboard",
                    1280, 800, "/static/css/styles.css");
            case EMPLOYEE -> showScene(stage, "/fxml/EmployeeDashboard.fxml", "SmartDesk - Employee Dashboard",
                    1180, 760, "/static/css/employee-dashboard.css");
        }
    }

    public static void showScene(Stage stage, String fxmlPath, String title, int width, int height, String stylesheet)
            throws IOException {
        Parent root = load(fxmlPath);
        Scene scene = new Scene(root, width, height);
        if (stylesheet != null) {
            scene.getStylesheets().add(ViewNavigator.class.getResource(stylesheet).toExternalForm());
        }
        stage.setScene(scene);
        stage.setTitle(title);
        stage.show();
    }

    private static boolean showFaceDialog(Stage owner, User user, boolean enrollment) throws IOException {
        FXMLLoader loader = createLoader("/fxml/FaceAuthDialog.fxml");
        Parent root = loader.load();
        FaceAuthDialogController controller = loader.getController();

        if (enrollment) {
            controller.prepareEnrollment(user);
        } else {
            controller.prepareVerification(user);
        }

        Stage dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle(enrollment ? "SmartDesk - Face Registration" : "SmartDesk - Face Authentication");
        dialog.setScene(new Scene(root));
        controller.setDialogStage(dialog);
        dialog.showAndWait();
        return controller.isCompleted();
    }
}
