package remindme.Enums;

public enum SoundsEnum {
    NoSound("No Sound", ""),
    Sound1("Sound 1", "src/main/resources/res/sounds/sound1.wav"),
    Sound2("Sound 2", "src/main/resources/res/sounds/sound2.wav"),
    Sound3("Sound 3", "src/main/resources/res/sounds/sound3.wav"),
    Sound4("Sound 4", "src/main/resources/res/sounds/sound4.wav"),
    Sound5("Sound 5", "src/main/resources/res/sounds/sound5.wav"),
    //Sound6("Sound 6", "src/main/resources/res/sounds/sound6.wav"),
    //Sound7("Sound 7", "src/main/resources/res/sounds/sound7.wav"),
    Sound8("Sound 8", "src/main/resources/res/sounds/sound8.wav"),
    Sound9("Sound 9", "src/main/resources/res/sounds/sound9.wav"),
    //Sound10("Sound 10", "src/main/resources/res/sounds/sound10.wav"),
    Sound11("Sound 11", "src/main/resources/res/sounds/sound11.wav"),
    Sound12("Sound 12", "src/main/resources/res/sounds/sound12.wav"),
    Sound13("Owu Woman Sound", "src/main/resources/res/sounds/UwuSound.wav");

    private final String soundName;
    private final String soundPath;

    private SoundsEnum(String soundName, String soundPath) {
        this.soundName = soundName;
        this.soundPath = soundPath;
    }

    public String getSoundName() {
        return soundName;
    }

    public String getSoundPath() {
        return soundPath;
    }

    public static SoundsEnum getSoundbyName(String soundName) {
        for (SoundsEnum sound : SoundsEnum.values()) {
            if (sound.getSoundName().equals(soundName)) {
                return sound;
            }
        }

        return null;
    }

    public static SoundsEnum getDefaultSound() {
        return NoSound;
    }
}
