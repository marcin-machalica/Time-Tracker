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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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

    private ObservableList<Activity> activities = FXCollections.observableArrayList();
    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    private void initialize() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<Activity, String>("date"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<Activity, String>("time"));
        infoColumn.setCellValueFactory(new PropertyValueFactory<Activity, String>("info"));

        activityTable.setItems(activities);

        dateInput.setPromptText("DD/MM/YYYY");
        timeInput.setPromptText("00:00");
        setupDateInputConverter();
        setupDateInputListener();
        setupAddActivityButtonListeners();
        setupActionButtonsColumnCellFactories();
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
                return localDate == null ? null : dateTimeFormatter.format(localDate);
            }

            @Override
            public LocalDate fromString(String dateString) {
                if(dateString == null || dateString.trim().isEmpty())
                {
                    return null;
                }
                return LocalDate.parse(dateString, dateTimeFormatter);
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
        } else if (time.equals("") || !time.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$")) {
            timeInput.requestFocus();
            return;
        } else if (info.equals("")){
            infoInput.requestFocus();
            return;
        }

        String date = dateTimeFormatter.format(localDate);

        activities.add(new Activity(
                        date,
                        time,
                        info
        ));

        dateInput.setValue(null);
        timeInput.clear();
        infoInput.clear();
        dateInput.requestFocus();
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