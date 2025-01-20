package remindme.Enums;

public enum IconsEnum {
    ALERT("Alert", "res/img/remind/alert.svg"),
    BOOK_CLOSED("Book closed", "res/img/remind/book-closed.svg"),
    BOOK("Book", "res/img/remind/book.svg"),
    EYE_CLOSED("Eye closed", "res/img/remind/eye-closed.svg"),
    EYE("Eye", "res/img/remind/eye.svg"),
    MAN_BEER("Man with a beer", "res/img/remind/man-beer.svg"),
    MAN_CALCULATOR("Man with a calculator", "res/img/remind/man-calculator.svg"),
    MAN_COMPUTER("Man with a computer", "res/img/remind/man-computer.svg"),
    MAN_JOGGING("Man doing jogging", "res/img/remind/man-jogging.svg"),
    MAN_SHOPPING("Man doind shopping", "res/img/remind/man-shopping.svg"),
    MAN_SLEEPING("Man sleeping", "res/img/remind/man-sleeping.svg"),
    MAN_WEARING_TIE("Man wearing a tie", "res/img/remind/man-wearing-tie.svg"),
    MAN_WITH_DIETARY("Man with diet", "res/img/remind/man-with-dietary.svg"),
    MAN_YOGA("Man doind yoga", "res/img/remind/man-yoga.svg"),
    MAN("Man", "res/img/remind/man.svg"),
    MUSIC1("Music 1", "res/img/remind/music1.svg"),
    MUSIC2("Music 2", "res/img/remind/music2.svg"),
    PAUSE_CIRCLE("Pause circle", "res/img/remind/pause-circle.svg"),
    WARNING("Warning", "res/img/remind/warning.svg"),
    WORK("Work", "res/img/remind/work.svg");

    private final String iconName;
    private final String iconPath;

    private IconsEnum(String iconName, String iconPath) {
        this.iconName = iconName;
        this.iconPath = iconPath;
    }

    public String getIconName() {
        return iconName;
    }

    public String getIconPath() {
        return iconPath;
    }

    public static IconsEnum getIconbyName(String iconName) {
        for (IconsEnum icon : IconsEnum.values()) {
            if (icon.getIconName().equals(iconName)) {
                return icon;
            }
        }

        return null;
    }

    public static IconsEnum getDefaultIcon() {
        return ALERT;
    }
}
