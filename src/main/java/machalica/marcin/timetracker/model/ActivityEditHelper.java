package machalica.marcin.timetracker.model;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import machalica.marcin.timetracker.helper.ShorthandSyntaxHelper;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.Optional;

public class ActivityEditHelper {
    public static boolean editActivity(Activity activity) {
        final Dialog<Activity> dialog = new Dialog<>();
        dialog.setTitle("Edit");

        final DatePicker editDateInput = new DatePicker();
        editDateInput.setPromptText("DD/MM/YYYY");
        editDateInput.getEditor().setText(activity.getDate());
        DatePickerHelper.setupDatePickerConverter(editDateInput);
        DatePickerHelper.setupShowCalendarListener(editDateInput);

        final TextField editTimeInput = new TextField();
        editTimeInput.setPromptText("0:00");
        editTimeInput.setText(activity.getTime());

        final TextField editInfoInput = new TextField();
        editInfoInput.setPromptText("Info");
        editInfoInput.setText(activity.getInfo());

        ShorthandSyntaxHelper.setupShorthandSyntaxListeners(editDateInput, editTimeInput, editInfoInput);

        final Label editWarningLabel = new Label();
        editWarningLabel.setTextFill(Color.valueOf("#cc3300"));
        final HBox editWarningLabelHBox = new HBox(editWarningLabel);
        editWarningLabelHBox.setAlignment(Pos.CENTER);

        final HBox editInputsHBox = new HBox(10, editDateInput, editTimeInput, editInfoInput);
        final VBox editVBox = new VBox(10, editInputsHBox, editWarningLabelHBox);
        dialog.getDialogPane().setContent(editVBox);

        final ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.YES);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        final Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        final Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);

        saveButton.addEventFilter(ActionEvent.ACTION, e -> {
            if (editDateInput.getValue() == null) {
                editDateInput.requestFocus();
                e.consume();
            } else if (editTimeInput.getText().equals("") || !editTimeInput.getText().matches(Activity.TIME_PATTERN)) {
                editTimeInput.requestFocus();
                e.consume();
            } else if (editInfoInput.getText().trim().equals("") || editInfoInput.getText().contains(";")) {
                editInfoInput.requestFocus();
                e.consume();
            }
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                LocalDate localDate = editDateInput.getValue();
                String time = editTimeInput.getText();
                String info = editInfoInput.getText();

                try {
                    return new Activity(localDate, time, info);
                } catch (DateTimeException | IllegalArgumentException ex) {
                    editWarningLabel.setText(ex.getMessage());
                }
            }
            return null;
        });

        dialog.getDialogPane().addEventFilter(KeyEvent.KEY_PRESSED, k -> {
            if (k.getCode() == KeyCode.ENTER) {
                if (cancelButton.isFocused()) {
                    dialog.close();
                } else if (!saveButton.isFocused()){
                    k.consume();
                }
            }
        });

        Platform.runLater(() -> editDateInput.requestFocus());
        Optional<Activity> result = dialog.showAndWait();

        SimpleBooleanProperty wasEdited = new SimpleBooleanProperty(false);

        result.ifPresent(editedActivity -> {
            editWarningLabel.setText("");
            activity.setDate(editedActivity.getLocalDate());
            activity.setTime(editedActivity.getTime());
            activity.setInfo(editedActivity.getInfo());
            wasEdited.set(true);
        });
        return wasEdited.get();
    }
}