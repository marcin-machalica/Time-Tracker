package machalica.marcin.timetracker.helper;

import java.io.*;

public class Settings {
    private static DataPersistenceOption dataPersistenceDefaultOption = DataPersistenceOption.TEXT_FILE;
    private static final String FINAL_FILE_NAME = "time_tracker_settings.ser";

    public static void loadSettings() throws IOException, ClassNotFoundException {
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FINAL_FILE_NAME))) {
            dataPersistenceDefaultOption = (DataPersistenceOption) ois.readObject();
            if(dataPersistenceDefaultOption == null) dataPersistenceDefaultOption = DataPersistenceOption.TEXT_FILE;
        } catch (FileNotFoundException ex) {
            dataPersistenceDefaultOption = DataPersistenceOption.TEXT_FILE;
            throw new FileNotFoundException("Settings File (" + FINAL_FILE_NAME + ") doesn't exist.");
        }
    }

    public static void saveSettings() throws IOException {
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