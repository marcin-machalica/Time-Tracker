package machalica.marcin.timetracker.model;

import java.io.Serializable;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Activity implements Serializable {
    private Date date;
    private String time;
    private String info;
    public static final String DATE_TIME_PATTERN = "dd/MM/yyyy";
    public static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat(DATE_TIME_PATTERN);
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
    public static final String TIME_PATTERN = "^(1?[0-9]|2[0-3]):[0-5][0-9]$";

    public Activity(LocalDate localDate, String time, String info) throws DateTimeException, IllegalArgumentException {
        if(localDate == null) { throw new IllegalArgumentException("LocalDate object cannot be null"); }
        if(time == null) { throw new IllegalArgumentException("Time cannot be null"); }
        if(info == null) { throw new IllegalArgumentException("Info cannot be null"); }

        if (time.equals("") || !time.matches(TIME_PATTERN)) { throw new IllegalArgumentException("Wrong time format"); }
        if (info.trim().equals("")) { throw new IllegalArgumentException("Info cannot be empty"); }
        if (info.contains(";")) { throw new IllegalArgumentException("Info cannot contain character: \";\""); }

        this.date = Date.valueOf(localDate);
        this.time = time;
        this.info = info.trim();
    }

    public Date getDate() {
        return date;
    }

    public String getFormattedDateString() { return DATE_TIME_FORMAT.format(date); }

    public void setDate(LocalDate localDate) throws DateTimeException, IllegalArgumentException {
        if(localDate == null) { throw new IllegalArgumentException("LocalDate object cannot be null"); }
        this.date = Date.valueOf(localDate);
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) throws IllegalArgumentException {
        if(time == null) { throw new IllegalArgumentException("Time cannot be null"); }
        if (time.isEmpty() || !time.matches(TIME_PATTERN)) { throw new IllegalArgumentException("Wrong time format"); }
        this.time = time;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) throws IllegalArgumentException {
        if(info == null) { throw new IllegalArgumentException("Info cannot be null"); }
        if (info.trim().isEmpty()) { throw new IllegalArgumentException("Info cannot be empty"); }
        if (info.contains(";")) { throw new IllegalArgumentException("Info cannot contain character: \";\""); }
        this.info = info.trim();
    }
}