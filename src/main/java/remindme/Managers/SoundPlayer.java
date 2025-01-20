package remindme.Managers;

import javax.sound.sampled.*;

import remindme.Logger;
import remindme.Enums.SoundsEnum;
import remindme.Logger.LogLevel;

import java.io.File;
import java.io.IOException;

public class SoundPlayer {

    public static void playSound(SoundsEnum sound) {
        if (sound == SoundsEnum.NoSound)
            return;

        try {
            File soundFile = new File(sound.getSoundPath());
            if (!soundFile.exists()) {
                Logger.logMessage("Audio file with name: " + sound.getSoundPath() + " doesn't exist" , LogLevel.WARN);
                return;
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);

            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);

            clip.start();

            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });

        } catch (UnsupportedAudioFileException e) {
            Logger.logMessage("Audio format not supported: " + e.getMessage(), LogLevel.ERROR, e);
        } catch (IOException e) {
            Logger.logMessage("I/O error: " + e.getMessage(), LogLevel.ERROR, e);
        } catch (LineUnavailableException e) {
            Logger.logMessage("Audio line unavailable: " + e.getMessage(), LogLevel.ERROR, e);
        }
    }
}

