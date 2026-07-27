package com.smartdesk.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

public final class OpenCvFxImageConverter {

    private OpenCvFxImageConverter() {
    }

    public static Image toImage(Mat frame) {
        if (frame == null || frame.empty()) {
            return null;
        }

        MatOfByte buffer = new MatOfByte();
        if (!Imgcodecs.imencode(".png", frame, buffer)) {
            return null;
        }

        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(buffer.toArray()));
            return image != null ? SwingFXUtils.toFXImage(image, null) : null;
        } catch (IOException ex) {
            return null;
        }
    }
}
