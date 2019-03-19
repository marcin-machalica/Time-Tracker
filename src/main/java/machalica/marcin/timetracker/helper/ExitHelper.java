package machalica.marcin.timetracker.helper;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import machalica.marcin.timetracker.datapersistence.DataHelper;
import machalica.marcin.timetracker.datapersistence.DataPersistenceStrategy;
import machalica.marcin.timetracker.model.Activity;
import machalica.marcin.timetracker.settings.Settings;

import java.util.Optional;

public class ExitHelper {
    public static boolean exit(ObservableList<Activity> activities, DataPersistenceStrategy dataPersistenceObject) {
        Dialog dialog = new Dialog();
        dialog.setTitle("Do you want to save?");
        dialog.getDialogPane().getStylesheets().add("dark-theme.css");

        DialogHelper.centerDialog(dialog, 370.0, 124.0);

        final ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.YES);
        final ButtonType discardButtonType = new ButtonType("Discard", ButtonBar.ButtonData.NO);
        dialog.getDialogPane().getButtonTypes().setAll(saveButtonType, discardButtonType, ButtonType.CANCEL);

        final Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        final Button discardButton = (Button) dialog.getDialogPane().lookupButton(discardButtonType);
        final Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        saveButton.getStyleClass().add("save-button");
        discardButton.getStyleClass().add("discard-button");
        cancelButton.getStyleClass().add("cancel-button");

        saveButton.translateXProperty().bind(saveButton.minWidthProperty().divide(-6));
        discardButton.translateXProperty().bind(discardButton.minWidthProperty().divide(-6));
        cancelButton.translateXProperty().bind(cancelButton.minWidthProperty().divide(-6));
        dialog.getDialogPane().setMinSize(370, 50);

        dialog.setResultConverter(dialogButton -> dialogButton);
        Optional<ButtonType> result = dialog.showAndWait();

        SimpleBooleanProperty areSettingsSaved = new SimpleBooleanProperty(false);
        SimpleBooleanProperty isDataSaved = new SimpleBooleanProperty(false);
        SimpleBooleanProperty isDiscarded = new SimpleBooleanProperty(false);

        result.ifPresent(clickedButton -> {
            areSettingsSaved.set(Settings.saveSettings());
            if(clickedButton == saveButtonType) {
                isDataSaved.set(DataHelper.saveData(activities, dataPersistenceObject));
            } else if(clickedButton == discardButtonType) {
                isDiscarded.set(true);
            }
        });

        if(isDiscarded.get() || (areSettingsSaved.get() && isDataSaved.get())) {
            return true;
        } else {
            return false;
        }
    }
}