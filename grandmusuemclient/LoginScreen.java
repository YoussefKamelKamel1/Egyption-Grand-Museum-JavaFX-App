package com.example.grandmusuemclient;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;

public class LoginScreen {

    private Scene scene;
    private Stage primaryStage;

    public LoginScreen(Stage primaryStage) {
        this.primaryStage = primaryStage;

        try {
            // Initialize SocketManager and connect to server
            SocketManager.connect("localhost", 12345);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, ResourceManager.getString("connectionErrorTitle"), ResourceManager.getString("connectionErrorMessage"));
            return;
        }

        // Initialize layout
        VBox layout = new VBox();
        layout.setPadding(new Insets(20));
        layout.setSpacing(15);
        layout.setAlignment(Pos.CENTER);

        // Title
        Label title = new Label(ResourceManager.getString("loginTitle"));
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // Image
        Image loginImage = new Image(getClass().getResourceAsStream("/img.jfif"));
        ImageView imageView = new ImageView(loginImage);
        imageView.setFitWidth(200);
        imageView.setPreserveRatio(true);

        // Form fields
        Label emailLabel = new Label(ResourceManager.getString("emailLabel"));
        TextField emailField = new TextField();
        emailField.setPromptText(ResourceManager.getString("emailPrompt"));

        Label passwordLabel = new Label(ResourceManager.getString("passwordLabel"));
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText(ResourceManager.getString("passwordPrompt"));

        // Buttons
        Button loginButton = new Button(ResourceManager.getString("loginButton"));
        loginButton.getStyleClass().add("button");

        Button cancelButton = new Button(ResourceManager.getString("cancelButton"));
        cancelButton.getStyleClass().add("button");

        // Add elements to layout
        layout.getChildren().addAll(title, imageView, emailLabel, emailField, passwordLabel, passwordField, loginButton, cancelButton);

        // Set button actions
        loginButton.setOnAction(e -> handleLogin(emailField.getText(), passwordField.getText()));
        cancelButton.setOnAction(e -> primaryStage.setScene(new HomeScreen(primaryStage, false).getScene()));

        // Create scene and set it to the primary stage
        scene = new Scene(layout, 400, 500);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
    }

    public Scene getScene() {
        return scene;
    }

    private void handleLogin(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, ResourceManager.getString("loginErrorTitle"), ResourceManager.getString("loginErrorRequiredFields"));
            return;
        }

        try {
            // Send login request
            SocketManager.send("LOGIN " + email + "," + password);

            // Read response
            String response = SocketManager.receive();
            switch (response) {
                case "SUCCESS":
                    showAlert(Alert.AlertType.INFORMATION, ResourceManager.getString("loginSuccessTitle"), ResourceManager.getString("loginSuccessMessage"));
                    primaryStage.setScene(new HomeScreen(primaryStage, true).getScene()); // HomeScreen with logged-in state
                    break;
                case "LOGIN_FAILED: Invalid password":
                    showAlert(Alert.AlertType.ERROR, ResourceManager.getString("loginErrorTitle"), ResourceManager.getString("loginErrorInvalidPassword"));
                    break;
                case "LOGIN_FAILED: User not found":
                    showAlert(Alert.AlertType.ERROR, ResourceManager.getString("loginErrorTitle"), ResourceManager.getString("loginErrorUserNotFound"));
                    break;
                default:
                    showAlert(Alert.AlertType.ERROR, ResourceManager.getString("loginErrorTitle"), ResourceManager.getString("loginErrorUnknown"));
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, ResourceManager.getString("loginErrorTitle"), ResourceManager.getString("loginErrorUnknown"));
        }
    }

    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
