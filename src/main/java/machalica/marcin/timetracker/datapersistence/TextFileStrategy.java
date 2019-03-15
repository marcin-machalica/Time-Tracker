package machalica.marcin.timetracker.datapersistence;

import javafx.collections.ObservableList;
import machalica.marcin.timetracker.model.Activity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;

public class TextFileStrategy implements DataPersistenceStrategy {
    private final String TEMP_FILE_NAME = "time_tracker_data_temp.txt";
    private final String FINAL_FILE_NAME = "time_tracker_data.txt";

    @Override
    public void save(ObservableList<Activity> observableList) throws IOException {
        final File tempFile = new File(TEMP_FILE_NAME);
        final File finalFile = new File(FINAL_FILE_NAME);

        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(tempFile))) {
            for(Activity activity : observableList) {
                bufferedWriter.append(activity.getDate() + ";" + activity.getTime() + ";" + activity.getInfo() + "\n");
            }
            if(finalFile.exists() && !finalFile.delete()) {
                tempFile.delete();
                throw new FileAlreadyExistsException(FINAL_FILE_NAME);
            } else {
                tempFile.renameTo(finalFile);
            }
        }
    }

    @Override
    public ObservableList<Activity> load() {
        return null;
    }

    @Override
    public String toString() {
        return "Text File (" + FINAL_FILE_NAME + ")";
    }
}