package machalica.marcin.timetracker.datapersistence;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import machalica.marcin.timetracker.model.Activity;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DatabaseStrategy implements DataPersistenceStrategy {
    private static final String DATABASE_NAME_PATTERN = "(?<=^DATABASE_NAME=)[a-zA-Z0-9]{1,15}$";
    private static final String TABLE_NAME_PATTERN = "(?<=^TABLE_NAME=)[a-zA-Z0-9]{1,15}$";
    private static final String USERNAME_PATTERN = "(?<=^USERNAME=)[a-zA-Z0-9]{1,15}$";
    private static final String PASSWORD_PATTERN = "(?<=^PASSWORD=).{0,15}";

    private static final String FINAL_FILE_NAME = "database_credentials.txt";

    private static String DB_URL;
    private static String TABLE_NAME;
    private static String USERNAME;
    private static String PASSWORD;

    private static void loadCredentials() throws IOException {
        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(FINAL_FILE_NAME))) {
            String line;
            int count = 0;
            Matcher matcher;
            boolean wasFound;

            while(((line = bufferedReader.readLine()) != null) && count <= 4) {
                count++;
                String[] patterns = {DATABASE_NAME_PATTERN, TABLE_NAME_PATTERN, USERNAME_PATTERN, PASSWORD_PATTERN};

                matcher = Pattern.compile(patterns[count-1]).matcher(line);
                if (matcher != null) {
                    wasFound = matcher.find();
                    if (wasFound) {
                        switch (count) {
                            case 1:
                                DB_URL = "jdbc:postgresql://localhost/" + matcher.group(0);
                                break;
                            case 2:
                                TABLE_NAME = matcher.group(0);
                                break;
                            case 3:
                                USERNAME = matcher.group(0);
                                break;
                            case 4:
                                PASSWORD = matcher.group(0);
                                break;
                        }
                    } else {
                        throw new IllegalArgumentException("Cannot load database credentials.\nWrong format in line: " + count);
                    }
                }
            }
        } catch (FileNotFoundException ex) { throw new FileNotFoundException("Credentials Text File (" + FINAL_FILE_NAME + ") doesn't exist."); }
    }

    @Override
    public void save(ObservableList<Activity> observableList) throws ClassNotFoundException, SQLException, IOException {
        Class.forName("org.postgresql.Driver");
        loadCredentials();

        try (
                Connection connection = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
                Statement statement = connection.createStatement()
        ) {

            String createTableQuery = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                    " (date   DATE," +
                    "  time   VARCHAR(5)," +
                    "  info   VARCHAR(200))";
            String truncateTableQuery = "TRUNCATE TABLE " + TABLE_NAME;

            statement.addBatch(createTableQuery);
            statement.addBatch(truncateTableQuery);

            for(Activity activity : observableList) {
                statement.addBatch("INSERT INTO " + TABLE_NAME + " VALUES(" +
                        "'" + activity.getDate() + "'," +
                        "'" + activity.getTime() + "'," +
                        "'" + activity.getInfo() + "')"
                );
            }
            statement.executeBatch();
        }
    }

    @Override
    public ObservableList<Activity> load() throws ClassNotFoundException, SQLException, IOException {
        Class.forName("org.postgresql.Driver");
        loadCredentials();

        try (
                Connection connection = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
                Statement statement = connection.createStatement()
        ) {
            String query = "SELECT date, time, info FROM " + TABLE_NAME;
            ObservableList<Activity> observableList = FXCollections.observableArrayList();

            try(ResultSet resultSet = statement.executeQuery(query)) {
                while (resultSet.next()) {
                    observableList.add(new Activity(
                            LocalDate.parse(resultSet.getString("date")),
                            resultSet.getString("time"),
                            resultSet.getString("info")
                    ));
                }
            }
            return observableList;
        }
    }

    @Override
    public String toString() {
        return "PostgreSQL Database";
    }
}
