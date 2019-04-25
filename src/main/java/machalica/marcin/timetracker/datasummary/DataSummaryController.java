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

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZonedDateTime;

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

    private void setBarChartData(DateOption dateOption) {
        dataChart.getData().clear();
        XYChart.Series series1 = new XYChart.Series();

        switch (dateOption) {
            case LAST_WEEK:
                Platform.runLater(() -> {
                    for (Activity activity : activities) {
                        if (activity.getDate().before(Date.from(ZonedDateTime.now().toInstant())) &&
                                activity.getDate().after(Date.from(ZonedDateTime.now().minusWeeks(1).toInstant()))) {
                            series1.getData().add(new XYChart.Data<>(activity.getDate().toString(), ActivityHelper.convertTimeStringToDouble(activity.getTime())));
                        }
                    }
                    dataChart.getData().addAll(series1);
                });
                break;
            case LAST_MONTH:
                Platform.runLater(() -> {
                    for (Activity activity : activities) {
                        if (activity.getDate().before(Date.from(ZonedDateTime.now().toInstant())) &&
                                activity.getDate().after(Date.from(ZonedDateTime.now().minusMonths(1).toInstant()))) {
                            series1.getData().add(new XYChart.Data<>(activity.getDate().toString(), ActivityHelper.convertTimeStringToDouble(activity.getTime())));
                        }
                    }
                    dataChart.getData().addAll(series1);
                });
                break;
            case LAST_YEAR:
                Platform.runLater(() -> {
                    for (Activity activity : activities) {
                        if (activity.getDate().before(Date.from(ZonedDateTime.now().toInstant())) &&
                                activity.getDate().after(Date.from(ZonedDateTime.now().minusYears(1).toInstant()))) {
                            series1.getData().add(new XYChart.Data<>(activity.getDate().toString(), ActivityHelper.convertTimeStringToDouble(activity.getTime())));
                        }
                    }
                    dataChart.getData().addAll(series1);
                });
                break;
            case ALL:
                Platform.runLater(() -> {
                    for (Activity activity : activities) {
                        series1.getData().add(new XYChart.Data<>(activity.getDate().toString(), ActivityHelper.convertTimeStringToDouble(activity.getTime())));
                    }
                    dataChart.getData().addAll(series1);
                });
                break;
            case BETWEEN:
                Platform.runLater(() -> {
                    LocalDate startLocalDate = startDateInput.getValue();
                    LocalDate endLocalDate = endDateInput.getValue();
                    if (startLocalDate != null && endLocalDate != null) {
                        Date startDate = Date.valueOf(LocalDate.ofEpochDay(startLocalDate.toEpochDay() - 1));
                        Date endDate = Date.valueOf(LocalDate.ofEpochDay(endLocalDate.toEpochDay() + 1));

                        for (Activity activity : activities) {
                            if (activity.getDate().after(startDate) && activity.getDate().before(endDate)) {
                                series1.getData().add(new XYChart.Data<>(activity.getDate().toString(), ActivityHelper.convertTimeStringToDouble(activity.getTime())));
                            }
                        }
                    } else if (startLocalDate != null) {
                        Date startDate = Date.valueOf(LocalDate.ofEpochDay(startLocalDate.toEpochDay() - 1));
                        for (Activity activity : activities) {
                            if (activity.getDate().after(startDate)) {
                                series1.getData().add(new XYChart.Data<>(activity.getDate().toString(), ActivityHelper.convertTimeStringToDouble(activity.getTime())));
                            }
                        }
                    } else if (endLocalDate != null) {
                        Date endDate = Date.valueOf(LocalDate.ofEpochDay(endLocalDate.toEpochDay() + 1));

                        for (Activity activity : activities) {
                            if (activity.getDate().before(endDate)) {
                                series1.getData().add(new XYChart.Data<>(activity.getDate().toString(), ActivityHelper.convertTimeStringToDouble(activity.getTime())));
                            }
                        }
                    }

                    dataChart.getData().addAll(series1);
                });
                break;
        }
    }

    public void setActivities(ObservableList<Activity> activities) {
        this.activities = activities;
    }
}
