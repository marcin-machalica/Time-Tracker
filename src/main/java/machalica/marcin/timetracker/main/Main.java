package machalica.marcin.timetracker.main;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.function.BooleanSupplier;

public class Main extends Application {
    public static Stage mainStage;

    @Override
    public void start(Stage primaryStage) throws Exception{
        mainStage = primaryStage;
        Parent root = FXMLLoader.load(getClass().getResource("/Main.fxml"));
        primaryStage.setTitle("Time Tracker");
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.show();
    }

    public static void setOnExit(BooleanSupplier booleanSupplier) {
        mainStage.setOnCloseRequest(e -> {
            boolean shouldExit = booleanSupplier.getAsBoolean();
            if(shouldExit) {
                Platform.exit();
                System.exit(0);
            } else {
                e.consume();
            }
        });
    }

    public static Scene getScene() {
        return mainStage.getScene();
    }

    public static void main(String[] args) {
        launch(args);
    }
}