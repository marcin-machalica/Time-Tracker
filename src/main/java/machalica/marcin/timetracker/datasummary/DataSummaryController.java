package machalica.marcin.timetracker.datasummary;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import machalica.marcin.timetracker.helper.ActivityHelper;
import machalica.marcin.timetracker.helper.ShorthandSyntaxHelper;
import machalica.marcin.timetracker.model.Activity;
import machalica.marcin.timetracker.model.DatePickerHelper;
import javafx.scene.chart.XYChart;

import static machalica.marcin.timetracker.datasummary.DateOption.*;

public class DataSummaryController {
    @FXML private DatePicker startDateInput;
    @FXML private DatePicker endDateInput;
    @FXML private ChoiceBox<DateOption> dateOptionChoiceBox;
    @FXML private BarChart dataChart;

    private ObservableList<Activity> activities;

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

        dateOptionChoiceBox.getItems().setAll(
                LAST_WEEK, LAST_MONTH, LAST_YEAR, ALL, BETWEEN
        );
    }

    private void setupChart() {
        dataChart.setPrefSize(1600, 900);
        dataChart.setLegendVisible(false);
        dataChart.setAnimated(false);
        dateOptionChoiceBox.setValue(ALL);

        XYChart.Series series1 = new XYChart.Series();

        Platform.runLater(() -> {
            for (Activity activity : activities) {
                series1.getData().add(new XYChart.Data<>(activity.getDate().toString(), ActivityHelper.convertTimeStringToDouble(activity.getTime())));
            }
            dataChart.getData().addAll(series1);
        });
    }

    public void setActivities(ObservableList<Activity> activities) {
        this.activities = activities;
    }
}
