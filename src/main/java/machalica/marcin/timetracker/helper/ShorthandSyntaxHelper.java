package machalica.marcin.timetracker.helper;

import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShorthandSyntaxHelper {
    public static void setupShorthandSyntaxListeners(DatePicker dateInput, TextField timeInput, TextField infoInput) {
        dateInput.focusedProperty().addListener(((observable, oldValue, newValue) -> {
            if(!newValue) {
                ShorthandSyntaxHelper.computeDatePicker(dateInput);
            }
        }));

        timeInput.focusedProperty().addListener(((observable, oldValue, newValue) -> {
            if(!newValue) {
                ShorthandSyntaxHelper.computeTimeInput(timeInput);
            }
        }));

        infoInput.focusedProperty().addListener(((observable, oldValue, newValue) -> {
            if(!newValue) {
                ShorthandSyntaxHelper.computeInfoInput(infoInput);
            }
        }));
    }

    public static void setupDateShorthandSyntaxListener(DatePicker dateInput) {
        dateInput.focusedProperty().addListener(((observable, oldValue, newValue) -> {
            if(!newValue) {
                ShorthandSyntaxHelper.computeDatePicker(dateInput);
            }
        }));
    }

    public static void computeActivityInputs(DatePicker dateInput, TextField timeInput, TextField infoInput) {
        computeDatePicker(dateInput);
        computeTimeInput(timeInput);
        computeInfoInput(infoInput);
    }

    public static void computeDatePicker(DatePicker dateInput) {
        Matcher matcher = Pattern.compile("^[yY]+$").matcher(dateInput.getEditor().getText());
        boolean wasFound = matcher.find();
        int count = 0;
        char foundCharacter = '_';

        if (wasFound) {
            count = matcher.group().split("").length;
            foundCharacter = 'y';
        } else {
            matcher = Pattern.compile("^[tT]+$").matcher(dateInput.getEditor().getText());
            wasFound = matcher.find();

            if(wasFound) {
                count = matcher.group().split("").length;
                foundCharacter = 't';
            } else {
                if (dateInput.getEditor().getText().matches("^[dD]+$")) {
                    count = 1;
                    foundCharacter = 'd';
                }
            }
        }
        if (count > 0) {
            long todayInEpochDays = LocalDate.now().toEpochDay();
            long pastDateInEpochDays = todayInEpochDays - count;
            long futureDateInEpochDays = todayInEpochDays + count;
            switch (foundCharacter) {
                case 'y':
                    LocalDate pastLocalDate = LocalDate.ofEpochDay(pastDateInEpochDays);
                    dateInput.setValue(pastLocalDate);
                    break;
                case 'd':
                    LocalDate todayLocalDate = LocalDate.ofEpochDay(todayInEpochDays);
                    dateInput.setValue(todayLocalDate);
                    break;
                case 't':
                    LocalDate futureLocalDate = LocalDate.ofEpochDay(futureDateInEpochDays);
                    dateInput.setValue(futureLocalDate);
                    break;
            }
        }
    }

    public static void computeTimeInput(TextField timeInput) {
        timeInput.setText(timeInput.getText().replaceAll(" +", " ").replaceAll("[^0-9: ]", "").trim());
        String time = "";

        Matcher matcher = Pattern.compile("^([0-1]?[0-9]|2[0-3])$").matcher(timeInput.getText());
        boolean wasFound = matcher.find();

        if (wasFound) {
            if(matcher.group(0).length() == 2 && matcher.group(0).startsWith("0")) {
                time = matcher.group().replaceFirst("0", "") + ":00";
            } else {
                time = matcher.group(0) + ":00";
            }
        } else {
            matcher = Pattern.compile("^(2[4-9]|[3-5][0-9])$").matcher(timeInput.getText());
            wasFound = matcher.find();

            if (wasFound) {
                time = "0:" + matcher.group(0);
            } else {
                matcher = Pattern.compile("^([0-1]?[0-9]|2[0-3]) ([0-5]?[0-9])$").matcher(timeInput.getText());
                wasFound = matcher.find();

                if (wasFound) {
                    String[] timeParts = matcher.group(0).split(" ");
                    String hours = (timeParts[0].length() == 2 && timeParts[0].startsWith("0")) ? timeParts[0].replaceFirst("0", "") : timeParts[0];
                    String minutes = timeParts[1].length() == 1 ? "0" + timeParts[1] : timeParts[1];
                    time = hours + ":" + minutes;
                }
            }
        }

        if(!time.equals("")) {
            timeInput.setText(time);
        }
    }

    public static void computeInfoInput(TextField infoInput) {
        infoInput.setText(infoInput.getText().trim());

        if(infoInput.getText().length() > 0) {
            char firstChar = infoInput.getText().charAt(0);
            if(Character.isLetter(firstChar)) {
                firstChar = Character.toUpperCase(firstChar);

                if(infoInput.getText().length() > 1) {
                    infoInput.setText(firstChar + infoInput.getText().substring(1));
                } else {
                    infoInput.setText(String.valueOf(firstChar));
                }
            }
        }
    }
}