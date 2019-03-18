package machalica.marcin.timetracker.model;

import javafx.beans.property.SimpleStringProperty;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Activity {
    private final SimpleStringProperty date;
    private final SimpleStringProperty time;
    private final SimpleStringProperty info;
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final String TIME_PATTERN = "^(1?[0-9]|2[0-3]):[0-5][0-9]$";

    public Activity(LocalDate localDate, String time, String info) throws DateTimeException, IllegalArgumentException {
        if(localDate == null) { throw new IllegalArgumentException("LocalDate object cannot be null"); }
        if(time == null) { throw new IllegalArgumentException("Time cannot be null"); }
        if(info == null) { throw new IllegalArgumentException("Info cannot be null"); }

        String date = DATE_TIME_FORMATTER.format(localDate);
        if (time.equals("") || !time.matches(TIME_PATTERN)) { throw new IllegalArgumentException("Wrong time format"); }
        if (info.trim().equals("")) { throw new IllegalArgumentException("Info cannot be empty"); }
        if (info.contains(";")) { throw new IllegalArgumentException("Info cannot contain character: \";\""); }

        this.date = new SimpleStringProperty(date);
        this.time = new SimpleStringProperty(time);
        this.info = new SimpleStringProperty(info.trim());
    }

    public String getDate() {
        return date.get();
    }

    public LocalDate getLocalDate() { return LocalDate.parse(getDate(), DATE_TIME_FORMATTER); }

    public void setDate(LocalDate localDate) throws DateTimeException, IllegalArgumentException {
        if(localDate == null) { throw new IllegalArgumentException("LocalDate object cannot be null"); }
        String date = DATE_TIME_FORMATTER.format(localDate);
        this.date.set(date);
    }

    public String getTime() {
        return time.get();
    }

    public void setTime(String time) throws IllegalArgumentException {
        if(time == null) { throw new IllegalArgumentException("Time cannot be null"); }
        if (time.equals("") || !time.matches(TIME_PATTERN)) { throw new IllegalArgumentException("Wrong time format"); }
        this.time.set(time);
    }

    public String getInfo() {
        return info.get();
    }

    public void setInfo(String info) throws IllegalArgumentException {
        if(info == null) { throw new IllegalArgumentException("Info cannot be null"); }
        if (info.trim().equals("")) { throw new IllegalArgumentException("Info cannot be empty"); }
        if (info.contains(";")) { throw new IllegalArgumentException("Info cannot contain character: \";\""); }
        this.info.set(info.trim());
    }
}