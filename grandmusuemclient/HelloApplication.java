package com.example.grandmusuemclient;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.application.Application;
import javafx.stage.Stage;


public class HelloApplication extends Application {

    private static HostServices hostServices;
    @Override
    public void start(Stage primaryStage) {
        hostServices = getHostServices();
        // Create HomeScreen instance and pass the primaryStage
        new HomeScreen(primaryStage , false);

        // Show the primary stage
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
    public static HostServices getHostServicesInstance() {
        return hostServices;
    }

}

