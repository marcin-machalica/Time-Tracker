package machalica.marcin.timetracker.datapersistence;

import javafx.collections.ObservableList;
import machalica.marcin.timetracker.model.Activity;

import java.io.IOException;

public interface DataPersistenceStrategy {
    void save(ObservableList<Activity> observableList) throws IOException;
    ObservableList<Activity> load() throws IOException, ClassNotFoundException;
}