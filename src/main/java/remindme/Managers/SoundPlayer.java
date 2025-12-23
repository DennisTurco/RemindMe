package remindme.Managers;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import remindme.Enums.SoundsEnum;

public class SoundPlayer {

    private static final Logger logger = LoggerFactory.getLogger(SoundPlayer.class);

    public static void playSound(SoundsEnum sound) {
        if (sound == SoundsEnum.NO_SOUND)
            return;

        try {
            File soundFile = new File(sound.getSoundPath());
            if (!soundFile.exists()) {
                logger.warn("Audio file with name: " + sound.getSoundPath() + " doesn't exist");
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
            logger.error("Audio format not supported: " + e.getMessage(), e);
        } catch (IOException e) {
            logger.error("I/O error: " + e.getMessage(), e);
        } catch (LineUnavailableException e) {
            logger.error("Audio line unavailable: " + e.getMessage(), e);
        }
    }
}

