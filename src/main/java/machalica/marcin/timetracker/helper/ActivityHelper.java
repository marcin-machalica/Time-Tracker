package machalica.marcin.timetracker.helper;

import machalica.marcin.timetracker.model.Activity;

public class ActivityHelper {
    public static double convertTimeStringToDouble(Activity activity) {
        byte hours = getActivityHours(activity);
        byte minutes = getActivityMinutes(activity);
        return Double.parseDouble(String.format("%.2f", hours + minutes/60d));
    }

    public static byte getActivityHours(Activity activity) {
        return Byte.parseByte(activity.getTime().split(":")[0]);
    }

    public static byte getActivityMinutes(Activity activity) {
        return Byte.parseByte(activity.getTime().split(":")[1]);
    }
}
