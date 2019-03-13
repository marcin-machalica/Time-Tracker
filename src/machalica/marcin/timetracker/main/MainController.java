package machalica.marcin.timetracker.main;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import machalica.marcin.timetracker.model.Activity;

import java.util.Random;

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
    private TextField dateInput;
    @FXML
    private TextField timeInput;
    @FXML
    private TextField infoInput;
    @FXML
    private Button addActivityButton;

    private ObservableList<Activity> activities = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<Activity, String>("date"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<Activity, String>("time"));
        infoColumn.setCellValueFactory(new PropertyValueFactory<Activity, String>("info"));

        activityTable.setItems(activities);
        
        setupAddActivityButtonListeners();
    }

    private void addActivity() {
        if(dateInput.getText().equals("") && timeInput.getText().equals("") && infoInput.getText().equals("")) {
            return;
        }

        activities.add(new Activity(
                        dateInput.getText(),
                        timeInput.getText(),
                        infoInput.getText()
        ));

        dateInput.clear();
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
