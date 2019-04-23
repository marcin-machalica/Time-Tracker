package machalica.marcin.timetracker.datapersistence;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import machalica.marcin.timetracker.helper.NotificationHelper;
import machalica.marcin.timetracker.model.Activity;
import machalica.marcin.timetracker.settings.DataPersistenceOption;
import machalica.marcin.timetracker.settings.Settings;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

public class DataHelper {
    private static final Logger logger = Logger.getLogger(DataHelper.class);
    public static boolean saveData(ObservableList<Activity> activities, DataPersistenceStrategy dataPersistenceObject) {
        boolean isSaved = false;
        try {
            dataPersistenceObject.save(activities);
            NotificationHelper.showNotification(5, "Data saved","Successfully saved data to \n" + dataPersistenceObject.toString() + ".", "/saveicon.png");
            isSaved = true;
        } catch (IOException ex) {
            logger.error(ex);
            NotificationHelper.showNotification(10, "Save error", "Error during saving data to \n" + dataPersistenceObject.toString() + ".\n" + ex.getMessage(), "/errornotification.png");
        } catch (ClassNotFoundException ex) {
            logger.error(ex);
            NotificationHelper.showNotification(10, "Save error", "Error during saving data to \n" + dataPersistenceObject.toString() + ".", "/errornotification.png");
        } catch (SQLException ex) {
            logger.error(ex);
            logger.error(ex.getNextException());
            NotificationHelper.showNotification(10, "Save error", "Error during saving data to \n" + dataPersistenceObject.toString() + ".", "/errornotification.png");
        } catch (IllegalArgumentException ex) {
          logger.error(ex);
            NotificationHelper.showNotification(10, "Save error", "Error during saving data to \n" + dataPersistenceObject.toString() + ".\n" + ex.getMessage(), "/errornotification.png");
        } finally {
            return isSaved;
        }
    }

    public static boolean loadData(ObservableList<Activity> activities, DataPersistenceStrategy dataPersistenceObject) {
        ObservableList<Activity> activitiesTemp = null;
        SimpleBooleanProperty isLoaded = new SimpleBooleanProperty(false);
        try {
            activitiesTemp = dataPersistenceObject.load();
        } catch (FileNotFoundException ex) {
            Platform.runLater(() -> {
                logger.error(ex.getMessage());
                NotificationHelper.showNotification(10, "Load error", ex.getMessage(), "/errornotification.png");
            });
        } catch (IllegalArgumentException | IOException ex) {
            Platform.runLater(() -> {
                logger.error(ex.getMessage());
                logger.error(ex);
                NotificationHelper.showNotification(10, "Load error", "Error during loading data from \n" + dataPersistenceObject.toString() + ".\n" + ex.getMessage(), "/errornotification.png");
            });
        } catch (ClassNotFoundException ex) {
            Platform.runLater(() -> {
                logger.error(ex);
                NotificationHelper.showNotification(10, "Load error", "Error during loading data from \n" + dataPersistenceObject.toString() + ".", "/errornotification.png");
            });
        } catch (SQLException ex) {
            Platform.runLater(() -> {
                logger.error(ex);
                logger.error(ex.getNextException());
                NotificationHelper.showNotification(10, "Load error", "Error during loading data from \n" + dataPersistenceObject.toString() + ".", "/errornotification.png");
            });
        }

        if(activitiesTemp != null && !activitiesTemp.isEmpty()) {
            activities.setAll(activitiesTemp);
            isLoaded.set(true);
            Platform.runLater(() -> {
                NotificationHelper.showNotification(5,"Data loaded", "Successfully loaded data from \n" + dataPersistenceObject.toString() + ".", "/loadicon.png");
            });
        } else if(activitiesTemp != null && activitiesTemp.isEmpty()) {
            Platform.runLater(() -> {
                String errorMsg = "No data found in \n" + dataPersistenceObject.toString() + ".";
                logger.error(errorMsg);
                NotificationHelper.showNotification(10, "Load error", errorMsg, "/errornotification.png");
            });
        }
        return isLoaded.get();
    }

    public static boolean exportCsv(ObservableList<Activity> activities) {
        DataPersistenceStrategy csvStrategy = getDataPersistenceObject(DataPersistenceOption.CSV);
        boolean isSaved = false;

        try {
            csvStrategy.save(activities);
            NotificationHelper.showNotification(5, "Data exported","Successfully exported data to \n" + csvStrategy.toString() + ".", "/saveicon.png");
            isSaved = true;
        } catch (IOException ex) {
            logger.error(ex);
            NotificationHelper.showNotification(10, "Export error", "Error during exporting data to \n" + csvStrategy.toString() + ".\n" + ex.getMessage(), "/errornotification.png");
        } catch (NullPointerException ex) { }
        finally {
            return isSaved;
        }
    }

    public static boolean importCsv(ObservableList<Activity> activities) {
        CsvStrategy csvStrategy = CsvStrategy.getInstance();
        ObservableList<Activity> activitiesTemp = null;
        SimpleBooleanProperty isLoaded = new SimpleBooleanProperty(false);

        try {
            activitiesTemp = csvStrategy.load();
        } catch (FileNotFoundException ex) {
            Platform.runLater(() -> {
                logger.error(ex.getMessage());
                NotificationHelper.showNotification(10, "Import error", ex.getMessage(), "/errornotification.png");
            });
        } catch (IllegalArgumentException | IOException ex) {
            Platform.runLater(() -> {
                logger.error(ex);
                NotificationHelper.showNotification(10, "Import error", "Error during importing data from \n" + csvStrategy.toString() + ".\n" + ex.getMessage(), "/errornotification.png");
            });
        }

        if(activitiesTemp != null && !activitiesTemp.isEmpty()) {
            activities.setAll(activitiesTemp);
            isLoaded.set(true);
            Platform.runLater(() -> {
                NotificationHelper.showNotification(5,"Data imported", "Successfully imported data from \n" + csvStrategy.toString() + ".", "/loadicon.png");
            });
        } else if(activitiesTemp != null && activitiesTemp.isEmpty()) {
            Platform.runLater(() -> {
                String errorMsg = "No data found in \n" + csvStrategy.toString() + ".";
                logger.error(errorMsg);
                NotificationHelper.showNotification(10, "Import error", errorMsg, "/errornotification.png");
            });
        }
        return isLoaded.get();
    }

    public static DataPersistenceStrategy getDataPersistenceObjectAccordingToSettings() {
        DataPersistenceStrategy dataPersistenceObject;

        switch (Settings.getDataPersistenceOption()) {
            case SERIALIZATION:
                dataPersistenceObject = SerializationStrategy.getInstance();
                break;
            case DATABASE:
                dataPersistenceObject = DatabaseStrategy.getInstance();
                break;
            case TEXT_FILE:
            default:
                dataPersistenceObject = TextFileStrategy.getInstance();
                break;
        }

        return dataPersistenceObject;
    }

    public static DataPersistenceStrategy getDataPersistenceObject(DataPersistenceOption dataPersistenceOption) {
        DataPersistenceStrategy dataPersistenceObject;

        switch (dataPersistenceOption) {
            case SERIALIZATION:
                dataPersistenceObject = SerializationStrategy.getInstance();
                break;
            case DATABASE:
                dataPersistenceObject = DatabaseStrategy.getInstance();
                break;
            case CSV:
                dataPersistenceObject = CsvStrategy.getInstance();
                break;
            case TEXT_FILE:
            default:
                dataPersistenceObject = TextFileStrategy.getInstance();
                break;
        }

        return dataPersistenceObject;
    }
}