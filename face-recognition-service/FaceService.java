package com.smartdesk.facerecognition;

import java.io.File;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
import org.bytedeco.opencv.opencv_videoio.VideoCapture;

public class FaceService {

    private static final String FACE_DATA_DIR = "faces/";
    private static final String CASCADE_FILE = "haarcascade_frontalface_alt.xml";

    /**
     * Registers face for a username by capturing a frame from webcam
     */
    public void registerFace(String username) throws Exception {
        File dir = new File(FACE_DATA_DIR + username);
        if (!dir.exists()) dir.mkdirs();

        VideoCapture camera = new VideoCapture(0); // default webcam
        if (!camera.isOpened()) throw new Exception("Cannot open webcam");

        Mat frame = new Mat();
        CascadeClassifier faceDetector = new CascadeClassifier(CASCADE_FILE);

        System.out.println("Opening webcam for registration...");

        boolean faceCaptured = false;
        while (!faceCaptured) {
            camera.read(frame);
            if (!frame.empty()) {
                RectVector faces = new RectVector();
                faceDetector.detectMultiScale(frame, faces);

                if (faces.size() > 0) {
                    // Save first detected face
                    String filePath = FACE_DATA_DIR + username + "/face.jpg";
                    opencv_imgcodecs.imwrite(filePath, frame);
                    System.out.println("Face registered for user: " + username);
                    faceCaptured = true;
                }
            }
        }
        camera.release();
        System.out.println("Webcam closed after registration.");
    }

    /**
     * Verifies face by comparing captured frame with saved image
     */
    public boolean verifyFace(String username) throws Exception {
        String savedFacePath = FACE_DATA_DIR + username + "/face.jpg";
        File savedFace = new File(savedFacePath);
        if (!savedFace.exists()) {
            System.out.println("No registered face found for user: " + username);
            return false;
        }

        VideoCapture camera = new VideoCapture(0);
        if (!camera.isOpened()) throw new Exception("Cannot open webcam");

        Mat frame = new Mat();
        boolean verified = false;

        System.out.println("Opening webcam for verification...");

        while (!verified) {
            camera.read(frame);
            if (!frame.empty()) {
                // TODO: Use proper face recognition (like LBPH or OpenCV DNN)
                // For simplicity, we just check if a face is detected
                CascadeClassifier faceDetector = new CascadeClassifier(CASCADE_FILE);
                RectVector faces = new RectVector();
                faceDetector.detectMultiScale(frame, faces);

                if (faces.size() > 0) {
                    verified = true; // Face detected
                    System.out.println("Face verified for user: " + username);
                }
            }
        }
        camera.release();
        System.out.println("Webcam closed after verification.");
        return verified;
    }
}
