package machalica.marcin.timetracker.main;

import com.sun.javafx.scene.control.skin.TableHeaderRow;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;
import machalica.marcin.timetracker.datapersistence.*;
import machalica.marcin.timetracker.helper.NotificationHelper;
import machalica.marcin.timetracker.helper.ShorthandSyntaxHelper;
import machalica.marcin.timetracker.model.Activity;
import machalica.marcin.timetracker.settings.DataPersistenceOption;
import machalica.marcin.timetracker.settings.Settings;
import org.controlsfx.dialog.CommandLinksDialog;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
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
    private static DataPersistenceStrategy dataPersistenceObject;

    @FXML
    private void initialize() {
        Main.setOnExit(() -> onExit());

        dateColumn.setCellValueFactory(new PropertyValueFactory<Activity, String>("date"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<Activity, String>("time"));
        infoColumn.setCellValueFactory(new PropertyValueFactory<Activity, String>("info"));
        setupActionButtonsColumnCellFactories();
        activityTable.skinProperty().addListener((observable, oldSkin, newSkin) -> {
            final TableHeaderRow header = (TableHeaderRow) activityTable.lookup("TableHeaderRow");
            header.reorderingProperty().addListener((observable1, oldValue, newValue) -> header.setReordering(false));
        });
        addActivityButton.focusedProperty().addListener(((observable, oldValue, newValue) -> {
            if(!newValue) {
                Platform.runLater(() -> dateInput.requestFocus());
            }
        }));

        activityTable.setItems(activities);

        setDataPersistenceOptionMenuItemsOnAction();
        loadSettings();
        loadData();

        setupDateInputConverter(dateInput);
        setupDateInputListener(dateInput);
        setupAddActivityButtonListeners();
        ShorthandSyntaxHelper.setupShorthandSyntaxListeners(dateInput, timeInput, infoInput);
        setupShortcuts();

        dateInput.setPromptText("DD/MM/YYYY");
        timeInput.setPromptText("0:00");
        Platform.runLater(() -> dateInput.requestFocus());
    }

    private void loadSettings() {
        if(Settings.loadSettings()) { setDataPersistenceOptionAccordingToSettings(); }
    }

    private void setupShortcuts() {
        KeyCombination saveKeyCombination = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);
        Runnable saveRunnable = () -> saveData();
        Platform.runLater(() -> Main.getScene().getAccelerators().put(saveKeyCombination, saveRunnable));

        KeyCombination addActivityKeyCombination = new KeyCodeCombination(KeyCode.ENTER, KeyCombination.CONTROL_DOWN);
        Runnable addActivityRunnable = () -> addActivity();
        Platform.runLater(() -> Main.getScene().getAccelerators().put(addActivityKeyCombination, addActivityRunnable));
    }


    private boolean onExit() {
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
                isDataSaved.set(saveData());
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

    @FXML
    private void exportCsv() {
        CsvStrategy csvStrategy = new CsvStrategy();
        try {
            csvStrategy.save(activities);
            NotificationHelper.showNotification(5, "Data exported","Successfully exported data to \n" + csvStrategy.toString() + ".", "/saveicon.png");
        } catch (IOException ex) {
            ex.printStackTrace();
            NotificationHelper.showNotification(10, "Export error", "Error during exporting data to \n" + csvStrategy.toString() + ".\n" + ex.getMessage(), "/errornotification.png");
        } catch (NullPointerException ex) { }
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
                NotificationHelper.showNotification(10, "Import error", ex.getMessage(), "/errornotification.png");
            });
        } catch (IllegalArgumentException | IOException ex) {
            Platform.runLater(() -> {
                ex.printStackTrace();
                NotificationHelper.showNotification(10, "Import error", "Error during importing data from \n" + csvStrategy.toString() + ".\n" + ex.getMessage(), "/errornotification.png");
            });
        }

        if(activitiesTemp != null && !activitiesTemp.isEmpty()) {
            activities.setAll(activitiesTemp);
            Platform.runLater(() -> {
                NotificationHelper.showNotification(5,"Data imported", "Successfully imported data from \n" + csvStrategy.toString() + ".", "/loadicon.png");
                activityTable.scrollTo(activities.size());
            });
        } else if(activitiesTemp != null && activitiesTemp.isEmpty()) {
            Platform.runLater(() -> {
                String errorMsg = "No data found in \n" + csvStrategy.toString() + ".";
                System.out.println(errorMsg);
                NotificationHelper.showNotification(10, "Import error", errorMsg, "/errornotification.png");
            });
        }
    }

    private void setDataPersistenceOptionAccordingToSettings() {
        switch (Settings.getDataPersistenceDefaultOption()) {
            case SERIALIZATION:
                dataPersistenceObject = new SerializationStrategy();
                Platform.runLater(() -> dataPersistenceOptionSerialization.setSelected(true));
                break;
            case DATABASE:
                dataPersistenceObject = new DatabaseStrategy();
                Platform.runLater(() -> dataPersistenceOptionDatabase.setSelected(true));
                break;
            case TEXT_FILE:
            default:
                dataPersistenceObject = new TextFileStrategy();
                Platform.runLater(() -> dataPersistenceOptionTextFile.setSelected(true));
                break;
        }
    }

    private void setDataPersistenceOptionMenuItemsOnAction() {
        dataPersistenceOptionTextFile.setOnAction(e -> {
            Platform.runLater(() -> {
                dataPersistenceObject = new TextFileStrategy();
                Settings.setDataPersistenceDefaultOption(DataPersistenceOption.TEXT_FILE);
                Settings.saveSettings();
            });
        });
        dataPersistenceOptionSerialization.setOnAction(e -> {
            Platform.runLater(() -> {
                dataPersistenceObject = new SerializationStrategy();
                Settings.setDataPersistenceDefaultOption(DataPersistenceOption.SERIALIZATION);
                Settings.saveSettings();
            });
        });
        dataPersistenceOptionDatabase.setOnAction(e -> {
            Platform.runLater(() -> {
                dataPersistenceObject = new DatabaseStrategy();
                Settings.setDataPersistenceDefaultOption(DataPersistenceOption.DATABASE);
                Settings.saveSettings();
            });
        });
    }

    @FXML
    private boolean saveData() {
        boolean isSaved = false;
        try {
            dataPersistenceObject.save(activities);
            NotificationHelper.showNotification(5, "Data saved","Successfully saved data to \n" + dataPersistenceObject.toString() + ".", "/saveicon.png");
            isSaved = true;
        } catch (IOException ex) {
            ex.printStackTrace();
            NotificationHelper.showNotification(10, "Save error", "Error during saving data to \n" + dataPersistenceObject.toString() + ".\n" + ex.getMessage(), "/errornotification.png");
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            NotificationHelper.showNotification(10, "Save error", "Error during saving data to \n" + dataPersistenceObject.toString() + ".", "/errornotification.png");
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println(ex.getNextException());
            NotificationHelper.showNotification(10, "Save error", "Error during saving data to \n" + dataPersistenceObject.toString() + ".", "/errornotification.png");
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            NotificationHelper.showNotification(10, "Save error", "Error during saving data to \n" + dataPersistenceObject.toString() + ".\n" + ex.getMessage(), "/errornotification.png");
        } finally {
            return isSaved;
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
                NotificationHelper.showNotification(10, "Load error", ex.getMessage(), "/errornotification.png");
            });
        } catch (IllegalArgumentException | IOException ex) {
            Platform.runLater(() -> {
                ex.printStackTrace();
                NotificationHelper.showNotification(10, "Load error", "Error during loading data from \n" + dataPersistenceObject.toString() + ".\n" + ex.getMessage(), "/errornotification.png");
            });
        } catch (ClassNotFoundException ex) {
            Platform.runLater(() -> {
                ex.printStackTrace();
                NotificationHelper.showNotification(10, "Load error", "Error during loading data from \n" + dataPersistenceObject.toString() + ".", "/errornotification.png");
            });
        } catch (SQLException ex) {
            Platform.runLater(() -> {
                ex.printStackTrace();
                System.out.println(ex.getNextException());
                NotificationHelper.showNotification(10, "Load error", "Error during loading data from \n" + dataPersistenceObject.toString() + ".", "/errornotification.png");
            });
        }

        if(activitiesTemp != null && !activitiesTemp.isEmpty()) {
            activities.setAll(activitiesTemp);
            Platform.runLater(() -> {
                NotificationHelper.showNotification(5,"Data loaded", "Successfully loaded data from \n" + dataPersistenceObject.toString() + ".", "/loadicon.png");
                activityTable.scrollTo(activities.size());
            });
        } else if(activitiesTemp != null && activitiesTemp.isEmpty()) {
            Platform.runLater(() -> {
                String errorMsg = "No data found in \n" + dataPersistenceObject.toString() + ".";
                System.out.println(errorMsg);
                NotificationHelper.showNotification(10, "Load error", errorMsg, "/errornotification.png");
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

    private void setupDateInputListener(DatePicker dateInput) {
        KeyCombination openCalendarKeyCode = new KeyCodeCombination(KeyCode.SPACE, KeyCombination.CONTROL_DOWN);
        Runnable openCalendarRunnable = () -> {
            if(dateInput.isFocused()) {
                dateInput.show();
            }
        };
        Platform.runLater(() -> Main.getScene().getAccelerators().put(openCalendarKeyCode, openCalendarRunnable));
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