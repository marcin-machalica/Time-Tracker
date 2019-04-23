package machalica.marcin.timetracker.settings;

import javafx.application.Platform;
import machalica.marcin.timetracker.helper.NotificationHelper;
import org.apache.log4j.Logger;

import java.io.*;

public class Settings {
    private static final Logger logger = Logger.getLogger(Settings.class);
    private static DataPersistenceOption dataPersistenceOption = DataPersistenceOption.TEXT_FILE;
    private static final String FINAL_FILE_NAME = "time_tracker_settings.ser";

    public static boolean saveSettings() {
        boolean isSaved = false;
        try {
            save();
            isSaved = true;
        } catch (IOException ex) {
            logger.error(ex);
            NotificationHelper.showNotification(10, "Save error", "Error during saving settings", "/errornotification.png");
        } finally {
            return isSaved;
        }
    }

    public static boolean loadSettings() {
        boolean isLoaded = false;
        try {
            load();
            isLoaded = true;
            Platform.runLater(() -> {
                NotificationHelper.showNotification(5,"Settings loaded", "Successfully loaded settings.", "/loadiconsettings.png");
            });
        } catch (FileNotFoundException ex) {
            Platform.runLater(() -> {
                logger.error(ex.getMessage());
                NotificationHelper.showNotification(10, "Load error", ex.getMessage(), "/errornotification.png");
            });
        } catch (ClassNotFoundException ex) {
            Platform.runLater(() -> {
                logger.error(ex);
                NotificationHelper.showNotification(10, "Load error", "Cannot load settings.", "/errornotification.png");
            });
        } catch (IOException ex) {
            Platform.runLater(() -> {
                logger.error(ex);
                NotificationHelper.showNotification(10, "Load error", "Cannot load settings.", "/errornotification.png");
            });
        } finally {
            saveSettings();
            return isLoaded;
        }
    }

    private static void load() throws IOException, ClassNotFoundException {
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FINAL_FILE_NAME))) {
            dataPersistenceOption = (DataPersistenceOption) ois.readObject();
            if(dataPersistenceOption == null) dataPersistenceOption = DataPersistenceOption.TEXT_FILE;
        } catch (FileNotFoundException ex) {
            dataPersistenceOption = DataPersistenceOption.TEXT_FILE;
            throw new FileNotFoundException("Settings File (" + FINAL_FILE_NAME + ") doesn't exist.");
        }
    }

    public static void save() throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FINAL_FILE_NAME));
        oos.writeObject(dataPersistenceOption);
        oos.close();
    }

    public static void setDataPersistenceOption(DataPersistenceOption dataPersistenceOption) {
        Settings.dataPersistenceOption = dataPersistenceOption;
    }

    public static DataPersistenceOption getDataPersistenceOption() {
        return dataPersistenceOption;
    }
}