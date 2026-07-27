package com.smartdesk.controllers;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.smartdesk.model.User;
import com.smartdesk.service.face.FaceAuthService;
import com.smartdesk.service.face.FaceCaptureResult;
import com.smartdesk.service.face.FaceEnrollmentPose;
import com.smartdesk.service.face.FaceVerificationResult;
import com.smartdesk.util.OpenCvFxImageConverter;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FaceAuthDialogController {

    private enum Mode {
        ENROLL,
        VERIFY
    }

    @FXML
    private Label titleLabel;

    @FXML
    private Label subtitleLabel;

    @FXML
    private Label currentPoseLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private ImageView cameraView;

    @FXML
    private VBox enrollmentPanel;

    @FXML
    private ListView<String> poseChecklist;

    @FXML
    private Button captureButton;

    @FXML
    private Button verifyButton;

    @FXML
    private Button finishButton;

    private final FaceAuthService faceAuthService;
    private final ObservableList<String> poseItems = FXCollections.observableArrayList();
    private final AtomicReference<Mat> latestFrame = new AtomicReference<>();
    private final AtomicBoolean cameraRunning = new AtomicBoolean(false);

    private Stage dialogStage;
    private Mode mode = Mode.VERIFY;
    private String username;
    private boolean completed;
    private FaceEnrollmentPose currentPose;
    private EnumSet<FaceEnrollmentPose> capturedPoses = EnumSet.noneOf(FaceEnrollmentPose.class);
    private VideoCapture camera;
    private ScheduledExecutorService cameraExecutor;

    public FaceAuthDialogController(FaceAuthService faceAuthService) {
        this.faceAuthService = faceAuthService;
    }

    @FXML
    void initialize() {
        poseChecklist.setItems(poseItems);
        poseChecklist.setFocusTraversable(false);
        poseChecklist.setMouseTransparent(true);
    }

    public void prepareEnrollment(User user) {
        mode = Mode.ENROLL;
        username = user.getUsername();
        capturedPoses = faceAuthService.getCapturedPoses(username);
        currentPose = nextMissingPose();

        titleLabel.setText("Mandatory Face Registration");
        subtitleLabel.setText("Capture each required angle. Employee dashboard access stays blocked until all poses are saved.");
        enrollmentPanel.setVisible(true);
        enrollmentPanel.setManaged(true);
        captureButton.setVisible(true);
        captureButton.setManaged(true);
        verifyButton.setVisible(false);
        verifyButton.setManaged(false);

        updateEnrollmentUi("Complete each pose in order for a strong biometric template.");
        startCamera();
    }

    public void prepareVerification(User user) {
        mode = Mode.VERIFY;
        username = user.getUsername();

        titleLabel.setText("Face Authentication");
        subtitleLabel.setText("A live face match is required before the employee dashboard can open.");
        currentPoseLabel.setText("Align your face inside the frame, then click Verify Face.");
        statusLabel.setText("Camera is ready.");
        enrollmentPanel.setVisible(false);
        enrollmentPanel.setManaged(false);
        captureButton.setVisible(false);
        captureButton.setManaged(false);
        finishButton.setVisible(false);
        finishButton.setManaged(false);
        verifyButton.setVisible(true);
        verifyButton.setManaged(true);

        startCamera();
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
        this.dialogStage.setOnCloseRequest(event -> stopCamera());
    }

    public boolean isCompleted() {
        return completed;
    }

    @FXML
    private void onCapturePose() {
        if (mode != Mode.ENROLL) {
            return;
        }

        Mat snapshot = snapshot();
        if (snapshot == null) {
            statusLabel.setText("Camera frame not ready. Wait a moment and try again.");
            return;
        }

        FaceCaptureResult result = faceAuthService.captureEnrollmentPose(username, currentPose, snapshot);
        statusLabel.setText(result.message());
        if (!result.success()) {
            return;
        }

        capturedPoses.add(currentPose);
        currentPose = nextMissingPose();
        updateEnrollmentUi("Pose saved. Continue with the next required angle.");
    }

    @FXML
    private void onVerify() {
        verifyButton.setDisable(true);
        statusLabel.setText("Checking live frames against the enrolled profile...");

        Thread worker = new Thread(() -> {
            List<Mat> frames = collectFrames();
            FaceVerificationResult result = faceAuthService.verify(username, frames);
            Platform.runLater(() -> {
                verifyButton.setDisable(false);
                statusLabel.setText(result.message());
                if (result.success()) {
                    completed = true;
                    closeDialog();
                }
            });
        }, "smartdesk-face-verify");
        worker.setDaemon(true);
        worker.start();
    }

    @FXML
    private void onFinish() {
        if (!faceAuthService.getMissingPoses(username).isEmpty()) {
            statusLabel.setText("Capture every required angle before finishing enrollment.");
            return;
        }

        completed = true;
        closeDialog();
    }

    @FXML
    private void onCancel() {
        closeDialog();
    }

    private void updateEnrollmentUi(String statusMessage) {
        poseItems.clear();
        for (FaceEnrollmentPose pose : FaceEnrollmentPose.values()) {
            String state = capturedPoses.contains(pose) ? "Captured" : "Pending";
            poseItems.add(pose.label() + " - " + state);
        }

        if (currentPose == null) {
            currentPoseLabel.setText("All required poses captured.");
            finishButton.setDisable(false);
            finishButton.setVisible(true);
            finishButton.setManaged(true);
        } else {
            currentPoseLabel.setText("Current pose: " + currentPose.label() + " | " + currentPose.instruction());
            finishButton.setDisable(true);
            finishButton.setVisible(true);
            finishButton.setManaged(true);
        }

        statusLabel.setText(statusMessage);
    }

    private FaceEnrollmentPose nextMissingPose() {
        for (FaceEnrollmentPose pose : FaceEnrollmentPose.values()) {
            if (!capturedPoses.contains(pose)) {
                return pose;
            }
        }
        return null;
    }

    private void startCamera() {
        if (cameraRunning.getAndSet(true)) {
            return;
        }

        camera = new VideoCapture(0);
        if (!camera.isOpened()) {
            statusLabel.setText("Unable to access the camera.");
            cameraRunning.set(false);
            return;
        }

        cameraExecutor = Executors.newSingleThreadScheduledExecutor();
        cameraExecutor.scheduleAtFixedRate(() -> {
            Mat frame = new Mat();
            if (!camera.read(frame) || frame.empty()) {
                return;
            }

            latestFrame.set(frame.clone());
            var image = OpenCvFxImageConverter.toImage(frame);
            if (image != null) {
                Platform.runLater(() -> cameraView.setImage(image));
            }
        }, 0, 120, TimeUnit.MILLISECONDS);
    }

    private void stopCamera() {
        cameraRunning.set(false);
        if (cameraExecutor != null) {
            cameraExecutor.shutdownNow();
            cameraExecutor = null;
        }

        if (camera != null && camera.isOpened()) {
            camera.release();
        }
        camera = null;

        Mat frame = latestFrame.getAndSet(null);
        if (frame != null) {
            frame.release();
        }
    }

    private Mat snapshot() {
        Mat frame = latestFrame.get();
        if (frame == null || frame.empty()) {
            return null;
        }
        return frame.clone();
    }

    private List<Mat> collectFrames() {
        List<Mat> frames = new ArrayList<>();
        for (int index = 0; index < 8; index++) {
            Mat frame = snapshot();
            if (frame != null) {
                frames.add(frame);
            }
            try {
                Thread.sleep(120);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return frames;
    }

    private void closeDialog() {
        stopCamera();
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
}
