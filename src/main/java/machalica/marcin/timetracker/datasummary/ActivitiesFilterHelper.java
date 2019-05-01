package machalica.marcin.timetracker.datasummary;

import com.sun.istack.internal.Nullable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import machalica.marcin.timetracker.model.Activity;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZonedDateTime;

public class ActivitiesFilterHelper {
    public static ObservableList<Activity> filterActivitiesByDate(ObservableList<Activity> activities, DateOption dateOption, @Nullable LocalDate startLocalDate, @Nullable LocalDate endLocalDate) {
        ObservableList<Activity> filteredActivities = FXCollections.observableArrayList();

        switch (dateOption) {
            case LAST_WEEK:
                for (Activity activity : activities) {
                    if (activity.getDate().before(Date.from(ZonedDateTime.now().toInstant())) &&
                            activity.getDate().after(Date.from(ZonedDateTime.now().minusWeeks(1).toInstant()))) {
                        filteredActivities.add(activity);
                    }
                }
                break;

            case LAST_MONTH:
                for (Activity activity : activities) {
                    if (activity.getDate().before(Date.from(ZonedDateTime.now().toInstant())) &&
                            activity.getDate().after(Date.from(ZonedDateTime.now().minusMonths(1).toInstant()))) {
                        filteredActivities.add(activity);
                    }
                }
                break;

            case LAST_YEAR:
                for (Activity activity : activities) {
                    if (activity.getDate().before(Date.from(ZonedDateTime.now().toInstant())) &&
                            activity.getDate().after(Date.from(ZonedDateTime.now().minusYears(1).toInstant()))) {
                        filteredActivities.add(activity);
                    }
                }
                break;

            case ALL:
                filteredActivities = activities;
                break;

            case BETWEEN:
                if (startLocalDate != null && endLocalDate != null) {
                    Date startDate = Date.valueOf(LocalDate.ofEpochDay(startLocalDate.toEpochDay() - 1));
                    Date endDate = Date.valueOf(LocalDate.ofEpochDay(endLocalDate.toEpochDay() + 1));
                    for (Activity activity : activities) {
                        if (activity.getDate().after(startDate) && activity.getDate().before(endDate)) {
                            filteredActivities.add(activity);
                        }
                    }
                } else if (startLocalDate != null) {
                    Date startDate = Date.valueOf(LocalDate.ofEpochDay(startLocalDate.toEpochDay() - 1));
                    for (Activity activity : activities) {
                        if (activity.getDate().after(startDate)) {
                            filteredActivities.add(activity);
                        }
                    }
                } else if (endLocalDate != null) {
                    Date endDate = Date.valueOf(LocalDate.ofEpochDay(endLocalDate.toEpochDay() + 1));
                    for (Activity activity : activities) {
                        if (activity.getDate().before(endDate)) {
                            filteredActivities.add(activity);
                        }
                    }
                } else {
                    filteredActivities = activities;
                }
                break;
        }

        return filteredActivities;
    }
}
