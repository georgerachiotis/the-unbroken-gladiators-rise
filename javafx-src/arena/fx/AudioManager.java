package arena.fx;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.AudioClip;
import javafx.util.Duration;

import java.net.URL;
import java.util.EnumMap;
import java.util.Map;
import java.util.prefs.Preferences;

/** Keeps background music in one place and switches themes without restarting them unnecessarily. */
final class AudioManager {

    private static final Preferences SETTINGS = Preferences.userNodeForPackage(AudioManager.class);

    enum Theme {
        LUDUS("assets/audio/music/ludus-dark-gladiator.mp3"),
        ARENA("assets/audio/music/arena-epic-drums.mp3"),
        CHAMPION("assets/audio/music/champion-great-arena.mp3");

        private final String resource;

        Theme(String resource) {
            this.resource = resource;
        }
    }

    enum Effect {
        WEAPON_IMPACT("assets/audio/effects/weapon-impact.mp3", 0.82),
        CROWD_CHEER("assets/audio/effects/crowd-cheer.mp3", 0.65),
        POISON("assets/audio/effects/poison.mp3", 0.55),
        VICTORY("assets/audio/effects/victory.mp3", 0.72),
        DEFEAT("assets/audio/effects/defeat.mp3", 0.76);

        private final String resource;
        private final double gain;

        Effect(String resource, double gain) {
            this.resource = resource;
            this.gain = gain;
        }
    }

    private MediaPlayer musicPlayer;
    private Theme currentTheme;
    private double musicVolume = clamp(SETTINGS.getDouble("musicVolume", 0.38));
    private double effectsVolume = clamp(SETTINGS.getDouble("effectsVolume", 0.70));
    private boolean muted = SETTINGS.getBoolean("muted", false);
    private final Map<Effect, AudioClip> effectClips = new EnumMap<>(Effect.class);

    void playTheme(Theme theme) {
        if (theme == null || theme == currentTheme) return;

        URL resource = AudioManager.class.getResource(theme.resource);
        if (resource == null) {
            System.err.println("Missing audio resource: " + theme.resource);
            return;
        }

        MediaPlayer nextPlayer = new MediaPlayer(new Media(resource.toExternalForm()));
        nextPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        nextPlayer.setVolume(0.0);
        nextPlayer.play();

        MediaPlayer previousPlayer = musicPlayer;
        musicPlayer = nextPlayer;
        currentTheme = theme;

        final int steps = 12;
        Timeline crossfade = new Timeline();
        for (int step = 0; step <= steps; step++) {
            final double progress = step / (double) steps;
            crossfade.getKeyFrames().add(new KeyFrame(Duration.millis(step * 45.0), event -> {
                nextPlayer.setVolume(effectiveMusicVolume() * progress);
                if (previousPlayer != null) {
                    previousPlayer.setVolume(effectiveMusicVolume() * (1.0 - progress));
                }
            }));
        }
        crossfade.setOnFinished(event -> {
            if (previousPlayer != null) {
                previousPlayer.stop();
                previousPlayer.dispose();
            }
        });
        crossfade.play();
    }

    double getMusicVolume() {
        return musicVolume;
    }

    void playEffect(Effect effect) {
        if (effect == null || muted || effectsVolume <= 0.0) return;
        AudioClip clip = effectClips.computeIfAbsent(effect, this::loadEffect);
        if (clip != null) clip.play(effectsVolume * effect.gain);
    }

    void setMusicVolume(double musicVolume) {
        this.musicVolume = clamp(musicVolume);
        SETTINGS.putDouble("musicVolume", this.musicVolume);
        applyMusicVolume();
    }

    double getEffectsVolume() {
        return effectsVolume;
    }

    void setEffectsVolume(double effectsVolume) {
        this.effectsVolume = clamp(effectsVolume);
        SETTINGS.putDouble("effectsVolume", this.effectsVolume);
    }

    boolean isMuted() {
        return muted;
    }

    void setMuted(boolean muted) {
        this.muted = muted;
        SETTINGS.putBoolean("muted", muted);
        applyMusicVolume();
    }

    void dispose() {
        if (musicPlayer != null) {
            musicPlayer.stop();
            musicPlayer.dispose();
            musicPlayer = null;
        }
        effectClips.values().forEach(AudioClip::stop);
        effectClips.clear();
    }

    private AudioClip loadEffect(Effect effect) {
        URL resource = AudioManager.class.getResource(effect.resource);
        if (resource == null) {
            System.err.println("Missing audio resource: " + effect.resource);
            return null;
        }
        return new AudioClip(resource.toExternalForm());
    }

    private void applyMusicVolume() {
        if (musicPlayer != null) musicPlayer.setVolume(effectiveMusicVolume());
    }

    private double effectiveMusicVolume() {
        return muted ? 0.0 : musicVolume;
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
