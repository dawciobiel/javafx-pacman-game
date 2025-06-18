package com.dawciobiel.pacman.sound;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SoundManager {
    public static final double SOUND_VOLUME_LEVEL = 0.5;
    private static SoundManager instance;
    private final Map<String, MediaPlayer> sounds = new HashMap<>();
    private boolean soundEnabled = true;

    private SoundManager() {
        initializeSounds();
    }

    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    private void initializeSounds() {
        try {
            // Próbuj załadować pliki audio, jeśli nie ma — użyj system beep
            loadSound("chomp", "chomp.wav");
            loadSound("power_pellet", "power_pellet.wav");
            loadSound("ghost_eaten", "ghost_eaten.wav");
            loadSound("death", "death.wav");
            loadSound("game_over", "game_over.wav");
            loadSound("level_complete", "level_complete.wav");
        } catch (Exception e) {
            System.err.println("Error during the initialization of sounds: " + e.getMessage());
            System.out.println("I use the beep system as fallback");
        }
    }

    private void loadSound(String soundName, String fileName) {
        try {
            // Spróbuj załadować plik audio z folderu resources/sounds/
            URL soundPath = getClass().getResource("/sounds/" + fileName);
            if (soundPath != null) {
                Media media = new Media(soundPath.toString());
                MediaPlayer player = new MediaPlayer(media);
                player.setVolume(SOUND_VOLUME_LEVEL);
                sounds.put(soundName, player);
                System.out.println("Sound loaded: " + fileName);
            } else {
                System.out.println("No file found: " + fileName + " - I use the beep system");
            }
        } catch (Exception e) {
            System.err.println("No sound can be loaded from: " + fileName + " - " + e.getMessage());
        }
    }

    public void playSound(String soundName) {
        if (!soundEnabled) return;

        // Najpierw spróbuj odtworzyć załadowany plik audio
        MediaPlayer player = sounds.get(soundName);
        if (player != null) {
            try {
                player.stop(); // Stop — jeśli już się odtwarza
                player.seek(Duration.ZERO); // Przewiń na początek
                player.play();
                return; // Jeśli się udało, kończymy
            } catch (Exception e) {
                System.err.println("Sound [ '" + soundName + "' ] Playback error. " + e.getMessage());
            }
        }

        // Fallback — użyj programatycznych dźwięków
        switch (soundName) {
            case "chomp" -> playChomp();
            case "power_pellet" -> playPowerPellet();
            case "ghost_eaten" -> playGhostEaten();
            case "death" -> playDeath();
            case "game_over" -> playGameOver();
            case "level_complete" -> playLevelComplete();
        }
    }

    private void playChomp() {
        // Używamy AudioClip dla lepszej kontroli dźwięku
        playTone(800, 100); // 800Hz, 100ms
    }

    private void playPowerPellet() {
        // Sekwencja tonów dla power pellet
        new Thread(() -> {
            try {
                playTone(400, 200);
                Thread.sleep(50);
                playTone(600, 200);
                Thread.sleep(50);
                playTone(800, 300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void playGhostEaten() {
        // Rosnąca sekwencja dla zjedzenia ducha
        new Thread(() -> {
            try {
                int[] frequencies = {400, 500, 600, 700, 800};
                for (int freq : frequencies) {
                    playTone(freq, 80);
                    Thread.sleep(20);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void playDeath() {
        // Opadająca sekwencja dla śmierci
        new Thread(() -> {
            try {
                int[] frequencies = {800, 600, 400, 200};
                for (int freq : frequencies) {
                    playTone(freq, 250);
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void playGameOver() {
        // Dramatyczna sekwencja game over
        new Thread(() -> {
            try {
                playTone(300, 500);
                Thread.sleep(200);
                playTone(200, 800);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void playLevelComplete() {
        // Wesoła melodyjka dla ukończenia poziomu
        new Thread(() -> {
            try {
                int[] melody = {523, 587, 659, 698, 784}; // C, D, E, F, G
                for (int note : melody) {
                    playTone(note, 200);
                    Thread.sleep(50);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void playTone(int frequency, int durationMs) {
        try {
            // Fallback do system beep — jeśli nie możemy wygenerować tonu
            java.awt.Toolkit.getDefaultToolkit().beep();

        } catch (Exception e) {
            // Jeśli system beep nie działa, po prostu ignorujemy
            System.out.println("The sound cannot be played: " + e.getMessage());
        }
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }

    public void cleanup() {
        for (MediaPlayer player : sounds.values()) {
            if (player != null) {
                player.dispose();
            }
        }
        sounds.clear();
    }
}