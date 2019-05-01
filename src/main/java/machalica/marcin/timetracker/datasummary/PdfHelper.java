package machalica.marcin.timetracker.datasummary;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.sun.istack.internal.Nullable;
import javafx.collections.ObservableList;
import machalica.marcin.timetracker.helper.ActivityHelper;
import machalica.marcin.timetracker.helper.DateHelper;
import machalica.marcin.timetracker.model.Activity;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

public class PdfHelper {
    public static String generatePdf(ObservableList<Activity> activities, DateOption dateOption, @Nullable LocalDate startLocalDate, @Nullable LocalDate endLocalDate) throws FileNotFoundException, DocumentException {
        String pdfName = generatePdfFilename(activities, dateOption, startLocalDate, endLocalDate) + ".pdf";

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(pdfName));
        document.open();

        Font headerFont = FontFactory.getFont(FontFactory.COURIER, 32, BaseColor.BLACK);

        Chunk headerChunk = new Chunk("Activity report", headerFont);
        Paragraph headerParagraph = new Paragraph(headerChunk);
        headerParagraph.setAlignment(Paragraph.ALIGN_CENTER);
        headerParagraph.setSpacingAfter(30);
        document.add(headerParagraph);

        Chunk dateOptionChunk = new Chunk("Date option: " + dateOption);
        Paragraph dateOptionParagraph = new Paragraph(dateOptionChunk);
        dateOptionParagraph.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(dateOptionParagraph);

        document.add(Chunk.NEWLINE);

        Chunk periodChunk = new Chunk("Period: " + getDataSummaryPeriodAsString(activities, dateOption, startLocalDate, endLocalDate));
        Paragraph periodParagraph = new Paragraph(periodChunk);
        periodParagraph.setAlignment(Paragraph.ALIGN_CENTER);
        periodParagraph.setSpacingAfter(30);
        document.add(periodParagraph);

        PdfPTable sumTable = new PdfPTable(5);
        addTableHeader(sumTable, new String[]{"Days", "Active days", "Time", "Time / day", "Time / active day"});
        addSumRows(sumTable, activities, dateOption, startLocalDate, endLocalDate);
        document.add(sumTable);

        document.add(Chunk.NEWLINE);

        PdfPTable dataTable = new PdfPTable(3);
        addTableHeader(dataTable, new String[]{"Date", "Time", "Info"});
        addActivityRows(dataTable, activities);
        document.add(dataTable);

