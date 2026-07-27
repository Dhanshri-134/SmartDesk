package com.smartdesk.facerecognition;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;

import com.smartdesk.facerecognition.utils.Utils;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class FaceLoginController {

    @FXML private ImageView cameraView;
    @FXML private Label statusLabel;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private javafx.scene.control.Button eyeButton;

    private Runnable onVerified;
    private VideoCapture camera;
    private Timer timer;
    private boolean capturing = false;

    private static final String USER_DATA_FILE = "dataset/users.txt";

    public void setOnVerified(Runnable onVerified) {
        this.onVerified = onVerified;
    }

@FXML
private void initialize() {
//    System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);

    // Fix eye button to toggle password visibility
    eyeButton.setOnAction(e -> {
        if (passwordField.getPromptText().equals("Password")) {
            passwordField.setPromptText("");
            passwordField.setPromptText("Password");
        } else {
            String pwd = passwordField.getText();
            passwordField.clear();
            passwordField.setPromptText(pwd);
        }
    });
    }

@FXML
    private void togglePasswordVisibility() {
    if (passwordField.getPromptText().equals("Enter Password")) {
        passwordField.setPromptText("");
        passwordField.setPromptText("Enter Password");
    } else {
        String pwd = passwordField.getText();
        passwordField.clear();
        passwordField.setPromptText(pwd);
    }
}

    // 🔹 Face Login button
    @FXML
    private void onLoginFace() {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            statusLabel.setText("Enter username for login!");
            return;
        }

        statusLabel.setText("Starting face verification...");
        startCamera(username, false);
    }

    //face and passwod registering button 

    @FXML
    private void onOpenRegisterFace() {
        stopCamera();
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/face_register.fxml"));
        Stage stage = new Stage();
        stage.setScene(new Scene(loader.load()));
        stage.setTitle("Register Face & Password");
        stage.initModality(Modality.APPLICATION_MODAL); // Optional: block main window
        stage.show();
    } catch (IOException e) {
        e.printStackTrace();
        statusLabel.setText("Failed to open Register Face window!");
    }
}


    // 🔹 Username + Password Login button
    @FXML
    private void onLoginPassword() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Enter both username and password!");
            return;
        }

        if (verifyCredentials(username, password)) {
            statusLabel.setText("Username + Password login successful!");
            if (onVerified != null) onVerified.run();
        } else {
            statusLabel.setText("Invalid username or password!");
        }
    }

    // 🔹 Forgot Password
    @FXML
    private void onForgotPassword() {
        statusLabel.setText("Forgot password feature coming soon!");
    }

    // 🔹 Start camera preview and face verification
    public void startCamera(String username, boolean isRegistering) {
        if (capturing) return;

        camera = new VideoCapture();
        if (!camera.isOpened()) {
            statusLabel.setText("Camera not available!");
            return;
        }

        capturing = true;

        Thread cameraThread = new Thread(() -> {
            Mat frame = new Mat();
            while (capturing) {
                if (camera.read(frame)) {
                    Image img = Utils.mat2Image(frame);
                    Platform.runLater(() -> cameraView.setImage(img));
                }
            }
        });
        cameraThread.setDaemon(true);
        cameraThread.start();

        // Schedule face verification after 3 seconds
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Mat frame = new Mat();
                if (camera.read(frame)) {
                    boolean matched = verifyFaceImage(frame, username);
                    stopCamera();
                    Platform.runLater(() -> {
                        if (matched) {
                            statusLabel.setText("Face verified! Login successful.");
                            if (onVerified != null) onVerified.run();
                        } else {
                            statusLabel.setText("Face not recognized. Try password login.");
                        }
                    });
                }
            }
        }, 3000);
    }


    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    // Optional: handle stage close to stop camera
        this.stage.setOnCloseRequest(e -> stopCamera());
}


    public void stopCamera() {
    capturing = false;
    if (timer != null) timer.cancel();
    if (camera != null && camera.isOpened()) camera.release();
    System.out.println("Camera released.");
}

    // 🔹 Credential handling
    private boolean verifyCredentials(String username, String password) {
        Map<String, String> users = loadUserCredentials();
        return users.containsKey(username) && users.get(username).equals(password);
    }

    private Map<String, String> loadUserCredentials() {
        Map<String, String> map = new HashMap<>();
        File file = new File(USER_DATA_FILE);
        if (!file.exists()) return map;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) map.put(parts[0], parts[1]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    // 🔹 Face verification
    private boolean verifyFaceImage(Mat frame, String username) {
        String filePath = "dataset/" + username + ".png";
        File file = new File(filePath);
        if (!file.exists()) return false;
        Mat saved = Imgcodecs.imread(filePath);
        return saved.size().equals(frame.size());
    }
}
