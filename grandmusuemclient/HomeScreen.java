package com.example.grandmusuemclient;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.control.ComboBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.net.Socket;
import java.util.ResourceBundle;

import static com.example.grandmusuemclient.HelloApplication.getHostServicesInstance;

public class HomeScreen {

    private Scene scene;
    private int currentImageIndex = 0;  // To track the current image
    private Stage primaryStage;
    private boolean isLoggedIn; // Boolean to track login status

    private Button leftButton;
    private Button rightButton;
    private Button profileButton;
    private Button footerLeftButton;
    private Button footerRightButton;
    private Button playPauseButton;
    private ComboBox<String> languageComboBox;
    private Button logoutButton;

    private ImageView secondImageView;
    private Timeline secondImageSliderTimeline;
    private int currentSecondImageIndex = 0;
    public HomeScreen(Stage primaryStage, boolean isLoggedIn) {
        this.primaryStage = primaryStage;
        this.isLoggedIn = isLoggedIn; // Initialize login status

        // Initialize layout
        VBox layout = new VBox();
        layout.setPadding(new Insets(0));

        // Create a navigation bar
        HBox navBar = new HBox();
        navBar.setStyle("-fx-background-color: #cda34b; -fx-padding: 10px; -fx-alignment: center; -fx-min-height: 60px;");
        navBar.setPadding(new Insets(10));
        navBar.setSpacing(10); // Add spacing between items

        // Logo image
        Image logoImage = new Image(getClass().getResourceAsStream("/Museum Logo.png")); // Ensure logo.png is in your resources
        ImageView logo = new ImageView(logoImage);
        logo.setFitHeight(100); // Adjust size as needed
        logo.setPreserveRatio(true);

        // Buttons
         leftButton = new Button(ResourceManager.getString(isLoggedIn ? "chatRoom" : "signUp"));
         rightButton = new Button(ResourceManager.getString(isLoggedIn ? "chatBot" : "login"));
         profileButton = new Button(ResourceManager.getString("profile"));
        logoutButton = new Button(ResourceManager.getString("logout"));

        // Style buttons
        leftButton.getStyleClass().add("gold-button");
        rightButton.getStyleClass().add("gold-button");
        profileButton.getStyleClass().add("gold-button");
        logoutButton.getStyleClass().add("gold-button");

        // Layout for buttons in nav bar
        HBox buttonBox ;
        if (isLoggedIn){
            buttonBox = new HBox (10 , leftButton , rightButton , profileButton , logoutButton);
        }else {
            buttonBox = new HBox(10 , leftButton , rightButton);
        }




        buttonBox.setAlignment(Pos.CENTER_RIGHT);


        // Create language selection ComboBox
         languageComboBox = new ComboBox<>();
        ObservableList<String> languages = FXCollections.observableArrayList("English", "Arabic", "Spanish", "French", "German");
        languageComboBox.setItems(languages);
        languageComboBox.setValue("English"); // Default value

        languageComboBox.setOnAction(e -> {
            String selectedLanguage = languageComboBox.getValue();
            ResourceManager.setLanguage(selectedLanguage);
            updateText(); // Refresh UI text
        });

        // Add elements to navigation bar
        navBar.getChildren().addAll(logo, buttonBox , languageComboBox);

        // Create video section
        Media videoMedia = new Media(getClass().getResource("/GrandMus.mp4").toExternalForm());
        MediaPlayer mediaPlayer = new MediaPlayer(videoMedia);
        MediaView mediaView = new MediaView(mediaPlayer);

        // Create a pane for the video
        StackPane videoPane = new StackPane();
        videoPane.setPadding(new Insets(10));
        videoPane.getChildren().add(mediaView);

        // Bind MediaView width to the scene width
        mediaView.fitWidthProperty().bind(primaryStage.widthProperty());
        mediaView.setPreserveRatio(true);

        // Create controls for the video
        HBox controlsBox = new HBox();
        controlsBox.setAlignment(Pos.CENTER);
        controlsBox.setPadding(new Insets(10));

         playPauseButton = new Button(">");
        Slider timeSlider = new Slider();
        Label timeLabel = new Label("0:00");

        playPauseButton.setOnAction(e -> {
            if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();
                playPauseButton.setText("||");
            } else {
                mediaPlayer.play();
                playPauseButton.setText(">");
            }
        });

