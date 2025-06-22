package com.dawciobiel.pacman.assets;

import javafx.scene.image.Image;
import java.util.HashMap;
import java.util.Map;

public class AssetManager {
    private static AssetManager instance;
    private final Map<String, Image> images;

    private AssetManager() {
        images = new HashMap<>();
        loadImages();
    }

    public static AssetManager getInstance() {
        if (instance == null) {
            instance = new AssetManager();
        }
        return instance;
    }

    public void loadImages() {
        // Prawo
        loadImage("pacman_right_1", "/sprites/pacman_right_1.png");
        loadImage("pacman_right_2", "/sprites/pacman_right_2.png");
        loadImage("pacman_right_3", "/sprites/pacman_right_3.png");

        // Lewo
        loadImage("pacman_left_1", "/sprites/pacman_left_1.png");
        loadImage("pacman_left_2", "/sprites/pacman_left_2.png");
        loadImage("pacman_left_3", "/sprites/pacman_left_3.png");

        // Góra
        loadImage("pacman_up_1", "/sprites/pacman_up_1.png");
        loadImage("pacman_up_2", "/sprites/pacman_up_2.png");
        loadImage("pacman_up_3", "/sprites/pacman_up_3.png");

        // Dół
        loadImage("pacman_down_1", "/sprites/pacman_down_1.png");
        loadImage("pacman_down_2", "/sprites/pacman_down_2.png");
        loadImage("pacman_down_3", "/sprites/pacman_down_3.png");

        // Śmierć
        loadImage("pacman_death_1", "/sprites/pacman_death_1.png");
        loadImage("pacman_death_2", "/sprites/pacman_death_2.png");
        loadImage("pacman_death_3", "/sprites/pacman_death_3.png");
        loadImage("pacman_death_4", "/sprites/pacman_death_4.png");
    }

    private void loadImage(String name, String path) {
        try {
            Image image = new Image(getClass().getResourceAsStream(path));
            images.put(name, image);
        } catch (Exception e) {
            System.err.println("Nie można załadować obrazu: " + path);
            e.printStackTrace();
        }
    }

    public Image getImage(String name) {
        return images.get(name);
    }
}