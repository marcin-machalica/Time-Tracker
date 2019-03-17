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
import machalica.marcin.timetracker.datapersistence.CsvStrategy;
import machalica.marcin.timetracker.datapersistence.DataPersistenceStrategy;
import machalica.marcin.timetracker.datapersistence.SerializationStrategy;
import machalica.marcin.timetracker.datapersistence.TextFileStrategy;
import machalica.marcin.timetracker.helper.DataPersistenceOption;
import machalica.marcin.timetracker.helper.Settings;
import machalica.marcin.timetracker.model.Activity;
import org.controlsfx.control.Notifications;

import java.io.FileNotFoundException;
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
    @FXML
    private RadioMenuItem dataPersistenceOptionTextFile;
    @FXML
    private RadioMenuItem dataPersistenceOptionSerialization;
    @FXML
    private RadioMenuItem dataPersistenceOptionDatabase;

    private ObservableList<Activity> activities = FXCollections.observableArrayList();
    private DataPersistenceStrategy dataPersistenceObject;

    @FXML
    private void initialize() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<Activity, String>("date"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<Activity, String>("time"));
        infoColumn.setCellValueFactory(new PropertyValueFactory<Activity, String>("info"));
        setupActionButtonsColumnCellFactories();

        activityTable.setItems(activities);

        setDataPersistenceOptionMenuItemsOnAction();
        loadSettings();
        loadData();

        Main.setOnExit(() -> {
            saveSettings();
            saveData();
        });

        setupDateInputConverter(dateInput);
        setupDateInputListener(dateInput);
        setupAddActivityButtonListeners();

        dateInput.setPromptText("DD/MM/YYYY");
        timeInput.setPromptText("00:00");
        Platform.runLater(() -> dateInput.requestFocus());
    }

    @FXML
    private void exportCsv() {
        CsvStrategy csvStrategy = new CsvStrategy();
        try {
            csvStrategy.save(activities);
            showNotification(5, "Data exported","Successfully exported data to \n" + csvStrategy.toString() + ".", "/saveicon.png");
        } catch (IOException ex) {
            ex.printStackTrace();
            showNotification(10, "Export error", "Error during exporting data to \n" + csvStrategy.toString() + ".\n" + ex.getMessage(), "/errornotification.png");
        }
    }

    @FXML
    private void importCsv() {
        CsvStrategy csvStrategy = new CsvStrategy();

        ObservableList<Activity> activitiesTemp = null;
        try {
            activitiesTemp = csvStrategy.load();
        } catch (FileNotFoundException ex) {
            Platform.runLater(() -> {
                System.out.println(ex.getMessage());
                showNotification(10, "Import error", ex.getMessage(), "/errornotification.png");
            });
        } catch (IllegalArgumentException | IOException ex) {
            Platform.runLater(() -> {
                ex.printStackTrace();
                showNotification(10, "Import error", "Error during importing data from \n" + csvStrategy.toString() + ".\n" + ex.getMessage(), "/errornotification.png");
            });
        }

        if(activitiesTemp != null && !activitiesTemp.isEmpty()) {
            activities.setAll(activitiesTemp);
            Platform.runLater(() -> {
                showNotification(5,"Data imported", "Successfully imported data from \n" + csvStrategy.toString() + ".", "/loadicon.png");
            });
        } else if(activitiesTemp != null && activitiesTemp.isEmpty()) {
            Platform.runLater(() -> {
                String errorMsg = "No data found in \n" + csvStrategy.toString() + ".";
                System.out.println(errorMsg);
                showNotification(10, "Import error", errorMsg, "/errornotification.png");
            });
        }
    }

    private void setDataPersistenceOptionAccordingToSettings() {
        switch (Settings.getDataPersistenceDefaultOption()) {
            case SERIALIZATION:
                dataPersistenceObject = new SerializationStrategy();
                Platform.runLater(() -> {
                    dataPersistenceOptionSerialization.setSelected(true);
                });
                break;
            case DATABASE:
                System.out.println("database");
                Platform.runLater(() -> {
                    dataPersistenceOptionDatabase.setSelected(true);
                });
                break;
            case TEXT_FILE:
            default:
                dataPersistenceObject = new TextFileStrategy();
                Platform.runLater(() -> {
                    dataPersistenceOptionTextFile.setSelected(true);
                });
                break;
        }
    }

    private void setDataPersistenceOptionMenuItemsOnAction() {
        dataPersistenceOptionTextFile.setOnAction(e -> {
            Platform.runLater(() -> {
                dataPersistenceObject = new TextFileStrategy();
                Settings.setDataPersistenceDefaultOption(DataPersistenceOption.TEXT_FILE);
                saveSettings();
            });
        });
        dataPersistenceOptionSerialization.setOnAction(e -> {
            Platform.runLater(() -> {
                dataPersistenceObject = new SerializationStrategy();
                Settings.setDataPersistenceDefaultOption(DataPersistenceOption.SERIALIZATION);
                saveSettings();
            });
        });
        dataPersistenceOptionDatabase.setOnAction(e -> {
            Platform.runLater(() -> {
                System.out.println("database");
                Settings.setDataPersistenceDefaultOption(DataPersistenceOption.DATABASE);
                saveSettings();
            });
        });
    }

    private void loadSettings() {
        try {
            Settings.loadSettings();
            Platform.runLater(() -> {
                showNotification(5,"Settings loaded", "Successfully loaded settings.", "/loadiconsettings.png");
            });
        } catch (FileNotFoundException ex) {
            Platform.runLater(() -> {
                System.out.println(ex.getMessage());
                showNotification(10, "Load error", ex.getMessage(), "/errornotification.png");
            });
        } catch (ClassNotFoundException ex) {
            Platform.runLater(() -> {
                ex.printStackTrace();
                showNotification(10, "Load error", "Cannot load settings.", "/errornotification.png");
            });
        } catch (IOException ex) {
            Platform.runLater(() -> {
                ex.printStackTrace();
                showNotification(10, "Load error", "Cannot load settings.", "/errornotification.png");
            });
        } finally {
            setDataPersistenceOptionAccordingToSettings();
            saveSettings();
        }
    }

    private void saveSettings() {
        try {
            Settings.saveSettings();
        } catch (IOException ex) {
            ex.printStackTrace();
            showNotification(10, "Save error", "Error during saving settings", "/errornotification.png");
        }
    }

    @FXML
    private void saveData() {
        try {
            dataPersistenceObject.save(activities);
            showNotification(5, "Data saved","Successfully saved data to \n" + dataPersistenceObject.toString() + ".", "/saveicon.png");
        } catch (IOException ex) {
            ex.printStackTrace();
            showNotification(10, "Save error", "Error during saving data to \n" + dataPersistenceObject.toString() + ".\n" + ex.getMessage(), "/errornotification.png");
        }
    }

    @FXML
    private void loadData() {
        ObservableList<Activity> activitiesTemp = null;
        try {
            activitiesTemp = dataPersistenceObject.load();
        } catch (FileNotFoundException ex) {
            Platform.runLater(() -> {
                System.out.println(ex.getMessage());
                showNotification(10, "Load error", ex.getMessage(), "/errornotification.png");
            });
        } catch (IllegalArgumentException | IOException ex) {
            Platform.runLater(() -> {
                ex.printStackTrace();
                showNotification(10, "Load error", "Error during loading data from \n" + dataPersistenceObject.toString() + ".\n" + ex.getMessage(), "/errornotification.png");
            });
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            showNotification(10, "Load error", "Error during loading data from \n" + dataPersistenceObject.toString() + ".", "/errornotification.png");
        }

        if(activitiesTemp != null && !activitiesTemp.isEmpty()) {
            activities.setAll(activitiesTemp);
            Platform.runLater(() -> {
                showNotification(5,"Data loaded", "Successfully loaded data from \n" + dataPersistenceObject.toString() + ".", "/loadicon.png");
            });
        } else if(activitiesTemp != null && activitiesTemp.isEmpty()) {
            Platform.runLater(() -> {
                String errorMsg = "No data found in \n" + dataPersistenceObject.toString() + ".";
                System.out.println(errorMsg);
                showNotification(10, "Load error", errorMsg, "/errornotification.png");
            });
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

    private void showNotification(int duration, String title, String text, String imgPath) {
        Image img = null;
        try {
            img = new Image(getClass().getResource(imgPath).toURI().toString());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        Notifications notificationBuilder = Notifications.create()
                .title(title)
                .text(text)
                .graphic(new ImageView(img))
                .hideAfter(Duration.seconds(duration))
                .position(Pos.BOTTOM_RIGHT);
        notificationBuilder.darkStyle();
        notificationBuilder.show();
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
        } else if (info.trim().equals("") || info.contains(";")) {
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