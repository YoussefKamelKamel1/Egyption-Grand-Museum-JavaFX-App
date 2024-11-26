package com.example.grandmusuemclient;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ProfileScreen {

    private Scene scene;
    private Stage stage;
    private String email;  // Store the user's email
    private String username; // Store the user's current username
    private String nationality; // Store the user's current nationality

    public ProfileScreen(Stage stage) {
        this.stage = stage;

        // Fetch user information from the server
        String userInfo = getUserInfo();
        if (userInfo == null) {
            showAlert(Alert.AlertType.ERROR, ResourceManager.getString("errorTitle"), ResourceManager.getString("errorLoadingUserInfo"));
            return;
        }

        // Parse user info (Assumed format: "username,email,nationality")
        String[] userDetails = userInfo.split(",");
        this.username = userDetails[0];
        this.email = userDetails[1];
        this.nationality = userDetails[2];

        // Initialize layout
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);

        Label profileTitle = new Label(ResourceManager.getString("profileTitle"));
        profileTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // Username
        Label usernameLabel = new Label(ResourceManager.getString("usernameLabel"));
        TextField usernameField = new TextField(username);

        // Email (read-only)
        Label emailLabel = new Label(ResourceManager.getString("emailLabel"));
        TextField emailField = new TextField(email);
        emailField.setDisable(true);  // Email cannot be changed

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
        nationalityComboBox.setValue(nationality);

        // Old password field for verification
        Label oldPasswordLabel = new Label(ResourceManager.getString("oldPasswordLabel"));
        PasswordField oldPasswordField = new PasswordField();
        oldPasswordField.setPromptText(ResourceManager.getString("oldPasswordPrompt"));

        // New password field for updating
        Label newPasswordLabel = new Label(ResourceManager.getString("newPasswordLabel"));
        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText(ResourceManager.getString("newPasswordPrompt"));

        // Buttons
        Button updateButton = new Button(ResourceManager.getString("updateButton"));
        Button deleteButton = new Button(ResourceManager.getString("deleteButton"));
        Button homeButton = new Button(ResourceManager.getString("homeButton"));

        // Set button styles
        updateButton.getStyleClass().add("gold-button");
        deleteButton.getStyleClass().add("gold-button");
        homeButton.getStyleClass().add("gold-button");

        // Button actions
        updateButton.setOnAction(e -> {
            String updatedUsername = usernameField.getText();
            String updatedNationality = nationalityComboBox.getValue();
            String oldPassword = oldPasswordField.getText();
            String newPassword = newPasswordField.getText();

            // Validate password and update profile
            if (validatePassword(newPassword)) {
                updateProfile(updatedUsername, updatedNationality, oldPassword, newPassword);
            }
        });

        deleteButton.setOnAction(e -> deleteAccount());
        homeButton.setOnAction(e -> navigateToHomeScreen());

        // Add elements to layout
        layout.getChildren().addAll(profileTitle, usernameLabel, usernameField, emailLabel, emailField, nationalityLabel, nationalityComboBox, oldPasswordLabel, oldPasswordField, newPasswordLabel, newPasswordField, updateButton, deleteButton, homeButton);

        scene = new Scene(layout, 400, 500);
    }

    public Scene getScene() {
        return scene;
    }

    private void updateProfile(String updatedUsername, String updatedNationality, String oldPassword, String newPassword) {
        try {
            // Construct the update request only for the fields that changed
            StringBuilder updateMessage = new StringBuilder("UPDATE_PROFILE ");

            // Check for changes and append to the message
            if (!updatedUsername.equals(username)) {
                updateMessage.append("username=").append(updatedUsername).append(",");
            }
            if (!updatedNationality.equals(nationality)) {
                updateMessage.append("nationality=").append(updatedNationality).append(",");
            }
            if (!oldPassword.isEmpty() && !newPassword.isEmpty()) {
                updateMessage.append("oldPassword=").append(oldPassword).append(",");
                updateMessage.append("newPassword=").append(newPassword).append(",");
            }

            // Remove the trailing comma and send update request to server
            String updateCommand = updateMessage.toString().replaceAll(",$", "").trim();

            // If no updates were made, show an error
            if (updateCommand.equals("UPDATE_PROFILE")) {
                showAlert(Alert.AlertType.ERROR, ResourceManager.getString("updateErrorTitle"), ResourceManager.getString("updateErrorNoChanges"));
                return;
            }

            // Send update request to server
            SocketManager.send(updateCommand);

            // Read server response
            String response = SocketManager.receive();
            if (response.equals("UPDATE_SUCCESS")) {
                showAlert(Alert.AlertType.INFORMATION, ResourceManager.getString("updateSuccessTitle"), ResourceManager.getString("updateSuccessMessage"));
            } else if (response.equals("INVALID_PASSWORD")) {
                showAlert(Alert.AlertType.ERROR, ResourceManager.getString("updateErrorTitle"), ResourceManager.getString("invalidOldPassword"));
            } else {
                showAlert(Alert.AlertType.ERROR, ResourceManager.getString("updateErrorTitle"), ResourceManager.getString("updateFailed"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, ResourceManager.getString("errorTitle"), ResourceManager.getString("errorUpdatingProfile"));
        }
    }

    private boolean validatePassword(String password) {
        if (password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, ResourceManager.getString("passwordErrorTitle"), ResourceManager.getString("passwordErrorEmpty"));
            return false;
        }

        // Check password length
        if (password.length() < 8) {
            showAlert(Alert.AlertType.ERROR, ResourceManager.getString("passwordErrorTitle"), ResourceManager.getString("passwordErrorShort"));
            return false;
        }

        // Check for at least one uppercase letter, one lowercase letter, and one digit
        if (!password.matches(".*[A-Z].*") || !password.matches(".*[a-z].*") || !password.matches(".*\\d.*")) {
            showAlert(Alert.AlertType.ERROR, ResourceManager.getString("passwordErrorTitle"), ResourceManager.getString("passwordErrorRequirements"));
            return false;
        }

        return true;
    }

    private void deleteAccount() {
        // Confirm deletion
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION, ResourceManager.getString("confirmDelete"), ButtonType.YES, ButtonType.NO);
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    // Send delete request to server
                    SocketManager.send("DELETE_ACCOUNT " + email);

                    // Read server response
                    String serverResponse = SocketManager.receive();
                    if (serverResponse.equals("DELETE_SUCCESS")) {
                        showAlert(Alert.AlertType.INFORMATION, ResourceManager.getString("deleteSuccessTitle"), ResourceManager.getString("deleteSuccessMessage"));
                        navigateToHomeScreen(false); // Navigate to home screen with user logged out
                    } else {
                        showAlert(Alert.AlertType.ERROR, ResourceManager.getString("deleteErrorTitle"), ResourceManager.getString("deleteFailed"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, ResourceManager.getString("errorTitle"), ResourceManager.getString("errorDeletingAccount"));
                }
            }
        });
    }

    private void navigateToHomeScreen(boolean isLoggedIn) {
        HomeScreen homeScreen = new HomeScreen(stage, isLoggedIn);
        stage.setScene(homeScreen.getScene());
    }

    private void navigateToHomeScreen() {
        navigateToHomeScreen(true);
    }

    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Simulating a request to get the user information (from the server)
    private String getUserInfo() {
        // This function would ideally communicate with the server to fetch user info
        try {
            // Simulating request
            SocketManager.send("GET_USER_INFO");
            // Simulating response format: "username,email,nationality"
            return SocketManager.receive();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
