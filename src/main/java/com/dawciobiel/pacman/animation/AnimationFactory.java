package com.dawciobiel.pacman.animation;

import javafx.scene.image.Image;
import com.dawciobiel.pacman.assets.AssetManager;

public class AnimationFactory {
    private static final double PACMAN_FRAME_DURATION = 0.1; // 100ms per frame
    
    public static Animation createPacmanWalkingRight() {
        Animation animation = new Animation(PACMAN_FRAME_DURATION, true);
        AssetManager assets = AssetManager.getInstance();
        
        animation.addFrame(assets.getImage("pacman_right_1"));
        animation.addFrame(assets.getImage("pacman_right_2"));
        animation.addFrame(assets.getImage("pacman_right_3"));
        animation.addFrame(assets.getImage("pacman_right_2")); // Powrót do środkowej klatki
        
        return animation;
    }
    
    public static Animation createPacmanWalkingLeft() {
        Animation animation = new Animation(PACMAN_FRAME_DURATION, true);
        AssetManager assets = AssetManager.getInstance();
        
        animation.addFrame(assets.getImage("pacman_left_1"));
        animation.addFrame(assets.getImage("pacman_left_2"));
        animation.addFrame(assets.getImage("pacman_left_3"));
        animation.addFrame(assets.getImage("pacman_left_2")); // Powrót do środkowej klatki
        
        return animation;
    }
    
    public static Animation createPacmanWalkingUp() {
        Animation animation = new Animation(PACMAN_FRAME_DURATION, true);
        AssetManager assets = AssetManager.getInstance();
        
        animation.addFrame(assets.getImage("pacman_up_1"));
        animation.addFrame(assets.getImage("pacman_up_2"));
        animation.addFrame(assets.getImage("pacman_up_3"));
        animation.addFrame(assets.getImage("pacman_up_2")); // Powrót do środkowej klatki
        
        return animation;
    }
    
    public static Animation createPacmanWalkingDown() {
        Animation animation = new Animation(PACMAN_FRAME_DURATION, true);
        AssetManager assets = AssetManager.getInstance();
        
        animation.addFrame(assets.getImage("pacman_down_1"));
        animation.addFrame(assets.getImage("pacman_down_2"));
        animation.addFrame(assets.getImage("pacman_down_3"));
        animation.addFrame(assets.getImage("pacman_down_2")); // Powrót do środkowej klatki
        
        return animation;
    }
    
    public static Animation createPacmanDying() {
        Animation animation = new Animation(PACMAN_FRAME_DURATION, false); // false = nie zapętlaj
        AssetManager assets = AssetManager.getInstance();
        
        animation.addFrame(assets.getImage("pacman_death_1"));
        animation.addFrame(assets.getImage("pacman_death_2"));
        animation.addFrame(assets.getImage("pacman_death_3"));
        animation.addFrame(assets.getImage("pacman_death_4"));
        
        return animation;
    }
}