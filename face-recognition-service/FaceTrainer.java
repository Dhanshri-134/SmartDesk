package com.smartdesk.facerecognition;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.face.LBPHFaceRecognizer;
import org.opencv.imgcodecs.Imgcodecs;

public class FaceTrainer {

    static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    private static final String DATA_DIR = "faces_data";
    private static final String MODEL_FILE = "faces_data/face_model.yml";

    public static void trainModel() {
        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists()) return;

        List<Mat> images = new ArrayList<>();
        List<Integer> labels = new ArrayList<>();
        int label = 0;

        for (File userDir : dataDir.listFiles()) {
            if (!userDir.isDirectory()) continue;

            for (File imgFile : userDir.listFiles()) {
                Mat img = Imgcodecs.imread(imgFile.getAbsolutePath(), Imgcodecs.IMREAD_GRAYSCALE);
                images.add(img);
                labels.add(label);
            }
            label++;
        }

        if (images.isEmpty()) return;

        LBPHFaceRecognizer recognizer = LBPHFaceRecognizer.create();
        recognizer.train(images, new MatOfInt(labels.stream().mapToInt(i -> i).toArray()));
        recognizer.save(MODEL_FILE);

        System.out.println("Face model trained successfully!");
    }
}
