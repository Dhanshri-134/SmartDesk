package com.smartdesk.facerecognition;

import java.io.File;
import java.net.URL;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import org.w3c.dom.css.Rect;

public class FaceCapture {

    static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    public static void capture(String username) throws Exception {
        URL cascadeResource = FaceCapture.class.getClassLoader()
                .getResource("haarcascades/haarcascade_frontalface_default.xml");
        if (cascadeResource == null) throw new RuntimeException("Cascade file not found!");

        CascadeClassifier faceDetector = new CascadeClassifier(cascadeResource.getPath());

        VideoCapture camera = new VideoCapture(0);
        if (!camera.isOpened()) throw new RuntimeException("Camera not available!");

        String userDir = "faces_data/" + username;
        File dir = new File(userDir);
        if (!dir.exists()) dir.mkdirs();

        int count = 0;
        Mat frame = new Mat();
        Mat gray = new Mat();

        System.out.println("Capturing face images for " + username);

        while (count < 20) {
            if (camera.read(frame)) {
                Imgproc.cvtColor(frame, gray, Imgproc.COLOR_BGR2GRAY);
                MatOfRect faces = new MatOfRect();
                faceDetector.detectMultiScale(gray, faces);

                for (Rect rect : faces.toArray()) {
                    Mat face = new Mat(gray, rect);
                    Imgcodecs.imwrite(userDir + "/" + username + "_" + count + ".png", face);
                    count++;
                    System.out.println("Captured image " + count);
                }
            }
        }

        camera.release();
        System.out.println("Face capture complete for " + username);
    }
}
