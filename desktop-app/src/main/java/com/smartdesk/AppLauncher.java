package com.smartdesk;

import com.smartdesk.config.SpringContext;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.context.ConfigurableApplicationContext;

public class AppLauncher extends Application {

    private ConfigurableApplicationContext context;

    @Override
    public void init() {
        context = SpringContext.start();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Home.fxml"));
        loader.setControllerFactory(context::getBean);
        Parent root = loader.load();

        Scene scene = new Scene(root, 960, 720);
        scene.getStylesheets().add(getClass().getResource("/static/css/styles.css").toExternalForm());

        primaryStage.setTitle("SmartDesk - Home Screen");
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
