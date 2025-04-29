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
    MEME_UWU("Meme - Uwu", "src/main/resources/res/sounds/meme_uwu.wav"),
    MEME_BLUE_LOBSTER("Meme - Blue Lobster", "src/main/resources/res/sounds/meme_blue_lobster.wav"),
    MEME_FUS_RO_DAH("Meme - Fus Ro Dah", "src/main/resources/res/sounds/meme_fus_ro_dah.wav"),
    MEME_MANZ("Meme - Manz", "src/main/resources/res/sounds/meme_maanz.wav"),
    MEME_METAL_PIPE("Meme - Metal Pipe", "src/main/resources/res/sounds/meme_metal_pipe.wav"),
    MEME_PERRO_SALCICCIA("Meme - Perro Salciccia", "src/main/resources/res/sounds/meme_perro_salciccia.wav"),
    MEME_SIUM("Meme - Sium", "src/main/resources/res/sounds/meme_sium.wav"),
    MEME_SPIN("Meme - Spin", "src/main/resources/res/sounds/meme_spin.wav"),
    MEME_TO_BE_CONTINUED("Meme - To Be Continued", "src/main/resources/res/sounds/meme_to_be_continued.wav");

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
