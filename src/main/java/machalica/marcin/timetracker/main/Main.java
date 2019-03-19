package machalica.marcin.timetracker.main;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.function.BooleanSupplier;

public class Main extends Application {
    private static Stage mainStage;

    @Override
    public void start(Stage primaryStage) throws Exception{
        mainStage = primaryStage;
        Parent root = FXMLLoader.load(getClass().getResource("/Main.fxml"));
        primaryStage.setTitle("Time Tracker");

        Scene scene = new Scene(root, 610, 500);
        scene.getStylesheets().add("dark-theme.css");

        primaryStage.setMinWidth(610);
        primaryStage.setMinHeight(500);
        primaryStage.setMaxWidth(800);

        primaryStage.setScene(scene);
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

    public static void setOnResize(Runnable runnable) {
        ChangeListener<Number> stageSizeListener = (observable, oldValue, newValue) -> {
            runnable.run();
        };

        mainStage.widthProperty().addListener(stageSizeListener);
        mainStage.heightProperty().addListener(stageSizeListener);
    }

    public static double getPosX() {
        return mainStage.getX();
    }

    public static double getPosY() {
        return mainStage.getY();
    }

    public static double getWidth() {
        return mainStage.getWidth();
    }

    public static double getHeight() {
        return mainStage.getHeight();
    }

    public static Scene getScene() {
        return mainStage.getScene();
    }

    public static File getCsvFromFileChooser(boolean isSaveDialog) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        if(isSaveDialog) {
            fileChooser.setTitle("Export CSV");
            return fileChooser.showSaveDialog(Main.mainStage);
        } else {
            fileChooser.setTitle("Import CSV");
            return fileChooser.showOpenDialog(Main.mainStage);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}