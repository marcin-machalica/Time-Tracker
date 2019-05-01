package machalica.marcin.timetracker.datasummary;

import com.itextpdf.text.DocumentException;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import machalica.marcin.timetracker.helper.ActivityHelper;
import machalica.marcin.timetracker.helper.NotificationHelper;
import machalica.marcin.timetracker.helper.ShorthandSyntaxHelper;
import machalica.marcin.timetracker.model.Activity;
import machalica.marcin.timetracker.model.DatePickerHelper;
import javafx.scene.chart.XYChart;

import java.io.FileNotFoundException;

import static machalica.marcin.timetracker.datasummary.DateOption.*;

public class DataSummaryController {
    @FXML private DatePicker startDateInput;
    @FXML private DatePicker endDateInput;
    @FXML private ChoiceBox<DateOption> dateOptionChoiceBox;
    @FXML private BarChart dataChart;
    @FXML private Button generatePdfButton;

    private ObservableList<Activity> activities = FXCollections.observableArrayList();
    private ObservableList<Activity> filteredActivities = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        setupInputs();
        setupChart();
    }

    private void setupInputs() {
        DatePickerHelper.setupDatePickerConverter(startDateInput);
        DatePickerHelper.setupDatePickerConverter(endDateInput);
        DatePickerHelper.setupShowCalendarListener(startDateInput);
        DatePickerHelper.setupShowCalendarListener(endDateInput);
        ShorthandSyntaxHelper.setupDateShorthandSyntaxListener(startDateInput);
        ShorthandSyntaxHelper.setupDateShorthandSyntaxListener(endDateInput);

        startDateInput.valueProperty().addListener(((observable, oldValue, newValue) -> {
            if (dateOptionChoiceBox.getValue().equals(BETWEEN)) {
                setBarChartData(BETWEEN);
            }
        }));

        endDateInput.valueProperty().addListener(((observable, oldValue, newValue) -> {
            if (dateOptionChoiceBox.getValue().equals(BETWEEN)) {
                setBarChartData(BETWEEN);
            }
        }));

        dateOptionChoiceBox.getItems().setAll(
                LAST_WEEK, LAST_MONTH, LAST_YEAR, ALL, BETWEEN
        );

        dateOptionChoiceBox.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
            setBarChartData(newValue);
        }));

        generatePdfButton.setOnAction(e -> {
            try {
                String pdfName = PdfHelper.generatePdf(filteredActivities, dateOptionChoiceBox.getValue(), startDateInput.getValue(), endDateInput.getValue());
                NotificationHelper.showNotification(5, "Report generated", "Successfully generated activity report\n" + pdfName + ".", "/pdfnotification.png");
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            } catch (DocumentException ex) {
                ex.printStackTrace();
            }
        });
    }

    private void setupChart() {
        dataChart.setPrefSize(1600, 900);
        dataChart.setLegendVisible(false);
        dataChart.setAnimated(false);
        dateOptionChoiceBox.setValue(ALL);
        setBarChartData(dateOptionChoiceBox.getValue());
    }

    private void setBarChartData(DateOption dateOption) {
        Platform.runLater(() -> {
            dataChart.getData().clear();
            filteredActivities.clear();
            XYChart.Series series1 = new XYChart.Series();

            filteredActivities.addAll(ActivitiesFilterHelper.filterActivitiesByDate(activities, dateOption, startDateInput.getValue(), endDateInput.getValue()));
            for (Activity activity : filteredActivities) {
                series1.getData().add(new XYChart.Data<>(activity.getDate().toString(), ActivityHelper.convertTimeStringToDouble(activity)));
            }

            dataChart.getData().addAll(series1);
        });
    }

    public void setActivities(ObservableList<Activity> activities) {
        if (activities != null) {
            this.activities = activities;
        }
    }
}
