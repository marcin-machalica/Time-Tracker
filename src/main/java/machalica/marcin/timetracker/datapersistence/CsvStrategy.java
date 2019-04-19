package machalica.marcin.timetracker.datapersistence;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import machalica.marcin.timetracker.main.Main;
import machalica.marcin.timetracker.model.Activity;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CsvStrategy implements DataPersistenceStrategy {
    private static final CsvStrategy csvStrategy = new CsvStrategy();

    private CsvStrategy() { }

    @Override
    public void save(ObservableList<Activity> observableList) throws IOException {
        File finalFile = Main.getCsvFromFileChooser(true);

        if (!finalFile.getName().contains(".")) {
            finalFile = new File(finalFile.getAbsolutePath() + ".csv");
        } else if (finalFile.getName().contains(".") && !finalFile.getName().endsWith(".csv")) {
            finalFile = new File(finalFile.getAbsolutePath().replaceAll("\\..*$", ".csv"));
        }

        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(finalFile))) {
            StringBuilder sb = new StringBuilder();
            sb.append("Date;Time;Info\n");
            for (Activity activity : observableList) {
                sb.append(activity.getDate() + ";" + activity.getTime() + ";" + activity.getInfo() + "\n");
            }
            bufferedWriter.write(sb.toString());
        }
    }

    @Override
    public ObservableList<Activity> load() throws IOException {
        File finalFile = Main.getCsvFromFileChooser(false);

        ObservableList<Activity> observableList = FXCollections.observableArrayList();
        if(finalFile == null || !finalFile.getName().endsWith(".csv")) { return null; }

        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(finalFile))) {
            String line;
            int count = 0;
            while((line = bufferedReader.readLine()) != null) {
                count++;
                if(line.isEmpty() || count == 1) { continue; }

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
        }
    }

    public static CsvStrategy getInstance() {
        return csvStrategy;
    }

    @Override
    public String toString() {
        return "CSV File";
    }
}
