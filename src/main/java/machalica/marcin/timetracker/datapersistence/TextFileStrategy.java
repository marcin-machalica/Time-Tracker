package machalica.marcin.timetracker.datapersistence;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import machalica.marcin.timetracker.model.Activity;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextFileStrategy implements DataPersistenceStrategy {
    private final String TEMP_FILE_NAME = "time_tracker_data_temp.txt";
    private final String FINAL_FILE_NAME = "time_tracker_data.txt";
    private final File tempFile = new File(TEMP_FILE_NAME);
    private final File finalFile = new File(FINAL_FILE_NAME);

    @Override
    public void save(ObservableList<Activity> observableList) throws IOException {
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
    public ObservableList<Activity> load() throws IOException {
        ObservableList<Activity> observableList = FXCollections.observableArrayList();
        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(FINAL_FILE_NAME))) {
            String line;
            int count = 0;
            while((line = bufferedReader.readLine()) != null) {
                count++;
                if(line.isEmpty()) { continue; }

                Matcher matcher = Pattern.compile("^[^;]*;[^;]*;[^;]*$").matcher(line);
                boolean wasFound = matcher.find();

                if (wasFound) {
                    String[] activityFields = matcher.group().split(";");
                    try {
                        observableList.add(new Activity(
                                LocalDate.parse(activityFields[0], Activity.DATE_TIME_FORMATTER),
                                activityFields[1],
                                activityFields[2]
                        ));
                    } catch (DateTimeParseException | IllegalArgumentException ex) {
                        throw new IllegalArgumentException("Wrong format in line: " + count);
                    }
                } else {
                    throw new IllegalArgumentException("Wrong format in line: " + count);
                }
            }
            return observableList;
        } catch (FileNotFoundException ex) { throw new FileNotFoundException("Text File (" + FINAL_FILE_NAME + ") doesn't exist."); }
    }

    @Override
    public String toString() {
        return "Text File (" + FINAL_FILE_NAME + ")";
    }
}