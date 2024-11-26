package com.example.grandmusuemclient;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ChattingRoom {
    private Stage stage;
    private Scene scene;
    private VBox chatBox; // Use VBox to stack messages vertically
    private TextField messageField;
    private Button sendButton;
    private Button homeButton; // Button to return to the home screen
    private Button saveChatLogButton; // Button to save chat log
    private ComboBox<String> statusComboBox; // Dropdown for user status
    private ImageView smallImageView; // ImageView for small image

    public ChattingRoom(Stage stage) {
        this.stage = stage;

        // Get the socket from SocketManager
        if (!SocketManager.isConnected()) {
            showError(ResourceManager.getString("socketError"));
            return;
        }

        // UI setup
        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(10));

        // Add small image at the top

        Image image = new Image(getClass().getResourceAsStream("/img.jfif")); // Ensure path is correct
        smallImageView = new ImageView(image);
        smallImageView.setFitWidth(100); // Adjust size as needed
        smallImageView.setPreserveRatio(true);
        layout.setTop(smallImageView);
        BorderPane.setAlignment(smallImageView, Pos.CENTER);

        // Create a VBox for chat messages
        chatBox = new VBox();
        chatBox.setSpacing(10);
        chatBox.setPadding(new Insets(10));

        ScrollPane scrollPane = new ScrollPane(chatBox);
        scrollPane.setFitToWidth(true);
        layout.setCenter(scrollPane);

        // Create message input field and send button
        messageField = new TextField();
        messageField.setPromptText(ResourceManager.getString("messagePrompt"));
        sendButton = new Button(ResourceManager.getString("sendButton"));
        sendButton.setStyle("-fx-background-color: #ffcc00; -fx-text-fill: #000000; -fx-font-size: 14px;");

        // Create home button
        homeButton = new Button(ResourceManager.getString("homeButton"));
        homeButton.setStyle("-fx-background-color: #ffd700; -fx-text-fill: #000000; -fx-font-size: 14px;");
        homeButton.setOnAction(event -> goHome());

        // Create save chat log button
        saveChatLogButton = new Button(ResourceManager.getString("saveChatLogButton"));
        saveChatLogButton.setStyle("-fx-background-color: #ffcc00; -fx-text-fill: #000000; -fx-font-size: 14px;");
        saveChatLogButton.setOnAction(event -> saveChatLog());

        // Create status dropdown
        statusComboBox = new ComboBox<>();
        statusComboBox.getItems().addAll(
                ResourceManager.getString("statusAvailable"),
                ResourceManager.getString("statusBusy")
        );
        statusComboBox.setValue(ResourceManager.getString("statusAvailable")); // Default status
        statusComboBox.setOnAction(event -> updateStatus(statusComboBox.getValue()));

        // Layout for buttons and status combo box
        HBox topBox = new HBox(10, saveChatLogButton, statusComboBox);
        topBox.setPadding(new Insets(10));
        topBox.setAlignment(Pos.CENTER_LEFT);
        layout.setTop(topBox);

        // Layout for message input and send button
        HBox inputBox = new HBox(10, messageField, sendButton);
        inputBox.setPadding(new Insets(10));
        messageField.setMaxWidth(Double.MAX_VALUE); // Make messageField take available width
        HBox.setHgrow(messageField, Priority.ALWAYS); // Grow messageField horizontally

        // Combine everything in bottomBox
        VBox bottomBox = new VBox(10, inputBox, homeButton);
        bottomBox.setPadding(new Insets(10));
        layout.setBottom(bottomBox);

        // Setup the event for the send button
        sendButton.setOnAction(event -> sendMessage());
        messageField.setOnAction(event -> sendMessage());

        // Scene and stage setup
        scene = new Scene(layout, 800, 600);
        stage.setScene(scene);
        stage.setTitle(ResourceManager.getString("chattingRoomTitle"));
        stage.show();

        // Start listening for incoming messages
        new Thread(this::listenForMessages).start();
    }

    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            try {
                SocketManager.send("CHAT " + message); // Use SocketManager to send the message
                messageField.clear();
                addMessageToChat(new ChatMessage(message, true)); // Add sent message to chat
            } catch (Exception e) {
                showError(ResourceManager.getString("sendMessageError") + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void listenForMessages() {
        try {
            String message;
            while ((message = SocketManager.receive()) != null) {
                if (message.startsWith("MESSAGE")) {
                    String[] parts = message.split(",", 5); // Splits into "MESSAGE", <username>, <nationality>, <status>, and <message>
                    if (parts.length != 0) {
                        String username = parts[1];
                        String nationality = parts[2];
                        String status = parts[3];
                        String actualMessage = parts[4];

                        // Format received messages
                        String formattedMessage = String.format("[%s - %s - %s]: %s", username, nationality, status, actualMessage);
                        addMessageToChat(new ChatMessage(formattedMessage, false)); // Add received message to chat
                    }
                }
            }
        } catch (IOException e) {
            showError(ResourceManager.getString("connectionError") + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addMessageToChat(ChatMessage chatMessage) {
        Platform.runLater(() -> {
            Label messageLabel = new Label(chatMessage.getContent());
            messageLabel.setWrapText(true);
            messageLabel.setPadding(new Insets(5, 10, 5, 10));

            if (chatMessage.isSent()) {
                // Style for messages sent by the user
                messageLabel.setStyle("-fx-background-color: #c3f1c3; -fx-text-fill: #000000; -fx-border-radius: 10; -fx-background-radius: 10;");
                messageLabel.setAlignment(Pos.CENTER_RIGHT);
            } else {
                // Style for messages from others
                messageLabel.setStyle("-fx-background-color: #cce5ff; -fx-text-fill: #000000; -fx-border-radius: 10; -fx-background-radius: 10;");
                messageLabel.setAlignment(Pos.CENTER_LEFT);
            }
            chatBox.getChildren().add(messageLabel);
        });
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(ResourceManager.getString("errorTitle"));
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void goHome() {
        stage.setScene(new HomeScreen(stage, true).getScene());
    }

    private void saveChatLog() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(ResourceManager.getString("saveChatLogButton"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                for (var node : chatBox.getChildren()) {
                    if (node instanceof Label) {
                        Label label = (Label) node;
                        writer.write(label.getText());
                        writer.newLine();
                    }
                }
                showAlert(ResourceManager.getString("chatLogSaved"));
            } catch (IOException e) {
                showError(ResourceManager.getString("saveChatLogError") + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void showAlert(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(ResourceManager.getString("informationTitle"));
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void updateStatus(String status) {
        SocketManager.send("STATUS " + status); // Use SocketManager to send status update
    }

    public Scene getScene() {
        return scene;
    }
}
