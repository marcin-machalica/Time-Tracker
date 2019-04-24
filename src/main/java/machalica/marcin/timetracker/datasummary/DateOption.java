package machalica.marcin.timetracker.datasummary;

public enum DateOption {
    LAST_WEEK("Last week"),
    LAST_MONTH("Last month"),
    LAST_YEAR("Last year"),
    ALL("All"),
    BETWEEN("Between");

    private String displayName;

    DateOption(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() { return displayName; }

    @Override public String toString() { return displayName; }
}
