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
import org.apache.log4j.Logger;
import java.util.regex.Pattern;

public class DatabaseStrategy implements DataPersistenceStrategy {
    private static final Logger logger = Logger.getLogger(DatabaseStrategy.class);
    private static final DatabaseStrategy databaseStrategy = new DatabaseStrategy();

    private static final String USERNAME_PATTERN = "(?<=^USERNAME=)[a-zA-Z0-9]{1,15}$";
    private static final String PASSWORD_PATTERN = "(?<=^PASSWORD=).{0,15}";

    private static final String FINAL_FILE_NAME = "database_credentials.txt";

    private final String DB_URL = "jdbc:postgresql://localhost/timetracker";
    private final String TABLE_NAME = "Activity";
    private String USERNAME;
    private String PASSWORD;
    Connection connection;

    private DatabaseStrategy() { }

    private void loadCredentials() throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(FINAL_FILE_NAME))) {
            String line;
            int count = 0;
            Matcher matcher;
            boolean wasFound;

            while (((line = bufferedReader.readLine()) != null) && count <= 2) {
                count++;
                String[] patterns = {USERNAME_PATTERN, PASSWORD_PATTERN};

                matcher = Pattern.compile(patterns[count - 1]).matcher(line);
                wasFound = matcher.find();

                if (wasFound) {
                    switch (count) {
                        case 1:
                            USERNAME = matcher.group(0);
                            break;
                        case 2:
                            PASSWORD = matcher.group(0);
                            break;
                    }
                } else {
                    throw new IllegalArgumentException("Cannot load database credentials.\nWrong format in line: " + count);
                }
            }
        } catch (FileNotFoundException ex) {
            throw new FileNotFoundException("Credentials Text File (" + FINAL_FILE_NAME + ") doesn't exist.");
        }
    }

    public boolean areCredentialsLoaded() {
        if (USERNAME == null || USERNAME.isEmpty() || PASSWORD == null) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void save(ObservableList<Activity> observableList) throws ClassNotFoundException, SQLException, IOException {
        Class.forName("org.postgresql.Driver");

        if (!areCredentialsLoaded()) {
            loadCredentials();
        }

        String createSQL = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                " (date   DATE," +
                "  time   VARCHAR(5)," +
                "  info   VARCHAR(200))";
        String truncateSQL = "TRUNCATE TABLE " + TABLE_NAME;
        String insertSQL = "INSERT INTO " + TABLE_NAME + " VALUES(?, ?, ?)";
        try (
                Connection connection = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
                PreparedStatement createTableStatement = connection.prepareStatement(createSQL);
                PreparedStatement truncateTableStatement = connection.prepareStatement(truncateSQL);
                PreparedStatement insertStatement = connection.prepareStatement(insertSQL)

        ) {
            connection.setAutoCommit(false);

            createTableStatement.execute();
            truncateTableStatement.execute();

            for (Activity activity : observableList) {
                insertStatement.setDate(1, activity.getDate());
                insertStatement.setString(2, activity.getTime());
                insertStatement.setString(3, activity.getInfo());

                insertStatement.addBatch();
            }

            insertStatement.executeBatch();

            connection.commit();
        } catch (SQLException ex) {
            if (connection != null) {
                logger.error(ex);
                connection.rollback();
                throw ex;
            }
        }
    }

    @Override
    public ObservableList<Activity> load() throws ClassNotFoundException, SQLException, IOException {
        Class.forName("org.postgresql.Driver");

        if (!areCredentialsLoaded()) {
            loadCredentials();
        }

        String selectSQL = "SELECT date, time, info FROM " + TABLE_NAME;
        try (
                Connection connection = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
                PreparedStatement selectStatement = connection.prepareStatement(selectSQL)
        ) {
            ObservableList<Activity> observableList = FXCollections.observableArrayList();

            try (ResultSet resultSet = selectStatement.executeQuery()) {
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

    public static DatabaseStrategy getInstance() {
        return databaseStrategy;
    }

    @Override
    public String toString() {
        return "PostgreSQL Database";
    }
}
