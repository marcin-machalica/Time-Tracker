package machalica.marcin.timetracker.model;

import javafx.beans.property.SimpleStringProperty;

public class Activity {
    private final SimpleStringProperty date;
    private final SimpleStringProperty time;
    private final SimpleStringProperty info;

    public Activity(String date, String time, String info) {
        this.date = new SimpleStringProperty(date);
        this.time = new SimpleStringProperty(time);
        this.info = new SimpleStringProperty(info);
    }

    public String getDate() {
        return date.get();
    }

    public void setDate(String date) {
        this.date.set(date);
    }

    public String getTime() {
        return time.get();
    }

    public void setTime(String time) {
        this.time.set(time);
    }

    public String getInfo() {
        return info.get();
    }

    public void setInfo(String info) {
        this.info.set(info);
    }
}
