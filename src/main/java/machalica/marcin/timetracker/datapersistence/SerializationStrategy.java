package machalica.marcin.timetracker.datapersistence;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import machalica.marcin.timetracker.model.Activity;
import machalica.marcin.timetracker.model.ActivitySerializationHelper;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SerializationStrategy implements DataPersistenceStrategy {
    private final String FINAL_FILE_NAME = "time_tracker_data.ser";

    @Override
    public void save(ObservableList<Activity> observableList) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FINAL_FILE_NAME));
        List<ActivitySerializationHelper> serializationHelperList = new ArrayList<>();

        for(Activity activity : observableList) {
            serializationHelperList.add(new ActivitySerializationHelper(
                    activity.getDate(),
                    activity.getTime(),
                    activity.getInfo()
            ));
        }

        oos.writeObject(serializationHelperList);
        oos.close();
    }

    @Override
    public ObservableList<Activity> load() throws IOException, ClassNotFoundException {
        ObservableList<Activity> observableList = FXCollections.observableArrayList();
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FINAL_FILE_NAME))) {
            List<ActivitySerializationHelper> ashList = (ArrayList<ActivitySerializationHelper>) ois.readObject();

            for (ActivitySerializationHelper ash : ashList) {
                observableList.add(new Activity(
                        LocalDate.parse(ash.getDate(), Activity.DATE_TIME_FORMATTER),
                        ash.getTime(),
                        ash.getInfo()
                ));
            }
        } catch (FileNotFoundException ex) { throw new FileNotFoundException("Serialized Data File (" + FINAL_FILE_NAME + ") doesn't exist."); }

        return observableList;
    }

    @Override
    public String toString() {
        return "Serialized Data File (" + FINAL_FILE_NAME + ")";
    }
}
