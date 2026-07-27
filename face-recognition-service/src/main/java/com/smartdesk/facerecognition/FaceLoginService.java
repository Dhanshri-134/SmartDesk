package com.smartdesk.facerecognition;

import javafx.application.Application;

public class FaceLoginService {
    public static void startFaceAuth() {
        new Thread(() -> Application.launch(FaceServiceLauncher.class)).start();
    }
}
