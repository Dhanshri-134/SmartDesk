package com.smartdesk.facerecognition.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.imageio.ImageIO;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

public class Utils {

    private static final String CREDENTIAL_FILE = "credentials.txt";

    // Save username and password to file
    public static void saveUserCredentials(String username, String password) {
        try (PrintWriter out = new PrintWriter(new FileWriter(CREDENTIAL_FILE, true))) {
            out.println(username + ":" + password);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Convert OpenCV Mat to JavaFX Image
    public static Image mat2Image(Mat frame) {
    try {
        if (frame == null || frame.empty()) {
            System.out.println("Warning: frame is null or empty.");
            return null;
        }
        MatOfByte buffer = new MatOfByte();
        boolean encoded = Imgcodecs.imencode(".png", frame, buffer);
        if (!encoded) {
            System.out.println("Warning: Failed to encode Mat to PNG.");
            return null;
        }
        byte[] byteArray = buffer.toArray();
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(byteArray));
        if (image == null) {
            System.out.println("Warning: ImageIO.read returned null for frame of size: " + frame.size());
            return null;
        }
        return SwingFXUtils.toFXImage(image, null);
    } catch (IOException e) {
        System.out.println("Error converting Mat to Image: " + e.getMessage());
        return null;
    }
}


}