        document.close();
        return pdfName;
    }

    private static String getDataSummaryPeriodAsString(ObservableList<Activity> activities, DateOption dateOption, @Nullable LocalDate startLocalDate, @Nullable LocalDate endLocalDate) {
        String period;

        switch (dateOption) {
            case BETWEEN:
                if (startLocalDate != null && endLocalDate != null) {
                    period = DateHelper.getFormattedLocalDate(startLocalDate) + " - " + DateHelper.getFormattedLocalDate(endLocalDate);
                } else if (startLocalDate != null) {
                    period = DateHelper.getFormattedLocalDate(startLocalDate) + " - " + DateHelper.getFormattedLocalDate(DateHelper.getCurrentDate());
                } else if (endLocalDate != null) {
                    period = "Before " + DateHelper.getFormattedLocalDate(endLocalDate);
                } else {
                    period = "Before " + DateHelper.getFormattedLocalDate(DateHelper.getCurrentDate());
                }
                break;
            case ALL:
                period = "Before " + DateHelper.getFormattedLocalDate(DateHelper.getCurrentDate());
                break;
            case LAST_WEEK:
                period = DateHelper.getFormattedLocalDate(DateHelper.getDateMinusWeeks(1)) + " - " + DateHelper.getFormattedLocalDate(DateHelper.getCurrentDate());
                break;
            case LAST_MONTH:
                period = DateHelper.getFormattedLocalDate(DateHelper.getDateMinusMonths(1)) + " - " + DateHelper.getFormattedLocalDate(DateHelper.getCurrentDate());
                break;
            case LAST_YEAR:
                period = DateHelper.getFormattedLocalDate(DateHelper.getDateMinusYears(1)) + " - " + DateHelper.getFormattedLocalDate(DateHelper.getCurrentDate());
                break;
            default:
                period = "";
                break;

        }
        return period;
    }

    private static String generatePdfFilename(ObservableList<Activity> activities, DateOption dateOption, @Nullable LocalDate startLocalDate, @Nullable LocalDate endLocalDate)  {
        if (dateOption.equals(DateOption.BETWEEN)) {
            if (startLocalDate != null && endLocalDate != null) {
                return LocalDate.now() + "_" + dateOption + "_" + startLocalDate + "_and_" + endLocalDate;
            } else if (startLocalDate != null) {
                return LocalDate.now() + "_" + dateOption + "_start_" + startLocalDate;
            } else if (endLocalDate != null) {
                return LocalDate.now() + "_" + dateOption + "_end_" + endLocalDate;
            } else {
                return LocalDate.now() + "_" + dateOption;
            }
        } else {
            return LocalDate.now() + "_" + dateOption.toString().replaceAll(" ", "_");
        }
    }

    private static void addTableHeader(PdfPTable table, String[] columnNames) {
        Stream.of(columnNames)
                .forEach(columnTitle -> {
                    PdfPCell header = new PdfPCell();
                    header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    header.setHorizontalAlignment(Element.ALIGN_CENTER);
                    header.setPhrase(new Phrase(columnTitle));
                    table.addCell(header);
                });
    }

    private static void addActivityRows(PdfPTable table, ObservableList<Activity> activities) {
        for (Activity activity : activities) {
            table.addCell(activity.getFormattedDateString());
            table.addCell(activity.getTime());
            table.addCell(activity.getInfo());
        }
    }

    private static void addSumRows(PdfPTable table, ObservableList<Activity> activities, DateOption dateOption, @Nullable LocalDate startLocalDate, @Nullable LocalDate endLocalDate) {
        /* days */
        long days = getDaysBetweenDates(activities, dateOption, startLocalDate, endLocalDate);
        addCenteredCell(table, Long.toString(days));

        /* active days */
        addCenteredCell(table, Integer.toString(activities.size()));

        /* time */
        long hours = 0;
        long minutes = 0;

        for (Activity activity : activities) {
            hours += ActivityHelper.getActivityHours(activity);
            minutes += ActivityHelper.getActivityMinutes(activity);
        }

        hours += minutes / 60;
        minutes = minutes % 60;
        addCenteredCell(table, hours + "h " + minutes + "min");

        /* time / day */
        double avgHoursAndMinsInMs = (hours * 60 * 1000 + minutes * 1000) / (double) getDaysBetweenDates(activities, dateOption, startLocalDate, endLocalDate);
        long avgHours = (long) avgHoursAndMinsInMs / 1000 / 60;
        long avgMinutes = Math.round(((avgHoursAndMinsInMs / 1000 / 60) - avgHours) * 60);
        addCenteredCell(table, avgHours + "h " + avgMinutes + "min");

        /* time / active day */
        avgHoursAndMinsInMs = (hours * 60 * 1000 + minutes * 1000) / (double) activities.size();
        avgHours = (long) avgHoursAndMinsInMs / 1000 / 60;
        avgMinutes = Math.round(((avgHoursAndMinsInMs / 1000 / 60) - avgHours) * 60);
        addCenteredCell(table, avgHours + "h " + avgMinutes + "min");
    }

    private static void addCenteredCell(PdfPTable table, String phrase) {
        PdfPCell cell = new PdfPCell();
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPhrase(new Phrase(phrase));
        table.addCell(cell);
    }

    private static long getDaysBetweenDates(ObservableList<Activity> activities, DateOption dateOption, @Nullable LocalDate startLocalDate, @Nullable LocalDate endLocalDate) {
        long days = 0;

        switch (dateOption) {
            case LAST_WEEK:
                days = 7;
                break;

            case LAST_MONTH:
                days = ChronoUnit.DAYS.between(DateHelper.getDateMinusMonths(1), DateHelper.getCurrentDate());
                break;

            case LAST_YEAR:
                days = ChronoUnit.DAYS.between(DateHelper.getDateMinusYears(1), DateHelper.getCurrentDate());
                break;

            case ALL:
                if(activities.isEmpty()) {
                    days = 0;
                } else if(activities.size() == 1) {
                    days = 1;
                } else {
                    days = ChronoUnit.DAYS.between(activities.get(0).getDate().toLocalDate(), activities.get(activities.size() - 1).getDate().toLocalDate());
                }
                break;

            case BETWEEN:
                if (startLocalDate != null && endLocalDate != null) {
                    days = ChronoUnit.DAYS.between(startLocalDate, endLocalDate);
                } else if (startLocalDate != null) {
                    days = ChronoUnit.DAYS.between(startLocalDate, DateHelper.getCurrentDate());
                } else if (endLocalDate != null) {
                    if(activities.isEmpty()) {
                        days = 0;
                    } else {
                        days = ChronoUnit.DAYS.between(activities.get(0).getDate().toLocalDate(), endLocalDate);
                    }
                } else {
                    if(activities.isEmpty()) {
                        days = 0;
                    } else if(activities.size() == 1) {
                        days = 1;
                    } else {
                        days = ChronoUnit.DAYS.between(activities.get(0).getDate().toLocalDate(), activities.get(activities.size() - 1).getDate().toLocalDate());
                    }
                }
                break;
        }
        return days;
    }
}
