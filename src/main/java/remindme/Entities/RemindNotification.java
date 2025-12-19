package remindme.Entities;

import remindme.Enums.IconsEnum;
import remindme.Enums.SoundsEnum;

public record RemindNotification(
        String name,
        String description,
        IconsEnum icon,
        SoundsEnum sound,
        boolean topLevel
) {
    public RemindNotification(Remind remind) {
        this(
            remind.getName(),
            remind.getDescription(),
            remind.getIcon(),
            remind.getSound(),
            remind.isTopLevel()
        );
    }

    @Override
    public String toString() {
        return String.format(
            "[Name: %s, IconName: %s, SoundName: %s, Description: %s]",
            name,
            icon.getIconName(),
            sound.getSoundName(),
            description.replace("\n", " \\n ")
        );
    }
}
