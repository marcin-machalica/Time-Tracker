package machalica.marcin.timetracker.model;

import java.io.Serializable;

public class ActivitySerializationHelper implements Serializable {
    private final String date;
    private final String time;
    private final String info;

    public ActivitySerializationHelper(String date, String time, String info) {
        this.date = date;
        this.time = time;
        this.info = info;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getInfo() {
        return info;
    }
}
