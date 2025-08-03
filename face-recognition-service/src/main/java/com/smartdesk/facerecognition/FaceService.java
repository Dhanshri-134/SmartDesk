package com.smartdesk.facerecognition;

import java.io.File;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import org.springframework.stereotype.Service;

@Service
public class FaceService {

    private static final String FACE_DIR = "faces"; // folder to store images
    private static final String CASCADE_FILE = "haarcascade_frontalface_default.xml";

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        System.out.println("✅ OpenCV loaded successfully!");
    }

    // Register face for a given username
    public void registerFace(String username) throws Exception {
        File dir = new File(FACE_DIR);
        if (!dir.exists()) dir.mkdirs();

        VideoCapture camera = new VideoCapture(0);
        if (!camera.isOpened()) throw new Exception("Cannot open webcam");

        CascadeClassifier faceDetector = new CascadeClassifier(
    getClass().getClassLoader().getResource(CASCADE_FILE).getPath()
);

        if (faceDetector.empty()) throw new Exception("Could not load Haar cascade file!");

        Mat frame = new Mat();
        System.out.println("📸 Look at the camera... capturing face in 5 seconds.");
        Thread.sleep(5000);

        if (camera.read(frame)) {
            MatOfRect faces = new MatOfRect();
            faceDetector.detectMultiScale(frame, faces);

            if (faces.toArray().length > 0) {
                for (Rect rect : faces.toArray()) {
                    Imgproc.rectangle(frame, rect.tl(), rect.br(), new org.opencv.core.Scalar(0, 255, 0), 2);
                    Mat faceMat = new Mat(frame, rect);
                    String path = FACE_DIR + "/" + username + ".png";
                    Imgcodecs.imwrite(path, faceMat);
                    System.out.println("✅ Face saved at: " + path);
                    break;
                }
            } else {
                throw new Exception("No face detected!");
            }

            // Show preview window
            HighGui.imshow("Captured Face", frame);
            HighGui.waitKey(2000);
            HighGui.destroyAllWindows();
        }

        camera.release();
    }

    // Simple verification check
    public boolean verifyFace(String username) {
        File file = new File(FACE_DIR + "/" + username + ".png");
        return file.exists();
    }
}
