package machalica.marcin.timetracker.main;

import com.sun.javafx.scene.control.skin.TableHeaderRow;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import machalica.marcin.timetracker.datapersistence.*;
import machalica.marcin.timetracker.helper.AboutHelper;
import machalica.marcin.timetracker.helper.ExitHelper;
import machalica.marcin.timetracker.helper.ShorthandSyntaxHelper;
import machalica.marcin.timetracker.model.Activity;
import machalica.marcin.timetracker.model.ActivityEditHelper;
import machalica.marcin.timetracker.model.DatePickerHelper;
import machalica.marcin.timetracker.settings.DataPersistenceOption;
import machalica.marcin.timetracker.settings.Settings;

import java.time.DateTimeException;
import java.time.LocalDate;

public class MainController {
    @FXML
    private TableView activityTable;
    @FXML
    private TableColumn<Activity, String> dateColumn;
    @FXML
    private TableColumn<Activity, String> timeColumn;
    @FXML
    private TableColumn<Activity, String> infoColumn;
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
    @FXML
    private MenuItem saveDataMenuItem;
    @FXML
    private MenuItem loadDataMenuItem;
    @FXML
    private MenuItem exportCsvMenuItem;
    @FXML
    private MenuItem importCsvMenuItem;
    @FXML
    private MenuItem aboutMenuItem;

    private ObservableList<Activity> activities = FXCollections.observableArrayList();
    private static DataPersistenceStrategy dataPersistenceObject;

    @FXML
    private void initialize() {
        Main.setOnExit(() -> ExitHelper.exit(activities, dataPersistenceObject));
        initialSetup();
        Platform.runLater(() -> dateInput.requestFocus());
    }

    private void initialSetup() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        infoColumn.setCellValueFactory(new PropertyValueFactory<>("info"));
        setColumnReorderingFalse();
        setupActionButtonsColumnCellFactories();

        setupMenuItemsActions();
        setupDataPersistenceOptionMenuItemsOnAction();
        setupAddActivityButtonListeners();
        setupAddActivityInputs();

        setupSettingsAndData();
    }

    private void addActivity() {
        ShorthandSyntaxHelper.computeActivityInputs(dateInput, timeInput, infoInput);
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

    private void editActivity(Activity activity) {
        if(ActivityEditHelper.editActivity(activity)) { activityTable.refresh(); }
    }

    private void setupSettingsAndData() {
        setupShortcuts();
        loadSettings();

        activityTable.setItems(activities);
        if(DataHelper.loadData(activities, dataPersistenceObject)) {
            Platform.runLater(() -> activityTable.scrollTo(activities.size()));
        }
    }

    private void setupAddActivityInputs() {
        dateInput.setPromptText("DD/MM/YYYY");
        timeInput.setPromptText("0:00");
        infoInput.setPromptText("Info");
        DatePickerHelper.setupDatePickerConverter(dateInput);
        DatePickerHelper.setupShowCalendarListener(dateInput);
        ShorthandSyntaxHelper.setupShorthandSyntaxListeners(dateInput, timeInput, infoInput);
    }

    private void setColumnReorderingFalse() {
        activityTable.skinProperty().addListener((observable, oldSkin, newSkin) -> {
            final TableHeaderRow header = (TableHeaderRow) activityTable.lookup("TableHeaderRow");
            header.reorderingProperty().addListener((observable1, oldValue, newValue) -> header.setReordering(false));
        });
    }

    private void loadSettings() {
        if(Settings.loadSettings()) { setDataPersistenceOptionAccordingToSettings(); }
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

    private void setupShortcuts() {
        KeyCombination saveKeyCombination = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);
        Runnable saveRunnable = () -> DataHelper.saveData(activities, dataPersistenceObject);
        Platform.runLater(() -> Main.getScene().getAccelerators().put(saveKeyCombination, saveRunnable));

        KeyCombination addActivityKeyCombination = new KeyCodeCombination(KeyCode.ENTER, KeyCombination.CONTROL_DOWN);
        Runnable addActivityRunnable = () -> addActivity();
        Platform.runLater(() -> Main.getScene().getAccelerators().put(addActivityKeyCombination, addActivityRunnable));
    }

    private void setupDataPersistenceOptionMenuItemsOnAction() {
        dataPersistenceOptionTextFile.setOnAction(e -> Platform.runLater(() -> {
            dataPersistenceObject = new TextFileStrategy();
            Settings.setDataPersistenceDefaultOption(DataPersistenceOption.TEXT_FILE);
            Settings.saveSettings();
        }));
        dataPersistenceOptionSerialization.setOnAction(e -> Platform.runLater(() -> {
            dataPersistenceObject = new SerializationStrategy();
            Settings.setDataPersistenceDefaultOption(DataPersistenceOption.SERIALIZATION);
            Settings.saveSettings();
        }));
        dataPersistenceOptionDatabase.setOnAction(e -> Platform.runLater(() -> {
            dataPersistenceObject = new DatabaseStrategy();
            Settings.setDataPersistenceDefaultOption(DataPersistenceOption.DATABASE);
            Settings.saveSettings();
        }));
    }

    private void setupMenuItemsActions() {
        saveDataMenuItem.setOnAction(e -> DataHelper.saveData(activities, dataPersistenceObject));
        loadDataMenuItem.setOnAction(e -> {
            if(DataHelper.loadData(activities, dataPersistenceObject)) {
                Platform.runLater(() -> activityTable.scrollTo(activities.size()));
            }
        });

        exportCsvMenuItem.setOnAction(e -> DataHelper.exportCsv(activities));
        importCsvMenuItem.setOnAction(e -> {
            if(DataHelper.importCsv(activities)) {
                Platform.runLater(() -> activityTable.scrollTo(activities.size()));
            }
        });

        aboutMenuItem.setOnAction(e -> AboutHelper.showAbout());
    }

    private void setupAddActivityButtonListeners() {
        addActivityButton.setOnAction(e -> addActivity());

        addActivityButton.setOnKeyReleased(k -> {
            if(k.getCode() == KeyCode.ENTER) {
                addActivity();
            }
        });

        addActivityButton.focusedProperty().addListener(((observable, oldValue, newValue) -> {
            if(!newValue) {
                Platform.runLater(() -> dateInput.requestFocus());
            }
        }));
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
}