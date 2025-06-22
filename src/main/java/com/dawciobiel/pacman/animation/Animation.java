package com.dawciobiel.pacman.animation;

import javafx.scene.image.Image;
import java.util.List;
import java.util.ArrayList;

public class Animation {
    private List<Image> frames;
    private double frameDuration; // czas trwania pojedynczej klatki w sekundach
    private double elapsedTime;
    private int currentFrameIndex;
    private boolean isLooping;

    public Animation(double frameDuration, boolean isLooping) {
        this.frames = new ArrayList<>();
        this.frameDuration = frameDuration;
        this.isLooping = isLooping;
        this.elapsedTime = 0;
        this.currentFrameIndex = 0;
    }

    public void addFrame(Image frame) {
        frames.add(frame);
    }

    public void update(double deltaTime) {
        if (frames.isEmpty()) return;

        elapsedTime += deltaTime;
        
        if (elapsedTime >= frameDuration) {
            elapsedTime = 0;
            currentFrameIndex++;
            
            if (currentFrameIndex >= frames.size()) {
                if (isLooping) {
                    currentFrameIndex = 0;
                } else {
                    currentFrameIndex = frames.size() - 1;
                }
            }
        }
    }

    public void reset() {
        currentFrameIndex = 0;
        elapsedTime = 0;
    }

    public Image getCurrentFrame() {
        if (frames.isEmpty()) return null;
        return frames.get(currentFrameIndex);
    }

    public boolean isFinished() {
        return !isLooping && currentFrameIndex == frames.size() - 1;
    }
}