package com.smartdesk.facerecognition;

import java.net.URL;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.face.LBPHFaceRecognizer;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import org.opencv.core.Rect;  


public class FaceRecognizer {

    static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    private static final String MODEL_FILE = "faces_data/face_model.yml";

    public static boolean verify(String username) throws Exception {
        URL cascadeResource = FaceRecognizer.class.getClassLoader()
                .getResource("haarcascades/haarcascade_frontalface_default.xml");
        if (cascadeResource == null) throw new RuntimeException("Cascade file not found!");

        CascadeClassifier faceDetector = new CascadeClassifier(cascadeResource.getPath());

        LBPHFaceRecognizer recognizer = LBPHFaceRecognizer.create();
        recognizer.read(MODEL_FILE);

        VideoCapture camera = new VideoCapture(0);
        if (!camera.isOpened()) throw new RuntimeException("Camera not available!");

        Mat frame = new Mat();
        Mat gray = new Mat();

        System.out.println("Look at the camera for verification...");

        boolean verified = false;

        while (camera.read(frame)) {
            Imgproc.cvtColor(frame, gray, Imgproc.COLOR_BGR2GRAY);
            MatOfRect faces = new MatOfRect();
            faceDetector.detectMultiScale(gray, faces);

            for (Rect rect : faces.toArray()) {
                Mat face = new Mat(gray, rect);
                int[] label = new int[1];
                double[] confidence = new double[1];
                recognizer.predict(face, label, confidence);

                if (confidence[0] < 50) { // adjust threshold
                    verified = true;
                    break;
                }
            }
            if (verified) break;
        }

        camera.release();
        return verified;
    }
}
