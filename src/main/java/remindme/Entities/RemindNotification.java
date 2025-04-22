package remindme.Entities;

import remindme.Enums.IconsEnum;
import remindme.Enums.SoundsEnum;

public class RemindNotification {
    private String name;
    private String description;
    private IconsEnum icon;
    private SoundsEnum sound;
    private boolean topLevel;

    public RemindNotification(String name, String description, IconsEnum icon, SoundsEnum sound, boolean topLevel) {
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.sound = sound;
        this.topLevel = topLevel;
    }

    public RemindNotification(Remind remind) {
        this.name = remind.getName();
        this.description = remind.getDescription();
        this.icon = remind.getIcon();
        this.sound = remind.getSound();
        this.topLevel = remind.isTopLevel();
    }

    @Override
    public String toString() {
        return String.format("[Name: %s, IconName: %s, SoundName: %s, Description: %s]",
            this.name,
            this.icon.getIconName(),
            this.sound.getSoundName(),
            this.description.replace("\n", " \\n ")
        );
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public IconsEnum getIcon() {
        return icon;
    }

    public SoundsEnum getSound() {
        return sound;
    }

    public boolean isTopLevel() {
        return topLevel;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setIcon(IconsEnum icon) {
        this.icon = icon;
    }

    public void setSound(SoundsEnum sound) {
        this.sound = sound;
    }

    public void setTopLevel(boolean topLevel) {
        this.topLevel = topLevel;
    }
}
