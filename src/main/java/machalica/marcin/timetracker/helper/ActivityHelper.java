package machalica.marcin.timetracker.helper;

public class ActivityHelper {
    public static double convertTimeStringToDouble(String time) {
        byte hours = Byte.parseByte(time.split(":")[0]);
        byte minutes = Byte.parseByte(time.split(":")[1]);
        return Double.parseDouble(String.format("%.2f", hours + minutes/60d));
    }
}
