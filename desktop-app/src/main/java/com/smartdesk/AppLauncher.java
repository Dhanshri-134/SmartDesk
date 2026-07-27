package com.smartdesk;

import org.springframework.context.ConfigurableApplicationContext;

import com.smartdesk.config.SpringContext;
import com.smartdesk.util.ViewNavigator;

import javafx.application.Application;
import javafx.stage.Stage;

public class AppLauncher extends Application {

    private ConfigurableApplicationContext context;

    @Override
    public void init() {
        context = SpringContext.start();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        ViewNavigator.showLogin(primaryStage);
    }

    @Override
    public void stop() {
        context.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
