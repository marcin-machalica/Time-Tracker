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
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import machalica.marcin.timetracker.datapersistence.*;
import machalica.marcin.timetracker.helper.AboutHelper;
import machalica.marcin.timetracker.helper.ExitHelper;
import machalica.marcin.timetracker.helper.ShorthandSyntaxHelper;
import machalica.marcin.timetracker.model.Activity;
import machalica.marcin.timetracker.model.ActivityEditHelper;
import machalica.marcin.timetracker.model.DatePickerHelper;
import machalica.marcin.timetracker.settings.DataPersistenceOption;
import machalica.marcin.timetracker.settings.Settings;
import org.apache.log4j.Logger;

import java.sql.Date;
import java.time.DateTimeException;
import java.time.LocalDate;

import static machalica.marcin.timetracker.datapersistence.DataHelper.getDataPersistenceObject;
import static machalica.marcin.timetracker.datapersistence.DataHelper.getDataPersistenceObjectAccordingToSettings;

public class MainController {
    @FXML private TableView activityTable;
    @FXML private TableColumn<Activity, Date> dateColumn;
    @FXML private TableColumn<Activity, String> timeColumn;
    @FXML private TableColumn<Activity, String> infoColumn;
    @FXML private TableColumn<Activity, Activity> actionButtonsColumn;
    @FXML private DatePicker dateInput;
    @FXML private TextField timeInput;
    @FXML private TextField infoInput;
    @FXML private Button addActivityButton;
    @FXML private Label warningLabel;
    @FXML private RadioMenuItem dataPersistenceOptionTextFile;
    @FXML private RadioMenuItem dataPersistenceOptionSerialization;
    @FXML private RadioMenuItem dataPersistenceOptionDatabase;
    @FXML private MenuItem saveDataMenuItem;
    @FXML private MenuItem loadDataMenuItem;
    @FXML private MenuItem exportCsvMenuItem;
    @FXML private MenuItem importCsvMenuItem;
    @FXML private MenuItem aboutMenuItem;

    private static final Logger logger = Logger.getLogger(MainController.class);
    private ObservableList<Activity> activities = FXCollections.observableArrayList();
    private static DataPersistenceStrategy dataPersistenceObject;

    @FXML
    private void initialize() {
        Main.setOnExit(() -> ExitHelper.exit(activities, dataPersistenceObject));
        initialSetup();
        Platform.runLater(() -> dateInput.requestFocus());
    }

    private void initialSetup() {
        setupTable();

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
        Settings.loadSettings();

        dataPersistenceObject = getDataPersistenceObjectAccordingToSettings();
        setDataPersistenceMenuOptionAccordingToSettings();

        activityTable.setItems(activities);
        logger.debug(dataPersistenceObject);
        if(DataHelper.loadData(activities, dataPersistenceObject)) {
            Platform.runLater(() -> {
                activityTable.refresh();
                activityTable.scrollTo(activities.size());
            });
        }
    }

    private void setupTable() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateColumn.setCellFactory(col -> {
            TableCell<Activity, Date> cell = new TableCell<Activity, Date>() {
                @Override
                protected void updateItem(Date item, boolean empty) {
                    super.updateItem(item, empty);
                    if(empty) {
                        setText(null);
                    }
                    else {
                        setText(Activity.DATE_TIME_FORMAT.format(item));
                    }
                }
            };
            return cell;
        });

        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        infoColumn.setCellValueFactory(new PropertyValueFactory<>("info"));
        setupInfoColumnCellFactory();
        setupActionButtonsColumnCellFactory();
        setColumnReorderingFalse();
        Main.setOnResize(() -> activityTable.refresh());
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

    private void setDataPersistenceMenuOptionAccordingToSettings() {
        switch (Settings.getDataPersistenceOption()) {
            case SERIALIZATION:
                Platform.runLater(() -> dataPersistenceOptionSerialization.setSelected(true));
                break;
            case DATABASE:
                Platform.runLater(() -> dataPersistenceOptionDatabase.setSelected(true));
                break;
            case TEXT_FILE:
            default:
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
            dataPersistenceObject = getDataPersistenceObject(DataPersistenceOption.TEXT_FILE);
            Settings.setDataPersistenceOption(DataPersistenceOption.TEXT_FILE);
            Settings.saveSettings();
        }));
        dataPersistenceOptionSerialization.setOnAction(e -> Platform.runLater(() -> {
            dataPersistenceObject = getDataPersistenceObject(DataPersistenceOption.SERIALIZATION);
            Settings.setDataPersistenceOption(DataPersistenceOption.SERIALIZATION);
            Settings.saveSettings();
        }));
        dataPersistenceOptionDatabase.setOnAction(e -> Platform.runLater(() -> {
            dataPersistenceObject = getDataPersistenceObject(DataPersistenceOption.DATABASE);
            Settings.setDataPersistenceOption(DataPersistenceOption.DATABASE);
            Settings.saveSettings();
        }));
    }

    private void setupMenuItemsActions() {
        saveDataMenuItem.setOnAction(e -> DataHelper.saveData(activities, dataPersistenceObject));
        loadDataMenuItem.setOnAction(e -> {
            if(DataHelper.loadData(activities, dataPersistenceObject)) {
                Platform.runLater(() -> {
                    activityTable.refresh();
                    activityTable.scrollTo(activities.size());
                });
            }
        });

        exportCsvMenuItem.setOnAction(e -> DataHelper.exportCsv(activities));
        importCsvMenuItem.setOnAction(e -> {
            if(DataHelper.importCsv(activities)) {
                Platform.runLater(() -> {
                    activityTable.refresh();
                    activityTable.scrollTo(activities.size());
                });
            }
        });

        aboutMenuItem.setOnAction(e -> AboutHelper.showAbout());
    }

    private void setupAddActivityButtonListeners() {
        addActivityButton.setOnAction(e -> addActivity());

        addActivityButton.setOnKeyPressed(k -> {
            if(k.getCode() == KeyCode.ENTER) {
                addActivity();
            } else if(k.getCode() == KeyCode.TAB) {
                Platform.runLater(() -> dateInput.requestFocus());
            }
        });
    }

    private void setupActionButtonsColumnCellFactory() {
        actionButtonsColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));

        actionButtonsColumn.setCellFactory(param -> new TableCell<Activity, Activity>() {
            private final Button editButton = new Button("");
            private final Button deleteButton = new Button("");
            private final HBox actionButtonsHBox = new HBox(10, editButton, deleteButton);

            @Override
            protected void updateItem(Activity activity, boolean empty) {
                super.updateItem(activity, empty);

                if (activity == null) {
                    setGraphic(null);
                    return;
                }

                setGraphic(actionButtonsHBox);

                deleteButton.getStyleClass().add("delete-button");
                editButton.getStyleClass().add("edit-button");

                deleteButton.setOnAction(e -> {
                    getTableView().getItems().remove(activity);
                    Platform.runLater(() -> activityTable.refresh());
                });
                editButton.setOnAction(e -> editActivity(activity));
            }
        });
    }

    private void setupInfoColumnCellFactory() {
        infoColumn.setCellFactory (col -> {
            TableCell<Activity, String> cell = new TableCell<Activity, String>() {
                @Override
                public void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null) {
                        Text text = new Text(item);
                        text.setWrappingWidth(col.getWidth() - 10);
                        text.setFill(Paint.valueOf("#ffffff"));
                        this.setPrefHeight(text.getLayoutBounds().getHeight()+10);
                        this.setGraphic(text);
                    }
                }
            };
            return cell;
        });
    }
}