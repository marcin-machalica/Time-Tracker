package machalica.marcin.timetracker.helper;

import machalica.marcin.timetracker.model.Activity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class DateHelper {
    private static final ZoneId zone = ZoneId.of("Europe/Warsaw");
    private static final DateTimeFormatter formatter = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd")
            .toFormatter();

    public static LocalDate getCurrentDate() {
        LocalDateTime now = LocalDateTime.now();
        String dateNow = now.toString().split("T")[0];
        return LocalDate.parse(dateNow, formatter);
    }

    public static LocalDate getDateMinusWeeks(long weeks) {
        return LocalDateTime.ofInstant((ZonedDateTime.now().minusWeeks(weeks).toInstant()), zone.getRules().getOffset(LocalDateTime.now())).toLocalDate();
    }

    public static LocalDate getDateMinusMonths(long months) {
        return LocalDateTime.ofInstant((ZonedDateTime.now().minusMonths(months).toInstant()), zone.getRules().getOffset(LocalDateTime.now())).toLocalDate();
    }

    public static LocalDate getDateMinusYears(long years) {
        return LocalDateTime.ofInstant((ZonedDateTime.now().minusYears(years).toInstant()), zone.getRules().getOffset(LocalDateTime.now())).toLocalDate();
    }

    public static String getFormattedLocalDate(LocalDate localDate) {
        return localDate.format(Activity.DATE_TIME_FORMATTER);
    }
}
