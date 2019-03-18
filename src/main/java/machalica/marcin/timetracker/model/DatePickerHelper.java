package machalica.marcin.timetracker.model;

import javafx.scene.control.DatePicker;
import javafx.scene.input.KeyCode;
import javafx.util.StringConverter;

import java.time.LocalDate;

public class DatePickerHelper {
    public static void setupDatePickerConverter(DatePicker dateInput) {
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

    public static void setupShowCalendarListener(DatePicker dateInput) {
        dateInput.setOnKeyPressed(k -> {
            if(k.getCode() == KeyCode.SPACE) {
                dateInput.show();
            }
        });
    }
}