package machalica.marcin.timetracker.helper;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import machalica.marcin.timetracker.datapersistence.DataHelper;
import machalica.marcin.timetracker.datapersistence.DataPersistenceStrategy;
import machalica.marcin.timetracker.model.Activity;
import machalica.marcin.timetracker.settings.Settings;
import org.controlsfx.dialog.CommandLinksDialog;

import java.util.Optional;

public class ExitHelper {
    public static boolean exit(ObservableList<Activity> activities, DataPersistenceStrategy dataPersistenceObject) {
        CommandLinksDialog dialog = new CommandLinksDialog();
        dialog.setTitle("Do you want to save?");

        final ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.YES);
        final ButtonType discardButtonType = new ButtonType("Discard", ButtonBar.ButtonData.NO);
        dialog.getDialogPane().getButtonTypes().setAll(saveButtonType, discardButtonType, ButtonType.CANCEL);

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