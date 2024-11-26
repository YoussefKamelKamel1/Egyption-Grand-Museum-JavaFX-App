package com.example.grandmusuemclient;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class ChatbotScreen {

    private VBox layout;
    private ListView<String> questionsList;
    private TextArea answerArea;
    private Stage primaryStage;
    private ResourceManager resourceManager;
    private Map<String, String> questionAnswerMap;

    public ChatbotScreen(Stage primaryStage) {
        this.primaryStage = primaryStage; // Save reference to the primary stage
        this.resourceManager = resourceManager; // Initialize ResourceManager

        // Initialize the main layout container
        layout = new VBox(20);
        layout.setPadding(new Insets(15, 20, 15, 20));
        layout.setAlignment(Pos.CENTER);
        layout.getStyleClass().add("chatbot-layout"); // Add CSS class to layout

        // Initialize question-answer map
        initializeQuestionAnswerMap();

        // Label for the question section
        Label selectQuestionLabel = new Label(resourceManager.getString("select_question"));
        selectQuestionLabel.getStyleClass().add("chatbot-label"); // Add CSS class to label

        // List view to display questions
        questionsList = new ListView<>();
        populateQuestionsList();
        questionsList.getStyleClass().add("chatbot-questions-list"); // Add CSS class to list view

        // Text area to display answers
        answerArea = new TextArea();
        answerArea.setPromptText(resourceManager.getString("answer_placeholder"));
        answerArea.setWrapText(true);
        answerArea.setEditable(false);
        answerArea.getStyleClass().add("chatbot-answer-area"); // Add CSS class to text area

        // Button to get the answer
        Button getAnswerButton = new Button(resourceManager.getString("get_answer"));
        getAnswerButton.getStyleClass().add("gold-button"); // Add CSS class to button
        getAnswerButton.setOnAction(e -> displayAnswer());

        // Back button to return to the home screen
        Button backButton = new Button(resourceManager.getString("back_to_home"));
        backButton.getStyleClass().add("gold-button"); // Add CSS class to button
        backButton.setOnAction(e -> navigateToHomeScreen());

        // HBox to hold the buttons
        HBox buttonBox = new HBox(10, getAnswerButton, backButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10));
        HBox.setHgrow(getAnswerButton, Priority.ALWAYS);
        HBox.setHgrow(backButton, Priority.ALWAYS);

        // Add components to the layout
        layout.getChildren().addAll(selectQuestionLabel, questionsList, buttonBox, answerArea);

        // Create the scene with the layout and set the CSS file
        Scene scene = new Scene(layout, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        // Set the scene on the primary stage
        primaryStage.setScene(scene);
        primaryStage.setTitle(resourceManager.getString("chatbot_title"));
        primaryStage.show();
    }

    private void initializeQuestionAnswerMap() {
        questionAnswerMap = new HashMap<>();
        questionAnswerMap.put(resourceManager.getString("opening_hours"), resourceManager.getString("opening_hours_answer"));
        questionAnswerMap.put(resourceManager.getString("museum_location"), resourceManager.getString("museum_location_answer"));
        questionAnswerMap.put(resourceManager.getString("current_exhibits"), resourceManager.getString("current_exhibits_answer"));
        questionAnswerMap.put(resourceManager.getString("entry_ticket_cost"), resourceManager.getString("entry_ticket_cost_answer"));
        questionAnswerMap.put(resourceManager.getString("museum_open_now"), resourceManager.getString("museum_open_now_answer"));
        questionAnswerMap.put(resourceManager.getString("veterans_entry"), resourceManager.getString("veterans_entry_answer"));
        questionAnswerMap.put(resourceManager.getString("show_ticket_price"), resourceManager.getString("show_ticket_price_answer"));
        questionAnswerMap.put(resourceManager.getString("contact_phone_number"), resourceManager.getString("contact_phone_number_answer"));
    }

    private void populateQuestionsList() {
        // Populate questions in the list view based on the current locale
        questionsList.getItems().addAll(
                resourceManager.getString("opening_hours"),
                resourceManager.getString("museum_location"),
                resourceManager.getString("current_exhibits"),
                resourceManager.getString("entry_ticket_cost"),
                resourceManager.getString("museum_open_now"),
                resourceManager.getString("veterans_entry"),
                resourceManager.getString("show_ticket_price"),
                resourceManager.getString("contact_phone_number")
        );
    }

    private void displayAnswer() {
        String selectedQuestion = questionsList.getSelectionModel().getSelectedItem();
        if (selectedQuestion != null) {
            String answer = questionAnswerMap.get(selectedQuestion);
            if (answer != null) {
                answerArea.setText(answer);
            } else {
                answerArea.setText(resourceManager.getString("invalid_question"));
            }
        } else {
            answerArea.setText(resourceManager.getString("select_question_first"));
        }
    }

    private void navigateToHomeScreen() {
        // Logic to navigate back to the home screen
        HomeScreen homeScreen = new HomeScreen(primaryStage, true);
        primaryStage.setScene(homeScreen.getScene());
    }

    public Scene getScene() {
        return new Scene(layout, 900, 700); // Return a scene for use in setting the primary stage
    }
}
