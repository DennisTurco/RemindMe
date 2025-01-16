package remindme.Enums;

public enum LanguagesEnum {
    ITA("Italiano", "ita.json"),
    ENG("English", "eng.json"),
    DEU("Deutsch", "deu.json"),
    ESP("Español", "esp.json"),
    FRA("Français", "fra.json");

    private final String languageName;
    private final String fileName;

    private LanguagesEnum(String languageName, String fileName) {
        this.languageName = languageName;
        this.fileName = fileName;
    }

    public String getLanguageName() {
        return languageName;
    }

    public String getFileName() {
        return fileName;
    }
}