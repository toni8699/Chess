package ui;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

final class SoundManager {

    private static final Logger LOGGER = Logger.getLogger(SoundManager.class.getName());

    private final Clip moveClip;
    private final Clip captureClip;
    private final Clip errorClip;
    private final Clip gameOverClip;

    SoundManager() {
        this.moveClip = loadClip("/res/sound/Move.wav");
        this.captureClip = loadClip("/res/sound/capture.wav");
        this.errorClip = loadClip("/res/sound/Error.wav");
        this.gameOverClip = loadClip("/res/sound/gameover.wav");
    }

    void playMove() {
        play(moveClip);
    }

    void playCapture() {
        play(captureClip != null ? captureClip : moveClip);
    }

    void playError() {
        play(errorClip);
    }

    void playGameOver() {
        play(gameOverClip);
    }

    void resetGameOver() {
        if (gameOverClip != null && gameOverClip.isRunning()) {
            gameOverClip.stop();
        }
    }

    private Clip loadClip(final String resourcePath) {
        final URL url = SoundManager.class.getResource(resourcePath);
        if (url == null) {
            LOGGER.log(Level.WARNING, "Audio resource not found: {0}", resourcePath);
            return null;
        }
        try (AudioInputStream sourceStream = AudioSystem.getAudioInputStream(url)) {
            final AudioFormat baseFormat = sourceStream.getFormat();
            final AudioFormat decodedFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    baseFormat.getSampleRate(),
                    16,
                    baseFormat.getChannels(),
                    baseFormat.getChannels() * 2,
                    baseFormat.getSampleRate(),
                    false);
            try (AudioInputStream decodedStream = AudioSystem.getAudioInputStream(decodedFormat, sourceStream)) {
                final Clip clip = AudioSystem.getClip();
                clip.open(decodedStream);
                return clip;
            }
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
            LOGGER.log(Level.WARNING, "Failed to load audio clip: " + resourcePath, ex);
            return null;
        }
    }

    private void play(final Clip clip) {
        if (clip == null) {
            return;
        }
        if (clip.isRunning()) {
            clip.stop();
        }
        clip.setFramePosition(0);
        clip.start();
    }
}

