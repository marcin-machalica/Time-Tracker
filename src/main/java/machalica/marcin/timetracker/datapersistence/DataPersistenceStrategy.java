package machalica.marcin.timetracker.datapersistence;

import javafx.collections.ObservableList;
import machalica.marcin.timetracker.model.Activity;

import java.io.IOException;
import java.sql.SQLException;

public interface DataPersistenceStrategy {
    void save(ObservableList<Activity> observableList) throws IOException, ClassNotFoundException, SQLException;
    ObservableList<Activity> load() throws IOException, ClassNotFoundException, SQLException;
}