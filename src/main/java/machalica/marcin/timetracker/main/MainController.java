package machalica.marcin.timetracker.main;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javafx.util.StringConverter;
import machalica.marcin.timetracker.datapersistence.DataPersistenceStrategy;
import machalica.marcin.timetracker.datapersistence.TextFileStrategy;
import machalica.marcin.timetracker.model.Activity;
import org.controlsfx.control.Notifications;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.Optional;

public class MainController {
    @FXML
    private TableView activityTable;
    @FXML
    private TableColumn dateColumn;
    @FXML
    private TableColumn timeColumn;
    @FXML
    private TableColumn infoColumn;
    @FXML
    private TableColumn<Activity, Activity> actionButtonsColumn;
    @FXML
    private DatePicker dateInput;
    @FXML
    private TextField timeInput;
    @FXML
    private TextField infoInput;
    @FXML
    private Button addActivityButton;
    @FXML
    private Label warningLabel;

    private ObservableList<Activity> activities = FXCollections.observableArrayList();
    private DataPersistenceStrategy dataPersistenceObject;

    @FXML
    private void initialize() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<Activity, String>("date"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<Activity, String>("time"));
        infoColumn.setCellValueFactory(new PropertyValueFactory<Activity, String>("info"));
        setupActionButtonsColumnCellFactories();

        activityTable.setItems(activities);

        dataPersistenceObject = new TextFileStrategy();
        Main.setOnExit(() -> {
            try {
                dataPersistenceObject.save(activities);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        setupDateInputConverter(dateInput);
        setupDateInputListener(dateInput);
        setupAddActivityButtonListeners();

        dateInput.setPromptText("DD/MM/YYYY");
        timeInput.setPromptText("00:00");
        Platform.runLater(() -> dateInput.requestFocus());
    }

    @FXML
    private void saveData() {
        Image img = null;
        try {
            img = new Image(getClass().getResource("/saveicon.png").toURI().toString());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        Notifications notificationBuilder = Notifications.create()
                .title("Data saved")
                .text("Saved data to " + dataPersistenceObject.toString() + ".")
                .graphic(new ImageView(img))
                .hideAfter(Duration.seconds(5))
                .position(Pos.BOTTOM_RIGHT);
        notificationBuilder.darkStyle();
        notificationBuilder.show();
        try {
            dataPersistenceObject.save(activities);
        } catch (IOException e) {
            warningLabel.setText(e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void showAbout() {
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

    private void setupDateInputListener(DatePicker dateInput) {
        dateInput.setOnKeyReleased(k -> {
            if(k.getCode() == KeyCode.ENTER) {
                dateInput.show();
            }
        });
    }

    private void setupActionButtonsColumnCellFactories() {
        actionButtonsColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));

        actionButtonsColumn.setCellFactory(param -> new TableCell<Activity, Activity>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox actionButtonsHBox = new HBox(10, editButton, deleteButton);

            @Override
            protected void updateItem(Activity activity, boolean empty) {
                super.updateItem(activity, empty);

                if (activity == null) {
                    setGraphic(null);
                    return;
                }

                setGraphic(actionButtonsHBox);

                deleteButton.setOnAction(e -> getTableView().getItems().remove(activity));
                editButton.setOnAction(e -> editActivity(activity));
            }
        });
    }

    private void editActivity(Activity activity) {
        final Dialog<Activity> dialog = new Dialog<>();
        dialog.setTitle("Edit");

        final DatePicker editDateInput = new DatePicker();
        editDateInput.setPromptText("DD/MM/YYYY");
        editDateInput.getEditor().setText(activity.getDate());
        setupDateInputConverter(editDateInput);
        setupDateInputListener(editDateInput);

        final TextField editTimeInput = new TextField();
        editTimeInput.setPromptText("00:00");
        editTimeInput.setText(activity.getTime());

        final TextField editInfoInput = new TextField();
        editInfoInput.setPromptText("Info");
        editInfoInput.setText(activity.getInfo());

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
            } else if (editInfoInput.getText().equals("")){
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

        result.ifPresent(editedActivity -> {
            editWarningLabel.setText("");
            activity.setDate(editedActivity.getLocalDate());
            activity.setTime(editedActivity.getTime());
            activity.setInfo(editedActivity.getInfo());
            activityTable.refresh();
        });
    }

    private void setupDateInputConverter(DatePicker dateInput) {
        dateInput.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate localDate) {
                return localDate == null ? null : Activity.DATE_TIME_FORMATTER.format(localDate);
            }

            @Override
            public LocalDate fromString(String dateString) {
                if(dateString == null || dateString.trim().isEmpty()) {
                    return null;
                }
                return LocalDate.parse(dateString, Activity.DATE_TIME_FORMATTER);
            }
        });
    }

    private void addActivity() {
        LocalDate localDate = dateInput.getValue();
        String time = timeInput.getText();
        String info = infoInput.getText();

        if (localDate == null) {
            dateInput.requestFocus();
            return;
        } else if (time.equals("") || !time.matches(Activity.TIME_PATTERN)) {
            timeInput.requestFocus();
            return;
        } else if (info.equals("")){
            infoInput.requestFocus();
            return;
        }

        try {
            activities.add(new Activity(
                    localDate,
                    time,
                    info
            ));
            dateInput.setValue(null);
            timeInput.clear();
            infoInput.clear();
            warningLabel.setText("");
        } catch (DateTimeException | IllegalArgumentException ex) {
            warningLabel.setText(ex.getMessage());
        } finally {
            dateInput.requestFocus();
        }
    }

    private void setupAddActivityButtonListeners() {
        addActivityButton.setOnAction(e -> {
            addActivity();
        });

        addActivityButton.setOnKeyReleased(k -> {
            if(k.getCode() == KeyCode.ENTER) {
                addActivity();
            }
        });
    }
}