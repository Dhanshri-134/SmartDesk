package com.smartdesk.meeting;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MeetingPopup extends Stage {

    public MeetingPopup() {
        // Popup modality: dashboard च्या वर
        this.initModality(Modality.APPLICATION_MODAL);

        // Title
        Label title = new Label("🚀 SmartDesk Meeting Launcher");
        title.setFont(Font.font(20));
        title.setTextFill(Color.web("#2c3e50"));

        // Room Name
        Label roomLbl = new Label("Enter Room Name:");
        TextField roomField = new TextField("PerformanceReviewRoom");

        // Display Name
        Label nameLbl = new Label("Enter Display Name:");
        TextField nameField = new TextField("Employee");

        // Join Button
        Button joinBtn = new Button("Join Meeting");
        joinBtn.setStyle(
                "-fx-background-color: linear-gradient(to right, #4facfe, #00f2fe);" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 14px;" +
                "-fx-padding: 8 20;" +
                "-fx-background-radius: 10;"
        );

        // Button click handler
        joinBtn.setOnAction(e -> {
            String room = roomField.getText().isBlank() ? "DefaultRoom" : roomField.getText().trim();
            String name = nameField.getText().isBlank() ? "Guest" : nameField.getText().trim();

            String url = "https://meet.jit.si/" + room + "#userInfo.displayName=" + encodeValue(name);

            try {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI(url));
                } else {
                    // Windows/Linux command fallback
                    Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
                }
            } catch (IOException | URISyntaxException ex) {
                ex.printStackTrace();
            }

            // Close popup after opening
            this.close();
        });

        // Layout
        VBox root = new VBox(12, title, roomLbl, roomField, nameLbl, nameField, joinBtn);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #ecf0f1; -fx-border-radius: 15; -fx-background-radius: 15;");

        Scene scene = new Scene(root, 400, 300);
        this.setScene(scene);
        this.setTitle("Join Meeting");
    }

    // Encode URL parameters
    private String encodeValue(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            return value;
        }
    }
}