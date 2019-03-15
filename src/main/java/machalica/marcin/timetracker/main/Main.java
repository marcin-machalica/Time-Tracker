package machalica.marcin.timetracker.main;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    private static Stage mainStage;

    @Override
    public void start(Stage primaryStage) throws Exception{
        mainStage = primaryStage;
        Parent root = FXMLLoader.load(getClass().getResource("/Main.fxml"));
        primaryStage.setTitle("Time Tracker");
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.show();
    }

    public static void setOnExit(Runnable runnable) {
        mainStage.setOnCloseRequest(e -> {
            runnable.run();
            Platform.exit();
            System.exit(0);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}