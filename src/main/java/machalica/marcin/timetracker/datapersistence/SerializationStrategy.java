package machalica.marcin.timetracker.datapersistence;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import machalica.marcin.timetracker.model.Activity;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SerializationStrategy implements DataPersistenceStrategy {
    private static final SerializationStrategy serializationStrategy = new SerializationStrategy();
    private final String FINAL_FILE_NAME = "time_tracker_data.ser";

    private SerializationStrategy() { }

    @Override
    public void save(ObservableList<Activity> activities) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FINAL_FILE_NAME));

        oos.writeObject(new ArrayList<>(activities));
        oos.close();
    }

    @Override
    public ObservableList<Activity> load() throws IOException, ClassNotFoundException {
        ObservableList<Activity> activities;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FINAL_FILE_NAME))) {
            List<Activity> activityList = (ArrayList<Activity>) ois.readObject();
            activities = FXCollections.observableList(activityList);
        } catch (FileNotFoundException ex) {
            throw new FileNotFoundException("Serialized Data File (" + FINAL_FILE_NAME + ") doesn't exist.");
        }

        return activities;
    }

    public static SerializationStrategy getInstance() {
        return serializationStrategy;
    }

    @Override
    public String toString() {
        return "Serialized Data File (" + FINAL_FILE_NAME + ")";
    }
}
