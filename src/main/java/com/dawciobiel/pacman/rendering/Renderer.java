package com.dawciobiel.pacman.rendering;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class Renderer {
    private GraphicsContext gc;
    
    public void setGraphicsContext(GraphicsContext gc) {
        this.gc = gc;
    }
    
    public void drawSprite(Image sprite, double x, double y) {
        gc.drawImage(sprite, x, y);
    }
    
    public void drawText(String text, double x, double y) {
        gc.fillText(text, x, y);
    }
    
    public void clear(Color color) {
        gc.setFill(color);
        gc.fillRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
    }
}