package com.smartdesk.facerecognition;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class FaceServiceLauncher extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load OpenCV library
//        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);

        // Load Face Login FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/face_login.fxml"));
        Scene scene = new Scene(loader.load());

        FaceLoginController controller = loader.getController();
        controller.setStage(primaryStage); // So camera can stop on close

        // Define action after successful verification
        controller.setOnVerified(() -> {
            System.out.println("User verified! Loading Face Register...");

            try {
                FXMLLoader regLoader = new FXMLLoader(getClass().getResource("/fxml/face_register.fxml"));
                Scene regScene = new Scene(regLoader.load());
                FaceRegisterController regController = regLoader.getController();
                regController.setStage(primaryStage);

                primaryStage.setScene(regScene);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        primaryStage.setTitle("SmartDesk Face Recognition");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(event -> controller.stopCamera()); // Ensure camera cleanup
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
