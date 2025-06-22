package com.dawciobiel.pacman.animation;

import javafx.scene.image.Image;
import java.util.HashMap;
import java.util.Map;

public class AnimationManager {
    private Map<String, Animation> animations;
    private Animation currentAnimation;
    private String currentAnimationName;

    public AnimationManager() {
        animations = new HashMap<>();
    }

    public void addAnimation(String name, Animation animation) {
        animations.put(name, animation);
    }

    public void playAnimation(String name) {
        if (!animations.containsKey(name)) return;

        if (!name.equals(currentAnimationName)) {
            currentAnimation = animations.get(name);
            currentAnimationName = name;
            currentAnimation.reset();
        }
    }

    public void update(double deltaTime) {
        if (currentAnimation != null) {
            currentAnimation.update(deltaTime);
        }
    }

    public Image getCurrentFrame() {
        return currentAnimation != null ? currentAnimation.getCurrentFrame() : null;
    }

    public boolean isCurrentAnimationFinished() {
        return currentAnimation != null && currentAnimation.isFinished();
    }
}