        // Update timeSlider and timeLabel with the current time
        mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
            if (!timeSlider.isValueChanging()) {
                timeSlider.setValue(newTime.toMillis());
            }
            timeLabel.setText(formatTime(newTime, mediaPlayer.getTotalDuration()));
        });

        // Update timeSlider when the user interacts with it
        timeSlider.setOnMouseReleased(e -> {
            mediaPlayer.seek(Duration.millis(timeSlider.getValue()));
        });

        // Update slider max value when media duration changes
        mediaPlayer.totalDurationProperty().addListener((obs, oldDuration, newDuration) -> {
            timeSlider.setMax(newDuration.toMillis());
        });

        // Add controls to the controlsBox
        controlsBox.getChildren().addAll(playPauseButton, timeSlider, timeLabel);

        // Image slider section
        ImageView imageView = new ImageView();
        imageView.setPreserveRatio(true);

        // Bind ImageView size to the scene size
        imageView.fitWidthProperty().bind(primaryStage.widthProperty().multiply(0.8)); // Slightly less than full width
        imageView.fitHeightProperty().bind(primaryStage.heightProperty().subtract(200)); // Adjusting height for other components

        // Load images
        String[] images = {
                "/path_to_image_1.jpg",  // Replace with actual image paths
                "/path_to_image_2.jpg",
                "/path_to_image_3.jpg",
                "/path_to_image_4.jpg"
        };

        // Function to update the image in the ImageView
        imageView.setImage(new Image(getClass().getResourceAsStream(images[currentImageIndex])));

        // Timeline to auto-slide images
        Timeline imageSliderTimeline = new Timeline(
                new KeyFrame(Duration.seconds(5), event -> {
                    currentImageIndex = (currentImageIndex + 1) % images.length;
                    imageView.setImage(new Image(getClass().getResourceAsStream(images[currentImageIndex])));
                })
        );
        imageSliderTimeline.setCycleCount(Timeline.INDEFINITE); // Loop indefinitely
        imageSliderTimeline.play();

        // Navigation buttons for image slider
        Button prevButton = new Button("<");
        Button nextButton = new Button(">");

        prevButton.setOnAction(e -> {
            currentImageIndex = (currentImageIndex - 1 + images.length) % images.length;
            imageView.setImage(new Image(getClass().getResourceAsStream(images[currentImageIndex])));
        });

        nextButton.setOnAction(e -> {
            currentImageIndex = (currentImageIndex + 1) % images.length;
            imageView.setImage(new Image(getClass().getResourceAsStream(images[currentImageIndex])));
        });

        // Layout for the image slider and navigation buttons
        HBox sliderBox = new HBox(10, prevButton, imageView, nextButton);
        sliderBox.setAlignment(Pos.CENTER);
        sliderBox.setPadding(new Insets(10));
        sliderBox.setMaxWidth(Double.MAX_VALUE); // Make the slider box stretch

        HBox.setHgrow(imageView, Priority.ALWAYS); // Allow ImageView to grow within HBox
        HBox.setHgrow(prevButton, Priority.NEVER); // Don't let buttons grow
        HBox.setHgrow(nextButton, Priority.NEVER);

        // Create second image slider section
        secondImageView = new ImageView();
        secondImageView.setPreserveRatio(true);
        secondImageView.fitWidthProperty().bind(primaryStage.widthProperty().multiply(0.8));
        secondImageView.fitHeightProperty().bind(primaryStage.heightProperty().multiply(0.3)); // Adjust as needed

        // Load images for the second slider
        String[] secondImages = {
                "/path_to_second_image_1.png",  // Replace with actual image paths
                "/path_to_second_image_2.png"
        };

        // Function to update the image in the second ImageView
        secondImageView.setImage(new Image(getClass().getResourceAsStream(secondImages[currentSecondImageIndex])));

        // Timeline to auto-slide images in the second slider
        secondImageSliderTimeline = new Timeline(
                new KeyFrame(Duration.seconds(5), event -> {
                    currentSecondImageIndex = (currentSecondImageIndex + 1) % secondImages.length;
                    secondImageView.setImage(new Image(getClass().getResourceAsStream(secondImages[currentSecondImageIndex])));
                })
        );
        secondImageSliderTimeline.setCycleCount(Timeline.INDEFINITE);
        secondImageSliderTimeline.play();

        // Add click events to open URLs
        secondImageView.setOnMouseClicked(event -> {
            String[] urls = {
                    "https://visit-gem.com/ar/briefTours",  // Replace with actual URLs
                    "https://visit-gem.com/ar/briefChildren"
            };
            if (currentSecondImageIndex < urls.length) {
                getHostServicesInstance().showDocument(urls[currentSecondImageIndex]);
            }
        });

        // Navigation buttons for the second image slider
        Button secondPrevButton = new Button("<");
        Button secondNextButton = new Button(">");

        secondPrevButton.setOnAction(e -> {
            currentSecondImageIndex = (currentSecondImageIndex - 1 + secondImages.length) % secondImages.length;
            secondImageView.setImage(new Image(getClass().getResourceAsStream(secondImages[currentSecondImageIndex])));
        });

        secondNextButton.setOnAction(e -> {
            currentSecondImageIndex = (currentSecondImageIndex + 1) % secondImages.length;
            secondImageView.setImage(new Image(getClass().getResourceAsStream(secondImages[currentSecondImageIndex])));
        });

        // Layout for the second image slider and navigation buttons
        HBox secondSliderBox = new HBox(10, secondPrevButton, secondImageView, secondNextButton);
        secondSliderBox.setAlignment(Pos.CENTER);
        secondSliderBox.setPadding(new Insets(10));
        secondSliderBox.setMaxWidth(Double.MAX_VALUE);

        HBox.setHgrow(secondImageView, Priority.ALWAYS);
        HBox.setHgrow(secondPrevButton, Priority.NEVER);
        HBox.setHgrow(secondNextButton, Priority.NEVER);


        // Create footer section (always visible)
        HBox footer = new HBox();
        footer.setStyle("-fx-background-color: #cda34b; -fx-padding: 10px; -fx-alignment: center; -fx-min-height: 60px;");
        footer.setPadding(new Insets(10));
        footer.setSpacing(10); // Add spacing between items

        // Footer image
        Image footerImage = new Image(getClass().getResourceAsStream("/img.jfif")); // Ensure footer image is in your resources
        ImageView footerImageView = new ImageView(footerImage);
        footerImageView.setFitHeight(50); // Adjust size as needed
        footerImageView.setPreserveRatio(true);

        // Footer buttons
         footerLeftButton = new Button(ResourceManager.getString(isLoggedIn ? "chatRoom" : "signUp"));
         footerRightButton = new Button(ResourceManager.getString(isLoggedIn ? "chatBot" : "login"));


        // Style footer buttons
        footerLeftButton.getStyleClass().add("gold-button");
        footerRightButton.getStyleClass().add("gold-button");

        // Layout for footer buttons
        HBox footerButtonBox = new HBox(10, footerLeftButton, footerRightButton);
        footerButtonBox.setAlignment(Pos.CENTER_RIGHT);

        // Add elements to footer
        footer.getChildren().addAll(footerImageView, footerButtonBox);

        // Add navigation bar, video, controls, image slider, and footer to layout
        layout.getChildren().addAll(navBar, videoPane, controlsBox, sliderBox,secondSliderBox, footer);

        // Wrap the layout in a ScrollPane
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(layout);
        scrollPane.setFitToWidth(true); // Makes sure the content width fits the viewport
        scrollPane.setFitToHeight(true); // Makes sure the content height fits the viewport
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // Disable horizontal scrollbar

        // Create scene and set it to the primary stage
        scene = new Scene(scrollPane, 900, 700); // Initialize with scrollPane for scrolling
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.setTitle("Grand Egyptian Museum");

        // Set up button actions
        leftButton.setOnAction(e -> {
            if (isLoggedIn) {
                navigateToChatRoomScreen();
            } else {
                navigateToSignUpScreen();
            }
        });

        rightButton.setOnAction(e -> {
            if (isLoggedIn) {
                navigateToChatBotScreen();
            } else {
                navigateToLoginScreen();
            }
        });
        profileButton.setOnAction(e -> {
            navigateToProfileScreen();
        });

        logoutButton.setOnAction(e -> logout());


        // Footer button actions (same logic as nav buttons)
        footerLeftButton.setOnAction(leftButton.getOnAction());
        footerRightButton.setOnAction(rightButton.getOnAction());








    }

    private void updateText() {
        // Update the text on navigation bar buttons
        leftButton.setText(ResourceManager.getString(isLoggedIn ? "chatRoom" : "signUp"));
        rightButton.setText(ResourceManager.getString(isLoggedIn ? "chatBot" : "login"));
        profileButton.setText(ResourceManager.getString("profile"));
        logoutButton.setText(ResourceManager.getString("logout"));
        // Update the text on footer buttons
        footerLeftButton.setText(ResourceManager.getString(isLoggedIn ? "chatRoom" : "signUp"));
        footerRightButton.setText(ResourceManager.getString(isLoggedIn ? "chatBot" : "login"));

        // Update other UI elements if necessary
        languageComboBox.setValue(ResourceManager.getString("currentLanguage"));
    }


    public Scene getScene() {
        return scene;
    }




    private String formatTime(Duration elapsed, Duration total) {
        int totalSecs = (int) Math.floor(total.toSeconds());
        int elapsedSecs = (int) Math.floor(elapsed.toSeconds());
        int minutes = elapsedSecs / 60;
        int seconds = elapsedSecs % 60;
        int totalMinutes = totalSecs / 60;
        int totalSeconds = totalSecs % 60;
        return String.format("%02d:%02d / %02d:%02d", minutes, seconds, totalMinutes, totalSeconds);
    }

    private void navigateToSignUpScreen() {
        SignUpScreen signUpScreen = new SignUpScreen(primaryStage);
        primaryStage.setScene(signUpScreen.getScene());
    }

    private void navigateToLoginScreen() {
        LoginScreen loginScreen = new LoginScreen(primaryStage);
        primaryStage.setScene(loginScreen.getScene());
    }

    private void navigateToChatRoomScreen() {

        ChattingRoom chattingRoom = new ChattingRoom(primaryStage);
        primaryStage.setScene(chattingRoom.getScene());
        System.out.println("Navigating to Chat Room screen...");
    }

    private void navigateToChatBotScreen() {
        ChatbotScreen chatbotScreen = new ChatbotScreen(primaryStage);
        primaryStage.setScene(chatbotScreen.getScene());
        System.out.println("Navigating to Chat Bot screen...");
    }
    private void navigateToProfileScreen(){
        ProfileScreen profileScreen = new ProfileScreen(primaryStage);
        primaryStage.setScene(profileScreen.getScene());

    }

    private void logout() {
        try {
            // Send logout command to the server
            SocketManager.send("LOGOUT");

            // Close the socket connection
            SocketManager.close();

            // Redirect to the home screen
            primaryStage.close(); // Replace with actual home screen scene
            new HomeScreen(primaryStage , false);

            // Show the primary stage
            primaryStage.show();


        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
