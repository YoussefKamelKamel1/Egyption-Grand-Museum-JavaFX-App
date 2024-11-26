package com.example.grandmusuemclient;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.regex.Pattern;

public class SignUpScreen {

    private Scene scene;
    private Stage primaryStage;

    public SignUpScreen(Stage primaryStage) {
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
        Label title = new Label(ResourceManager.getString("signUpTitle"));
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // Image
        Image signUpImage = new Image(getClass().getResourceAsStream("/img.jfif"));
        ImageView imageView = new ImageView(signUpImage);
        imageView.setFitWidth(200);
        imageView.setPreserveRatio(true);

        // Form fields
        Label usernameLabel = new Label(ResourceManager.getString("usernameLabel"));
        TextField usernameField = new TextField();
        usernameField.setPromptText(ResourceManager.getString("usernamePrompt"));

        Label emailLabel = new Label(ResourceManager.getString("emailLabel"));
        TextField emailField = new TextField();
        emailField.setPromptText(ResourceManager.getString("emailPrompt"));

        Label passwordLabel = new Label(ResourceManager.getString("passwordLabel"));
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText(ResourceManager.getString("passwordPrompt"));

        Label confirmPasswordLabel = new Label(ResourceManager.getString("confirmPasswordLabel"));
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText(ResourceManager.getString("confirmPasswordPrompt"));

        // Gender dropdown
        Label genderLabel = new Label(ResourceManager.getString("genderLabel"));
        ComboBox<String> genderComboBox = new ComboBox<>();
        genderComboBox.getItems().addAll(ResourceManager.getString("genderMale"), ResourceManager.getString("genderFemale"));

        // Nationality dropdown
        Label nationalityLabel = new Label(ResourceManager.getString("nationalityLabel"));
        ComboBox<String> nationalityComboBox = new ComboBox<>();
        nationalityComboBox.getItems().addAll(
                ResourceManager.getString("nationalityAmerican"),
                ResourceManager.getString("nationalityBritish"),
                ResourceManager.getString("nationalityCanadian"),
                ResourceManager.getString("nationalityEgyptian"),
                ResourceManager.getString("nationalityFrench"),
                ResourceManager.getString("nationalityGerman"),
                ResourceManager.getString("nationalityOther")
        );

        // Buttons
        Button signUpButton = new Button(ResourceManager.getString("signUpButton"));
        signUpButton.getStyleClass().add("button");

        Button cancelButton = new Button(ResourceManager.getString("cancelButton"));
        cancelButton.getStyleClass().add("button");

        // Add elements to layout
        layout.getChildren().addAll(title, imageView, usernameLabel, usernameField, emailLabel, emailField, passwordLabel,
                passwordField, confirmPasswordLabel, confirmPasswordField, genderLabel, genderComboBox, nationalityLabel, nationalityComboBox, signUpButton, cancelButton);

        // Set button actions
        signUpButton.setOnAction(e -> handleSignUp(usernameField.getText(), emailField.getText(), passwordField.getText(), confirmPasswordField.getText(), genderComboBox.getValue(), nationalityComboBox.getValue()));
        cancelButton.setOnAction(e -> primaryStage.setScene(new HomeScreen(primaryStage, false).getScene()));

        // Create scene and set it to the primary stage
        scene = new Scene(layout, 400, 500);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
    }

    public Scene getScene() {
        return scene;
    }

    private void handleSignUp(String username, String email, String password, String confirmPassword, String gender, String nationality) {
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, ResourceManager.getString("signUpErrorTitle"), ResourceManager.getString("signUpErrorRequiredFields"));
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, ResourceManager.getString("signUpErrorTitle"), ResourceManager.getString("signUpErrorPasswordsDoNotMatch"));
            return;
        }

        if (!isValidEmail(email)) {
            showAlert(Alert.AlertType.ERROR, ResourceManager.getString("signUpErrorTitle"), ResourceManager.getString("signUpErrorInvalidEmail"));
            return;
        }

        if (!isValidPassword(password)) {
            showAlert(Alert.AlertType.ERROR, ResourceManager.getString("signUpErrorTitle"), ResourceManager.getString("signUpErrorWeakPassword"));
            return;
        }

        try {
            // Send sign-up request
            SocketManager.send("SIGNUP " + username + "," + email + "," + password + "," + gender + "," + nationality);

            // Read response
            String response = SocketManager.receive();
            switch (response) {
                case "SUCCESS":
                    showAlert(Alert.AlertType.INFORMATION, ResourceManager.getString("signUpSuccessTitle"), ResourceManager.getString("signUpSuccessMessage"));
                    primaryStage.setScene(new LoginScreen(primaryStage).getScene());
                    break;
                case "SIGNUP_FAILED: User already exists":
                    showAlert(Alert.AlertType.ERROR, ResourceManager.getString("signUpErrorTitle"), ResourceManager.getString("signUpErrorUserExists"));
                    break;
                case "SIGNUP_FAILED: Invalid email":
                    showAlert(Alert.AlertType.ERROR, ResourceManager.getString("signUpErrorTitle"), ResourceManager.getString("signUpErrorInvalidEmail"));
                    break;
                default:
                    showAlert(Alert.AlertType.ERROR, ResourceManager.getString("signUpErrorTitle"), ResourceManager.getString("signUpErrorUnknown"));
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, ResourceManager.getString("signUpErrorTitle"), ResourceManager.getString("signUpErrorUnknown"));
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return Pattern.matches(emailRegex, email);
    }

    private boolean isValidPassword(String password) {
        return password.length() >= 8 && Pattern.compile(".*[a-zA-Z].*").matcher(password).find() && Pattern.compile(".*\\d.*").matcher(password).find();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
