package machalica.marcin.timetracker.settings;

import javafx.application.Platform;
import machalica.marcin.timetracker.helper.NotificationHelper;

import java.io.*;

public class Settings {
    private static DataPersistenceOption dataPersistenceDefaultOption = DataPersistenceOption.TEXT_FILE;
    private static final String FINAL_FILE_NAME = "time_tracker_settings.ser";

    public static boolean saveSettings() {
        boolean isSaved = false;
        try {
            save();
            isSaved = true;
        } catch (IOException ex) {
            ex.printStackTrace();
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
                System.out.println(ex.getMessage());
                NotificationHelper.showNotification(10, "Load error", ex.getMessage(), "/errornotification.png");
            });
        } catch (ClassNotFoundException ex) {
            Platform.runLater(() -> {
                ex.printStackTrace();
                NotificationHelper.showNotification(10, "Load error", "Cannot load settings.", "/errornotification.png");
            });
        } catch (IOException ex) {
            Platform.runLater(() -> {
                ex.printStackTrace();
                NotificationHelper.showNotification(10, "Load error", "Cannot load settings.", "/errornotification.png");
            });
        } finally {
            saveSettings();
            return isLoaded;
        }
    }

    private static void load() throws IOException, ClassNotFoundException {
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FINAL_FILE_NAME))) {
            dataPersistenceDefaultOption = (DataPersistenceOption) ois.readObject();
            if(dataPersistenceDefaultOption == null) dataPersistenceDefaultOption = DataPersistenceOption.TEXT_FILE;
        } catch (FileNotFoundException ex) {
            dataPersistenceDefaultOption = DataPersistenceOption.TEXT_FILE;
            throw new FileNotFoundException("Settings File (" + FINAL_FILE_NAME + ") doesn't exist.");
        }
    }

    public static void save() throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FINAL_FILE_NAME));
        oos.writeObject(dataPersistenceDefaultOption);
        oos.close();
    }

    public static void setDataPersistenceDefaultOption(DataPersistenceOption dataPersistenceDefaultOption) {
        Settings.dataPersistenceDefaultOption = dataPersistenceDefaultOption;
    }

    public static DataPersistenceOption getDataPersistenceDefaultOption() {
        return dataPersistenceDefaultOption;
    }
}