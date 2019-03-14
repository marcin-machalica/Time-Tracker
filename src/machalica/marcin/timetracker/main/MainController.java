package machalica.marcin.timetracker.main;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import machalica.marcin.timetracker.model.Activity;

import java.time.DateTimeException;
import java.time.LocalDate;

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

    @FXML
    private void initialize() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<Activity, String>("date"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<Activity, String>("time"));
        infoColumn.setCellValueFactory(new PropertyValueFactory<Activity, String>("info"));
        setupActionButtonsColumnCellFactories();

        activityTable.setItems(activities);

        setupDateInputConverter();
        setupDateInputListener();
        setupAddActivityButtonListeners();

        dateInput.setPromptText("DD/MM/YYYY");
        timeInput.setPromptText("00:00");
        dateInput.requestFocus();
    }

    private void setupDateInputListener() {
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
                editButton.setOnAction(event -> System.out.println("editing"));
            }
        });
    }

    private void setupDateInputConverter() {
        dateInput.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate localDate) {
                return localDate == null ? null : Activity.DATE_TIME_FORMATTER.format(localDate);
            }

            @Override
            public LocalDate fromString(String dateString) {
                if(dateString == null || dateString.trim().isEmpty())
                {
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