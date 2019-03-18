package machalica.marcin.timetracker.helper;

import javafx.geometry.Pos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import machalica.marcin.timetracker.model.Activity;

public class AboutHelper {
    public static void showAbout() {
        final Dialog<Activity> dialog = new Dialog<>();
        dialog.setTitle("About");

        final Label aboutLabel = new Label();
        aboutLabel.setText("Time Tracker created by Marcin Machalica\nIcons by icons8 (https://icons8.com)");
        final HBox aboutLabelHBox = new HBox(aboutLabel);
        aboutLabelHBox.setAlignment(Pos.CENTER);

        dialog.getDialogPane().setContent(aboutLabelHBox);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);

        dialog.show();
    }
}