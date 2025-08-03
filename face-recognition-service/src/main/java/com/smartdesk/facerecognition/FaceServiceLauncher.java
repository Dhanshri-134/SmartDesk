package com.smartdesk.facerecognition;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class FaceServiceLauncher extends Application {

    private AnnotationConfigApplicationContext context;

    @Override
    public void init() {
        // Initialize Spring context so FaceService can be autowired
        context = new AnnotationConfigApplicationContext(FaceService.class);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FaceService faceService = context.getBean(FaceService.class);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/face_login.fxml"));
        loader.setControllerFactory(param -> new FaceLoginController(faceService));

        Scene scene = new Scene(loader.load());
        primaryStage.setTitle("SmartDesk Face Login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() {
        context.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
