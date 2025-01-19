package remindme.Enums;

public enum SoundsEnum {
    NoSound("No Sound", ""),
    Sound1("Sound 1", "res/sounds/sound1.wav"),
    Sound2("Sound 2", "res/sounds/sound2.wav"),
    Sound3("Sound 3", "res/sounds/sound3.mp3"),
    Sound4("Sound 4", "res/sounds/sound4.mp3"),
    Sound5("Sound 5", "res/sounds/sound5.mp3"),
    Sound6("Sound 6", "res/sounds/sound6.wav"),
    Sound7("Sound 7", "res/sounds/sound7.wav"),
    Sound8("Sound 8", "res/sounds/sound8.mp3"),
    Sound9("Sound 9", "res/sounds/sound9.ogg"),
    Sound10("Sound 10", "res/sounds/sound10.wav"),
    Sound11("Sound 11", "res/sounds/sound11.mp3"),
    Sound12("Sound 12", "res/sounds/sound12.wav");

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
}
