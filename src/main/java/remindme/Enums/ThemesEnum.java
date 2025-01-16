package remindme.Enums;

public enum ThemesEnum {
    INTELLIJ("Light"),
    DRACULA("Dark"),
    CARBON("Carbon"),
    ARC_ORAGE("Arc - Orange"),
    ARC_DARK_ORANGE("Arc Dark - Orange"),
    CYAN_LIGHT("Cyan light"),
    NORD("Nord"),
    HIGH_CONTRAST("High contrast"),
    SOLARIZED_DARK("Solarized dark"),
    SOLARIZED_LIGHT("Solarized light");

    private final String themeName;

    private ThemesEnum(String themeName) {
        this.themeName = themeName;
    }

    public String getThemeName() {
        return themeName;
    }
}