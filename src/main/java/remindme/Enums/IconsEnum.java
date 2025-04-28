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
    WORK("Work", "res/img/remind/work.svg"),
    MEME_BABY_YODA("Meme - Baby Yoda", "res/img/remind/meme_baby_yoda.svg"),
    MEME_DOGE("Meme - Doge", "res/img/remind/meme_doge.svg"),
    MEME_FACEPALM("Meme - Facepalm", "res/img/remind/meme_facepalm.svg"),
    MEME_HANDSOME_SQIDWARD("Meme - Handsome Squidward", "res/img/remind/meme_handsome_squidward.svg"),
    MEME_LEONARDO_DICAPRIO("Meme - Leonardo Dicaprio", "res/img/remind/meme_leonardo_dicaprio_laughing.svg"),
    MEME_POLITE_CAT("Meme - Polite Cat", "res/img/remind/meme_polite_cat.svg"),
    MEME_ROLL_SAFE("Meme - Roll Safe", "res/img/remind/meme_roll_safe.svg"),
    MEME_FINE_DOG("Meme - Fine Dog", "res/img/remind/meme_this_is_fine_dog.svg"),
    MEME_LOOK_MONKEY("Meme - Look Monkey", "res/img/remind/meme_lookmonkey.svg"),
    MEME_OLD_MAN("Meme - Old Man", "res/img/remind/meme_old_man.svg"),
    MEME_WOMAN_YELLING("Meme - Woman Yelling", "res/img/remind/meme_woman_yelling.svg"),
    MEME_HOMER_SIMPSON("Meme - Homer Simpson", "res/img/remind/meme_homer_simpson.svg");

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
