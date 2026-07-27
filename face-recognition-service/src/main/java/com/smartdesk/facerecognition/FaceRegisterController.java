package com.smartdesk.facerecognition;


import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;

import com.smartdesk.facerecognition.utils.Utils;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class FaceRegisterController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField passwordVisibleField;

    @FXML
    private Button togglePasswordBtn;

    @FXML
    private ImageView cameraView;

    @FXML
    private Button captureButton;

    private VideoCapture capture;
    private boolean capturing = false; 

    @FXML
    public void initialize() {
//        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
        startCamera();
        captureButton.setOnAction(e -> captureFace());
    }

   

private void startCamera() {
    if (capturing) return;

    capture = new VideoCapture();
    if (!capture.isOpened()) {
        System.out.println("Camera not available!");
        return;
    }

    capturing = true;

    Thread cameraThread = new Thread(() -> {
        Mat frame = new Mat();
        while (capturing) {
            boolean grabbed = capture.read(frame);
            if (grabbed && !frame.empty()) {
                Image img = Utils.mat2Image(frame);
                if (img != null) { // check if conversion succeeded
                    Platform.runLater(() -> cameraView.setImage(img));
                }
            } else {
                System.out.println("Warning: Can't grab frame or frame is empty.");
                try { Thread.sleep(100); } catch (InterruptedException e) { }
            }
        }
    });
    cameraThread.setDaemon(true);
    cameraThread.start();
}

    @FXML
    private void togglePasswordVisibility() {
        if (passwordField.isVisible()) {
            passwordVisibleField.setText(passwordField.getText());
            passwordVisibleField.setVisible(true);
            passwordField.setVisible(false);
        } else {
            passwordField.setText(passwordVisibleField.getText());
            passwordField.setVisible(true);
            passwordVisibleField.setVisible(false);
        }
    }

@FXML
private void registerFace() {
    String username = usernameField.getText();
    String password = passwordField.isVisible() ? passwordField.getText() : passwordVisibleField.getText();

    if (username.isEmpty() || password.isEmpty()) {
        System.out.println("Username or password cannot be empty!");
        return;
    }

    captureFace(); // save face image
    Utils.saveUserCredentials(username, password); // save credentials
    System.out.println("Registered: " + username + " / " + password);

    closeCamera();
}

    
    private void captureFace() {
        if (capture != null && capture.isOpened()) {
            Mat frame = new Mat();
            if (capture.read(frame)) {
                String username = usernameField.getText().trim();
                if (!username.isEmpty()) {
                    String filePath = "dataset/" + username + ".png";
                    Imgcodecs.imwrite(filePath, frame);
                    System.out.println("Face captured and saved at: " + filePath);
                } else {
                    System.out.println("Enter username first!");
                }
            }
        }
    }


    @FXML
    private void cancelRegistration() {
        closeCamera();
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.close();
    }

    private void closeCamera() {
        capturing = false;
        if (capture != null && capture.isOpened()) capture.release();
        System.out.println("Camera released in register window.");
    }


    // Optional: release camera on window close
    public void setStage(Stage stage) {
        stage.setOnCloseRequest(event -> closeCamera());
    }
}


